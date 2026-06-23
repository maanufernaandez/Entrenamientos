package com.example.entrenamientos.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Player::class, Attendance::class, TrainingNote::class, Match::class, TrainingSchedule::class],
    version = 2, // Subimos a versión 2
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "entrenamientos_database"
                )
                    .fallbackToDestructiveMigration() // Borra la DB vieja de tu teléfono para instalar la nueva sin error
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}