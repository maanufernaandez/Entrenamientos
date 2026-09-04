package com.example.entrenamientos.ui.screens

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.entrenamientos.data.Player
import com.example.entrenamientos.ui.BasketViewModel
import com.example.entrenamientos.ui.theme.AttendanceGreen
import com.example.entrenamientos.ui.theme.AttendanceRed
import com.example.entrenamientos.ui.theme.SuccessGreen

@SuppressLint("MutableCollectionMutableState")
@Composable
fun ConvocatoriaScreen(
    viewModel: BasketViewModel,
    navController: NavController
) {
    val selectedDateStr by viewModel.selectedDate.collectAsState()
    val teamYear by viewModel.selectedTeamYear.collectAsState()

    val date = try {
        java.time.LocalDate.parse(selectedDateStr)
    } catch (_: Exception) {
        return
    }

    val allMatches by viewModel.matches.collectAsState()

    val match = allMatches.find {
        it.date == selectedDateStr && it.teamYear == teamYear
    } ?: return

    val teamsList by viewModel.teams.collectAsState()
    val matchTeam = teamsList.find { it.year == match.teamYear }
    val isFemale = matchTeam?.gender == "F"
    val category = matchTeam?.categoryYear ?: ""

    val isSeniorCategory = category.startsWith("Cadete") || category.startsWith("Junior") || category.startsWith("Senior")
    val isInfantil = category.startsWith("Infantil") || category.startsWith("Preinfantil")
    val isMini = category.startsWith("Minibasket") || category.startsWith("PreMinibasket") || category.startsWith("Benjamin 5x5")
    val is3x3 = category.startsWith("Benjamin 3x3") || category.startsWith("Pre-Benjamin 3x3")

    val minPlayers = when {
        is3x3 -> 4
        isSeniorCategory -> 5
        else -> 8
    }

    val absoluteMinPlayers = if (isInfantil) 5 else minPlayers
    val maxPlayers = if (isMini) 15 else 12

    val txtConvocadas = if (isFemale) "CONVOCADAS" else "CONVOCADOS"
    val txtDesconvocadas = if (isFemale) "DESCONVOCADAS" else "DESCONVOCADOS"
    val txtSeleccionadas = if (isFemale) "Seleccionadas" else "Seleccionados"
    val txtJugadoras = if (isFemale) "jugadoras" else "jugadores"
    val txtConvocado = if (isFemale) "Convocada" else "Convocado"

    val players by viewModel.getPlayersForTeam(match.teamYear).collectAsState(initial = emptyList())
    val context = androidx.compose.ui.platform.LocalContext.current

    val hasDraft = !match.isConvocatoriaSaved && viewModel.draftMatchDate == selectedDateStr && viewModel.draftTeamYear == teamYear

    var isEditMode by remember(match.id) {
        mutableStateOf(if (hasDraft) viewModel.draftIsEditMode ?: !match.isConvocatoriaSaved else !match.isConvocatoriaSaved)
    }

    var summonedIds by remember(match.id) {
        mutableStateOf(
            if (hasDraft && viewModel.draftSummonedIds != null) viewModel.draftSummonedIds!!
            else if (match.isConvocatoriaSaved) match.summonedPlayers.toSet()
            else emptySet()
        )
    }

    LaunchedEffect(players, match.id) {
        if (!match.isConvocatoriaSaved && !hasDraft && summonedIds.isEmpty() && players.isNotEmpty()) {
            summonedIds = players.map { it.id }.toSet()
        }
    }

    var reasonsMap by remember(match.id) {
        mutableStateOf(
            if (hasDraft && viewModel.draftReasonsMap != null) viewModel.draftReasonsMap!!.toMutableMap()
            else match.unsummonedReasons.toMutableMap()
        )
    }

    var shouldSaveDraft by remember { mutableStateOf(true) }
    var showMinPlayersWarning by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    val currentSummonedIds = rememberUpdatedState(summonedIds)
    val currentReasonsMap = rememberUpdatedState(reasonsMap)
    val currentIsEditMode = rememberUpdatedState(isEditMode)

    DisposableEffect(selectedDateStr, teamYear) {
        onDispose {
            if (shouldSaveDraft) {
                viewModel.saveDraftConvocatoria(
                    selectedDateStr,
                    teamYear,
                    currentSummonedIds.value,
                    currentReasonsMap.value.toMap(),
                    currentIsEditMode.value
                )
            } else {
                viewModel.clearDraftConvocatoria()
            }
        }
    }

    BackHandler {
        shouldSaveDraft = false
        navController.popBackStack()
    }

    fun saveConvocatoria() {
        if (isSaving) return
        isSaving = true
        showMinPlayersWarning = false

        val convocatoriaChanged = !match.isConvocatoriaSaved || match.summonedPlayers.toSet() != summonedIds || match.unsummonedReasons != reasonsMap

        val updatedMatch = match.copy(
            isConvocatoriaSaved = true,
            summonedPlayers = summonedIds.toList().sorted(),
            unsummonedReasons = reasonsMap.toMap()
        )

        viewModel.addOrUpdateMatch(
            match = updatedMatch,
            onSuccess = {
                if (convocatoriaChanged) {
                    viewModel.saveTrainingNote(selectedDateStr, match.teamYear, "QUINTETOS", "", null)
                }
                viewModel.clearDraftConvocatoria()
                shouldSaveDraft = false
                isEditMode = false
                isSaving = false
                Toast.makeText(context, "Convocatoria guardada correctamente", Toast.LENGTH_SHORT).show()
            },
            onError = { errorMessage ->
                isSaving = false
                shouldSaveDraft = true
                Toast.makeText(context, "No se pudo guardar la convocatoria:\n$errorMessage", Toast.LENGTH_LONG).show()
            }
        )
    }

    fun eliminarConvocatoria() {
        if (isSaving) return
        isSaving = true
        shouldSaveDraft = false

        val resetMatch = match.copy(
            isConvocatoriaSaved = false,
            summonedPlayers = emptyList(),
            unsummonedReasons = emptyMap()
        )

        viewModel.addOrUpdateMatch(
            match = resetMatch,
            onSuccess = {
                viewModel.saveTrainingNote(selectedDateStr, match.teamYear, "QUINTETOS", "", null)
                viewModel.clearDraftConvocatoria()

                // Al eliminar, volvemos a poner a todas en verde por defecto
                summonedIds = players.map { it.id }.toSet()
                reasonsMap = mutableMapOf()

                shouldSaveDraft = true
                isEditMode = true
                isSaving = false
                Toast.makeText(context, "Convocatoria eliminada", Toast.LENGTH_SHORT).show()
            },
            onError = { errorMessage ->
                isSaving = false
                shouldSaveDraft = true
                Toast.makeText(context, "No se pudo eliminar la convocatoria:\n$errorMessage", Toast.LENGTH_LONG).show()
            }
        )
    }

    var playerToUnsummon by remember { mutableStateOf<Player?>(null) }
    val reasonOptions = listOf("Rotación", "Lesión", "Falta a entrenamientos", "Castigada", "No puede ir")

    val playerSortComparator = compareBy<Player> { it.dorsal.isNullOrBlank() }.thenBy { it.dorsal?.toIntOrNull() ?: 999 }
    val sortedPlayers = players.sortedWith(playerSortComparator)

    val dayOfWeek = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES")).replaceFirstChar { it.uppercase() }
    val monthName = date.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES")).replaceFirstChar { it.uppercase() }
    val matchDateFormatted = "$dayOfWeek ${date.dayOfMonth} de $monthName"

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = if (isEditMode) { if (match.isConvocatoriaSaved) "Editar Convocatoria" else "Crear Convocatoria" } else "Convocatoria Oficial", style = MaterialTheme.typography.headlineMedium)
        Text(text = "vs ${match.opponent} - $matchDateFormatted", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))

        if (!isEditMode) {
            if (isInfantil && match.summonedPlayers.size in 5..7) {
                Text(
                    text = "¡AVISO! No dispones del número mínimo de $txtJugadoras para cumplir con la normativa.",
                    color = AttendanceRed,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
            }

            val convocadas = sortedPlayers.filter { match.summonedPlayers.contains(it.id) }
            val desconvocadas = sortedPlayers.filter { !match.summonedPlayers.contains(it.id) }
            val listStateRead = rememberLazyListState()

            LazyColumn(state = listStateRead, modifier = Modifier.weight(1f).convocatoriaVerticalScrollShadow(listStateRead)) {
                item {
                    Text(text = "- $txtConvocadas (${convocadas.size}/$maxPlayers)", style = MaterialTheme.typography.titleMedium, color = SuccessGreen)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(convocadas) { p ->
                    val dDisplay = p.dorsal?.takeIf { it.isNotBlank() } ?: "s.n."
                    val baseName = if (p.lastName.isNotBlank()) "${p.name} ${p.lastName}" else p.name
                    Row(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = dDisplay, style = MaterialTheme.typography.bodyLarge, fontSize = 19.sp, modifier = Modifier.width(42.dp), textAlign = TextAlign.Center)
                        Text(text = "-", style = MaterialTheme.typography.bodyLarge, fontSize = 19.sp, modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                        Text(text = baseName, style = MaterialTheme.typography.bodyLarge, fontSize = 19.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Start)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "- $txtDesconvocadas (${desconvocadas.size})", style = MaterialTheme.typography.titleMedium, color = AttendanceRed)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(desconvocadas) { p ->
                    val baseName = if (p.lastName.isNotBlank()) "${p.name} ${p.lastName}" else p.name

                    // Aquí estaba el error de Compose. Faltaba eliminar el atributo horizontalArrangement
                    Row(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, bottom = 8.dp, end = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "• $baseName", style = MaterialTheme.typography.bodyLarge, fontSize = 19.sp, modifier = Modifier.weight(1f))
                        Text(
                            text = match.unsummonedReasons[p.id.toString()]?.takeIf { it.isNotBlank() } ?: "Sin motivo",
                            style = MaterialTheme.typography.bodyMedium, fontSize = 16.sp, color = Color.DarkGray
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
                        modifier = Modifier.weight(1f),
                        enabled = !isSaving
                    ) { Text("Volver al Calendario", color = Color.White) }

                    Button(
                        onClick = {
                            shouldSaveDraft = true
                            summonedIds = match.summonedPlayers.toSet()
                            reasonsMap = match.unsummonedReasons.toMutableMap()
                            isEditMode = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AttendanceGreen),
                        modifier = Modifier.weight(1f),
                        enabled = !isSaving
                    ) { Text("Editar", color = Color.Black) }
                }

                Button(
                    onClick = { eliminarConvocatoria() },
                    colors = ButtonDefaults.buttonColors(containerColor = AttendanceRed),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSaving
                ) { Text(if (isSaving) "Guardando..." else "Eliminar Convocatoria", color = Color.White) }
            }

        } else {

            Text(text = "$txtSeleccionadas: ${summonedIds.size} (Mín. $minPlayers - Máx. $maxPlayers)", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            val listStateEdit = rememberLazyListState()

            LazyColumn(state = listStateEdit, modifier = Modifier.weight(1f).convocatoriaVerticalScrollShadow(listStateEdit)) {
                items(sortedPlayers) { player ->
                    val isSummoned = summonedIds.contains(player.id)
                    val containerColor = if (isSummoned) SuccessGreen else AttendanceRed

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(MaterialTheme.shapes.small).background(containerColor.copy(alpha = 0.2f)).clickable(enabled = !isSaving) {
                            if (isSummoned) {
                                playerToUnsummon = player
                            } else {
                                summonedIds = summonedIds + player.id
                                reasonsMap = reasonsMap.toMutableMap().apply { remove(player.id.toString()) }
                            }
                        }.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val dDisplay = player.dorsal?.takeIf { it.isNotBlank() } ?: "s.n."
                        val baseName = if (player.lastName.isNotBlank()) "${player.name} ${player.lastName}" else player.name

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = dDisplay, style = MaterialTheme.typography.bodyLarge, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = containerColor, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
                            Text(text = " · ", style = MaterialTheme.typography.bodyLarge, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = containerColor)
                            Text(text = baseName, style = MaterialTheme.typography.bodyLarge, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = containerColor)
                        }

                        if (isSummoned) {
                            Text(txtConvocado, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = containerColor)
                        } else {
                            Text(reasonsMap[player.id.toString()] ?: "", style = MaterialTheme.typography.bodyMedium, color = containerColor, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        if (match.isConvocatoriaSaved) {
                            shouldSaveDraft = false
                            summonedIds = match.summonedPlayers.toSet()
                            reasonsMap = match.unsummonedReasons.toMutableMap()
                            isEditMode = false
                        } else {
                            shouldSaveDraft = false
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AttendanceRed),
                    enabled = !isSaving
                ) { Text("Cancelar", color = Color.White) }

                Button(
                    onClick = {
                        if (isSaving) return@Button
                        when {
                            summonedIds.size > maxPlayers -> Toast.makeText(context, "Máximo $maxPlayers $txtJugadoras en esta categoría", Toast.LENGTH_LONG).show()
                            summonedIds.size < absoluteMinPlayers -> Toast.makeText(context, "Debes convocar mínimo $absoluteMinPlayers $txtJugadoras", Toast.LENGTH_LONG).show()
                            isInfantil && summonedIds.size in 5..7 -> showMinPlayersWarning = true
                            summonedIds.size < minPlayers -> Toast.makeText(context, "Debes convocar mínimo $minPlayers $txtJugadoras", Toast.LENGTH_LONG).show()
                            else -> saveConvocatoria()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AttendanceGreen),
                    enabled = !isSaving
                ) { Text(if (isSaving) "Guardando..." else "Guardar", color = Color.Black) }
            }
        }
    }

    if (showMinPlayersWarning) {
        AlertDialog(
            onDismissRequest = { if (!isSaving) showMinPlayersWarning = false },
            title = { Text("Aviso de Normativa", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold) },
            text = { Text("No tienes el número mínimo de $txtJugadoras para cumplir con la normativa (Mínimo 8). ¿Deseas guardar la convocatoria de todos modos?", textAlign = TextAlign.Justify) },
            confirmButton = {},
            dismissButton = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { showMinPlayersWarning = false }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = AttendanceRed), enabled = !isSaving) { Text("Revisar", color = Color.White) }
                    Button(onClick = { if (!isSaving) saveConvocatoria() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)), enabled = !isSaving) { Text(if (isSaving) "Guardando..." else "Continuar", color = Color.White) }
                }
            }
        )
    }

    if (playerToUnsummon != null) {
        AlertDialog(
            onDismissRequest = { if (!isSaving) playerToUnsummon = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.fillMaxWidth(0.95f).padding(16.dp),
            title = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Motivo de desconvocatoria:", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    val dDisplay = playerToUnsummon?.dorsal?.takeIf { it.isNotBlank() } ?: "s.n."
                    val baseName = if (playerToUnsummon?.lastName?.isNotBlank() == true) "${playerToUnsummon?.name} ${playerToUnsummon?.lastName}" else playerToUnsummon?.name ?: ""

                    Row(modifier = Modifier.align(Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = dDisplay, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.width(42.dp), textAlign = TextAlign.Center)
                        Text(text = " · ", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(text = baseName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    }
                }
            },
            text = {
                Column {
                    reasonOptions.forEach { reason ->
                        Button(
                            onClick = {
                                val player = playerToUnsummon ?: return@Button
                                reasonsMap = reasonsMap.toMutableMap().apply { put(player.id.toString(), reason) }
                                summonedIds = summonedIds - player.id
                                playerToUnsummon = null
                            },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AttendanceRed),
                            enabled = !isSaving
                        ) { Text(text = reason, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold) }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { playerToUnsummon = null }, enabled = !isSaving) { Text("Cancelar", color = Color.Gray) } }
        )
    }
}

fun Modifier.convocatoriaVerticalScrollShadow(listState: LazyListState) = this.drawWithContent {
    drawContent()
    val showTop = listState.canScrollBackward
    val showBottom = listState.canScrollForward
    val shadowHeight = 16.dp.toPx()

    if (showTop) {
        drawRect(brush = Brush.verticalGradient(colors = listOf(Color.Black.copy(alpha = 0.15f), Color.Transparent), startY = 0f, endY = shadowHeight), size = Size(size.width, shadowHeight))
    }
    if (showBottom) {
        drawRect(brush = Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.15f)), startY = size.height - shadowHeight, endY = size.height), topLeft = Offset(0f, size.height - shadowHeight), size = Size(size.width, shadowHeight))
    }
}