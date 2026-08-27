package com.nexusai.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "Home", Icons.Default.Home)
    data object Tabs : Screen("tabs", "Tabs", Icons.Default.Chat)
    data object AIProvider : Screen("ai_provider", "AI Provider", Icons.Default.Settings)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Tabs,
    Screen.AIProvider,
    Screen.Settings
)
