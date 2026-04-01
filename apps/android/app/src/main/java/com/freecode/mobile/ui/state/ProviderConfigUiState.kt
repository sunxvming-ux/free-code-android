package com.freecode.mobile.ui.state

import com.freecode.mobile.domain.model.ProviderAuthMode

data class ProviderConfigUiState(
    val providerId: String = "",
    val baseUrl: String = "",
    val apiKey: String = "",
    val defaultModel: String = "",
    val authMode: ProviderAuthMode = ProviderAuthMode.API_KEY,
    val oauthAccessToken: String = "",
    val oauthRefreshToken: String = "",
    val oauthClientId: String = "",
)
