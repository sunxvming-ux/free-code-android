package com.freecode.mobile.domain.model

data class ProviderApiConfig(
    val providerId: String,
    val baseUrl: String = "",
    val apiKey: String = "",
    val defaultModel: String = "",
)
