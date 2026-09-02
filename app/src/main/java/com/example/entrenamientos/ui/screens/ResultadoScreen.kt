package com.example.entrenamientos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.entrenamientos.ui.BasketViewModel

@Composable
fun ResultadoScreen(viewModel: BasketViewModel, navController: NavController) {
    val selectedDateStr by viewModel.selectedDate.collectAsState()
    val date = java.time.LocalDate.parse(selectedDateStr)
    val match = viewModel.getMatchForDate(date) ?: return

    var resLocal by remember { mutableStateOf(match.resultLocal?.toString() ?: "") }
    var resVisitor by remember { mutableStateOf(match.resultVisitor?.toString() ?: "") }
    var ftMade by remember { mutableStateOf(match.ftMade.toString()) }
    var ftAttempted by remember { mutableStateOf(match.ftAttempted.toString()) }

    var observaciones by remember { mutableStateOf(match.observations) }

    androidx.activity.compose.BackHandler {
        navController.popBackStack()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Resultado del Partido", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Marcador Final", style = MaterialTheme.typography.titleMedium)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = resLocal,
                onValueChange = { resLocal = it },
                label = { Text("Local") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = resVisitor,
                onValueChange = { resVisitor = it },
                label = { Text("Visitante") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Tiros Libres", style = MaterialTheme.typography.titleMedium)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = ftMade,
                onValueChange = { ftMade = it },
                label = { Text("Convertidos") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = ftAttempted,
                onValueChange = { ftAttempted = it },
                label = { Text("Intentados") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Observaciones del partido", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = observaciones,
            onValueChange = { observaciones = it },
            label = { Text("Escribe aquí las observaciones...") },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            maxLines = 4
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { navController.popBackStack() },
                colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceRed),
                modifier = Modifier.weight(1f)
            ) {
                Text("Volver", color = Color.White)
            }

            Button(
                onClick = {
                    val localScore = resLocal.toIntOrNull()
                    val visitorScore = resVisitor.toIntOrNull()

                    if (localScore != null && visitorScore != null && localScore == visitorScore) {
                        android.widget.Toast.makeText(
                            navController.context,
                            "El resultado no puede ser empate",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val updatedMatch = match.copy(
                            resultLocal = localScore,
                            resultVisitor = visitorScore,
                            ftMade = ftMade.toIntOrNull() ?: 0,
                            ftAttempted = ftAttempted.toIntOrNull() ?: 0,
                            observations = observaciones
                        )
                        viewModel.addOrUpdateMatch(updatedMatch)
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceGreen)
            ) {
                Text(if (match.resultLocal != null) "Actualizar" else "Guardar", color = Color.Black)
            }
        }
    }
}