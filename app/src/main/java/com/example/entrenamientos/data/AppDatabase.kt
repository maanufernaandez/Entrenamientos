package com.example.entrenamientos.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Player::class, Attendance::class, Match::class, TrainingNote::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
}