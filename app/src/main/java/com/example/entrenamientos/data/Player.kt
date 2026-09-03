package com.example.entrenamientos.data

data class Player(
    val id: Long = 0L,
    val name: String = "",
    val lastName: String = "",
    val teamYear: Int = 0,
    val dorsal: String? = null
)