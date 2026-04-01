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
import com.freecode.mobile.domain.model.ConversationMessage
import com.freecode.mobile.domain.model.MessageRole
import com.freecode.mobile.domain.model.PermissionLevel
import com.freecode.mobile.domain.model.ProviderConfig
import com.freecode.mobile.domain.model.ProviderApiConfig
import com.freecode.mobile.domain.model.ProviderSetting
import com.freecode.mobile.domain.model.WorkspaceBinding
import com.freecode.mobile.domain.model.permissionPreset
import com.freecode.mobile.domain.model.toDraft
import com.freecode.mobile.domain.service.HttpModelGateway
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
    private val httpModelGateway: HttpModelGateway = HttpModelGateway(),
) : ViewModel() {
    val contacts: StateFlow<List<AiContact>> = repository.observeContacts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val threads: StateFlow<List<ConversationThread>> = repository.observeThreads()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val providers: StateFlow<List<ProviderSetting>> = repository.observeProviders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val persistedMessages: StateFlow<List<ConversationMessage>> = repository.observeMessages()
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

    private val _conversationMessages =
        MutableStateFlow<Map<String, List<ConversationMessage>>>(emptyMap())
    val conversationMessages: StateFlow<Map<String, List<ConversationMessage>>> = _conversationMessages

    init {
        viewModelScope.launch {
            repository.bootstrapIfEmpty(
                contacts = FakeAppRepository.contacts,
                threads = FakeAppRepository.threads,
                providers = FakeAppRepository.providers,
            )
            persistedMessages.value.groupBy { it.threadId }.takeIf { it.isNotEmpty() }?.let {
                _conversationMessages.value = it
            }
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
            _conversationMessages.value = _conversationMessages.value
                .filterKeys { threadId -> threads.value.firstOrNull { it.id == threadId }?.aiId != contactId }
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
        val activeContact = contacts.value.firstOrNull { it.workspace.rootPath == snapshot.activeWorkspacePath }
        if (activeContact != null && !canWriteFiles(activeContact)) {
            _fileEditorUiState.value = snapshot.copy(statusMessage = "File creation blocked by permission profile")
            return
        }
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
        val activeContact = contacts.value.firstOrNull { it.workspace.rootPath == snapshot.activeWorkspacePath }
        if (activeContact != null && !canWriteFiles(activeContact)) {
            _fileEditorUiState.value = snapshot.copy(statusMessage = "Save blocked by permission profile")
            return
        }
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
        val selectedThread = threads.value.firstOrNull { it.id == messageComposerUiState.value.selectedThreadId }
        val selectedContact = selectedThread?.let { thread -> contacts.value.firstOrNull { it.id == thread.aiId } }
        if (selectedContact != null && !canExecuteShell(selectedContact, snapshot.useRoot)) {
            _shellUiState.value = snapshot.copy(stderr = "Shell execution blocked by permission profile")
            return
        }
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
            val saved = repository.getProviderConfig(provider.id)
            _providerConfigUiState.value = _providerConfigUiState.value.copy(
                providerId = provider.id,
                baseUrl = saved?.baseUrl ?: _providerConfigUiState.value.baseUrl,
                apiKey = saved?.apiKey ?: _providerConfigUiState.value.apiKey,
                defaultModel = saved?.defaultModel ?: provider.title,
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

    fun selectProviderConfig(providerId: String, fallbackModel: String) {
        viewModelScope.launch {
            val saved = repository.getProviderConfig(providerId)
            _providerConfigUiState.value = ProviderConfigUiState(
                providerId = providerId,
                baseUrl = saved?.baseUrl.orEmpty(),
                apiKey = saved?.apiKey.orEmpty(),
                defaultModel = saved?.defaultModel ?: fallbackModel,
            )
        }
    }

    fun saveProviderConfig() {
        val snapshot = _providerConfigUiState.value
        if (snapshot.providerId.isBlank()) return
        viewModelScope.launch {
            repository.saveProviderConfig(
                ProviderApiConfig(
                    providerId = snapshot.providerId,
                    baseUrl = snapshot.baseUrl,
                    apiKey = snapshot.apiKey,
                    defaultModel = snapshot.defaultModel,
                ),
            )
            _shellUiState.value = _shellUiState.value.copy(stdout = "Saved provider config for ${snapshot.providerId}")
        }
    }

    fun updateComposerGatewayMode(useHttp: Boolean) {
        _messageComposerUiState.value = _messageComposerUiState.value.copy(useHttpGateway = useHttp)
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
            val providerRequest = ProviderApiConfig(
                providerId = contact.provider.id,
                baseUrl = providerConfigUiState.value.baseUrl.ifBlank { contact.provider.baseUrl.orEmpty() },
                apiKey = providerConfigUiState.value.apiKey,
                defaultModel = providerConfigUiState.value.defaultModel.ifBlank { contact.provider.model },
            )
            val gatewayResult = if (composer.useHttpGateway) {
                httpModelGateway.send(
                    config = providerRequest,
                    request = ModelRequest(
                        prompt = composer.prompt,
                        model = providerRequest.defaultModel.ifBlank { contact.provider.model },
                    ),
                )
            } else {
                modelGateway.send(
                    config = providerRequest,
                    request = ModelRequest(
                        prompt = composer.prompt,
                        model = providerRequest.defaultModel.ifBlank { contact.provider.model },
                    ),
                )
            }
            val response = gatewayResult.getOrNull()
            appendMessage(
                threadId = thread.id,
                role = MessageRole.USER,
                content = composer.prompt,
            )
            response?.content?.let {
                appendMessage(
                    threadId = thread.id,
                    role = MessageRole.ASSISTANT,
                    content = it,
                )
            }
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
                statusMessage = if (gatewayResult.isSuccess) "Message sent" else "Send failed",
            )
        }
    }

    private fun appendMessage(
        threadId: String,
        role: MessageRole,
        content: String,
    ) {
        val current = _conversationMessages.value[threadId].orEmpty()
        val message = ConversationMessage(
            id = "${threadId}-${current.size + 1}",
            threadId = threadId,
            role = role,
            content = content,
            timestamp = Instant.now().toString(),
        )
        val next = current + message
        _conversationMessages.value = _conversationMessages.value + (threadId to next)
        viewModelScope.launch {
            repository.upsertMessage(message)
        }
    }

    private fun canWriteFiles(contact: AiContact): Boolean =
        contact.permissions.toolPolicy.allowFilesystemWrite

    private fun canExecuteShell(contact: AiContact, useRoot: Boolean): Boolean {
        if (!contact.permissions.toolPolicy.allowShell) return false
        if (useRoot && !contact.permissions.toolPolicy.allowRootExecution) return false
        return true
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
