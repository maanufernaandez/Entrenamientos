package com.example.entrenamientos.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.entrenamientos.ui.BasketViewModel

@SuppressLint("MutableCollectionMutableState")
@Composable
fun ConvocatoriaScreen(viewModel: BasketViewModel, navController: NavController) {
    val selectedDateStr by viewModel.selectedDate.collectAsState()
    val date = java.time.LocalDate.parse(selectedDateStr)

    val allMatches by viewModel.matches.collectAsState()
    val match = allMatches.find { it.date == selectedDateStr } ?: return

    val teamsList by viewModel.teams.collectAsState()
    val matchTeam = teamsList.find { it.year == match.teamYear }
    val isFemale = matchTeam?.gender == "F"

    val txtConvocadas = if (isFemale) "CONVOCADAS" else "CONVOCADOS"
    val txtDesconvocadas = if (isFemale) "DESCONVOCADAS" else "DESCONVOCADOS"
    val txtSeleccionadas = if (isFemale) "Seleccionadas" else "Seleccionados"
    val txtJugadoras = if (isFemale) "jugadoras" else "jugadores"

    val players by viewModel.getPlayersForTeam(match.teamYear).collectAsState(initial = emptyList())
    val context = androidx.compose.ui.platform.LocalContext.current

    val hasDraft = viewModel.draftMatchDate == selectedDateStr

    var isEditMode by remember(match) {
        mutableStateOf(if (hasDraft) viewModel.draftIsEditMode ?: !match.isConvocatoriaSaved else !match.isConvocatoriaSaved)
    }

    var summonedIds by remember(players, match) {
        mutableStateOf(
            if (hasDraft && viewModel.draftSummonedIds != null) viewModel.draftSummonedIds!!
            else if (match.isConvocatoriaSaved) match.summonedPlayers.toSet()
            else players.map { it.id }.toSet()
        )
    }
    var reasonsMap by remember(match) {
        mutableStateOf(
            if (hasDraft && viewModel.draftReasonsMap != null) viewModel.draftReasonsMap!!.toMutableMap()
            else match.unsummonedReasons.toMutableMap()
        )
    }

    var shouldSaveDraft by remember { mutableStateOf(true) }
    val currentSummonedIds = androidx.compose.runtime.rememberUpdatedState(summonedIds)
    val currentReasonsMap = androidx.compose.runtime.rememberUpdatedState(reasonsMap)
    val currentIsEditMode = androidx.compose.runtime.rememberUpdatedState(isEditMode)

    androidx.compose.runtime.DisposableEffect(selectedDateStr) {
        onDispose {
            if (shouldSaveDraft) {
                viewModel.saveDraftConvocatoria(
                    selectedDateStr,
                    currentSummonedIds.value,
                    currentReasonsMap.value.toMap(),
                    currentIsEditMode.value
                )
            } else {
                viewModel.clearDraftConvocatoria()
            }
        }
    }

    androidx.activity.compose.BackHandler {
        shouldSaveDraft = false
        navController.popBackStack()
    }

    var playerToUnsummon by remember { mutableStateOf<com.example.entrenamientos.data.Player?>(null) }
    val reasonOptions = listOf("Rotación", "Lesión", "Falta a entrenamientos", "Castigada", "No puede ir")

    val playerSortComparator = compareBy<com.example.entrenamientos.data.Player> { it.dorsal.isNullOrBlank() }
        .thenBy { it.dorsal?.toIntOrNull() ?: 999 }

    val sortedPlayers = players.sortedWith(playerSortComparator)

    val dayOfWeek = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES")).replaceFirstChar { it.uppercase() }
    val monthName = date.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES")).replaceFirstChar { it.uppercase() }
    val matchDateFormatted = "$dayOfWeek ${date.dayOfMonth} de $monthName"

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(if (isEditMode) {
            if (match.isConvocatoriaSaved) "Editar Convocatoria" else "Crear Convocatoria"
        } else "Convocatoria Oficial", style = MaterialTheme.typography.headlineMedium)

        Text("vs ${match.opponent} - $matchDateFormatted", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))

        if (!isEditMode) {
            val convocadas = sortedPlayers.filter { match.summonedPlayers.contains(it.id) }
            val desconvocadas = sortedPlayers.filter { !match.summonedPlayers.contains(it.id) }

            LazyColumn(modifier = Modifier.weight(1f)) {
                item {
                    Text("- $txtConvocadas (${convocadas.size}/12)", style = MaterialTheme.typography.titleMedium, color = com.example.entrenamientos.ui.theme.SuccessGreen)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(convocadas) { p ->
                    val dDisplay = p.dorsal?.takeIf { it.isNotBlank() } ?: "s.n."
                    val baseName = if (p.lastName.isNotBlank()) "${p.name} ${p.lastName}" else p.name

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dDisplay,
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = 19.sp,
                            modifier = Modifier.width(42.dp),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "-",
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = 19.sp,
                            modifier = Modifier.width(24.dp),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = baseName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = 19.sp,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Start
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("- $txtDesconvocadas", style = MaterialTheme.typography.titleMedium, color = com.example.entrenamientos.ui.theme.AttendanceRed)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(desconvocadas) { p ->
                    val baseName = if (p.lastName.isNotBlank()) "${p.name} ${p.lastName}" else p.name

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, bottom = 8.dp, end = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "• $baseName",
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = match.unsummonedReasons[p.id]?.takeIf { it.isNotBlank() } ?: "Sin motivo",
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 16.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            shouldSaveDraft = false
                            navController.popBackStack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                        modifier = Modifier.weight(1f)
                    ) { Text("Volver", color = Color.White) }

                    Button(
                        onClick = { isEditMode = true },
                        colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceGreen),
                        modifier = Modifier.weight(1f)
                    ) { Text("Editar", color = Color.Black) }
                }

                Button(
                    onClick = {
                        shouldSaveDraft = false
                        val resetMatch = match.copy(isConvocatoriaSaved = false, summonedPlayers = emptyList(), unsummonedReasons = emptyMap())
                        viewModel.addOrUpdateMatch(resetMatch)
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceRed),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Eliminar", color = Color.White) }
            }

        } else {
            Text("$txtSeleccionadas: ${summonedIds.size} (Mín. 8 - Máx. 12)", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(sortedPlayers) { player ->
                    val isSummoned = summonedIds.contains(player.id)
                    val containerColor = if (isSummoned) com.example.entrenamientos.ui.theme.SuccessGreen else com.example.entrenamientos.ui.theme.AttendanceRed

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(containerColor.copy(alpha = 0.2f))
                            .clickable {
                                if (isSummoned) {
                                    playerToUnsummon = player
                                } else {
                                    summonedIds = summonedIds + player.id
                                    reasonsMap.remove(player.id)
                                }
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val dDisplay = player.dorsal?.takeIf { it.isNotBlank() } ?: "s.n."
                        val baseName = if (player.lastName.isNotBlank()) "${player.name} ${player.lastName}" else player.name

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = dDisplay,
                                style = MaterialTheme.typography.bodyLarge,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = containerColor,
                                modifier = Modifier.width(36.dp),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = " · ",
                                style = MaterialTheme.typography.bodyLarge,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = containerColor
                            )
                            Text(
                                text = baseName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = containerColor
                            )
                        }

                        if (isSummoned) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = containerColor)
                        } else {
                            Text(reasonsMap[player.id] ?: "", style = MaterialTheme.typography.bodyMedium, color = containerColor, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (summonedIds.size < 8 || summonedIds.size > 12) {
                        android.widget.Toast.makeText(
                            context,
                            "Debes convocar entre 8 y 12 $txtJugadoras (Actual: ${summonedIds.size})",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    } else {
                        shouldSaveDraft = false
                        val updatedMatch = match.copy(
                            isConvocatoriaSaved = true,
                            summonedPlayers = summonedIds.toList(),
                            unsummonedReasons = reasonsMap.toMap()
                        )
                        viewModel.addOrUpdateMatch(updatedMatch)
                        isEditMode = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceGreen)
            ) { Text("Guardar", color = Color.Black) }
        }
    }

    if (playerToUnsummon != null) {
        AlertDialog(
            onDismissRequest = { playerToUnsummon = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(16.dp),
            title = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Motivo de desconvocatoria:", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    val dDisplay = playerToUnsummon?.dorsal?.takeIf { it.isNotBlank() } ?: "s.n."
                    val baseName = if (playerToUnsummon?.lastName?.isNotBlank() == true) "${playerToUnsummon?.name} ${playerToUnsummon?.lastName}" else playerToUnsummon?.name ?: ""

                    Row(modifier = Modifier.align(Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = dDisplay,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(42.dp),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = " · ",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = baseName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            text = {
                Column {
                    reasonOptions.forEach { reason ->
                        Button(
                            onClick = {
                                reasonsMap = reasonsMap.toMutableMap().apply { put(playerToUnsummon!!.id, reason) }
                                summonedIds = summonedIds - playerToUnsummon!!.id
                                playerToUnsummon = null
                            },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceRed)
                        ) {
                            Text(
                                text = reason,
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { playerToUnsummon = null }) { Text("Cancelar", color = Color.Gray) }
            }
        )
    }
}