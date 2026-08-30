package com.example.entrenamientos.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matches")
data class Match(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val time: String,
    val isLocal: Boolean,
    val location: String,
    val opponent: String,
    val isConvocatoriaSaved: Boolean = false,
    val summonedPlayers: List<Long> = emptyList(),
    val unsummonedReasons: Map<Long, String> = emptyMap(),
    val resultLocal: Int? = null,
    val resultVisitor: Int? = null,
    val ftMade: Int = 0,
    val ftAttempted: Int = 0,
    val teamYear: Int
)