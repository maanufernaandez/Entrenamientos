package com.example.entrenamientos.logic

import com.example.entrenamientos.data.Team
import com.example.entrenamientos.data.TrainingSchedule
import java.time.LocalDate

object ConvocatoriaRules {

    fun firstMissingAttendance(
        matchDate: LocalDate,
        teamYear: Int,
        attendedDates: Set<String>,
        schedules: List<TrainingSchedule>,
        teams: List<Team>,
        holidayDates: Set<String>
    ): LocalDate? {

        val seasonStartYear =
            if (matchDate.monthValue >= 9) matchDate.year else matchDate.year - 1

        var day = LocalDate.of(seasonStartYear, 9, 1)

        while (day.isBefore(matchDate)) {

            val isTrainingDay = TrainingDayCalculator
                .teamYearsForDate(day, schedules, teams, holidayDates)
                .contains(teamYear)

            if (isTrainingDay && !attendedDates.contains(day.toString())) {
                return day
            }

            day = day.plusDays(1)
        }

        return null
    }
}