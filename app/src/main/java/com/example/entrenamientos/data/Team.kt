package com.example.entrenamientos.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teams")
data class Team(
    @PrimaryKey val year: Int,
    val name: String,
    val shortName: String, // NUEVO
    val gender: String,    // NUEVO: "M" o "F"
    val categoryYear: String,
    val colorHex: String,
    val firstTrainingDate: String = "2026-09-01",
    val trackMatches: Boolean = true
)