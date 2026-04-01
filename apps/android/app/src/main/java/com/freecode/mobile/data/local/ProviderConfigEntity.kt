package com.freecode.mobile.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.freecode.mobile.domain.model.ProviderAuthMode

@Entity(tableName = "provider_configs")
data class ProviderConfigEntity(
    @PrimaryKey val providerId: String,
    val baseUrl: String,
    val apiKey: String,
    val defaultModel: String,
    val authMode: ProviderAuthMode,
    val oauthAccessToken: String,
    val oauthRefreshToken: String,
    val oauthClientId: String,
)
