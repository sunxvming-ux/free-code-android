package com.freecode.mobile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.padding
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
            composable(AppDestination.Messages.route) { MessagesScreen(viewModel) }
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
