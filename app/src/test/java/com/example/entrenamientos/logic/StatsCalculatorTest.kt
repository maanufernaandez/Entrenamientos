package com.example.entrenamientos.logic

import com.example.entrenamientos.data.Attendance
import com.example.entrenamientos.data.Match
import com.example.entrenamientos.data.Player
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class StatsCalculatorTest {

    private fun attendance(date: String, playerId: Long, status: Int) =
        Attendance(date = date, playerId = playerId, teamYear = 1, status = status)

    // ---------------- Asistencia por jugador ----------------

    @Test
    fun `cuenta presentes y faltas justificadas e injustificadas por jugador`() {
        val players = listOf(Player(id = 1, name = "Ana"), Player(id = 2, name = "Luis"))
        val attendances = listOf(
            attendance("2026-09-14", 1, 0),
            attendance("2026-09-16", 1, 0),
            attendance("2026-09-18", 1, 1),
            attendance("2026-09-14", 2, 2),
            attendance("2026-09-14", 99, 0) // jugador que no está en la lista: se ignora
        )

        val result = StatsCalculator.attendanceCounts(players, attendances)

        val ana = result.first { it.player.id == 1L }
        assertEquals(2, ana.present)
        assertEquals(1, ana.justified)
        assertEquals(0, ana.unjustified)

        val luis = result.first { it.player.id == 2L }
        assertEquals(0, luis.present)
        assertEquals(1, luis.unjustified)
    }

    @Test
    fun `los jugadores salen de mas a menos presencias y en empate conservan su orden`() {
        val players = listOf(
            Player(id = 1, name = "A"),
            Player(id = 2, name = "B"),
            Player(id = 3, name = "C")
        )
        val attendances = listOf(
            attendance("2026-09-14", 3, 0),
            attendance("2026-09-14", 2, 0),
            attendance("2026-09-16", 3, 0)
        )

        val result = StatsCalculator.attendanceCounts(players, attendances)

        assertEquals(listOf(3L, 2L, 1L), result.map { it.player.id })
    }

    // ---------------- Semanas por mes ----------------

    @Test
    fun `la semana se asigna al mes en el que caen mas dias y los meses salen de mas reciente a mas antiguo`() {
        // 2026-09-28 (lunes): 3 días de septiembre y 4 de octubre -> octubre.
        val validWeeks = setOf(
            LocalDate.parse("2026-09-07"),
            LocalDate.parse("2026-09-21"),
            LocalDate.parse("2026-09-28")
        )

        val result = StatsCalculator.weeksByMonth(validWeeks, emptyList())

        assertEquals(listOf(YearMonth.of(2026, 10), YearMonth.of(2026, 9)), result.map { it.month })
        assertEquals(
            listOf(LocalDate.parse("2026-09-21"), LocalDate.parse("2026-09-07")),
            result[1].weeks.map { it.weekStart }
        )
    }

    @Test
    fun `las asistencias se asignan a la semana de su lunes y una semana sin registros queda vacia`() {
        val validWeeks = setOf(LocalDate.parse("2026-09-14"), LocalDate.parse("2026-09-21"))
        val attendances = listOf(attendance("2026-09-16", 1, 0)) // miércoles de la semana del 14

        val weeks = StatsCalculator.weeksByMonth(validWeeks, attendances).single().weeks

        assertEquals(1, weeks.first { it.weekStart == LocalDate.parse("2026-09-14") }.attendances.size)
        assertTrue(weeks.first { it.weekStart == LocalDate.parse("2026-09-21") }.attendances.isEmpty())
    }

    @Test
    fun `una asistencia con fecha ilegible se ignora sin fallar`() {
        val validWeeks = setOf(LocalDate.parse("2026-09-14"))
        val attendances = listOf(
            attendance("no-es-fecha", 1, 0),
            attendance("2026-09-16", 1, 0)
        )

        val weeks = StatsCalculator.weeksByMonth(validWeeks, attendances).single().weeks

        assertEquals(1, weeks.single().attendances.size)
    }

    // ---------------- Partidos ----------------

    private val localWin = Match(
        id = 1, isLocal = true, resultLocal = 60, resultVisitor = 50, ftMade = 5, ftAttempted = 8
    )
    private val visitorLoss = Match(
        id = 2, isLocal = false, resultLocal = 70, resultVisitor = 55, ftMade = 3, ftAttempted = 4
    )
    private val localLoss = Match(
        id = 3, isLocal = true, resultLocal = 40, resultVisitor = 45
    )
    private val tie = Match(
        id = 4, isLocal = true, resultLocal = 50, resultVisitor = 50
    )
    private val notPlayed = Match(
        id = 5, isLocal = true, ftMade = 9, ftAttempted = 9
    )

    @Test
    fun `sin partidos jugados no hay estadisticas`() {
        assertNull(StatsCalculator.matchSeasonStats(emptyList()))
        assertNull(StatsCalculator.matchSeasonStats(listOf(notPlayed)))
    }

    @Test
    fun `victorias y derrotas se cuentan desde el punto de vista de nuestro equipo`() {
        val stats = StatsCalculator.matchSeasonStats(listOf(localWin, visitorLoss, localLoss))!!

        assertEquals(3, stats.played)
        assertEquals(1, stats.wins)
        assertEquals(2, stats.losses)
        assertEquals(1, stats.localWins)
        assertEquals(1, stats.localLosses)
        assertEquals(0, stats.visitorWins)
        assertEquals(1, stats.visitorLosses)
    }

    @Test
    fun `puntos a favor y en contra tienen en cuenta si jugamos en casa o fuera`() {
        val stats = StatsCalculator.matchSeasonStats(listOf(localWin, visitorLoss))!!

        // Local: 60 a favor, 50 en contra. Visitante: 55 a favor, 70 en contra.
        assertEquals(115, stats.pointsFor)
        assertEquals(120, stats.pointsAgainst)
        assertEquals(57.5f, stats.avgPointsFor, 0.001f)
        assertEquals(60f, stats.avgPointsAgainst, 0.001f)
    }

    @Test
    fun `los partidos sin marcador no cuentan ni sus tiros libres`() {
        val stats = StatsCalculator.matchSeasonStats(listOf(localWin, notPlayed))!!

        assertEquals(1, stats.played)
        assertEquals(5, stats.ftMade)
        assertEquals(8, stats.ftAttempted)
    }

    @Test
    fun `un empate cuenta como jugado pero no como victoria ni derrota`() {
        val stats = StatsCalculator.matchSeasonStats(listOf(tie))!!

        assertEquals(1, stats.played)
        assertEquals(0, stats.wins)
        assertEquals(0, stats.losses)
        assertEquals(50, stats.pointsFor)
    }

    @Test
    fun `porcentaje de tiros libres sobre el total y cero si no hay intentos`() {
        val withFt = StatsCalculator.matchSeasonStats(listOf(localWin, visitorLoss))!!
        assertEquals(66.666f, withFt.ftPercentage, 0.01f) // 8 de 12

        val noFt = StatsCalculator.matchSeasonStats(listOf(localLoss))!!
        assertEquals(0f, noFt.ftPercentage, 0.0001f)
    }

    // ---------------- Desconvocatorias ----------------

    @Test
    fun `las desconvocatorias solo cuentan en partidos con la convocatoria guardada`() {
        val saved = Match(
            id = 1,
            isConvocatoriaSaved = true,
            unsummonedReasons = mapOf("7" to "Lesión", "8" to "Estudios")
        )
        val savedAgain = Match(
            id = 2,
            isConvocatoriaSaved = true,
            unsummonedReasons = mapOf("7" to "Lesión")
        )
        val notSaved = Match(
            id = 3,
            isConvocatoriaSaved = false,
            unsummonedReasons = mapOf("7" to "Lesión")
        )

        val stats = StatsCalculator.unsummonedReasonCounts(listOf(saved, savedAgain, notSaved))

        assertEquals(mapOf("Lesión" to 2), stats[7L])
        assertEquals(mapOf("Estudios" to 1), stats[8L])
    }

    @Test
    fun `un identificador de jugador ilegible se agrupa bajo el cero`() {
        val match = Match(
            id = 1,
            isConvocatoriaSaved = true,
            unsummonedReasons = mapOf("abc" to "Otro")
        )

        assertEquals(mapOf("Otro" to 1), StatsCalculator.unsummonedReasonCounts(listOf(match))[0L])
    }

    // ---------------- Partidos por mes ----------------

    @Test
    fun `los partidos se agrupan por mes de mas reciente a mas antiguo e ignoran fechas ilegibles`() {
        val sept1 = Match(id = 1, date = "2026-09-13")
        val sept2 = Match(id = 2, date = "2026-09-27")
        val oct = Match(id = 3, date = "2026-10-11")
        val broken = Match(id = 4, date = "sin-fecha")

        val result = StatsCalculator.matchesByMonth(listOf(sept1, oct, sept2, broken))

        assertEquals(listOf(YearMonth.of(2026, 10), YearMonth.of(2026, 9)), result.map { it.month })
        assertEquals(listOf(2L, 1L), result[1].matches.map { it.id })
        assertEquals(3, result.sumOf { it.matches.size })
    }

    // ---------------- Formato ----------------

    @Test
    fun `formatStat quita el cero decimal y usa punto`() {
        assertEquals("62", StatsCalculator.formatStat(62f))
        assertEquals("62.5", StatsCalculator.formatStat(62.5f))
        assertEquals("66.7", StatsCalculator.formatStat(66.666f))
        assertEquals("0", StatsCalculator.formatStat(0f))
    }
}