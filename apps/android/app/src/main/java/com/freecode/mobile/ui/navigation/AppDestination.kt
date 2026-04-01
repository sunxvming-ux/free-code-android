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
    data object Messages : AppDestination("messages", "消息", Icons.Outlined.ChatBubbleOutline)
    data object Contacts : AppDestination("contacts", "通讯录", Icons.Outlined.Contacts)
    data object Files : AppDestination("files", "文件夹", Icons.Outlined.CollectionsBookmark)
    data object Settings : AppDestination("settings", "设置", Icons.Outlined.Settings)
}

val bottomDestinations = listOf(
    AppDestination.Messages,
    AppDestination.Contacts,
    AppDestination.Files,
    AppDestination.Settings,
)
