package com.example.entrenamientos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.entrenamientos.ui.BasketViewModel

@Composable
fun TrainingNoteScreen(viewModel: BasketViewModel = hiltViewModel(), navController: NavController, noteType: String) {
    val dateStr by viewModel.selectedDate.collectAsState()
    val teamYear by viewModel.selectedTeamYear.collectAsState()
    val teamsList by viewModel.teams.collectAsState()

    val existingNote by viewModel.getTrainingNoteForDateAndTeam(dateStr, teamYear, noteType).collectAsState(initial = null)

    var noteContent by remember(existingNote) { mutableStateOf(existingNote?.content ?: "") }

    val team = teamsList.find { it.year == teamYear }
    val titlePrefix = if (noteType == "ENTRENAMIENTO") "Entrenamiento" else "Notas"

    val date = java.time.LocalDate.parse(dateStr)
    val dayOfWeek = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES")).replaceFirstChar { it.uppercase() }
    val monthName = date.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES"))

    // Lógica para obtener el color y la categoría completa del equipo
    val teamColor = team?.colorHex?.let {
        try { Color(android.graphics.Color.parseColor(it)) } catch (_: Exception) { Color.Black }
    } ?: Color.Black

    val genderStr = when (team?.gender) {
        "M" -> "Masculino"
        "F" -> "Femenino"
        else -> "Mixto"
    }

    val catSplit = team?.categoryYear?.split(" ") ?: emptyList()
    val fullCategory = if (catSplit.size >= 2 && (catSplit.last() == "1ª" || catSplit.last() == "2ª")) {
        val baseCat = catSplit.dropLast(1).joinToString(" ")
        "$baseCat $genderStr ${catSplit.last()}"
    } else if (!team?.categoryYear.isNullOrBlank()) {
        "${team?.categoryYear} $genderStr"
    } else {
        genderStr
    }

    androidx.activity.compose.BackHandler {
        navController.popBackStack()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = titlePrefix, style = MaterialTheme.typography.headlineMedium)
        Text(text = "$dayOfWeek ${date.dayOfMonth} de $monthName", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)

        Spacer(modifier = Modifier.height(8.dp))

        // Categoría centrada y con el color del equipo
        Text(
            text = fullCategory,
            style = MaterialTheme.typography.titleLarge,
            color = teamColor,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = noteContent,
            onValueChange = { noteContent = it },
            modifier = Modifier.fillMaxWidth().weight(1f),
            placeholder = { Text("Escribe aquí...") },
            textStyle = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceRed)
            ) { Text("Cancelar", color = Color.White) }

            Button(
                onClick = {
                    viewModel.saveTrainingNote(date = dateStr, teamYear = teamYear, type = noteType, content = noteContent, existingNote = existingNote)
                    navController.popBackStack()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceGreen)
            ) { Text("Guardar", color = Color.Black) }
        }
    }
}