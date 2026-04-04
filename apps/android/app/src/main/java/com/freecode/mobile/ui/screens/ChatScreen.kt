package com.freecode.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.freecode.mobile.domain.model.MessageRole
import com.freecode.mobile.domain.model.ProviderAuthMode
import com.freecode.mobile.ui.state.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: AppViewModel,
    threadId: String,
    onBack: () -> Unit,
) {
    val threads by viewModel.threads.collectAsState()
    val contacts by viewModel.contacts.collectAsState()
    val composer by viewModel.messageComposerUiState.collectAsState()
    val messagesByThread by viewModel.conversationMessages.collectAsState()
    val providerConfig by viewModel.providerConfigUiState.collectAsState()
    val thread = threads.firstOrNull { it.id == threadId }
    val contact = thread?.let { current -> contacts.firstOrNull { it.id == current.aiId } }
    val messages = messagesByThread[threadId].orEmpty()
    var showMenu by remember { mutableStateOf(false) }
    var showProviderEditor by remember { mutableStateOf(false) }

    LaunchedEffect(threadId) {
        if (threadId.isNotBlank()) {
            viewModel.selectThread(threadId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(contact?.name ?: "聊天") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Outlined.MoreHoriz, contentDescription = "更多")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("编辑 Provider") },
                                onClick = {
                                    showMenu = false
                                    showProviderEditor = true
                                },
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                contact?.let { current ->
                    Text(
                        text = "${current.provider.kind.name} / ${providerConfig.defaultModel.ifBlank { current.provider.model }} / ${providerConfig.authMode.name}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray,
                    )
                    Text(
                        text = "真实接口模式 · 工具：/shell /root /read /write /ls",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                    )
                }
                OutlinedTextField(
                    value = composer.prompt,
                    onValueChange = viewModel::updateComposerPrompt,
                    label = { Text("输入消息") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Button(onClick = { viewModel.sendMessageToSelectedThread() }, enabled = !composer.sending) {
                        Text(if (composer.sending) "发送中…" else "发送")
                    }
                }
                if (composer.statusMessage.isNotBlank()) {
                    Text(composer.statusMessage, color = if (composer.statusMessage.contains("成功") || composer.statusMessage.contains("完成")) Color(0xFF07C160) else Color.Gray)
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFEDEDED))
                .padding(innerPadding),
        ) {
            if (showProviderEditor && contact != null) {
                ProviderEditorCard(
                    providerName = contact.provider.kind.name,
                    authMode = providerConfig.authMode,
                    baseUrl = providerConfig.baseUrl,
                    apiKey = providerConfig.apiKey,
                    defaultModel = providerConfig.defaultModel,
                    oauthAccessToken = providerConfig.oauthAccessToken,
                    oauthRefreshToken = providerConfig.oauthRefreshToken,
                    oauthClientId = providerConfig.oauthClientId,
                    onAuthModeChange = viewModel::updateProviderAuthMode,
                    onBaseUrlChange = viewModel::updateProviderBaseUrl,
                    onApiKeyChange = viewModel::updateProviderApiKey,
                    onModelChange = viewModel::updateProviderDefaultModel,
                    onOauthAccessTokenChange = viewModel::updateProviderOauthAccessToken,
                    onOauthRefreshTokenChange = viewModel::updateProviderOauthRefreshToken,
                    onOauthClientIdChange = viewModel::updateProviderOauthClientId,
                    onSave = {
                        viewModel.saveProviderConfig()
                        showProviderEditor = false
                    },
                    onClose = { showProviderEditor = false },
                )
            }

            if (messages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("开始和这个 AI 聊天吧。", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .fillMaxWidth(),
                    reverseLayout = false,
                ) {
                    items(messages) { message ->
                        val isUser = message.role == MessageRole.USER
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
                            verticalAlignment = Alignment.Top,
                        ) {
                            if (!isUser) {
                                ChatAvatar(contact?.avatarLabel ?: "AI", Color(0xFF07C160))
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth(0.78f),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .background(if (isUser) Color(0xFF95EC69) else Color.White)
                                        .padding(12.dp),
                                ) {
                                    Text(message.content, style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                            if (isUser) {
                                Spacer(modifier = Modifier.width(8.dp))
                                ChatAvatar("我", Color(0xFF5B8FF9))
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }
        }
    }
}

@Composable
private fun ChatAvatar(label: String, color: Color) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center,
    ) {
        Text(label.take(2), color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ProviderEditorCard(
    providerName: String,
    authMode: ProviderAuthMode,
    baseUrl: String,
    apiKey: String,
    defaultModel: String,
    oauthAccessToken: String,
    oauthRefreshToken: String,
    oauthClientId: String,
    onAuthModeChange: (ProviderAuthMode) -> Unit,
    onBaseUrlChange: (String) -> Unit,
    onApiKeyChange: (String) -> Unit,
    onModelChange: (String) -> Unit,
    onOauthAccessTokenChange: (String) -> Unit,
    onOauthRefreshTokenChange: (String) -> Unit,
    onOauthClientIdChange: (String) -> Unit,
    onSave: () -> Unit,
    onClose: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("$providerName Provider 配置", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onAuthModeChange(ProviderAuthMode.API_KEY) }) { Text("Key") }
                Button(onClick = { onAuthModeChange(ProviderAuthMode.OAUTH) }) { Text("OAuth") }
            }
            OutlinedTextField(value = baseUrl, onValueChange = onBaseUrlChange, label = { Text("Base URL") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = defaultModel, onValueChange = onModelChange, label = { Text("模型") }, modifier = Modifier.fillMaxWidth())
            if (authMode == ProviderAuthMode.API_KEY) {
                OutlinedTextField(value = apiKey, onValueChange = onApiKeyChange, label = { Text("API Key") }, modifier = Modifier.fillMaxWidth())
            } else {
                OutlinedTextField(value = oauthClientId, onValueChange = onOauthClientIdChange, label = { Text("OAuth Client ID") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = oauthAccessToken, onValueChange = onOauthAccessTokenChange, label = { Text("Access Token") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = oauthRefreshToken, onValueChange = onOauthRefreshTokenChange, label = { Text("Refresh Token") }, modifier = Modifier.fillMaxWidth())
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onSave) { Text("保存") }
                Button(onClick = onClose) { Text("关闭") }
            }
        }
    }
}
