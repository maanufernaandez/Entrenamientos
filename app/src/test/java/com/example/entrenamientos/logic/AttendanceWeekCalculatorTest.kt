package com.example.entrenamientos.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class AttendanceWeekCalculatorTest {

    // Lunes=1 ... Domingo=7 (java.time.DayOfWeek.value)
    private val MONDAY = DayOfWeek.MONDAY.value
    private val THURSDAY = DayOfWeek.THURSDAY.value
    private val FRIDAY = DayOfWeek.FRIDAY.value

    private val SEASON_END = LocalDate.of(2027, 5, 31)

    /**
     * Caso real que motivó estos tests: horario Lunes/Jueves/Viernes,
     * primer entrenamiento el jueves 10 de septiembre de 2026, "hoy" es
     * sábado 12 de septiembre de 2026.
     *
     * Solo debe aparecer la semana del 7 al 13 de septiembre (contiene el
     * jueves 10 y el viernes 11); ni la semana anterior (31 ago-6 sep, antes
     * del inicio de temporada) ni las siguientes (aún no ha llegado su lunes).
     */
    @Test
    fun `solo muestra la semana del primer entrenamiento, no la anterior ni las futuras`() {
        val firstTrainingDate = LocalDate.of(2026, 9, 10) // Jueves
        val today = LocalDate.of(2026, 9, 12) // Sábado

        val validWeeks = AttendanceWeekCalculator.calculateValidWeeks(
            attendanceDates = emptyList(),
            scheduleDaysOfWeek = listOf(MONDAY, THURSDAY, FRIDAY),
            holidayDates = emptySet(),
            firstTrainingDate = firstTrainingDate,
            lastTrainingDate = SEASON_END,
            today = today
        )

        assertEquals(setOf(LocalDate.of(2026, 9, 7)), validWeeks)
    }

    @Test
    fun `no muestra ninguna semana anterior al inicio de temporada aunque el horario coincida`() {
        val firstTrainingDate = LocalDate.of(2026, 9, 10) // Jueves
        val today = LocalDate.of(2026, 9, 10) // El mismo día del primer entrenamiento

        val validWeeks = AttendanceWeekCalculator.calculateValidWeeks(
            attendanceDates = emptyList(),
            // Un horario que también coincidiría en Sept 1 y Sept 3 (semana anterior)
            scheduleDaysOfWeek = listOf(DayOfWeek.TUESDAY.value, THURSDAY),
            holidayDates = emptySet(),
            firstTrainingDate = firstTrainingDate,
            lastTrainingDate = SEASON_END,
            today = today
        )

        assertFalse(
            "La semana del 31 de agosto no debería aparecer nunca",
            validWeeks.contains(LocalDate.of(2026, 8, 31))
        )
        assertTrue(validWeeks.contains(LocalDate.of(2026, 9, 7)))
    }

    @Test
    fun `una semana futura no aparece aunque tenga un dia de entrenamiento programado`() {
        val firstTrainingDate = LocalDate.of(2026, 9, 10)
        val today = LocalDate.of(2026, 9, 12) // Todavía dentro de la semana del 7-13

        val validWeeks = AttendanceWeekCalculator.calculateValidWeeks(
            attendanceDates = emptyList(),
            scheduleDaysOfWeek = listOf(MONDAY, THURSDAY, FRIDAY),
            holidayDates = emptySet(),
            firstTrainingDate = firstTrainingDate,
            lastTrainingDate = SEASON_END,
            today = today
        )

        assertFalse(validWeeks.contains(LocalDate.of(2026, 9, 14)))
        assertFalse(validWeeks.contains(LocalDate.of(2026, 9, 21)))
    }

    @Test
    fun `una semana futura SI aparece si ya se ha completado asistencia por adelantado`() {
        val firstTrainingDate = LocalDate.of(2026, 9, 10)
        val today = LocalDate.of(2026, 9, 12)

        val validWeeks = AttendanceWeekCalculator.calculateValidWeeks(
            // Asistencia ya guardada para el jueves 17 (semana del 14-20, futura)
            attendanceDates = listOf("2026-09-17"),
            scheduleDaysOfWeek = listOf(MONDAY, THURSDAY, FRIDAY),
            holidayDates = emptySet(),
            firstTrainingDate = firstTrainingDate,
            lastTrainingDate = SEASON_END,
            today = today
        )

        assertTrue(validWeeks.contains(LocalDate.of(2026, 9, 14)))
    }

    @Test
    fun `una asistencia suelta anterior al inicio de temporada no revive esa semana`() {
        val firstTrainingDate = LocalDate.of(2026, 9, 10)
        val today = LocalDate.of(2026, 9, 12)

        val validWeeks = AttendanceWeekCalculator.calculateValidWeeks(
            // Dato suelto/erróneo de una fecha anterior al inicio de temporada
            attendanceDates = listOf("2026-09-03"),
            scheduleDaysOfWeek = listOf(MONDAY, THURSDAY, FRIDAY),
            holidayDates = emptySet(),
            firstTrainingDate = firstTrainingDate,
            lastTrainingDate = SEASON_END,
            today = today
        )

        assertFalse(
            "Una asistencia fuera de temporada no debe hacer aparecer esa semana",
            validWeeks.contains(LocalDate.of(2026, 8, 31))
        )
    }

    @Test
    fun `una semana con todos sus dias de entrenamiento marcados como festivo no aparece`() {
        val firstTrainingDate = LocalDate.of(2026, 9, 10) // Jueves
        val today = LocalDate.of(2026, 9, 12)

        val validWeeks = AttendanceWeekCalculator.calculateValidWeeks(
            attendanceDates = emptyList(),
            scheduleDaysOfWeek = listOf(THURSDAY, FRIDAY),
            // Los dos únicos días de entrenamiento de esa semana son festivos
            holidayDates = setOf("2026-09-10", "2026-09-11"),
            firstTrainingDate = firstTrainingDate,
            lastTrainingDate = SEASON_END,
            today = today
        )

        assertFalse(validWeeks.contains(LocalDate.of(2026, 9, 7)))
    }

    @Test
    fun `no aparece ninguna semana posterior al fin de temporada`() {
        val firstTrainingDate = LocalDate.of(2026, 9, 10)
        val lastTrainingDate = LocalDate.of(2026, 9, 20)
        val today = LocalDate.of(2026, 10, 1) // Muy por delante del fin de temporada

        val validWeeks = AttendanceWeekCalculator.calculateValidWeeks(
            attendanceDates = emptyList(),
            scheduleDaysOfWeek = listOf(MONDAY, THURSDAY, FRIDAY),
            holidayDates = emptySet(),
            firstTrainingDate = firstTrainingDate,
            lastTrainingDate = lastTrainingDate,
            today = today
        )

        validWeeks.forEach { weekStart ->
            assertFalse(
                "Ninguna semana debería empezar después del fin de temporada",
                weekStart.isAfter(lastTrainingDate)
            )
        }
    }

    @Test
    fun `sin horario configurado no aparece ninguna semana sin asistencia`() {
        val validWeeks = AttendanceWeekCalculator.calculateValidWeeks(
            attendanceDates = emptyList(),
            scheduleDaysOfWeek = emptyList(),
            holidayDates = emptySet(),
            firstTrainingDate = LocalDate.of(2026, 9, 1),
            lastTrainingDate = SEASON_END,
            today = LocalDate.of(2026, 9, 12)
        )

        assertTrue(validWeeks.isEmpty())
    }
}