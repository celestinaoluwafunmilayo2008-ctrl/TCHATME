package com.example.ui.screens

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.model.CallType
import com.example.data.model.ContactEntity
import com.example.data.model.MessageEntity
import com.example.data.model.MessageType
import com.example.ui.components.AvatarView
import com.example.ui.components.VoiceNotePlayer
import com.example.ui.components.VoiceNoteRecorderBar
import com.example.ui.theme.WhatsAppBlueTick
import com.example.ui.theme.WhatsAppGreenDark
import com.example.ui.theme.WhatsAppGreenPrimary
import com.example.ui.theme.WhatsAppIncomingBubbleLight
import com.example.ui.theme.WhatsAppOutgoingBubbleLight
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    contact: ContactEntity,
    messages: List<MessageEntity>,
    onBack: () -> Unit,
    onSendMessage: (content: String, type: MessageType, mediaUri: String?, durationSec: Int, replyToMessageId: Long?) -> Unit,
    onSendPoll: (question: String, options: List<String>) -> Unit,
    onVotePoll: (messageId: Long, currentVotes: String, optionIndex: Int, totalOptions: Int) -> Unit,
    onStartCall: (callType: CallType) -> Unit,
    onOpenContactInfo: () -> Unit,
    onSetReaction: (messageId: Long, reaction: String?) -> Unit,
    onDeleteMessage: (messageId: Long) -> Unit,
    onToggleStarMessage: (messageId: Long) -> Unit,
    onClearChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    var isRecordingVoice by remember { mutableStateOf(false) }
    var showAttachmentsSheet by remember { mutableStateOf(false) }
    var showHeaderMenu by remember { mutableStateOf(false) }
    var selectedMessageForReaction by remember { mutableStateOf<MessageEntity?>(null) }
    var replyingToMessage by remember { mutableStateOf<MessageEntity?>(null) }
    var showPollDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Wallpaper background
        Image(
            painter = painterResource(id = R.drawable.img_chat_wallpaper),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Header Top Bar
            Surface(
                color = WhatsAppGreenDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(60.dp)
                        .padding(horizontal = 4.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("chat_detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    // Contact Avatar + Info (Clickable for Contact Info)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onOpenContactInfo() }
                            .padding(vertical = 4.dp)
                            .testTag("chat_contact_header_clickable")
                    ) {
                        AvatarView(
                            name = contact.name,
                            colorHex = contact.avatarColorHex,
                            size = 38.dp
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = contact.name,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (contact.isGroup) "${contact.groupMembersCount} participants" else contact.lastSeen,
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Action Icons: Video Call, Voice Call, More
                    IconButton(
                        onClick = { onStartCall(CallType.VIDEO) },
                        modifier = Modifier.testTag("chat_video_call_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Video Call",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = { onStartCall(CallType.VOICE) },
                        modifier = Modifier.testTag("chat_voice_call_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Voice Call",
                            tint = Color.White
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { showHeaderMenu = true },
                            modifier = Modifier.testTag("chat_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More",
                                tint = Color.White
                            )
                        }

                        DropdownMenu(
                            expanded = showHeaderMenu,
                            onDismissRequest = { showHeaderMenu = false },
                            modifier = Modifier.background(
                                MaterialTheme.colorScheme.surface,
                                RoundedCornerShape(12.dp)
                            )
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (contact.isGroup) "Group info" else "View contact") },
                                onClick = {
                                    showHeaderMenu = false
                                    onOpenContactInfo()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Disappearing messages") },
                                onClick = {
                                    showHeaderMenu = false
                                    Toast.makeText(context, "Disappearing messages set to 24h", Toast.LENGTH_SHORT).show()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Search chat") },
                                onClick = {
                                    showHeaderMenu = false
                                    Toast.makeText(context, "Search ready", Toast.LENGTH_SHORT).show()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Clear chat") },
                                onClick = {
                                    showHeaderMenu = false
                                    onClearChat()
                                }
                            )
                        }
                    }
                }
            }

            // Message List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                contentPadding = PaddingValues(top = 10.dp, bottom = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Encryption disclaimer badge
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            color = Color(0xFFFFF3CD),
                            shape = RoundedCornerShape(8.dp),
                            shadowElevation = 0.5.dp
                        ) {
                            Text(
                                text = "🔒 Messages and calls are end-to-end encrypted. No one outside of this chat, not even TChatMe, can read or listen to them.",
                                fontSize = 11.sp,
                                color = Color(0xFF664D03),
                                lineHeight = 15.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                items(messages, key = { it.messageId }) { msg ->
                    MessageBubble(
                        message = msg,
                        onLongClick = { selectedMessageForReaction = msg },
                        onVotePoll = { optionIdx ->
                            val opts = (msg.pollOptions ?: "").split("||")
                            onVotePoll(msg.messageId, msg.pollVotes ?: "", optionIdx, opts.size)
                        }
                    )
                }
            }

            // Reply Banner Preview if replying
            AnimatedVisibility(visible = replyingToMessage != null) {
                if (replyingToMessage != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(36.dp)
                                    .background(WhatsAppGreenPrimary, RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (replyingToMessage?.senderId == "me") "You" else contact.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = WhatsAppGreenPrimary
                                )
                                Text(
                                    text = replyingToMessage?.content ?: "",
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { replyingToMessage = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Cancel reply", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            // Bottom Input Bar or Voice Recording Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 6.dp, vertical = 6.dp)
            ) {
                if (isRecordingVoice) {
                    VoiceNoteRecorderBar(
                        onSendVoiceNote = { duration ->
                            isRecordingVoice = false
                            onSendMessage("🎙️ Voice message (${duration}s)", MessageType.VOICE_NOTE, null, duration, replyingToMessage?.messageId)
                            replyingToMessage = null
                        },
                        onCancel = {
                            isRecordingVoice = false
                        }
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Message input pill
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 2.dp,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        inputText += "😊"
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEmotions,
                                        contentDescription = "Emojis",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                BasicTextField(
                                    value = inputText,
                                    onValueChange = { inputText = it },
                                    textStyle = TextStyle(
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 16.sp
                                    ),
                                    cursorBrush = SolidColor(WhatsAppGreenPrimary),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 4.dp)
                                        .testTag("chat_input_text"),
                                    decorationBox = { innerTextField ->
                                        if (inputText.isEmpty()) {
                                            Text(
                                                text = "Message",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                fontSize = 16.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                )

                                IconButton(
                                    onClick = { showAttachmentsSheet = true },
                                    modifier = Modifier.testTag("attachment_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AttachFile,
                                        contentDescription = "Attach",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (inputText.isEmpty()) {
                                    IconButton(onClick = {
                                        onSendMessage("📷 [Photo snapshot]", MessageType.IMAGE, null, 0, replyingToMessage?.messageId)
                                        replyingToMessage = null
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = "Camera",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Mic or Send action circle
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(WhatsAppGreenPrimary, CircleShape)
                                .clickable {
                                    if (inputText.isNotBlank()) {
                                        val replyText = if (replyingToMessage != null) "↩️ Replying: \"${replyingToMessage?.content}\"\n" else ""
                                        onSendMessage(replyText + inputText.trim(), MessageType.TEXT, null, 0, replyingToMessage?.messageId)
                                        inputText = ""
                                        replyingToMessage = null
                                    } else {
                                        isRecordingVoice = true
                                    }
                                }
                                .testTag("chat_send_or_mic_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (inputText.isNotBlank()) Icons.AutoMirrored.Filled.Send else Icons.Default.Mic,
                                contentDescription = if (inputText.isNotBlank()) "Send" else "Record Voice Note",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        // Reaction & Message Actions Dialog
        if (selectedMessageForReaction != null) {
            val msg = selectedMessageForReaction!!
            Dialog(onDismissRequest = { selectedMessageForReaction = null }) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Quick Emoji Reactions
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val emojis = listOf("❤️", "👍", "😂", "😮", "😢", "🙏")
                            emojis.forEach { emoji ->
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            onSetReaction(msg.messageId, emoji)
                                            selectedMessageForReaction = null
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = emoji, fontSize = 24.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceAround,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(onClick = {
                                replyingToMessage = msg
                                selectedMessageForReaction = null
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Reply,
                                    contentDescription = "Reply",
                                    tint = WhatsAppGreenPrimary
                                )
                            }

                            IconButton(onClick = {
                                onToggleStarMessage(msg.messageId)
                                selectedMessageForReaction = null
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Star",
                                    tint = if (msg.isStarred) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurface
                                )
                            }

                            IconButton(onClick = {
                                onDeleteMessage(msg.messageId)
                                selectedMessageForReaction = null
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color.Red
                                )
                            }
                        }
                    }
                }
            }
        }

        // Attachment Sheet
        if (showAttachmentsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAttachmentsSheet = false },
                sheetState = rememberModalBottomSheetState()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Share Content",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.SpaceAround,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AttachmentOption(
                            icon = Icons.Default.Image,
                            title = "Gallery",
                            bgColor = Color(0xFF9C27B0)
                        ) {
                            showAttachmentsSheet = false
                            onSendMessage("📷 [Photo from Gallery]", MessageType.IMAGE, null, 0, replyingToMessage?.messageId)
                            replyingToMessage = null
                        }

                        AttachmentOption(
                            icon = Icons.Default.Description,
                            title = "Document",
                            bgColor = Color(0xFF536DFE)
                        ) {
                            showAttachmentsSheet = false
                            onSendMessage("📄 Project_Plan_2026.pdf (2.4 MB)", MessageType.DOCUMENT, null, 0, replyingToMessage?.messageId)
                            replyingToMessage = null
                        }

                        AttachmentOption(
                            icon = Icons.Default.BarChart,
                            title = "Poll",
                            bgColor = Color(0xFFFF9800)
                        ) {
                            showAttachmentsSheet = false
                            showPollDialog = true
                        }

                        AttachmentOption(
                            icon = Icons.Default.LocationOn,
                            title = "Location",
                            bgColor = Color(0xFF4CAF50)
                        ) {
                            showAttachmentsSheet = false
                            onSendMessage("📍 Current Location: Lagos, Nigeria", MessageType.LOCATION, null, 0, replyingToMessage?.messageId)
                            replyingToMessage = null
                        }
                    }
                }
            }
        }

        // Create Poll Dialog
        if (showPollDialog) {
            CreatePollDialog(
                onDismiss = { showPollDialog = false },
                onCreatePoll = { q, opts ->
                    showPollDialog = false
                    onSendPoll(q, opts)
                }
            )
        }
    }
}

@Composable
fun MessageBubble(
    message: MessageEntity,
    onLongClick: () -> Unit,
    onVotePoll: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isOutgoing = message.senderId == "me"
    val bubbleColor = if (isOutgoing) WhatsAppOutgoingBubbleLight else WhatsAppIncomingBubbleLight

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        contentAlignment = if (isOutgoing) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(horizontalAlignment = if (isOutgoing) Alignment.End else Alignment.Start) {
            Surface(
                color = bubbleColor,
                shape = RoundedCornerShape(
                    topStart = 14.dp,
                    topEnd = 14.dp,
                    bottomStart = if (isOutgoing) 14.dp else 2.dp,
                    bottomEnd = if (isOutgoing) 2.dp else 14.dp
                ),
                shadowElevation = 1.dp,
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .clickable { onLongClick() }
                    .testTag("message_bubble_${message.messageId}")
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                    when (message.messageType) {
                        MessageType.POLL -> {
                            PollBubbleContent(
                                message = message,
                                onVoteOption = onVotePoll
                            )
                        }
                        MessageType.DOCUMENT -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(WhatsAppGreenPrimary, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Description,
                                        contentDescription = "Document",
                                        tint = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = message.content,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color.Black
                                    )
                                    Text("PDF Document", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                        }
                        MessageType.VOICE_NOTE -> {
                            VoiceNotePlayer(
                                durationSec = message.durationSec,
                                isOutgoing = isOutgoing
                            )
                        }
                        MessageType.IMAGE -> {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.LightGray.copy(alpha = 0.3f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.background(WhatsAppGreenDark.copy(alpha = 0.15f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = "Photo",
                                        tint = WhatsAppGreenPrimary,
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = message.content,
                                fontSize = 15.sp,
                                color = Color.Black
                            )
                        }
                        MessageType.LOCATION -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Location",
                                    tint = Color.Red,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = message.content,
                                    fontSize = 15.sp,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        MessageType.CONTACT_CARD -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Contact",
                                    tint = WhatsAppGreenPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = message.content,
                                    fontSize = 15.sp,
                                    color = Color.Black
                                )
                            }
                        }
                        else -> {
                            Text(
                                text = message.content,
                                fontSize = 15.sp,
                                color = Color.Black,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Time and Ticks footer
                    Row(
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        if (message.isStarred) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Starred",
                                tint = Color(0xFFFFB300),
                                modifier = Modifier
                                    .size(12.dp)
                                    .padding(end = 4.dp)
                            )
                        }

                        val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.timestamp))
                        Text(
                            text = timeStr,
                            fontSize = 11.sp,
                            color = Color.Gray
                        )

                        if (isOutgoing) {
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Delivered / Read",
                                tint = WhatsAppBlueTick,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // Emoji reaction sticker badge
            if (message.reaction != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp,
                    modifier = Modifier.padding(top = 2.dp, start = 6.dp, end = 6.dp)
                ) {
                    Text(
                        text = message.reaction,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PollBubbleContent(
    message: MessageEntity,
    onVoteOption: (Int) -> Unit
) {
    val pollOpts = message.pollOptions ?: ""
    val pollVotesStr = message.pollVotes ?: ""
    val options = remember(pollOpts) {
        if (pollOpts.isBlank()) emptyList()
        else pollOpts.split("||")
    }
    val votes = remember(pollVotesStr, options.size) {
        if (pollVotesStr.isBlank()) List(options.size) { 0 }
        else {
            val list = pollVotesStr.split(",").mapNotNull { it.toIntOrNull() }
            if (list.size == options.size) list else List(options.size) { 0 }
        }
    }
    val totalVotes = remember(votes) { votes.sum() }

    Column(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.BarChart, contentDescription = null, tint = WhatsAppGreenPrimary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = (message.pollQuestion ?: "").ifBlank { message.content },
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        options.forEachIndexed { index, option ->
            val voteCount = votes.getOrElse(index) { 0 }
            val progress = if (totalVotes > 0) voteCount.toFloat() / totalVotes else 0f

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onVoteOption(index) }
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = if (voteCount > 0) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (voteCount > 0) WhatsAppGreenPrimary else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = option, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                        }
                        Text(
                            text = "$voteCount votes",
                            fontSize = 12.sp,
                            color = Color.DarkGray,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = WhatsAppGreenPrimary,
                        trackColor = Color.LightGray.copy(alpha = 0.5f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Total votes: $totalVotes • Select one option",
            fontSize = 11.sp,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

@Composable
fun CreatePollDialog(
    onDismiss: () -> Unit,
    onCreatePoll: (question: String, options: List<String>) -> Unit
) {
    var question by remember { mutableStateOf("") }
    var opt1 by remember { mutableStateOf("") }
    var opt2 by remember { mutableStateOf("") }
    var opt3 by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create a Poll") },
        text = {
            Column {
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text("Ask Question") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = opt1,
                    onValueChange = { opt1 = it },
                    label = { Text("Option 1") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = opt2,
                    onValueChange = { opt2 = it },
                    label = { Text("Option 2") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = opt3,
                    onValueChange = { opt3 = it },
                    label = { Text("Option 3 (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (question.isNotBlank() && opt1.isNotBlank() && opt2.isNotBlank()) {
                        val opts = mutableListOf(opt1.trim(), opt2.trim())
                        if (opt3.isNotBlank()) opts.add(opt3.trim())
                        onCreatePoll(question.trim(), opts)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreenPrimary)
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AttachmentOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    bgColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .background(bgColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
