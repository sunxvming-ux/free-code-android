package com.freecode.mobile.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.freecode.mobile.data.AppRepository
import com.freecode.mobile.data.FakeAppRepository
import com.freecode.mobile.domain.files.FileNode
import com.freecode.mobile.domain.files.LocalWorkspaceFileService
import com.freecode.mobile.domain.model.AiContact
import com.freecode.mobile.domain.model.ContactDraft
import com.freecode.mobile.domain.model.ConversationThread
import com.freecode.mobile.domain.model.PermissionLevel
import com.freecode.mobile.domain.model.ProviderConfig
import com.freecode.mobile.domain.model.ProviderSetting
import com.freecode.mobile.domain.model.WorkspaceBinding
import com.freecode.mobile.domain.model.permissionPreset
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(
    private val repository: AppRepository,
    private val fileService: LocalWorkspaceFileService = LocalWorkspaceFileService(),
) : ViewModel() {
    val contacts: StateFlow<List<AiContact>> = repository.observeContacts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val threads: StateFlow<List<ConversationThread>> = repository.observeThreads()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val providers: StateFlow<List<ProviderSetting>> = repository.observeProviders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _workspacePreview = MutableStateFlow<List<FileNode>>(emptyList())
    val workspacePreview: StateFlow<List<FileNode>> = _workspacePreview

    init {
        viewModelScope.launch {
            repository.bootstrapIfEmpty(
                contacts = FakeAppRepository.contacts,
                threads = FakeAppRepository.threads,
                providers = FakeAppRepository.providers,
            )
        }
    }

    fun createContact(draft: ContactDraft) {
        viewModelScope.launch {
            val contactSnapshot = contacts.value
            val threadSnapshot = threads.value
            val safeName = draft.name.ifBlank { "New AI" }
            val workspaceName = draft.workspaceName.ifBlank { safeName }
            val workspacePath = draft.workspacePath.ifBlank {
                "/storage/emulated/0/free-code/workspaces/${safeName.lowercase().replace(' ', '-')}"
            }
            val profile = permissionPreset(draft.permissionLevel).copy(
                allowedPaths = listOf(workspacePath),
            )
            val provider = ProviderConfig(
                id = "provider-${draft.providerKind.name.lowercase()}-${contactSnapshot.size + 1}",
                kind = draft.providerKind,
                model = draft.model,
                enabledPlugins = when (draft.permissionLevel) {
                    PermissionLevel.ROOT, PermissionLevel.EXTENDED -> listOf("filesystem", "shell", "plugin-runtime")
                    else -> listOf("filesystem")
                },
            )
            val contact = AiContact(
                id = "agent-${contactSnapshot.size + 1}",
                name = safeName,
                avatarLabel = safeName.take(1),
                systemPrompt = draft.systemPrompt,
                description = draft.description.ifBlank { "Custom AI contact" },
                provider = provider,
                workspace = WorkspaceBinding(
                    id = "ws-${contactSnapshot.size + 1}",
                    name = workspaceName,
                    rootPath = workspacePath,
                    writableRoots = listOf(workspacePath),
                ),
                permissions = profile,
                tags = listOf(draft.permissionLevel.name.lowercase()),
            )
            repository.upsertContact(contact)
            repository.upsertThread(
                ConversationThread(
                    id = "thread-${threadSnapshot.size + 1}",
                    aiId = contact.id,
                    title = "${contact.name} session",
                    lastMessagePreview = "AI contact created and ready for tasks.",
                    updatedAt = Instant.now().toString(),
                    pinned = true,
                ),
            )
            fileService.createDirectory(workspacePath)
            loadWorkspacePreview(workspacePath)
        }
    }

    fun toggleProvider(id: String) {
        viewModelScope.launch {
            val provider = providers.value.firstOrNull { it.id == id } ?: return@launch
            repository.setProviderEnabled(id, !provider.enabled)
        }
    }

    fun loadWorkspacePreview(path: String) {
        viewModelScope.launch {
            _workspacePreview.value = fileService.listTree(path)
        }
    }

    companion object {
        fun factory(repository: AppRepository): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AppViewModel(repository) as T
            }
        }
    }
}
