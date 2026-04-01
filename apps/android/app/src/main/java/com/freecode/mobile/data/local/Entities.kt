package com.freecode.mobile.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.freecode.mobile.domain.model.PermissionLevel
import com.freecode.mobile.domain.model.ProviderKind

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val id: String,
    val name: String,
    val avatarLabel: String,
    val systemPrompt: String,
    val description: String,
    val providerId: String,
    val providerKind: ProviderKind,
    val model: String,
    val baseUrl: String?,
    val apiKeyAlias: String?,
    val enabledPlugins: List<String>,
    val workspaceId: String,
    val workspaceName: String,
    val workspaceRootPath: String,
    val workspaceWritableRoots: List<String>,
    val workspaceExternalRoots: List<String>,
    val permissionLevel: PermissionLevel,
    val allowShell: Boolean,
    val allowFilesystemRead: Boolean,
    val allowFilesystemWrite: Boolean,
    val allowNetwork: Boolean,
    val allowPluginInstall: Boolean,
    val allowRootExecution: Boolean,
    val allowedPaths: List<String>,
    val deniedPaths: List<String>,
    val tags: List<String>,
)

@Entity(tableName = "threads")
data class ConversationThreadEntity(
    @PrimaryKey val id: String,
    val aiId: String,
    val title: String,
    val lastMessagePreview: String,
    val updatedAt: String,
    val pinned: Boolean,
)

@Entity(tableName = "providers")
data class ProviderSettingEntity(
    @PrimaryKey val id: String,
    val title: String,
    val enabled: Boolean,
    val summary: String,
)
