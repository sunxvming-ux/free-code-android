package com.freecode.mobile.data

import com.freecode.mobile.data.local.AppDatabase
import com.freecode.mobile.data.local.toDomain
import com.freecode.mobile.data.local.toEntity
import com.freecode.mobile.domain.model.AiContact
import com.freecode.mobile.domain.model.ConversationMessage
import com.freecode.mobile.domain.model.ConversationThread
import com.freecode.mobile.domain.model.ProviderApiConfig
import com.freecode.mobile.domain.model.ProviderSetting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class OfflineAppRepository(
    private val database: AppDatabase,
) : AppRepository {
    override fun observeContacts(): Flow<List<AiContact>> =
        database.contactDao().observeAll().map { items -> items.map { it.toDomain() } }

    override fun observeThreads(): Flow<List<ConversationThread>> =
        database.conversationThreadDao().observeAll()
            .map { items -> items.map { it.toDomain() } }

    override fun observeProviders(): Flow<List<ProviderSetting>> =
        database.providerSettingDao().observeAll().map { items -> items.map { it.toDomain() } }

    override fun observeMessages(): Flow<List<ConversationMessage>> =
        database.conversationMessageDao().observeAll().map { items -> items.map { it.toDomain() } }

    override suspend fun bootstrapIfEmpty(
        contacts: List<AiContact>,
        threads: List<ConversationThread>,
        providers: List<ProviderSetting>,
    ) {
        if (database.contactDao().observeAll().first().isNotEmpty()) return
        database.contactDao().upsertAll(contacts.map { it.toEntity() })
        database.conversationThreadDao().upsertAll(threads.map { it.toEntity() })
        database.providerSettingDao().upsertAll(providers.map { it.toEntity() })
    }

    override suspend fun upsertContact(contact: AiContact) {
        database.contactDao().upsert(contact.toEntity())
    }

    override suspend fun upsertThread(thread: ConversationThread) {
        database.conversationThreadDao().upsert(thread.toEntity())
    }

    override suspend fun upsertMessage(message: ConversationMessage) {
        database.conversationMessageDao().upsert(message.toEntity())
    }

    override suspend fun setProviderEnabled(id: String, enabled: Boolean) {
        database.providerSettingDao().setEnabled(id, enabled)
    }

    override suspend fun deleteContact(contactId: String) {
        database.contactDao().deleteById(contactId)
        database.conversationThreadDao().deleteByAiId(contactId)
    }

    override suspend fun saveProviderConfig(config: ProviderApiConfig) {
        database.providerConfigDao().upsert(config.toEntity())
    }

    override suspend fun getProviderConfig(providerId: String): ProviderApiConfig? =
        database.providerConfigDao().getById(providerId)?.toDomain()
}
