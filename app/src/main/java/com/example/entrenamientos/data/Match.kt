package com.example.entrenamientos.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matches")
data class Match(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // Formato ISO y hora "YYYY-MM-DD HH:mm"
    val pointsFor: Int = 0,
    val pointsAgainst: Int = 0,
    val freeThrowPercentage: Float = 0f,
    val isInfantil: Boolean = true // Solo las infantiles tienen partidos de momento
)