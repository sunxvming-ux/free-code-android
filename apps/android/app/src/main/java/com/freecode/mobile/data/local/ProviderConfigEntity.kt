package com.freecode.mobile.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "provider_configs")
data class ProviderConfigEntity(
    @PrimaryKey val providerId: String,
    val baseUrl: String,
    val apiKey: String,
    val defaultModel: String,
)
