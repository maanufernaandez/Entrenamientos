package com.example.entrenamientos.ui

import androidx.compose.ui.graphics.Color
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
    // --- ESTADOS Y MÉTODOS PARA PARTIDOS ---
    private val _matches = MutableStateFlow<List<Match>>(emptyList())
    val matches: StateFlow<List<Match>> = _matches.asStateFlow()

    init {
        checkAndPopulateDefaultPlayers()

        viewModelScope.launch {
            repository.getAllSchedules().collect { list: List<TrainingSchedule> ->
                if (list.isEmpty()) {
                    val defaultSchedules = listOf(
                        TrainingSchedule(teamYear = 2018, dayOfWeek = 2, startTime = "16:30", endTime = "17:45"),
                        TrainingSchedule(teamYear = 2018, dayOfWeek = 4, startTime = "16:30", endTime = "17:45"),
                        TrainingSchedule(teamYear = 2013, dayOfWeek = 1, startTime = "19:00", endTime = "20:15"),
                        TrainingSchedule(teamYear = 2013, dayOfWeek = 4, startTime = "19:00", endTime = "20:15"),
                        TrainingSchedule(teamYear = 2013, dayOfWeek = 5, startTime = "17:45", endTime = "19:00")
                    )
                    defaultSchedules.forEach { repository.insertSchedule(it) }
                } else {
                    _schedules.value = list
                }
            }
        }

        viewModelScope.launch {
            repository.getAllMatches().collect { list: List<Match> ->
                _matches.value = list
            }
        }

        viewModelScope.launch {
            repository.getAllHolidays().collect { list ->
                if (list.isEmpty()) {
                    val defaultHolidays = listOf(
                        Holiday("2026-10-12"), // Fiesta Nacional de España
                        Holiday("2026-11-01"), // Todos los Santos
                        Holiday("2026-12-06"), // Día de la Constitución
                        Holiday("2026-12-08"), // Inmaculada Concepción
                        Holiday("2026-12-24"), // Nochebuena
                        Holiday("2026-12-25"), // Navidad
                        Holiday("2026-12-31"), // Nochevieja
                        Holiday("2027-01-01"), // Año Nuevo
                        Holiday("2027-01-06"), // Reyes Magos
                        Holiday("2027-03-25"), // Jueves Santo (2027)
                        Holiday("2027-03-26"), // Viernes Santo (2027)
                        Holiday("2027-05-01")  // Día del Trabajador
                    )
                    defaultHolidays.forEach { repository.insertHoliday(it) }
                } else {
                    _holidays.value = list
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

    fun getTeamsForDate(date: java.time.LocalDate): List<Int> {
        if (isHoliday(date)) return emptyList()

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

    fun hasMatchOnDate(date: java.time.LocalDate): Boolean {
        return _matches.value.any { it.date == date.toString() }
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

    fun addOrUpdateMatch(match: Match) {
        viewModelScope.launch { repository.insertMatch(match) }
    }

    fun deleteMatch(match: Match) {
        viewModelScope.launch { repository.deleteMatch(match) }
    }

    fun getMatchForDate(date: java.time.LocalDate): Match? = _matches.value.find { it.date == date.toString() }

    fun getMatchColor(match: Match): Color {
        if (match.resultLocal == null || match.resultVisitor == null) {
            return com.example.entrenamientos.ui.theme.InfantilBlue
        }

        if (match.resultLocal == match.resultVisitor) {
            return Color.Gray
        }

        val hasWon = if (match.isLocal) {
            match.resultLocal > match.resultVisitor
        } else {
            match.resultVisitor > match.resultLocal
        }

        return if (hasWon) com.example.entrenamientos.ui.theme.SuccessGreen else Color.Red
    }

    fun getAllAttendancesByTeam(year: Int): kotlinx.coroutines.flow.Flow<List<Attendance>> {
        return repository.getAllAttendancesByTeam(year)
    }

    // --- ESTADOS Y MÉTODOS PARA FESTIVOS ---
    private val _holidays = MutableStateFlow<List<Holiday>>(emptyList())
    val holidays: StateFlow<List<Holiday>> = _holidays.asStateFlow()

    // 🔴 Añade esto dentro de tu bloque init { ... } existente:
    // viewModelScope.launch {
    //     repository.getAllHolidays().collect { list -> _holidays.value = list }
    // }

    fun isHoliday(date: java.time.LocalDate): Boolean {
        return _holidays.value.any { it.date == date.toString() }
    }

    fun addHoliday(date: String) {
        viewModelScope.launch { repository.insertHoliday(Holiday(date)) }
    }

    fun removeHoliday(holiday: Holiday) {
        viewModelScope.launch { repository.deleteHoliday(holiday) }
    }
}