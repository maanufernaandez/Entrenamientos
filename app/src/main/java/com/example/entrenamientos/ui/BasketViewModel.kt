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
import kotlinx.coroutines.flow.first

@HiltViewModel
class BasketViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    // Esto se ejecuta automáticamente al iniciar el ViewModel
    init {
        checkAndPopulateDefaultPlayers()
    }

    private fun checkAndPopulateDefaultPlayers() {
        viewModelScope.launch {
            // Comprobamos los prebenjamines
            val currentPrebenjamines = repository.getPlayers(2018).first()
            if (currentPrebenjamines.isEmpty()) {
                val defaultPrebenjamines = listOf(
                    "Jericó", "Andrea", "Silvia", "Daniela", "Carlos", "Jaqueline",
                    "Emma", "Vega", "Lara", "Crismeily", "Martina", "Izan"
                )
                defaultPrebenjamines.forEach { name ->
                    repository.addPlayer(Player(name = name, teamYear = 2018))
                }
            }

            // Comprobamos las infantiles
            val currentInfantiles = repository.getPlayers(2013).first()
            if (currentInfantiles.isEmpty()) {
                val defaultInfantiles = listOf(
                    "Mayte", "Martina", "Maria", "Nahia", "Leire", "Tania",
                    "Aitana", "Wiktoria", "Saioa", "Alba", "Arianna", "Paula", "Salome"
                )
                defaultInfantiles.forEach { name ->
                    repository.addPlayer(Player(name = name, teamYear = 2013))
                }
            }
        }
    }

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

    // Devuelve qué equipos entrenan en una fecha concreta para pintar las franjas
    fun getTeamsForDate(date: java.time.LocalDate): List<Int> {
        return com.example.entrenamientos.domain.CalendarLogic.getTrainingTeams(date)
    }

    // Devuelve si hay partido en esa fecha para pintar el icono azul
    fun hasMatchOnDate(date: java.time.LocalDate): Boolean {
        return com.example.entrenamientos.domain.CalendarLogic.isInfantilMatchDay(date)
    }
}