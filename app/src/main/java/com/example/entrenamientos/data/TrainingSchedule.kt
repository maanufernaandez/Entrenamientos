package com.example.entrenamientos.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "training_schedules")
data class TrainingSchedule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val teamYear: Int,
    val dayOfWeek: Int, // 1 = Lunes, 2 = Martes, ..., 7 = Domingo
    val startTime: String, // Ejemplo: "17:00"
    val endTime: String    // Ejemplo: "18:30"
)