package com.example.entrenamientos.data

data class TrainingSchedule(
    val id: Long = 0L,
    val teamYear: Int = 0,
    val dayOfWeek: Int = 1,
    val startTime: String = "",
    val endTime: String = ""
)