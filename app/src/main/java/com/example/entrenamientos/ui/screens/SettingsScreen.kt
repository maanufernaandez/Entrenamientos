package com.example.entrenamientos.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.entrenamientos.ui.BasketViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: BasketViewModel = hiltViewModel()) {
    var activeTab by remember { mutableStateOf("EQUIPOS") }
    val teamsList by viewModel.teams.collectAsState()
    val selectedTeam by viewModel.selectedTeamYear.collectAsState()

    androidx.compose.runtime.LaunchedEffect(teamsList) {
        if (teamsList.isNotEmpty() && teamsList.none { it.year == selectedTeam }) {
            viewModel.setSelectedTeamYear(teamsList.first().year)
        }
        if (teamsList.isEmpty()) {
            activeTab = "EQUIPOS"
        }
    }

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val scale = (configuration.screenWidthDp / 360f).coerceIn(0.85f, 1.25f)
    fun sp(base: Int) = (base * scale).sp
    val buttonPaddingH = (14 * scale).dp
    val buttonPaddingV = (8 * scale).dp
    val buttonContentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = buttonPaddingH, vertical = buttonPaddingV)

    val players by viewModel.getPlayersForTeam(selectedTeam).collectAsState(initial = emptyList())
    val allSchedules by viewModel.schedules.collectAsState()
    val allMatches by viewModel.matches.collectAsState()
    val teamSchedules = allSchedules.filter { it.teamYear == selectedTeam }.sortedBy { it.dayOfWeek }
    val teamMatches = allMatches.filter { it.teamYear == selectedTeam }

    var showPlayerDialog by remember { mutableStateOf(false) }
    var playerToEdit by remember { mutableStateOf<com.example.entrenamientos.data.Player?>(null) }
    var playerNameInput by remember { mutableStateOf("") }
    var playerLastNameInput by remember { mutableStateOf("") }
    var playerDorsalInput by remember { mutableStateOf("") }

    var showScheduleDialog by remember { mutableStateOf(false) }
    var scheduleToEdit by remember { mutableStateOf<com.example.entrenamientos.data.TrainingSchedule?>(null) }
    var scheduleDayInput by remember { mutableIntStateOf(1) }
    var scheduleStartInput by remember { mutableStateOf("17:00") }
    var scheduleEndInput by remember { mutableStateOf("18:30") }
    var scheduleError by remember { mutableStateOf("") }

    var showMatchDialog by remember { mutableStateOf(false) }
    var matchStep by remember { mutableIntStateOf(1) }
    var matchToEdit by remember { mutableStateOf<com.example.entrenamientos.data.Match?>(null) }
    var matchDateInput by remember { mutableStateOf("05-09-2026") }
    var matchTimeInput by remember { mutableStateOf("10:00") }
    var matchIsLocalInput by remember { mutableStateOf(true) }
    var matchLocationInput by remember { mutableStateOf("") }
    var matchOpponentInput by remember { mutableStateOf("") }

    var teamToDelete by remember { mutableStateOf<com.example.entrenamientos.data.Team?>(null) }
    var playerToDelete by remember { mutableStateOf<com.example.entrenamientos.data.Player?>(null) }
    var scheduleToDelete by remember { mutableStateOf<com.example.entrenamientos.data.TrainingSchedule?>(null) }
    var matchToDelete by remember { mutableStateOf<com.example.entrenamientos.data.Match?>(null) }

    val expandedMatchSettingsMonths = remember { androidx.compose.runtime.mutableStateMapOf<java.time.YearMonth, Boolean>() }
    val daysOfWeek = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
    val context = androidx.compose.ui.platform.LocalContext.current

    var showTeamDialog by remember { mutableStateOf(false) }
    var teamToEdit by remember { mutableStateOf<com.example.entrenamientos.data.Team?>(null) }

    val selectedBlue = Color(0xFF2196F3)
    val unselectedLightBlue = Color(0xFFE3F2FD)

    val currentTeamObj = teamsList.find { it.year == selectedTeam }
    val trackMatches = currentTeamObj?.trackMatches ?: true
    val isFemale = currentTeamObj?.gender == "F"
    val labelJugadores = if (isFemale) "Jugadoras" else "Jugadores"
    val labelJugador = if (isFemale) "Jugadora" else "Jugador"

    val jugadorasTitle = "$labelJugadores (${players.size})"
    val dayInitials = mapOf(1 to "L", 2 to "M", 3 to "X", 4 to "J", 5 to "V", 6 to "S", 7 to "D")
    val teamDaysStr = teamSchedules.map { it.dayOfWeek }.sorted().joinToString(" / ") { dayInitials[it] ?: "" }
    val horariosTitle = if (teamDaysStr.isNotEmpty()) "Horarios ($teamDaysStr)" else "Horarios"

    val uniformSpacing = 24.dp

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("AJUSTES", style = MaterialTheme.typography.headlineMedium.copy(fontSize = sp(24)))
        Spacer(modifier = Modifier.height(16.dp))

        if (teamsList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Button(
                    onClick = { teamToEdit = null; showTeamDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceGreen)
                ) {
                    Text("Crear un nuevo equipo", fontSize = sp(16), color = Color.Black)
                }
            }
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(teamsList) { team ->
                    val teamColor = team.colorHex.let {
                        try { Color(android.graphics.Color.parseColor(it)) } catch (_: Exception) { Color.Gray }
                    }
                    val isTeamSelected = selectedTeam == team.year
                    Button(
                        onClick = {
                            viewModel.setSelectedTeamYear(team.year)
                            if (!team.trackMatches && activeTab == "PARTIDOS") {
                                activeTab = "EQUIPOS"
                            }
                        },
                        contentPadding = buttonContentPadding,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isTeamSelected) teamColor else teamColor.copy(alpha = 0.25f)
                        )
                    ) {
                        Text(
                            team.shortName.ifBlank { team.name.take(6).uppercase() },
                            fontSize = sp(13),
                            color = if (isTeamSelected) Color.White else Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { activeTab = "EQUIPOS" },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = buttonContentPadding,
                    colors = ButtonDefaults.buttonColors(containerColor = if (activeTab == "EQUIPOS") selectedBlue else unselectedLightBlue)
                ) { Text("Equipos", color = if (activeTab == "EQUIPOS") Color.White else Color.Black, fontSize = sp(13), maxLines = 1) }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { activeTab = "PLANTILLA" },
                        modifier = Modifier.weight(1f),
                        contentPadding = buttonContentPadding,
                        colors = ButtonDefaults.buttonColors(containerColor = if (activeTab == "PLANTILLA") selectedBlue else unselectedLightBlue)
                    ) { Text(jugadorasTitle, color = if (activeTab == "PLANTILLA") Color.White else Color.Black, fontSize = sp(13), maxLines = 1) }

                    Button(
                        onClick = { activeTab = "HORARIOS" },
                        modifier = Modifier.weight(1f),
                        contentPadding = buttonContentPadding,
                        colors = ButtonDefaults.buttonColors(containerColor = if (activeTab == "HORARIOS") selectedBlue else unselectedLightBlue)
                    ) { Text(horariosTitle, color = if (activeTab == "HORARIOS") Color.White else Color.Black, fontSize = sp(13), maxLines = 1) }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Button(
                            onClick = { if (trackMatches) activeTab = "PARTIDOS" },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = buttonContentPadding,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!trackMatches) Color.LightGray.copy(alpha = 0.5f)
                                else if (activeTab == "PARTIDOS") selectedBlue
                                else unselectedLightBlue
                            )
                        ) { Text("Partidos", color = if (!trackMatches) Color.Gray else if (activeTab == "PARTIDOS") Color.White else Color.Black, fontSize = sp(13), maxLines = 1) }

                        if (!trackMatches) {
                            Canvas(modifier = Modifier.matchParentSize().clip(CircleShape)) {
                                val w = size.width; val h = size.height; val strokeW = (3.5f * scale).dp.toPx()
                                drawLine(color = Color.Red, start = Offset(w * 0.1f, h * 0.15f), end = Offset(w * 0.9f, h * 0.85f), strokeWidth = strokeW, cap = StrokeCap.Round)
                                drawLine(color = Color.Red, start = Offset(w * 0.9f, h * 0.15f), end = Offset(w * 0.1f, h * 0.85f), strokeWidth = strokeW, cap = StrokeCap.Round)
                            }
                        }
                    }

                    Button(
                        onClick = { activeTab = "FESTIVOS" },
                        modifier = Modifier.weight(1f),
                        contentPadding = buttonContentPadding,
                        colors = ButtonDefaults.buttonColors(containerColor = if (activeTab == "FESTIVOS") selectedBlue else unselectedLightBlue)
                    ) { Text("Festivos", color = if (activeTab == "FESTIVOS") Color.White else Color.Black, fontSize = sp(13), maxLines = 1) }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (activeTab) {
                "EQUIPOS" -> {
                    val cellBg = Color.LightGray.copy(alpha = 0.2f)
                    val cellShape = MaterialTheme.shapes.small
                    val cellHeight = (42 * scale).dp

                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(teamsList) { team ->
                            val teamColor = try { Color(android.graphics.Color.parseColor(team.colorHex)) } catch (_: Exception) { Color.Gray }
                            val genderStr = when (team.gender) {
                                "M" -> "Masculino"
                                "F" -> "Femenino"
                                else -> "Mixto"
                            }
                            val subtitle = "Categoría: ${team.categoryYear} $genderStr"

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.weight(1f).height(cellHeight).clip(cellShape).background(cellBg).padding(horizontal = 16.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(teamColor))
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(text = team.name, style = MaterialTheme.typography.bodyLarge.copy(fontSize = sp(15)), fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium.copy(fontSize = sp(12)), color = Color.Gray, maxLines = 1)
                                        }
                                    }
                                }

                                Box(modifier = Modifier.size(cellHeight).clip(cellShape).background(cellBg), contentAlignment = Alignment.Center) {
                                    IconButton(onClick = { teamToEdit = team; showTeamDialog = true }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.DarkGray)
                                    }
                                }

                                Box(modifier = Modifier.size(cellHeight).clip(cellShape).background(cellBg), contentAlignment = Alignment.Center) {
                                    IconButton(onClick = { teamToDelete = team }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = com.example.entrenamientos.ui.theme.AttendanceRed)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { teamToEdit = null; showTeamDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = buttonContentPadding,
                        colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceGreen)
                    ) { Text("Añadir Nuevo Equipo", color = Color.Black, fontSize = sp(14)) }
                }
                "PLANTILLA" -> {
                    val sortedPlayers = players.sortedWith(
                        compareBy<com.example.entrenamientos.data.Player> { it.dorsal.isNullOrBlank() }
                            .thenBy { it.dorsal?.toIntOrNull() ?: 999 }
                    )

                    LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        items(sortedPlayers) { player ->
                            val displayName = if (player.lastName.isNotBlank()) "${player.name} ${player.lastName}" else player.name
                            val dDisplay = player.dorsal?.takeIf { it.isNotBlank() } ?: "s.n."

                            val cellBg = Color.LightGray.copy(alpha = 0.2f)
                            val cellShape = MaterialTheme.shapes.small
                            val cellHeight = (42 * scale).dp

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.width((48 * scale).dp).height(cellHeight).clip(cellShape).background(cellBg), contentAlignment = Alignment.Center) {
                                    Text(text = dDisplay, style = MaterialTheme.typography.bodyLarge.copy(fontSize = sp(17)), fontWeight = FontWeight.Normal)
                                }

                                Box(modifier = Modifier.weight(1f).height(cellHeight).clip(cellShape).background(cellBg).padding(horizontal = 12.dp), contentAlignment = Alignment.CenterStart) {
                                    Text(text = displayName, style = MaterialTheme.typography.bodyLarge.copy(fontSize = sp(15)), fontWeight = FontWeight.Normal, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }

                                Box(modifier = Modifier.size(cellHeight).clip(cellShape).background(cellBg), contentAlignment = Alignment.Center) {
                                    IconButton(onClick = {
                                        playerToEdit = player; playerNameInput = player.name; playerLastNameInput = player.lastName; playerDorsalInput = player.dorsal ?: ""; showPlayerDialog = true
                                    }) { Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.DarkGray) }
                                }

                                Box(modifier = Modifier.size(cellHeight).clip(cellShape).background(cellBg), contentAlignment = Alignment.Center) {
                                    IconButton(onClick = { playerToDelete = player }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = com.example.entrenamientos.ui.theme.AttendanceRed)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { playerToEdit = null; playerNameInput = ""; playerLastNameInput = ""; playerDorsalInput = ""; showPlayerDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = buttonContentPadding,
                        colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceGreen)
                    ) { Text("Añadir Nuev${if(isFemale) "a" else "o"} $labelJugador", color = Color.Black, fontSize = sp(14)) }
                }
                "HORARIOS" -> {
                    var firstTrainingDateInput by remember(selectedTeam) {
                        mutableStateOf(teamsList.find { it.year == selectedTeam }?.firstTrainingDate ?: "2026-09-01")
                    }

                    val teamDaysUsed = teamSchedules.map { it.dayOfWeek }
                    val availableDaysSettings = (1..5).filter { !teamDaysUsed.contains(it) }

                    LazyColumn(modifier = Modifier.weight(1f)) {
                        item {
                            Text("Fecha del primer entrenamiento:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            androidx.compose.material3.OutlinedButton(
                                onClick = {
                                    val parts = firstTrainingDateInput.split("-")
                                    val y = parts.getOrNull(0)?.toIntOrNull() ?: 2026
                                    val m = (parts.getOrNull(1)?.toIntOrNull() ?: 9) - 1
                                    val d = parts.getOrNull(2)?.toIntOrNull() ?: 1
                                    android.app.DatePickerDialog(context, { _, year, month, day ->
                                        val newDate = String.format(java.util.Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day)
                                        firstTrainingDateInput = newDate
                                        viewModel.updateTeamFirstTrainingDate(selectedTeam, newDate)
                                    }, y, m, d).show()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val parsedDate = try { java.time.LocalDate.parse(firstTrainingDateInput) } catch (_: Exception) { java.time.LocalDate.of(2026, 9, 1) }
                                val formatted = "${parsedDate.dayOfMonth} de ${parsedDate.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES"))} ${parsedDate.year}"
                                Text("📅 $formatted", color = Color.Black)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Horarios Semanales:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (teamSchedules.isEmpty()) {
                            item {
                                Text("No tienes horarios de entrenamiento registrados", color = Color.Gray, fontSize = sp(14), modifier = Modifier.padding(vertical = 8.dp))
                            }
                        } else {
                            items(teamSchedules) { schedule ->
                                val cellBg = Color.LightGray.copy(alpha = 0.2f)
                                val cellShape = MaterialTheme.shapes.small
                                val cellHeight = (42 * scale).dp

                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier.weight(1f).height(cellHeight).clip(cellShape).background(cellBg).padding(horizontal = 16.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = daysOfWeek[schedule.dayOfWeek - 1], style = MaterialTheme.typography.bodyLarge.copy(fontSize = sp(15)), fontWeight = FontWeight.Bold)
                                            Text(text = "${schedule.startTime} - ${schedule.endTime}", style = MaterialTheme.typography.bodyMedium.copy(fontSize = sp(13)), color = Color.DarkGray)
                                        }
                                    }

                                    Box(modifier = Modifier.size(cellHeight).clip(cellShape).background(cellBg), contentAlignment = Alignment.Center) {
                                        IconButton(onClick = { scheduleToEdit = schedule; scheduleDayInput = schedule.dayOfWeek; scheduleStartInput = schedule.startTime; scheduleEndInput = schedule.endTime; scheduleError = ""; showScheduleDialog = true }) { Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.DarkGray) }
                                    }

                                    Box(modifier = Modifier.size(cellHeight).clip(cellShape).background(cellBg), contentAlignment = Alignment.Center) {
                                        IconButton(onClick = { scheduleToDelete = schedule }) { Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = com.example.entrenamientos.ui.theme.AttendanceRed) }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { scheduleToEdit = null; scheduleDayInput = availableDaysSettings.firstOrNull() ?: 1; scheduleStartInput = "17:00"; scheduleEndInput = "18:00"; scheduleError = ""; showScheduleDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = buttonContentPadding,
                        enabled = availableDaysSettings.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceGreen)
                    ) { Text(if (availableDaysSettings.isEmpty()) "Todos los días cubiertos" else "Añadir Horario", color = Color.Black, fontSize = sp(14)) }
                }
                "PARTIDOS" -> {
                    val matchesByMonth = teamMatches.groupBy { java.time.YearMonth.from(java.time.LocalDate.parse(it.date)) }
                    val sortedMonths = matchesByMonth.toSortedMap(compareByDescending { it })
                    val formatterMonth = java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy", java.util.Locale("es", "ES"))
                    val activeColor = currentTeamObj?.colorHex?.let {
                        try { Color(android.graphics.Color.parseColor(it)) } catch (_: Exception) { Color.Black }
                    } ?: Color.Black

                    LazyColumn(modifier = Modifier.weight(1f)) {
                        if (sortedMonths.isEmpty()) { item { Text("No hay partidos programados.", color = Color.Gray, fontSize = sp(14)) } }

                        sortedMonths.forEach { (month, matchesInMonth) ->
                            val isExpanded = expandedMatchSettingsMonths[month] ?: false

                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.small).clickable { expandedMatchSettingsMonths[month] = !isExpanded }.padding(vertical = 6.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = month.format(formatterMonth).uppercase(), style = MaterialTheme.typography.titleMedium.copy(fontSize = sp(15)), color = activeColor)
                                    Icon(imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = "Expandir/Colapsar", tint = Color.Gray)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            if (isExpanded) {
                                val sortedMatches = matchesInMonth.sortedByDescending { it.date }

                                items(sortedMatches) { match ->
                                    val dateObj = java.time.LocalDate.parse(match.date)
                                    val dayOfWeekName = dateObj.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES")).replaceFirstChar { it.uppercase() }
                                    val shortDate = String.format(java.util.Locale.getDefault(), "%02d/%02d", dateObj.dayOfMonth, dateObj.monthValue)
                                    val teamShortName = currentTeamObj?.shortName?.takeIf { it.isNotBlank() } ?: "Local"

                                    val cellBg = Color.LightGray.copy(alpha = 0.2f)
                                    val cellShape = MaterialTheme.shapes.small

                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).height(androidx.compose.foundation.layout.IntrinsicSize.Min),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier.weight(1f).clip(cellShape).background(cellBg).padding(horizontal = 12.dp, vertical = 12.dp),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Center) {
                                                Text(text = "$dayOfWeekName $shortDate - ${match.time}", style = MaterialTheme.typography.bodyLarge.copy(fontSize = sp(13)), fontWeight = FontWeight.Bold)
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(text = "Polideportivo ${match.location}", style = MaterialTheme.typography.bodyMedium.copy(fontSize = sp(12)), color = Color.DarkGray)
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(text = if (match.isLocal) "$teamShortName - ${match.opponent}" else "${match.opponent} - $teamShortName", style = MaterialTheme.typography.bodyMedium.copy(fontSize = sp(12)), color = Color.DarkGray)
                                            }
                                        }

                                        Box(modifier = Modifier.width(48.dp).fillMaxHeight().clip(cellShape).background(cellBg), contentAlignment = Alignment.Center) {
                                            IconButton(onClick = { matchToEdit = match; matchStep = 1; val parts = match.date.split("-"); matchDateInput = "${parts[2]}-${parts[1]}-${parts[0]}"; matchTimeInput = match.time; matchIsLocalInput = match.isLocal; matchLocationInput = match.location; matchOpponentInput = match.opponent; showMatchDialog = true }) {
                                                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.DarkGray)
                                            }
                                        }

                                        Box(modifier = Modifier.width(48.dp).fillMaxHeight().clip(cellShape).background(cellBg), contentAlignment = Alignment.Center) {
                                            IconButton(onClick = { matchToDelete = match }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = com.example.entrenamientos.ui.theme.AttendanceRed)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { matchToEdit = null; matchStep = 1; matchDateInput = "05-09-2026"; matchTimeInput = "10:00"; matchIsLocalInput = true; matchLocationInput = ""; matchOpponentInput = ""; showMatchDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = buttonContentPadding,
                        colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceGreen)
                    ) { Text("Añadir Partido", color = Color.Black, fontSize = sp(14)) }
                }
                "FESTIVOS" -> {
                    FestivosSettingsTab(viewModel)
                }
            }
        }
    }

    // --- POPUP ÚNICO DE EQUIPOS (CREAR / EDITAR) ---
    if (showTeamDialog) {
        val isEdit = teamToEdit != null
        var eName by remember { mutableStateOf(teamToEdit?.name ?: "") }
        var eShortName by remember { mutableStateOf(teamToEdit?.shortName ?: "") }

        val categories = listOf(
            "Pre-Benjamin 3x3", "Benjamin 3x3", "Benjamin 5x5",
            "PreMinibasket", "Minibasket", "Preinfantil",
            "Infantil", "Cadete", "Junior", "Senior"
        )
        var eCategory by remember { mutableStateOf(teamToEdit?.categoryYear?.takeIf { it.isNotBlank() } ?: categories.first()) }
        var expandedCategory by remember { mutableStateOf(false) }

        var eGender by remember { mutableStateOf(teamToEdit?.gender ?: "M") }
        var eColor by remember { mutableStateOf(teamToEdit?.colorHex ?: "#2196F3") }
        var eTrackMatches by remember { mutableStateOf(teamToEdit?.trackMatches ?: true) }

        val uniformGenderColor = com.example.entrenamientos.ui.theme.InfantilBlue

        AlertDialog(
            onDismissRequest = { showTeamDialog = false; teamToEdit = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.fillMaxWidth(0.98f).padding(8.dp),
            title = { Text(if (isEdit) "Editar Equipo" else "Crear Nuevo Equipo", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
            text = {
                Column(modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())) {
                    OutlinedTextField(value = eName, onValueChange = { eName = it }, label = { Text("Nombre completo del equipo") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    Spacer(Modifier.height(8.dp))

                    // Selector de categoría desplegable debajo del nombre y encima de la abreviatura
                    ExposedDropdownMenuBox(
                        expanded = expandedCategory,
                        onExpandedChange = { expandedCategory = !expandedCategory },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = eCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Categoría") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedCategory,
                            onDismissRequest = { expandedCategory = false }
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category) },
                                    onClick = {
                                        eCategory = category
                                        expandedCategory = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = eShortName,
                        onValueChange = { input -> eShortName = input.take(6) },
                        label = { Text("Abreviatura (Máx 6 caract)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { eGender = "M" },
                            modifier = Modifier.weight(1f),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (eGender == "M") uniformGenderColor else Color.LightGray)
                        ) { Text("Masculino", color = if (eGender == "M") Color.White else Color.Black, fontSize = 13.sp, maxLines = 1) }

                        Button(
                            onClick = { eGender = "F" },
                            modifier = Modifier.weight(1f),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (eGender == "F") uniformGenderColor else Color.LightGray)
                        ) { Text("Femenino", color = if (eGender == "F") Color.White else Color.Black, fontSize = 13.sp, maxLines = 1) }

                        Button(
                            onClick = { eGender = "X" },
                            modifier = Modifier.weight(1f),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (eGender == "X") uniformGenderColor else Color.LightGray)
                        ) { Text("Mixto", color = if (eGender == "X") Color.White else Color.Black, fontSize = 13.sp, maxLines = 1) }
                    }
                    Spacer(Modifier.height(16.dp))

                    FullColorPicker(colorHex = eColor, onColorChanged = { eColor = it })
                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.small).clickable { eTrackMatches = !eTrackMatches }.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.Checkbox(checked = eTrackMatches, onCheckedChange = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("¿Hacer seguimiento de partidos?", style = MaterialTheme.typography.bodyMedium, color = Color.Black)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { showTeamDialog = false; teamToEdit = null }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceRed)) { Text("Cancelar", color = Color.White) }
                    Button(onClick = {
                        if (eName.isBlank() || eShortName.isBlank()) {
                            android.widget.Toast.makeText(context, "El nombre y la abreviatura son obligatorios", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            if (isEdit) {
                                viewModel.updateTeamData(teamToEdit!!.year, eName, eShortName, eGender, eCategory, eColor, eTrackMatches)
                            } else {
                                viewModel.addTeam(eName, eShortName, eGender, eCategory, eColor, eTrackMatches)
                            }
                            showTeamDialog = false
                            teamToEdit = null
                        }
                    }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceGreen)) { Text(if (isEdit) "Actualizar" else "Guardar", color = Color.Black) }
                }
            }
        )
    }

    if (showPlayerDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showPlayerDialog = false }, properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)) {
            Card(modifier = Modifier.fillMaxWidth(0.98f).padding(8.dp), shape = MaterialTheme.shapes.medium) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(if (playerToEdit == null) "Añadir $labelJugador" else "Editar $labelJugador", style = MaterialTheme.typography.titleLarge, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = playerNameInput,
                            onValueChange = { if (it.length <= 15) playerNameInput = it },
                            label = { Text("Nombre (*)", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            singleLine = true,
                            modifier = Modifier.weight(1f).height(64.dp)
                        )
                        OutlinedTextField(
                            value = playerLastNameInput,
                            onValueChange = { if (it.length <= 25) playerLastNameInput = it },
                            label = { Text("Apellido (*)", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            singleLine = true,
                            modifier = Modifier.weight(1.3f).height(64.dp)
                        )
                        OutlinedTextField(
                            value = playerDorsalInput,
                            onValueChange = { if(it.length <= 2 && it.all { char -> char.isDigit() }) playerDorsalInput = it },
                            label = { Text("Nº", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            singleLine = true,
                            modifier = Modifier.width(64.dp).height(64.dp),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { showPlayerDialog = false }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceRed)) { Text("Cancelar", color = Color.White) }
                        Button(onClick = {
                            if (playerNameInput.isNotBlank() && playerLastNameInput.isNotBlank()) {
                                val hasDuplicate = players.any { it.dorsal == playerDorsalInput && playerDorsalInput.isNotBlank() && it.id != playerToEdit?.id }
                                if (hasDuplicate) { android.widget.Toast.makeText(context, "El dorsal $playerDorsalInput ya está en uso", android.widget.Toast.LENGTH_SHORT).show() } else {
                                    if (playerToEdit == null) { viewModel.addPlayer(playerNameInput, playerLastNameInput, playerDorsalInput, selectedTeam) } else { viewModel.updatePlayer(playerToEdit!!.copy(name = playerNameInput, lastName = playerLastNameInput, dorsal = playerDorsalInput)) }
                                    showPlayerDialog = false
                                }
                            } else { android.widget.Toast.makeText(context, "Nombre y Apellido son obligatorios", android.widget.Toast.LENGTH_SHORT).show() }
                        }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceGreen)) { Text(if (playerToEdit == null) "Guardar" else "Actualizar", color = Color.Black) }
                    }
                }
            }
        }
    }

    if (showScheduleDialog) {
        val teamDaysUsed = teamSchedules.map { it.dayOfWeek }
        val currentAvailableDays = if (scheduleToEdit != null) {
            ((1..5).filter { !teamDaysUsed.contains(it) } + scheduleToEdit!!.dayOfWeek).sorted().distinct()
        } else {
            (1..5).filter { !teamDaysUsed.contains(it) }
        }

        androidx.compose.ui.window.Dialog(onDismissRequest = { showScheduleDialog = false }, properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)) {
            Card(modifier = Modifier.fillMaxWidth(0.98f).padding(8.dp), shape = MaterialTheme.shapes.medium) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(if (scheduleToEdit == null) "Nuevo Horario" else "Editar Horario", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)

                    Spacer(modifier = Modifier.height(uniformSpacing))

                    if (scheduleError.isNotEmpty()) {
                        Text(scheduleError, color = com.example.entrenamientos.ui.theme.AttendanceRed, style = MaterialTheme.typography.bodySmall, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(uniformSpacing))
                    }

                    Text("Día de la semana:", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)

                    Spacer(modifier = Modifier.height(uniformSpacing))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Button(onClick = {
                            val idx = currentAvailableDays.indexOf(scheduleDayInput)
                            if (idx > 0) scheduleDayInput = currentAvailableDays[idx - 1] else if (currentAvailableDays.isNotEmpty()) scheduleDayInput = currentAvailableDays.last()
                        }) { Text("-") }

                        Text(if (currentAvailableDays.isEmpty()) "" else daysOfWeek[scheduleDayInput - 1])

                        Button(onClick = {
                            val idx = currentAvailableDays.indexOf(scheduleDayInput)
                            if (idx < currentAvailableDays.size - 1) scheduleDayInput = currentAvailableDays[idx + 1] else if (currentAvailableDays.isNotEmpty()) scheduleDayInput = currentAvailableDays.first()
                        }) { Text("+") }
                    }

                    Spacer(modifier = Modifier.height(uniformSpacing))

                    androidx.compose.material3.OutlinedButton(onClick = {
                        val parts = scheduleStartInput.split(":")
                        val h = parts.getOrNull(0)?.toIntOrNull() ?: 17
                        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
                        android.app.TimePickerDialog(context, { _, hour, minute -> scheduleStartInput = String.format(java.util.Locale.getDefault(), "%02d:%02d", hour, minute) }, h, m, true).show()
                    }, modifier = Modifier.fillMaxWidth()) { Text("Hora Inicio: $scheduleStartInput", color = Color.Black) }

                    Spacer(modifier = Modifier.height(uniformSpacing))

                    androidx.compose.material3.OutlinedButton(onClick = {
                        val parts = scheduleEndInput.split(":")
                        val h = parts.getOrNull(0)?.toIntOrNull() ?: 18
                        val m = parts.getOrNull(1)?.toIntOrNull() ?: 30
                        android.app.TimePickerDialog(context, { _, hour, minute -> scheduleEndInput = String.format(java.util.Locale.getDefault(), "%02d:%02d", hour, minute) }, h, m, true).show()
                    }, modifier = Modifier.fillMaxWidth()) { Text("Hora Fin: $scheduleEndInput", color = Color.Black) }

                    Spacer(modifier = Modifier.height(uniformSpacing))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { showScheduleDialog = false }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceRed)) { Text("Cancelar", color = Color.White) }
                        Button(onClick = {
                            val newSchedule = com.example.entrenamientos.data.TrainingSchedule(id = scheduleToEdit?.id ?: 0, teamYear = selectedTeam, dayOfWeek = scheduleDayInput, startTime = scheduleStartInput, endTime = scheduleEndInput)
                            viewModel.addOrUpdateSchedule(newSchedule, onSuccess = { showScheduleDialog = false }, onError = { errorMsg -> scheduleError = errorMsg })
                        }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceGreen)) { Text(if (scheduleToEdit == null) "Guardar" else "Actualizar", color = Color.Black) }
                    }
                }
            }
        }
    }

    if (showMatchDialog) {
        val subtitleStep = when(matchStep) { 1 -> "Seleccionar Día"; 2 -> "Seleccionar Hora"; 3 -> "¿Local o Visitante?"; 4 -> "Polideportivo y Equipo Rival"; else -> "" }
        androidx.compose.ui.window.Dialog(onDismissRequest = { showMatchDialog = false }, properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)) {
            Card(modifier = Modifier.fillMaxWidth(0.98f).padding(8.dp), shape = MaterialTheme.shapes.medium) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(if (matchToEdit == null) "Nuevo Partido" else "Editar Partido", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(uniformSpacing))
                    Text(subtitleStep, style = MaterialTheme.typography.bodyMedium, color = Color.Gray, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(uniformSpacing))
                    when (matchStep) {
                        1 -> androidx.compose.material3.OutlinedButton(onClick = { val parts = matchDateInput.split("-"); val d = parts.getOrNull(0)?.toIntOrNull() ?: 5; val m = (parts.getOrNull(1)?.toIntOrNull() ?: 9) - 1; val y = parts.getOrNull(2)?.toIntOrNull() ?: 2026; android.app.DatePickerDialog(context, { _, year, month, day -> matchDateInput = String.format(java.util.Locale.getDefault(), "%02d-%02d-%04d", day, month + 1, year) }, y, m, d).show() }, modifier = Modifier.fillMaxWidth()) { Text(matchDateInput, color = Color.Black) }
                        2 -> androidx.compose.material3.OutlinedButton(onClick = { val parts = matchTimeInput.split(":"); val h = parts.getOrNull(0)?.toIntOrNull() ?: 10; val m = parts.getOrNull(1)?.toIntOrNull() ?: 0; android.app.TimePickerDialog(context, { _, hour, minute -> matchTimeInput = String.format(java.util.Locale.getDefault(), "%02d:%02d", hour, minute) }, h, m, true).show() }, modifier = Modifier.fillMaxWidth()) { Text(matchTimeInput, color = Color.Black) }
                        3 -> Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { matchIsLocalInput = true }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = if (matchIsLocalInput) com.example.entrenamientos.ui.theme.InfantilBlue else Color.LightGray)) { Text("Local", color = if (matchIsLocalInput) Color.White else Color.Black) }
                            Button(onClick = { matchIsLocalInput = false }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = if (!matchIsLocalInput) Color.Red else Color.LightGray)) { Text("Visitante", color = if (!matchIsLocalInput) Color.White else Color.Black) }
                        }
                        4 -> {
                            OutlinedTextField(value = matchLocationInput, onValueChange = { matchLocationInput = it }, label = { Text("Polideportivo") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(uniformSpacing))
                            OutlinedTextField(value = matchOpponentInput, onValueChange = { matchOpponentInput = it }, label = { Text("Equipo Rival") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        }
                    }
                    Spacer(modifier = Modifier.height(uniformSpacing))

                    if (matchStep == 1) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { showMatchDialog = false }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceRed)) { Text("Cancelar", color = Color.White, fontSize = sp(13), maxLines = 1) }
                            Button(onClick = { matchStep++ }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceGreen)) { Text("Siguiente", color = Color.Black, fontSize = sp(13), maxLines = 1) }
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { matchStep-- }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) { Text("Atrás", color = Color.White, fontSize = sp(13), maxLines = 1) }
                                if (matchStep < 4) {
                                    Button(onClick = { matchStep++ }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceGreen)) { Text("Siguiente", color = Color.Black, fontSize = sp(13), maxLines = 1) }
                                } else {
                                    Button(
                                        onClick = {
                                            val parts = matchDateInput.split("-"); val dateForDb = "${parts[2]}-${parts[1]}-${parts[0]}"; val newMatch = com.example.entrenamientos.data.Match(id = matchToEdit?.id ?: 0, date = dateForDb, time = matchTimeInput, isLocal = matchIsLocalInput, location = matchLocationInput, opponent = matchOpponentInput, teamYear = selectedTeam); viewModel.addOrUpdateMatch(newMatch); showMatchDialog = false
                                        },
                                        modifier = Modifier.weight(1f), enabled = matchLocationInput.isNotBlank() && matchOpponentInput.isNotBlank(), colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceGreen)
                                    ) { Text(if (matchToEdit == null) "Guardar" else "Actualizar", color = Color.Black, fontSize = sp(13), maxLines = 1) }
                                }
                            }
                            Button(onClick = { showMatchDialog = false }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceRed)) { Text("Cancelar", color = Color.White, fontSize = sp(13), maxLines = 1) }
                        }
                    }
                }
            }
        }
    }

    if (teamToDelete != null) {
        AlertDialog(
            onDismissRequest = { teamToDelete = null },
            title = { Text("Eliminar Equipo", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
            text = { Text("¿Estás seguro de que quieres eliminar este equipo? Se borrarán también sus jugador@s y horarios.", textAlign = TextAlign.Center) },
            confirmButton = {},
            dismissButton = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { teamToDelete = null }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) { Text("Cancelar", color = Color.White) }
                    Button(onClick = { viewModel.deleteTeamCascade(teamToDelete!!); teamToDelete = null }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceRed)) { Text("Eliminar", color = Color.White) }
                }
            }
        )
    }

    if (playerToDelete != null) {
        AlertDialog(
            onDismissRequest = { playerToDelete = null },
            title = { Text("Eliminar Jugador/a", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
            text = { Text("¿Estás seguro de que quieres eliminar este/a jugador/a?", textAlign = TextAlign.Center) },
            confirmButton = {},
            dismissButton = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { playerToDelete = null }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) { Text("Cancelar", color = Color.White) }
                    Button(onClick = { viewModel.deletePlayer(playerToDelete!!); playerToDelete = null }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceRed)) { Text("Eliminar", color = Color.White) }
                }
            }
        )
    }

    if (scheduleToDelete != null) {
        AlertDialog(
            onDismissRequest = { scheduleToDelete = null },
            title = { Text("Eliminar Horario", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
            text = { Text("¿Estás seguro de que quieres eliminar este horario de entrenamiento?", textAlign = TextAlign.Center) },
            confirmButton = {},
            dismissButton = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { scheduleToDelete = null }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) { Text("Cancelar", color = Color.White) }
                    Button(onClick = { viewModel.deleteSchedule(scheduleToDelete!!); scheduleToDelete = null }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceRed)) { Text("Eliminar", color = Color.White) }
                }
            }
        )
    }

    if (matchToDelete != null) {
        AlertDialog(
            onDismissRequest = { matchToDelete = null },
            title = { Text("Eliminar Partido", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
            text = { Text("¿Estás seguro de que quieres eliminar este partido?", textAlign = TextAlign.Center) },
            confirmButton = {},
            dismissButton = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { matchToDelete = null }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) { Text("Cancelar", color = Color.White) }
                    Button(onClick = { viewModel.deleteMatch(matchToDelete!!); matchToDelete = null }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceRed)) { Text("Eliminar", color = Color.White) }
                }
            }
        )
    }
}