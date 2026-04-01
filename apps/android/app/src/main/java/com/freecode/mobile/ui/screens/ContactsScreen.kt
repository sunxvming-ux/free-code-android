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
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.freecode.mobile.data.FakeAppRepository

@Composable
fun ContactsScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("???", style = MaterialTheme.typography.headlineMedium)
                Text("?? AI????????????????")
            }
        }
        items(FakeAppRepository.contacts) { contact ->
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
                        Text("??: ${contact.provider.model}")
                        Text("??: ${contact.permissions.level}")
                        Text("??: ${contact.workspace.rootPath}")
                    }
                }
            }
        }
    }
}
