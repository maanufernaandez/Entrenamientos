package com.example.entrenamientos.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromSummonedList(value: String): List<Long> {
        if (value.isEmpty()) return emptyList()
        return value.split(",").mapNotNull { it.toLongOrNull() }
    }

    @TypeConverter
    fun toSummonedList(list: List<Long>): String {
        return list.joinToString(",")
    }

    @TypeConverter
    fun fromReasonsMap(value: String): Map<Long, String> {
        if (value.isEmpty()) return emptyMap()
        return value.split("||").associate {
            val parts = it.split("::")
            (parts.getOrNull(0)?.toLongOrNull() ?: 0L) to (parts.getOrNull(1) ?: "")
        }.filterKeys { it != 0L }
    }

    @TypeConverter
    fun toReasonsMap(map: Map<Long, String>): String {
        return map.entries.joinToString("||") { "${it.key}::${it.value}" }
    }
}