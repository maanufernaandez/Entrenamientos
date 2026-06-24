package com.example.entrenamientos.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "holidays")
data class Holiday(
    @PrimaryKey val date: String
)