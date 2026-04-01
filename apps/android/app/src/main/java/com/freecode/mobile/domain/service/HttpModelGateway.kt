package com.freecode.mobile.domain.service

import com.freecode.mobile.domain.model.ProviderApiConfig
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HttpModelGateway : ModelGateway {
    override suspend fun send(
        config: ProviderApiConfig,
        request: ModelRequest,
    ): Result<ModelResponse> = withContext(Dispatchers.IO) {
        runCatching {
            if (config.baseUrl.isBlank()) {
                return@runCatching ModelResponse(
                    content = "HTTP gateway skipped: base URL is empty. Falling back to local preview.",
                    providerLabel = config.providerId.ifBlank { "http-preview" },
                )
            }

            val providerFlavor = detectProviderFlavor(config.providerId, config.baseUrl)
            val targetUrl = buildTargetUrl(providerFlavor, config.baseUrl)
            val connection = URL(targetUrl).openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.connectTimeout = 15_000
            connection.readTimeout = 30_000
            connection.setRequestProperty("Content-Type", "application/json")

            when (providerFlavor) {
                ProviderFlavor.ANTHROPIC -> {
                    if (config.apiKey.isNotBlank()) {
                        connection.setRequestProperty("x-api-key", config.apiKey)
                    }
                    connection.setRequestProperty("anthropic-version", "2023-06-01")
                }

                ProviderFlavor.OPENAI,
                ProviderFlavor.GENERIC -> {
                    if (config.apiKey.isNotBlank()) {
                        connection.setRequestProperty("Authorization", "Bearer ${config.apiKey}")
                    }
                }
            }

            val body = buildRequestBody(providerFlavor, request)
            OutputStreamWriter(connection.outputStream).use { it.write(body) }
            val stream = if (connection.responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            val payload = stream?.let { BufferedReader(it.reader()).use { reader -> reader.readText() } }.orEmpty()
            ModelResponse(
                content = payload.ifBlank { "Empty response from $targetUrl" },
                providerLabel = providerFlavor.name.lowercase(),
            )
        }
    }
}

private enum class ProviderFlavor {
    OPENAI,
    ANTHROPIC,
    GENERIC,
}

private fun detectProviderFlavor(providerId: String, baseUrl: String): ProviderFlavor {
    val source = "${providerId.lowercase()} ${baseUrl.lowercase()}"
    return when {
        "anthropic" in source -> ProviderFlavor.ANTHROPIC
        "openai" in source || "/chat/completions" in source -> ProviderFlavor.OPENAI
        else -> ProviderFlavor.GENERIC
    }
}

private fun buildTargetUrl(flavor: ProviderFlavor, baseUrl: String): String {
    val trimmed = baseUrl.trimEnd('/')
    return when (flavor) {
        ProviderFlavor.OPENAI ->
            if (trimmed.endsWith("/chat/completions")) trimmed else "$trimmed/chat/completions"

        ProviderFlavor.ANTHROPIC ->
            if (trimmed.endsWith("/v1/messages")) trimmed else "$trimmed/v1/messages"

        ProviderFlavor.GENERIC -> trimmed
    }
}

private fun buildRequestBody(flavor: ProviderFlavor, request: ModelRequest): String =
    when (flavor) {
        ProviderFlavor.OPENAI, ProviderFlavor.GENERIC -> """
            {
              "model": "${request.model}",
              "messages": [
                {"role": "user", "content": ${request.prompt.quoteJson()}}
              ]
            }
        """.trimIndent()

        ProviderFlavor.ANTHROPIC -> """
            {
              "model": "${request.model}",
              "max_tokens": 1024,
              "messages": [
                {"role": "user", "content": ${request.prompt.quoteJson()}}
              ]
            }
        """.trimIndent()
    }

private fun String.quoteJson(): String =
    "\"" + replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\""
