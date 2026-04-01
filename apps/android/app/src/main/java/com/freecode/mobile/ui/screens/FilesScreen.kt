package com.freecode.mobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F4F4)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("文件夹", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("像轻量版 VS Code 一样浏览 AI 工作目录，可创建、查看和保存文本文件。", color = Color.Gray)
            }
        }
        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("文件操作", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = editorState.newFileName,
                        onValueChange = viewModel::updateNewFileName,
                        label = { Text("新文件名") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { viewModel.createFileInActiveWorkspace() }) {
                            Text("创建文件")
                        }
                        if (editorState.selectedFilePath.isNotBlank()) {
                            Button(onClick = { viewModel.saveSelectedFile() }) {
                                Text(if (editorState.dirty) "保存修改" else "重新保存")
                            }
                        }
                    }
                    if (editorState.statusMessage.isNotBlank()) {
                        Text(editorState.statusMessage, color = Color.Gray)
                    }
                }
            }
        }
        items(workspaces) { workspace ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.loadWorkspacePreview(workspace.rootPath) },
                shape = RoundedCornerShape(18.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(workspace.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("根目录：${workspace.rootPath}")
                    Text("可写目录数量：${workspace.writableRoots.size}", color = Color.Gray)
                }
            }
        }
        if (preview.isNotEmpty()) {
            item {
                Text("当前工作区", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            items(preview.take(60)) { node ->
                Text(
                    text = "${"  ".repeat(node.depth)}${if (node.type == FileNodeType.FILE) "📄" else "📁"} ${node.name}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { if (node.type == FileNodeType.FILE) viewModel.readFile(node.path) }
                        .padding(vertical = 4.dp),
                )
            }
        }
        if (editorState.selectedFilePath.isNotBlank()) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("正在编辑", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(editorState.selectedFilePath, color = Color.Gray)
                        OutlinedTextField(
                            value = editorState.selectedFileContent,
                            onValueChange = viewModel::updateSelectedFileContent,
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 10,
                            label = { Text("文件内容") },
                        )
                    }
                }
            }
        }
    }
}
