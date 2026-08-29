package com.nexusai.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Storage
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "Главная", Icons.Default.Home)
    data object Chat : Screen("chat", "Чат", Icons.Default.SmartToy)
    data object Code : Screen("code", "Код", Icons.Default.Code)
    data object Image : Screen("image", "Изображение", Icons.Default.Image)
    data object Video : Screen("video", "Видео", Icons.Default.PlayCircle)
    data object Agents : Screen("agents", "Агенты", Icons.Default.SmartToy)
    data object Files : Screen("files", "Файлы", Icons.Default.Folder)
    data object Memory : Screen("memory", "Память", Icons.Default.Storage)
    data object Marketplace : Screen("marketplace", "Маркетплейс", Icons.Default.ShoppingBag)
    data object Settings : Screen("settings", "Настройки", Icons.Default.Settings)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Chat,
    Screen.Image,
    Screen.Agents,
    Screen.Settings
)

val drawerNavItems = listOf(
    Screen.Home,
    Screen.Chat,
    Screen.Code,
    Screen.Image,
    Screen.Video,
    Screen.Agents,
    Screen.Files,
    Screen.Memory,
    Screen.Marketplace,
    Screen.Settings
)
