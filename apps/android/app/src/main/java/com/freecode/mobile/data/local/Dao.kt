package com.freecode.mobile.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY name ASC")
    fun observeAll(): Flow<List<ContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ContactEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ContactEntity)

    @Query("DELETE FROM contacts WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface ConversationThreadDao {
    @Query("SELECT * FROM threads ORDER BY pinned DESC, updatedAt DESC")
    fun observeAll(): Flow<List<ConversationThreadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ConversationThreadEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ConversationThreadEntity)

    @Query("DELETE FROM threads WHERE aiId = :aiId")
    suspend fun deleteByAiId(aiId: String)
}

@Dao
interface ProviderSettingDao {
    @Query("SELECT * FROM providers ORDER BY title ASC")
    fun observeAll(): Flow<List<ProviderSettingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ProviderSettingEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ProviderSettingEntity)

    @Query("UPDATE providers SET enabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: String, enabled: Boolean)
}

@Dao
interface ProviderConfigDao {
    @Query("SELECT * FROM provider_configs WHERE providerId = :providerId LIMIT 1")
    suspend fun getById(providerId: String): ProviderConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ProviderConfigEntity)
}

@Dao
interface ConversationMessageDao {
    @Query("SELECT * FROM conversation_messages ORDER BY timestamp ASC")
    fun observeAll(): Flow<List<ConversationMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ConversationMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ConversationMessageEntity>)

    @Query("DELETE FROM conversation_messages WHERE threadId = :threadId")
    suspend fun deleteByThreadId(threadId: String)
}
