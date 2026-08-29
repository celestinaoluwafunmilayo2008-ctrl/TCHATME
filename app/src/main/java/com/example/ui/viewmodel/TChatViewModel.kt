package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.contacts.ContactSyncHelper
import com.example.data.db.AppDatabase
import com.example.data.model.CallDirection
import com.example.data.model.CallType
import com.example.data.model.CallWithContact
import com.example.data.model.ChatWithContact
import com.example.data.model.ContactEntity
import com.example.data.model.MessageEntity
import com.example.data.model.MessageType
import com.example.data.model.StatusWithContact
import com.example.data.repository.TChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class AppDestination {
    object Main : AppDestination()
    data class ChatDetail(val chatId: Long, val contactId: Long) : AppDestination()
    data class ActiveCall(val contactId: Long, val callType: CallType, val isVideo: Boolean) : AppDestination()
    object ContactsList : AppDestination()
    data class StatusViewer(val statusId: Long) : AppDestination()
    object CreateStatus : AppDestination()
    object ProfileSettings : AppDestination()
    data class ContactInfo(val contactId: Long) : AppDestination()
    object CreateGroup : AppDestination()
    object StarredMessages : AppDestination()
    object LinkedDevices : AppDestination()
    object PrivacySettings : AppDestination()
    object ChatSettings : AppDestination()
    object StorageSettings : AppDestination()
}

enum class HomeTab {
    CHATS,
    UPDATES,
    COMMUNITIES,
    CALLS
}

enum class ChatFilter {
    ALL,
    UNREAD,
    FAVORITES,
    GROUPS
}

class TChatViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = TChatRepository(database.tChatDao(), application)

    // Navigation State
    private val _currentDestination = MutableStateFlow<AppDestination>(AppDestination.Main)
    val currentDestination: StateFlow<AppDestination> = _currentDestination.asStateFlow()

    // Home Tab State
    private val _currentTab = MutableStateFlow(HomeTab.CHATS)
    val currentTab: StateFlow<HomeTab> = _currentTab.asStateFlow()

    // Chat Filter State
    private val _chatFilter = MutableStateFlow(ChatFilter.ALL)
    val chatFilter: StateFlow<ChatFilter> = _chatFilter.asStateFlow()

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    // User Profile
    val myProfileName = MutableStateFlow("Celestina O.")
    val myProfileAbout = MutableStateFlow("Hey there! I am using TChatMe 🌟")
    val myProfilePhone = MutableStateFlow("+1 (555) 019-2834")

    // Active Call State
    val isMicMuted = MutableStateFlow(false)
    val isSpeakerOn = MutableStateFlow(true)
    val isVideoDisabled = MutableStateFlow(false)
    val isFrontCamera = MutableStateFlow(true)

    // Raw Chats Flow
    val allChats: StateFlow<List<ChatWithContact>> = repository.chatsWithContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Chats
    val filteredChats: StateFlow<List<ChatWithContact>> = combine(
        allChats,
        _chatFilter,
        _searchQuery
    ) { chats, filter, query ->
        var list = chats
        if (query.isNotBlank()) {
            list = list.filter {
                it.contact.name.contains(query, ignoreCase = true) ||
                it.chat.lastMessageText.contains(query, ignoreCase = true) ||
                it.contact.phoneNumber.contains(query)
            }
        }
        when (filter) {
            ChatFilter.ALL -> list
            ChatFilter.UNREAD -> list.filter { it.chat.unreadCount > 0 }
            ChatFilter.FAVORITES -> list.filter { it.contact.isStarred }
            ChatFilter.GROUPS -> list.filter { it.contact.name.contains("Group", ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Contacts
    val allContacts: StateFlow<List<ContactEntity>> = repository.allContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Contacts for search
    val filteredContacts: StateFlow<List<ContactEntity>> = combine(
        allContacts,
        _searchQuery
    ) { contacts, query ->
        if (query.isBlank()) contacts
        else contacts.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.phoneNumber.contains(query) ||
            it.about.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Calls
    val allCalls: StateFlow<List<CallWithContact>> = repository.callsWithContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Statuses
    val allStatuses: StateFlow<List<StatusWithContact>> = repository.statusStoriesWithContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Navigation triggers
    fun navigateTo(destination: AppDestination) {
        _currentDestination.value = destination
    }

    fun navigateBack() {
        if (_isSearching.value) {
            _isSearching.value = false
            _searchQuery.value = ""
        }
        _currentDestination.value = AppDestination.Main
    }

    fun setHomeTab(tab: HomeTab) {
        _currentTab.value = tab
    }

    fun setChatFilter(filter: ChatFilter) {
        _chatFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSearching(searching: Boolean) {
        _isSearching.value = searching
        if (!searching) _searchQuery.value = ""
    }

    // Active Chat operations
    fun getMessagesForChat(chatId: Long): StateFlow<List<MessageEntity>> {
        return repository.getMessagesForChat(chatId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun markChatRead(chatId: Long) {
        viewModelScope.launch {
            repository.markChatAsRead(chatId)
        }
    }

    fun openChatWithContact(contactId: Long) {
        viewModelScope.launch {
            val chatId = repository.getOrCreateChatForContact(contactId)
            repository.markChatAsRead(chatId)
            _currentDestination.value = AppDestination.ChatDetail(chatId, contactId)
        }
    }

    fun togglePinChat(chatId: Long) {
        viewModelScope.launch {
            repository.togglePinChat(chatId)
        }
    }

    fun deleteChat(chatId: Long) {
        viewModelScope.launch {
            repository.deleteChat(chatId)
        }
    }

    fun sendMessage(
        chatId: Long,
        contactId: Long,
        content: String,
        type: MessageType = MessageType.TEXT,
        mediaUri: String? = null,
        durationSec: Int = 0,
        replyToMessageId: Long? = null
    ) {
        viewModelScope.launch {
            repository.sendMessage(chatId, contactId, content, type, mediaUri, durationSec, replyToMessageId)
        }
    }

    fun setMessageReaction(messageId: Long, reaction: String?) {
        viewModelScope.launch {
            repository.setMessageReaction(messageId, reaction)
        }
    }

    fun deleteMessage(messageId: Long) {
        viewModelScope.launch {
            repository.deleteMessage(messageId)
        }
    }

    fun toggleStarMessage(messageId: Long) {
        viewModelScope.launch {
            repository.toggleStarMessage(messageId)
        }
    }

    // Calls
    fun startCall(contactId: Long, callType: CallType) {
        isMicMuted.value = false
        isSpeakerOn.value = true
        isVideoDisabled.value = false
        isFrontCamera.value = true
        _currentDestination.value = AppDestination.ActiveCall(
            contactId = contactId,
            callType = callType,
            isVideo = (callType == CallType.VIDEO)
        )
    }

    fun endCall(contactId: Long, callType: CallType, durationSeconds: Int) {
        viewModelScope.launch {
            repository.recordCall(
                contactId = contactId,
                callType = callType,
                direction = CallDirection.OUTGOING,
                durationSeconds = durationSeconds
            )
            _currentDestination.value = AppDestination.Main
        }
    }

    fun clearCallLogs() {
        viewModelScope.launch {
            repository.clearCallLogs()
        }
    }

    // Contacts
    fun syncPhoneContacts() {
        viewModelScope.launch {
            val phoneContacts = ContactSyncHelper.fetchDeviceContacts(getApplication())
            repository.syncDevicePhoneContacts(phoneContacts)
        }
    }

    fun addNewContact(name: String, phone: String, about: String) {
        viewModelScope.launch {
            val id = repository.addContact(name, phone, about, isRegistered = true)
            openChatWithContact(id)
        }
    }

    // Status
    fun addStatusStory(caption: String, bgHex: Long) {
        viewModelScope.launch {
            repository.addStatusStory(caption, bgHex)
            _currentDestination.value = AppDestination.Main
        }
    }

    fun markStatusViewed(statusId: Long) {
        viewModelScope.launch {
            repository.markStatusViewed(statusId)
        }
    }

    // Starred Messages
    val starredMessages: StateFlow<List<MessageEntity>> = repository.starredMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createGroup(name: String, memberIds: List<Long>, description: String = "TChatMe Group") {
        viewModelScope.launch {
            val chatId = repository.createGroup(name, memberIds, description)
            val contact = repository.allContacts.first().find { it.name == name }
            if (contact != null) {
                _currentDestination.value = AppDestination.ChatDetail(chatId, contact.id)
            } else {
                _currentDestination.value = AppDestination.Main
            }
        }
    }

    fun sendPoll(chatId: Long, contactId: Long, question: String, options: List<String>) {
        viewModelScope.launch {
            repository.sendPoll(chatId, contactId, question, options)
        }
    }

    fun votePoll(messageId: Long, currentVotes: String, optionIndex: Int, totalOptions: Int) {
        viewModelScope.launch {
            repository.votePoll(messageId, currentVotes, optionIndex, totalOptions)
        }
    }

    fun setDisappearingTimer(chatId: Long, timer: String) {
        viewModelScope.launch {
            repository.setDisappearingTimer(chatId, timer)
        }
    }

    // Profile
    fun updateProfile(name: String, about: String, phone: String) {
        myProfileName.value = name
        myProfileAbout.value = about
        myProfilePhone.value = phone
    }
}
