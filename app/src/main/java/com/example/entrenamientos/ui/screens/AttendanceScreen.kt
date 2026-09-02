package com.example.entrenamientos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.entrenamientos.data.Attendance
import com.example.entrenamientos.ui.BasketViewModel

@Composable
fun AttendanceScreen(viewModel: BasketViewModel, navController: NavController) {
    val date by viewModel.selectedDate.collectAsState()
    val teamYear by viewModel.selectedTeamYear.collectAsState()
    val players by viewModel.getPlayersForTeam(teamYear).collectAsState(initial = emptyList())
    val currentAttendances by viewModel.getAttendanceForDateAndTeam(date, teamYear).collectAsState(initial = emptyList())

    val attendanceMap = remember(currentAttendances) {
        mutableStateMapOf<Long, Int>().apply {
            currentAttendances.forEach { put(it.playerId, it.status) }
        }
    }

    val playerSortComparator = compareBy<com.example.entrenamientos.data.Player> { it.dorsal.isNullOrBlank() }
        .thenBy { it.dorsal?.toIntOrNull() ?: 999 }

    val sortedPlayers = players.sortedWith(playerSortComparator)
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    val dateObj = java.time.LocalDate.parse(date)
    val dayOfWeek = dateObj.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES")).replaceFirstChar { it.uppercase() }

    val monthNames = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sept", "Oct", "Nov", "Dic")
    val customMonth = monthNames[dateObj.monthValue - 1]
    val dayNumber = dateObj.dayOfMonth

    androidx.activity.compose.BackHandler {
        navController.popBackStack()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Asistencia $dayOfWeek $dayNumber $customMonth", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Toca para cambiar el estado", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(state = listState, modifier = Modifier.weight(1f).fillMaxWidth()) {
            items(sortedPlayers) { player ->
                val status = attendanceMap[player.id] ?: 0
                val containerColor = when (status) {
                    0 -> com.example.entrenamientos.ui.theme.AttendanceGreen
                    1 -> com.example.entrenamientos.ui.theme.AttendanceYellow
                    else -> com.example.entrenamientos.ui.theme.AttendanceRed
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(containerColor)
                        .clickable {
                            attendanceMap[player.id] = viewModel.getNextAttendanceStatus(status)
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val baseName = if (player.lastName.isNotBlank()) "${player.name} ${player.lastName}" else player.name

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = baseName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    val statusText = when (status) {
                        0 -> "Asiste"
                        1 -> "Justificada"
                        else -> "Injustificada"
                    }
                    Text(statusText, fontWeight = FontWeight.Normal, color = Color.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { navController.popBackStack() },
                colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceRed),
                modifier = Modifier.weight(1f)
            ) { Text("Cancelar", color = Color.White) }

            Button(
                onClick = {
                    val newAttendances = sortedPlayers.map { player ->
                        Attendance(
                            id = currentAttendances.find { it.playerId == player.id }?.id ?: 0,
                            date = date,
                            playerId = player.id,
                            teamYear = teamYear,
                            status = attendanceMap[player.id] ?: 0
                        )
                    }
                    viewModel.saveAttendances(newAttendances)
                    navController.popBackStack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceGreen),
                modifier = Modifier.weight(1f)
            ) { Text("Guardar", color = Color.Black) }
        }
    }
}