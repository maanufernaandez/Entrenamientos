package com.example.entrenamientos.logic

import com.example.entrenamientos.data.Match
import com.example.entrenamientos.data.Team
import com.example.entrenamientos.data.TrainingSchedule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class TrainingDayCalculatorTest {

    // 2026-09-14, 2026-10-05 y 2026-10-12 son lunes; 2026-08-31 también.
    private val team = Team(
        year = 1,
        firstTrainingDate = "2026-09-01",
        lastTrainingDate = "2026-10-05"
    )

    private val mondaySchedule = TrainingSchedule(
        id = 1, teamYear = 1, dayOfWeek = 1, startTime = "18:00", endTime = "19:30"
    )

    private fun schedulesFor(
        date: String,
        schedules: List<TrainingSchedule> = listOf(mondaySchedule),
        teams: List<Team> = listOf(team),
        holidays: Set<String> = emptySet()
    ) = TrainingDayCalculator.schedulesForDate(LocalDate.parse(date), schedules, teams, holidays)

    @Test
    fun `aparece el entrenamiento en su dia de la semana`() {
        assertEquals(1, schedulesFor("2026-09-14").size)
    }

    @Test
    fun `no aparece en otro dia de la semana`() {
        assertTrue(schedulesFor("2026-09-15").isEmpty())
    }

    @Test
    fun `no aparece antes del primer entrenamiento`() {
        assertTrue(schedulesFor("2026-08-31").isEmpty())
    }

    @Test
    fun `el ultimo dia de entrenamiento es inclusivo y el siguiente lunes no aparece`() {
        assertEquals(1, schedulesFor("2026-10-05").size)
        assertTrue(schedulesFor("2026-10-12").isEmpty())
    }

    @Test
    fun `no aparece en festivo`() {
        assertTrue(schedulesFor("2026-09-14", holidays = setOf("2026-09-14")).isEmpty())
    }

    @Test
    fun `se ignoran los horarios de un equipo que no existe`() {
        val orphan = mondaySchedule.copy(teamYear = 99)
        assertTrue(schedulesFor("2026-09-14", schedules = listOf(orphan)).isEmpty())
    }

    @Test
    fun `los horarios del dia salen ordenados por hora de inicio`() {
        val late = mondaySchedule.copy(id = 2, startTime = "18:30")
        val early = mondaySchedule.copy(id = 3, startTime = "17:00")

        val result = schedulesFor("2026-09-14", schedules = listOf(late, early))

        assertEquals(listOf("17:00", "18:30"), result.map { it.startTime })
    }

    @Test
    fun `teamYearsForDate no repite equipos`() {
        val second = mondaySchedule.copy(id = 2, startTime = "20:00")

        val years = TrainingDayCalculator.teamYearsForDate(
            LocalDate.parse("2026-09-14"), listOf(mondaySchedule, second), listOf(team), emptySet()
        )

        assertEquals(listOf(1), years)
    }

    @Test
    fun `una fecha ilegible en el equipo usa la fecha por defecto`() {
        val broken = team.copy(firstTrainingDate = "no-es-fecha")
        assertEquals(TrainingDayCalculator.DEFAULT_FIRST_DATE, TrainingDayCalculator.firstDateOf(broken))
    }

    @Test
    fun `sin equipos ni partidos el rango es el de por defecto`() {
        val (start, end) = TrainingDayCalculator.seasonBounds(emptyList(), emptyList())

        assertEquals(TrainingDayCalculator.DEFAULT_FIRST_DATE, start)
        assertEquals(TrainingDayCalculator.DEFAULT_LAST_DATE, end)
    }

    @Test
    fun `el rango va de la primera a la ultima fecha de entrenamiento`() {
        val (start, end) = TrainingDayCalculator.seasonBounds(listOf(team), emptyList())

        assertEquals(LocalDate.parse("2026-09-01"), start)
        assertEquals(LocalDate.parse("2026-10-05"), end)
    }

    @Test
    fun `un partido fuera del periodo de entrenamientos amplia el rango`() {
        val before = Match(id = 1, date = "2026-08-25")
        val after = Match(id = 2, date = "2026-10-20")

        val (start, end) = TrainingDayCalculator.seasonBounds(listOf(team), listOf(before, after))

        assertEquals(LocalDate.parse("2026-08-25"), start)
        assertEquals(LocalDate.parse("2026-10-20"), end)
    }

    @Test
    fun `si el fin es anterior al inicio el rango se queda en el inicio`() {
        val inverted = team.copy(firstTrainingDate = "2026-10-01", lastTrainingDate = "2026-09-01")

        val (start, end) = TrainingDayCalculator.seasonBounds(listOf(inverted), emptyList())

        assertEquals(LocalDate.parse("2026-10-01"), start)
        assertEquals(start, end)
    }
}