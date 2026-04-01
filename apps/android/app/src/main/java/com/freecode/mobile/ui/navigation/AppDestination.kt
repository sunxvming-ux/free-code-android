package com.freecode.mobile.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class AppDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    data object Messages : AppDestination("messages", "??", Icons.Outlined.ChatBubbleOutline)
    data object Contacts : AppDestination("contacts", "???", Icons.Outlined.Contacts)
    data object Files : AppDestination("files", "???", Icons.Outlined.CollectionsBookmark)
    data object Settings : AppDestination("settings", "??", Icons.Outlined.Settings)
}

val bottomDestinations = listOf(
    AppDestination.Messages,
    AppDestination.Contacts,
    AppDestination.Files,
    AppDestination.Settings,
)
