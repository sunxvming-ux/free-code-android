package com.freecode.mobile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.freecode.mobile.ui.screens.ChatScreen
import com.freecode.mobile.ui.screens.ContactsScreen
import com.freecode.mobile.ui.screens.FilesScreen
import com.freecode.mobile.ui.screens.MainScaffold
import com.freecode.mobile.ui.screens.MessagesScreen
import com.freecode.mobile.ui.screens.SettingsScreen
import com.freecode.mobile.ui.state.AppViewModel

@Composable
fun FreeCodeNavGraph(
    navController: NavHostController,
    viewModel: AppViewModel,
) {
    MainScaffold(navController = navController) { innerPadding ->
        NavHost(
            modifier = Modifier.padding(innerPadding),
            navController = navController,
            startDestination = AppDestination.Messages.route,
        ) {
            composable(AppDestination.Messages.route) {
                MessagesScreen(
                    viewModel = viewModel,
                    onOpenThread = { threadId ->
                        viewModel.selectThread(threadId)
                        navController.navigate(AppDestination.Chat.route(threadId))
                    },
                )
            }
            composable(
                route = AppDestination.Chat.route,
                arguments = listOf(navArgument("threadId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val threadId = backStackEntry.arguments?.getString("threadId").orEmpty()
                ChatScreen(
                    viewModel = viewModel,
                    threadId = threadId,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(AppDestination.Contacts.route) { ContactsScreen(viewModel) }
            composable(AppDestination.Files.route) { FilesScreen(viewModel) }
            composable(AppDestination.Settings.route) { SettingsScreen(viewModel) }
        }
    }
}

fun NavHostController.navigateBottom(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
