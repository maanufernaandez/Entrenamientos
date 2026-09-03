package com.example.entrenamientos.data

data class Match(
    val id: Long = 0L,
    val date: String = "",
    val time: String = "",
    val isLocal: Boolean = true,
    val location: String = "",
    val opponent: String = "",
    val teamYear: Int = 0,
    val isConvocatoriaSaved: Boolean = false,
    val summonedPlayers: List<Long> = emptyList(),
    val unsummonedReasons: Map<Long, String> = emptyMap(),
    val resultLocal: Int? = null,
    val resultVisitor: Int? = null
)