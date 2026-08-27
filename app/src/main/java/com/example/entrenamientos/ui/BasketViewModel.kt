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

    // --- NUEVO ESTADO PARA VALIDACIÓN DE ASISTENCIAS ---
    private val _allAttendances2013 = MutableStateFlow<List<Attendance>>(emptyList())

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
                        Holiday("2026-10-12"),
                        Holiday("2026-10-21"),
                        Holiday("2026-10-30"),
                        Holiday("2026-11-02"),
                        Holiday("2026-11-30"),
                        Holiday("2026-12-03"),
                        Holiday("2026-12-04"),
                        Holiday("2026-12-07"),
                        Holiday("2026-12-08"),
                        Holiday("2026-12-22"),
                        Holiday("2026-12-23"),
                        Holiday("2026-12-24"),
                        Holiday("2026-12-25"),
                        Holiday("2026-12-26"),
                        Holiday("2026-12-27"),
                        Holiday("2026-12-28"),
                        Holiday("2026-12-29"),
                        Holiday("2026-12-30"),
                        Holiday("2026-12-31"),
                        Holiday("2027-01-01"),
                        Holiday("2027-01-02"),
                        Holiday("2027-01-03"),
                        Holiday("2027-01-04"),
                        Holiday("2027-01-05"),
                        Holiday("2027-01-06"),
                        Holiday("2027-01-07"),
                        Holiday("2027-01-08"),
                        Holiday("2027-02-08"),
                        Holiday("2027-02-09"),
                        Holiday("2027-03-19"),
                        Holiday("2027-03-25"),
                        Holiday("2027-03-26"),
                        Holiday("2027-03-27"),
                        Holiday("2027-03-28"),
                        Holiday("2027-03-29"),
                        Holiday("2027-03-30"),
                        Holiday("2027-03-31"),
                        Holiday("2027-04-01"),
                        Holiday("2027-04-02"),
                        Holiday("2027-04-30"),
                    )
                    defaultHolidays.forEach { repository.insertHoliday(it) }
                } else {
                    _holidays.value = list
                }
            }
        }

        viewModelScope.launch {
            repository.getAllAttendancesByTeam(2013).collect { list ->
                _allAttendances2013.value = list
            }
        }
    }

    private fun checkAndPopulateDefaultPlayers() {
        viewModelScope.launch {
            // Comprobamos los prebenjamines
            val currentPrebenjamines = repository.getPlayers(2018).first()
            if (currentPrebenjamines.isEmpty()) {
                val defaultPrebenjamines = listOf(
                    Player(name = "Jericó", lastName = "Rios", teamYear = 2018),
                    Player(name = "Andrea", lastName = "Arrizabalaga", teamYear = 2018),
                    Player(name = "Silvia", lastName = "Beriain", teamYear = 2018),
                    Player(name = "Daniela", lastName = "Urdanoz", teamYear = 2018),
                    Player(name = "Carlos", lastName = "Ibero", teamYear = 2018),
                    Player(name = "Jaqueline", lastName = "Echeverria", teamYear = 2018),
                    Player(name = "Emma", lastName = "Berango", teamYear = 2018),
                    Player(name = "Vega", lastName = "Sadaba", teamYear = 2018),
                    Player(name = "Lara", lastName = "Sadaba", teamYear = 2018),
                    Player(name = "Martina", lastName = "del Pozo", teamYear = 2018),
                    Player(name = "Izan", lastName = "Marin", teamYear = 2018)
                )
                defaultPrebenjamines.forEach { player ->
                    repository.addPlayer(player) // Corregido: addPlayer en lugar de insertPlayer
                }
            }

            // Comprobamos las infantiles
            val currentInfantiles = repository.getPlayers(2013).first()
            if (currentInfantiles.isEmpty()) {
                val defaultInfantiles = listOf(
                    Player(name = "Mayte", lastName = "Mayte", teamYear = 2013),
                    Player(name = "Martina", lastName = "Berrio", teamYear = 2013),
                    Player(name = "Maria", lastName = "Del Pozo", teamYear = 2013),
                    Player(name = "Nahia", lastName = "Altagracia", teamYear = 2013),
                    Player(name = "Leire", lastName = "Elarre", teamYear = 2013),
                    Player(name = "Tania", lastName = "Elcano", teamYear = 2013),
                    Player(name = "Aitana", lastName = "Hernandez", teamYear = 2013),
                    Player(name = "Wiktoria", lastName = "Konig", teamYear = 2013),
                    Player(name = "Saioa", lastName = "Lizarraga", teamYear = 2013),
                    Player(name = "Alba", lastName = "Rodriguez", teamYear = 2013),
                    Player(name = "Arianna", lastName = "Vieira", teamYear = 2013),
                    Player(name = "Paula", lastName = "Zamarreño", teamYear = 2013),
                    Player(name = "Salome", lastName = "Militino", teamYear = 2013),
                    Player(name = "Ainhoa", lastName = "Ainhoa", teamYear = 2013),
                    Player(name = "Miriam", lastName = "Miriam", teamYear = 2013)
                )
                defaultInfantiles.forEach { player ->
                    repository.addPlayer(player) // Corregido: addPlayer en lugar de insertPlayer
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
    fun addPlayer(name: String, lastName: String, teamYear: Int) {
        val player = Player(name = name, lastName = lastName, teamYear = teamYear)
        viewModelScope.launch {
            repository.addPlayer(player)
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

    fun isHoliday(date: java.time.LocalDate): Boolean {
        return _holidays.value.any { it.date == date.toString() }
    }

    fun addHoliday(date: String) {
        viewModelScope.launch { repository.insertHoliday(Holiday(date)) }
    }

    fun removeHoliday(holiday: Holiday) {
        viewModelScope.launch { repository.deleteHoliday(holiday) }
    }

    fun canMakeConvocatoria(matchDate: java.time.LocalDate): Pair<Boolean, String?> {
        val attendances = _allAttendances2013.value

        // Asumimos que la temporada empieza el 1 de septiembre
        val seasonStartYear = if (matchDate.monthValue >= 9) matchDate.year else matchDate.year - 1
        val seasonStart = java.time.LocalDate.of(seasonStartYear, 9, 1)

        var currDate = seasonStart
        while (currDate.isBefore(matchDate)) {
            // Comprobamos si las Infantiles (2013) tenían entrenamiento ese día (se salta los festivos automáticamente)
            if (getTeamsForDate(currDate).contains(2013)) {
                // Comprobamos si hay algún registro de asistencia ese día
                val hasAttendance = attendances.any { it.date == currDate.toString() }
                if (!hasAttendance) {
                    val formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
                    return Pair(false, "Falta asistencia del ${currDate.format(formatter)}")
                }
            }
            currDate = currDate.plusDays(1)
        }
        return Pair(true, null)
    }
}