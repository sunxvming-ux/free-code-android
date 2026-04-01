package com.freecode.mobile.domain.service

import com.freecode.mobile.domain.model.ProviderApiConfig
import com.freecode.mobile.domain.model.ProviderAuthMode
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

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
                    if (config.authMode == ProviderAuthMode.OAUTH && config.oauthAccessToken.isNotBlank()) {
                        connection.setRequestProperty("Authorization", "Bearer ${config.oauthAccessToken}")
                    } else if (config.apiKey.isNotBlank()) {
                        connection.setRequestProperty("x-api-key", config.apiKey)
                    }
                    connection.setRequestProperty("anthropic-version", "2023-06-01")
                }

                ProviderFlavor.OPENAI,
                ProviderFlavor.GENERIC -> {
                    val bearer = when {
                        config.authMode == ProviderAuthMode.OAUTH && config.oauthAccessToken.isNotBlank() -> config.oauthAccessToken
                        config.apiKey.isNotBlank() -> config.apiKey
                        else -> ""
                    }
                    if (bearer.isNotBlank()) {
                        connection.setRequestProperty("Authorization", "Bearer $bearer")
                    }
                }
            }

            val body = buildRequestBody(providerFlavor, request).toString()
            OutputStreamWriter(connection.outputStream).use { it.write(body) }
            val stream = if (connection.responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            val payload = stream?.let { BufferedReader(it.reader()).use { reader -> reader.readText() } }.orEmpty()
            val parsedContent = parseResponseContent(
                flavor = providerFlavor,
                payload = payload,
                statusCode = connection.responseCode,
                statusMessage = connection.responseMessage.orEmpty(),
            )
            ModelResponse(
                content = parsedContent.ifBlank { payload.ifBlank { "Empty response from $targetUrl" } },
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

private fun buildRequestBody(flavor: ProviderFlavor, request: ModelRequest): JSONObject =
    when (flavor) {
        ProviderFlavor.OPENAI, ProviderFlavor.GENERIC -> JSONObject().apply {
            put("model", request.model)
            put("messages", buildOpenAiMessages(request))
        }

        ProviderFlavor.ANTHROPIC -> JSONObject().apply {
            put("model", request.model)
            put("max_tokens", 1024)
            if (request.systemPrompt.isNotBlank()) {
                put("system", request.systemPrompt)
            }
            put("messages", buildAnthropicMessages(request))
        }
    }

private fun parseResponseContent(flavor: ProviderFlavor, payload: String): String {
    if (payload.isBlank()) return ""
    return when (flavor) {
        ProviderFlavor.OPENAI, ProviderFlavor.GENERIC -> parseOpenAiPayload(payload)

        ProviderFlavor.ANTHROPIC -> parseAnthropicPayload(payload)
    }.ifBlank { payload }
}

private fun parseResponseContent(
    flavor: ProviderFlavor,
    payload: String,
    statusCode: Int,
    statusMessage: String,
): String {
    if (statusCode !in 200..299) {
        return parseErrorPayload(payload).ifBlank {
            "HTTP $statusCode ${statusMessage.ifBlank { "request failed" }}"
        }
    }
    return parseResponseContent(flavor, payload)
}

private fun buildOpenAiMessages(request: ModelRequest): JSONArray = JSONArray().apply {
    if (request.systemPrompt.isNotBlank()) {
        put(
            JSONObject().apply {
                put("role", "system")
                put("content", request.systemPrompt)
            },
        )
    }
    request.messages.forEach { message ->
        put(
            JSONObject().apply {
                put("role", message.role)
                put("content", message.content)
            },
        )
    }
    if (request.prompt.isNotBlank()) {
        put(
            JSONObject().apply {
                put("role", "user")
                put("content", request.prompt)
            },
        )
    }
}

private fun buildAnthropicMessages(request: ModelRequest): JSONArray = JSONArray().apply {
    request.messages.forEach { message ->
        if (message.role != "system") {
            put(
                JSONObject().apply {
                    put("role", if (message.role == "assistant") "assistant" else "user")
                    put("content", message.content)
                },
            )
        }
    }
    if (request.prompt.isNotBlank()) {
        put(
            JSONObject().apply {
                put("role", "user")
                put("content", request.prompt)
            },
        )
    }
}

private fun parseOpenAiPayload(payload: String): String = runCatching {
    val root = JSONObject(payload)
    val choices = root.optJSONArray("choices") ?: return@runCatching payload
    if (choices.length() == 0) return@runCatching payload
    val first = choices.optJSONObject(0) ?: return@runCatching payload
    val message = first.optJSONObject("message")
    val content = message?.opt("content")
    when (content) {
        is String -> content
        is JSONArray -> buildString {
            for (i in 0 until content.length()) {
                val block = content.optJSONObject(i) ?: continue
                val text = block.optString("text")
                if (text.isNotBlank()) {
                    if (isNotEmpty()) append('\n')
                    append(text)
                }
            }
        }
        else -> null
    }.takeUnless { it.isNullOrBlank() }
        ?: first.optString("text").takeUnless { it.isNullOrBlank() }
        ?: payload
}.getOrDefault(payload)

private fun parseAnthropicPayload(payload: String): String = runCatching {
    val root = JSONObject(payload)
    val content = root.optJSONArray("content")
    if (content != null && content.length() > 0) {
        buildString {
            for (i in 0 until content.length()) {
                val block = content.optJSONObject(i) ?: continue
                val text = block.optString("text")
                if (text.isNotBlank()) {
                    if (isNotEmpty()) append('\n')
                    append(text)
                }
            }
        }.ifBlank {
            root.optString("completion").ifBlank { payload }
        }
    } else {
        root.optString("completion").ifBlank { payload }
    }
}.getOrDefault(payload)

private fun parseErrorPayload(payload: String): String = runCatching {
    val root = JSONObject(payload)
    val error = root.optJSONObject("error")
    when {
        error != null -> {
            val type = error.optString("type")
            val message = error.optString("message")
            listOf(type.takeIf { it.isNotBlank() }, message.takeIf { it.isNotBlank() })
                .joinToString(": ")
                .ifBlank { payload }
        }
        root.optString("message").isNotBlank() -> root.optString("message")
        else -> payload
    }
}.getOrDefault(payload)
