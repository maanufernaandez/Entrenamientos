package com.example.entrenamientos.di

import com.example.entrenamientos.data.AppDao
import com.example.entrenamientos.data.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @javax.inject.Singleton
    fun provideDatabase(@dagger.hilt.android.qualifiers.ApplicationContext context: android.content.Context): com.example.entrenamientos.data.AppDatabase {
        return androidx.room.Room.databaseBuilder(
            context,
            com.example.entrenamientos.data.AppDatabase::class.java,
            "entrenamientos_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideAppDao(database: AppDatabase): AppDao {
        return database.appDao()
    }
}