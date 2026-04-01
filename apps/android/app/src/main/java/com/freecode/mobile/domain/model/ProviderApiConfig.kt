package com.freecode.mobile.domain.model

enum class ProviderAuthMode {
    API_KEY,
    OAUTH,
}

data class ProviderApiConfig(
    val providerId: String,
    val baseUrl: String = "",
    val apiKey: String = "",
    val defaultModel: String = "",
    val authMode: ProviderAuthMode = ProviderAuthMode.API_KEY,
    val oauthAccessToken: String = "",
    val oauthRefreshToken: String = "",
    val oauthClientId: String = "",
)
