package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WhatsAppBlueTick
import com.example.ui.theme.WhatsAppGreenDark
import com.example.ui.theme.WhatsAppGreenPrimary
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun VoiceNotePlayer(
    durationSec: Int,
    isOutgoing: Boolean,
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var speedMultiplier by remember { mutableFloatStateOf(1.0f) }

    val safeDuration = if (durationSec > 0) durationSec else 12

    // Simulated playback loop
    LaunchedEffect(isPlaying, speedMultiplier) {
        if (isPlaying) {
            val stepMs = 100L
            val totalSteps = (safeDuration * 1000L) / (stepMs * speedMultiplier)
            var currentStep = (progress * totalSteps).toInt()

            while (isPlaying && currentStep < totalSteps) {
                delay(stepMs)
                currentStep++
                progress = (currentStep.toFloat() / totalSteps).coerceIn(0f, 1f)
            }
            if (progress >= 1f) {
                isPlaying = false
                progress = 0f
            }
        }
    }

    val barHeights = remember {
        listOf(8, 14, 22, 10, 18, 28, 16, 24, 12, 20, 30, 18, 10, 26, 14, 20, 8, 16, 22, 12)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .width(220.dp)
    ) {
        // Play/Pause circular button
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(if (isOutgoing) WhatsAppGreenPrimary else WhatsAppGreenDark, CircleShape)
                .clickable { isPlaying = !isPlaying }
                .testTag("voice_note_play_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Waveform bars
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier
                .weight(1f)
                .height(32.dp)
                .clickable {
                    // Tap to seek
                    progress = (progress + 0.25f) % 1f
                }
        ) {
            barHeights.forEachIndexed { index, height ->
                val barProgress = index.toFloat() / barHeights.size
                val isPassed = barProgress <= progress
                val activeColor = if (isOutgoing) WhatsAppGreenDark else WhatsAppBlueTick
                val inactiveColor = Color.Gray.copy(alpha = 0.35f)

                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(height.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (isPassed) activeColor else inactiveColor)
                )
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Speed toggle pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.08f))
                .clickable {
                    speedMultiplier = when (speedMultiplier) {
                        1.0f -> 1.5f
                        1.5f -> 2.0f
                        else -> 1.0f
                    }
                }
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Text(
                text = "${speedMultiplier}x",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun VoiceNoteRecorderBar(
    onSendVoiceNote: (durationSec: Int) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var secondsElapsed by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            secondsElapsed++
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp),
        shadowElevation = 4.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .padding(horizontal = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
        ) {
            // Pulsing recording red dot
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(Color.Red.copy(alpha = pulseAlpha), CircleShape)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Timer display
            val mins = secondsElapsed / 60
            val secs = secondsElapsed % 60
            Text(
                text = String.format("%02d:%02d", mins, secs),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Live voice wave
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.5.dp),
                modifier = Modifier.weight(1f)
            ) {
                repeat(16) { i ->
                    val waveHeight = remember(secondsElapsed, i) {
                        Random.nextInt(6, 26)
                    }
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(waveHeight.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(WhatsAppGreenPrimary)
                    )
                }
            }

            // Cancel / Trash button
            IconButton(
                onClick = onCancel,
                modifier = Modifier.testTag("cancel_recording_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Cancel Recording",
                    tint = Color.Red.copy(alpha = 0.8f)
                )
            }

            // Send voice note button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(WhatsAppGreenPrimary, CircleShape)
                    .clickable {
                        onSendVoiceNote(secondsElapsed.coerceAtLeast(1))
                    }
                    .testTag("send_voice_note_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send voice note",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
