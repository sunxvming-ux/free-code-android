package com.freecode.mobile.ui.state

data class MessageComposerUiState(
    val selectedThreadId: String = "",
    val prompt: String = "",
    val sending: Boolean = false,
    val responsePreview: String = "",
    val statusMessage: String = "",
    val useHttpGateway: Boolean = false,
)
