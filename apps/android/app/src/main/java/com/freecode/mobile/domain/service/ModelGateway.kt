package com.freecode.mobile.domain.service

import com.freecode.mobile.domain.model.ProviderApiConfig

data class ModelMessage(
    val role: String,
    val content: String,
)

data class ModelRequest(
    val prompt: String,
    val model: String,
    val systemPrompt: String = "",
    val messages: List<ModelMessage> = emptyList(),
)

data class ModelResponse(
    val content: String,
    val providerLabel: String,
)

interface ModelGateway {
    suspend fun send(
        config: ProviderApiConfig,
        request: ModelRequest,
    ): Result<ModelResponse>
}
