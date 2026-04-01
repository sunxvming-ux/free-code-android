package com.freecode.mobile.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.freecode.mobile.domain.files.FileNodeType
import com.freecode.mobile.ui.state.AppViewModel

@Composable
fun FilesScreen(viewModel: AppViewModel) {
    val contacts by viewModel.contacts.collectAsState()
    val preview by viewModel.workspacePreview.collectAsState()
    val editorState by viewModel.fileEditorUiState.collectAsState()
    val workspaces = contacts.map { it.workspace }
    val primaryWorkspace = contacts.firstOrNull()?.workspace?.rootPath

    LaunchedEffect(primaryWorkspace) {
        primaryWorkspace?.let(viewModel::loadWorkspacePreview)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Files", style = MaterialTheme.typography.headlineMedium)
                Text("Create files, inspect workspace trees, and preview file content.")
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
                    Text("File actions", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = editorState.newFileName,
                        onValueChange = viewModel::updateNewFileName,
                        label = { Text("New file name") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(onClick = { viewModel.createFileInActiveWorkspace() }) {
                        Text("Create file in active workspace")
                    }
                    if (editorState.selectedFilePath.isNotBlank()) {
                        Button(onClick = { viewModel.saveSelectedFile() }) {
                            Text(if (editorState.dirty) "Save selected file" else "Save current file")
                        }
                    }
                    if (editorState.statusMessage.isNotBlank()) {
                        Text(editorState.statusMessage)
                    }
                }
            }
        }
        items(workspaces) { workspace ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(workspace.name, style = MaterialTheme.typography.titleMedium)
                    Text("Root: ${workspace.rootPath}")
                    Text("Writable roots: ${workspace.writableRoots.size}")
                }
            }
        }
        if (preview.isNotEmpty()) {
            item {
                Text("Workspace preview", style = MaterialTheme.typography.titleMedium)
            }
            items(preview.take(40)) { node ->
                Text(
                    text = "${"  ".repeat(node.depth)}- ${node.name}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { if (node.type == FileNodeType.FILE) viewModel.readFile(node.path) }
                        .padding(vertical = 2.dp),
                )
            }
        }
        if (editorState.selectedFilePath.isNotBlank()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("Selected file", style = MaterialTheme.typography.titleMedium)
                        Text(editorState.selectedFilePath)
                        OutlinedTextField(
                            value = editorState.selectedFileContent,
                            onValueChange = viewModel::updateSelectedFileContent,
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 8,
                            label = { Text("Content preview") },
                        )
                    }
                }
            }
        }
    }
}
