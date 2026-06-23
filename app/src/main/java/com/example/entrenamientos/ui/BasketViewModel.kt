package com.example.entrenamientos.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.entrenamientos.data.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@HiltViewModel
class BasketViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    // --- ESTADOS Y MÉTODOS PARA HORARIOS DE ENTRENAMIENTO ---
    private val _schedules = MutableStateFlow<List<TrainingSchedule>>(emptyList())
    val schedules: StateFlow<List<TrainingSchedule>> = _schedules.asStateFlow()

    init {
        checkAndPopulateDefaultPlayers()

        // Cargar horarios predeterminados si la base de datos está vacía y escuchar cambios
        viewModelScope.launch {
            repository.getAllSchedules().collect { list ->
                if (list.isEmpty()) {
                    val defaultSchedules = listOf(
                        TrainingSchedule(teamYear = 2018, dayOfWeek = 2, startTime = "17:00", endTime = "18:00"), // Martes
                        TrainingSchedule(teamYear = 2018, dayOfWeek = 4, startTime = "17:00", endTime = "18:00"), // Jueves
                        TrainingSchedule(teamYear = 2013, dayOfWeek = 1, startTime = "18:00", endTime = "19:30"), // Lunes
                        TrainingSchedule(teamYear = 2013, dayOfWeek = 4, startTime = "18:00", endTime = "19:30"), // Jueves
                        TrainingSchedule(teamYear = 2013, dayOfWeek = 5, startTime = "18:00", endTime = "19:30")  // Viernes
                    )
                    defaultSchedules.forEach { repository.insertSchedule(it) }
                } else {
                    _schedules.value = list
                }
            }
        }
    }

    private fun checkAndPopulateDefaultPlayers() {
        viewModelScope.launch {
            // Comprobamos los prebenjamines
            val currentPrebenjamines = repository.getPlayers(2018).first()
            if (currentPrebenjamines.isEmpty()) {
                val defaultPrebenjamines = listOf(
                    "Jericó", "Andrea", "Silvia", "Daniela", "Carlos", "Jaqueline",
                    "Emma", "Vega", "Lara", "Crismeily", "Martina", "Izan"
                )
                defaultPrebenjamines.forEach { name ->
                    repository.addPlayer(Player(name = name, teamYear = 2018))
                }
            }

            // Comprobamos las infantiles
            val currentInfantiles = repository.getPlayers(2013).first()
            if (currentInfantiles.isEmpty()) {
                val defaultInfantiles = listOf(
                    "Mayte", "Martina", "Maria", "Nahia", "Leire", "Tania",
                    "Aitana", "Wiktoria", "Saioa", "Alba", "Arianna", "Paula", "Salome"
                )
                defaultInfantiles.forEach { name ->
                    repository.addPlayer(Player(name = name, teamYear = 2013))
                }
            }
        }
    }

    // Estado básico para la fecha seleccionada en el calendario
    private val _selectedDate = MutableStateFlow("2026-09-01")
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    // Lógica del ciclo de asistencia (Verde: 0 -> Amarillo: 1 -> Rojo: 2 -> Verde: 0)
    fun getNextAttendanceStatus(currentStatus: Int): Int {
        return (currentStatus + 1) % 3
    }

    // Funciones para interactuar con la Base de Datos (Jugadoras)
    fun addPlayer(name: String, teamYear: Int) {
        viewModelScope.launch {
            repository.addPlayer(Player(name = name, teamYear = teamYear))
        }
    }

    fun updatePlayer(player: Player) {
        viewModelScope.launch {
            repository.updatePlayer(player)
        }
    }

    fun deletePlayer(player: Player) {
        viewModelScope.launch {
            repository.deletePlayer(player)
        }
    }

    // Funciones para interactuar con la Base de Datos (Notas)
    fun saveTrainingNote(date: String, teamYear: Int, type: String, content: String) {
        viewModelScope.launch {
            repository.saveTrainingNote(TrainingNote(date = date, teamYear = teamYear, noteType = type, content = content))
        }
    }

    fun getTrainingNoteForDateAndTeam(date: String, year: Int, type: String): kotlinx.coroutines.flow.Flow<TrainingNote?> {
        return repository.getTrainingNotes(date, year).map { notes ->
            notes.find { it.noteType == type }
        }
    }

    // AHORA es dinámico y lee de la base de datos (_schedules) en lugar de un objeto fijo
    fun getTeamsForDate(date: java.time.LocalDate): List<Int> {
        val dayValue = date.dayOfWeek.value
        return _schedules.value.filter { it.dayOfWeek == dayValue }.map { it.teamYear }.distinct()
    }

    // Funciones para interactuar con la Base de Datos (Horarios)
    fun deleteSchedule(schedule: TrainingSchedule) {
        viewModelScope.launch { repository.deleteSchedule(schedule) }
    }

    fun addOrUpdateSchedule(newSchedule: TrainingSchedule, onSuccess: () -> Unit, onError: (String) -> Unit) {
        // Validación de solapamientos con otros equipos en el mismo día
        val daySchedules = _schedules.value.filter { it.dayOfWeek == newSchedule.dayOfWeek && it.id != newSchedule.id }

        val newStart = parseTime(newSchedule.startTime)
        val newEnd = parseTime(newSchedule.endTime)

        if (newStart >= newEnd) {
            onError("La hora de inicio debe ser anterior a la de fin")
            return
        }

        for (schedule in daySchedules) {
            val existStart = parseTime(schedule.startTime)
            val existEnd = parseTime(schedule.endTime)

            // Fórmula de solapamiento: Max(start1, start2) < Min(end1, end2)
            if (maxOf(newStart, existStart) < minOf(newEnd, existEnd)) {
                onError("Se solapa con el equipo ${schedule.teamYear} (${schedule.startTime}-${schedule.endTime})")
                return
            }
        }

        viewModelScope.launch {
            repository.insertSchedule(newSchedule)
            onSuccess()
        }
    }

    // Convierte "17:30" a 1050 (minutos totales del día) para hacer matemáticas con las horas
    private fun parseTime(time: String): Int {
        val parts = time.split(":")
        if (parts.size != 2) return 0
        return parts[0].toIntOrNull()?.times(60)?.plus(parts[1].toIntOrNull() ?: 0) ?: 0
    }

    // Devuelve si hay partido en esa fecha para pintar el icono azul
    fun hasMatchOnDate(date: java.time.LocalDate): Boolean {
        return com.example.entrenamientos.domain.CalendarLogic.isInfantilMatchDay(date)
    }

    // --- ESTADOS Y MÉTODOS PARA ASISTENCIA ---
    private val _selectedTeamYear = MutableStateFlow(2018)
    val selectedTeamYear: StateFlow<Int> = _selectedTeamYear.asStateFlow()

    fun setSelectedTeamYear(year: Int) {
        _selectedTeamYear.value = year
    }

    fun getPlayersForTeam(year: Int): kotlinx.coroutines.flow.Flow<List<Player>> {
        return repository.getPlayers(year)
    }

    fun getAttendanceForDateAndTeam(date: String, year: Int): kotlinx.coroutines.flow.Flow<List<Attendance>> {
        return repository.getAttendance(date, year)
    }

    fun saveAttendances(attendances: List<Attendance>) {
        viewModelScope.launch {
            repository.saveAttendances(attendances)
        }
    }

    fun saveConvocatoria(date: String, selectedPlayerIds: Set<Long>, reasons: Map<Long, String>) {
        println("Convocatoria guardada para $date: $selectedPlayerIds")
        println("Motivos de ausencia: $reasons")
    }
}