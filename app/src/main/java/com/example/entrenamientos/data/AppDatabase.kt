package com.example.entrenamientos.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Player::class, TrainingSchedule::class, Match::class, Attendance::class, Holiday::class, TrainingNote::class, Team::class],
    version = 4 ,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
}