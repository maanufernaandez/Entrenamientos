package com.example.entrenamientos.logic

import java.time.DayOfWeek
import java.time.LocalDate

object AttendanceWeekCalculator {

    fun calculateValidWeeks(
        attendanceDates: List<String>,
        scheduleDaysOfWeek: List<Int>,
        holidayDates: Set<String>,
        firstTrainingDate: LocalDate,
        lastTrainingDate: LocalDate,
        today: LocalDate
    ): Set<LocalDate> {

        fun hasRealTrainingInWeek(weekStart: LocalDate): Boolean {
            if (scheduleDaysOfWeek.isEmpty()) return false
            return (0..6).any { offset ->
                val day = weekStart.plusDays(offset.toLong())
                !day.isBefore(firstTrainingDate) &&
                        !day.isAfter(lastTrainingDate) &&
                        !holidayDates.contains(day.toString()) &&
                        scheduleDaysOfWeek.contains(day.dayOfWeek.value)
            }
        }

        val attendancesByWeek = attendanceDates
            .mapNotNull { dateStr ->
                try {
                    LocalDate.parse(dateStr).with(DayOfWeek.MONDAY)
                } catch (_: Exception) {
                    null
                }
            }
            .toSet()

        val currentWeek = today.with(DayOfWeek.MONDAY)

        val validWeeks = mutableSetOf<LocalDate>()
        validWeeks.addAll(attendancesByWeek)

        var tempWeek = firstTrainingDate.with(DayOfWeek.MONDAY)
        if (!tempWeek.isAfter(currentWeek)) {
            var limit = 0
            while (!tempWeek.isAfter(currentWeek) && limit < 200) {
                validWeeks.add(tempWeek)
                tempWeek = tempWeek.plusWeeks(1)
                limit++
            }
        }

        validWeeks.retainAll { weekStart ->
            val weekEndSunday = weekStart.plusDays(6)
            val weekWithinSeason = !weekEndSunday.isBefore(firstTrainingDate) && !weekStart.isAfter(lastTrainingDate)

            weekWithinSeason && (attendancesByWeek.contains(weekStart) || hasRealTrainingInWeek(weekStart))
        }

        return validWeeks
    }
}