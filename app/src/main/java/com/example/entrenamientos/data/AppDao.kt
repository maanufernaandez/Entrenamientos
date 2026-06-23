package com.example.entrenamientos.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // --- PLAYERS ---
    @Query("SELECT * FROM players WHERE teamYear = :year")
    fun getPlayersByTeam(year: Int): Flow<List<Player>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: Player)

    @Update
    suspend fun updatePlayer(player: Player)

    @Delete
    suspend fun deletePlayer(player: Player)

    // --- ATTENDANCE ---
    @Query("SELECT * FROM attendances WHERE date = :date AND teamYear = :year")
    fun getAttendanceByDateAndTeam(date: String, year: Int): Flow<List<Attendance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendances(attendances: List<Attendance>)

    // --- MATCHES ---
    @Query("SELECT * FROM matches ORDER BY date ASC")
    fun getAllMatches(): Flow<List<Match>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: Match)

    @Update
    suspend fun updateMatch(match: Match)

    @Delete
    suspend fun deleteMatch(match: Match)

    // --- TRAINING NOTES ---
    @Query("SELECT * FROM training_notes WHERE date = :date AND teamYear = :year")
    fun getNotesByDateAndTeam(date: String, year: Int): Flow<List<TrainingNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrainingNote(note: TrainingNote)

    // --- TRAINING SCHEDULES ---
    @Query("SELECT * FROM training_schedules")
    fun getAllSchedules(): Flow<List<TrainingSchedule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: TrainingSchedule)

    @Delete
    suspend fun deleteSchedule(schedule: TrainingSchedule)
}