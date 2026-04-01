package com.freecode.mobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.freecode.mobile.ui.state.AppViewModel

@Composable
fun MessagesScreen(viewModel: AppViewModel) {
    val threads by viewModel.threads.collectAsState()
    val contacts by viewModel.contacts.collectAsState()
    val composer by viewModel.messageComposerUiState.collectAsState()
    val messagesByThread by viewModel.conversationMessages.collectAsState()
    val selectedThread = threads.firstOrNull { it.id == composer.selectedThreadId }
    val selectedContact = selectedThread?.let { thread -> contacts.firstOrNull { it.id == thread.aiId } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("消息", style = MaterialTheme.typography.headlineMedium)
                Text("每个 AI 联系人都有独立线程、模型配置和工作目录。")
                if (selectedContact != null) {
                    Text("当前 AI：${selectedContact.name} · 模型：${selectedContact.provider.model}")
                }
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("消息输入", style = MaterialTheme.typography.titleMedium)
                    Text("当前线程：${composer.selectedThreadId.ifBlank { "未选择" }}")
                    if (selectedContact != null) {
                        Text(
                            "权限：${selectedContact.permissions.level} / 网络：${
                                if (selectedContact.permissions.toolPolicy.allowNetwork) "允许" else "禁用"
                            }",
                        )
                    }
                    OutlinedTextField(
                        value = composer.prompt,
                        onValueChange = viewModel::updateComposerPrompt,
                        label = { Text("输入消息（支持中文）") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("HTTP 网关")
                        Switch(
                            checked = composer.useHttpGateway,
                            onCheckedChange = viewModel::updateComposerGatewayMode,
                        )
                    }
                    Button(
                        onClick = { viewModel.sendMessageToSelectedThread() },
                        enabled = !composer.sending,
                    ) {
                        Text(if (composer.sending) "发送中..." else "发送到当前线程")
                    }
                    if (composer.statusMessage.isNotBlank()) {
                        Text(composer.statusMessage)
                    }
                    if (composer.responsePreview.isNotBlank()) {
                        Text("最新回复")
                        Text(composer.responsePreview)
                    }
                }
            }
        }
        val selectedMessages = messagesByThread[composer.selectedThreadId].orEmpty()
        if (selectedMessages.isNotEmpty()) {
            item {
                Text("当前会话", style = MaterialTheme.typography.titleMedium)
            }
            items(selectedMessages) { message ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(message.role.name, style = MaterialTheme.typography.labelMedium)
                        Text(message.content)
                        Text(message.timestamp, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
        items(threads) { thread ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(thread.title, style = MaterialTheme.typography.titleMedium)
                    Text(thread.lastMessagePreview, style = MaterialTheme.typography.bodyMedium)
                    Text(thread.updatedAt, style = MaterialTheme.typography.labelMedium)
                    Button(onClick = { viewModel.selectThread(thread.id) }) {
                        Text("切换到此线程")
                    }
                }
            }
        }
    }
}
