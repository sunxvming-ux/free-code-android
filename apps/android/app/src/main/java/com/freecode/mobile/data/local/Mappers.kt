package com.freecode.mobile.data.local

import com.freecode.mobile.domain.model.AiContact
import com.freecode.mobile.domain.model.ConversationThread
import com.freecode.mobile.domain.model.PermissionProfile
import com.freecode.mobile.domain.model.ProviderConfig
import com.freecode.mobile.domain.model.ProviderSetting
import com.freecode.mobile.domain.model.ToolPolicy
import com.freecode.mobile.domain.model.WorkspaceBinding

fun ContactEntity.toDomain(): AiContact = AiContact(
    id = id,
    name = name,
    avatarLabel = avatarLabel,
    systemPrompt = systemPrompt,
    description = description,
    provider = ProviderConfig(
        id = providerId,
        kind = providerKind,
        model = model,
        baseUrl = baseUrl,
        apiKeyAlias = apiKeyAlias,
        enabledPlugins = enabledPlugins,
    ),
    workspace = WorkspaceBinding(
        id = workspaceId,
        name = workspaceName,
        rootPath = workspaceRootPath,
        writableRoots = workspaceWritableRoots,
        externalRoots = workspaceExternalRoots,
    ),
    permissions = PermissionProfile(
        level = permissionLevel,
        toolPolicy = ToolPolicy(
            allowShell = allowShell,
            allowFilesystemRead = allowFilesystemRead,
            allowFilesystemWrite = allowFilesystemWrite,
            allowNetwork = allowNetwork,
            allowPluginInstall = allowPluginInstall,
            allowRootExecution = allowRootExecution,
        ),
        allowedPaths = allowedPaths,
        deniedPaths = deniedPaths,
    ),
    tags = tags,
)

fun AiContact.toEntity(): ContactEntity = ContactEntity(
    id = id,
    name = name,
    avatarLabel = avatarLabel,
    systemPrompt = systemPrompt,
    description = description,
    providerId = provider.id,
    providerKind = provider.kind,
    model = provider.model,
    baseUrl = provider.baseUrl,
    apiKeyAlias = provider.apiKeyAlias,
    enabledPlugins = provider.enabledPlugins,
    workspaceId = workspace.id,
    workspaceName = workspace.name,
    workspaceRootPath = workspace.rootPath,
    workspaceWritableRoots = workspace.writableRoots,
    workspaceExternalRoots = workspace.externalRoots,
    permissionLevel = permissions.level,
    allowShell = permissions.toolPolicy.allowShell,
    allowFilesystemRead = permissions.toolPolicy.allowFilesystemRead,
    allowFilesystemWrite = permissions.toolPolicy.allowFilesystemWrite,
    allowNetwork = permissions.toolPolicy.allowNetwork,
    allowPluginInstall = permissions.toolPolicy.allowPluginInstall,
    allowRootExecution = permissions.toolPolicy.allowRootExecution,
    allowedPaths = permissions.allowedPaths,
    deniedPaths = permissions.deniedPaths,
    tags = tags,
)

fun ConversationThreadEntity.toDomain(): ConversationThread = ConversationThread(
    id = id,
    aiId = aiId,
    title = title,
    lastMessagePreview = lastMessagePreview,
    updatedAt = updatedAt,
    pinned = pinned,
)

fun ConversationThread.toEntity(): ConversationThreadEntity = ConversationThreadEntity(
    id = id,
    aiId = aiId,
    title = title,
    lastMessagePreview = lastMessagePreview,
    updatedAt = updatedAt,
    pinned = pinned,
)

fun ProviderSettingEntity.toDomain(): ProviderSetting = ProviderSetting(
    id = id,
    title = title,
    enabled = enabled,
    summary = summary,
)

fun ProviderSetting.toEntity(): ProviderSettingEntity = ProviderSettingEntity(
    id = id,
    title = title,
    enabled = enabled,
    summary = summary,
)
