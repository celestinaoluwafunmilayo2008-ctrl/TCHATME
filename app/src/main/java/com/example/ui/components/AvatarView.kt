package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WhatsAppGreenPrimary

@Composable
fun AvatarView(
    name: String,
    avatarRes: Int? = null,
    colorHex: Long = 0xFF00A884,
    size: Dp = 48.dp,
    hasStory: Boolean = false,
    isStoryViewed: Boolean = false,
    isOnline: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val outerModifier = modifier
        .size(size)
        .then(
            if (hasStory) {
                Modifier.border(
                    width = 2.5.dp,
                    color = if (isStoryViewed) Color.LightGray else WhatsAppGreenPrimary,
                    shape = CircleShape
                ).padding(3.dp)
            } else {
                Modifier
            }
        )
        .clip(CircleShape)
        .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)

    Box(modifier = outerModifier) {
        if (avatarRes != null && avatarRes != 0) {
            Image(
                painter = painterResource(id = avatarRes),
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            val baseColor = Color(colorHex)
            val gradient = Brush.linearGradient(
                colors = listOf(baseColor, baseColor.copy(alpha = 0.8f))
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(gradient),
                contentAlignment = Alignment.Center
            ) {
                val initial = name.trim().firstOrNull()?.uppercase() ?: "?"
                Text(
                    text = initial,
                    color = Color.White,
                    fontSize = (size.value * 0.42f).sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (isOnline && !hasStory) {
            Box(
                modifier = Modifier
                    .size(size * 0.28f)
                    .align(Alignment.BottomEnd)
                    .background(WhatsAppGreenPrimary, CircleShape)
                    .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
            )
        }
    }
}
