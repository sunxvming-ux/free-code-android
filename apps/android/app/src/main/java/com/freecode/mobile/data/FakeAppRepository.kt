package com.freecode.mobile.data

import com.freecode.mobile.domain.model.AiContact
import com.freecode.mobile.domain.model.ConversationThread
import com.freecode.mobile.domain.model.PermissionLevel
import com.freecode.mobile.domain.model.PermissionProfile
import com.freecode.mobile.domain.model.ProviderConfig
import com.freecode.mobile.domain.model.ProviderKind
import com.freecode.mobile.domain.model.ProviderSetting
import com.freecode.mobile.domain.model.ToolPolicy
import com.freecode.mobile.domain.model.WorkspaceBinding

object FakeAppRepository {
    val contacts = listOf(
        AiContact(
            id = "agent-architect",
            name = "Architect",
            avatarLabel = "A",
            systemPrompt = "Design architecture, review code, and drive Android migration tasks.",
            description = "High-permission engineering AI",
            provider = ProviderConfig(
                id = "provider-openai",
                kind = ProviderKind.OPENAI,
                model = "gpt-5.4",
                enabledPlugins = listOf("filesystem", "shell", "plugin-runtime"),
            ),
            workspace = WorkspaceBinding(
                id = "ws-main",
                name = "Main Project",
                rootPath = "/storage/emulated/0/free-code/workspaces/main",
                writableRoots = listOf("/storage/emulated/0/free-code/workspaces/main"),
            ),
            permissions = PermissionProfile(
                level = PermissionLevel.ROOT,
                toolPolicy = ToolPolicy(
                    allowShell = true,
                    allowFilesystemRead = true,
                    allowFilesystemWrite = true,
                    allowNetwork = true,
                    allowPluginInstall = true,
                    allowRootExecution = true,
                ),
                allowedPaths = listOf(
                    "/storage/emulated/0/free-code/workspaces/main",
                    "/data/local/tmp",
                ),
            ),
            tags = listOf("root", "workspace", "primary"),
        ),
        AiContact(
            id = "agent-helper",
            name = "Helper",
            avatarLabel = "H",
            systemPrompt = "Handle lightweight tasks, docs, and workspace organization.",
            description = "Workspace-scoped assistant",
            provider = ProviderConfig(
                id = "provider-anthropic",
                kind = ProviderKind.ANTHROPIC,
                model = "claude-sonnet-4-6",
                enabledPlugins = listOf("filesystem"),
            ),
            workspace = WorkspaceBinding(
                id = "ws-docs",
                name = "Docs Workspace",
                rootPath = "/storage/emulated/0/free-code/workspaces/docs",
                writableRoots = listOf("/storage/emulated/0/free-code/workspaces/docs"),
            ),
            permissions = PermissionProfile(
                level = PermissionLevel.WORKSPACE,
                toolPolicy = ToolPolicy(
                    allowShell = false,
                    allowFilesystemRead = true,
                    allowFilesystemWrite = true,
                    allowNetwork = true,
                    allowPluginInstall = false,
                    allowRootExecution = false,
                ),
                allowedPaths = listOf("/storage/emulated/0/free-code/workspaces/docs"),
            ),
            tags = listOf("docs"),
        ),
    )

    val threads = listOf(
        ConversationThread(
            id = "thread-1",
            aiId = "agent-architect",
            title = "Android migration plan",
            lastMessagePreview = "Scaffolded Android modules, navigation, and permission models.",
            updatedAt = "2026-04-02 09:30",
            pinned = true,
        ),
        ConversationThread(
            id = "thread-2",
            aiId = "agent-helper",
            title = "Documentation cleanup",
            lastMessagePreview = "Please extend the README with Android adaptation notes.",
            updatedAt = "2026-04-02 08:15",
        ),
    )

    val providers = listOf(
        ProviderSetting("anthropic", "Anthropic", true, "Use API key or OAuth"),
        ProviderSetting("openai", "OpenAI / Codex", true, "Primary coding-focused provider"),
        ProviderSetting("bedrock", "AWS Bedrock", false, "Enterprise cloud routing"),
        ProviderSetting("vertex", "Google Vertex", false, "Connect through a GCP project"),
    )
}
