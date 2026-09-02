package com.nexusai.core.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
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

private val NexusHighContrastScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF7C3AED),
    onPrimaryContainer = Color.White,

    secondary = Color(0xFF64B5F6),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF1E3A5F),
    onSecondaryContainer = Color.White,

    tertiary = Color(0xFF4DD0E1),
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF006064),
    onTertiaryContainer = Color.White,

    background = Color.Black,
    onBackground = Color.White,

    surface = Color(0xFF0D0D0D),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1A1A1A),
    onSurfaceVariant = Color(0xFFE0E0E0),

    error = Color(0xFFFF6B6B),
    onError = Color.Black,
    errorContainer = Color(0xFF4A0000),
    onErrorContainer = Color(0xFFFFB4AB),

    outline = Color(0xFF444444),
    outlineVariant = Color(0xFF2A2A2A),
)

fun scaledTypography(scale: Int) = Typography(
    displayLarge = TextStyle(fontSize = (57 + scale * 4).sp, lineHeight = (64 + scale * 5).sp),
    displayMedium = TextStyle(fontSize = (45 + scale * 3).sp, lineHeight = (52 + scale * 4).sp),
    displaySmall = TextStyle(fontSize = (36 + scale * 3).sp, lineHeight = (44 + scale * 3).sp),
    headlineLarge = TextStyle(fontSize = (32 + scale * 2).sp, lineHeight = (40 + scale * 3).sp, fontWeight = FontWeight.SemiBold),
    headlineMedium = TextStyle(fontSize = (28 + scale * 2).sp, lineHeight = (36 + scale * 3).sp, fontWeight = FontWeight.SemiBold),
    headlineSmall = TextStyle(fontSize = (24 + scale * 2).sp, lineHeight = (32 + scale * 2).sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontSize = (22 + scale * 2).sp, lineHeight = (28 + scale * 2).sp, fontWeight = FontWeight.Medium),
    titleMedium = TextStyle(fontSize = (16 + scale).sp, lineHeight = (24 + scale).sp, fontWeight = FontWeight.Medium),
    titleSmall = TextStyle(fontSize = (14 + scale).sp, lineHeight = (20 + scale).sp, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontSize = (16 + scale).sp, lineHeight = (24 + scale).sp),
    bodyMedium = TextStyle(fontSize = (14 + scale).sp, lineHeight = (20 + scale).sp),
    bodySmall = TextStyle(fontSize = (12 + scale).sp, lineHeight = (16 + scale).sp),
    labelLarge = TextStyle(fontSize = (14 + scale).sp, lineHeight = (20 + scale).sp, fontWeight = FontWeight.Medium),
    labelMedium = TextStyle(fontSize = (12 + scale).sp, lineHeight = (16 + scale).sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = (11 + scale).sp, lineHeight = (16 + scale).sp, fontWeight = FontWeight.Medium),
)

@Composable
fun NexsusAITheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    fontScale: Int = 1,
    highContrast: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (highContrast) NexusHighContrastScheme else NexusDarkColorScheme

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
        typography = scaledTypography(fontScale),
        content = content
    )
}
