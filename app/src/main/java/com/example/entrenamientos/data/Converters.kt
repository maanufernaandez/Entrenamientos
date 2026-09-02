package com.example.entrenamientos.data

import androidx.room.TypeConverter
import org.json.JSONArray
import org.json.JSONObject

class Converters {
    @TypeConverter
    fun fromSummonedList(value: String): List<Long> {
        if (value.isEmpty()) return emptyList()
        val jsonArray = JSONArray(value)
        return (0 until jsonArray.length()).map { jsonArray.getLong(it) }
    }

    @TypeConverter
    fun toSummonedList(list: List<Long>): String {
        val jsonArray = JSONArray()
        list.forEach { jsonArray.put(it) }
        return jsonArray.toString()
    }

    // --- Map<Long, String> <-> JSON ---
    @TypeConverter
    fun fromReasonsMap(value: String): Map<Long, String> {
        if (value.isEmpty()) return emptyMap()
        val jsonObject = JSONObject(value)
        return jsonObject.keys().asSequence().mapNotNull { key ->
            key.toLongOrNull()?.let { longKey -> longKey to jsonObject.getString(key) }
        }.toMap()
    }

    @TypeConverter
    fun toReasonsMap(map: Map<Long, String>): String {
        val jsonObject = JSONObject()
        map.forEach { (key, value) -> jsonObject.put(key.toString(), value) }
        return jsonObject.toString()
    }
}