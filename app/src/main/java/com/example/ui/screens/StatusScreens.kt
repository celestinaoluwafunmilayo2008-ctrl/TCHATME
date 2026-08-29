package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StatusWithContact
import com.example.ui.components.AvatarView
import com.example.ui.theme.WhatsAppGreenPrimary

@Composable
fun StatusViewerScreen(
    statusWithContact: StatusWithContact,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 5000, easing = LinearEasing)
        )
        onClose()
    }

    val contact = statusWithContact.contact
    val status = statusWithContact.status

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(status.backgroundColorHex))
            .clickable { onClose() }
    ) {
        // Top Bars (Progress bar + Contact info)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(12.dp)
        ) {
            // Story Progress Indicator
            LinearProgressIndicator(
                progress = { progress.value },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                AvatarView(
                    name = contact.name,
                    avatarRes = contact.avatarRes,
                    colorHex = contact.avatarColorHex,
                    size = 40.dp
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = contact.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = formatTimestamp(status.timestamp),
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }

                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }
        }

        // Center Story Text Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = status.textCaption,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                lineHeight = 36.sp
            )
        }

        // Bottom Reply Bar
        Surface(
            color = Color.Black.copy(alpha = 0.4f),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Reply to status...",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun CreateStatusScreen(
    onPostStatus: (caption: String, bgHex: Long) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgPalette = listOf(
        0xFF075E54, 0xFF128C7E, 0xFF00A884, 0xFFE91E63,
        0xFF9C27B0, 0xFF3F51B5, 0xFFFF5722, 0xFF673AB7
    )
    var currentBgIndex by remember { mutableIntStateOf(0) }
    var captionText by remember { mutableStateOf("") }

    val currentBg = bgPalette[currentBgIndex]

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(currentBg))
    ) {
        // Top Action Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Change Background Color Button
                IconButton(
                    onClick = {
                        currentBgIndex = (currentBgIndex + 1) % bgPalette.size
                    },
                    modifier = Modifier.testTag("status_color_picker_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ColorLens,
                        contentDescription = "Change Color",
                        tint = Color.White
                    )
                }

                IconButton(
                    onClick = {
                        captionText += " ✨"
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEmotions,
                        contentDescription = "Emoji",
                        tint = Color.White
                    )
                }
            }
        }

        // Status Text Input
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            BasicTextField(
                value = captionText,
                onValueChange = { captionText = it },
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                cursorBrush = SolidColor(Color.White),
                modifier = Modifier.fillMaxWidth().testTag("create_status_input"),
                decorationBox = { innerTextField ->
                    if (captionText.isEmpty()) {
                        Text(
                            text = "Type a status...",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    innerTextField()
                }
            )
        }

        // Post Floating Action Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .imePadding()
                .navigationBarsPadding()
                .padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.White, CircleShape)
                    .clickable {
                        if (captionText.isNotBlank()) {
                            onPostStatus(captionText.trim(), currentBg)
                        }
                    }
                    .testTag("post_status_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Post Status",
                    tint = WhatsAppGreenPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
