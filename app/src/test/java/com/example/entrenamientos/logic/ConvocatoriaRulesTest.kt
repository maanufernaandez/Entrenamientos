package com.example.entrenamientos.logic

import com.example.entrenamientos.data.Team
import com.example.entrenamientos.data.TrainingSchedule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class ConvocatoriaRulesTest {

    // Los lunes de septiembre de 2026 son el 7, 14, 21 y 28.
    private val team = Team(year = 1, firstTrainingDate = "2026-09-01", lastTrainingDate = "2027-05-31")
    private val mondays = listOf(
        TrainingSchedule(id = 1, teamYear = 1, dayOfWeek = 1, startTime = "18:00", endTime = "19:30")
    )

    private fun firstMissing(
        matchDate: String,
        attended: Set<String> = emptySet(),
        schedules: List<TrainingSchedule> = mondays,
        teams: List<Team> = listOf(team),
        holidays: Set<String> = emptySet(),
        teamYear: Int = 1
    ): LocalDate? =
        ConvocatoriaRules.firstMissingAttendance(
            LocalDate.parse(matchDate), teamYear, attended, schedules, teams, holidays
        )

    @Test
    fun `sin asistencias falta el primer entrenamiento de la temporada`() {
        assertEquals(LocalDate.parse("2026-09-07"), firstMissing("2026-09-19"))
    }

    @Test
    fun `devuelve el primer entrenamiento sin asistencia`() {
        assertEquals(
            LocalDate.parse("2026-09-14"),
            firstMissing("2026-09-19", attended = setOf("2026-09-07"))
        )
    }

    @Test
    fun `si todos los entrenamientos tienen asistencia no falta nada`() {
        assertNull(firstMissing("2026-09-19", attended = setOf("2026-09-07", "2026-09-14")))
    }

    @Test
    fun `el propio dia del partido no se exige`() {
        // Partido el lunes 14: solo se exige el entrenamiento del 7.
        assertNull(firstMissing("2026-09-14", attended = setOf("2026-09-07")))
    }

    @Test
    fun `un festivo no exige asistencia`() {
        assertEquals(
            LocalDate.parse("2026-09-14"),
            firstMissing("2026-09-19", holidays = setOf("2026-09-07"))
        )
    }

    @Test
    fun `no se exige asistencia antes del primer entrenamiento del equipo`() {
        val lateTeam = team.copy(firstTrainingDate = "2026-09-14")

        assertEquals(LocalDate.parse("2026-09-14"), firstMissing("2026-09-19", teams = listOf(lateTeam)))
    }

    @Test
    fun `los horarios de otro equipo no cuentan`() {
        val otherTeamOnly = mondays.map { it.copy(teamYear = 2) }

        assertNull(firstMissing("2026-09-19", schedules = otherTeamOnly, teams = listOf(team, team.copy(year = 2))))
    }

    @Test
    fun `sin entrenamientos programados no falta nada`() {
        assertNull(firstMissing("2026-09-19", schedules = emptyList()))
    }

    @Test
    fun `un partido de enero pertenece a la temporada que empezo en septiembre anterior`() {
        assertEquals(LocalDate.parse("2026-09-07"), firstMissing("2027-01-11"))
    }
}