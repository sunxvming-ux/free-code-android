package com.freecode.mobile.domain.service

import com.freecode.mobile.domain.model.ProviderApiConfig

class StubModelGateway : ModelGateway {
    override suspend fun send(
        config: ProviderApiConfig,
        request: ModelRequest,
    ): Result<ModelResponse> = Result.success(
        ModelResponse(
            content = buildString {
                append("来自 ")
                append(config.providerId.ifBlank { "provider" })
                append(" 的模拟回复，模型：")
                append(request.model)
                append("。")
                if (request.systemPrompt.isNotBlank()) {
                    append("系统提示词已加载。")
                }
                if (request.messages.isNotEmpty()) {
                    append("历史消息条数：")
                    append(request.messages.size)
                    append("。")
                }
                append("当前输入：")
                append(request.prompt.take(160))
            },
            providerLabel = config.providerId.ifBlank { "stub" },
        ),
    )
}
