package com.example.entrenamientos.logic

import com.example.entrenamientos.data.Attendance
import com.example.entrenamientos.data.Match
import com.example.entrenamientos.data.Player
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

/** Asistencias de un jugador: presente, falta justificada y falta injustificada. */
data class PlayerAttendanceCount(
    val player: Player,
    val present: Int,
    val justified: Int,
    val unjustified: Int
)

/** Una semana (identificada por su lunes) con las asistencias registradas en ella. */
data class WeekAttendance(
    val weekStart: LocalDate,
    val attendances: List<Attendance>
)

/** Semanas que pertenecen a un mes, de la más reciente a la más antigua. */
data class MonthWeeks(
    val month: YearMonth,
    val weeks: List<WeekAttendance>
)

/** Partidos de un mes, del más reciente al más antiguo. */
data class MonthMatches(
    val month: YearMonth,
    val matches: List<Match>
)

/** Resumen de los partidos jugados (con marcador de los dos equipos). */
data class MatchSeasonStats(
    val played: Int,
    val wins: Int,
    val losses: Int,
    val localWins: Int,
    val localLosses: Int,
    val visitorWins: Int,
    val visitorLosses: Int,
    val pointsFor: Int,
    val pointsAgainst: Int,
    val ftMade: Int,
    val ftAttempted: Int
) {
    val avgPointsFor: Float
        get() = if (played > 0) pointsFor.toFloat() / played else 0f

    val avgPointsAgainst: Float
        get() = if (played > 0) pointsAgainst.toFloat() / played else 0f

    val ftPercentage: Float
        get() = if (ftAttempted > 0) ftMade.toFloat() / ftAttempted * 100 else 0f
}

/**
 * Cálculos de la pantalla de estadísticas, sin Android ni Compose, para poder
 * probarlos y para no recalcularlos dentro de la interfaz.
 *
 * Todas las fechas se leen de forma segura: un dato con una fecha mal guardada
 * se ignora en lugar de cerrar la aplicación.
 */
object StatsCalculator {

    const val STATUS_PRESENT = 0
    const val STATUS_JUSTIFIED = 1
    const val STATUS_UNJUSTIFIED = 2

    /** Asistencias por jugador, de más a menos presencias (en empate, orden original). */
    fun attendanceCounts(
        players: List<Player>,
        attendances: List<Attendance>
    ): List<PlayerAttendanceCount> {

        val byPlayer = attendances.groupBy { it.playerId }

        return players
            .map { player ->
                val playerAttendances = byPlayer[player.id].orEmpty()

                PlayerAttendanceCount(
                    player = player,
                    present = playerAttendances.count { it.status == STATUS_PRESENT },
                    justified = playerAttendances.count { it.status == STATUS_JUSTIFIED },
                    unjustified = playerAttendances.count { it.status == STATUS_UNJUSTIFIED }
                )
            }
            .sortedByDescending { it.present }
    }

    /**
     * Agrupa las semanas válidas por mes (el mes en el que caen más días de la
     * semana), con los meses y las semanas de más reciente a más antiguo.
     */
    fun weeksByMonth(
        validWeeks: Collection<LocalDate>,
        attendances: List<Attendance>
    ): List<MonthWeeks> {

        val attendancesByWeek = attendances
            .mapNotNull { attendance ->
                parseDateOrNull(attendance.date)?.let { date ->
                    date.with(DayOfWeek.MONDAY) to attendance
                }
            }
            .groupBy({ it.first }, { it.second })

        return validWeeks
            .map { weekStart ->
                WeekAttendance(
                    weekStart = weekStart,
                    attendances = attendancesByWeek[weekStart].orEmpty()
                )
            }
            .groupBy { monthOfWeek(it.weekStart) }
            .toSortedMap(compareByDescending { it })
            .map { (month, weeks) ->
                MonthWeeks(
                    month = month,
                    weeks = weeks.sortedByDescending { it.weekStart }
                )
            }
    }

    /** Estadísticas de los partidos jugados, o null si no hay ninguno. */
    fun matchSeasonStats(matches: List<Match>): MatchSeasonStats? {

        val played = matches.filter { it.resultLocal != null && it.resultVisitor != null }

        if (played.isEmpty()) return null

        var wins = 0
        var losses = 0
        var localWins = 0
        var localLosses = 0
        var visitorWins = 0
        var visitorLosses = 0
        var pointsFor = 0
        var pointsAgainst = 0
        var ftMade = 0
        var ftAttempted = 0

        played.forEach { match ->
            val localScore = match.resultLocal ?: 0
            val visitorScore = match.resultVisitor ?: 0

            if (match.isLocal) {
                pointsFor += localScore
                pointsAgainst += visitorScore

                if (localScore > visitorScore) {
                    wins++
                    localWins++
                } else if (localScore < visitorScore) {
                    losses++
                    localLosses++
                }
            } else {
                pointsFor += visitorScore
                pointsAgainst += localScore

                if (visitorScore > localScore) {
                    wins++
                    visitorWins++
                } else if (visitorScore < localScore) {
                    losses++
                    visitorLosses++
                }
            }

            ftMade += match.ftMade
            ftAttempted += match.ftAttempted
        }

        return MatchSeasonStats(
            played = played.size,
            wins = wins,
            losses = losses,
            localWins = localWins,
            localLosses = localLosses,
            visitorWins = visitorWins,
            visitorLosses = visitorLosses,
            pointsFor = pointsFor,
            pointsAgainst = pointsAgainst,
            ftMade = ftMade,
            ftAttempted = ftAttempted
        )
    }

    /**
     * Desconvocatorias por jugador y motivo, contando solo los partidos con la
     * convocatoria guardada.
     */
    fun unsummonedReasonCounts(matches: List<Match>): Map<Long, Map<String, Int>> {

        val stats = mutableMapOf<Long, MutableMap<String, Int>>()

        matches
            .filter { it.isConvocatoriaSaved }
            .forEach { match ->
                match.unsummonedReasons.forEach { (playerIdText, reason) ->
                    val playerId = playerIdText.toLongOrNull() ?: 0L
                    val playerStats = stats.getOrPut(playerId) { mutableMapOf() }

                    playerStats[reason] = playerStats.getOrDefault(reason, 0) + 1
                }
            }

        return stats
    }

    /** Partidos agrupados por mes, de más reciente a más antiguo. Ignora fechas ilegibles. */
    fun matchesByMonth(matches: List<Match>): List<MonthMatches> =
        matches
            .mapNotNull { match ->
                parseDateOrNull(match.date)?.let { date ->
                    YearMonth.from(date) to match
                }
            }
            .groupBy({ it.first }, { it.second })
            .toSortedMap(compareByDescending { it })
            .map { (month, monthMatches) ->
                MonthMatches(
                    month = month,
                    matches = monthMatches.sortedByDescending { it.date }
                )
            }

    /** Un decimal, sin ".0" cuando es entero: 62.0 -> "62", 62.5 -> "62.5". */
    fun formatStat(value: Float): String =
        String.format(Locale.US, "%.1f", value).removeSuffix(".0")

    private fun monthOfWeek(weekStart: LocalDate): YearMonth =
        (0L..6L)
            .map { offset -> YearMonth.from(weekStart.plusDays(offset)) }
            .groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key
            ?: YearMonth.from(weekStart)

    private fun parseDateOrNull(value: String): LocalDate? =
        try {
            LocalDate.parse(value)
        } catch (_: Exception) {
            null
        }
}