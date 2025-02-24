package com.example.booktok.model

import androidx.room.TypeConverter
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun toByteArray(data: String?): ByteArray? {
        return data?.let { android.util.Base64.decode(it, android.util.Base64.DEFAULT) }
    }

    @TypeConverter
    fun fromByteArray(bytes: ByteArray?): String? {
        return bytes?.let { android.util.Base64.encodeToString(it, android.util.Base64.DEFAULT) }
    }
}