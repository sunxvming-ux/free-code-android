package com.freecode.mobile.data

import com.freecode.mobile.domain.model.AiContact
import com.freecode.mobile.domain.model.ConversationThread
import com.freecode.mobile.domain.model.ProviderSetting
import kotlinx.coroutines.flow.Flow

interface AppRepository {
    fun observeContacts(): Flow<List<AiContact>>
    fun observeThreads(): Flow<List<ConversationThread>>
    fun observeProviders(): Flow<List<ProviderSetting>>

    suspend fun bootstrapIfEmpty(
        contacts: List<AiContact>,
        threads: List<ConversationThread>,
        providers: List<ProviderSetting>,
    )

    suspend fun upsertContact(contact: AiContact)
    suspend fun upsertThread(thread: ConversationThread)
    suspend fun setProviderEnabled(id: String, enabled: Boolean)
}
