package com.freecode.mobile.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.freecode.mobile.ui.state.AppViewModel

@Composable
fun FilesScreen(viewModel: AppViewModel) {
    val contacts by viewModel.contacts.collectAsState()
    val preview by viewModel.workspacePreview.collectAsState()
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
                Text("Phase 1 exposes workspace roots and a lightweight file tree preview.")
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
            items(preview.take(20)) { node ->
                Text(
                    text = "${"  ".repeat(node.depth)}- ${node.name}",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
