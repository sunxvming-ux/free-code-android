package com.freecode.mobile.data.local

import com.freecode.mobile.domain.model.ConversationMessage
import com.freecode.mobile.domain.model.ProviderApiConfig

fun ProviderConfigEntity.toDomain(): ProviderApiConfig = ProviderApiConfig(
    providerId = providerId,
    baseUrl = baseUrl,
    apiKey = apiKey,
    defaultModel = defaultModel,
)

fun ProviderApiConfig.toEntity(): ProviderConfigEntity = ProviderConfigEntity(
    providerId = providerId,
    baseUrl = baseUrl,
    apiKey = apiKey,
    defaultModel = defaultModel,
)

fun ConversationMessageEntity.toDomain(): ConversationMessage = ConversationMessage(
    id = id,
    threadId = threadId,
    role = role,
    content = content,
    timestamp = timestamp,
)

fun ConversationMessage.toEntity(): ConversationMessageEntity = ConversationMessageEntity(
    id = id,
    threadId = threadId,
    role = role,
    content = content,
    timestamp = timestamp,
)
