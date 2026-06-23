package com.example.entrenamientos.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(
    private val dao: AppDao
) {
    // Jugadores
    fun getPlayers(teamYear: Int): Flow<List<Player>> = dao.getPlayersByTeam(teamYear)
    suspend fun addPlayer(player: Player) = dao.insertPlayer(player)
    suspend fun removePlayer(playerId: Long) = dao.deletePlayer(playerId)

    // Asistencia
    fun getAttendance(date: String, teamYear: Int): Flow<List<Attendance>> = dao.getAttendanceByDateAndTeam(date, teamYear)
    suspend fun saveAttendances(attendances: List<Attendance>) = dao.insertAttendances(attendances)

    // Partidos
    fun getMatches(): Flow<List<Match>> = dao.getAllMatches()
    suspend fun saveMatch(match: Match) = dao.insertMatch(match)
    suspend fun updateMatch(match: Match) = dao.updateMatch(match)

    // Notas de Entrenamiento
    fun getTrainingNotes(date: String, teamYear: Int): Flow<List<TrainingNote>> = dao.getNotesByDateAndTeam(date, teamYear)
    suspend fun saveTrainingNote(note: TrainingNote) = dao.insertTrainingNote(note)

    suspend fun updatePlayer(player: Player) {
        dao.updatePlayer(player)
    }

    suspend fun deletePlayer(player: Player) {
        dao.deletePlayer(player)
    }

    fun getAllSchedules() = dao.getAllSchedules()

    suspend fun insertSchedule(schedule: com.example.entrenamientos.data.TrainingSchedule) {
        dao.insertSchedule(schedule)
    }

    suspend fun deleteSchedule(schedule: com.example.entrenamientos.data.TrainingSchedule) {
        dao.deleteSchedule(schedule)
    }
}