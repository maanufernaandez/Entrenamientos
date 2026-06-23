package com.example.entrenamientos.domain

import java.time.DayOfWeek
import java.time.LocalDate

object CalendarLogic {
    val seasonStartDate: LocalDate = LocalDate.of(2026, 9, 1)
    val seasonEndDate: LocalDate = LocalDate.of(2027, 5, 31)

    // Devuelve qué equipos entrenan en una fecha concreta (2018 o 2013)
    fun getTrainingTeams(date: LocalDate): List<Int> {
        val day = date.dayOfWeek
        val teams = mutableListOf<Int>()

        // Prebenjamin: Martes y Jueves
        if (day == DayOfWeek.TUESDAY || day == DayOfWeek.THURSDAY) {
            teams.add(2018)
        }

        // Infantil: Lunes, Jueves y Viernes
        if (day == DayOfWeek.MONDAY || day == DayOfWeek.THURSDAY || day == DayOfWeek.FRIDAY) {
            teams.add(2013)
        }

        return teams
    }

    // Devuelve si es sábado a partir del 10 de octubre
    fun isInfantilMatchDay(date: LocalDate): Boolean {
        val firstMatchDate = LocalDate.of(2026, 10, 10)
        return date.dayOfWeek == DayOfWeek.SATURDAY && !date.isBefore(firstMatchDate) && !date.isAfter(seasonEndDate)
    }
}