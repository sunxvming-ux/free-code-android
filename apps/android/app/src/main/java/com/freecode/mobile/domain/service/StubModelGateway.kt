package com.freecode.mobile.domain.service

import com.freecode.mobile.domain.model.ProviderApiConfig

class StubModelGateway : ModelGateway {
    override suspend fun send(
        config: ProviderApiConfig,
        request: ModelRequest,
    ): Result<ModelResponse> = Result.success(
        ModelResponse(
            content = "Stub response from ${config.providerId.ifBlank { "provider" }} for model ${request.model}: ${request.prompt.take(120)}",
            providerLabel = config.providerId.ifBlank { "stub" },
        ),
    )
}
