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
fun SettingsScreen(viewModel: AppViewModel) {
    val providers by viewModel.providers.collectAsState()
    val shellUiState by viewModel.shellUiState.collectAsState()
    val providerConfig by viewModel.providerConfigUiState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("设置", style = MaterialTheme.typography.headlineMedium)
                Text("这里管理模型配置、Root 执行和健康检查，支持中文输入。")
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
                    Text("系统执行", style = MaterialTheme.typography.titleMedium)
                    Text("这里可以测试 shell / root 执行，并验证权限策略。")
                    Switch(
                        checked = shellUiState.useRoot,
                        onCheckedChange = viewModel::updateShellRoot,
                    )
                    OutlinedTextField(
                        value = shellUiState.command,
                        onValueChange = viewModel::updateShellCommand,
                        label = { Text("Shell 命令") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(
                        onClick = { viewModel.runShellCommand() },
                        enabled = !shellUiState.running,
                    ) {
                        Text(if (shellUiState.running) "执行中..." else "执行命令")
                    }
                    Button(
                        onClick = { viewModel.runProviderHealthcheck() },
                        enabled = !shellUiState.running,
                    ) {
                        Text("执行 Provider 健康检查")
                    }
                    shellUiState.exitCode?.let {
                        Text("退出码：$it")
                    }
                    if (shellUiState.stdout.isNotBlank()) {
                        Text("stdout")
                        Text(shellUiState.stdout)
                    }
                    if (shellUiState.stderr.isNotBlank()) {
                        Text("stderr")
                        Text(shellUiState.stderr)
                    }
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
                    Text("Provider 请求配置", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = providerConfig.baseUrl,
                        onValueChange = viewModel::updateProviderBaseUrl,
                        label = { Text("Base URL") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = providerConfig.apiKey,
                        onValueChange = viewModel::updateProviderApiKey,
                        label = { Text("API key") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = providerConfig.defaultModel,
                        onValueChange = viewModel::updateProviderDefaultModel,
                        label = { Text("Default model") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(onClick = { viewModel.saveProviderConfig() }) {
                        Text("保存 Provider 配置")
                    }
                }
            }
        }
        items(providers) { provider ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(provider.title, style = MaterialTheme.typography.titleMedium)
                    Text(provider.summary)
                    Switch(
                        checked = provider.enabled,
                        onCheckedChange = { viewModel.toggleProvider(provider.id) },
                    )
                    Button(onClick = { viewModel.selectProviderConfig(provider.id, provider.title) }) {
                        Text("编辑请求配置")
                    }
                }
            }
        }
    }
}
