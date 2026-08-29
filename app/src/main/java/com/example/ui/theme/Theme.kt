package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = WhatsAppGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = WhatsAppGreenDark,
    onPrimaryContainer = Color.White,
    secondary = WhatsAppGreenTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF233138),
    onSecondaryContainer = Color(0xFFE9EDEF),
    tertiary = WhatsAppBlueTick,
    background = WhatsAppSurfaceDark,
    onBackground = WhatsAppTextPrimaryDark,
    surface = WhatsAppSurfaceDark,
    onSurface = WhatsAppTextPrimaryDark,
    surfaceVariant = WhatsAppSurfaceHeaderDark,
    onSurfaceVariant = WhatsAppTextSecondaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = WhatsAppGreenDark,
    onPrimary = Color.White,
    primaryContainer = WhatsAppGreenPrimary,
    onPrimaryContainer = Color.White,
    secondary = WhatsAppGreenTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD9FDD3),
    onSecondaryContainer = Color(0xFF111B21),
    tertiary = WhatsAppBlueTick,
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF111B21),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111B21),
    surfaceVariant = Color(0xFFF0F2F5),
    onSurfaceVariant = Color(0xFF667781)
)

@Composable
fun TChatMeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
