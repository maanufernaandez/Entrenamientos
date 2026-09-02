package com.example.entrenamientos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.entrenamientos.ui.BasketViewModel

@Composable
fun QuintetosScreen(viewModel: BasketViewModel, navController: NavController) {
    val selectedDateStr by viewModel.selectedDate.collectAsState()
    val date = java.time.LocalDate.parse(selectedDateStr)
    val match = viewModel.getMatchForDate(date) ?: return

    val players by viewModel.getPlayersForTeam(match.teamYear).collectAsState(initial = emptyList())
    val quintetosNote by viewModel.getTrainingNoteForDateAndTeam(selectedDateStr, match.teamYear, "QUINTETOS").collectAsState(initial = null)

    val teamsList by viewModel.teams.collectAsState()
    val matchTeam = teamsList.find { it.year == match.teamYear }
    val isFemale = matchTeam?.gender == "F"

    val txtSeleccionadas = if (isFemale) "Seleccionadas" else "Seleccionados"
    val txtJugadoras = if (isFemale) "jugadoras" else "jugadores"
    val txtActa = if (isFemale) "Acta de Cuartos / Jugadora" else "Acta de Cuartos / Jugador"

    val teamColor = matchTeam?.colorHex?.let {
        try { Color(android.graphics.Color.parseColor(it)) } catch (_: Exception) { Color.DarkGray }
    } ?: Color.DarkGray

    val context = androidx.compose.ui.platform.LocalContext.current

    val lineups = remember(quintetosNote?.content) {
        val content = quintetosNote?.content ?: ""
        if (content.isBlank()) emptyList()
        else content.split("|").map { quarter -> quarter.split(",").mapNotNull { it.toLongOrNull() } }
    }

    val playerSortComparator = compareBy<com.example.entrenamientos.data.Player> { it.dorsal.isNullOrBlank() }
        .thenBy { it.dorsal?.toIntOrNull() ?: 999 }

    val currentQuarter = lineups.size + 1
    val summonedPlayers = players.filter { match.summonedPlayers.contains(it.id) }.sortedWith(playerSortComparator)

    val forcedPlayersForQ3 = remember(currentQuarter, lineups, summonedPlayers) {
        if (currentQuarter == 3 && lineups.size == 2) {
            summonedPlayers.filter { p -> !lineups[0].contains(p.id) && !lineups[1].contains(p.id) }.map { it.id }.toSet()
        } else {
            emptySet()
        }
    }

    var currentSelection by remember(currentQuarter, forcedPlayersForQ3) {
        mutableStateOf(forcedPlayersForQ3)
    }

    var showTotalDialog by remember { mutableStateOf(false) }

    val dayOfWeek = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES")).replaceFirstChar { it.uppercase() }
    val monthName = date.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES")).replaceFirstChar { it.uppercase() }
    val matchDateFormatted = "$dayOfWeek ${date.dayOfMonth} de $monthName"

    androidx.activity.compose.BackHandler {
        navController.popBackStack()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Quintetos", style = MaterialTheme.typography.headlineMedium)
        Text("vs ${match.opponent} - $matchDateFormatted", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))

        if (currentQuarter <= 4) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (i in 0 until 4) {
                    val isCurrent = i == currentQuarter - 1
                    val isCompleted = i < currentQuarter - 1
                    val isFuture = i > currentQuarter - 1

                    val cardBgColor = when {
                        isCurrent -> Color.LightGray.copy(alpha = 0.3f)
                        isFuture -> Color.LightGray.copy(alpha = 0.5f)
                        else -> Color.LightGray.copy(alpha = 0.1f)
                    }

                    val cardBorder = when {
                        isCurrent -> androidx.compose.foundation.BorderStroke(2.dp, Color.Black)
                        isCompleted -> androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                        else -> null
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = cardBgColor),
                        border = cardBorder
                    ) {
                        Column(
                            modifier = Modifier.padding(4.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Q${i + 1}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = if (isCurrent) Color.Black else Color.DarkGray)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp), color = if (isCurrent) Color.Gray else Color.LightGray)

                            Column(modifier = Modifier.fillMaxWidth()) {
                                if (isCompleted) {
                                    val quarterPlayers = players.filter { lineups[i].contains(it.id) }.sortedWith(playerSortComparator)
                                    quarterPlayers.forEach { p ->
                                        val dDisplay = p.dorsal?.takeIf { it.isNotBlank() } ?: "s.n."
                                        Row(
                                            modifier = Modifier.fillMaxWidth().height(18.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = dDisplay, fontSize = 11.sp, modifier = Modifier.width(26.dp), textAlign = TextAlign.Center, color = Color.Black)
                                            Text(text = " · ", fontSize = 11.sp, color = Color.Black)
                                            Text(text = p.name, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color.Black)
                                        }
                                    }
                                } else {
                                    repeat(5) {
                                        val dotColor = if (isCurrent) Color.Black else Color.Gray.copy(alpha = 0.5f)
                                        val dotWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Normal
                                        Box(
                                            modifier = Modifier.fillMaxWidth().height(18.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "•",
                                                fontSize = 16.sp,
                                                fontWeight = dotWeight,
                                                color = dotColor
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            val quarterTitle = when (currentQuarter) {
                1 -> "Quinteto 1er Cuarto"
                2 -> "Quinteto 2º Cuarto"
                3 -> "Quinteto 3er Cuarto"
                4 -> "Quinteto 4º Cuarto"
                else -> "Quinteto"
            }
            Text(quarterTitle, style = MaterialTheme.typography.titleLarge)

            val counterColor = when {
                currentSelection.size == 5 -> com.example.entrenamientos.ui.theme.SuccessGreen
                currentSelection.size > 5 -> com.example.entrenamientos.ui.theme.AttendanceRed
                else -> Color.Gray
            }
            Text("$txtSeleccionadas: ${currentSelection.size}/5", style = MaterialTheme.typography.bodyMedium, color = counterColor, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            if (currentQuarter == 3 && forcedPlayersForQ3.size > 5) {
                Text("¡Error de rotación! Has dejado a más de 5 $txtJugadoras sin jugar en los primeros cuartos. No caben en pista. Pulsa 'Empezar de 0'.", color = com.example.entrenamientos.ui.theme.AttendanceRed, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
            }

            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                items(summonedPlayers) { player ->
                    val isSelected = currentSelection.contains(player.id)
                    val isForced = currentQuarter == 3 && forcedPlayersForQ3.contains(player.id)
                    val isBanned = currentQuarter == 3 && lineups.size >= 2 && lineups[0].contains(player.id) && lineups[1].contains(player.id)

                    val isWarning = when {
                        currentQuarter == 2 && lineups.isNotEmpty() -> lineups[0].contains(player.id)
                        currentQuarter == 3 && summonedPlayers.size > 10 && lineups.isNotEmpty() -> lineups[0].contains(player.id) || (lineups.size > 1 && lineups[1].contains(player.id))
                        else -> false
                    }

                    val containerColor = when {
                        isBanned -> com.example.entrenamientos.ui.theme.AttendanceRed
                        isSelected -> com.example.entrenamientos.ui.theme.SuccessGreen
                        isWarning -> Color(0xFFFFEBEE)
                        else -> Color.White
                    }

                    val cellShape = MaterialTheme.shapes.small
                    val cellHeight = 48.dp

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val dDisplay = player.dorsal?.takeIf { it.isNotBlank() } ?: "s.n."
                        val baseName = if (player.lastName.isNotBlank()) "${player.name} ${player.lastName}" else player.name
                        val quartersPlayed = lineups.count { it.contains(player.id) }
                        val nameDisplay = if (currentQuarter > 1) "$baseName ($quartersPlayed)" else baseName

                        Box(
                            modifier = Modifier
                                .width(48.dp)
                                .height(cellHeight)
                                .clip(cellShape)
                                .background(containerColor)
                                .clickable(enabled = !isBanned && !isForced) {
                                    if (isSelected) {
                                        currentSelection = currentSelection - player.id
                                    } else if (currentSelection.size < 5) {
                                        currentSelection = currentSelection + player.id
                                    } else {
                                        android.widget.Toast.makeText(context, "Ya has seleccionado 5 $txtJugadoras", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dDisplay,
                                fontSize = 17.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isBanned || isSelected) Color.White else Color.Black,
                                textAlign = TextAlign.Center
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(cellHeight)
                                .clip(cellShape)
                                .background(containerColor)
                                .clickable(enabled = !isBanned && !isForced) {
                                    if (isSelected) {
                                        currentSelection = currentSelection - player.id
                                    } else if (currentSelection.size < 5) {
                                        currentSelection = currentSelection + player.id
                                    } else {
                                        android.widget.Toast.makeText(context, "Ya has seleccionado 5 $txtJugadoras", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .padding(horizontal = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = nameDisplay,
                                    fontSize = 16.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isBanned || isSelected) Color.White else Color.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                if (isSelected) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillParentMaxHeight()
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Card(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.15f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray)
                            ) {
                                Column(modifier = Modifier.padding(8.dp).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceEvenly) {
                                    Text("1er Cuarto", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Black)
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.Gray.copy(alpha = 0.3f))
                                    players.filter { lineups[0].contains(it.id) }.sortedWith(playerSortComparator).forEach { p ->
                                        val dDisplay = p.dorsal?.takeIf { it.isNotBlank() } ?: "s.n."
                                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = dDisplay, fontSize = 16.sp, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center, color = Color.Black)
                                            Text(text = " · ", fontSize = 16.sp, color = Color.Black)
                                            Text(text = p.name, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color.Black)
                                        }
                                    }
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.15f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray)
                            ) {
                                Column(modifier = Modifier.padding(8.dp).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceEvenly) {
                                    Text("2º Cuarto", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Black)
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.Gray.copy(alpha = 0.3f))
                                    players.filter { lineups[1].contains(it.id) }.sortedWith(playerSortComparator).forEach { p ->
                                        val dDisplay = p.dorsal?.takeIf { it.isNotBlank() } ?: "s.n."
                                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = dDisplay, fontSize = 16.sp, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center, color = Color.Black)
                                            Text(text = " · ", fontSize = 16.sp, color = Color.Black)
                                            Text(text = p.name, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color.Black)
                                        }
                                    }
                                }
                            }
                        }
                        Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Card(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.15f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray)
                            ) {
                                Column(modifier = Modifier.padding(8.dp).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceEvenly) {
                                    Text("3er Cuarto", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Black)
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.Gray.copy(alpha = 0.3f))
                                    players.filter { lineups[2].contains(it.id) }.sortedWith(playerSortComparator).forEach { p ->
                                        val dDisplay = p.dorsal?.takeIf { it.isNotBlank() } ?: "s.n."
                                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = dDisplay, fontSize = 16.sp, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center, color = Color.Black)
                                            Text(text = " · ", fontSize = 16.sp, color = Color.Black)
                                            Text(text = p.name, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color.Black)
                                        }
                                    }
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.15f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray)
                            ) {
                                Column(modifier = Modifier.padding(8.dp).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceEvenly) {
                                    Text("4º Cuarto", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Black)
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.Gray.copy(alpha = 0.3f))
                                    players.filter { lineups[3].contains(it.id) }.sortedWith(playerSortComparator).forEach { p ->
                                        val dDisplay = p.dorsal?.takeIf { it.isNotBlank() } ?: "s.n."
                                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = dDisplay, fontSize = 16.sp, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center, color = Color.Black)
                                            Text(text = " · ", fontSize = 16.sp, color = Color.Black)
                                            Text(text = p.name, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color.Black)
                                        }
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = { showTotalDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = teamColor)
                        ) {
                            Text("Ver Total", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (currentQuarter <= 4) {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { navController.popBackStack() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                        modifier = Modifier.weight(1f)
                    ) { Text("Volver", color = Color.White, fontSize = 13.sp, maxLines = 1) }

                    Button(
                        onClick = {
                            val newLineups = lineups.toMutableList()
                            newLineups.add(currentSelection.toList())
                            val newContent = newLineups.joinToString("|") { q -> q.joinToString(",") }
                            viewModel.saveTrainingNote(selectedDateStr, match.teamYear, "QUINTETOS", newContent, quintetosNote)
                            currentSelection = setOf()
                        },
                        enabled = currentSelection.size == 5,
                        colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceGreen),
                        modifier = Modifier.weight(1f)
                    ) { Text(if (currentQuarter < 4) "Siguiente" else "Guardar", color = Color.Black, fontSize = 13.sp, maxLines = 1) }
                }

                Button(
                    onClick = {
                        viewModel.saveTrainingNote(selectedDateStr, match.teamYear, "QUINTETOS", "", quintetosNote)
                        currentSelection = setOf()
                    },
                    enabled = lineups.isNotEmpty() || currentSelection.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceRed),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Empezar de 0", color = Color.White, fontSize = 13.sp, maxLines = 1) }
            }
        } else {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { navController.popBackStack() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                    modifier = Modifier.weight(1f)
                ) { Text("Volver", color = Color.White, fontSize = 13.sp, maxLines = 1) }

                Button(
                    onClick = {
                        viewModel.saveTrainingNote(selectedDateStr, match.teamYear, "QUINTETOS", "", quintetosNote)
                        currentSelection = setOf()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceRed),
                    modifier = Modifier.weight(1f)
                ) { Text("Empezar de 0", color = Color.White, fontSize = 13.sp, maxLines = 1) }
            }
        }
    }

    if (showTotalDialog) {
        AlertDialog(
            onDismissRequest = { showTotalDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.fillMaxWidth(0.98f).padding(16.dp),
            title = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(txtActa, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Black)
                    HorizontalDivider(modifier = Modifier.padding(top = 8.dp), color = Color.Black, thickness = 1.dp)
                }
            },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    val sortedPlayersDialog = summonedPlayers.sortedWith(playerSortComparator)

                    items(sortedPlayersDialog) { player ->
                        val dDisplay = player.dorsal?.takeIf { it.isNotBlank() } ?: "s.n."
                        val baseName = if (player.lastName.isNotBlank()) "${player.name} ${player.lastName}" else player.name

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = baseName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Black,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = dDisplay,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(36.dp),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(0.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                for (qIndex in 0 until 4) {
                                    val playedInQuarter = lineups.getOrNull(qIndex)?.contains(player.id) == true

                                    val modifierCell = if (qIndex == 0) {
                                        Modifier
                                            .size(26.dp)
                                            .border(1.dp, Color.DarkGray)
                                            .background(Color.White)
                                    } else {
                                        Modifier
                                            .size(26.dp)
                                            .offset(x = (-1 * qIndex).dp)
                                            .border(1.dp, Color.DarkGray)
                                            .background(Color.White)
                                    }

                                    Box(
                                        modifier = modifierCell,
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (playedInQuarter) {
                                            Text(
                                                text = "X",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.Black,
                                                fontSize = 17.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                Button(
                    onClick = { showTotalDialog = false },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Text("Cerrar", color = Color.White)
                }
            }
        )
    }
}