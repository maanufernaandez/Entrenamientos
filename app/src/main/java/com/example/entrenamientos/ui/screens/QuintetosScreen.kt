package com.example.entrenamientos.ui.screens

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
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
    val teamYear by viewModel.selectedTeamYear.collectAsState()
    val date = java.time.LocalDate.parse(selectedDateStr)

    val allMatches by viewModel.matches.collectAsState()
    val match = allMatches.find { it.date == selectedDateStr && it.teamYear == teamYear } ?: return

    val players by viewModel.getPlayersForTeam(match.teamYear).collectAsState(initial = emptyList())
    val quintetosNote by viewModel.getTrainingNoteForDateAndTeam(selectedDateStr, match.teamYear, "QUINTETOS").collectAsState(initial = null)

    val teamsList by viewModel.teams.collectAsState()
    val matchTeam = teamsList.find { it.year == match.teamYear }
    val isFemale = matchTeam?.gender == "F"
    val category = matchTeam?.categoryYear ?: ""

    val isSeniorCategory = category.startsWith("Cadete") || category.startsWith("Junior") || category.startsWith("Senior")
    val isInfantil = category.startsWith("Infantil") || category.startsWith("Preinfantil")
    val isMini = category.startsWith("Minibasket") || category.startsWith("PreMinibasket") || category.startsWith("Benjamin 5x5")
    val is3x3 = category.startsWith("Benjamin 3x3") || category.startsWith("Pre-Benjamin 3x3")

    val totalQuarters = when {
        isSeniorCategory -> 1
        isInfantil -> 4
        isMini -> 6
        is3x3 -> 8
        else -> 4
    }

    val playersPerQuarter = if (is3x3) 3 else 5

    val txtSeleccionadas = if (isFemale) "Seleccionadas" else "Seleccionados"
    val txtJugadoras = if (isFemale) "jugadoras" else "jugadores"
    val txtActa = if (isFemale) "Acta de Cuartos / Jugadora" else "Acta de Cuartos / Jugador"
    val prefixText = if (is3x3) "Trío" else "Quinteto"

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

    val forcedPlayers = remember(currentQuarter, lineups, summonedPlayers, isInfantil, isMini) {
        if (isInfantil && currentQuarter == 3 && lineups.size == 2) {
            summonedPlayers.filter { p -> !lineups[0].contains(p.id) && !lineups[1].contains(p.id) }.map { it.id }.toSet()
        } else if (isMini) {
            val forced = mutableSetOf<Long>()
            summonedPlayers.forEach { p ->
                val qPlayed = lineups.count { it.contains(p.id) }
                if (currentQuarter == 5 && qPlayed == 0) forced.add(p.id)
                if (currentQuarter == 6 && qPlayed < 2) forced.add(p.id)
            }
            forced
        } else {
            emptySet()
        }
    }

    var currentSelection by remember(currentQuarter, forcedPlayers) {
        mutableStateOf(forcedPlayers)
    }

    var showTotalDialog by remember { mutableStateOf(false) }

    val dayOfWeek = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES")).replaceFirstChar { it.uppercase() }
    val monthName = date.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES")).replaceFirstChar { it.uppercase() }
    val matchDateFormatted = "$dayOfWeek ${date.dayOfMonth} de $monthName"

    androidx.activity.compose.BackHandler {
        navController.popBackStack()
    }

    val renderQuarterBox: @Composable (Int, Modifier) -> Unit = { i, modifier ->
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
            modifier = modifier,
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
                                Text(text = dDisplay, fontSize = 11.sp, modifier = Modifier.width(22.dp), textAlign = TextAlign.Center, color = Color.Black)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "${p.name} ${p.lastName}".trim(), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color.Black, modifier = Modifier.weight(1f), textAlign = TextAlign.Start)
                            }
                        }
                    } else {
                        repeat(playersPerQuarter) {
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

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(if (isSeniorCategory) "Quinteto" else "Quintetos", style = MaterialTheme.typography.headlineMedium)
        Text("vs ${match.opponent} - $matchDateFormatted", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))

        if (isSeniorCategory) {
            if (lineups.isEmpty()) {
                Text("5 Inicial", style = MaterialTheme.typography.titleLarge)
                val counterColor = when {
                    currentSelection.size == 5 -> com.example.entrenamientos.ui.theme.SuccessGreen
                    currentSelection.size > 5 -> com.example.entrenamientos.ui.theme.AttendanceRed
                    else -> Color.Gray
                }
                Text("$txtSeleccionadas: ${currentSelection.size}/5", style = MaterialTheme.typography.bodyMedium, color = counterColor, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                val listStateSenior = rememberLazyListState()

                LazyColumn(
                    state = listStateSenior,
                    modifier = Modifier.weight(1f).fillMaxWidth().verticalScrollShadow(listStateSenior)
                ) {
                    items(summonedPlayers) { player ->
                        val isSelected = currentSelection.contains(player.id)
                        val containerColor = if (isSelected) com.example.entrenamientos.ui.theme.SuccessGreen else Color.White
                        val cellShape = MaterialTheme.shapes.small
                        val cellHeight = 48.dp

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val dDisplay = player.dorsal?.takeIf { it.isNotBlank() } ?: "s.n."
                            val baseName = if (player.lastName.isNotBlank()) "${player.name} ${player.lastName}" else player.name

                            Box(
                                modifier = Modifier.width(48.dp).height(cellHeight).clip(cellShape).background(containerColor)
                                    .clickable {
                                        if (isSelected) currentSelection = currentSelection - player.id
                                        else if (currentSelection.size < 5) currentSelection = currentSelection + player.id
                                        else android.widget.Toast.makeText(context, "Ya has seleccionado 5 $txtJugadoras", android.widget.Toast.LENGTH_SHORT).show()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(dDisplay, fontSize = 17.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) Color.White else Color.Black, textAlign = TextAlign.Center)
                            }

                            Box(
                                modifier = Modifier.weight(1f).height(cellHeight).clip(cellShape).background(containerColor)
                                    .clickable {
                                        if (isSelected) currentSelection = currentSelection - player.id
                                        else if (currentSelection.size < 5) currentSelection = currentSelection + player.id
                                        else android.widget.Toast.makeText(context, "Ya has seleccionado 5 $txtJugadoras", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(baseName, fontSize = 16.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) Color.White else Color.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { navController.popBackStack() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                        modifier = Modifier.weight(1f)
                    ) { Text("Volver", color = Color.White, fontSize = 13.sp, maxLines = 1) }

                    Button(
                        onClick = {
                            val newContent = currentSelection.joinToString(",")
                            viewModel.saveTrainingNote(selectedDateStr, match.teamYear, "QUINTETOS", newContent, quintetosNote)
                            currentSelection = setOf()
                        },
                        enabled = currentSelection.size == 5,
                        colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceGreen),
                        modifier = Modifier.weight(1f)
                    ) { Text("Guardar", color = Color.Black, fontSize = 13.sp, maxLines = 1) }
                }

            } else {
                val titulares = players.filter { lineups[0].contains(it.id) }.sortedWith(playerSortComparator)
                val suplentes = summonedPlayers.filter { !lineups[0].contains(it.id) }.sortedWith(playerSortComparator)

                val listStateSeniorRes = rememberLazyListState()

                LazyColumn(
                    state = listStateSeniorRes,
                    modifier = Modifier.weight(1f).fillMaxWidth().verticalScrollShadow(listStateSeniorRes)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.15f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray)
                        ) {
                            Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                                Text("5 Inicial", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Black)
                                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color.Gray.copy(alpha = 0.3f))
                                titulares.forEach { p ->
                                    val dDisplay = p.dorsal?.takeIf { it.isNotBlank() } ?: "s.n."
                                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = dDisplay, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center, color = Color.Black)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = "${p.name} ${p.lastName}".trim(), fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color.Black, modifier = Modifier.weight(1f), textAlign = TextAlign.Start)
                                    }
                                }
                            }
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.05f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                        ) {
                            Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                                Text("Suplentes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color.LightGray)
                                suplentes.forEach { p ->
                                    val dDisplay = p.dorsal?.takeIf { it.isNotBlank() } ?: "s.n."
                                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = dDisplay, fontSize = 16.sp, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center, color = Color.DarkGray)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = "${p.name} ${p.lastName}".trim(), fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color.DarkGray, modifier = Modifier.weight(1f), textAlign = TextAlign.Start)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

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
        } else {
            if (currentQuarter <= totalQuarters) {
                if (totalQuarters == 4) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (i in 0 until 4) {
                            renderQuarterBox(i, Modifier.weight(1f))
                        }
                    }
                } else if (totalQuarters == 8 && is3x3) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            for (i in 0 until 4) {
                                renderQuarterBox(i, Modifier.weight(1f))
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            for (i in 4 until 8) {
                                renderQuarterBox(i, Modifier.weight(1f))
                            }
                        }
                    }
                } else {
                    val configuration = LocalConfiguration.current
                    val screenWidth = configuration.screenWidthDp.dp
                    val cardWidth = (screenWidth - 32.dp - 12.dp) / 4
                    val quartersScrollState = rememberScrollState()

                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .horizontalScrollShadow(quartersScrollState)
                            .horizontalScroll(quartersScrollState),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (i in 0 until totalQuarters) {
                            renderQuarterBox(i, Modifier.width(cardWidth))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val quarterTitle = "$prefixText ${currentQuarter}º Cuarto"
                Text(quarterTitle, style = MaterialTheme.typography.titleLarge)

                val counterColor = when {
                    currentSelection.size == playersPerQuarter -> com.example.entrenamientos.ui.theme.SuccessGreen
                    currentSelection.size > playersPerQuarter -> com.example.entrenamientos.ui.theme.AttendanceRed
                    else -> Color.Gray
                }
                Text("$txtSeleccionadas: ${currentSelection.size}/$playersPerQuarter", style = MaterialTheme.typography.bodyMedium, color = counterColor, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                if (isInfantil && currentQuarter == 3 && forcedPlayers.size > playersPerQuarter) {
                    Text("¡Error de rotación! Has dejado a más de $playersPerQuarter $txtJugadoras sin jugar en los primeros cuartos. No caben en pista. Pulsa 'Empezar de 0'.", color = com.example.entrenamientos.ui.theme.AttendanceRed, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (isMini && currentQuarter == 5 && forcedPlayers.size > playersPerQuarter) {
                    Text("¡Error de rotación! Has dejado a demasiadas jugadoras con 0 cuartos y deben jugar 2 mínimos. Pulsa 'Empezar de 0'.", color = com.example.entrenamientos.ui.theme.AttendanceRed, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (isMini && currentQuarter == 6 && forcedPlayers.size > playersPerQuarter) {
                    Text("¡Error de rotación! Has dejado a demasiadas jugadoras con 1 cuarto y deben jugar 2 mínimos. Pulsa 'Empezar de 0'.", color = com.example.entrenamientos.ui.theme.AttendanceRed, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                val selectionListState = rememberLazyListState()

                LazyColumn(
                    state = selectionListState,
                    modifier = Modifier.weight(1f).fillMaxWidth().verticalScrollShadow(selectionListState)
                ) {
                    items(summonedPlayers) { player ->
                        val isSelected = currentSelection.contains(player.id)
                        val isForced = forcedPlayers.contains(player.id)

                        val quartersPlayedTotal = lineups.count { it.contains(player.id) }

                        val isBanned = when {
                            is3x3 -> false
                            isMini -> {
                                val qFirst5 = if (currentQuarter <= 5) quartersPlayedTotal else lineups.take(5).count { it.contains(player.id) }

                                if (currentQuarter <= 5) {
                                    if (summonedPlayers.size == 8) {
                                        if (quartersPlayedTotal >= 4) {
                                            true
                                        } else if (quartersPlayedTotal == 3) {
                                            summonedPlayers.any { p ->
                                                p.id != player.id && ((lineups.count { it.contains(p.id) } + if (currentSelection.contains(p.id)) 1 else 0) >= 4)
                                            }
                                        } else false
                                    } else {
                                        quartersPlayedTotal >= 3
                                    }
                                } else if (currentQuarter == 6) {
                                    if (summonedPlayers.size == 8) {
                                        qFirst5 >= 4
                                    } else if (summonedPlayers.size in 13..15) {
                                        if (quartersPlayedTotal >= 3) {
                                            summonedPlayers.any { p ->
                                                p.id != player.id && ((lineups.count { it.contains(p.id) } + if (currentSelection.contains(p.id)) 1 else 0) < 3)
                                            }
                                        } else false
                                    } else false
                                } else false
                            }
                            isInfantil -> currentQuarter == 3 && lineups.size >= 2 && lineups[0].contains(player.id) && lineups[1].contains(player.id)
                            else -> false
                        }

                        val isWarning = when {
                            is3x3 -> false
                            isMini -> false
                            isInfantil -> currentQuarter == 2 && lineups.isNotEmpty() && lineups[0].contains(player.id) ||
                                    currentQuarter == 3 && summonedPlayers.size > 10 && lineups.isNotEmpty() && (lineups[0].contains(player.id) || (lineups.size > 1 && lineups[1].contains(player.id)))
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
                            val nameDisplay = if (currentQuarter > 1) "$baseName ($quartersPlayedTotal)" else baseName

                            Box(
                                modifier = Modifier
                                    .width(48.dp)
                                    .height(cellHeight)
                                    .clip(cellShape)
                                    .background(containerColor)
                                    .clickable(enabled = !isBanned && !isForced) {
                                        if (isSelected) {
                                            currentSelection = currentSelection - player.id
                                        } else if (currentSelection.size < playersPerQuarter) {
                                            currentSelection = currentSelection + player.id
                                        } else {
                                            android.widget.Toast.makeText(context, "Ya has seleccionado $playersPerQuarter $txtJugadoras", android.widget.Toast.LENGTH_SHORT).show()
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
                                        } else if (currentSelection.size < playersPerQuarter) {
                                            currentSelection = currentSelection + player.id
                                        } else {
                                            android.widget.Toast.makeText(context, "Ya has seleccionado $playersPerQuarter $txtJugadoras", android.widget.Toast.LENGTH_SHORT).show()
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
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
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
                            enabled = currentSelection.size == playersPerQuarter,
                            colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceGreen),
                            modifier = Modifier.weight(1f)
                        ) { Text(if (currentQuarter < totalQuarters) "Siguiente" else "Guardar", color = Color.Black, fontSize = 13.sp, maxLines = 1) }
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
                val finalResListState = rememberLazyListState()

                LazyColumn(
                    state = finalResListState,
                    modifier = Modifier.weight(1f).verticalScrollShadow(finalResListState),
                    verticalArrangement = Arrangement.Center
                ) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val chunkedQuarters = (0 until totalQuarters).chunked(2)

                            chunkedQuarters.forEach { pair ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    pair.forEach { qIndex ->
                                        Card(
                                            modifier = Modifier.weight(1f),
                                            colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.15f)),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray)
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceEvenly) {
                                                Text("${qIndex + 1}º Cuarto", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Black)
                                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color.Gray.copy(alpha = 0.3f))
                                                players.filter { lineups[qIndex].contains(it.id) }.sortedWith(playerSortComparator).forEach { p ->
                                                    val dDisplay = p.dorsal?.takeIf { it.isNotBlank() } ?: "s.n."
                                                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                                        Text(text = dDisplay, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center, color = Color.Black)
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(text = "${p.name} ${p.lastName}".trim(), fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color.Black, modifier = Modifier.weight(1f), textAlign = TextAlign.Start)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (pair.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
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

                Spacer(modifier = Modifier.height(16.dp))
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
                val dialogListState = rememberLazyListState()

                LazyColumn(
                    state = dialogListState,
                    modifier = Modifier.fillMaxWidth().verticalScrollShadow(dialogListState)
                ) {
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
                            if (is3x3 || isMini) {
                                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = dDisplay,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.width(36.dp),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = baseName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Black,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                val quartersPlayed = lineups.count { it.contains(player.id) }
                                val qText = if (quartersPlayed == 1) "1 cuarto" else "$quartersPlayed cuartos"

                                Text(
                                    text = "($qText)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.DarkGray
                                )
                            } else {
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

                                val playerScrollState = rememberScrollState()

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .horizontalScrollShadow(playerScrollState)
                                        .horizontalScroll(playerScrollState)
                                ) {
                                    for (qIndex in 0 until totalQuarters) {
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

// Extensión para sombras en Scrolls Horizontales
fun Modifier.horizontalScrollShadow(scrollState: ScrollState) = this.drawWithContent {
    drawContent()
    val showStart = scrollState.canScrollBackward
    val showEnd = scrollState.canScrollForward
    val shadowWidth = 16.dp.toPx()

    if (showStart) {
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Black.copy(alpha = 0.15f), Color.Transparent),
                startX = 0f,
                endX = shadowWidth
            ),
            size = Size(shadowWidth, size.height)
        )
    }
    if (showEnd) {
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.15f)),
                startX = size.width - shadowWidth,
                endX = size.width
            ),
            topLeft = Offset(size.width - shadowWidth, 0f),
            size = Size(shadowWidth, size.height)
        )
    }
}