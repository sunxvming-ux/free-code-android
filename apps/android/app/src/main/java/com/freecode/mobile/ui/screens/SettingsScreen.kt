package com.freecode.mobile.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.freecode.mobile.ui.state.AppViewModel

@Composable
fun SettingsScreen(viewModel: AppViewModel) {
    val providers by viewModel.providers.collectAsState()
    val shellUiState by viewModel.shellUiState.collectAsState()
    val providerConfig by viewModel.providerConfigUiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F4F4)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("设置", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("管理模型接口、Root 执行和全局运行参数。", color = Color.Gray)
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
                    Text("系统执行测试", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("使用 Root")
                        Switch(checked = shellUiState.useRoot, onCheckedChange = viewModel::updateShellRoot)
                    }
                    OutlinedTextField(
                        value = shellUiState.command,
                        onValueChange = viewModel::updateShellCommand,
                        label = { Text("Shell 命令") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { viewModel.runShellCommand() }, enabled = !shellUiState.running) {
                            Text(if (shellUiState.running) "执行中…" else "执行命令")
                        }
                        Button(onClick = { viewModel.runProviderHealthcheck() }, enabled = !shellUiState.running) {
                            Text("接口自检")
                        }
                    }
                    shellUiState.exitCode?.let { Text("退出码：$it") }
                    if (shellUiState.stdout.isNotBlank()) {
                        Text("输出", fontWeight = FontWeight.SemiBold)
                        Text(shellUiState.stdout)
                    }
                    if (shellUiState.stderr.isNotBlank()) {
                        Text("错误", fontWeight = FontWeight.SemiBold, color = Color(0xFFC62828))
                        Text(shellUiState.stderr, color = Color(0xFFC62828))
                    }
                }
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
                    Text("当前 Provider 配置", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    if (providerConfig.providerId.isNotBlank()) {
                        Text("Provider ID：${providerConfig.providerId}", color = Color.Gray)
                    }
                    OutlinedTextField(
                        value = providerConfig.baseUrl,
                        onValueChange = viewModel::updateProviderBaseUrl,
                        label = { Text("Base URL") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = providerConfig.apiKey,
                        onValueChange = viewModel::updateProviderApiKey,
                        label = { Text("API Key") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = providerConfig.defaultModel,
                        onValueChange = viewModel::updateProviderDefaultModel,
                        label = { Text("默认模型") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(onClick = { viewModel.saveProviderConfig() }) {
                        Text("保存 Provider 配置")
                    }
                }
            }
        }
        item {
            Text("Provider 列表", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        items(providers) { provider ->
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(provider.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(provider.summary, color = Color.Gray)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Switch(checked = provider.enabled, onCheckedChange = { viewModel.toggleProvider(provider.id) })
                        Text(if (provider.enabled) "已启用" else "已停用")
                    }
                    Button(onClick = { viewModel.selectProviderConfig(provider.id, provider.title) }) {
                        Text("编辑该 Provider")
                    }
                }
            }
        }
    }
}
