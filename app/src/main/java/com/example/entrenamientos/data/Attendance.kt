package com.example.entrenamientos.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attendances")
data class Attendance(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // Formato ISO "YYYY-MM-DD"
    val playerId: Long,
    val status: Int, // 0: Verde (Defecto), 1: Amarillo (Justificado), 2: Rojo (Ausente)
    val teamYear: Int
)