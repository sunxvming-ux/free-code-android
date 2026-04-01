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
import com.freecode.mobile.domain.model.ProviderApiConfig
import com.freecode.mobile.domain.model.ProviderSetting
import com.freecode.mobile.domain.model.WorkspaceBinding
import com.freecode.mobile.domain.model.permissionPreset
import com.freecode.mobile.domain.model.toDraft
import com.freecode.mobile.domain.service.ModelRequest
import com.freecode.mobile.domain.service.StubModelGateway
import com.freecode.mobile.domain.system.AndroidShellBridge
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(
    private val repository: AppRepository,
    private val fileService: LocalWorkspaceFileService = LocalWorkspaceFileService(),
    private val shellBridge: AndroidShellBridge = AndroidShellBridge(),
    private val modelGateway: StubModelGateway = StubModelGateway(),
) : ViewModel() {
    val contacts: StateFlow<List<AiContact>> = repository.observeContacts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val threads: StateFlow<List<ConversationThread>> = repository.observeThreads()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val providers: StateFlow<List<ProviderSetting>> = repository.observeProviders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _workspacePreview = MutableStateFlow<List<FileNode>>(emptyList())
    val workspacePreview: StateFlow<List<FileNode>> = _workspacePreview

    private val _shellUiState = MutableStateFlow(ShellUiState())
    val shellUiState: StateFlow<ShellUiState> = _shellUiState

    private val _fileEditorUiState = MutableStateFlow(FileEditorUiState())
    val fileEditorUiState: StateFlow<FileEditorUiState> = _fileEditorUiState

    private val _editingContactId = MutableStateFlow<String?>(null)
    val editingContactId: StateFlow<String?> = _editingContactId

    private val _messageComposerUiState = MutableStateFlow(MessageComposerUiState())
    val messageComposerUiState: StateFlow<MessageComposerUiState> = _messageComposerUiState

    private val _providerConfigUiState = MutableStateFlow(ProviderConfigUiState())
    val providerConfigUiState: StateFlow<ProviderConfigUiState> = _providerConfigUiState

    init {
        viewModelScope.launch {
            repository.bootstrapIfEmpty(
                contacts = FakeAppRepository.contacts,
                threads = FakeAppRepository.threads,
                providers = FakeAppRepository.providers,
            )
            _messageComposerUiState.value = _messageComposerUiState.value.copy(
                selectedThreadId = FakeAppRepository.threads.firstOrNull()?.id.orEmpty(),
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

    fun startEditingContact(contactId: String) {
        _editingContactId.value = contactId
    }

    fun stopEditingContact() {
        _editingContactId.value = null
    }

    fun getEditingDraft(): ContactDraft? =
        contacts.value.firstOrNull { it.id == editingContactId.value }?.toDraft()

    fun updateContact(contactId: String, draft: ContactDraft) {
        val existing = contacts.value.firstOrNull { it.id == contactId } ?: return
        viewModelScope.launch {
            val workspacePath = draft.workspacePath.ifBlank { existing.workspace.rootPath }
            val workspaceName = draft.workspaceName.ifBlank { existing.workspace.name }
            val profile = permissionPreset(draft.permissionLevel).copy(
                allowedPaths = listOf(workspacePath),
            )
            val updated = existing.copy(
                name = draft.name.ifBlank { existing.name },
                avatarLabel = draft.name.ifBlank { existing.name }.take(1),
                systemPrompt = draft.systemPrompt,
                description = draft.description.ifBlank { existing.description },
                provider = existing.provider.copy(
                    kind = draft.providerKind,
                    model = draft.model,
                    enabledPlugins = when (draft.permissionLevel) {
                        PermissionLevel.ROOT, PermissionLevel.EXTENDED -> listOf("filesystem", "shell", "plugin-runtime")
                        else -> listOf("filesystem")
                    },
                ),
                workspace = existing.workspace.copy(
                    name = workspaceName,
                    rootPath = workspacePath,
                    writableRoots = listOf(workspacePath),
                ),
                permissions = profile,
                tags = listOf(draft.permissionLevel.name.lowercase()),
            )
            repository.upsertContact(updated)
            fileService.createDirectory(workspacePath)
            loadWorkspacePreview(workspacePath)
            stopEditingContact()
        }
    }

    fun deleteContact(contactId: String) {
        viewModelScope.launch {
            repository.deleteContact(contactId)
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
            _fileEditorUiState.value = _fileEditorUiState.value.copy(
                activeWorkspacePath = path,
                statusMessage = "Loaded workspace preview for $path",
            )
        }
    }

    fun updateNewFileName(name: String) {
        _fileEditorUiState.value = _fileEditorUiState.value.copy(newFileName = name)
    }

    fun createFileInActiveWorkspace() {
        val snapshot = _fileEditorUiState.value
        if (snapshot.activeWorkspacePath.isBlank() || snapshot.newFileName.isBlank()) return
        viewModelScope.launch {
            val filePath = "${snapshot.activeWorkspacePath}/${snapshot.newFileName}"
            val success = fileService.createFile(filePath, "")
            _fileEditorUiState.value = _fileEditorUiState.value.copy(
                statusMessage = if (success) "Created $filePath" else "Failed to create $filePath",
            )
            loadWorkspacePreview(snapshot.activeWorkspacePath)
            if (success) readFile(filePath)
        }
    }

    fun readFile(path: String) {
        viewModelScope.launch {
            val content = fileService.readText(path)
            _fileEditorUiState.value = _fileEditorUiState.value.copy(
                selectedFilePath = path,
                selectedFileContent = content,
                dirty = false,
                statusMessage = "Opened $path",
            )
        }
    }

    fun updateSelectedFileContent(content: String) {
        _fileEditorUiState.value = _fileEditorUiState.value.copy(
            selectedFileContent = content,
            dirty = true,
        )
    }

    fun saveSelectedFile() {
        val snapshot = _fileEditorUiState.value
        if (snapshot.selectedFilePath.isBlank()) return
        viewModelScope.launch {
            val success = fileService.writeText(snapshot.selectedFilePath, snapshot.selectedFileContent)
            _fileEditorUiState.value = _fileEditorUiState.value.copy(
                dirty = !success,
                statusMessage = if (success) "Saved ${snapshot.selectedFilePath}" else "Failed to save ${snapshot.selectedFilePath}",
            )
            loadWorkspacePreview(snapshot.activeWorkspacePath)
        }
    }

    fun updateShellCommand(command: String) {
        _shellUiState.value = _shellUiState.value.copy(command = command)
    }

    fun updateShellRoot(enabled: Boolean) {
        _shellUiState.value = _shellUiState.value.copy(useRoot = enabled)
    }

    fun runShellCommand() {
        val snapshot = _shellUiState.value
        if (snapshot.command.isBlank()) return
        viewModelScope.launch {
            _shellUiState.value = snapshot.copy(
                running = true,
                exitCode = null,
                stdout = "",
                stderr = "",
            )
            val result = shellBridge.execute(snapshot.command, snapshot.useRoot)
            _shellUiState.value = _shellUiState.value.copy(
                running = false,
                exitCode = result.exitCode,
                stdout = result.stdout,
                stderr = result.stderr,
            )
        }
    }

    fun runProviderHealthcheck() {
        val provider = providers.value.firstOrNull { it.enabled } ?: return
        viewModelScope.launch {
            _providerConfigUiState.value = _providerConfigUiState.value.copy(
                providerId = provider.id,
                defaultModel = provider.title,
            )
            val result = modelGateway.send(
                config = ProviderApiConfig(
                    providerId = _providerConfigUiState.value.providerId.ifBlank { provider.id },
                    baseUrl = _providerConfigUiState.value.baseUrl,
                    apiKey = _providerConfigUiState.value.apiKey,
                    defaultModel = _providerConfigUiState.value.defaultModel.ifBlank { provider.title },
                ),
                request = ModelRequest(
                    prompt = "healthcheck",
                    model = _providerConfigUiState.value.defaultModel.ifBlank { provider.title },
                ),
            )
            _shellUiState.value = _shellUiState.value.copy(
                stdout = result.getOrNull()?.content ?: "",
                stderr = result.exceptionOrNull()?.message ?: "",
            )
        }
    }

    fun updateProviderBaseUrl(value: String) {
        _providerConfigUiState.value = _providerConfigUiState.value.copy(baseUrl = value)
    }

    fun updateProviderApiKey(value: String) {
        _providerConfigUiState.value = _providerConfigUiState.value.copy(apiKey = value)
    }

    fun updateProviderDefaultModel(value: String) {
        _providerConfigUiState.value = _providerConfigUiState.value.copy(defaultModel = value)
    }

    fun selectThread(threadId: String) {
        _messageComposerUiState.value = _messageComposerUiState.value.copy(selectedThreadId = threadId)
    }

    fun updateComposerPrompt(prompt: String) {
        _messageComposerUiState.value = _messageComposerUiState.value.copy(prompt = prompt)
    }

    fun sendMessageToSelectedThread() {
        val composer = _messageComposerUiState.value
        val thread = threads.value.firstOrNull { it.id == composer.selectedThreadId } ?: return
        val contact = contacts.value.firstOrNull { it.id == thread.aiId } ?: return
        if (composer.prompt.isBlank()) return

        viewModelScope.launch {
            _messageComposerUiState.value = composer.copy(sending = true, statusMessage = "Sending...")
            val result = modelGateway.send(
                config = ProviderApiConfig(
                    providerId = contact.provider.id,
                    baseUrl = contact.provider.baseUrl.orEmpty(),
                    defaultModel = contact.provider.model,
                ),
                request = ModelRequest(
                    prompt = composer.prompt,
                    model = contact.provider.model,
                ),
            )
            val response = result.getOrNull()
            val updatedThread = thread.copy(
                lastMessagePreview = response?.content ?: "Request failed",
                updatedAt = Instant.now().toString(),
                pinned = true,
            )
            repository.upsertThread(updatedThread)
            _messageComposerUiState.value = composer.copy(
                sending = false,
                prompt = "",
                responsePreview = response?.content ?: "",
                statusMessage = if (result.isSuccess) "Message sent" else "Send failed",
            )
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
