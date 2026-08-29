package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.CallType
import com.example.data.model.MessageType
import com.example.ui.screens.CallScreen
import com.example.ui.screens.ChatDetailScreen
import com.example.ui.screens.ChatsSettingsScreen
import com.example.ui.screens.ContactInfoScreen
import com.example.ui.screens.ContactsScreen
import com.example.ui.screens.CreateGroupScreen
import com.example.ui.screens.CreateStatusScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LinkedDevicesScreen
import com.example.ui.screens.PrivacySettingsScreen
import com.example.ui.screens.ProfileSettingsScreen
import com.example.ui.screens.StarredMessagesScreen
import com.example.ui.screens.StatusViewerScreen
import com.example.ui.screens.StorageSettingsScreen
import com.example.ui.theme.TChatMeTheme
import com.example.ui.viewmodel.AppDestination
import com.example.ui.viewmodel.TChatViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TChatMeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TChatMeApp()
                }
            }
        }
    }
}

@Composable
fun TChatMeApp(
    viewModel: TChatViewModel = viewModel()
) {
    val destination by viewModel.currentDestination.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val chatFilter by viewModel.chatFilter.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val chats by viewModel.filteredChats.collectAsStateWithLifecycle()
    val contacts by viewModel.filteredContacts.collectAsStateWithLifecycle()
    val calls by viewModel.allCalls.collectAsStateWithLifecycle()
    val statuses by viewModel.allStatuses.collectAsStateWithLifecycle()
    val starredMessages by viewModel.starredMessages.collectAsStateWithLifecycle()

    val profileName by viewModel.myProfileName.collectAsStateWithLifecycle()
    val profileAbout by viewModel.myProfileAbout.collectAsStateWithLifecycle()
    val profilePhone by viewModel.myProfilePhone.collectAsStateWithLifecycle()

    val isMicMuted by viewModel.isMicMuted.collectAsStateWithLifecycle()
    val isSpeakerOn by viewModel.isSpeakerOn.collectAsStateWithLifecycle()
    val isVideoDisabled by viewModel.isVideoDisabled.collectAsStateWithLifecycle()
    val isFrontCamera by viewModel.isFrontCamera.collectAsStateWithLifecycle()

    // Handle system back button
    BackHandler(enabled = destination != AppDestination.Main) {
        viewModel.navigateBack()
    }

    when (val dest = destination) {
        is AppDestination.Main -> {
            HomeScreen(
                currentTab = currentTab,
                onTabSelected = { viewModel.setHomeTab(it) },
                chatFilter = chatFilter,
                onFilterSelected = { viewModel.setChatFilter(it) },
                isSearching = isSearching,
                searchQuery = searchQuery,
                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                onSearchToggle = { viewModel.setSearching(it) },
                chats = chats,
                calls = calls,
                statuses = statuses,
                onOpenChat = { contactId -> viewModel.openChatWithContact(contactId) },
                onOpenContacts = { viewModel.navigateTo(AppDestination.ContactsList) },
                onStartNewGroup = { viewModel.navigateTo(AppDestination.CreateGroup) },
                onOpenStarredMessages = { viewModel.navigateTo(AppDestination.StarredMessages) },
                onOpenLinkedDevices = { viewModel.navigateTo(AppDestination.LinkedDevices) },
                onStartCall = { contactId, callType -> viewModel.startCall(contactId, callType) },
                onOpenStatusViewer = { statusId ->
                    viewModel.markStatusViewed(statusId)
                    viewModel.navigateTo(AppDestination.StatusViewer(statusId))
                },
                onCreateStatus = { viewModel.navigateTo(AppDestination.CreateStatus) },
                onOpenProfileSettings = { viewModel.navigateTo(AppDestination.ProfileSettings) },
                onClearCallLogs = { viewModel.clearCallLogs() }
            )
        }

        is AppDestination.ChatDetail -> {
            val contact = contacts.find { it.id == dest.contactId }
            val messages by viewModel.getMessagesForChat(dest.chatId).collectAsStateWithLifecycle()

            if (contact != null) {
                ChatDetailScreen(
                    contact = contact,
                    messages = messages,
                    onBack = { viewModel.navigateBack() },
                    onSendMessage = { content, type, mediaUri, durationSec, replyToId ->
                        viewModel.sendMessage(dest.chatId, dest.contactId, content, type, mediaUri, durationSec, replyToId)
                    },
                    onSendPoll = { question, options ->
                        viewModel.sendPoll(dest.chatId, dest.contactId, question, options)
                    },
                    onVotePoll = { messageId, currentVotes, optionIndex, totalOptions ->
                        viewModel.votePoll(messageId, currentVotes, optionIndex, totalOptions)
                    },
                    onStartCall = { callType ->
                        viewModel.startCall(dest.contactId, callType)
                    },
                    onOpenContactInfo = {
                        viewModel.navigateTo(AppDestination.ContactInfo(dest.contactId))
                    },
                    onSetReaction = { messageId, reaction ->
                        viewModel.setMessageReaction(messageId, reaction)
                    },
                    onDeleteMessage = { messageId ->
                        viewModel.deleteMessage(messageId)
                    },
                    onToggleStarMessage = { messageId ->
                        viewModel.toggleStarMessage(messageId)
                    },
                    onClearChat = {
                        viewModel.deleteChat(dest.chatId)
                        viewModel.navigateBack()
                    }
                )
            } else {
                viewModel.navigateBack()
            }
        }

        is AppDestination.ActiveCall -> {
            val contact = contacts.find { it.id == dest.contactId }

            if (contact != null) {
                CallScreen(
                    contact = contact,
                    callType = dest.callType,
                    isMicMuted = isMicMuted,
                    isSpeakerOn = isSpeakerOn,
                    isVideoDisabled = isVideoDisabled,
                    isFrontCamera = isFrontCamera,
                    onToggleMic = { viewModel.isMicMuted.value = !isMicMuted },
                    onToggleSpeaker = { viewModel.isSpeakerOn.value = !isSpeakerOn },
                    onToggleVideo = { viewModel.isVideoDisabled.value = !isVideoDisabled },
                    onFlipCamera = { viewModel.isFrontCamera.value = !isFrontCamera },
                    onEndCall = { durationSec ->
                        viewModel.endCall(dest.contactId, dest.callType, durationSec)
                    }
                )
            } else {
                viewModel.navigateBack()
            }
        }

        is AppDestination.ContactsList -> {
            ContactsScreen(
                contacts = contacts,
                onBack = { viewModel.navigateBack() },
                onOpenChat = { contactId -> viewModel.openChatWithContact(contactId) },
                onStartCall = { contactId, callType -> viewModel.startCall(contactId, callType) },
                onAddNewContact = { name, phone, about ->
                    viewModel.addNewContact(name, phone, about)
                },
                onSyncPhoneContacts = { viewModel.syncPhoneContacts() }
            )
        }

        is AppDestination.CreateGroup -> {
            CreateGroupScreen(
                contacts = contacts,
                onCreateGroup = { groupName, selectedContactIds, description ->
                    viewModel.createGroup(groupName, selectedContactIds, description)
                },
                onBack = { viewModel.navigateBack() }
            )
        }

        is AppDestination.StarredMessages -> {
            StarredMessagesScreen(
                starredMessages = starredMessages,
                onBack = { viewModel.navigateBack() },
                onUnstarMessage = { messageId -> viewModel.toggleStarMessage(messageId) }
            )
        }

        is AppDestination.LinkedDevices -> {
            LinkedDevicesScreen(
                onBack = { viewModel.navigateBack() }
            )
        }

        is AppDestination.PrivacySettings -> {
            PrivacySettingsScreen(
                onBack = { viewModel.navigateBack() }
            )
        }

        is AppDestination.ChatSettings -> {
            ChatsSettingsScreen(
                onBack = { viewModel.navigateBack() }
            )
        }

        is AppDestination.StorageSettings -> {
            StorageSettingsScreen(
                onBack = { viewModel.navigateBack() }
            )
        }

        is AppDestination.StatusViewer -> {
            val statusItem = statuses.find { it.status.statusId == dest.statusId }

            if (statusItem != null) {
                StatusViewerScreen(
                    statusWithContact = statusItem,
                    onClose = { viewModel.navigateBack() }
                )
            } else {
                viewModel.navigateBack()
            }
        }

        is AppDestination.CreateStatus -> {
            CreateStatusScreen(
                onPostStatus = { caption, bgHex ->
                    viewModel.addStatusStory(caption, bgHex)
                },
                onClose = { viewModel.navigateBack() }
            )
        }

        is AppDestination.ProfileSettings -> {
            ProfileSettingsScreen(
                profileName = profileName,
                profileAbout = profileAbout,
                profilePhone = profilePhone,
                onSaveProfile = { name, about, phone ->
                    viewModel.updateProfile(name, about, phone)
                },
                onOpenPrivacySettings = { viewModel.navigateTo(AppDestination.PrivacySettings) },
                onOpenChatSettings = { viewModel.navigateTo(AppDestination.ChatSettings) },
                onOpenStorageSettings = { viewModel.navigateTo(AppDestination.StorageSettings) },
                onBack = { viewModel.navigateBack() }
            )
        }

        is AppDestination.ContactInfo -> {
            val contact = contacts.find { it.id == dest.contactId }

            if (contact != null) {
                ContactInfoScreen(
                    contact = contact,
                    onBack = { viewModel.navigateBack() },
                    onStartVoiceCall = { viewModel.startCall(contact.id, CallType.VOICE) },
                    onStartVideoCall = { viewModel.startCall(contact.id, CallType.VIDEO) },
                    onStartMessage = { viewModel.openChatWithContact(contact.id) }
                )
            } else {
                viewModel.navigateBack()
            }
        }
    }
}
