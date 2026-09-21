package com.example.entrenamientos.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DefaultHolidaysTest {

    private val dates = DefaultHolidays.DATES_2026_27

    @Test
    fun `todas las fechas son validas`() {
        dates.forEach { date ->
            LocalDate.parse(date) // lanza excepción si el formato es incorrecto
        }
    }

    @Test
    fun `no hay fechas repetidas y estan ordenadas`() {
        assertEquals(dates.size, dates.toSet().size)
        assertEquals(dates.sorted(), dates)
    }

    @Test
    fun `todas caen dentro del curso 2026-27`() {
        val start = LocalDate.of(2026, 9, 1)
        val end = LocalDate.of(2027, 6, 30)

        dates.forEach { date ->
            val day = LocalDate.parse(date)
            assertTrue("$date fuera del curso", !day.isBefore(start) && !day.isAfter(end))
        }
    }

    @Test
    fun `holidays devuelve un festivo por fecha`() {
        assertEquals(dates, DefaultHolidays.holidays().map { it.date })
    }
}