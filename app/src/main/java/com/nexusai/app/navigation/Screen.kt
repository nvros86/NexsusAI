package com.nexusai.app.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TextSnippet
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.ui.graphics.vector.ImageVector
import com.nexusai.app.R

sealed class Screen(val route: String, @StringRes val titleRes: Int, val icon: ImageVector) {
    data object Home : Screen("home", R.string.nav_home, Icons.Default.Home)
    data object Chat : Screen("chat", R.string.nav_chat, Icons.Default.SmartToy)
    data object Code : Screen("code", R.string.nav_code, Icons.Default.Code)
    data object Image : Screen("image", R.string.nav_image, Icons.Default.Image)
    data object Video : Screen("video", R.string.nav_video, Icons.Default.PlayCircle)
    data object Agents : Screen("agents", R.string.nav_agents, Icons.Default.SmartToy)
    data object Files : Screen("files", R.string.nav_files, Icons.Default.Folder)
    data object Memory : Screen("memory", R.string.nav_memory, Icons.Default.Storage)
    data object CodePlayground : Screen("code_playground", R.string.playground_title, Icons.Default.PhoneAndroid)
    data object Templates : Screen("templates", R.string.nav_templates, Icons.AutoMirrored.Filled.TextSnippet)
    data object Export : Screen("export", R.string.nav_export, Icons.Default.IosShare)
    data object Marketplace : Screen("marketplace", R.string.nav_marketplace, Icons.Default.ShoppingCart)
    data object Prompts : Screen("prompts", R.string.nav_prompts, Icons.Default.Lightbulb)
    data object Modules : Screen("modules", R.string.nav_modules, Icons.Default.Widgets)
    data object SplitView : Screen("split_view", R.string.split_view_title, Icons.Default.SmartToy)
    data object VoiceMode : Screen("voice_mode", R.string.voice_mode_title, Icons.Default.Mic)
    data object AIRouter : Screen("ai_router", R.string.ai_router_title, Icons.Default.SwapHoriz)
    data object Settings : Screen("settings", R.string.nav_settings, Icons.Default.Settings)
    data object AIProvider : Screen("ai_provider/{providerId}", R.string.nav_ai_provider, Icons.Default.SmartToy)
    data object Chains : Screen("chains", R.string.nav_chains, Icons.Default.SwapHoriz)
    data object ChainDetail : Screen("chain_detail/{chainId}", R.string.nav_chain, Icons.Default.SwapHoriz)
    data object Plugins : Screen("plugins", R.string.nav_plugins, Icons.Default.Widgets)
    data object LocalAI : Screen("local_ai", R.string.local_ai_title, Icons.Default.SmartToy)
    data object TeamWorkspaces : Screen("team_workspaces", R.string.workspaces_title, Icons.Default.SmartToy)
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
    Screen.Chains,
    Screen.Plugins,
    Screen.LocalAI,
    Screen.TeamWorkspaces,
    Screen.SplitView,
    Screen.VoiceMode,
    Screen.AIRouter,
    Screen.Export,
    Screen.Settings
)
