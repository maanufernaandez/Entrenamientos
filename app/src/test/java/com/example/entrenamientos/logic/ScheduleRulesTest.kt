package com.example.entrenamientos.logic

import com.example.entrenamientos.data.TrainingSchedule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScheduleRulesTest {

    private fun schedule(
        id: Long,
        team: Int = 1,
        day: Int = 1,
        start: String = "18:00",
        end: String = "19:30"
    ) = TrainingSchedule(id = id, teamYear = team, dayOfWeek = day, startTime = start, endTime = end)

    // ---------------- parseMinutes ----------------

    @Test
    fun `convierte horas a minutos`() {
        assertEquals(1110, ScheduleRules.parseMinutes("18:30"))
        assertEquals(545, ScheduleRules.parseMinutes("9:05"))
        assertEquals(0, ScheduleRules.parseMinutes("00:00"))
    }

    @Test
    fun `un texto ilegible cuenta como cero`() {
        assertEquals(0, ScheduleRules.parseMinutes(""))
        assertEquals(0, ScheduleRules.parseMinutes("abc"))
        assertEquals(0, ScheduleRules.parseMinutes("18"))
        assertEquals(1080, ScheduleRules.parseMinutes("18:xx"))
    }

    // ---------------- validate ----------------

    @Test
    fun `un horario sin conflictos es valido`() {
        assertNull(ScheduleRules.validate(schedule(1), emptyList()))
    }

    @Test
    fun `el inicio debe ser anterior al fin`() {
        assertEquals(
            ScheduleRules.MSG_START_AFTER_END,
            ScheduleRules.validate(schedule(1, start = "19:30", end = "18:00"), emptyList())
        )
        assertEquals(
            ScheduleRules.MSG_START_AFTER_END,
            ScheduleRules.validate(schedule(1, start = "18:00", end = "18:00"), emptyList())
        )
    }

    @Test
    fun `se rechaza el solape con otro entrenamiento del mismo equipo`() {
        val existing = listOf(schedule(1, start = "18:00", end = "19:30"))

        val error = ScheduleRules.validate(schedule(2, start = "19:00", end = "20:00"), existing)

        assertTrue(error!!.contains("solapa"))
        assertTrue(error.contains("18:00-19:30"))
    }

    @Test
    fun `dos horarios consecutivos no se solapan`() {
        val existing = listOf(schedule(1, start = "18:00", end = "19:30"))

        assertNull(ScheduleRules.validate(schedule(2, start = "19:30", end = "21:00"), existing))
    }

    @Test
    fun `otro equipo puede entrenar a la misma hora`() {
        val existing = listOf(schedule(1, team = 1))

        assertNull(ScheduleRules.validate(schedule(2, team = 2), existing))
    }

    @Test
    fun `un horario de otro dia no cuenta`() {
        val existing = listOf(schedule(1, day = 2))

        assertNull(ScheduleRules.validate(schedule(2, day = 1), existing))
    }

    @Test
    fun `al editar un horario no choca consigo mismo`() {
        val existing = listOf(schedule(1))

        assertNull(ScheduleRules.validate(schedule(1, start = "18:15", end = "19:45"), existing))
    }

    @Test
    fun `no puede haber mas de tres entrenamientos el mismo dia`() {
        val existing = listOf(
            schedule(1, team = 1),
            schedule(2, team = 2),
            schedule(3, team = 3)
        )

        assertEquals(
            ScheduleRules.MSG_TOO_MANY_PER_DAY,
            ScheduleRules.validate(schedule(4, team = 4), existing)
        )
    }

    @Test
    fun `editar uno de los tres entrenamientos del dia es valido`() {
        val existing = listOf(
            schedule(1, team = 1),
            schedule(2, team = 2),
            schedule(3, team = 3)
        )

        assertNull(ScheduleRules.validate(schedule(2, team = 2, start = "17:00", end = "18:00"), existing))
    }

    @Test
    fun `el error de horas tiene prioridad sobre los demas`() {
        val existing = listOf(schedule(1, team = 1), schedule(2, team = 2), schedule(3, team = 3))

        assertEquals(
            ScheduleRules.MSG_START_AFTER_END,
            ScheduleRules.validate(schedule(4, team = 4, start = "20:00", end = "19:00"), existing)
        )
    }
}