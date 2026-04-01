package com.freecode.mobile.domain.model

data class ConversationMessage(
    val id: String,
    val threadId: String,
    val role: MessageRole,
    val content: String,
    val timestamp: String,
)

enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM,
}
