package com.example.entrenamientos.ui

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.entrenamientos.data.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.first

@HiltViewModel
class BasketViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    // --- ESTADOS ---
    private val _teams = MutableStateFlow<List<Team>>(emptyList())
    val teams: StateFlow<List<Team>> = _teams.asStateFlow()

    private val _schedules = MutableStateFlow<List<TrainingSchedule>>(emptyList())
    val schedules: StateFlow<List<TrainingSchedule>> = _schedules.asStateFlow()

    private val _matches = MutableStateFlow<List<Match>>(emptyList())
    val matches: StateFlow<List<Match>> = _matches.asStateFlow()

    private val _holidays = MutableStateFlow<List<Holiday>>(emptyList())
    val holidays: StateFlow<List<Holiday>> = _holidays.asStateFlow()

    private val _currentTeamAttendances = MutableStateFlow<List<Attendance>>(emptyList())

    private val _selectedDate = MutableStateFlow("2026-09-01")
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _selectedTeamYear = MutableStateFlow(0)
    val selectedTeamYear: StateFlow<Int> = _selectedTeamYear.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllTeams().collect { list ->
                _teams.value = list
                if (list.isNotEmpty() && _selectedTeamYear.value == 0) {
                    _selectedTeamYear.value = list.first().year
                }
            }
        }

        viewModelScope.launch {
            repository.getAllSchedules().collect { list ->
                _schedules.value = list
            }
        }

        viewModelScope.launch {
            repository.getAllMatches().collect { list ->
                _matches.value = list
            }
        }

        viewModelScope.launch {
            repository.getAllHolidays().collect { list ->
                if (list.isEmpty()) {
                    val defaultHolidays = listOf(
                        Holiday("2026-10-12"), Holiday("2026-10-21"), Holiday("2026-10-30"),
                        Holiday("2026-11-02"), Holiday("2026-11-30"), Holiday("2026-12-03"),
                        Holiday("2026-12-04"), Holiday("2026-12-07"), Holiday("2026-12-08"),
                        Holiday("2026-12-22"), Holiday("2026-12-23"), Holiday("2026-12-24"),
                        Holiday("2026-12-25"), Holiday("2026-12-26"), Holiday("2026-12-27"),
                        Holiday("2026-12-28"), Holiday("2026-12-29"), Holiday("2026-12-30"),
                        Holiday("2026-12-31"), Holiday("2027-01-01"), Holiday("2027-01-02"),
                        Holiday("2027-01-03"), Holiday("2027-01-04"), Holiday("2027-01-05"),
                        Holiday("2027-01-06"), Holiday("2027-01-07"), Holiday("2027-01-08"),
                        Holiday("2027-02-08"), Holiday("2027-02-09"), Holiday("2027-03-19"),
                        Holiday("2027-03-25"), Holiday("2027-03-26"), Holiday("2027-03-27"),
                        Holiday("2027-03-28"), Holiday("2027-03-29"), Holiday("2027-03-30"),
                        Holiday("2027-03-31"), Holiday("2027-04-01"), Holiday("2027-04-02"),
                        Holiday("2027-04-30")
                    )
                    defaultHolidays.forEach { repository.insertHoliday(it) }
                } else {
                    _holidays.value = list
                }
            }
        }

        viewModelScope.launch {
            selectedTeamYear.collect { year ->
                if (year != 0) {
                    repository.getAllAttendancesByTeam(year).collect { list ->
                        _currentTeamAttendances.value = list
                    }
                }
            }
        }
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    fun setSelectedTeamYear(year: Int) {
        _selectedTeamYear.value = year
    }

    fun getNextAttendanceStatus(currentStatus: Int): Int {
        return (currentStatus + 1) % 3
    }

    // --- OPERACIONES CON EQUIPOS ---
    fun addTeam(name: String, shortName: String, gender: String, categoryYear: String, colorHex: String, trackMatches: Boolean) {
        viewModelScope.launch {
            val newId = if (_teams.value.isEmpty()) 1 else _teams.value.maxOf { it.year } + 1
            val newTeam = Team(
                year = newId,
                name = name,
                shortName = shortName,
                gender = gender,
                categoryYear = categoryYear,
                colorHex = colorHex,
                trackMatches = trackMatches
            )
            repository.insertTeam(newTeam)
            _selectedTeamYear.value = newId
        }
    }

    fun updateTeamData(teamId: Int, newName: String, shortName: String, gender: String, newCategoryYear: String, newColorHex: String, trackMatches: Boolean) {
        viewModelScope.launch {
            val existingTeam = _teams.value.find { it.year == teamId }
            val firstTrainingDate = existingTeam?.firstTrainingDate ?: "2026-09-01"

            repository.insertTeam(Team(
                year = teamId,
                name = newName,
                shortName = shortName,
                gender = gender,
                categoryYear = newCategoryYear,
                colorHex = newColorHex,
                firstTrainingDate = firstTrainingDate,
                trackMatches = trackMatches
            ))
        }
    }

    fun deleteTeamCascade(team: Team) {
        viewModelScope.launch {
            val teamYear = team.year
            val players = repository.getPlayers(teamYear).first()
            players.forEach { repository.deletePlayer(it) }

            val scheds = repository.getAllSchedules().first().filter { it.teamYear == teamYear }
            scheds.forEach { repository.deleteSchedule(it) }

            val matches = repository.getAllMatches().first().filter { it.teamYear == teamYear }
            matches.forEach { repository.deleteMatch(it) }

            repository.deleteTeam(team)

            val remainingTeams = repository.getAllTeams().first()
            if (remainingTeams.isNotEmpty()) {
                _selectedTeamYear.value = remainingTeams.first().year
            } else {
                _selectedTeamYear.value = 0
            }
        }
    }

    // --- OPERACIONES CON JUGADORES ---
    fun addPlayer(name: String, lastName: String, dorsal: String?, teamYear: Int) {
        val player = Player(name = name, lastName = lastName, teamYear = teamYear, dorsal = dorsal)
        viewModelScope.launch { repository.addPlayer(player) }
    }

    fun updatePlayer(player: Player) {
        viewModelScope.launch { repository.updatePlayer(player) }
    }

    fun deletePlayer(player: Player) {
        viewModelScope.launch { repository.deletePlayer(player) }
    }

    fun getPlayersForTeam(year: Int): kotlinx.coroutines.flow.Flow<List<Player>> {
        return repository.getPlayers(year)
    }

    // --- OPERACIONES CON NOTAS Y QUINTETOS ---
    fun saveTrainingNote(date: String, teamYear: Int, type: String, content: String, existingNote: TrainingNote? = null) {
        viewModelScope.launch {
            val noteToSave = existingNote?.copy(content = content)
                ?: TrainingNote(date = date, teamYear = teamYear, noteType = type, content = content)
            repository.saveTrainingNote(noteToSave)
        }
    }

    fun getTrainingNoteForDateAndTeam(date: String, year: Int, type: String): kotlinx.coroutines.flow.Flow<TrainingNote?> {
        return repository.getTrainingNotes(date, year).map { notes ->
            notes.find { it.noteType == type }
        }
    }

    // --- OPERACIONES CON HORARIOS ---
    fun getTeamsForDate(date: java.time.LocalDate): List<Int> {
        if (isHoliday(date)) return emptyList()
        val dayValue = date.dayOfWeek.value
        return _schedules.value.filter { schedule ->
            if (schedule.dayOfWeek != dayValue) return@filter false
            val team = _teams.value.find { it.year == schedule.teamYear }
            val firstDateStr = team?.firstTrainingDate ?: "2026-09-01"
            val firstDate = try { java.time.LocalDate.parse(firstDateStr) } catch (_: Exception) { java.time.LocalDate.of(2026, 9, 1) }
            !date.isBefore(firstDate)
        }.map { it.teamYear }.distinct()
    }

    fun updateTeamFirstTrainingDate(teamId: Int, newDate: String) {
        viewModelScope.launch {
            val team = _teams.value.find { it.year == teamId }
            if (team != null) {
                repository.insertTeam(team.copy(firstTrainingDate = newDate))
            }
        }
    }

    fun deleteSchedule(schedule: TrainingSchedule) {
        viewModelScope.launch { repository.deleteSchedule(schedule) }
    }

    fun addOrUpdateSchedule(newSchedule: TrainingSchedule, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val daySchedules = _schedules.value.filter {
            it.teamYear == newSchedule.teamYear &&
                    it.dayOfWeek == newSchedule.dayOfWeek &&
                    it.id != newSchedule.id
        }

        val newStart = parseTime(newSchedule.startTime)
        val newEnd = parseTime(newSchedule.endTime)

        if (newStart >= newEnd) {
            onError("La hora de inicio debe ser anterior a la de fin")
            return
        }

        for (schedule in daySchedules) {
            val existStart = parseTime(schedule.startTime)
            val existEnd = parseTime(schedule.endTime)

            if (maxOf(newStart, existStart) < minOf(newEnd, existEnd)) {
                onError("El horario se solapa con otro entrenamiento de este mismo equipo (${schedule.startTime}-${schedule.endTime})")
                return
            }
        }

        viewModelScope.launch {
            repository.insertSchedule(newSchedule)
            onSuccess()
        }
    }

    private fun parseTime(time: String): Int {
        val parts = time.split(":")
        if (parts.size != 2) return 0
        return parts[0].toIntOrNull()?.times(60)?.plus(parts[1].toIntOrNull() ?: 0) ?: 0
    }

    // --- OPERACIONES CON ASISTENCIAS ---
    fun getAttendanceForDateAndTeam(date: String, year: Int): kotlinx.coroutines.flow.Flow<List<Attendance>> {
        return repository.getAttendance(date, year)
    }

    fun saveAttendances(attendances: List<Attendance>) {
        viewModelScope.launch { repository.saveAttendances(attendances) }
    }

    fun getAllAttendancesByTeam(year: Int): kotlinx.coroutines.flow.Flow<List<Attendance>> {
        return repository.getAllAttendancesByTeam(year)
    }

    // --- OPERACIONES CON PARTIDOS ---
    fun addOrUpdateMatch(match: Match, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        val existing = _matches.value.find { it.date == match.date && it.teamYear == match.teamYear && it.id != match.id }
        if (existing != null) {
            onError("Este equipo ya tiene un partido programado para este día.")
            return
        }
        viewModelScope.launch {
            repository.insertMatch(match)
            onSuccess()
        }
    }

    fun deleteMatch(match: Match) {
        viewModelScope.launch { repository.deleteMatch(match) }
    }

    fun getMatchForDate(date: java.time.LocalDate): Match? = _matches.value.find { it.date == date.toString() }

    fun getMatchColor(match: Match): Color {
        val team = _teams.value.find { it.year == match.teamYear }
        val baseColor = team?.colorHex?.let {
            try { Color(android.graphics.Color.parseColor(it)) } catch (_: Exception) { Color.Gray }
        } ?: Color.Gray

        if (match.resultLocal == null || match.resultVisitor == null) {
            return baseColor
        }

        if (match.resultLocal == match.resultVisitor) return Color.Gray

        val hasWon = if (match.isLocal) match.resultLocal > match.resultVisitor else match.resultVisitor > match.resultLocal
        return if (hasWon) Color(0xFF4CAF50) else Color.Red
    }

    // --- OPERACIONES CON FESTIVOS ---
    fun isHoliday(date: java.time.LocalDate): Boolean {
        return _holidays.value.any { it.date == date.toString() }
    }

    fun addHoliday(date: String) {
        viewModelScope.launch { repository.insertHoliday(Holiday(date)) }
    }

    fun removeHoliday(holiday: Holiday) {
        viewModelScope.launch { repository.deleteHoliday(holiday) }
    }

    // --- VALIDACIÓN DE CONVOCATORIA ---
    fun canMakeConvocatoria(matchDate: java.time.LocalDate, teamYear: Int, attendances: List<Attendance>): Pair<Boolean, String?> {
        val seasonStartYear = if (matchDate.monthValue >= 9) matchDate.year else matchDate.year - 1
        val seasonStart = java.time.LocalDate.of(seasonStartYear, 9, 1)

        var currDate = seasonStart
        while (currDate.isBefore(matchDate)) {
            if (getTeamsForDate(currDate).contains(teamYear)) {
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

    // --- BORRADOR DE CONVOCATORIA ---
    var draftMatchDate: String? = null
    var draftTeamYear: Int? = null
    var draftSummonedIds: Set<Long>? = null
    var draftReasonsMap: Map<Long, String>? = null
    var draftIsEditMode: Boolean? = null

    fun saveDraftConvocatoria(date: String, teamYear: Int, summoned: Set<Long>, reasons: Map<Long, String>, isEdit: Boolean) {
        draftMatchDate = date
        draftTeamYear = teamYear
        draftSummonedIds = summoned
        draftReasonsMap = reasons
        draftIsEditMode = isEdit
    }

    fun clearDraftConvocatoria() {
        draftMatchDate = null
        draftTeamYear = null
        draftSummonedIds = null
        draftReasonsMap = null
        draftIsEditMode = null
    }
}