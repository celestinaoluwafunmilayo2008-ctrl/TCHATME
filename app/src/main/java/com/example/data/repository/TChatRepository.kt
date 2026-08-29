package com.example.data.repository

import android.content.Context
import com.example.data.contacts.ContactSyncHelper
import com.example.data.contacts.PhoneContactItem
import com.example.data.db.TChatDao
import com.example.data.model.CallDirection
import com.example.data.model.CallRecordEntity
import com.example.data.model.CallType
import com.example.data.model.CallWithContact
import com.example.data.model.ChatEntity
import com.example.data.model.ChatWithContact
import com.example.data.model.ContactEntity
import com.example.data.model.MessageEntity
import com.example.data.model.MessageStatus
import com.example.data.model.MessageType
import com.example.data.model.StatusStoryEntity
import com.example.data.model.StatusWithContact
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TChatRepository(
    private val dao: TChatDao,
    private val context: Context
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        scope.launch {
            val contacts = dao.getAllContacts().first()
            if (contacts.isEmpty()) {
                ContactSyncHelper.seedInitialDataIfEmpty(dao)
            }
        }
    }

    // Combine Chats with their Contact details
    val chatsWithContacts: Flow<List<ChatWithContact>> = combine(
        dao.getAllChats(),
        dao.getAllContacts()
    ) { chats, contacts ->
        val contactMap = contacts.associateBy { it.id }
        chats.mapNotNull { chat ->
            val contact = contactMap[chat.contactId]
            if (contact != null) ChatWithContact(chat, contact) else null
        }
    }

    // Contacts
    val allContacts: Flow<List<ContactEntity>> = dao.getAllContacts()

    // Calls with Contact details
    val callsWithContacts: Flow<List<CallWithContact>> = combine(
        dao.getAllCallRecords(),
        dao.getAllContacts()
    ) { calls, contacts ->
        val contactMap = contacts.associateBy { it.id }
        calls.mapNotNull { call ->
            val contact = contactMap[call.contactId]
            if (contact != null) CallWithContact(call, contact) else null
        }
    }

    // Status stories with Contact details
    val statusStoriesWithContacts: Flow<List<StatusWithContact>> = combine(
        dao.getAllStatusStories(),
        dao.getAllContacts()
    ) { statuses, contacts ->
        val contactMap = contacts.associateBy { it.id }
        val myContact = ContactEntity(
            id = 0,
            name = "My Status",
            phoneNumber = "+1 (555) 000-0000",
            about = "Tap to add status update"
        )
        statuses.map { status ->
            val contact = if (status.contactId == 0L) myContact else (contactMap[status.contactId] ?: myContact)
            StatusWithContact(status, contact)
        }
    }

    fun getMessagesForChat(chatId: Long): Flow<List<MessageEntity>> = dao.getMessagesForChat(chatId)

    suspend fun getContact(contactId: Long): ContactEntity? = dao.getContactById(contactId)

    suspend fun getChat(chatId: Long): ChatEntity? = dao.getChatById(chatId)

    suspend fun getOrCreateChatForContact(contactId: Long): Long {
        val existing = dao.getChatByContactId(contactId)
        if (existing != null) return existing.chatId
        val contact = dao.getContactById(contactId)
        val newChat = ChatEntity(
            contactId = contactId,
            lastMessageText = contact?.about ?: "Tap to start conversation",
            lastMessageTimestamp = System.currentTimeMillis()
        )
        return dao.insertChat(newChat)
    }

    suspend fun markChatAsRead(chatId: Long) {
        dao.markChatAsRead(chatId)
    }

    suspend fun togglePinChat(chatId: Long) {
        dao.togglePinChat(chatId)
    }

    suspend fun deleteChat(chatId: Long) {
        dao.clearMessagesForChat(chatId)
        dao.deleteChat(chatId)
    }

    suspend fun sendMessage(
        chatId: Long,
        contactId: Long,
        content: String,
        type: MessageType = MessageType.TEXT,
        mediaUri: String? = null,
        durationSec: Int = 0,
        replyToMessageId: Long? = null
    ) {
        val now = System.currentTimeMillis()
        val msg = MessageEntity(
            chatId = chatId,
            senderId = "me",
            content = content,
            messageType = type,
            mediaUri = mediaUri,
            durationSec = durationSec,
            timestamp = now,
            status = MessageStatus.SENT,
            replyToMessageId = replyToMessageId
        )
        dao.insertMessage(msg)

        val chat = dao.getChatById(chatId)
        if (chat != null) {
            val previewText = when (type) {
                MessageType.VOICE_NOTE -> "🎤 Voice message (${durationSec}s)"
                MessageType.IMAGE -> "📷 Photo"
                MessageType.LOCATION -> "📍 Location"
                MessageType.CONTACT_CARD -> "👤 Contact Card"
                else -> content
            }
            dao.updateChat(
                chat.copy(
                    lastMessageText = previewText,
                    lastMessageType = type,
                    lastMessageTimestamp = now
                )
            )
        }

        // Trigger intelligent friend auto-response simulation after slight delay
        scope.launch {
            delay(1500)
            generateSimulatedFriendReply(chatId, contactId, content, type)
        }
    }

    private suspend fun generateSimulatedFriendReply(
        chatId: Long,
        contactId: Long,
        userPrompt: String,
        type: MessageType
    ) {
        val contact = dao.getContactById(contactId) ?: return

        // Update contact last seen to "typing..."
        dao.updateContact(contact.copy(lastSeen = "typing..."))
        delay(1800)

        val replyText = when {
            userPrompt.contains("call", ignoreCase = true) || userPrompt.contains("video", ignoreCase = true) ->
                "I'm ready! Hit the video call button on the top right whenever you want 📹📞"
            userPrompt.contains("hello", ignoreCase = true) || userPrompt.contains("hey", ignoreCase = true) || userPrompt.contains("hi", ignoreCase = true) ->
                "Hey there! How's your day going? Enjoying TChatMe? ✨"
            userPrompt.contains("voice", ignoreCase = true) || userPrompt.contains("audio", ignoreCase = true) ->
                "Voice notes sound super crisp on here! Try holding the mic icon to record one! 🎙️"
            type == MessageType.VOICE_NOTE ->
                "Got your voice note! Loud and clear! 🎧"
            type == MessageType.IMAGE ->
                "Nice photo! 📸 Love the quality!"
            userPrompt.contains("contact", ignoreCase = true) || userPrompt.contains("phone", ignoreCase = true) ->
                "You can also import everyone directly from your phone address book! Tap the green chat FAB!"
            else -> {
                val replies = listOf(
                    "Got it! Thanks for letting me know 😊",
                    "Sounds awesome! Let's talk more soon 🚀",
                    "That is great news! I love this new TChatMe interface.",
                    "Haha absolutely! 😂",
                    "Perfect! Catch you in a bit! 👋"
                )
                replies.random()
            }
        }

        val replyTime = System.currentTimeMillis()
        dao.insertMessage(
            MessageEntity(
                chatId = chatId,
                senderId = contactId.toString(),
                content = replyText,
                timestamp = replyTime,
                status = MessageStatus.DELIVERED
            )
        )

        val updatedChat = dao.getChatById(chatId)
        if (updatedChat != null) {
            dao.updateChat(
                updatedChat.copy(
                    lastMessageText = replyText,
                    lastMessageType = MessageType.TEXT,
                    lastMessageTimestamp = replyTime,
                    unreadCount = updatedChat.unreadCount + 1
                )
            )
        }

        // Reset contact status to online
        dao.updateContact(contact.copy(lastSeen = "online"))
    }

    val starredMessages: Flow<List<MessageEntity>> = dao.getStarredMessages()

    suspend fun setMessageReaction(messageId: Long, reaction: String?) {
        dao.setMessageReaction(messageId, reaction)
    }

    suspend fun toggleStarMessage(messageId: Long) {
        dao.toggleStarMessage(messageId)
    }

    suspend fun deleteMessage(messageId: Long) {
        dao.deleteMessage(messageId)
    }

    suspend fun setDisappearingTimer(chatId: Long, timer: String) {
        dao.setDisappearingTimer(chatId, timer)
    }

    suspend fun setChatArchived(chatId: Long, isArchived: Boolean) {
        dao.setChatArchived(chatId, isArchived)
    }

    suspend fun createGroup(
        name: String,
        selectedMemberIds: List<Long>,
        description: String = "TChatMe WhatsApp Group"
    ): Long {
        val groupContact = ContactEntity(
            name = name,
            phoneNumber = "Group • ${selectedMemberIds.size + 1} participants",
            about = description,
            avatarColorHex = 0xFF075E54,
            isGroup = true,
            groupMembersCount = selectedMemberIds.size + 1,
            groupAdmin = "You",
            isOnline = true,
            lastSeen = "You created group \"$name\""
        )
        val contactId = dao.insertContact(groupContact)
        val now = System.currentTimeMillis()
        val chatId = dao.insertChat(
            ChatEntity(
                contactId = contactId,
                lastMessageText = "You created group \"$name\"",
                lastMessageType = MessageType.TEXT,
                lastMessageTimestamp = now
            )
        )
        dao.insertMessage(
            MessageEntity(
                chatId = chatId,
                senderId = "me",
                content = "You created group \"$name\" with ${selectedMemberIds.size + 1} members. Messages and calls are end-to-end encrypted.",
                timestamp = now,
                status = MessageStatus.READ
            )
        )

        // Seed simulated member response in group
        scope.launch {
            delay(2000)
            dao.insertMessage(
                MessageEntity(
                    chatId = chatId,
                    senderId = selectedMemberIds.firstOrNull()?.toString() ?: "1",
                    content = "Hey everyone! Happy to join this group! 🎉",
                    timestamp = System.currentTimeMillis(),
                    status = MessageStatus.DELIVERED
                )
            )
        }

        return chatId
    }

    suspend fun sendPoll(
        chatId: Long,
        contactId: Long,
        question: String,
        options: List<String>
    ) {
        val now = System.currentTimeMillis()
        val optionsJoined = options.joinToString("||")
        val initialVotes = options.map { 0 }.joinToString(",")
        val msg = MessageEntity(
            chatId = chatId,
            senderId = "me",
            content = question,
            messageType = MessageType.POLL,
            pollQuestion = question,
            pollOptions = optionsJoined,
            pollVotes = initialVotes,
            timestamp = now,
            status = MessageStatus.SENT
        )
        dao.insertMessage(msg)
        val chat = dao.getChatById(chatId)
        if (chat != null) {
            dao.updateChat(
                chat.copy(
                    lastMessageText = "📊 Poll: $question",
                    lastMessageType = MessageType.POLL,
                    lastMessageTimestamp = now
                )
            )
        }
    }

    suspend fun votePoll(messageId: Long, currentVotes: String, optionIndex: Int, totalOptions: Int) {
        val votesList = if (currentVotes.isBlank()) {
            MutableList(totalOptions) { 0 }
        } else {
            val list = currentVotes.split(",").mapNotNull { it.toIntOrNull() }.toMutableList()
            while (list.size < totalOptions) list.add(0)
            list
        }
        if (optionIndex in votesList.indices) {
            votesList[optionIndex] = votesList[optionIndex] + 1
        }
        dao.updatePollVotes(messageId, votesList.joinToString(","))
    }

    suspend fun recordCall(
        contactId: Long,
        callType: CallType,
        direction: CallDirection,
        durationSeconds: Int
    ) {
        dao.insertCallRecord(
            CallRecordEntity(
                contactId = contactId,
                callType = callType,
                callDirection = direction,
                durationSeconds = durationSeconds,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun addStatusStory(textCaption: String, backgroundColorHex: Long) {
        dao.insertStatusStory(
            StatusStoryEntity(
                contactId = 0L,
                textCaption = textCaption,
                backgroundColorHex = backgroundColorHex,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun markStatusViewed(statusId: Long) {
        dao.markStatusViewed(statusId)
    }

    suspend fun addContact(
        name: String,
        phoneNumber: String,
        about: String = "Hey there! I am using TChatMe.",
        isRegistered: Boolean = true
    ): Long {
        val colorPalettes = listOf(
            0xFF00A884, 0xFF128C7E, 0xFF075E54, 0xFF25D366,
            0xFF34B7F1, 0xFFE91E63, 0xFF9C27B0, 0xFFFF9800
        )
        val newContact = ContactEntity(
            name = name,
            phoneNumber = phoneNumber,
            about = about,
            avatarColorHex = colorPalettes.random(),
            isRegisteredOnTChatMe = isRegistered,
            isOnline = isRegistered,
            lastSeen = if (isRegistered) "online" else "Invited"
        )
        return dao.insertContact(newContact)
    }

    suspend fun syncDevicePhoneContacts(items: List<PhoneContactItem>): Int {
        var addedCount = 0
        val existingContacts = dao.getAllContacts().first()
        val existingPhones = existingContacts.map { it.phoneNumber.replace("\\D".toRegex(), "") }.toSet()

        val colorPalettes = listOf(
            0xFF00A884, 0xFF128C7E, 0xFF075E54, 0xFF25D366,
            0xFF34B7F1, 0xFFE91E63, 0xFF9C27B0, 0xFFFF9800
        )

        for (item in items) {
            val cleanPhone = item.phoneNumber.replace("\\D".toRegex(), "")
            if (cleanPhone.isNotEmpty() && !existingPhones.contains(cleanPhone)) {
                val newContactId = dao.insertContact(
                    ContactEntity(
                        name = item.name,
                        phoneNumber = item.phoneNumber,
                        avatarColorHex = colorPalettes.random(),
                        about = "📱 Added from phone contacts • Available on TChatMe",
                        isRegisteredOnTChatMe = true,
                        isOnline = true,
                        lastSeen = "online"
                    )
                )
                addedCount++
            }
        }
        return addedCount
    }

    suspend fun clearCallLogs() {
        dao.clearAllCallRecords()
    }
}
