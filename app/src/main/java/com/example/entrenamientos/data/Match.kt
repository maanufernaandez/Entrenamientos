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
    val resultLocal: Int? = null,
    val resultVisitor: Int? = null,
    val ftMade: Int = 0,
    val ftAttempted: Int = 0,
    val isConvocatoriaSaved: Boolean = false
)