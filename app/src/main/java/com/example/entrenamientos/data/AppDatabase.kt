package com.example.entrenamientos.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Player::class, Attendance::class, TrainingNote::class, Match::class, TrainingSchedule::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "entrenamientos_database"
                )
                    .fallbackToDestructiveMigration() // <--- OBLIGATORIO
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}