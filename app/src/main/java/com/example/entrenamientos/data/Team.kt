package com.example.entrenamientos.data

data class Team(
    val year: Int = 0,
    val name: String = "",
    val shortName: String = "",
    val gender: String = "",
    val categoryYear: String = "",
    val colorHex: String = "",
    val firstTrainingDate: String = "2026-09-01",
    val trackMatches: Boolean = true
)