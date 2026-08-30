package com.example.entrenamientos.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teams")
data class Team(
    @PrimaryKey val year: Int, // ID interno (no se muestra al usuario)
    val name: String,
    val categoryYear: String,  // El año visible (opcional)
    val colorHex: String,
    val firstTrainingDate: String = "2026-09-01"
)