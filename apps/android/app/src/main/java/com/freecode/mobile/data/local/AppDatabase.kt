package com.freecode.mobile.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        ContactEntity::class,
        ConversationThreadEntity::class,
        ProviderSettingEntity::class,
        ProviderConfigEntity::class,
        ConversationMessageEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun conversationThreadDao(): ConversationThreadDao
    abstract fun providerSettingDao(): ProviderSettingDao
    abstract fun providerConfigDao(): ProviderConfigDao
    abstract fun conversationMessageDao(): ConversationMessageDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "free_code_android.db",
            ).fallbackToDestructiveMigration().build()
    }
}
