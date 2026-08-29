package com.nexusai.core.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val NexusDarkColorScheme = darkColorScheme(
    primary = NexusPurple,
    onPrimary = NexusTextOnPurple,
    primaryContainer = NexusPurpleDark,
    onPrimaryContainer = NexusPurpleLight,

    secondary = NexusBlue,
    onSecondary = Color.White,
    secondaryContainer = NexusSurfaceVariant,
    onSecondaryContainer = NexusTextPrimary,

    tertiary = NexusCyan,
    onTertiary = Color.White,
    tertiaryContainer = NexusSurfaceVariant,
    onTertiaryContainer = NexusTextPrimary,

    background = NexusBackground,
    onBackground = NexusTextPrimary,

    surface = NexusSurface,
    onSurface = NexusTextPrimary,
    surfaceVariant = NexusSurfaceLight,
    onSurfaceVariant = NexusTextSecondary,

    error = ErrorColor,
    onError = Color.White,
    errorContainer = Color(0xFF3D1114),
    onErrorContainer = Color(0xFFFFB4AB),

    outline = NexusDivider,
    outlineVariant = NexusSurfaceVariant,
)

@Composable
fun NexsusAITheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = NexusDarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.background.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = NexsusTypography,
        content = content
    )
}
