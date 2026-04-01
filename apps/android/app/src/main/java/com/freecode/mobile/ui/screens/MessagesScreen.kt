package com.freecode.mobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
    val composer by viewModel.messageComposerUiState.collectAsState()
    val messagesByThread by viewModel.conversationMessages.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Messages", style = MaterialTheme.typography.headlineMedium)
                Text("Each AI contact owns its own thread list and assigned workspace.")
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
                    Text("Composer", style = MaterialTheme.typography.titleMedium)
                    Text("Selected thread: ${composer.selectedThreadId.ifBlank { "none" }}")
                    OutlinedTextField(
                        value = composer.prompt,
                        onValueChange = viewModel::updateComposerPrompt,
                        label = { Text("Message") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text("Use HTTP gateway")
                    Switch(
                        checked = composer.useHttpGateway,
                        onCheckedChange = viewModel::updateComposerGatewayMode,
                    )
                    Button(
                        onClick = { viewModel.sendMessageToSelectedThread() },
                        enabled = !composer.sending,
                    ) {
                        Text(if (composer.sending) "Sending..." else "Send to selected thread")
                    }
                    if (composer.statusMessage.isNotBlank()) {
                        Text(composer.statusMessage)
                    }
                    if (composer.responsePreview.isNotBlank()) {
                        Text("Latest response")
                        Text(composer.responsePreview)
                    }
                }
            }
        }
        val selectedMessages = messagesByThread[composer.selectedThreadId].orEmpty()
        if (selectedMessages.isNotEmpty()) {
            item {
                Text("Conversation", style = MaterialTheme.typography.titleMedium)
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
                        Text("Select thread")
                    }
                }
            }
        }
    }
}
