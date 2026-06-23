package com.example.entrenamientos.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Players
    @Query("SELECT * FROM players WHERE teamYear = :year")
    fun getPlayersByTeam(year: Int): Flow<List<Player>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: Player)

    @Query("DELETE FROM players WHERE id = :playerId")
    suspend fun deletePlayer(playerId: Long)

    // Attendance
    @Query("SELECT * FROM attendances WHERE date = :date AND teamYear = :year")
    fun getAttendanceByDateAndTeam(date: String, year: Int): Flow<List<Attendance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendances(attendances: List<Attendance>)

    // Matches
    @Query("SELECT * FROM matches ORDER BY date ASC")
    fun getAllMatches(): Flow<List<Match>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: Match)

    @Update
    suspend fun updateMatch(match: Match)

    // Training Notes
    @Query("SELECT * FROM training_notes WHERE date = :date AND teamYear = :year")
    fun getNotesByDateAndTeam(date: String, year: Int): Flow<List<TrainingNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrainingNote(note: TrainingNote)

    @androidx.room.Update
    suspend fun updatePlayer(player: Player)

    @androidx.room.Delete
    suspend fun deletePlayer(player: Player)

    @androidx.room.Query("SELECT * FROM training_schedules")
    fun getAllSchedules(): kotlinx.coroutines.flow.Flow<List<TrainingSchedule>>

    @androidx.room.Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: TrainingSchedule)

    @androidx.room.Delete
    suspend fun deleteSchedule(schedule: TrainingSchedule)
}