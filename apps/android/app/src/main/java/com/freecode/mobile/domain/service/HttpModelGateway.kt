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

            val connection = URL(config.baseUrl).openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.connectTimeout = 15_000
            connection.readTimeout = 30_000
            connection.setRequestProperty("Content-Type", "application/json")
            if (config.apiKey.isNotBlank()) {
                connection.setRequestProperty("Authorization", "Bearer ${config.apiKey}")
            }

            val body = """
                {
                  "model": "${request.model}",
                  "prompt": ${request.prompt.quoteJson()}
                }
            """.trimIndent()

            OutputStreamWriter(connection.outputStream).use { it.write(body) }
            val stream = if (connection.responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            val payload = stream?.let { BufferedReader(it.reader()).use { reader -> reader.readText() } }.orEmpty()
            ModelResponse(
                content = payload.ifBlank { "Empty response from ${config.baseUrl}" },
                providerLabel = config.providerId.ifBlank { "http" },
            )
        }
    }
}

private fun String.quoteJson(): String =
    "\"" + replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\""
