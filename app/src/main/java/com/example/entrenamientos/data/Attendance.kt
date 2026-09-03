package com.example.entrenamientos.data

data class Attendance(
    val id: Long = 0L,
    val date: String = "",
    val playerId: Long = 0L,
    val teamYear: Int = 0,
    val status: Int = 0
)