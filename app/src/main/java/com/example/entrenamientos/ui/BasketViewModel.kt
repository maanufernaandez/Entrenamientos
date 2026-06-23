package com.example.entrenamientos.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.entrenamientos.data.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BasketViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    // Estado básico para la fecha seleccionada en el calendario
    private val _selectedDate = MutableStateFlow("2026-09-01")
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    // Lógica del ciclo de asistencia (Verde: 0 -> Amarillo: 1 -> Rojo: 2 -> Verde: 0)
    fun getNextAttendanceStatus(currentStatus: Int): Int {
        return (currentStatus + 1) % 3
    }

    // Funciones para interactuar con la Base de Datos
    fun addPlayer(name: String, teamYear: Int) {
        viewModelScope.launch {
            repository.addPlayer(Player(name = name, teamYear = teamYear))
        }
    }

    fun saveTrainingNote(date: String, teamYear: Int, type: String, content: String) {
        viewModelScope.launch {
            repository.saveTrainingNote(TrainingNote(date = date, teamYear = teamYear, noteType = type, content = content))
        }
    }
}