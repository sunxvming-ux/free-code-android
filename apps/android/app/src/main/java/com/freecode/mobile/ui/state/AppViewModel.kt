package com.freecode.mobile.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.freecode.mobile.data.FakeAppRepository
import com.freecode.mobile.domain.model.ContactDraft
import com.freecode.mobile.domain.model.ConversationThread
import com.freecode.mobile.domain.model.PermissionLevel
import com.freecode.mobile.domain.model.ProviderConfig
import com.freecode.mobile.domain.model.WorkspaceBinding
import com.freecode.mobile.domain.model.permissionPreset
import java.time.Instant

class AppViewModel : ViewModel() {
    var contacts by mutableStateOf(FakeAppRepository.contacts)
        private set

    var threads by mutableStateOf(FakeAppRepository.threads)
        private set

    var providers by mutableStateOf(FakeAppRepository.providers)
        private set

    fun createContact(draft: ContactDraft) {
        val safeName = draft.name.ifBlank { "New AI" }
        val workspaceName = draft.workspaceName.ifBlank { safeName }
        val workspacePath = draft.workspacePath.ifBlank {
            "/storage/emulated/0/free-code/workspaces/${safeName.lowercase().replace(' ', '-')}"
        }
        val profile = permissionPreset(draft.permissionLevel).copy(
            allowedPaths = listOf(workspacePath),
        )
        val provider = ProviderConfig(
            id = "provider-${draft.providerKind.name.lowercase()}-${contacts.size + 1}",
            kind = draft.providerKind,
            model = draft.model,
            enabledPlugins = when (draft.permissionLevel) {
                PermissionLevel.ROOT, PermissionLevel.EXTENDED -> listOf("filesystem", "shell", "plugin-runtime")
                else -> listOf("filesystem")
            },
        )
        val contact = com.freecode.mobile.domain.model.AiContact(
            id = "agent-${contacts.size + 1}",
            name = safeName,
            avatarLabel = safeName.take(1),
            systemPrompt = draft.systemPrompt,
            description = draft.description.ifBlank { "Custom AI contact" },
            provider = provider,
            workspace = WorkspaceBinding(
                id = "ws-${contacts.size + 1}",
                name = workspaceName,
                rootPath = workspacePath,
                writableRoots = listOf(workspacePath),
            ),
            permissions = profile,
            tags = listOf(draft.permissionLevel.name.lowercase()),
        )
        contacts = listOf(contact) + contacts
        threads = listOf(
            ConversationThread(
                id = "thread-${threads.size + 1}",
                aiId = contact.id,
                title = "${contact.name} session",
                lastMessagePreview = "AI contact created and ready for tasks.",
                updatedAt = Instant.now().toString(),
                pinned = true,
            ),
        ) + threads
    }

    fun toggleProvider(id: String) {
        providers = providers.map {
            if (it.id == id) it.copy(enabled = !it.enabled) else it
        }
    }
}
