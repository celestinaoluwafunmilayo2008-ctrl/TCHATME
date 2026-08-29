package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CallRecordEntity
import com.example.data.model.ChatEntity
import com.example.data.model.ContactEntity
import com.example.data.model.MessageEntity
import com.example.data.model.StatusStoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TChatDao {

    // --- CONTACTS ---
    @Query("SELECT * FROM contacts ORDER BY name ASC")
    fun getAllContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE id = :contactId LIMIT 1")
    suspend fun getContactById(contactId: Long): ContactEntity?

    @Query("SELECT * FROM contacts WHERE phoneNumber = :phone LIMIT 1")
    suspend fun getContactByPhone(phone: String): ContactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<ContactEntity>)

    @Update
    suspend fun updateContact(contact: ContactEntity)

    @Query("DELETE FROM contacts WHERE id = :contactId")
    suspend fun deleteContact(contactId: Long)

    // --- CHATS ---
    @Query("SELECT * FROM chats ORDER BY isPinned DESC, lastMessageTimestamp DESC")
    fun getAllChats(): Flow<List<ChatEntity>>

    @Query("SELECT * FROM chats WHERE chatId = :chatId LIMIT 1")
    suspend fun getChatById(chatId: Long): ChatEntity?

    @Query("SELECT * FROM chats WHERE contactId = :contactId LIMIT 1")
    suspend fun getChatByContactId(contactId: Long): ChatEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity): Long

    @Update
    suspend fun updateChat(chat: ChatEntity)

    @Query("UPDATE chats SET unreadCount = 0 WHERE chatId = :chatId")
    suspend fun markChatAsRead(chatId: Long)

    @Query("UPDATE chats SET isPinned = NOT isPinned WHERE chatId = :chatId")
    suspend fun togglePinChat(chatId: Long)

    @Query("DELETE FROM chats WHERE chatId = :chatId")
    suspend fun deleteChat(chatId: Long)

    // --- MESSAGES ---
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getMessagesForChat(chatId: Long): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Query("UPDATE messages SET reaction = :reaction WHERE messageId = :messageId")
    suspend fun setMessageReaction(messageId: Long, reaction: String?)

    @Query("UPDATE messages SET isStarred = NOT isStarred WHERE messageId = :messageId")
    suspend fun toggleStarMessage(messageId: Long)

    @Query("UPDATE messages SET pollVotes = :votes WHERE messageId = :messageId")
    suspend fun updatePollVotes(messageId: Long, votes: String)

    @Query("SELECT * FROM messages WHERE isStarred = 1 ORDER BY timestamp DESC")
    fun getStarredMessages(): Flow<List<MessageEntity>>

    @Query("UPDATE chats SET isArchived = :isArchived WHERE chatId = :chatId")
    suspend fun setChatArchived(chatId: Long, isArchived: Boolean)

    @Query("UPDATE chats SET disappearingTimer = :timer WHERE chatId = :chatId")
    suspend fun setDisappearingTimer(chatId: Long, timer: String)

    @Query("DELETE FROM messages WHERE messageId = :messageId")
    suspend fun deleteMessage(messageId: Long)

    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun clearMessagesForChat(chatId: Long)

    // --- CALLS ---
    @Query("SELECT * FROM call_records ORDER BY timestamp DESC")
    fun getAllCallRecords(): Flow<List<CallRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallRecord(call: CallRecordEntity): Long

    @Query("DELETE FROM call_records WHERE callId = :callId")
    suspend fun deleteCallRecord(callId: Long)

    @Query("DELETE FROM call_records")
    suspend fun clearAllCallRecords()

    // --- STATUS STORIES ---
    @Query("SELECT * FROM status_stories ORDER BY timestamp DESC")
    fun getAllStatusStories(): Flow<List<StatusStoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatusStory(status: StatusStoryEntity): Long

    @Query("UPDATE status_stories SET isViewed = 1 WHERE statusId = :statusId")
    suspend fun markStatusViewed(statusId: Long)
}
