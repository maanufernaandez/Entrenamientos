package com.example.entrenamientos.logic

import com.example.entrenamientos.data.TrainingSchedule

object ScheduleRules {

    const val MAX_SCHEDULES_PER_DAY = 3

    const val MSG_START_AFTER_END = "La hora de inicio debe ser anterior a la de fin."
    const val MSG_TOO_MANY_PER_DAY =
        "No puede haber más de $MAX_SCHEDULES_PER_DAY entrenamientos programados el mismo día."

    fun parseMinutes(time: String): Int {
        val parts = time.split(":")

        if (parts.size != 2) return 0

        return parts[0]
            .toIntOrNull()
            ?.times(60)
            ?.plus(parts[1].toIntOrNull() ?: 0)
            ?: 0
    }

    fun validate(
        newSchedule: TrainingSchedule,
        existingSchedules: List<TrainingSchedule>
    ): String? {

        val newStart = parseMinutes(newSchedule.startTime)
        val newEnd = parseMinutes(newSchedule.endTime)

        if (newStart >= newEnd) return MSG_START_AFTER_END

        val others = existingSchedules.filter {
            it.dayOfWeek == newSchedule.dayOfWeek && it.id != newSchedule.id
        }

        others
            .filter { it.teamYear == newSchedule.teamYear }
            .forEach { schedule ->
                val existingStart = parseMinutes(schedule.startTime)
                val existingEnd = parseMinutes(schedule.endTime)

                if (maxOf(newStart, existingStart) < minOf(newEnd, existingEnd)) {
                    return "El horario se solapa con otro entrenamiento de este equipo " +
                            "(${schedule.startTime}-${schedule.endTime})."
                }
            }

        if (others.size >= MAX_SCHEDULES_PER_DAY) return MSG_TOO_MANY_PER_DAY

        return null
    }
}