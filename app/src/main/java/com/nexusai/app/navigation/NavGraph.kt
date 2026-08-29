package com.nexusai.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.nexusai.app.ui.FilesScreen
import com.nexusai.app.ui.TemplatesScreen
import com.nexusai.domain.repository.TaskTemplateRepository
import com.nexusai.feature.aiprovider.ui.AIProviderScreen
import com.nexusai.feature.editor.ui.EditorScreen
import com.nexusai.feature.settings.ui.SettingsScreen
import com.nexusai.feature.tabs.ui.TabsScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    taskTemplateRepository: TaskTemplateRepository? = null
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            TabsScreen()
        }
        composable(Screen.Chat.route) {
            TabsScreen()
        }
        composable(Screen.Code.route) {
            EditorScreen()
        }
        composable(Screen.Image.route) {
            TabsScreen()
        }
        composable(Screen.Video.route) {
            TabsScreen()
        }
        composable(Screen.Agents.route) {
            TabsScreen()
        }
        composable(Screen.Files.route) {
            FilesScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Memory.route) {
            TabsScreen()
        }
        composable(Screen.Templates.route) {
            if (taskTemplateRepository != null) {
                TemplatesScreen(
                    repository = taskTemplateRepository,
                    onBack = { navController.popBackStack() }
                )
            }
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
