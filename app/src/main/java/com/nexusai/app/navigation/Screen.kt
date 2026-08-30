package com.nexusai.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Splits
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material.icons.filled.Widgets
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
    data object CodePlayground : Screen("code_playground", "Playground", Icons.Default.PhoneAndroid)
    data object Templates : Screen("templates", "Шаблоны", Icons.Default.TextSnippet)
    data object Export : Screen("export", "Экспорт", Icons.Default.IosShare)
    data object Marketplace : Screen("marketplace", "Маркетплейс", Icons.Default.ShoppingCart)
    data object Prompts : Screen("prompts", "Промпты", Icons.Default.Lightbulb)
    data object Modules : Screen("modules", "Модули", Icons.Default.Widgets)
    data object SplitView : Screen("split_view", "Split View", Icons.Default.SmartToy)
    data object AIRouter : Screen("ai_router", "AI Router", Icons.Default.SwapHoriz)
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
    Screen.Templates,
    Screen.CodePlayground,
    Screen.Marketplace,
    Screen.Prompts,
    Screen.Modules,
    Screen.SplitView,
    Screen.AIRouter,
    Screen.Export,
    Screen.Settings
)
