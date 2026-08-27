package com.nexusai.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.nexusai.feature.aiprovider.ui.AIProviderScreen
import com.nexusai.feature.editor.ui.EditorScreen
import com.nexusai.feature.settings.ui.SettingsScreen
import com.nexusai.feature.tabs.ui.TabsScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            EditorScreen()
        }
        composable(Screen.Tabs.route) {
            TabsScreen()
        }
        composable(Screen.AIProvider.route) {
            AIProviderScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
