package com.freecode.mobile.data.local

import androidx.room.TypeConverter
import com.freecode.mobile.domain.model.PermissionLevel
import com.freecode.mobile.domain.model.ProviderKind

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>): String = value.joinToString("||")

    @TypeConverter
    fun toStringList(value: String): List<String> =
        value.takeIf { it.isNotBlank() }?.split("||") ?: emptyList()

    @TypeConverter
    fun fromPermissionLevel(value: PermissionLevel): String = value.name

    @TypeConverter
    fun toPermissionLevel(value: String): PermissionLevel = PermissionLevel.valueOf(value)

    @TypeConverter
    fun fromProviderKind(value: ProviderKind): String = value.name

    @TypeConverter
    fun toProviderKind(value: String): ProviderKind = ProviderKind.valueOf(value)
}
