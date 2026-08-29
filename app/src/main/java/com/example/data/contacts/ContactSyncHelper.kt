package com.example.data.contacts

import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.model.CallDirection
import com.example.data.model.CallRecordEntity
import com.example.data.model.CallType
import com.example.data.model.ChatEntity
import com.example.data.model.ContactEntity
import com.example.data.model.MessageEntity
import com.example.data.model.MessageStatus
import com.example.data.model.MessageType
import com.example.data.model.StatusStoryEntity
import com.example.data.db.TChatDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class PhoneContactItem(
    val name: String,
    val phoneNumber: String
)

object ContactSyncHelper {

    fun hasContactPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    suspend fun fetchDeviceContacts(context: Context): List<PhoneContactItem> = withContext(Dispatchers.IO) {
        val contactList = mutableListOf<PhoneContactItem>()
        if (hasContactPermission(context)) {
            try {
                val cursor: Cursor? = context.contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    arrayOf(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                    ),
                    null,
                    null,
                    "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
                )

                cursor?.use {
                    val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                    while (it.moveToNext()) {
                        val name = if (nameIndex >= 0) it.getString(nameIndex) ?: "Unknown" else "Unknown"
                        val number = if (numberIndex >= 0) it.getString(numberIndex) ?: "" else ""
                        if (number.isNotBlank()) {
                            contactList.add(PhoneContactItem(name = name, phoneNumber = number.trim()))
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // If real device has zero contacts (e.g., fresh emulator), provide realistic device address book contacts
        if (contactList.isEmpty()) {
            contactList.addAll(getSimulatedPhoneAddressBook())
        }

        contactList.distinctBy { it.phoneNumber }
    }

    fun getSimulatedPhoneAddressBook(): List<PhoneContactItem> {
        return listOf(
            PhoneContactItem("Daniel Craig", "+1 (555) 781-9022"),
            PhoneContactItem("Olivia Rodrigo", "+1 (555) 892-3411"),
            PhoneContactItem("Uncle David", "+1 (555) 431-8890"),
            PhoneContactItem("Sophia Martinez", "+1 (555) 672-1144"),
            PhoneContactItem("Jordan Lee (Work)", "+1 (555) 238-7765"),
            PhoneContactItem("Hannah Smith", "+1 (555) 904-5532"),
            PhoneContactItem("Bestie Chloe", "+1 (555) 321-4477"),
            PhoneContactItem("Lucas Garcia", "+1 (555) 654-9810")
        )
    }

    suspend fun seedInitialDataIfEmpty(dao: TChatDao) = withContext(Dispatchers.IO) {
        val existing = dao.getAllContacts()
        // Check if database already has data by checking one item
        val contactsList = listOf(
            ContactEntity(
                name = "Sarah Jenkins",
                phoneNumber = "+1 (555) 234-5678",
                avatarRes = R.drawable.img_avatar_friend,
                avatarColorHex = 0xFF00A884,
                about = "Coffee lover ☕ | Available on TChatMe",
                isRegisteredOnTChatMe = true,
                isOnline = true,
                lastSeen = "online"
            ),
            ContactEntity(
                name = "Alex Rivera",
                phoneNumber = "+1 (555) 345-6789",
                avatarColorHex = 0xFF128C7E,
                about = "Working remotely 💻 | Call for urgent matters",
                isRegisteredOnTChatMe = true,
                isOnline = true,
                lastSeen = "online"
            ),
            ContactEntity(
                name = "Emma Watson",
                phoneNumber = "+1 (555) 456-7890",
                avatarColorHex = 0xFF25D366,
                about = "Living in the moment ✨",
                isRegisteredOnTChatMe = true,
                isOnline = false,
                lastSeen = "today at 1:42 PM"
            ),
            ContactEntity(
                name = "TChatMe Developers Group",
                phoneNumber = "+1 (555) 999-0000",
                avatarColorHex = 0xFF075E54,
                about = "Official TChatMe Android release discussion",
                isRegisteredOnTChatMe = true,
                isOnline = true,
                lastSeen = "Sarah, Alex, You"
            ),
            ContactEntity(
                name = "David Miller",
                phoneNumber = "+1 (555) 567-8901",
                avatarColorHex = 0xFF34B7F1,
                about = "At the gym 🏋️‍♂️",
                isRegisteredOnTChatMe = true,
                isOnline = false,
                lastSeen = "yesterday at 9:15 PM"
            ),
            ContactEntity(
                name = "Mom ❤️",
                phoneNumber = "+1 (555) 123-4567",
                avatarColorHex = 0xFFE91E63,
                about = "Always there for you 💖",
                isRegisteredOnTChatMe = true,
                isOnline = true,
                lastSeen = "online"
            ),
            ContactEntity(
                name = "Michael Chen",
                phoneNumber = "+1 (555) 678-9012",
                avatarColorHex = 0xFF9C27B0,
                about = "In a meeting 🔇",
                isRegisteredOnTChatMe = false,
                isOnline = false,
                lastSeen = "Invited"
            ),
            ContactEntity(
                name = "Jessica Taylor",
                phoneNumber = "+1 (555) 789-0123",
                avatarColorHex = 0xFFFF9800,
                about = "Available for calls",
                isRegisteredOnTChatMe = false,
                isOnline = false,
                lastSeen = "Invited"
            )
        )

        for (contact in contactsList) {
            val contactId = dao.insertContact(contact)
            val now = System.currentTimeMillis()

            if (contact.name == "Sarah Jenkins") {
                val chatId = dao.insertChat(
                    ChatEntity(
                        contactId = contactId,
                        lastMessageText = "Are we still doing the video call later today? 📹",
                        lastMessageType = MessageType.TEXT,
                        lastMessageTimestamp = now - 1000 * 60 * 5,
                        unreadCount = 1,
                        isPinned = true
                    )
                )

                // Insert seed messages
                dao.insertMessage(
                    MessageEntity(
                        chatId = chatId,
                        senderId = contactId.toString(),
                        content = "Hey! Welcome to TChatMe 👋 It works just like WhatsApp!",
                        timestamp = now - 1000 * 60 * 30,
                        status = MessageStatus.READ
                    )
                )
                dao.insertMessage(
                    MessageEntity(
                        chatId = chatId,
                        senderId = "me",
                        content = "Awesome! The interface and voice notes look super smooth! 🚀",
                        timestamp = now - 1000 * 60 * 25,
                        status = MessageStatus.READ
                    )
                )
                dao.insertMessage(
                    MessageEntity(
                        chatId = chatId,
                        senderId = contactId.toString(),
                        messageType = MessageType.VOICE_NOTE,
                        content = "Voice note (0:14)",
                        durationSec = 14,
                        timestamp = now - 1000 * 60 * 15,
                        status = MessageStatus.READ
                    )
                )
                dao.insertMessage(
                    MessageEntity(
                        chatId = chatId,
                        senderId = contactId.toString(),
                        content = "Are we still doing the video call later today? 📹",
                        timestamp = now - 1000 * 60 * 5,
                        status = MessageStatus.DELIVERED
                    )
                )

                // Call record
                dao.insertCallRecord(
                    CallRecordEntity(
                        contactId = contactId,
                        callType = CallType.VIDEO,
                        callDirection = CallDirection.INCOMING,
                        timestamp = now - 1000 * 60 * 60 * 2,
                        durationSeconds = 245
                    )
                )

                // Status Story
                dao.insertStatusStory(
                    StatusStoryEntity(
                        contactId = contactId,
                        textCaption = "Coffee break with the best sunset view ☕🌅",
                        backgroundColorHex = 0xFF128C7E,
                        timestamp = now - 1000 * 60 * 45
                    )
                )
            } else if (contact.name == "Alex Rivera") {
                val chatId = dao.insertChat(
                    ChatEntity(
                        contactId = contactId,
                        lastMessageText = "Just checked in the new update!",
                        lastMessageType = MessageType.TEXT,
                        lastMessageTimestamp = now - 1000 * 60 * 40,
                        unreadCount = 0,
                        isPinned = false
                    )
                )
                dao.insertMessage(
                    MessageEntity(
                        chatId = chatId,
                        senderId = "me",
                        content = "Hey Alex, are voice calls working on your phone?",
                        timestamp = now - 1000 * 60 * 50,
                        status = MessageStatus.READ
                    )
                )
                dao.insertMessage(
                    MessageEntity(
                        chatId = chatId,
                        senderId = contactId.toString(),
                        content = "Just checked in the new update! Crystal clear HD voice 🎙️",
                        timestamp = now - 1000 * 60 * 40,
                        status = MessageStatus.READ,
                        reaction = "👍"
                    )
                )

                dao.insertCallRecord(
                    CallRecordEntity(
                        contactId = contactId,
                        callType = CallType.VOICE,
                        callDirection = CallDirection.OUTGOING,
                        timestamp = now - 1000 * 60 * 60 * 5,
                        durationSeconds = 112
                    )
                )
            } else if (contact.name == "Mom ❤️") {
                val chatId = dao.insertChat(
                    ChatEntity(
                        contactId = contactId,
                        lastMessageText = "Call me whenever you get home sweetie ❤️",
                        lastMessageType = MessageType.TEXT,
                        lastMessageTimestamp = now - 1000 * 60 * 60,
                        unreadCount = 0
                    )
                )
                dao.insertMessage(
                    MessageEntity(
                        chatId = chatId,
                        senderId = contactId.toString(),
                        content = "Call me whenever you get home sweetie ❤️",
                        timestamp = now - 1000 * 60 * 60,
                        status = MessageStatus.READ,
                        reaction = "❤️"
                    )
                )

                dao.insertCallRecord(
                    CallRecordEntity(
                        contactId = contactId,
                        callType = CallType.VOICE,
                        callDirection = CallDirection.MISSED,
                        timestamp = now - 1000 * 60 * 120,
                        durationSeconds = 0
                    )
                )

                dao.insertStatusStory(
                    StatusStoryEntity(
                        contactId = contactId,
                        textCaption = "Family dinner tonight! Everyone is welcome 🍲",
                        backgroundColorHex = 0xFFE91E63,
                        timestamp = now - 1000 * 60 * 180
                    )
                )
            } else if (contact.name == "Emma Watson") {
                val chatId = dao.insertChat(
                    ChatEntity(
                        contactId = contactId,
                        lastMessageText = "See you tomorrow at 10!",
                        lastMessageType = MessageType.TEXT,
                        lastMessageTimestamp = now - 1000 * 60 * 150,
                        unreadCount = 0
                    )
                )
                dao.insertMessage(
                    MessageEntity(
                        chatId = chatId,
                        senderId = "me",
                        content = "Will you be at the meetup?",
                        timestamp = now - 1000 * 60 * 160,
                        status = MessageStatus.READ
                    )
                )
                dao.insertMessage(
                    MessageEntity(
                        chatId = chatId,
                        senderId = contactId.toString(),
                        content = "See you tomorrow at 10!",
                        timestamp = now - 1000 * 60 * 150,
                        status = MessageStatus.READ
                    )
                )
            }
        }
    }
}
