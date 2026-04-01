package com.freecode.mobile.domain.model

enum class PermissionLevel {
    SANDBOX,
    WORKSPACE,
    EXTENDED,
    ROOT,
}

enum class ProviderKind {
    ANTHROPIC,
    OPENAI,
    BEDROCK,
    VERTEX,
    FOUNDRY,
    CUSTOM,
}

data class WorkspaceBinding(
    val id: String,
    val name: String,
    val rootPath: String,
    val writableRoots: List<String>,
    val externalRoots: List<String> = emptyList(),
)

data class ToolPolicy(
    val allowShell: Boolean,
    val allowFilesystemRead: Boolean,
    val allowFilesystemWrite: Boolean,
    val allowNetwork: Boolean,
    val allowPluginInstall: Boolean,
    val allowRootExecution: Boolean,
)

data class PermissionProfile(
    val level: PermissionLevel,
    val toolPolicy: ToolPolicy,
    val allowedPaths: List<String>,
    val deniedPaths: List<String> = emptyList(),
)

data class ProviderConfig(
    val id: String,
    val kind: ProviderKind,
    val model: String,
    val baseUrl: String? = null,
    val apiKeyAlias: String? = null,
    val enabledPlugins: List<String> = emptyList(),
)

data class AiContact(
    val id: String,
    val name: String,
    val avatarLabel: String,
    val systemPrompt: String,
    val description: String,
    val provider: ProviderConfig,
    val workspace: WorkspaceBinding,
    val permissions: PermissionProfile,
    val tags: List<String> = emptyList(),
)

data class ConversationThread(
    val id: String,
    val aiId: String,
    val title: String,
    val lastMessagePreview: String,
    val updatedAt: String,
    val pinned: Boolean = false,
)

data class ProviderSetting(
    val id: String,
    val title: String,
    val enabled: Boolean,
    val summary: String,
)
