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
                Text("文件夹", style = MaterialTheme.typography.headlineMedium)
                Text("创建文件、查看工作区树和预览内容，支持中文文件。")
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
                    Text("文件操作", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = editorState.newFileName,
                        onValueChange = viewModel::updateNewFileName,
                        label = { Text("新文件名") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(onClick = { viewModel.createFileInActiveWorkspace() }) {
                        Text("在当前工作区创建文件")
                    }
                    if (editorState.selectedFilePath.isNotBlank()) {
                        Button(onClick = { viewModel.saveSelectedFile() }) {
                            Text(if (editorState.dirty) "保存当前文件" else "重新保存文件")
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
                    Text("根目录：${workspace.rootPath}")
                    Text("可写目录数：${workspace.writableRoots.size}")
                }
            }
        }
        if (preview.isNotEmpty()) {
            item {
                Text("工作区预览", style = MaterialTheme.typography.titleMedium)
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
                        Text("当前文件", style = MaterialTheme.typography.titleMedium)
                        Text(editorState.selectedFilePath)
                        OutlinedTextField(
                            value = editorState.selectedFileContent,
                            onValueChange = viewModel::updateSelectedFileContent,
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 8,
                            label = { Text("文件内容") },
                        )
                    }
                }
            }
        }
    }
}
