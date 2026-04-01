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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.freecode.mobile.domain.model.ContactDraft
import com.freecode.mobile.domain.model.PermissionLevel
import com.freecode.mobile.domain.model.ProviderKind
import com.freecode.mobile.ui.state.AppViewModel

@Composable
fun ContactsScreen(viewModel: AppViewModel) {
    val contacts by viewModel.contacts.collectAsState()
    val editingContactId by viewModel.editingContactId.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F4F4)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("通讯录", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("每个联系人就是一个独立 AI，可单独配置模型、权限和工作目录。", color = Color.Gray)
                OutlinedButton(onClick = { showCreateDialog = true }) {
                    Text("新建 AI")
                }
            }
        }

        items(contacts) { contact ->
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF07C160)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(contact.avatarLabel.take(2), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(contact.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(contact.description, color = Color.Gray)
                        Text("模型：${contact.provider.kind.name} / ${contact.provider.model}")
                        Text("权限：${contact.permissions.level} · 目录：${contact.workspace.name}")
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(onClick = { viewModel.startEditingContact(contact.id) }) {
                        Text("编辑")
                    }
                    OutlinedButton(onClick = { viewModel.loadWorkspacePreview(contact.workspace.rootPath) }) {
                        Text("查看文件")
                    }
                    OutlinedButton(onClick = { viewModel.deleteContact(contact.id) }) {
                        Text("删除")
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        ContactDialog(
            title = "新建 AI 联系人",
            initialDraft = ContactDraft(),
            onDismiss = { showCreateDialog = false },
            onSave = {
                viewModel.createContact(it)
                showCreateDialog = false
            },
        )
    }

    if (editingContactId != null) {
        ContactDialog(
            title = "编辑 AI 联系人",
            initialDraft = viewModel.getEditingDraft() ?: ContactDraft(),
            onDismiss = { viewModel.stopEditingContact() },
            onSave = { draft -> editingContactId?.let { viewModel.updateContact(it, draft) } },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContactDialog(
    title: String,
    initialDraft: ContactDraft,
    onDismiss: () -> Unit,
    onSave: (ContactDraft) -> Unit,
) {
    var draft by remember(initialDraft) { mutableStateOf(initialDraft) }
    var providerMenuExpanded by remember { mutableStateOf(false) }
    var permissionMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onSave(draft) }) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = draft.name,
                    onValueChange = { draft = draft.copy(name = it) },
                    label = { Text("名称") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = draft.description,
                    onValueChange = { draft = draft.copy(description = it) },
                    label = { Text("描述") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = draft.systemPrompt,
                    onValueChange = { draft = draft.copy(systemPrompt = it) },
                    label = { Text("系统提示词") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                )
                ExposedDropdownMenuBox(
                    expanded = providerMenuExpanded,
                    onExpandedChange = { providerMenuExpanded = !providerMenuExpanded },
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        readOnly = true,
                        value = draft.providerKind.name,
                        onValueChange = {},
                        label = { Text("模型提供商") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = providerMenuExpanded) },
                    )
                    DropdownMenu(
                        expanded = providerMenuExpanded,
                        onDismissRequest = { providerMenuExpanded = false },
                    ) {
                        ProviderKind.entries.forEach { provider ->
                            DropdownMenuItem(
                                text = { Text(provider.name) },
                                onClick = {
                                    draft = draft.copy(providerKind = provider)
                                    providerMenuExpanded = false
                                },
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = draft.model,
                    onValueChange = { draft = draft.copy(model = it) },
                    label = { Text("模型名称") },
                    modifier = Modifier.fillMaxWidth(),
                )
                ExposedDropdownMenuBox(
                    expanded = permissionMenuExpanded,
                    onExpandedChange = { permissionMenuExpanded = !permissionMenuExpanded },
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        readOnly = true,
                        value = draft.permissionLevel.name,
                        onValueChange = {},
                        label = { Text("权限级别") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = permissionMenuExpanded) },
                    )
                    DropdownMenu(
                        expanded = permissionMenuExpanded,
                        onDismissRequest = { permissionMenuExpanded = false },
                    ) {
                        PermissionLevel.entries.forEach { level ->
                            DropdownMenuItem(
                                text = { Text(level.name) },
                                onClick = {
                                    draft = draft.copy(permissionLevel = level)
                                    permissionMenuExpanded = false
                                },
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                OutlinedTextField(
                    value = draft.workspaceName,
                    onValueChange = { draft = draft.copy(workspaceName = it) },
                    label = { Text("工作区名称") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = draft.workspacePath,
                    onValueChange = { draft = draft.copy(workspacePath = it) },
                    label = { Text("工作区路径") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
    )
}
