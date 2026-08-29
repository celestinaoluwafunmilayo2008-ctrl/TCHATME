package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallMissed
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.CallDirection
import com.example.data.model.CallType
import com.example.data.model.CallWithContact
import com.example.data.model.ChatWithContact
import com.example.data.model.MessageType
import com.example.data.model.StatusWithContact
import com.example.ui.components.AvatarView
import com.example.ui.components.WhatsAppBottomNav
import com.example.ui.components.WhatsAppTopBar
import com.example.ui.theme.WhatsAppBlueTick
import com.example.ui.theme.WhatsAppGreenDark
import com.example.ui.theme.WhatsAppGreenLight
import com.example.ui.theme.WhatsAppGreenPrimary
import com.example.ui.viewmodel.ChatFilter
import com.example.ui.viewmodel.HomeTab
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    currentTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
    chatFilter: ChatFilter,
    onFilterSelected: (ChatFilter) -> Unit,
    isSearching: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchToggle: (Boolean) -> Unit,
    chats: List<ChatWithContact>,
    calls: List<CallWithContact>,
    statuses: List<StatusWithContact>,
    onOpenChat: (contactId: Long) -> Unit,
    onOpenContacts: () -> Unit,
    onStartNewGroup: () -> Unit,
    onOpenStarredMessages: () -> Unit,
    onOpenLinkedDevices: () -> Unit,
    onStartCall: (contactId: Long, callType: CallType) -> Unit,
    onOpenStatusViewer: (statusId: Long) -> Unit,
    onCreateStatus: () -> Unit,
    onOpenProfileSettings: () -> Unit,
    onClearCallLogs: () -> Unit,
    modifier: Modifier = Modifier
) {
    val unreadTotal = remember(chats) { chats.sumOf { it.chat.unreadCount } }
    val hasUnseenStatus = remember(statuses) { statuses.any { !it.status.isViewed && it.status.contactId != 0L } }

    Scaffold(
        topBar = {
            WhatsAppTopBar(
                isSearching = isSearching,
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                onSearchToggle = onSearchToggle,
                onOpenProfileSettings = onOpenProfileSettings,
                onOpenContacts = onOpenContacts,
                onStartNewGroup = onStartNewGroup,
                onOpenStarredMessages = onOpenStarredMessages,
                onOpenLinkedDevices = onOpenLinkedDevices,
                onClearCallLogs = if (currentTab == HomeTab.CALLS) onClearCallLogs else null
            )
        },
        bottomBar = {
            WhatsAppBottomNav(
                currentTab = currentTab,
                onTabSelected = onTabSelected,
                unreadChatCount = unreadTotal,
                hasUnseenStatus = hasUnseenStatus
            )
        },
        floatingActionButton = {
            when (currentTab) {
                HomeTab.CHATS -> {
                    FloatingActionButton(
                        onClick = onOpenContacts,
                        containerColor = WhatsAppGreenPrimary,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("fab_new_chat")
                    ) {
                        Icon(imageVector = Icons.Default.Chat, contentDescription = "New Chat")
                    }
                }
                HomeTab.UPDATES -> {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FloatingActionButton(
                            onClick = onCreateStatus,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            shape = CircleShape,
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("fab_text_status")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Text status",
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        FloatingActionButton(
                            onClick = onCreateStatus,
                            containerColor = WhatsAppGreenPrimary,
                            contentColor = Color.White,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.testTag("fab_camera_status")
                        ) {
                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Add Status")
                        }
                    }
                }
                HomeTab.COMMUNITIES -> {
                    FloatingActionButton(
                        onClick = onStartNewGroup,
                        containerColor = WhatsAppGreenPrimary,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("fab_new_community")
                    ) {
                        Icon(imageVector = Icons.Default.Groups, contentDescription = "New Community")
                    }
                }
                HomeTab.CALLS -> {
                    FloatingActionButton(
                        onClick = onOpenContacts,
                        containerColor = WhatsAppGreenPrimary,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("fab_new_call")
                    ) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = "New Call")
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentTab) {
                HomeTab.CHATS -> {
                    ChatsTabContent(
                        chats = chats,
                        currentFilter = chatFilter,
                        onFilterSelected = onFilterSelected,
                        onOpenChat = onOpenChat,
                        onOpenContacts = onOpenContacts
                    )
                }
                HomeTab.UPDATES -> {
                    UpdatesTabContent(
                        statuses = statuses,
                        onOpenStatusViewer = onOpenStatusViewer,
                        onCreateStatus = onCreateStatus
                    )
                }
                HomeTab.COMMUNITIES -> {
                    CommunitiesTabContent(
                        onStartNewCommunity = onStartNewGroup,
                        onOpenChat = onOpenChat
                    )
                }
                HomeTab.CALLS -> {
                    CallsTabContent(
                        calls = calls,
                        onStartCall = onStartCall,
                        onOpenContacts = onOpenContacts
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// CHATS TAB
// -------------------------------------------------------------
@Composable
fun ChatsTabContent(
    chats: List<ChatWithContact>,
    currentFilter: ChatFilter,
    onFilterSelected: (ChatFilter) -> Unit,
    onOpenChat: (contactId: Long) -> Unit,
    onOpenContacts: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Quick Filters Pills (All, Unread, Favorites, Groups)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ChatFilterChip(
                title = "All",
                selected = currentFilter == ChatFilter.ALL,
                onClick = { onFilterSelected(ChatFilter.ALL) }
            )
            ChatFilterChip(
                title = "Unread",
                selected = currentFilter == ChatFilter.UNREAD,
                onClick = { onFilterSelected(ChatFilter.UNREAD) }
            )
            ChatFilterChip(
                title = "Favorites",
                selected = currentFilter == ChatFilter.FAVORITES,
                onClick = { onFilterSelected(ChatFilter.FAVORITES) }
            )
            ChatFilterChip(
                title = "Groups",
                selected = currentFilter == ChatFilter.GROUPS,
                onClick = { onFilterSelected(ChatFilter.GROUPS) }
            )
        }

        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.surfaceVariant
        )

        // Archived row banner (WhatsApp style)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { }
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Campaign,
                contentDescription = "Archived",
                tint = WhatsAppGreenPrimary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Archived",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "0",
                fontSize = 13.sp,
                color = WhatsAppGreenPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        if (chats.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        tint = WhatsAppGreenPrimary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No chats yet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Start chatting with your contacts on TChatMe",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onOpenContacts,
                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreenPrimary)
                    ) {
                        Text("Start Chat")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(chats, key = { it.chat.chatId }) { item ->
                    ChatItemRow(
                        chatItem = item,
                        onClick = { onOpenChat(item.contact.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ChatFilterChip(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (selected) Color(0xFFD9FDD3) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) WhatsAppGreenDark else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun ChatItemRow(
    chatItem: ChatWithContact,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contact = chatItem.contact
    val chat = chatItem.chat

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("chat_item_${contact.id}")
    ) {
        AvatarView(
            name = contact.name,
            avatarRes = contact.avatarRes,
            colorHex = contact.avatarColorHex,
            size = 54.dp,
            isOnline = contact.isOnline
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = contact.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                val timeString = formatTimestamp(chat.lastMessageTimestamp)
                Text(
                    text = timeString,
                    fontSize = 12.sp,
                    color = if (chat.unreadCount > 0) WhatsAppGreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.Normal
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (chat.unreadCount == 0 && chat.lastMessageText.isNotBlank()) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Read",
                            tint = WhatsAppBlueTick,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(end = 4.dp)
                        )
                    }

                    if (chat.lastMessageType == MessageType.VOICE_NOTE) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice message",
                            tint = WhatsAppGreenPrimary,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(end = 4.dp)
                        )
                    }

                    Text(
                        text = if (contact.lastSeen == "typing...") "typing..." else chat.lastMessageText,
                        fontSize = 14.sp,
                        color = if (contact.lastSeen == "typing...") WhatsAppGreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (contact.lastSeen == "typing..." || chat.unreadCount > 0) FontWeight.Medium else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (chat.isPinned) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(end = 6.dp)
                        )
                    }

                    if (chat.unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(WhatsAppGreenPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = chat.unreadCount.toString(),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// UPDATES TAB (STATUS STORIES + CHANNELS)
// -------------------------------------------------------------
@Composable
fun UpdatesTabContent(
    statuses: List<StatusWithContact>,
    onOpenStatusViewer: (statusId: Long) -> Unit,
    onCreateStatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val myStatuses = statuses.filter { it.status.contactId == 0L }
    val friendStatuses = statuses.filter { it.status.contactId != 0L }
    val followedChannels = remember { mutableStateMapOf<String, Boolean>() }

    val mockChannels = listOf(
        Triple("WhatsApp Official", "Official news and feature updates from WhatsApp", "148M followers"),
        Triple("Real Madrid C.F.", "Welcome to the official Real Madrid channel!", "42M followers"),
        Triple("Tech & AI Insider", "Breaking tech news, AI developments and gadgets", "18.5M followers"),
        Triple("BBC News", "Stories and analysis from BBC journalists worldwide", "9.2M followers")
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Text(
                text = "Status",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            // My Status Card
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (myStatuses.isNotEmpty()) onOpenStatusViewer(myStatuses.first().status.statusId)
                        else onCreateStatus()
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    AvatarView(
                        name = "My Status",
                        colorHex = 0xFF075E54,
                        size = 54.dp,
                        hasStory = myStatuses.isNotEmpty(),
                        isStoryViewed = false
                    )

                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(WhatsAppGreenPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Status",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "My Status",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (myStatuses.isNotEmpty()) "Tap to view status update" else "Tap to add status update",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.surfaceVariant
            )

            Text(
                text = "Recent updates",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        if (friendStatuses.isEmpty()) {
            item {
                Text(
                    text = "No recent status updates from contacts",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        } else {
            items(friendStatuses, key = { it.status.statusId }) { item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenStatusViewer(item.status.statusId) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    AvatarView(
                        name = item.contact.name,
                        avatarRes = item.contact.avatarRes,
                        colorHex = item.contact.avatarColorHex,
                        size = 52.dp,
                        hasStory = true,
                        isStoryViewed = item.status.isViewed
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.contact.name,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formatTimestamp(item.status.timestamp),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // WhatsApp Channels Section
        item {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.surfaceVariant
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Channels",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Explore",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = WhatsAppGreenPrimary
                )
            }

            Text(
                text = "Stay updated on topics you care about. Find channels to follow below.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        items(mockChannels) { (chName, chDesc, chFollowers) ->
            val isFollowed = followedChannels[chName] == true
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                AvatarView(name = chName, colorHex = 0xFF128C7E, size = 48.dp)
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(chName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(chDesc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(chFollowers, fontSize = 11.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { followedChannels[chName] = !isFollowed },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFollowed) MaterialTheme.colorScheme.surfaceVariant else WhatsAppGreenPrimary
                    ),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isFollowed) "Following" else "Follow",
                        color = if (isFollowed) MaterialTheme.colorScheme.onSurfaceVariant else Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// COMMUNITIES TAB (WHATSAPP COMMUNITIES)
// -------------------------------------------------------------
@Composable
fun CommunitiesTabContent(
    onStartNewCommunity: () -> Unit,
    onOpenChat: (contactId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            // New Community action card
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onStartNewCommunity() }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(WhatsAppGreenPrimary, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GroupAdd,
                        contentDescription = "New community",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "New community",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 6.dp)
        }

        // Active Communities Cards
        item {
            CommunityCard(
                communityName = "TChatMe Developer Network",
                communityTag = "Global Kotlin & Compose Tech Community",
                groups = listOf(
                    "Announcements • TChatMe HQ" to "📢 Welcome new developers to the platform!",
                    "Android Jetpack Compose" to "Sarah: Have you checked out the new Material 3 M3 styling?",
                    "WebRTC & Calls Engine" to "Alex: Audio and HD Video calls are operating smoothly."
                ),
                onGroupClick = { onOpenChat(1L) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
            CommunityCard(
                communityName = "University Campus Hub",
                communityTag = "Campus news, events, and class groups",
                groups = listOf(
                    "Campus Announcements" to "📌 Final semester project submissions due next Friday!",
                    "Computer Science Class of '26" to "Emma: Study session in library at 4 PM"
                ),
                onGroupClick = { onOpenChat(2L) }
            )
        }
    }
}

@Composable
fun CommunityCard(
    communityName: String,
    communityTag: String,
    groups: List<Pair<String, String>>,
    onGroupClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(0xFF00796B.toInt().let { Color(it) }, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Groups, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(communityName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(communityTag, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.surfaceVariant)

        groups.forEach { (title, subtitle) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onGroupClick() }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = null,
                    tint = WhatsAppGreenPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 6.dp)
    }
}

// -------------------------------------------------------------
// CALLS TAB
// -------------------------------------------------------------
@Composable
fun CallsTabContent(
    calls: List<CallWithContact>,
    onStartCall: (contactId: Long, callType: CallType) -> Unit,
    onOpenContacts: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showCallLinkDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Create call link header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCallLinkDialog = true }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(WhatsAppGreenPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = "Create link",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Create call link",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Share a link for your TChatMe voice or video call",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "Recent",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        if (calls.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No call logs yet. Tap the button below to call friends!",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            items(calls, key = { it.call.callId }) { callItem ->
                val contact = callItem.contact
                val call = callItem.call

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .testTag("call_item_${call.callId}")
                ) {
                    AvatarView(
                        name = contact.name,
                        avatarRes = contact.avatarRes,
                        colorHex = contact.avatarColorHex,
                        size = 50.dp
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = contact.name,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = if (call.callDirection == CallDirection.MISSED) Color.Red else MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val (arrowIcon, arrowColor) = when (call.callDirection) {
                                CallDirection.INCOMING -> Icons.Default.CallReceived to WhatsAppGreenPrimary
                                CallDirection.OUTGOING -> Icons.Default.CallMade to WhatsAppGreenPrimary
                                CallDirection.MISSED -> Icons.Default.CallMissed to Color.Red
                            }

                            Icon(
                                imageVector = arrowIcon,
                                contentDescription = null,
                                tint = arrowColor,
                                modifier = Modifier.size(16.dp)
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = "${formatTimestamp(call.timestamp)}${if (call.durationSeconds > 0) " (${call.durationSeconds}s)" else ""}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Call Action Button
                    IconButton(
                        onClick = { onStartCall(contact.id, call.callType) },
                        modifier = Modifier.testTag("repeat_call_button_${contact.id}")
                    ) {
                        Icon(
                            imageVector = if (call.callType == CallType.VIDEO) Icons.Default.Videocam else Icons.Default.Call,
                            contentDescription = "Call",
                            tint = WhatsAppGreenPrimary
                        )
                    }
                }
            }
        }
    }

    if (showCallLinkDialog) {
        val link = "https://call.tchatme.com/v/tchat-${System.currentTimeMillis() % 100000}"
        AlertDialog(
            onDismissRequest = { showCallLinkDialog = false },
            title = { Text("TChatMe Call Link") },
            text = {
                Column {
                    Text("Anyone with TChatMe can use this link to join this call:")
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = link,
                            color = WhatsAppGreenPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("TChatMe Call Link", link))
                        Toast.makeText(context, "Link copied to clipboard!", Toast.LENGTH_SHORT).show()
                        showCallLinkDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreenPrimary)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy link")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCallLinkDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60 * 1000 -> "Just now"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m ago"
        diff < 24 * 60 * 60 * 1000 -> {
            val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
        diff < 48 * 60 * 60 * 1000 -> "Yesterday"
        else -> {
            val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}
