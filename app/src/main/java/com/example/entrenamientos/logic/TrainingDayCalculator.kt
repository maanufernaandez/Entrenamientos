package com.example.entrenamientos.logic

import com.example.entrenamientos.data.Match
import com.example.entrenamientos.data.Team
import com.example.entrenamientos.data.TrainingSchedule
import java.time.LocalDate

/**
 * Lógica pura (sin Android ni Firebase) para saber qué entrenamientos hay cada
 * día y qué rango de fechas debe mostrar el calendario.
 */
object TrainingDayCalculator {

    // Valores por defecto de Team cuando la fecha falta o no se puede leer.
    val DEFAULT_FIRST_DATE: LocalDate = LocalDate.of(2026, 9, 1)
    val DEFAULT_LAST_DATE: LocalDate = LocalDate.of(2027, 5, 31)

    fun firstDateOf(team: Team): LocalDate =
        parseOrDefault(team.firstTrainingDate, DEFAULT_FIRST_DATE)

    fun lastDateOf(team: Team): LocalDate =
        parseOrDefault(team.lastTrainingDate, DEFAULT_LAST_DATE)

    /**
     * Horarios que se entrenan en [date]: mismo día de la semana, no festivo, y
     * dentro de [firstDateOf]..[lastDateOf] (ambos inclusive) de su equipo.
     * Los horarios de un equipo que ya no existe se ignoran. Ordenados por hora.
     */
    fun schedulesForDate(
        date: LocalDate,
        schedules: List<TrainingSchedule>,
        teams: List<Team>,
        holidayDates: Set<String>
    ): List<TrainingSchedule> {
        if (holidayDates.contains(date.toString())) return emptyList()

        val dayValue = date.dayOfWeek.value

        return schedules
            .filter { schedule ->
                if (schedule.dayOfWeek != dayValue) return@filter false

                val team = teams.find { it.year == schedule.teamYear }
                    ?: return@filter false

                !date.isBefore(firstDateOf(team)) && !date.isAfter(lastDateOf(team))
            }
            .sortedBy { it.startTime }
    }

    fun teamYearsForDate(
        date: LocalDate,
        schedules: List<TrainingSchedule>,
        teams: List<Team>,
        holidayDates: Set<String>
    ): List<Int> =
        schedulesForDate(date, schedules, teams, holidayDates)
            .map { it.teamYear }
            .distinct()

    /**
     * Rango de fechas que debe cubrir el calendario: desde el primer entrenamiento
     * (o partido) más temprano hasta el último entrenamiento (o partido) más tardío.
     * Incluir los partidos evita que uno fuera del periodo de entrenamientos
     * (amistoso de pretemporada, playoff) quede inaccesible.
     */
    fun seasonBounds(
        teams: List<Team>,
        matches: List<Match>
    ): Pair<LocalDate, LocalDate> {
        val matchDates = matches.mapNotNull { parseOrNull(it.date) }

        val start = (teams.map { firstDateOf(it) } + matchDates).minOrNull()
            ?: DEFAULT_FIRST_DATE

        val end = (teams.map { lastDateOf(it) } + matchDates).maxOrNull()
            ?: DEFAULT_LAST_DATE

        return start to (if (end.isBefore(start)) start else end)
    }

    private fun parseOrNull(value: String): LocalDate? =
        try {
            LocalDate.parse(value)
        } catch (_: Exception) {
            null
        }

    private fun parseOrDefault(value: String, default: LocalDate): LocalDate =
        parseOrNull(value) ?: default
}