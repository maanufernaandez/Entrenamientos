package com.example.entrenamientos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.entrenamientos.data.Player
import com.example.entrenamientos.ui.BasketViewModel

@Composable
fun StatsScreen(viewModel: BasketViewModel) {
    val teamsList by viewModel.teams.collectAsState()
    val selectedTeam by viewModel.selectedTeamYear.collectAsState()

    LaunchedEffect(teamsList) {
        if (teamsList.isNotEmpty() && teamsList.none { it.year == selectedTeam }) {
            viewModel.setSelectedTeamYear(teamsList.first().year)
        }
    }

    val activeTeamObj = teamsList.find { it.year == selectedTeam }
    val trackMatches = activeTeamObj?.trackMatches ?: true
    val isFemale = activeTeamObj?.gender == "F"
    val labelJugadoras = if (isFemale) "Jugadoras" else "Jugadores"

    val attendances by viewModel.getAllAttendancesByTeam(selectedTeam).collectAsState(initial = emptyList())
    val allMatches by viewModel.matches.collectAsState()
    val teamMatches = allMatches.filter { it.teamYear == selectedTeam }
    val players by viewModel.getPlayersForTeam(selectedTeam).collectAsState(initial = emptyList())

    val expandedAttendanceMonths = remember { mutableStateMapOf<java.time.YearMonth, Boolean>() }
    val expandedMatchMonths = remember { mutableStateMapOf<java.time.YearMonth, Boolean>() }
    val expandedMatchDetails = remember { mutableStateMapOf<Long, Boolean>() }
    var isSeasonAttendanceExpanded by remember { mutableStateOf(false) }
    var isSeasonStatsExpanded by remember { mutableStateOf(false) }
    var isSeasonMatchesExpanded by remember { mutableStateOf(false) }
    val expandedWeekDetails = remember { mutableStateMapOf<java.time.LocalDate, Boolean>() }

    data class PlayerAttendanceCount(
        val player: Player,
        val present: Int,
        val justified: Int,
        val unjustified: Int
    )

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("PANEL DE ESTADÍSTICAS", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    teamsList.forEach { team ->
                        val teamColor = team.colorHex.let {
                            try { Color(android.graphics.Color.parseColor(it)) } catch (_: Exception) { Color.Gray }
                        }
                        val isTeamSelected = selectedTeam == team.year
                        Button(
                            onClick = { viewModel.setSelectedTeamYear(team.year) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isTeamSelected) teamColor else teamColor.copy(alpha = 0.25f)
                            )
                        ) {
                            Text(
                                team.shortName.ifBlank { team.name.take(6).uppercase() },
                                color = if (isTeamSelected) Color.White else Color.Black
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Text("Asistencia Mensual / Semanal", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (attendances.isNotEmpty()) {
                item {
                    val activeColor = activeTeamObj?.colorHex?.let {
                        try { Color(android.graphics.Color.parseColor(it)) } catch (_: Exception) { Color.Black }
                    } ?: Color.Black

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                            .clickable { isSeasonAttendanceExpanded = !isSeasonAttendanceExpanded }
                            .padding(vertical = 4.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("TEMPORADA", style = MaterialTheme.typography.titleMedium, color = activeColor)
                        Icon(
                            imageVector = if (isSeasonAttendanceExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expandir/Colapsar",
                            tint = Color.Gray
                        )
                    }
                }

                if (isSeasonAttendanceExpanded) {
                    val sortedPlayersByAttendance = players.map { player ->
                        val pAtts = attendances.filter { it.playerId == player.id }
                        PlayerAttendanceCount(
                            player = player,
                            present = pAtts.count { it.status == 0 },
                            justified = pAtts.count { it.status == 1 },
                            unjustified = pAtts.count { it.status == 2 }
                        )
                    }.sortedByDescending { it.present }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                if (sortedPlayersByAttendance.isEmpty()) {
                                    Text("No hay registros.", style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
                                } else {
                                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        Text(labelJugadoras, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Row {
                                            Box(modifier = Modifier.width(36.dp), contentAlignment = Alignment.Center) {
                                                Box(modifier = Modifier.size(12.dp).background(com.example.entrenamientos.ui.theme.AttendanceGreen, MaterialTheme.shapes.extraSmall))
                                            }
                                            Box(modifier = Modifier.width(36.dp), contentAlignment = Alignment.Center) {
                                                Box(modifier = Modifier.size(12.dp).background(com.example.entrenamientos.ui.theme.AttendanceYellow, MaterialTheme.shapes.extraSmall))
                                            }
                                            Box(modifier = Modifier.width(36.dp), contentAlignment = Alignment.Center) {
                                                Box(modifier = Modifier.size(12.dp).background(com.example.entrenamientos.ui.theme.AttendanceRed, MaterialTheme.shapes.extraSmall))
                                            }
                                        }
                                    }
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color.Black, thickness = 1.dp)

                                    sortedPlayersByAttendance.forEachIndexed { index, entry ->
                                        val displayName = if (entry.player.lastName.isNotBlank()) "${entry.player.name} ${entry.player.lastName}" else entry.player.name
                                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Text(displayName, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                                            Row {
                                                Box(modifier = Modifier.width(36.dp), contentAlignment = Alignment.Center) {
                                                    Text("x${entry.present}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                                }
                                                Box(modifier = Modifier.width(36.dp), contentAlignment = Alignment.Center) {
                                                    Text("x${entry.justified}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                                }
                                                Box(modifier = Modifier.width(36.dp), contentAlignment = Alignment.Center) {
                                                    Text("x${entry.unjustified}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                        if (index < sortedPlayersByAttendance.size - 1) {
                                            HorizontalDivider(color = Color.Black.copy(alpha = 0.2f), thickness = 1.dp)
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            val attendancesByWeek = attendances.groupBy {
                java.time.LocalDate.parse(it.date).with(java.time.DayOfWeek.MONDAY)
            }

            val monthToWeeksMap = mutableMapOf<java.time.YearMonth, MutableList<Pair<java.time.LocalDate, List<com.example.entrenamientos.data.Attendance>>>>()

            attendancesByWeek.forEach { (weekStart, weekAtts) ->
                val targetMonth = (0..6)
                    .map { offset -> java.time.YearMonth.from(weekStart.plusDays(offset.toLong())) }
                    .groupingBy { it }
                    .eachCount()
                    .maxByOrNull { it.value }!!
                    .key
                monthToWeeksMap.getOrPut(targetMonth) { mutableListOf() }.add(weekStart to weekAtts)
            }

            val sortedMonths = monthToWeeksMap.toSortedMap(compareByDescending { it })
            val formatterMonth = java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy", java.util.Locale("es", "ES"))

            if (sortedMonths.isEmpty()) {
                item { Text("No hay datos de asistencia todavía.", color = Color.Gray) }
            }

            sortedMonths.forEach { (month, weeks) ->
                val isExpanded = expandedAttendanceMonths[month] ?: false

                item {
                    val activeColor = activeTeamObj?.colorHex?.let {
                        try { Color(android.graphics.Color.parseColor(it)) } catch (_: Exception) { Color.Black }
                    } ?: Color.Black
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                            .clickable { expandedAttendanceMonths[month] = !isExpanded }
                            .padding(vertical = 4.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = month.format(formatterMonth).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            color = activeColor
                        )
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expandir/Colapsar",
                            tint = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                if (isExpanded) {
                    val sortedWeeks = weeks.sortedByDescending { it.first }
                    sortedWeeks.forEach { (weekStart, weekAttendances) ->
                        val weekEnd = weekStart.plusDays(4)
                        val isWeekExpanded = expandedWeekDetails[weekStart] ?: false

                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.small)
                                    .clickable { expandedWeekDetails[weekStart] = !isWeekExpanded }
                                    .padding(vertical = 6.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Semana del ${weekStart.dayOfMonth} al ${weekEnd.dayOfMonth} de ${weekEnd.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES")).lowercase()}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = if (isWeekExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Expandir/Colapsar",
                                    tint = Color.Gray
                                )
                            }
                        }

                        if (isWeekExpanded) {
                            val sortedPlayersByWeekAttendance = players.map { player ->
                                val pAtts = weekAttendances.filter { it.playerId == player.id }
                                PlayerAttendanceCount(
                                    player = player,
                                    present = pAtts.count { it.status == 0 },
                                    justified = pAtts.count { it.status == 1 },
                                    unjustified = pAtts.count { it.status == 2 }
                                )
                            }.sortedByDescending { it.present }

                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.2f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        if (sortedPlayersByWeekAttendance.isEmpty()) {
                                            Text("No hay registros.", style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
                                        } else {
                                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                                Text(labelJugadoras, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                                Row {
                                                    Box(modifier = Modifier.width(36.dp), contentAlignment = Alignment.Center) {
                                                        Box(modifier = Modifier.size(12.dp).background(com.example.entrenamientos.ui.theme.AttendanceGreen, MaterialTheme.shapes.extraSmall))
                                                    }
                                                    Box(modifier = Modifier.width(36.dp), contentAlignment = Alignment.Center) {
                                                        Box(modifier = Modifier.size(12.dp).background(com.example.entrenamientos.ui.theme.AttendanceYellow, MaterialTheme.shapes.extraSmall))
                                                    }
                                                    Box(modifier = Modifier.width(36.dp), contentAlignment = Alignment.Center) {
                                                        Box(modifier = Modifier.size(12.dp).background(com.example.entrenamientos.ui.theme.AttendanceRed, MaterialTheme.shapes.extraSmall))
                                                    }
                                                }
                                            }
                                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color.Black, thickness = 1.dp)

                                            sortedPlayersByWeekAttendance.forEachIndexed { index, entry ->
                                                val displayName = if (entry.player.lastName.isNotBlank()) "${entry.player.name} ${entry.player.lastName}" else entry.player.name
                                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                                    Text(displayName, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                                                    Row {
                                                        Box(modifier = Modifier.width(36.dp), contentAlignment = Alignment.Center) {
                                                            Text("x${entry.present}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                                        }
                                                        Box(modifier = Modifier.width(36.dp), contentAlignment = Alignment.Center) {
                                                            Text("x${entry.justified}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                                        }
                                                        Box(modifier = Modifier.width(36.dp), contentAlignment = Alignment.Center) {
                                                            Text("x${entry.unjustified}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                                if (index < sortedPlayersByWeekAttendance.size - 1) {
                                                    HorizontalDivider(color = Color.Black.copy(alpha = 0.2f), thickness = 1.dp)
                                                }
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }

            if (trackMatches) {
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    Text("Medias de Temporada", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    val activeColor = activeTeamObj?.colorHex?.let {
                        try { Color(android.graphics.Color.parseColor(it)) } catch (_: Exception) { Color.Black }
                    } ?: Color.Black

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                            .clickable { isSeasonStatsExpanded = !isSeasonStatsExpanded }
                            .padding(vertical = 4.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("TEMPORADA", style = MaterialTheme.typography.titleMedium, color = activeColor)
                        Icon(
                            imageVector = if (isSeasonStatsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expandir/Colapsar",
                            tint = Color.Gray
                        )
                    }
                }

                if (isSeasonStatsExpanded) {
                    val playedMatches = teamMatches.filter { it.resultLocal != null && it.resultVisitor != null }

                    if (playedMatches.isEmpty()) {
                        item { Text("No existen datos de ningún partido", color = Color.Gray, modifier = Modifier.padding(horizontal = 8.dp)) }
                    } else {
                        var totalWins = 0
                        var totalLosses = 0
                        var localWins = 0
                        var localLosses = 0
                        var visitorWins = 0
                        var visitorLosses = 0
                        var totalScored = 0
                        var totalReceived = 0
                        var totalFtMade = 0
                        var totalFtAttempted = 0

                        playedMatches.forEach { m ->
                            val localScore = m.resultLocal ?: 0
                            val visitorScore = m.resultVisitor ?: 0

                            if (m.isLocal) {
                                totalScored += localScore
                                totalReceived += visitorScore
                                if (localScore > visitorScore) {
                                    totalWins++; localWins++
                                } else if (localScore < visitorScore) {
                                    totalLosses++; localLosses++
                                }
                            } else {
                                totalScored += visitorScore
                                totalReceived += localScore
                                if (visitorScore > localScore) {
                                    totalWins++; visitorWins++
                                } else if (visitorScore < localScore) {
                                    totalLosses++; visitorLosses++
                                }
                            }
                            totalFtMade += m.ftMade
                            totalFtAttempted += m.ftAttempted
                        }

                        val avgScored = if (playedMatches.isNotEmpty()) totalScored.toFloat() / playedMatches.size else 0f
                        val avgReceived = if (playedMatches.isNotEmpty()) totalReceived.toFloat() / playedMatches.size else 0f
                        val ftPercentage = if (totalFtAttempted > 0) (totalFtMade.toFloat() / totalFtAttempted) * 100 else 0f

                        val fmtScored = String.format(java.util.Locale.US, "%.1f", avgScored).removeSuffix(".0")
                        val fmtReceived = String.format(java.util.Locale.US, "%.1f", avgReceived).removeSuffix(".0")
                        val fmtFt = String.format(java.util.Locale.US, "%.1f", ftPercentage).removeSuffix(".0")

                        item {
                            val activeColor = activeTeamObj?.colorHex?.let {
                                try { Color(android.graphics.Color.parseColor(it)) } catch (_: Exception) { Color.Black }
                            } ?: Color.Black

                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = activeColor.copy(alpha = 0.15f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {

                                    val statRows = listOf(
                                        "Resultados:" to "$totalWins / $totalLosses",
                                        "Resultados (Local):" to "$localWins / $localLosses",
                                        "Resultados (Visitante):" to "$visitorWins / $visitorLosses",
                                        "PF / Partido:" to fmtScored,
                                        "PC / Partido:" to fmtReceived,
                                        "TL (%):" to "$fmtFt ($totalFtMade/$totalFtAttempted)"
                                    )

                                    statRows.forEach { (label, value) ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = label,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                text = value,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.width(95.dp),
                                                textAlign = TextAlign.Start
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    Text("Resumen de Convocatorias", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                val matchesWithConvocatoria = teamMatches.filter { it.isConvocatoriaSaved }

                item {
                    val activeColor = activeTeamObj?.colorHex?.let {
                        try { Color(android.graphics.Color.parseColor(it)) } catch (_: Exception) { Color.Black }
                    } ?: Color.Black

                    Row(
                        modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.small).clickable { isSeasonMatchesExpanded = !isSeasonMatchesExpanded }.padding(vertical = 4.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("TEMPORADA", style = MaterialTheme.typography.titleMedium, color = activeColor)
                        Icon(imageVector = if (isSeasonMatchesExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = "Expandir/Colapsar", tint = Color.Gray)
                    }
                }

                if (isSeasonMatchesExpanded) {
                    val unsummonedStats: Map<Long, Map<String, Int>> = run {
                        val stats = mutableMapOf<Long, MutableMap<String, Int>>()
                        matchesWithConvocatoria.forEach { match ->
                            match.unsummonedReasons.forEach { (playerId, reason) ->
                                val playerStats = stats.getOrPut(playerId) { mutableMapOf() }
                                playerStats[reason] = playerStats.getOrDefault(reason, 0) + 1
                            }
                        }
                        stats
                    }

                    val sortedPlayersByUnsummoned = players.map { player -> player to (unsummonedStats[player.id]?.values?.sum() ?: 0) }.sortedByDescending { it.second }

                    item {
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.2f))) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                if (sortedPlayersByUnsummoned.all { it.second == 0 }) {
                                    Text("No hay desconvocatorias registradas.", style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
                                } else {
                                    sortedPlayersByUnsummoned.forEach { (player, total) ->
                                        val displayName = if (player.lastName.isNotBlank()) "${player.name} ${player.lastName}" else player.name
                                        val label = if (total == 1) "1 desconvocatoria" else "$total desconvocatorias"
                                        Column(modifier = Modifier.padding(bottom = 10.dp)) {
                                            Text(text = "• $displayName ($label)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = if (total > 0) com.example.entrenamientos.ui.theme.AttendanceRed else Color.DarkGray)
                                            unsummonedStats[player.id]?.forEach { (reason, count) ->
                                                Text(text = "   - $reason: $count", style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                if (teamMatches.isEmpty()) {
                    item { Text("No hay partidos programados.", color = Color.Gray) }
                } else {
                    val matchesByMonth = teamMatches.groupBy { java.time.YearMonth.from(java.time.LocalDate.parse(it.date)) }
                    val sortedMatchMonths = matchesByMonth.toSortedMap(compareByDescending { it })

                    sortedMatchMonths.forEach { (month, matchesInMonth) ->
                        val isExpanded = expandedMatchMonths[month] ?: false

                        item {
                            val activeColor = activeTeamObj?.colorHex?.let {
                                try { Color(android.graphics.Color.parseColor(it)) } catch (_: Exception) { Color.Black }
                            } ?: Color.Black
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.small).clickable { expandedMatchMonths[month] = !isExpanded }.padding(vertical = 4.dp, horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = month.format(formatterMonth).uppercase(), style = MaterialTheme.typography.titleMedium, color = activeColor)
                                Icon(imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = "Expandir/Colapsar", tint = Color.Gray)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        if (isExpanded) {
                            val sortedMatches = matchesInMonth.sortedByDescending { it.date }

                            items(sortedMatches) { match ->
                                val matchDateObj = java.time.LocalDate.parse(match.date)
                                val matchDayOfWeek = matchDateObj.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES")).replaceFirstChar { it.uppercase() }
                                val matchMonthName = matchDateObj.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES")).lowercase()
                                val formattedDate = "$matchDayOfWeek ${matchDateObj.dayOfMonth} de $matchMonthName · ${match.time}"
                                val isMatchExpanded = expandedMatchDetails[match.id] ?: false

                                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.2f))) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth().clickable { expandedMatchDetails[match.id] = !isMatchExpanded }, horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("vs ${match.opponent}", fontWeight = FontWeight.Bold)
                                                Text(formattedDate, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                            }
                                            Icon(imageVector = if (isMatchExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = "Expandir/Colapsar", tint = Color.Gray)
                                        }

                                        if (isMatchExpanded) {
                                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                                            if (!match.isConvocatoriaSaved) {
                                                Text("Convocatoria no guardada todavía.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                            } else {
                                                val convocadas = players.filter { match.summonedPlayers.contains(it.id) }
                                                val desconvocadas = players.filter { match.unsummonedReasons.containsKey(it.id) }

                                                Text("- Convocadas (${convocadas.size})", style = MaterialTheme.typography.titleSmall, color = com.example.entrenamientos.ui.theme.SuccessGreen, fontWeight = FontWeight.Bold)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                convocadas.forEach { p ->
                                                    val displayName = if (p.lastName.isNotBlank()) "${p.name} ${p.lastName}" else p.name
                                                    Text("• $displayName", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 8.dp, bottom = 2.dp))
                                                }

                                                Spacer(modifier = Modifier.height(10.dp))
                                                Text("- Desconvocadas (${desconvocadas.size})", style = MaterialTheme.typography.titleSmall, color = com.example.entrenamientos.ui.theme.AttendanceRed, fontWeight = FontWeight.Bold)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                desconvocadas.forEach { p ->
                                                    val displayName = if (p.lastName.isNotBlank()) "${p.name} ${p.lastName}" else p.name
                                                    Column(modifier = Modifier.padding(start = 8.dp, bottom = 6.dp)) {
                                                        Text(displayName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                                        Text("Motivo: ${match.unsummonedReasons[p.id]?.takeIf { it.isNotBlank() } ?: "Sin motivo especificado"}", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}