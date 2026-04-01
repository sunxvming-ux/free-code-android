package com.freecode.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.freecode.mobile.domain.model.ContactDraft
import com.freecode.mobile.domain.model.PermissionLevel
import com.freecode.mobile.domain.model.ProviderKind
import com.freecode.mobile.ui.state.AppViewModel

@Composable
fun ContactsScreen(viewModel: AppViewModel) {
    var showCreateDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Contacts", style = MaterialTheme.typography.headlineMedium)
                Text("Create AI contacts with provider, workspace, and permission presets.")
                Button(onClick = { showCreateDialog = true }) {
                    Text("Create AI")
                }
            }
        }
        items(viewModel.contacts) { contact ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF6750A4)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(contact.avatarLabel, color = Color.White)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(contact.name, style = MaterialTheme.typography.titleMedium)
                        Text(contact.description, style = MaterialTheme.typography.bodyMedium)
                        Text("Model: ${contact.provider.model}")
                        Text("Permission: ${contact.permissions.level}")
                        Text("Workspace: ${contact.workspace.rootPath}")
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateContactDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = {
                viewModel.createContact(it)
                showCreateDialog = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateContactDialog(
    onDismiss: () -> Unit,
    onCreate: (ContactDraft) -> Unit,
) {
    var draft by remember { mutableStateOf(ContactDraft()) }
    var providerMenuExpanded by remember { mutableStateOf(false) }
    var permissionMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onCreate(draft) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Create AI Contact") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = draft.name,
                    onValueChange = { draft = draft.copy(name = it) },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = draft.description,
                    onValueChange = { draft = draft.copy(description = it) },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = draft.systemPrompt,
                    onValueChange = { draft = draft.copy(systemPrompt = it) },
                    label = { Text("System prompt") },
                    modifier = Modifier.fillMaxWidth(),
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
                        label = { Text("Provider") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = providerMenuExpanded) },
                    )
                    ExposedDropdownMenu(
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
                    label = { Text("Model") },
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
                        label = { Text("Permission level") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = permissionMenuExpanded) },
                    )
                    ExposedDropdownMenu(
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
                OutlinedTextField(
                    value = draft.workspaceName,
                    onValueChange = { draft = draft.copy(workspaceName = it) },
                    label = { Text("Workspace name") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = draft.workspacePath,
                    onValueChange = { draft = draft.copy(workspacePath = it) },
                    label = { Text("Workspace path") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
    )
}
