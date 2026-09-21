package com.example.entrenamientos.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.entrenamientos.data.Attendance
import com.example.entrenamientos.data.Holiday
import com.example.entrenamientos.data.Match
import com.example.entrenamientos.data.Player
import com.example.entrenamientos.data.Team
import com.example.entrenamientos.data.TrainingNote
import com.example.entrenamientos.data.TrainingSchedule
import com.example.entrenamientos.logic.ConvocatoriaRules
import com.example.entrenamientos.logic.DefaultHolidays
import com.example.entrenamientos.logic.ScheduleRules
import com.example.entrenamientos.logic.TrainingDayCalculator
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@HiltViewModel
class BasketViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : ViewModel() {

    companion object {
        private const val TAG = "BasketViewModel"

        // Firestore admite como máximo 500 operaciones por batch.
        // Dejamos margen por seguridad.
        private const val BATCH_LIMIT = 400
    }

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
    // ESTADO DEL PERFIL DE USUARIO
    // ============================================================
    private val _userProfile = MutableStateFlow<Map<String, String>>(emptyMap())
    val userProfile: StateFlow<Map<String, String>> = _userProfile.asStateFlow()

    // ============================================================
    // ESTADOS
    // ============================================================

    private val _teams =
        MutableStateFlow<List<Team>>(emptyList())
    val teams: StateFlow<List<Team>> =
        _teams.asStateFlow()

    private val _schedules =
        MutableStateFlow<List<TrainingSchedule>>(emptyList())
    val schedules: StateFlow<List<TrainingSchedule>> =
        _schedules.asStateFlow()

    private val _matches =
        MutableStateFlow<List<Match>>(emptyList())
    val matches: StateFlow<List<Match>> =
        _matches.asStateFlow()

    private val _holidays =
        MutableStateFlow<List<Holiday>>(emptyList())
    val holidays: StateFlow<List<Holiday>> =
        _holidays.asStateFlow()

    private val _players =
        MutableStateFlow<List<Player>>(emptyList())
    val players: StateFlow<List<Player>> =
        _players.asStateFlow()

    private val _attendances =
        MutableStateFlow<List<Attendance>>(emptyList())
    val attendances: StateFlow<List<Attendance>> =
        _attendances.asStateFlow()

    private val _trainingNotes =
        MutableStateFlow<List<TrainingNote>>(emptyList())
    val trainingNotes: StateFlow<List<TrainingNote>> =
        _trainingNotes.asStateFlow()

    // Por defecto, hoy: así el calendario abre en el mes actual.
    private val _selectedDate =
        MutableStateFlow(java.time.LocalDate.now().toString())
    val selectedDate: StateFlow<String> =
        _selectedDate.asStateFlow()

    private val _selectedTeamYear =
        MutableStateFlow(0)
    val selectedTeamYear: StateFlow<Int> =
        _selectedTeamYear.asStateFlow()

    // ============================================================
    // LISTENERS FIREBASE
    // ============================================================

    private val firebaseListeners =
        mutableListOf<ListenerRegistration>()

    private var listeningUid: String? = null

    private val authStateListener =
        FirebaseAuth.AuthStateListener { firebaseAuth ->

            val uid =
                firebaseAuth.currentUser?.uid

            if (uid == listeningUid) {
                return@AuthStateListener
            }

            removeFirebaseListeners()

            if (uid == null) {
                clearLocalData()
            } else {
                startFirebaseListeners(uid)
            }
        }

    init {
        // La persistencia offline de Firestore ya viene activada por defecto en
        // Android, así que no hace falta tocar firestoreSettings (además, cambiarlos
        // después de haber usado Firestore lanza IllegalStateException).
        auth.addAuthStateListener(authStateListener)
    }

    // ============================================================
    // FIREBASE - LISTENERS
    // ============================================================

    private fun startFirebaseListeners(uid: String) {
        listeningUid = uid
        val user = db.collection("users").document(uid)

        // ========================================================
        // PERFIL DE USUARIO
        // ========================================================
        firebaseListeners +=
            user.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error leyendo perfil", error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val data = snapshot.data
                    val name = data?.get("name") as? String ?: ""
                    val lastName = data?.get("lastName") as? String ?: ""
                    val club = data?.get("club") as? String ?: ""

                    _userProfile.value = mapOf(
                        "name" to name,
                        "lastName" to lastName,
                        "club" to club
                    )
                }
            }

        // ========================================================
        // EQUIPOS
        // ========================================================
        firebaseListeners +=
            user.collection("teams")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error leyendo equipos", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val list = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(Team::class.java)
                        }
                        _teams.value = list
                        if (list.isNotEmpty() && _selectedTeamYear.value == 0) {
                            _selectedTeamYear.value = list.first().year
                        }
                    }
                }

        // ========================================================
        // HORARIOS
        // ========================================================
        firebaseListeners +=
            user.collection("schedules")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error leyendo horarios", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        _schedules.value = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(TrainingSchedule::class.java)
                        }
                    }
                }

        // ========================================================
        // PARTIDOS
        // ========================================================
        firebaseListeners +=
            user.collection("matches")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error leyendo partidos", error)
                        return@addSnapshotListener
                    }
                    if (snapshot == null) return@addSnapshotListener

                    val loadedMatches = snapshot.documents.mapNotNull { doc ->
                        val parsed = doc.toObject(Match::class.java) ?: return@mapNotNull null
                        val documentId = doc.id.toLongOrNull()
                        val resolvedId = documentId ?: parsed.id

                        val data = doc.data
                        val hasSavedField = data?.containsKey("isConvocatoriaSaved") == true

                        val normalized = if (!hasSavedField && parsed.summonedPlayers.isNotEmpty()) {
                            parsed.copy(id = resolvedId, isConvocatoriaSaved = true)
                        } else {
                            parsed.copy(id = resolvedId)
                        }
                        normalized
                    }
                    _matches.value = loadedMatches
                }

        // ========================================================
        // FESTIVOS
        // ========================================================
        firebaseListeners +=
            user.collection("holidays")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error leyendo festivos", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val list = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(Holiday::class.java)
                        }
                        if (list.isEmpty()) {
                            createDefaultHolidays(uid)
                        } else {
                            _holidays.value = list
                        }
                    }
                }

        // ========================================================
        // JUGADORES
        // ========================================================
        firebaseListeners +=
            user.collection("players")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error leyendo jugadores", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        _players.value = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(Player::class.java)
                        }
                    }
                }

        // ========================================================
        // ASISTENCIAS
        // ========================================================
        firebaseListeners +=
            user.collection("attendances")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error leyendo asistencias", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        _attendances.value = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(Attendance::class.java)
                        }
                    }
                }

        // ========================================================
        // NOTAS / QUINTETOS
        // ========================================================
        firebaseListeners +=
            user.collection("training_notes")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error leyendo notas", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        _trainingNotes.value = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(TrainingNote::class.java)
                        }
                    }
                }
    }

    private fun removeFirebaseListeners() {

        firebaseListeners.forEach {
            it.remove()
        }

        firebaseListeners.clear()

        listeningUid = null
    }

    private fun clearLocalData() {
        _teams.value = emptyList()
        _schedules.value = emptyList()
        _matches.value = emptyList()
        _holidays.value = emptyList()
        _players.value = emptyList()
        _attendances.value = emptyList()
        _trainingNotes.value = emptyList()
        _userProfile.value = emptyMap() // Limpiar perfil

        _selectedTeamYear.value = 0
    }

    // ============================================================
    // FESTIVOS POR DEFECTO
    // ============================================================

    private fun createDefaultHolidays(
        uid: String
    ) {

        val user =
            db.collection("users")
                .document(uid)

        val defaultHolidays =
            DefaultHolidays.holidays()

        val batch =
            db.batch()

        defaultHolidays.forEach { holiday ->

            batch.set(
                user.collection("holidays")
                    .document(holiday.date),
                holiday
            )
        }

        batch.commit()
            .addOnFailureListener { error ->

                Log.e(
                    TAG,
                    "Error creando festivos",
                    error
                )
            }
    }

    // ============================================================
    // SELECCIÓN
    // ============================================================

    fun setSelectedDate(
        date: String
    ) {
        _selectedDate.value = date
    }

    fun setSelectedTeamYear(
        year: Int
    ) {
        _selectedTeamYear.value = year
    }

    fun getNextAttendanceStatus(
        currentStatus: Int
    ): Int {
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

        val user =
            userDoc ?: return

        val newId =
            if (_teams.value.isEmpty()) {
                1
            } else {
                _teams.value.maxOf {
                    it.year
                } + 1
            }

        val newTeam =
            Team(
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
            .addOnSuccessListener {

                _teams.value =
                    _teams.value
                        .filter {
                            it.year != newId
                        } + newTeam

                _selectedTeamYear.value =
                    newId
            }
            .addOnFailureListener { error ->

                Log.e(
                    TAG,
                    "Error guardando equipo",
                    error
                )
            }
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

        val user =
            userDoc ?: return

        val existingTeam =
            _teams.value.find {
                it.year == teamId
            }

        // Partimos del equipo existente para no perder los campos que este
        // método no edita (firstTrainingDate y lastTrainingDate).
        val updatedTeam =
            (existingTeam ?: Team(year = teamId)).copy(
                name = newName,
                shortName = shortName,
                gender = gender,
                categoryYear = newCategoryYear,
                colorHex = newColorHex,
                trackMatches = trackMatches
            )

        user.collection("teams")
            .document(teamId.toString())
            .set(updatedTeam)
            .addOnFailureListener { error ->

                Log.e(
                    TAG,
                    "Error actualizando equipo",
                    error
                )
            }
    }

    /**
     * Borra los documentos indicados en batches de [BATCH_LIMIT] operaciones
     * (Firestore rechaza batches de más de 500). Llama a [onSuccess] solo si
     * TODOS los batches se confirman; si alguno falla llama a [onFailure].
     */
    private fun deleteDocumentsInChunks(
        refs: List<DocumentReference>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (refs.isEmpty()) {
            onSuccess()
            return
        }

        val commits = refs
            .chunked(BATCH_LIMIT)
            .map { chunk ->
                val batch = db.batch()
                chunk.forEach { ref -> batch.delete(ref) }
                batch.commit()
            }

        Tasks.whenAll(commits)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { error -> onFailure(error) }
    }

    fun deleteTeamCascade(
        team: Team
    ) {

        val user =
            userDoc ?: return

        val teamYear =
            team.year

        val refs =
            mutableListOf<DocumentReference>()

        refs += user.collection("teams")
            .document(teamYear.toString())

        _players.value
            .filter { it.teamYear == teamYear }
            .forEach { player ->
                refs += user.collection("players")
                    .document(player.id.toString())
            }

        _schedules.value
            .filter { it.teamYear == teamYear }
            .forEach { schedule ->
                refs += user.collection("schedules")
                    .document(schedule.id.toString())
            }

        _matches.value
            .filter { it.teamYear == teamYear }
            .forEach { match ->
                refs += user.collection("matches")
                    .document(match.id.toString())
            }

        _attendances.value
            .filter { it.teamYear == teamYear }
            .forEach { attendance ->
                refs += user.collection("attendances")
                    .document("${attendance.date}_${attendance.playerId}")
            }

        _trainingNotes.value
            .filter { it.teamYear == teamYear }
            .forEach { note ->
                refs += user.collection("training_notes")
                    .document("${note.date}_${note.teamYear}_${note.noteType}")
            }

        deleteDocumentsInChunks(
            refs = refs,
            onSuccess = {

                _teams.value =
                    _teams.value.filter { it.year != teamYear }

                _players.value =
                    _players.value.filter { it.teamYear != teamYear }

                _schedules.value =
                    _schedules.value.filter { it.teamYear != teamYear }

                _matches.value =
                    _matches.value.filter { it.teamYear != teamYear }

                _attendances.value =
                    _attendances.value.filter { it.teamYear != teamYear }

                _trainingNotes.value =
                    _trainingNotes.value.filter { it.teamYear != teamYear }

                _selectedTeamYear.value =
                    _teams.value
                        .firstOrNull()
                        ?.year
                        ?: 0
            },
            onFailure = { error ->
                Log.e(TAG, "Error eliminando equipo", error)
            }
        )
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

        val user =
            userDoc ?: return

        // UUID en vez de System.currentTimeMillis(): dos altas muy seguidas
        // (doble toque, lag de red) ya no pueden compartir el mismo ID y
        // sobrescribirse una a la otra.
        val newId =
            java.util.UUID.randomUUID().mostSignificantBits

        val player =
            Player(
                id = newId,
                name = name,
                lastName = lastName,
                teamYear = teamYear,
                dorsal = dorsal
            )

        user.collection("players")
            .document(newId.toString())
            .set(player)
            .addOnSuccessListener {

                _players.value =
                    _players.value
                        .filter {
                            it.id != newId
                        } + player
            }
            .addOnFailureListener { error ->

                Log.e(
                    TAG,
                    "Error guardando jugador",
                    error
                )
            }
    }

    fun updatePlayer(
        player: Player
    ) {

        val user =
            userDoc ?: return

        user.collection("players")
            .document(player.id.toString())
            .set(player)
            .addOnSuccessListener {

                _players.value =
                    _players.value.map {
                        if (it.id == player.id) {
                            player
                        } else {
                            it
                        }
                    }
            }
            .addOnFailureListener { error ->

                Log.e(
                    TAG,
                    "Error actualizando jugador",
                    error
                )
            }
    }

    fun deletePlayer(
        player: Player
    ) {

        val user =
            userDoc ?: return

        val refs =
            mutableListOf<DocumentReference>()

        refs += user.collection("players")
            .document(player.id.toString())

        // Las asistencias del jugador se borran con él para no dejar
        // documentos huérfanos en Firestore.
        _attendances.value
            .filter { it.playerId == player.id }
            .forEach { attendance ->
                refs += user.collection("attendances")
                    .document("${attendance.date}_${attendance.playerId}")
            }

        deleteDocumentsInChunks(
            refs = refs,
            onSuccess = {

                _players.value =
                    _players.value.filter { it.id != player.id }

                _attendances.value =
                    _attendances.value.filter { it.playerId != player.id }

                removePlayerFromMatches(player)
            },
            onFailure = { error ->
                Log.e(TAG, "Error eliminando jugador", error)
            }
        )
    }

    /**
     * Quita al jugador borrado de las convocatorias (convocados y desconvocados)
     * de los partidos de su equipo, para que no cuente como "jugador fantasma".
     */
    private fun removePlayerFromMatches(
        player: Player
    ) {

        val user =
            userDoc ?: return

        val reasonKey =
            player.id.toString()

        _matches.value
            .filter { match ->
                match.teamYear == player.teamYear &&
                        (
                                match.summonedPlayers.contains(player.id) ||
                                        match.unsummonedReasons.containsKey(reasonKey)
                                )
            }
            .forEach { match ->

                val cleaned =
                    match.copy(
                        summonedPlayers = match.summonedPlayers - player.id,
                        unsummonedReasons = match.unsummonedReasons - reasonKey
                    )

                _matches.value =
                    _matches.value.map {
                        if (it.id == cleaned.id) cleaned else it
                    }

                user.collection("matches")
                    .document(cleaned.id.toString())
                    .set(cleaned)
                    .addOnFailureListener { error ->
                        Log.e(TAG, "Error limpiando la convocatoria", error)
                    }
            }
    }

    fun getPlayersForTeam(
        year: Int
    ): Flow<List<Player>> {

        return flow {

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
    // NOTAS / QUINTETOS
    // ============================================================

    fun saveTrainingNote(
        date: String,
        teamYear: Int,
        type: String,
        content: String,
        existingNote: TrainingNote? = null
    ) {

        val user =
            userDoc ?: return

        val noteToSave =
            existingNote?.copy(
                date = date,
                teamYear = teamYear,
                noteType = type,
                content = content
            )
                ?: TrainingNote(
                    date = date,
                    teamYear = teamYear,
                    noteType = type,
                    content = content
                )

        val docId =
            "${date}_${teamYear}_${type}"

        user.collection("training_notes")
            .document(docId)
            .set(noteToSave)
            .addOnSuccessListener {

                _trainingNotes.value =
                    _trainingNotes.value
                        .filterNot {
                            it.date == date &&
                                    it.teamYear == teamYear &&
                                    it.noteType == type
                        } + noteToSave
            }
            .addOnFailureListener { error ->

                Log.e(
                    TAG,
                    "Error guardando TrainingNote",
                    error
                )
            }
    }

    // Guarda (o borra, pasando photoPath = null) la ruta de la foto asociada
    // a una nota de entrenamiento, sin tocar el texto (content) que ya tuviera.
    fun updateTrainingNotePhoto(
        date: String,
        teamYear: Int,
        type: String,
        photoBase64: String?,
        existingNote: TrainingNote?,
        onError: (String) -> Unit = {}
    ) {

        val user =
            userDoc ?: return

        // Margen de seguridad respecto al límite de 1 MB por documento de
        // Firestore (dejamos hueco para el resto de campos del documento).
        if (photoBase64 != null && photoBase64.length > 900_000) {
            onError("La foto es demasiado grande para guardarla. Vuelve a intentarlo.")
            return
        }

        val noteToSave =
            existingNote?.copy(photoBase64 = photoBase64)
                ?: TrainingNote(
                    date = date,
                    teamYear = teamYear,
                    noteType = type,
                    photoBase64 = photoBase64
                )

        val docId =
            "${date}_${teamYear}_${type}"

        user.collection("training_notes")
            .document(docId)
            .set(noteToSave)
            .addOnSuccessListener {

                _trainingNotes.value =
                    _trainingNotes.value
                        .filterNot {
                            it.date == date &&
                                    it.teamYear == teamYear &&
                                    it.noteType == type
                        } + noteToSave
            }
            .addOnFailureListener { error ->

                Log.e(
                    TAG,
                    "Error guardando la foto de la nota de entrenamiento",
                    error
                )

                onError("No se pudo guardar la foto: ${error.message}")
            }
    }

    fun getTrainingNoteForDateAndTeam(
        date: String,
        year: Int,
        type: String
    ): Flow<TrainingNote?> {

        return flow {

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

        return TrainingDayCalculator.teamYearsForDate(
            date = date,
            schedules = _schedules.value,
            teams = _teams.value,
            holidayDates = _holidays.value
                .map { it.date }
                .toSet()
        )
    }

    fun updateTeamFirstTrainingDate(
        teamId: Int,
        newDate: String
    ) {

        val user =
            userDoc ?: return

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
                .addOnFailureListener { error ->

                    Log.e(
                        TAG,
                        "Error actualizando fecha inicial",
                        error
                    )
                }
        }
    }

    fun updateTeamLastTrainingDate(year: Int, newDate: String) {
        val userRef = userDoc ?: return
        val currentTeams = _teams.value.toMutableList()
        val index = currentTeams.indexOfFirst { it.year == year }
        if (index != -1) {
            val updatedTeam = currentTeams[index].copy(lastTrainingDate = newDate)
            currentTeams[index] = updatedTeam
            _teams.value = currentTeams

            userRef.collection("teams").document(year.toString())
                .update("lastTrainingDate", newDate)
                .addOnFailureListener {
                }
        }
    }

    fun deleteSchedule(
        schedule: TrainingSchedule
    ) {

        val user =
            userDoc ?: return

        user.collection("schedules")
            .document(schedule.id.toString())
            .delete()
            .addOnSuccessListener {

                _schedules.value =
                    _schedules.value.filter {
                        it.id != schedule.id
                    }
            }
            .addOnFailureListener { error ->

                Log.e(
                    TAG,
                    "Error eliminando horario",
                    error
                )
            }
    }

    fun addOrUpdateSchedule(newSchedule: TrainingSchedule, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val user = userDoc
        if (user == null) {
            onError("No hay un usuario autenticado.")
            return
        }

        // Validaciones (hora de inicio/fin, solape con el mismo equipo y máximo por día)
        val validationError = ScheduleRules.validate(newSchedule, _schedules.value)

        if (validationError != null) {
            onError(validationError)
            return
        }

        // Guardado
        val scheduleToSave = if (newSchedule.id == 0L) {
            newSchedule.copy(id = java.util.UUID.randomUUID().mostSignificantBits)
        } else {
            newSchedule
        }

        user.collection("schedules")
            .document(scheduleToSave.id.toString())
            .set(scheduleToSave)
            .addOnSuccessListener {
                _schedules.value = _schedules.value.filterNot { it.id == scheduleToSave.id } + scheduleToSave
                onSuccess()
            }
            .addOnFailureListener { error ->
                onError(error.message ?: "No se pudo guardar el horario.")
            }
    }

    // ============================================================
    // ASISTENCIAS
    // ============================================================

    fun getAttendanceForDateAndTeam(
        date: String,
        year: Int
    ): Flow<List<Attendance>> {

        return flow {

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

        val user =
            userDoc ?: return

        val batch =
            db.batch()

        attendances.forEach { attendance ->

            val docRef =
                user.collection("attendances")
                    .document(
                        "${attendance.date}_${attendance.playerId}"
                    )

            batch.set(
                docRef,
                attendance
            )
        }

        batch.commit()
            .addOnSuccessListener {

                _attendances.value =
                    _attendances.value
                        .filterNot { existing ->

                            attendances.any { saved ->

                                saved.date ==
                                        existing.date &&
                                        saved.playerId ==
                                        existing.playerId
                            }

                        } + attendances
            }
            .addOnFailureListener { error ->

                Log.e(
                    TAG,
                    "Error guardando asistencias",
                    error
                )
            }
    }

    fun deleteAttendances(attendances: List<Attendance>) {
        val userRef = userDoc ?: return
        val batch = db.batch()

        attendances.forEach { attendance ->
            // El ID que usamos en Firestore para las asistencias es "${date}_${playerId}"
            val docRef = userRef.collection("attendances")
                .document("${attendance.date}_${attendance.playerId}")
            batch.delete(docRef)
        }

        batch.commit().addOnSuccessListener {
            val datesAndPlayersToDelete = attendances.map { "${it.date}_${it.playerId}" }
            _attendances.value = _attendances.value.filterNot {
                "${it.date}_${it.playerId}" in datesAndPlayersToDelete
            }
        }.addOnFailureListener {
            Log.e(TAG, "Error eliminando asistencias", it)
        }
    }

    fun getAllAttendancesByTeam(
        year: Int
    ): Flow<List<Attendance>> {

        return flow {

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
    // PARTIDOS / CONVOCATORIA / RESULTADO
    // ============================================================

    fun addOrUpdateMatch(
        match: Match,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {

        val user =
            userDoc

        if (user == null) {

            onError(
                "No hay ningún usuario autenticado."
            )

            return
        }

        /*
         * Solo comprobamos duplicados cuando es realmente
         * otro documento.
         */
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
                    id = java.util.UUID.randomUUID().mostSignificantBits
                )

            } else {
                match
            }

        /*
         * Muy importante:
         *
         * Se utiliza SIEMPRE el id real del Match.
         * No se crea otro documento al editar una convocatoria.
         */
        val matchRef =
            user.collection("matches")
                .document(
                    matchToSave.id.toString()
                )

        matchRef
            .set(matchToSave)
            .addOnSuccessListener {

                /*
                 * Solo actualizamos el estado local después
                 * de que Firestore confirme el guardado.
                 */
                _matches.value =
                    _matches.value
                        .filterNot {
                            it.id == matchToSave.id
                        } + matchToSave

                Log.d(
                    TAG,
                    "Match guardado correctamente: " +
                            "id=${matchToSave.id}, " +
                            "date=${matchToSave.date}, " +
                            "team=${matchToSave.teamYear}, " +
                            "isConvocatoriaSaved=${matchToSave.isConvocatoriaSaved}"
                )

                onSuccess()
            }
            .addOnFailureListener { error ->

                Log.e(
                    TAG,
                    "ERROR guardando Match " +
                            "${matchToSave.id}",
                    error
                )

                onError(
                    error.message
                        ?: "No se pudo guardar el partido en Firebase."
                )
            }
    }

    fun deleteMatch(
        match: Match
    ) {

        val user =
            userDoc ?: return

        user.collection("matches")
            .document(match.id.toString())
            .delete()
            .addOnSuccessListener {

                _matches.value =
                    _matches.value.filter {
                        it.id != match.id
                    }
            }
            .addOnFailureListener { error ->

                Log.e(
                    TAG,
                    "Error eliminando partido",
                    error
                )
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

        val user =
            userDoc ?: return

        user.collection("holidays")
            .document(holiday.date)
            .set(holiday)
            .addOnSuccessListener {

                _holidays.value =
                    _holidays.value
                        .filterNot {
                            it.date == holiday.date
                        } + holiday
            }
            .addOnFailureListener { error ->

                Log.e(
                    TAG,
                    "Error insertando festivo",
                    error
                )
            }
    }

    fun addHoliday(
        date: String
    ) {

        insertHoliday(
            Holiday(date)
        )
    }

    fun removeHoliday(
        holiday: Holiday
    ) {

        val user =
            userDoc ?: return

        user.collection("holidays")
            .document(holiday.date)
            .delete()
            .addOnSuccessListener {

                _holidays.value =
                    _holidays.value.filter {
                        it.date != holiday.date
                    }
            }
            .addOnFailureListener { error ->

                Log.e(
                    TAG,
                    "Error eliminando festivo",
                    error
                )
            }
    }

    // ============================================================
    // VALIDACIÓN CONVOCATORIA
    // ============================================================

    fun canMakeConvocatoria(
        matchDate: java.time.LocalDate,
        teamYear: Int,
        attendances: List<Attendance>
    ): Pair<Boolean, String?> {

        val missingDate =
            ConvocatoriaRules.firstMissingAttendance(
                matchDate = matchDate,
                teamYear = teamYear,
                attendedDates = attendances.map { it.date }.toSet(),
                schedules = _schedules.value,
                teams = _teams.value,
                holidayDates = _holidays.value.map { it.date }.toSet()
            ) ?: return Pair(true, null)

        val formatter =
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")

        return Pair(
            false,
            "Falta asistencia del ${missingDate.format(formatter)}"
        )
    }

    // ============================================================
    // BORRADOR CONVOCATORIA
    // ============================================================

    var draftMatchDate: String? = null
        private set

    var draftTeamYear: Int? = null
        private set

    var draftSummonedIds: Set<Long>? = null
        private set

    var draftReasonsMap: Map<String, String>? = null
        private set

    var draftIsEditMode: Boolean? = null
        private set

    fun saveDraftConvocatoria(
        date: String,
        teamYear: Int,
        summoned: Set<Long>,
        reasons: Map<String, String>,
        isEdit: Boolean
    ) {

        draftMatchDate =
            date

        draftTeamYear =
            teamYear

        draftSummonedIds =
            summoned

        draftReasonsMap =
            reasons

        draftIsEditMode =
            isEdit
    }

    fun clearDraftConvocatoria() {

        draftMatchDate = null
        draftTeamYear = null
        draftSummonedIds = null
        draftReasonsMap = null
        draftIsEditMode = null
    }

    // ============================================================
    // ACTUALIZAR PERFIL Y CONTRASEÑA
    // ============================================================
    fun updateUserProfile(name: String, lastName: String, club: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val userRef = userDoc
        if (userRef == null) {
            onError("No se encontró sesión de usuario.")
            return
        }

        userRef.update(
            mapOf(
                "name" to name,
                "lastName" to lastName,
                "club" to club
            )
        ).addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener { error ->
            onError(error.message ?: "Error al actualizar los datos.")
        }
    }

    fun changePassword(oldPass: String, newPass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val currentUser = auth.currentUser
        val email = currentUser?.email

        if (currentUser != null && email != null) {
            val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, oldPass)

            currentUser.reauthenticate(credential).addOnSuccessListener {
                currentUser.updatePassword(newPass).addOnSuccessListener {
                    onSuccess()
                }.addOnFailureListener { error ->
                    onError(error.message ?: "Error al actualizar la contraseña.")
                }
            }.addOnFailureListener {
                onError("La contraseña actual es incorrecta.")
            }
        } else {
            onError("No se encontró sesión de usuario activa.")
        }
    }

    // ============================================================
    // LIFECYCLE
    // ============================================================

    override fun onCleared() {

        removeFirebaseListeners()

        auth.removeAuthStateListener(
            authStateListener
        )

        super.onCleared()
    }
}