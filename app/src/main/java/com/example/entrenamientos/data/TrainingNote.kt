package com.example.entrenamientos.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "training_notes")
data class TrainingNote(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val teamYear: Int,
    val noteType: String, // "ENTRENAMIENTO" u "OTROS"
    val content: String
)