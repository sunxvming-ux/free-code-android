package com.freecode.mobile.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.freecode.mobile.domain.model.MessageRole

@Entity(tableName = "conversation_messages")
data class ConversationMessageEntity(
    @PrimaryKey val id: String,
    val threadId: String,
    val role: MessageRole,
    val content: String,
    val timestamp: String,
)
