package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.CallType
import com.example.data.model.ContactEntity
import com.example.ui.components.AvatarView
import com.example.ui.theme.WhatsAppCallActionBg
import com.example.ui.theme.WhatsAppCallBg
import com.example.ui.theme.WhatsAppCallRedEnd
import com.example.ui.theme.WhatsAppGreenPrimary
import kotlinx.coroutines.delay

@Composable
fun CallScreen(
    contact: ContactEntity,
    callType: CallType,
    isMicMuted: Boolean,
    isSpeakerOn: Boolean,
    isVideoDisabled: Boolean,
    isFrontCamera: Boolean,
    onToggleMic: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onToggleVideo: () -> Unit,
    onFlipCamera: () -> Unit,
    onEndCall: (durationSeconds: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var callStateText by remember { mutableStateOf("Calling...") }
    var durationSeconds by remember { mutableIntStateOf(0) }
    var isConnected by remember { mutableStateOf(false) }

    // Simulate realistic call connection transition
    LaunchedEffect(Unit) {
        delay(1200)
        callStateText = "Ringing..."
        delay(1800)
        callStateText = "00:00"
        isConnected = true

        while (true) {
            delay(1000)
            durationSeconds++
            val mins = durationSeconds / 60
            val secs = durationSeconds % 60
            callStateText = String.format("%02d:%02d", mins, secs)
        }
    }

    val isVideo = (callType == CallType.VIDEO) && !isVideoDisabled

    val infiniteTransition = rememberInfiniteTransition(label = "callPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WhatsAppCallBg)
    ) {
        if (isVideo) {
            // Video Call Remote Stream Simulation
            Box(modifier = Modifier.fillMaxSize()) {
                if (contact.avatarRes != null && contact.avatarRes != 0) {
                    Image(
                        painter = painterResource(id = contact.avatarRes),
                        contentDescription = "Video Feed",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(contact.avatarColorHex), Color(0xFF101D25))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        AvatarView(
                            name = contact.name,
                            avatarRes = contact.avatarRes,
                            colorHex = contact.avatarColorHex,
                            size = 140.dp
                        )
                    }
                }

                // Gradient overlays for top & bottom readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Black.copy(alpha = 0.6f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.75f)
                                )
                            )
                        )
                )

                // Local Camera Picture-in-Picture Tile
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.DarkGray,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(top = 70.dp, end = 16.dp)
                        .size(width = 100.dp, height = 140.dp)
                        .border(1.5.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .clickable { onFlipCamera() }
                        .testTag("pip_local_video_tile")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF1E293B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "You",
                                tint = WhatsAppGreenPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isFrontCamera) "Front Cam" else "Back Cam",
                                fontSize = 10.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        } else {
            // Voice Call Background with subtle ambient blur
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF1F3540),
                                Color(0xFF101D25)
                            )
                        )
                    )
            )
        }

        // Top info section
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 28.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "End-to-end encrypted",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }

            Text(
                text = contact.name,
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = callStateText,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )

            if (!isVideo) {
                Spacer(modifier = Modifier.height(50.dp))

                // Pulsing Avatar for Voice Call
                Box(
                    modifier = Modifier
                        .scale(if (!isConnected) pulseScale else 1f)
                        .size(130.dp)
                        .clip(CircleShape)
                        .border(3.dp, WhatsAppGreenPrimary.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    AvatarView(
                        name = contact.name,
                        avatarRes = contact.avatarRes,
                        colorHex = contact.avatarColorHex,
                        size = 130.dp
                    )
                }

                Spacer(modifier = Modifier.height(36.dp))

                // Animated audio soundwave
                if (isConnected) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.height(40.dp)
                    ) {
                        repeat(18) { i ->
                            val animHeight by infiniteTransition.animateFloat(
                                initialValue = 8f,
                                targetValue = 36f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(400 + (i * 50) % 500, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "waveBar$i"
                            )
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(animHeight.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(WhatsAppGreenPrimary)
                            )
                        }
                    }
                }
            }
        }

        // Bottom Controls Bar
        Surface(
            color = WhatsAppCallActionBg.copy(alpha = 0.95f),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 20.dp, horizontal = 16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Speaker toggle
                    CallControlButton(
                        icon = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                        label = "Speaker",
                        isActive = isSpeakerOn,
                        onClick = onToggleSpeaker,
                        testTag = "call_speaker_button"
                    )

                    // Video toggle
                    CallControlButton(
                        icon = if (isVideoDisabled) Icons.Default.VideocamOff else Icons.Default.Videocam,
                        label = "Video",
                        isActive = !isVideoDisabled,
                        onClick = onToggleVideo,
                        testTag = "call_video_toggle_button"
                    )

                    // Mic mute
                    CallControlButton(
                        icon = if (isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        label = if (isMicMuted) "Unmute" else "Mute",
                        isActive = isMicMuted,
                        onClick = onToggleMic,
                        testTag = "call_mute_button"
                    )

                    // Flip camera
                    if (callType == CallType.VIDEO) {
                        CallControlButton(
                            icon = Icons.Default.Cameraswitch,
                            label = "Flip",
                            isActive = false,
                            onClick = onFlipCamera,
                            testTag = "call_flip_camera_button"
                        )
                    }

                    // Red End Call Button
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(WhatsAppCallRedEnd, CircleShape)
                            .clickable { onEndCall(durationSeconds) }
                            .testTag("end_call_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "End Call",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CallControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }.testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    if (isActive) Color.White else Color.White.copy(alpha = 0.15f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Color.Black else Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}
