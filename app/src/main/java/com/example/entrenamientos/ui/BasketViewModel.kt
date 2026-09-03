package com.example.entrenamientos.ui

import androidx.lifecycle.ViewModel
import com.example.entrenamientos.data.Attendance
import com.example.entrenamientos.data.Holiday
import com.example.entrenamientos.data.Match
import com.example.entrenamientos.data.Player
import com.example.entrenamientos.data.Team
import com.example.entrenamientos.data.TrainingNote
import com.example.entrenamientos.data.TrainingSchedule
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class BasketViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : ViewModel() {

    // ============================================================
    // USUARIO ACTUAL
    // ============================================================

    private val currentUid: String?
        get() = auth.currentUser?.uid

    private val userDoc
        get() = currentUid?.let { uid ->
            db.collection("users").document(uid)
        }

    // ============================================================
    // ESTADOS
    // ============================================================

    private val _teams = MutableStateFlow<List<Team>>(emptyList())
    val teams: StateFlow<List<Team>> = _teams.asStateFlow()

    private val _schedules = MutableStateFlow<List<TrainingSchedule>>(emptyList())
    val schedules: StateFlow<List<TrainingSchedule>> = _schedules.asStateFlow()

    private val _matches = MutableStateFlow<List<Match>>(emptyList())
    val matches: StateFlow<List<Match>> = _matches.asStateFlow()

    private val _holidays = MutableStateFlow<List<Holiday>>(emptyList())
    val holidays: StateFlow<List<Holiday>> = _holidays.asStateFlow()

    private val _players = MutableStateFlow<List<Player>>(emptyList())
    private val _attendances = MutableStateFlow<List<Attendance>>(emptyList())
    private val _trainingNotes = MutableStateFlow<List<TrainingNote>>(emptyList())

    private val _selectedDate = MutableStateFlow("2026-09-01")
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _selectedTeamYear = MutableStateFlow(0)
    val selectedTeamYear: StateFlow<Int> = _selectedTeamYear.asStateFlow()

    init {
        setupFirebaseListeners()
    }

    // ============================================================
    // FIREBASE
    // ============================================================

    private fun setupFirebaseListeners() {
        val user = userDoc ?: return

        // EQUIPOS
        user.collection("teams").addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val list = snapshot.toObjects(Team::class.java)
                _teams.value = list

                if (
                    list.isNotEmpty() &&
                    _selectedTeamYear.value == 0
                ) {
                    _selectedTeamYear.value = list.first().year
                }
            }
        }

        // HORARIOS
        user.collection("schedules").addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            if (snapshot != null) {
                _schedules.value =
                    snapshot.toObjects(TrainingSchedule::class.java)
            }
        }

        // PARTIDOS
        user.collection("matches").addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            if (snapshot != null) {
                _matches.value =
                    snapshot.toObjects(Match::class.java)
            }
        }

        // FESTIVOS
        user.collection("holidays").addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val list = snapshot.toObjects(Holiday::class.java)

                if (list.isEmpty()) {
                    createDefaultHolidays()
                } else {
                    _holidays.value = list
                }
            }
        }

        // JUGADORES
        user.collection("players").addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            if (snapshot != null) {
                _players.value =
                    snapshot.toObjects(Player::class.java)
            }
        }

        // ASISTENCIAS
        user.collection("attendances").addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            if (snapshot != null) {
                _attendances.value =
                    snapshot.toObjects(Attendance::class.java)
            }
        }

        // NOTAS
        user.collection("training_notes").addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            if (snapshot != null) {
                _trainingNotes.value =
                    snapshot.toObjects(TrainingNote::class.java)
            }
        }
    }

    private fun createDefaultHolidays() {
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
            Holiday("2027-04-30")
        )

        defaultHolidays.forEach {
            insertHoliday(it)
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

    // ============================================================
    // EQUIPOS
    // ============================================================

    fun addTeam(
        name: String,
        shortName: String,
        gender: String,
        categoryYear: String,
        colorHex: String,
        trackMatches: Boolean
    ) {
        val user = userDoc ?: return

        val newId =
            if (_teams.value.isEmpty()) {
                1
            } else {
                _teams.value.maxOf { it.year } + 1
            }

        val newTeam = Team(
            year = newId,
            name = name,
            shortName = shortName,
            gender = gender,
            categoryYear = categoryYear,
            colorHex = colorHex,
            trackMatches = trackMatches
        )

        user.collection("teams")
            .document(newId.toString())
            .set(newTeam)

        _selectedTeamYear.value = newId
    }

    fun updateTeamData(
        teamId: Int,
        newName: String,
        shortName: String,
        gender: String,
        newCategoryYear: String,
        newColorHex: String,
        trackMatches: Boolean
    ) {
        val user = userDoc ?: return
        val existingTeam = _teams.value.find {
            it.year == teamId
        }

        val firstTrainingDate =
            existingTeam?.firstTrainingDate ?: "2026-09-01"

        val updatedTeam = Team(
            year = teamId,
            name = newName,
            shortName = shortName,
            gender = gender,
            categoryYear = newCategoryYear,
            colorHex = newColorHex,
            firstTrainingDate = firstTrainingDate,
            trackMatches = trackMatches
        )

        user.collection("teams")
            .document(teamId.toString())
            .set(updatedTeam)
    }

    fun deleteTeamCascade(team: Team) {
        val user = userDoc ?: return

        val teamYear = team.year
        val batch = db.batch()

        batch.delete(
            user.collection("teams")
                .document(teamYear.toString())
        )

        _players.value
            .filter { it.teamYear == teamYear }
            .forEach { player ->
                batch.delete(
                    user.collection("players")
                        .document(player.id.toString())
                )
            }

        _schedules.value
            .filter { it.teamYear == teamYear }
            .forEach { schedule ->
                batch.delete(
                    user.collection("schedules")
                        .document(schedule.id.toString())
                )
            }

        _matches.value
            .filter { it.teamYear == teamYear }
            .forEach { match ->
                batch.delete(
                    user.collection("matches")
                        .document(match.id.toString())
                )
            }

        _attendances.value
            .filter { it.teamYear == teamYear }
            .forEach { attendance ->
                batch.delete(
                    user.collection("attendances")
                        .document(
                            "${attendance.date}_${attendance.playerId}"
                        )
                )
            }

        _trainingNotes.value
            .filter { it.teamYear == teamYear }
            .forEach { note ->
                batch.delete(
                    user.collection("training_notes")
                        .document(
                            "${note.date}_${note.teamYear}_${note.noteType}"
                        )
                )
            }

        batch.commit().addOnSuccessListener {
            val remaining =
                _teams.value.filter {
                    it.year != teamYear
                }

            _selectedTeamYear.value =
                remaining.firstOrNull()?.year ?: 0
        }
    }

    // ============================================================
    // JUGADORES
    // ============================================================

    fun addPlayer(
        name: String,
        lastName: String,
        dorsal: String?,
        teamYear: Int
    ) {
        val user = userDoc ?: return

        val newId = System.currentTimeMillis()

        val player = Player(
            id = newId,
            name = name,
            lastName = lastName,
            teamYear = teamYear,
            dorsal = dorsal
        )

        user.collection("players")
            .document(newId.toString())
            .set(player)
    }

    fun updatePlayer(player: Player) {
        val user = userDoc ?: return

        user.collection("players")
            .document(player.id.toString())
            .set(player)
    }

    fun deletePlayer(player: Player) {
        val user = userDoc ?: return

        user.collection("players")
            .document(player.id.toString())
            .delete()
    }

    fun getPlayersForTeam(
        year: Int
    ): kotlinx.coroutines.flow.Flow<List<Player>> {
        return kotlinx.coroutines.flow.flow {
            _players.collect { allPlayers ->
                emit(
                    allPlayers.filter {
                        it.teamYear == year
                    }
                )
            }
        }
    }

    // ============================================================
    // NOTAS Y QUINTETOS
    // ============================================================

    fun saveTrainingNote(
        date: String,
        teamYear: Int,
        type: String,
        content: String,
        existingNote: TrainingNote? = null
    ) {
        val user = userDoc ?: return

        val noteToSave =
            existingNote?.copy(
                content = content
            )
                ?: TrainingNote(
                    date = date,
                    teamYear = teamYear,
                    noteType = type,
                    content = content
                )

        val docId = "${date}_${teamYear}_${type}"

        user.collection("training_notes")
            .document(docId)
            .set(noteToSave)
    }

    fun getTrainingNoteForDateAndTeam(
        date: String,
        year: Int,
        type: String
    ): kotlinx.coroutines.flow.Flow<TrainingNote?> {
        return kotlinx.coroutines.flow.flow {
            _trainingNotes.collect { notes ->
                emit(
                    notes.find {
                        it.date == date &&
                                it.teamYear == year &&
                                it.noteType == type
                    }
                )
            }
        }
    }

    // ============================================================
    // HORARIOS
    // ============================================================

    fun getTeamsForDate(
        date: java.time.LocalDate
    ): List<Int> {
        if (isHoliday(date)) {
            return emptyList()
        }

        val dayValue = date.dayOfWeek.value

        return _schedules.value
            .filter { schedule ->
                if (schedule.dayOfWeek != dayValue) {
                    return@filter false
                }

                val team =
                    _teams.value.find {
                        it.year == schedule.teamYear
                    }

                val firstDateStr =
                    team?.firstTrainingDate ?: "2026-09-01"

                val firstDate =
                    try {
                        java.time.LocalDate.parse(firstDateStr)
                    } catch (_: Exception) {
                        java.time.LocalDate.of(2026, 9, 1)
                    }

                !date.isBefore(firstDate)
            }
            .map { it.teamYear }
            .distinct()
    }

    fun updateTeamFirstTrainingDate(
        teamId: Int,
        newDate: String
    ) {
        val user = userDoc ?: return

        val team =
            _teams.value.find {
                it.year == teamId
            }

        if (team != null) {
            user.collection("teams")
                .document(teamId.toString())
                .set(
                    team.copy(
                        firstTrainingDate = newDate
                    )
                )
        }
    }

    fun deleteSchedule(schedule: TrainingSchedule) {
        val user = userDoc ?: return

        user.collection("schedules")
            .document(schedule.id.toString())
            .delete()
    }

    fun addOrUpdateSchedule(
        newSchedule: TrainingSchedule,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = userDoc

        if (user == null) {
            onError("No hay un usuario autenticado.")
            return
        }

        val daySchedules =
            _schedules.value.filter {
                it.teamYear == newSchedule.teamYear &&
                        it.dayOfWeek == newSchedule.dayOfWeek &&
                        it.id != newSchedule.id
            }

        val newStart =
            parseTime(newSchedule.startTime)

        val newEnd =
            parseTime(newSchedule.endTime)

        if (newStart >= newEnd) {
            onError(
                "La hora de inicio debe ser anterior a la de fin"
            )
            return
        }

        for (schedule in daySchedules) {
            val existStart =
                parseTime(schedule.startTime)

            val existEnd =
                parseTime(schedule.endTime)

            if (
                maxOf(newStart, existStart) <
                minOf(newEnd, existEnd)
            ) {
                onError(
                    "El horario se solapa con otro entrenamiento (${schedule.startTime}-${schedule.endTime})"
                )
                return
            }
        }

        val scheduleToSave =
            if (newSchedule.id == 0L) {
                newSchedule.copy(
                    id = System.currentTimeMillis()
                )
            } else {
                newSchedule
            }

        user.collection("schedules")
            .document(scheduleToSave.id.toString())
            .set(scheduleToSave)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onError(
                    it.message
                        ?: "No se pudo guardar el horario."
                )
            }
    }

    private fun parseTime(time: String): Int {
        val parts = time.split(":")

        if (parts.size != 2) {
            return 0
        }

        return parts[0].toIntOrNull()
            ?.times(60)
            ?.plus(parts[1].toIntOrNull() ?: 0)
            ?: 0
    }

    // ============================================================
    // ASISTENCIAS
    // ============================================================

    fun getAttendanceForDateAndTeam(
        date: String,
        year: Int
    ): kotlinx.coroutines.flow.Flow<List<Attendance>> {
        return kotlinx.coroutines.flow.flow {
            _attendances.collect { allAtt ->
                emit(
                    allAtt.filter {
                        it.date == date &&
                                it.teamYear == year
                    }
                )
            }
        }
    }

    fun saveAttendances(
        attendances: List<Attendance>
    ) {
        val user = userDoc ?: return

        val batch = db.batch()

        attendances.forEach { attendance ->
            val docRef =
                user.collection("attendances")
                    .document(
                        "${attendance.date}_${attendance.playerId}"
                    )

            batch.set(docRef, attendance)
        }

        batch.commit()
    }

    fun getAllAttendancesByTeam(
        year: Int
    ): kotlinx.coroutines.flow.Flow<List<Attendance>> {
        return kotlinx.coroutines.flow.flow {
            _attendances.collect { allAtt ->
                emit(
                    allAtt.filter {
                        it.teamYear == year
                    }
                )
            }
        }
    }

    // ============================================================
    // PARTIDOS
    // ============================================================

    fun addOrUpdateMatch(
        match: Match,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val user = userDoc

        if (user == null) {
            onError(
                "No hay ningún usuario autenticado."
            )
            return
        }

        // Comprobamos que no exista otro partido
        // del mismo equipo en la misma fecha.
        val existing =
            _matches.value.find {
                it.date == match.date &&
                        it.teamYear == match.teamYear &&
                        it.id != match.id
            }

        if (existing != null) {
            onError(
                "Este equipo ya tiene un partido programado para este día."
            )
            return
        }

        val matchToSave =
            if (match.id == 0L) {
                match.copy(
                    id = System.currentTimeMillis()
                )
            } else {
                match
            }

        // IMPORTANTE:
        // NO actualizamos _matches todavía.
        // Primero esperamos confirmación de Firebase.
        user.collection("matches")
            .document(matchToSave.id.toString())
            .set(matchToSave)
            .addOnSuccessListener {

                // Firebase ha confirmado el guardado.
                val currentMatches =
                    _matches.value

                _matches.value =
                    if (
                        currentMatches.any {
                            it.id == matchToSave.id
                        }
                    ) {
                        currentMatches.map {
                            if (it.id == matchToSave.id) {
                                matchToSave
                            } else {
                                it
                            }
                        }
                    } else {
                        currentMatches + matchToSave
                    }

                onSuccess()
            }
            .addOnFailureListener { error ->

                onError(
                    error.message
                        ?: "No se pudo guardar el partido en Firebase."
                )
            }
    }

    fun deleteMatch(match: Match) {
        val user = userDoc ?: return

        user.collection("matches")
            .document(match.id.toString())
            .delete()
            .addOnSuccessListener {

                _matches.value =
                    _matches.value.filter {
                        it.id != match.id
                    }
            }
    }

    // ============================================================
    // FESTIVOS
    // ============================================================

    fun isHoliday(
        date: java.time.LocalDate
    ): Boolean {
        return _holidays.value.any {
            it.date == date.toString()
        }
    }

    private fun insertHoliday(
        holiday: Holiday
    ) {
        val user = userDoc ?: return

        user.collection("holidays")
            .document(holiday.date)
            .set(holiday)
    }

    fun addHoliday(date: String) {
        insertHoliday(
            Holiday(date)
        )
    }

    fun removeHoliday(
        holiday: Holiday
    ) {
        val user = userDoc ?: return

        user.collection("holidays")
            .document(holiday.date)
            .delete()
    }

    // ============================================================
    // VALIDACIÓN CONVOCATORIA
    // ============================================================

    fun canMakeConvocatoria(
        matchDate: java.time.LocalDate,
        teamYear: Int,
        attendances: List<Attendance>
    ): Pair<Boolean, String?> {

        val seasonStartYear =
            if (matchDate.monthValue >= 9) {
                matchDate.year
            } else {
                matchDate.year - 1
            }

        val seasonStart =
            java.time.LocalDate.of(
                seasonStartYear,
                9,
                1
            )

        var currDate = seasonStart

        while (currDate.isBefore(matchDate)) {

            if (
                getTeamsForDate(currDate)
                    .contains(teamYear)
            ) {

                val hasAttendance =
                    attendances.any {
                        it.date == currDate.toString()
                    }

                if (!hasAttendance) {

                    val formatter =
                        java.time.format.DateTimeFormatter.ofPattern(
                            "dd/MM/yyyy"
                        )

                    return Pair(
                        false,
                        "Falta asistencia del ${currDate.format(formatter)}"
                    )
                }
            }

            currDate =
                currDate.plusDays(1)
        }

        return Pair(true, null)
    }

    // ============================================================
    // BORRADOR CONVOCATORIA
    // ============================================================

    var draftMatchDate: String? = null
    var draftTeamYear: Int? = null
    var draftSummonedIds: Set<Long>? = null
    var draftReasonsMap: Map<String, String>? = null
    var draftIsEditMode: Boolean? = null

    fun saveDraftConvocatoria(
        date: String,
        teamYear: Int,
        summoned: Set<Long>,
        reasons: Map<String, String>,
        isEdit: Boolean
    ) {
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
