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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.freecode.mobile.ui.state.AppViewModel

@Composable
fun MessagesScreen(
    viewModel: AppViewModel,
    onOpenThread: (String) -> Unit,
) {
    val threads by viewModel.threads.collectAsState()
    val contacts by viewModel.contacts.collectAsState()

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
                Text("每个聊天框对应一个 AI，点开后直接进入聊天。", color = Color.Gray)
            }
        }

        if (threads.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("还没有 AI 会话，请先去通讯录创建。")
            }
            return
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(threads) { thread ->
                val contact = contacts.firstOrNull { it.id == thread.aiId }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    onClick = { onOpenThread(thread.id) },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF07C160)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text((contact?.avatarLabel ?: "AI").take(2), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(contact?.name ?: thread.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
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
        }
    }
}
