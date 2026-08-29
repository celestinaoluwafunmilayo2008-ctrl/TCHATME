package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MessageType {
    TEXT,
    VOICE_NOTE,
    IMAGE,
    LOCATION,
    CALL_LOG,
    CONTACT_CARD,
    DOCUMENT,
    POLL
}

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ
}

enum class CallType {
    VOICE,
    VIDEO
}

enum class CallDirection {
    INCOMING,
    OUTGOING,
    MISSED
}

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val avatarRes: Int? = null,
    val avatarColorHex: Long = 0xFF00A884,
    val about: String = "Hey there! I am using TChatMe.",
    val isRegisteredOnTChatMe: Boolean = true,
    val isOnline: Boolean = false,
    val lastSeen: String = "online",
    val isStarred: Boolean = false,
    val customStatusText: String? = null,
    val isGroup: Boolean = false,
    val groupMembersCount: Int = 0,
    val groupAdmin: String = "You"
)

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey(autoGenerate = true)
    val chatId: Long = 0,
    val contactId: Long,
    val lastMessageText: String = "",
    val lastMessageType: MessageType = MessageType.TEXT,
    val lastMessageTimestamp: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val isArchived: Boolean = false,
    val disappearingTimer: String = "Off",
    val draft: String = ""
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val messageId: Long = 0,
    val chatId: Long,
    val senderId: String, // "me" or contactId string
    val messageType: MessageType = MessageType.TEXT,
    val content: String,
    val mediaUri: String? = null,
    val durationSec: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val status: MessageStatus = MessageStatus.READ,
    val reaction: String? = null,
    val isStarred: Boolean = false,
    val replyToMessageId: Long? = null,
    val replyToText: String? = null,
    val replyToSender: String? = null,
    val pollQuestion: String? = null,
    val pollOptions: String? = null,
    val pollVotes: String? = null
)

@Entity(tableName = "call_records")
data class CallRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val callId: Long = 0,
    val contactId: Long,
    val callType: CallType = CallType.VOICE,
    val callDirection: CallDirection = CallDirection.OUTGOING,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0
)

@Entity(tableName = "status_stories")
data class StatusStoryEntity(
    @PrimaryKey(autoGenerate = true)
    val statusId: Long = 0,
    val contactId: Long, // 0 for current user
    val textCaption: String,
    val backgroundColorHex: Long = 0xFF075E54,
    val timestamp: Long = System.currentTimeMillis(),
    val isViewed: Boolean = false
)

data class ChatWithContact(
    val chat: ChatEntity,
    val contact: ContactEntity
)

data class CallWithContact(
    val call: CallRecordEntity,
    val contact: ContactEntity
)

data class StatusWithContact(
    val status: StatusStoryEntity,
    val contact: ContactEntity
)
