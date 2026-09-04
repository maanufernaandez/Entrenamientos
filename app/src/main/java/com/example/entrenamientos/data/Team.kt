package com.example.entrenamientos.data

data class Team(
    val year: Int = 0,
    val name: String = "",
    val shortName: String = "",
    val gender: String = "M",
    val categoryYear: String = "",
    val colorHex: String = "#2196F3",
    val trackMatches: Boolean = true,
    val firstTrainingDate: String = "2026-09-01",
    val lastTrainingDate: String = "2027-05-31"
)