package com.freecode.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.freecode.mobile.domain.model.MessageRole
import com.freecode.mobile.ui.state.AppViewModel

@Composable
fun MessagesScreen(viewModel: AppViewModel) {
    val threads by viewModel.threads.collectAsState()
    val contacts by viewModel.contacts.collectAsState()
    val composer by viewModel.messageComposerUiState.collectAsState()
    val messagesByThread by viewModel.conversationMessages.collectAsState()
    val selectedThread = threads.firstOrNull { it.id == composer.selectedThreadId }
    val selectedContact = selectedThread?.let { thread -> contacts.firstOrNull { it.id == thread.aiId } }
    val selectedMessages = messagesByThread[composer.selectedThreadId].orEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F4F4)),
    ) {
        Surface(shadowElevation = 2.dp, tonalElevation = 1.dp) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Text("消息", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    text = selectedContact?.let { "当前 AI：${it.name} · ${it.provider.model}" } ?: "选择一个 AI 会话开始聊天",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                )
            }
        }

        if (threads.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("还没有会话，请先到通讯录创建 AI。")
            }
            return
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Text("会话列表", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            items(threads) { thread ->
                val contact = contacts.firstOrNull { it.id == thread.aiId }
                val selected = thread.id == composer.selectedThreadId
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    onClick = { viewModel.selectThread(thread.id) },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AvatarBubble(label = contact?.avatarLabel ?: "AI", color = if (selected) Color(0xFF07C160) else Color(0xFF5B8FF9))
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = contact?.name ?: thread.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                if (thread.pinned) {
                                    Text("置顶", color = Color(0xFF07C160), style = MaterialTheme.typography.labelMedium)
                                }
                            }
                            Text(
                                text = thread.lastMessagePreview,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Text(thread.updatedAt.take(16), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text("当前聊天", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }

            if (selectedMessages.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
                        Text(
                            "这里会显示和 AI 的对话记录，支持中文输入和 OpenAI / Anthropic 接口。",
                            modifier = Modifier.padding(16.dp),
                            color = Color.Gray,
                        )
                    }
                }
            } else {
                items(selectedMessages) { message ->
                    val isUser = message.role == MessageRole.USER
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
                        verticalAlignment = Alignment.Top,
                    ) {
                        if (!isUser) {
                            AvatarBubble(label = selectedContact?.avatarLabel ?: "AI", color = Color(0xFF07C160))
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.fillMaxWidth(0.82f),
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = if (isUser) "我" else selectedContact?.name ?: "AI",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.Gray,
                                )
                                Text(message.content, style = MaterialTheme.typography.bodyLarge)
                                Text(message.timestamp.take(16), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                        }
                        if (isUser) {
                            Spacer(modifier = Modifier.width(8.dp))
                            AvatarBubble(label = "我", color = Color(0xFF5B8FF9))
                        }
                    }
                }
            }
        }

        Divider()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            selectedContact?.let { contact ->
                Text(
                    text = "权限：${contact.permissions.level} · 网络：${if (contact.permissions.toolPolicy.allowNetwork) "已开启" else "已禁用"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray,
                )
            }
            OutlinedTextField(
                value = composer.prompt,
                onValueChange = viewModel::updateComposerPrompt,
                label = { Text("输入消息（支持中文）") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("HTTP 接口", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = composer.useHttpGateway,
                        onCheckedChange = viewModel::updateComposerGatewayMode,
                    )
                }
                Button(onClick = { viewModel.sendMessageToSelectedThread() }, enabled = !composer.sending) {
                    Text(if (composer.sending) "发送中…" else "发送")
                }
            }
            if (composer.statusMessage.isNotBlank()) {
                Text(composer.statusMessage, color = if (composer.statusMessage.contains("成功")) Color(0xFF07C160) else Color.Gray)
            }
        }
    }
}

@Composable
private fun AvatarBubble(label: String, color: Color) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center,
    ) {
        Text(label.take(2), color = Color.White, fontWeight = FontWeight.Bold)
    }
}
