package com.example.entrenamientos.data

data class TrainingNote(
    val id: Long = 0L,
    val date: String = "",
    val teamYear: Int = 0,
    val noteType: String = "",
    val content: String = ""
)