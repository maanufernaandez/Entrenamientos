package com.example.entrenamientos.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.entrenamientos.ui.BasketViewModel

@Composable
fun CalendarScreen(
    viewModel: BasketViewModel = hiltViewModel(),
    navController: NavController
) {
    var currentMonth by remember { mutableStateOf(java.time.YearMonth.of(2026, 9)) }
    val minMonth = java.time.YearMonth.of(2026, 9)
    val maxMonth = java.time.YearMonth.of(2027, 5)

    var showDayDialog by remember { mutableStateOf(false) }
    var clickedDate by remember { mutableStateOf<java.time.LocalDate?>(null) }

    val schedules by viewModel.schedules.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }, enabled = currentMonth.isAfter(minMonth)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Mes anterior")
            }
            Text("${currentMonth.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES")).uppercase()} ${currentMonth.year}", style = MaterialTheme.typography.titleLarge)
            IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }, enabled = currentMonth.isBefore(maxMonth)) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Mes siguiente")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        val days = mutableListOf<java.time.LocalDate?>()
        val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value
        for (i in 1 until firstDayOfWeek) days.add(null)
        for (i in 1..currentMonth.lengthOfMonth()) days.add(currentMonth.atDay(i))
        while (days.size % 7 != 0) days.add(null)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.Black)
                .padding(1.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                listOf("L", "M", "X", "J", "V", "S", "D").forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFFEEEEEE))
                            .padding(vertical = 8.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            val weeks = days.chunked(7)
            weeks.forEach { week ->
                Row(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    week.forEach { date ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            if (date != null) {
                                DayCell(
                                    date = date,
                                    viewModel = viewModel,
                                    schedules = schedules,
                                    onClick = {
                                        viewModel.setSelectedDate(date.toString())
                                        clickedDate = date
                                        showDayDialog = true
                                    }
                                )
                            } else {
                                Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5)))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDayDialog && clickedDate != null) {
        DayOptionsDialog(
            date = clickedDate!!,
            viewModel = viewModel,
            navController = navController,
            onDismiss = { showDayDialog = false }
        )
    }
}

@Composable
fun DayCell(
    date: java.time.LocalDate,
    viewModel: BasketViewModel,
    schedules: List<com.example.entrenamientos.data.TrainingSchedule>,
    onClick: () -> Unit
) {
    val isHoliday = viewModel.isHoliday(date)
    val dayValue = date.dayOfWeek.value
    val teamsList by viewModel.teams.collectAsState()

    val isPastDay = date.isBefore(java.time.LocalDate.now())
    val cellBackgroundColor = when {
        isHoliday -> Color(0xFFFFEBEE)
        isPastDay -> Color(0xFFFFF5F5)
        else -> Color.White
    }

    val daySchedules = if (isHoliday) {
        emptyList()
    } else {
        schedules.filter { schedule ->
            if (schedule.dayOfWeek != dayValue) return@filter false

            val team = teamsList.find { it.year == schedule.teamYear }
            val firstDateStr = team?.firstTrainingDate ?: "2026-09-01"
            val firstDate = try {
                java.time.LocalDate.parse(firstDateStr)
            } catch (_: Exception) {
                java.time.LocalDate.of(2026, 9, 1)
            }

            // Solo incluimos el horario si la fecha actual NO es anterior a su primer entrenamiento
            !date.isBefore(firstDate)
        }.sortedBy { it.startTime }
    }

    val matchesOnDay = viewModel.matches.collectAsState().value.filter { it.date == date.toString() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(cellBackgroundColor)
            // Aquí desactivamos el clic si el día no tiene entrenamientos ni partidos
            .clickable(enabled = daySchedules.isNotEmpty() || matchesOnDay.isNotEmpty()) { onClick() }
            .padding(4.dp)
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            modifier = Modifier.align(Alignment.TopStart),
            style = MaterialTheme.typography.bodyLarge,
            color = if (isHoliday) Color.Red else Color.Unspecified,
            fontWeight = if (isHoliday) FontWeight.ExtraBold else FontWeight.SemiBold
        )

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Círculos de partidos apilados verticalmente y centrados
            if (matchesOnDay.isNotEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    matchesOnDay.forEach { m ->
                        val circleColor = viewModel.getMatchColor(m)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(circleColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (m.isLocal) "L" else "V",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Entrenamientos ordenados cronológicamente
            daySchedules.forEachIndexed { index, schedule ->
                val team = teamsList.find { it.year == schedule.teamYear }
                val bgColor = team?.colorHex?.let {
                    try { Color(android.graphics.Color.parseColor(it)) } catch (_: Exception) { Color.Gray }
                } ?: Color.Gray

                val displayTitle = if (team?.categoryYear?.isNotBlank() == true) team.categoryYear else team?.name ?: ""

                if (index > 0 || matchesOnDay.isNotEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth(0.8f).height(1.dp).background(Color.Black))
                }

                Box(
                    modifier = Modifier.fillMaxWidth(0.95f).height(24.dp).background(bgColor, shape = MaterialTheme.shapes.extraSmall),
                    contentAlignment = Alignment.Center
                ) {
                    Text(displayTitle, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }
        }
    }
}

@Composable
fun DayOptionsDialog(
    date: java.time.LocalDate,
    viewModel: BasketViewModel,
    navController: NavController,
    onDismiss: () -> Unit
) {
    val teams = viewModel.getTeamsForDate(date)
    val match = viewModel.getMatchForDate(date)
    val hasMatch = match != null
    val teamsList by viewModel.teams.collectAsState()

    val dayOfWeek = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES")).replaceFirstChar { it.uppercase() }
    val monthNumber = date.monthValue
    val dayNumber = date.dayOfMonth
    val monthName = date.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES"))

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.95f).wrapContentHeight(),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (hasMatch && match != null) {
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.Black)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "$dayOfWeek $dayNumber/$monthNumber - ${match.time}",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.Black,
                                fontWeight = FontWeight.Normal
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Sports, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.Black)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (match.isLocal) "CD Huerto - ${match.opponent}" else "${match.opponent} - CD Huerto",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.Black,
                                fontWeight = FontWeight.Normal
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.Black)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Polideportivo ${match.location}",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.Black,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.Black)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "$dayOfWeek $dayNumber de $monthName",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Black,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (teams.isNotEmpty()) {
                    teams.forEachIndexed { index, teamYearLoop ->
                        if (index > 0) {
                            HorizontalDivider(color = Color.Black, thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))
                        }

                        val team = teamsList.find { it.year == teamYearLoop }
                        val teamNameBase = team?.name ?: "Equipo $teamYearLoop"
                        val catYear = team?.categoryYear
                        val teamDisplayName = if (!catYear.isNullOrBlank()) "$teamNameBase ($catYear)" else teamNameBase

                        val teamColor = team?.colorHex?.let {
                            try { Color(android.graphics.Color.parseColor(it)) } catch (_: Exception) { Color.Gray }
                        } ?: Color.Gray

                        Text(teamDisplayName, style = MaterialTheme.typography.bodyLarge, color = teamColor, fontWeight = FontWeight.Bold)

                        val attendanceForTeam by viewModel.getAttendanceForDateAndTeam(date.toString(), teamYearLoop).collectAsState(initial = emptyList())
                        val attendanceLabel = if (attendanceForTeam.isNotEmpty()) "Ver Asistencia" else "Asistencia"

                        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { viewModel.setSelectedTeamYear(teamYearLoop); onDismiss(); navController.navigate("notes/ENTRENAMIENTO") }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = teamColor)) { Text("Entrenamiento", color = Color.White, maxLines = 1, fontSize = 13.sp) }
                            Button(onClick = { viewModel.setSelectedTeamYear(teamYearLoop); onDismiss(); navController.navigate("attendance") }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = teamColor)) { Text(attendanceLabel, color = Color.White, maxLines = 1, fontSize = 13.sp) }
                        }

                        Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Spacer(modifier = Modifier.weight(0.5f))
                            Button(onClick = { viewModel.setSelectedTeamYear(teamYearLoop); onDismiss(); navController.navigate("notes/OTROS") }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = teamColor)) { Text("Notas", color = Color.White, maxLines = 1, fontSize = 13.sp) }
                            Spacer(modifier = Modifier.weight(0.5f))
                        }
                    }
                }

                if (hasMatch && match != null) {
                    HorizontalDivider(color = Color.Black, thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))
                    viewModel.setSelectedTeamYear(match.teamYear)

                    // Extraemos el color del equipo del partido
                    val matchTeam = teamsList.find { it.year == match.teamYear }
                    val matchTeamColor = matchTeam?.colorHex?.let {
                        try { Color(android.graphics.Color.parseColor(it)) } catch (_: Exception) { Color.DarkGray }
                    } ?: Color.DarkGray

                    val (canMake, _) = viewModel.canMakeConvocatoria(date)
                    val isConvocatoriaEnabled = canMake || match.isConvocatoriaSaved

                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { viewModel.setSelectedDate(date.toString()); onDismiss(); navController.navigate("convocatoria") },
                                enabled = isConvocatoriaEnabled,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = matchTeamColor, disabledContainerColor = Color.LightGray)
                            ) { Text(if (match.isConvocatoriaSaved) "Ver Convocatoria" else "Convocatoria", color = Color.White, maxLines = 1, fontSize = 12.sp) }

                            Button(
                                onClick = { viewModel.setSelectedDate(date.toString()); onDismiss(); navController.navigate("quintetos") },
                                enabled = match.isConvocatoriaSaved,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = matchTeamColor, disabledContainerColor = Color.LightGray)
                            ) { Text("Quintetos", color = Color.White, maxLines = 1, fontSize = 13.sp) }
                        }

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Spacer(modifier = Modifier.weight(0.5f))
                            Button(
                                onClick = { viewModel.setSelectedDate(date.toString()); onDismiss(); navController.navigate("resultado") },
                                enabled = match.isConvocatoriaSaved,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = matchTeamColor, disabledContainerColor = Color.LightGray)
                            ) { Text(if (match.resultLocal != null) "Ver Resultado" else "Resultado", color = Color.White, maxLines = 1, fontSize = 13.sp) }
                            Spacer(modifier = Modifier.weight(0.5f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AttendanceScreen(viewModel: BasketViewModel = hiltViewModel(), navController: NavController) {
    val dateStr by viewModel.selectedDate.collectAsState()
    val teamYear by viewModel.selectedTeamYear.collectAsState()
    val teamsList by viewModel.teams.collectAsState()

    val players by viewModel.getPlayersForTeam(teamYear).collectAsState(initial = emptyList())
    val existingAttendance by viewModel.getAttendanceForDateAndTeam(dateStr, teamYear).collectAsState(initial = emptyList())

    val attendanceState = remember(players, existingAttendance) {
        androidx.compose.runtime.mutableStateMapOf<Long, Int>().apply {
            players.forEach { player ->
                val existing = existingAttendance.find { it.playerId == player.id }
                put(player.id, existing?.status ?: 0)
            }
        }
    }

    val team = teamsList.find { it.year == teamYear }
    val teamName = team?.name ?: "$teamYear"

    val date = java.time.LocalDate.parse(dateStr)
    val dayOfWeek = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES")).replaceFirstChar { it.uppercase() }
    val monthName = date.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES"))

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Asistencia $teamName:", style = MaterialTheme.typography.headlineMedium)
        Text(text = "$dayOfWeek ${date.dayOfMonth} de $monthName", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(players) { player ->
                val status = attendanceState[player.id] ?: 0

                val bgColor = when (status) {
                    0 -> com.example.entrenamientos.ui.theme.AttendanceGreen
                    1 -> com.example.entrenamientos.ui.theme.AttendanceYellow
                    2 -> com.example.entrenamientos.ui.theme.AttendanceRed
                    else -> com.example.entrenamientos.ui.theme.AttendanceGreen
                }

                val statusText = when (status) {
                    0 -> "Asiste"
                    1 -> "Justificado"
                    2 -> "Injustificado"
                    else -> "Asiste"
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(bgColor)
                        .clickable {
                            attendanceState[player.id] = viewModel.getNextAttendanceStatus(status)
                        }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val displayName = if (player.lastName.isNotBlank()) "${player.name} ${player.lastName}" else player.name
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 17.sp,
                        color = Color.Black
                    )

                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceRed)
            ) { Text("Cancelar", color = Color.White) }

            Button(
                onClick = {
                    val newAttendances = players.map { player ->
                        com.example.entrenamientos.data.Attendance(
                            id = existingAttendance.find { it.playerId == player.id }?.id ?: 0,
                            date = dateStr,
                            playerId = player.id,
                            status = attendanceState[player.id] ?: 0,
                            teamYear = teamYear
                        )
                    }
                    viewModel.saveAttendances(newAttendances)
                    navController.popBackStack()
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceGreen)
            ) { Text("Guardar", color = Color.Black) }
        }
    }
}

@SuppressLint("MutableCollectionMutableState")
@Composable
fun ConvocatoriaScreen(viewModel: BasketViewModel, navController: NavController) {
    val selectedDateStr by viewModel.selectedDate.collectAsState()
    val date = java.time.LocalDate.parse(selectedDateStr)
    val match = viewModel.getMatchForDate(date) ?: return

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

    val teamsList by viewModel.teams.collectAsState()
    val activeColor = teamsList.find { it.year == match.teamYear }?.colorHex?.let {
        try { Color(android.graphics.Color.parseColor(it)) } catch (_: Exception) { Color.Black }
    } ?: Color.Black

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(if (isEditMode) {
            if (match.isConvocatoriaSaved) "Editar Convocatoria" else "Crear Convocatoria"
        } else "Convocatoria Oficial", style = MaterialTheme.typography.headlineMedium)
        Text("vs ${match.opponent} - ${match.date}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))

        if (!isEditMode) {
            val convocadas = players.filter { match.summonedPlayers.contains(it.id) }
            val desconvocadas = players.filter { !match.summonedPlayers.contains(it.id) }

            LazyColumn(modifier = Modifier.weight(1f)) {
                item {
                    Text("✅ CONVOCADAS (${convocadas.size}/12)", style = MaterialTheme.typography.titleMedium, color = com.example.entrenamientos.ui.theme.SuccessGreen)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(convocadas) { p ->
                    val displayName = if (p.lastName.isNotBlank()) "${p.name} ${p.lastName}" else p.name
                    Text(
                        text = "• $displayName",
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 17.sp,
                        modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("❌ DESCONVOCADAS", style = MaterialTheme.typography.titleMedium, color = com.example.entrenamientos.ui.theme.AttendanceRed)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(desconvocadas) { p ->
                    Column(modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)) {
                        val displayName = if (p.lastName.isNotBlank()) "${p.name} ${p.lastName}" else p.name
                        Text(
                            text = "• $displayName",
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Motivo: ${match.unsummonedReasons[p.id]?.takeIf { it.isNotBlank() } ?: "Sin motivo especificado"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.DarkGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(
                    onClick = {
                        shouldSaveDraft = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                    modifier = Modifier.weight(1f)
                ) { Text("Volver", color = Color.White) }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        shouldSaveDraft = false
                        val resetMatch = match.copy(isConvocatoriaSaved = false, summonedPlayers = emptyList(), unsummonedReasons = emptyMap())
                        viewModel.addOrUpdateMatch(resetMatch)
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceRed),
                    modifier = Modifier.weight(1f)
                ) { Text("Eliminar", color = Color.White) }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { isEditMode = true },
                    colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceGreen),
                    modifier = Modifier.weight(1f)
                ) { Text("Editar", color = Color.Black) }
            }

        } else {
            Text("Seleccionadas: ${summonedIds.size} (Mín. 8 - Máx. 12)", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(players) { player ->
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
                        val displayName = if (player.lastName.isNotBlank()) "${player.name} ${player.lastName}" else player.name
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = containerColor
                        )

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
                            "Debes convocar entre 8 y 12 jugadoras (Actual: ${summonedIds.size})",
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
                    val displayName = if (playerToUnsummon?.lastName?.isNotBlank() == true) "${playerToUnsummon?.name} ${playerToUnsummon?.lastName}" else playerToUnsummon?.name ?: ""
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
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

@Composable
fun QuintetosScreen(viewModel: BasketViewModel, navController: NavController) {
    val selectedDateStr by viewModel.selectedDate.collectAsState()
    val date = java.time.LocalDate.parse(selectedDateStr)
    val match = viewModel.getMatchForDate(date) ?: return

    val players by viewModel.getPlayersForTeam(match.teamYear).collectAsState(initial = emptyList())
    val quintetosNote by viewModel.getTrainingNoteForDateAndTeam(selectedDateStr, match.teamYear, "QUINTETOS").collectAsState(initial = null)

    val teamsList by viewModel.teams.collectAsState()
    val matchTeam = teamsList.find { it.year == match.teamYear }
    val teamColor = matchTeam?.colorHex?.let {
        try { Color(android.graphics.Color.parseColor(it)) } catch (_: Exception) { Color.DarkGray }
    } ?: Color.DarkGray

    val context = androidx.compose.ui.platform.LocalContext.current

    val lineups = remember(quintetosNote?.content) {
        val content = quintetosNote?.content ?: ""
        if (content.isBlank()) emptyList()
        else content.split("|").map { quarter -> quarter.split(",").mapNotNull { it.toLongOrNull() } }
    }

    val currentQuarter = lineups.size + 1
    val summonedPlayers = players.filter { match.summonedPlayers.contains(it.id) }

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

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Quintetos", style = MaterialTheme.typography.headlineMedium)
        Text("vs ${match.opponent} - ${match.date}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
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

                            if (isCompleted) {
                                val quarterPlayers = players.filter { lineups[i].contains(it.id) }
                                quarterPlayers.forEach { p ->
                                    Text(p.name, style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, maxLines = 1, textAlign = TextAlign.Center, color = Color.Black)
                                }
                            } else {
                                repeat(5) {
                                    Text("•", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = if (isFuture) Color.Gray.copy(alpha = 0.5f) else Color.Gray, textAlign = TextAlign.Center)
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = if (currentQuarter > 4) Arrangement.Center else Arrangement.Top
        ) {
            if (currentQuarter <= 4) {
                item {
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
                    Text("Seleccionadas: ${currentSelection.size}/5", style = MaterialTheme.typography.bodyMedium, color = counterColor, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (currentQuarter == 3 && forcedPlayersForQ3.size > 5) {
                        Text("¡Error de rotación! Has dejado a más de 5 jugadoras sin jugar en los primeros cuartos. No caben en pista. Pulsa 'Empezar de 0'.", color = com.example.entrenamientos.ui.theme.AttendanceRed, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

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

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(containerColor)
                            .clickable(enabled = !isBanned && !isForced) {
                                if (isSelected) {
                                    currentSelection = currentSelection - player.id
                                } else if (currentSelection.size < 5) {
                                    currentSelection = currentSelection + player.id
                                } else {
                                    android.widget.Toast.makeText(context, "Ya has seleccionado 5 jugadoras", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val baseName = if (player.lastName.isNotBlank()) "${player.name} ${player.lastName}" else player.name
                        val quartersPlayed = lineups.count { it.contains(player.id) }
                        val displayName = if (currentQuarter > 1) "$baseName ($quartersPlayed)" else baseName

                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = 17.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isBanned || isSelected) Color.White else Color.Black
                        )

                        if (isSelected) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White)
                        }
                    }
                }
            } else {
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
                                    players.filter { lineups[0].contains(it.id) }.forEach { p ->
                                        Text(p.name, fontSize = 16.sp, maxLines = 1, fontWeight = FontWeight.Medium)
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
                                    players.filter { lineups[1].contains(it.id) }.forEach { p ->
                                        Text(p.name, fontSize = 16.sp, maxLines = 1, fontWeight = FontWeight.Medium)
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
                                    players.filter { lineups[2].contains(it.id) }.forEach { p ->
                                        Text(p.name, fontSize = 16.sp, maxLines = 1, fontWeight = FontWeight.Medium)
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
                                    players.filter { lineups[3].contains(it.id) }.forEach { p ->
                                        Text(p.name, fontSize = 16.sp, maxLines = 1, fontWeight = FontWeight.Medium)
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
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Button(
                onClick = { navController.popBackStack() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                modifier = Modifier.weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp)
            ) { Text("Volver", color = Color.White, fontSize = 13.sp, maxLines = 1) }

            Button(
                onClick = {
                    viewModel.saveTrainingNote(selectedDateStr, match.teamYear, "QUINTETOS", "", quintetosNote)
                    currentSelection = setOf()
                },
                enabled = lineups.isNotEmpty() || currentSelection.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceRed),
                modifier = Modifier.weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp)
            ) { Text("Empezar de 0", color = Color.White, fontSize = 13.sp, maxLines = 1) }

            if (currentQuarter <= 4) {
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
                    modifier = Modifier.weight(1f),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp)
                ) { Text(if (currentQuarter < 4) "Siguiente" else "Guardar", color = Color.Black, fontSize = 13.sp, maxLines = 1) }
            }
        }
    }

    if (showTotalDialog) {
        AlertDialog(
            onDismissRequest = { showTotalDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.fillMaxWidth(0.95f).padding(16.dp),
            title = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Nº de Cuartos / Jugadora", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    HorizontalDivider(modifier = Modifier.padding(top = 8.dp), color = Color.Black, thickness = 1.dp)
                }
            },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    val sortedPlayers = summonedPlayers.sortedByDescending { p -> lineups.count { it.contains(p.id) } }
                    items(sortedPlayers) { player ->
                        val quartersPlayed = lineups.count { it.contains(player.id) }
                        val quarterText = if (quartersPlayed == 1) "1 Cuarto" else "$quartersPlayed Cuartos"
                        val displayName = if (player.lastName.isNotBlank()) "${player.name} ${player.lastName}" else player.name

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("• $displayName", style = MaterialTheme.typography.bodyLarge)
                            Text("($quarterText)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
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

@Composable
fun ResultadoScreen(viewModel: BasketViewModel, navController: NavController) {
    val selectedDateStr by viewModel.selectedDate.collectAsState()
    val date = java.time.LocalDate.parse(selectedDateStr)
    val match = viewModel.getMatchForDate(date) ?: return

    var resLocal by remember { mutableStateOf(match.resultLocal?.toString() ?: "") }
    var resVisitor by remember { mutableStateOf(match.resultVisitor?.toString() ?: "") }
    var ftMade by remember { mutableStateOf(match.ftMade.toString()) }
    var ftAttempted by remember { mutableStateOf(match.ftAttempted.toString()) }

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
                            ftAttempted = ftAttempted.toIntOrNull() ?: 0
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

@Composable
fun TrainingNoteScreen(viewModel: BasketViewModel = hiltViewModel(), navController: NavController, noteType: String) {
    val dateStr by viewModel.selectedDate.collectAsState()
    val teamYear by viewModel.selectedTeamYear.collectAsState()
    val teamsList by viewModel.teams.collectAsState()

    val existingNote by viewModel.getTrainingNoteForDateAndTeam(dateStr, teamYear, noteType).collectAsState(initial = null)

    var noteContent by remember(existingNote) { mutableStateOf(existingNote?.content ?: "") }

    val team = teamsList.find { it.year == teamYear }
    val teamTitle = team?.name ?: ""
    val titlePrefix = if (noteType == "ENTRENAMIENTO") "Entrenamiento" else "Notas"

    val date = java.time.LocalDate.parse(dateStr)
    val dayOfWeek = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES")).replaceFirstChar { it.uppercase() }
    val monthName = date.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES"))

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "$titlePrefix $teamTitle:", style = MaterialTheme.typography.headlineMedium)
        Text(text = "$dayOfWeek ${date.dayOfMonth} de $monthName", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
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

@Composable
fun StatsScreen(viewModel: BasketViewModel) {
    val teamsList by viewModel.teams.collectAsState()
    var selectedTeam by remember { mutableIntStateOf(0) }

    androidx.compose.runtime.LaunchedEffect(teamsList) {
        if (teamsList.isNotEmpty() && teamsList.none { it.year == selectedTeam }) {
            selectedTeam = teamsList.first().year
        }
    }

    val attendances by viewModel.getAllAttendancesByTeam(selectedTeam).collectAsState(initial = emptyList())
    val allMatches by viewModel.matches.collectAsState()
    val teamMatches = allMatches.filter { it.teamYear == selectedTeam }

    val players by viewModel.getPlayersForTeam(selectedTeam).collectAsState(initial = emptyList())

    val expandedAttendanceMonths = remember { androidx.compose.runtime.mutableStateMapOf<java.time.YearMonth, Boolean>() }
    val expandedMatchMonths = remember { androidx.compose.runtime.mutableStateMapOf<java.time.YearMonth, Boolean>() }
    val expandedMatchDetails = remember { androidx.compose.runtime.mutableStateMapOf<Long, Boolean>() }
    var isSeasonAttendanceExpanded by remember { mutableStateOf(false) }
    var isSeasonMatchesExpanded by remember { mutableStateOf(false) }

    val expandedWeekDetails = remember { androidx.compose.runtime.mutableStateMapOf<java.time.LocalDate, Boolean>() }

    data class PlayerAttendanceCount(
        val player: com.example.entrenamientos.data.Player,
        val present: Int,
        val justified: Int,
        val unjustified: Int
    )

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Panel de Estadísticas", style = MaterialTheme.typography.headlineMedium)
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
                            onClick = { selectedTeam = team.year },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isTeamSelected) teamColor else teamColor.copy(alpha = 0.25f)
                            )
                        ) {
                            Text(
                                team.name,
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
                    val activeTeam = teamsList.find { it.year == selectedTeam }
                    val activeColor = activeTeam?.colorHex?.let {
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
                    }.sortedByDescending { it.present } // ORDENADO DE MAYOR A MENOR ASISTENCIA

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                if (sortedPlayersByAttendance.isEmpty()) {
                                    Text("No hay jugadoras registradas.", style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
                                } else {
                                    // CABECERA DE LA TABLA
                                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        Text("Jugadoras", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
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

                                    // LÍNEA SEPARADORA DE CABECERA
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color.Black, thickness = 1.dp)

                                    // FILAS DE JUGADORAS
                                    sortedPlayersByAttendance.forEachIndexed { index, entry ->
                                        val displayName = if (entry.player.lastName.isNotBlank()) "${entry.player.name} ${entry.player.lastName}" else entry.player.name
                                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Text("• $displayName", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
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

                                        // FINAS LÍNEAS SEPARADORAS ENTRE JUGADORAS
                                        if (index < sortedPlayersByAttendance.size - 1) {
                                            HorizontalDivider(color = Color.Black.copy(alpha = 0.15f), thickness = 0.5.dp)
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
                    val activeTeam = teamsList.find { it.year == selectedTeam }
                    val activeColor = activeTeam?.colorHex?.let {
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
                            }.sortedByDescending { it.present } // ORDENADO DE MAYOR A MENOR ASISTENCIA

                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.2f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        if (sortedPlayersByWeekAttendance.isEmpty()) {
                                            Text("No hay jugadoras registradas.", style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
                                        } else {
                                            // CABECERA DE LA TABLA
                                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                                Text("Jugadoras", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
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

                                            // LÍNEA SEPARADORA DE CABECERA
                                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color.Black, thickness = 1.dp)

                                            // FILAS DE JUGADORAS
                                            sortedPlayersByWeekAttendance.forEachIndexed { index, entry ->
                                                val displayName = if (entry.player.lastName.isNotBlank()) "${entry.player.name} ${entry.player.lastName}" else entry.player.name
                                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                                    Text("• $displayName", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
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

                                                // FINAS LÍNEAS SEPARADORAS ENTRE JUGADORAS
                                                if (index < sortedPlayersByWeekAttendance.size - 1) {
                                                    HorizontalDivider(color = Color.Black.copy(alpha = 0.15f), thickness = 0.5.dp)
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

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                Text("Medias de Temporada", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
            }

            val playedMatches = teamMatches.filter { it.resultLocal != null && it.resultVisitor != null }

            if (playedMatches.isEmpty()) {
                item { Text("No hay partidos jugados todavía.", color = Color.Gray) }
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
                            totalWins++
                            localWins++
                        } else if (localScore < visitorScore) {
                            totalLosses++
                            localLosses++
                        }
                    } else {
                        totalScored += visitorScore
                        totalReceived += localScore
                        if (visitorScore > localScore) {
                            totalWins++
                            visitorWins++
                        } else if (visitorScore < localScore) {
                            totalLosses++
                            visitorLosses++
                        }
                    }
                    totalFtMade += m.ftMade
                    totalFtAttempted += m.ftAttempted
                }

                val avgScored = if (playedMatches.isNotEmpty()) totalScored.toFloat() / playedMatches.size else 0f
                val avgReceived = if (playedMatches.isNotEmpty()) totalReceived.toFloat() / playedMatches.size else 0f
                val ftPercentage = if (totalFtAttempted > 0) (totalFtMade.toFloat() / totalFtAttempted) * 100 else 0f

                item {
                    val activeTeam = teamsList.find { it.year == selectedTeam }
                    val activeColor = activeTeam?.colorHex?.let {
                        try { Color(android.graphics.Color.parseColor(it)) } catch (_: Exception) { Color.Black }
                    } ?: Color.Black

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = activeColor.copy(alpha = 0.15f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Resultados: $totalWins / $totalLosses", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Resultados (Local): $localWins / $localLosses", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Resultados (Visitante): $visitorWins / $visitorLosses", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Puntos Anotados / partido: ${String.format(java.util.Locale.US, "%.1f", avgScored)} pts", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Puntos Recibidos / partido: ${String.format(java.util.Locale.US, "%.1f", avgReceived)} pts", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Tiros Libres (%): ${String.format(java.util.Locale.US, "%.1f", ftPercentage)}% ($totalFtMade/$totalFtAttempted)", fontWeight = FontWeight.Bold)
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
                val activeTeam = teamsList.find { it.year == selectedTeam }
                val activeColor = activeTeam?.colorHex?.let {
                    try { Color(android.graphics.Color.parseColor(it)) } catch (_: Exception) { Color.Black }
                } ?: Color.Black

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .clickable { isSeasonMatchesExpanded = !isSeasonMatchesExpanded }
                        .padding(vertical = 4.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("TEMPORADA", style = MaterialTheme.typography.titleMedium, color = activeColor)
                    Icon(
                        imageVector = if (isSeasonMatchesExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expandir/Colapsar",
                        tint = Color.Gray
                    )
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

                val sortedPlayersByUnsummoned = players
                    .map { player -> player to (unsummonedStats[player.id]?.values?.sum() ?: 0) }
                    .sortedByDescending { it.second }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (sortedPlayersByUnsummoned.all { it.second == 0 }) {
                                Text("No hay desconvocatorias registradas.", style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
                            } else {
                                sortedPlayersByUnsummoned.forEach { (player, total) ->
                                    val displayName = if (player.lastName.isNotBlank()) "${player.name} ${player.lastName}" else player.name
                                    val label = if (total == 1) "1 desconvocatoria" else "$total desconvocatorias"
                                    Column(modifier = Modifier.padding(bottom = 10.dp)) {
                                        Text(
                                            text = "• $displayName ($label)",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (total > 0) com.example.entrenamientos.ui.theme.AttendanceRed else Color.DarkGray
                                        )
                                        unsummonedStats[player.id]?.forEach { (reason, count) ->
                                            Text(
                                                text = "   - $reason: $count",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color.DarkGray
                                            )
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
                val matchesByMonth = teamMatches.groupBy {
                    java.time.YearMonth.from(java.time.LocalDate.parse(it.date))
                }
                val sortedMatchMonths = matchesByMonth.toSortedMap(compareByDescending { it })

                sortedMatchMonths.forEach { (month, matchesInMonth) ->
                    val isExpanded = expandedMatchMonths[month] ?: false

                    item {
                        val activeTeam = teamsList.find { it.year == selectedTeam }
                        val activeColor = activeTeam?.colorHex?.let {
                            try { Color(android.graphics.Color.parseColor(it)) } catch (_: Exception) { Color.Black }
                        } ?: Color.Black
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.small)
                                .clickable { expandedMatchMonths[month] = !isExpanded }
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
                        val sortedMatches = matchesInMonth.sortedByDescending { it.date }

                        items(sortedMatches) { match ->
                            val matchDateObj = java.time.LocalDate.parse(match.date)
                            val matchDayOfWeek = matchDateObj.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES")).replaceFirstChar { it.uppercase() }
                            val matchMonthName = matchDateObj.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES")).lowercase()
                            val formattedDate = "$matchDayOfWeek ${matchDateObj.dayOfMonth} de $matchMonthName · ${match.time}"

                            val isMatchExpanded = expandedMatchDetails[match.id] ?: false

                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.2f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { expandedMatchDetails[match.id] = !isMatchExpanded },
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("vs ${match.opponent}", fontWeight = FontWeight.Bold)
                                            Text(formattedDate, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        }
                                        Icon(
                                            imageVector = if (isMatchExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Expandir/Colapsar",
                                            tint = Color.Gray
                                        )
                                    }

                                    if (isMatchExpanded) {
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                                        if (!match.isConvocatoriaSaved) {
                                            Text("Convocatoria no guardada todavía.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                        } else {
                                            val convocadas = players.filter { match.summonedPlayers.contains(it.id) }
                                            val desconvocadas = players.filter { match.unsummonedReasons.containsKey(it.id) }

                                            Text("✅ Convocadas (${convocadas.size})", style = MaterialTheme.typography.titleSmall, color = com.example.entrenamientos.ui.theme.SuccessGreen, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            convocadas.forEach { p ->
                                                val displayName = if (p.lastName.isNotBlank()) "${p.name} ${p.lastName}" else p.name
                                                Text("• $displayName", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 8.dp, bottom = 2.dp))
                                            }

                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text("❌ Desconvocadas (${desconvocadas.size})", style = MaterialTheme.typography.titleSmall, color = com.example.entrenamientos.ui.theme.AttendanceRed, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            desconvocadas.forEach { p ->
                                                val displayName = if (p.lastName.isNotBlank()) "${p.name} ${p.lastName}" else p.name
                                                Column(modifier = Modifier.padding(start = 8.dp, bottom = 6.dp)) {
                                                    Text(displayName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                                    Text(
                                                        "Motivo: ${match.unsummonedReasons[p.id]?.takeIf { it.isNotBlank() } ?: "Sin motivo especificado"}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = Color.DarkGray
                                                    )
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

@Composable
fun SettingsScreen(viewModel: BasketViewModel = hiltViewModel()) {
    var activeTab by remember { mutableStateOf("EQUIPOS") }
    val teamsList by viewModel.teams.collectAsState()
    var selectedTeam by remember { mutableIntStateOf(0) }

    androidx.compose.runtime.LaunchedEffect(teamsList) {
        if (teamsList.isNotEmpty() && teamsList.none { it.year == selectedTeam }) {
            selectedTeam = teamsList.first().year
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

    // Variables para Pop-ups de eliminación
    var teamToDelete by remember { mutableStateOf<com.example.entrenamientos.data.Team?>(null) }
    var playerToDelete by remember { mutableStateOf<com.example.entrenamientos.data.Player?>(null) }
    var scheduleToDelete by remember { mutableStateOf<com.example.entrenamientos.data.TrainingSchedule?>(null) }
    var matchToDelete by remember { mutableStateOf<com.example.entrenamientos.data.Match?>(null) }

    val expandedMatchSettingsMonths = remember { androidx.compose.runtime.mutableStateMapOf<java.time.YearMonth, Boolean>() }
    val daysOfWeek = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
    val context = androidx.compose.ui.platform.LocalContext.current

    var showTeamWizard by remember { mutableStateOf(false) }
    var teamToEdit by remember { mutableStateOf<com.example.entrenamientos.data.Team?>(null) }

    val selectedBlue = Color(0xFF2196F3)
    val unselectedLightBlue = Color(0xFFE3F2FD)

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Ajustes", style = MaterialTheme.typography.headlineMedium.copy(fontSize = sp(24)))
        Spacer(modifier = Modifier.height(16.dp))

        if (teamsList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Button(
                    onClick = { showTeamWizard = true },
                    colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceGreen)
                ) {
                    Text("Crear un nuevo equipo", fontSize = sp(16), color = Color.Black)
                }
            }
        } else {
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
                        onClick = { activeTab = "JUGADORAS" },
                        modifier = Modifier.weight(1f),
                        contentPadding = buttonContentPadding,
                        colors = ButtonDefaults.buttonColors(containerColor = if (activeTab == "JUGADORAS") selectedBlue else unselectedLightBlue)
                    ) { Text("Jugadoras", color = if (activeTab == "JUGADORAS") Color.White else Color.Black, fontSize = sp(13), maxLines = 1) }

                    Button(
                        onClick = { activeTab = "HORARIOS" },
                        modifier = Modifier.weight(1f),
                        contentPadding = buttonContentPadding,
                        colors = ButtonDefaults.buttonColors(containerColor = if (activeTab == "HORARIOS") selectedBlue else unselectedLightBlue)
                    ) { Text("Horarios", color = if (activeTab == "HORARIOS") Color.White else Color.Black, fontSize = sp(13), maxLines = 1) }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { activeTab = "PARTIDOS" },
                        modifier = Modifier.weight(1f),
                        contentPadding = buttonContentPadding,
                        colors = ButtonDefaults.buttonColors(containerColor = if (activeTab == "PARTIDOS") selectedBlue else unselectedLightBlue)
                    ) { Text("Partidos", color = if (activeTab == "PARTIDOS") Color.White else Color.Black, fontSize = sp(13), maxLines = 1) }

                    Button(
                        onClick = { activeTab = "FESTIVOS" },
                        modifier = Modifier.weight(1f),
                        contentPadding = buttonContentPadding,
                        colors = ButtonDefaults.buttonColors(containerColor = if (activeTab == "FESTIVOS") selectedBlue else unselectedLightBlue)
                    ) { Text("Festivos", color = if (activeTab == "FESTIVOS") Color.White else Color.Black, fontSize = sp(13), maxLines = 1) }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (activeTab in listOf("JUGADORAS", "HORARIOS", "PARTIDOS")) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    LazyColumn(modifier = Modifier.weight(1f).height(50.dp)) {
                        item {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                teamsList.forEach { team ->
                                    val teamColor = team.colorHex.let {
                                        try { Color(android.graphics.Color.parseColor(it)) } catch (_: Exception) { Color.Gray }
                                    }
                                    val isTeamSelected = selectedTeam == team.year
                                    Button(
                                        onClick = { selectedTeam = team.year },
                                        contentPadding = buttonContentPadding,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isTeamSelected) teamColor else teamColor.copy(alpha = 0.25f)
                                        )
                                    ) {
                                        Text(
                                            team.name,
                                            fontSize = sp(13),
                                            color = if (isTeamSelected) Color.White else Color.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            when (activeTab) {
                "EQUIPOS" -> {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(teamsList) { team ->
                            val teamColor = try { Color(android.graphics.Color.parseColor(team.colorHex)) } catch (_: Exception) { Color.Gray }
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(MaterialTheme.shapes.small).background(Color.LightGray.copy(alpha = 0.2f)).padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(teamColor))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(text = team.name, style = MaterialTheme.typography.bodyLarge.copy(fontSize = sp(15)), fontWeight = FontWeight.Bold)
                                        if (team.categoryYear.isNotBlank()) {
                                            Text(text = "Año: ${team.categoryYear}", style = MaterialTheme.typography.bodyMedium.copy(fontSize = sp(13)), color = Color.Gray)
                                        }
                                    }
                                }
                                Row {
                                    IconButton(onClick = { teamToEdit = team }) { Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.DarkGray) }
                                    IconButton(onClick = { teamToDelete = team }) { Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = com.example.entrenamientos.ui.theme.AttendanceRed) }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showTeamWizard = true },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = buttonContentPadding,
                        colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceGreen)
                    ) { Text("Añadir Nuevo Equipo", color = Color.Black, fontSize = sp(14)) }
                }
                "JUGADORAS" -> {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(players) { player ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(MaterialTheme.shapes.small).background(Color.LightGray.copy(alpha = 0.2f)).padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                val displayName = if (player.lastName.isNotBlank()) "${player.name} ${player.lastName}" else player.name
                                Text(text = displayName, style = MaterialTheme.typography.bodyLarge.copy(fontSize = sp(15)))
                                Row {
                                    IconButton(onClick = { playerToEdit = player; playerNameInput = player.name; playerLastNameInput = player.lastName; showPlayerDialog = true }) { Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.DarkGray) }
                                    IconButton(onClick = { playerToDelete = player }) { Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = com.example.entrenamientos.ui.theme.AttendanceRed) }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { playerToEdit = null; playerNameInput = ""; playerLastNameInput = ""; showPlayerDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = buttonContentPadding,
                        colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceGreen)
                    ) { Text("Añadir Nueva Jugadora", color = Color.Black, fontSize = sp(14)) }
                }
                "HORARIOS" -> {
                    var firstTrainingDateInput by remember(selectedTeam) {
                        mutableStateOf(teamsList.find { it.year == selectedTeam }?.firstTrainingDate ?: "2026-09-01")
                    }

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

                        items(teamSchedules) { schedule ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(MaterialTheme.shapes.small).background(Color.LightGray.copy(alpha = 0.2f)).padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text(text = daysOfWeek[schedule.dayOfWeek - 1], style = MaterialTheme.typography.bodyLarge.copy(fontSize = sp(15)), fontWeight = FontWeight.Bold)
                                    Text(text = "${schedule.startTime} - ${schedule.endTime}", style = MaterialTheme.typography.bodyMedium.copy(fontSize = sp(13)), color = Color.Gray)
                                }
                                Row {
                                    IconButton(onClick = { scheduleToEdit = schedule; scheduleDayInput = schedule.dayOfWeek; scheduleStartInput = schedule.startTime; scheduleEndInput = schedule.endTime; scheduleError = ""; showScheduleDialog = true }) { Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.DarkGray) }
                                    IconButton(onClick = { scheduleToDelete = schedule }) { Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = com.example.entrenamientos.ui.theme.AttendanceRed) }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { scheduleToEdit = null; scheduleDayInput = 1; scheduleStartInput = "17:00"; scheduleEndInput = "18:00"; scheduleError = ""; showScheduleDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = buttonContentPadding,
                        colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceGreen)
                    ) { Text("Añadir Horario", color = Color.Black, fontSize = sp(14)) }
                }
                "PARTIDOS" -> {
                    val matchesByMonth = teamMatches.groupBy { java.time.YearMonth.from(java.time.LocalDate.parse(it.date)) }
                    val sortedMonths = matchesByMonth.toSortedMap(compareByDescending { it })
                    val formatterMonth = java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy", java.util.Locale("es", "ES"))
                    val activeTeam = teamsList.find { it.year == selectedTeam }
                    val activeColor = activeTeam?.colorHex?.let {
                        try { Color(android.graphics.Color.parseColor(it)) } catch (_: Exception) { Color.Black }
                    } ?: Color.Black

                    LazyColumn(modifier = Modifier.weight(1f)) {
                        if (sortedMonths.isEmpty()) {
                            item { Text("No hay partidos programados.", color = Color.Gray, fontSize = sp(14)) }
                        }

                        sortedMonths.forEach { (month, matchesInMonth) ->
                            val isExpanded = expandedMatchSettingsMonths[month] ?: false

                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(MaterialTheme.shapes.small)
                                        .clickable { expandedMatchSettingsMonths[month] = !isExpanded }
                                        .padding(vertical = 6.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = month.format(formatterMonth).uppercase(),
                                        style = MaterialTheme.typography.titleMedium.copy(fontSize = sp(15)),
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
                                val sortedMatches = matchesInMonth.sortedByDescending { it.date }

                                items(sortedMatches) { match ->
                                    val dateObj = java.time.LocalDate.parse(match.date)
                                    val dayOfWeekName = dateObj.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES")).replaceFirstChar { it.uppercase() }
                                    val shortDate = String.format(java.util.Locale.getDefault(), "%02d/%02d", dateObj.dayOfMonth, dateObj.monthValue)

                                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(MaterialTheme.shapes.small).background(Color.LightGray.copy(alpha = 0.2f)).padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = "$dayOfWeekName $shortDate - ${match.time}", style = MaterialTheme.typography.bodyLarge.copy(fontSize = sp(15)), fontWeight = FontWeight.Bold)
                                            Text(text = "Polideportivo ${match.location}", style = MaterialTheme.typography.bodyMedium.copy(fontSize = sp(13)))
                                            Text(text = if (match.isLocal) "CD Huerto - ${match.opponent}" else "${match.opponent} - CD Huerto", style = MaterialTheme.typography.bodyMedium.copy(fontSize = sp(13)))
                                        }
                                        Row {
                                            IconButton(onClick = { matchToEdit = match; matchStep = 1; val parts = match.date.split("-"); matchDateInput = "${parts[2]}-${parts[1]}-${parts[0]}"; matchTimeInput = match.time; matchIsLocalInput = match.isLocal; matchLocationInput = match.location; matchOpponentInput = match.opponent; showMatchDialog = true }) { Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.DarkGray) }
                                            IconButton(onClick = { matchToDelete = match }) { Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = com.example.entrenamientos.ui.theme.AttendanceRed) }
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

    // --- POPUPS DE EDICIÓN ---
    if (teamToEdit != null) {
        var eName by remember { mutableStateOf(teamToEdit!!.name) }
        var eCategory by remember { mutableStateOf(teamToEdit!!.categoryYear) }
        var eColor by remember { mutableStateOf(teamToEdit!!.colorHex) }

        AlertDialog(
            onDismissRequest = { teamToEdit = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.fillMaxWidth(0.98f).padding(8.dp),
            title = { Text("Editar Equipo", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
            text = {
                Column {
                    OutlinedTextField(value = eName, onValueChange = { eName = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = eCategory, onValueChange = { if(it.all{c->c.isDigit()}) eCategory = it }, label = { Text("Año (opcional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
                    Spacer(Modifier.height(16.dp))

                    FullColorPicker(colorHex = eColor, onColorChanged = { eColor = it })
                }
            },
            confirmButton = {},
            dismissButton = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { teamToEdit = null }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) { Text("Cancelar", color = Color.White) }
                    Button(onClick = {
                        viewModel.updateTeamData(teamToEdit!!.year, eName, eCategory, eColor)
                        teamToEdit = null
                    }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceGreen)) { Text("Actualizar", color = Color.Black) }
                }
            }
        )
    }

    if (showPlayerDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showPlayerDialog = false }, properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)) {
            Card(modifier = Modifier.fillMaxWidth(0.98f).padding(8.dp), shape = MaterialTheme.shapes.medium) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(if (playerToEdit == null) "Añadir Jugadora" else "Editar Jugadora", style = MaterialTheme.typography.titleLarge, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = playerNameInput, onValueChange = { playerNameInput = it }, label = { Text("Nombre") }, singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = playerLastNameInput, onValueChange = { playerLastNameInput = it }, label = { Text("Apellido") }, singleLine = true, modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { showPlayerDialog = false }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceRed)) { Text("Cancelar", color = Color.White) }
                        Button(onClick = {
                            if (playerNameInput.isNotBlank()) {
                                if (playerToEdit == null) viewModel.addPlayer(playerNameInput, playerLastNameInput, selectedTeam)
                                else viewModel.updatePlayer(playerToEdit!!.copy(name = playerNameInput, lastName = playerLastNameInput))
                                showPlayerDialog = false
                            }
                        }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceGreen)) { Text(if (playerToEdit == null) "Guardar" else "Actualizar", color = Color.Black) }
                    }
                }
            }
        }
    }

    if (showScheduleDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showScheduleDialog = false }, properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)) {
            Card(modifier = Modifier.fillMaxWidth(0.98f).padding(8.dp), shape = MaterialTheme.shapes.medium) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(if (scheduleToEdit == null) "Nuevo Horario" else "Editar Horario", style = MaterialTheme.typography.titleLarge, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    if (scheduleError.isNotEmpty()) { Text(scheduleError, color = com.example.entrenamientos.ui.theme.AttendanceRed, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 8.dp)) }
                    Text("Día de la semana:", style = MaterialTheme.typography.labelSmall)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Button(onClick = { if (scheduleDayInput > 1) scheduleDayInput-- else scheduleDayInput = 5 }) { Text("-") }
                        Text(daysOfWeek[scheduleDayInput - 1])
                        Button(onClick = { if (scheduleDayInput < 5) scheduleDayInput++ else scheduleDayInput = 1 }) { Text("+") }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    androidx.compose.material3.OutlinedButton(onClick = {
                        val parts = scheduleStartInput.split(":")
                        val h = parts.getOrNull(0)?.toIntOrNull() ?: 17
                        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
                        android.app.TimePickerDialog(context, { _, hour, minute -> scheduleStartInput = String.format(java.util.Locale.getDefault(), "%02d:%02d", hour, minute) }, h, m, true).show()
                    }, modifier = Modifier.fillMaxWidth()) { Text("Hora Inicio: $scheduleStartInput", color = Color.Black) }
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.material3.OutlinedButton(onClick = {
                        val parts = scheduleEndInput.split(":")
                        val h = parts.getOrNull(0)?.toIntOrNull() ?: 18
                        val m = parts.getOrNull(1)?.toIntOrNull() ?: 30
                        android.app.TimePickerDialog(context, { _, hour, minute -> scheduleEndInput = String.format(java.util.Locale.getDefault(), "%02d:%02d", hour, minute) }, h, m, true).show()
                    }, modifier = Modifier.fillMaxWidth()) { Text("Hora Fin: $scheduleEndInput", color = Color.Black) }
                    Spacer(modifier = Modifier.height(24.dp))
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
        val titleStep = when(matchStep) { 1 -> "Seleccionar Día"; 2 -> "Seleccionar Hora"; 3 -> "¿Local o Visitante?"; 4 -> "Polideportivo y Equipo Rival"; else -> "" }
        androidx.compose.ui.window.Dialog(onDismissRequest = { showMatchDialog = false }, properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)) {
            Card(modifier = Modifier.fillMaxWidth(0.98f).padding(8.dp), shape = MaterialTheme.shapes.medium) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(titleStep, style = MaterialTheme.typography.titleLarge, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    when (matchStep) {
                        1 -> androidx.compose.material3.OutlinedButton(onClick = { val parts = matchDateInput.split("-"); val d = parts.getOrNull(0)?.toIntOrNull() ?: 5; val m = (parts.getOrNull(1)?.toIntOrNull() ?: 9) - 1; val y = parts.getOrNull(2)?.toIntOrNull() ?: 2026; android.app.DatePickerDialog(context, { _, year, month, day -> matchDateInput = String.format(java.util.Locale.getDefault(), "%02d-%02d-%04d", day, month + 1, year) }, y, m, d).show() }, modifier = Modifier.fillMaxWidth()) { Text(matchDateInput, color = Color.Black) }
                        2 -> androidx.compose.material3.OutlinedButton(onClick = { val parts = matchTimeInput.split(":"); val h = parts.getOrNull(0)?.toIntOrNull() ?: 10; val m = parts.getOrNull(1)?.toIntOrNull() ?: 0; android.app.TimePickerDialog(context, { _, hour, minute -> matchTimeInput = String.format(java.util.Locale.getDefault(), "%02d:%02d", hour, minute) }, h, m, true).show() }, modifier = Modifier.fillMaxWidth()) { Text(matchTimeInput, color = Color.Black) }
                        3 -> Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { matchIsLocalInput = true }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = if (matchIsLocalInput) com.example.entrenamientos.ui.theme.InfantilBlue else Color.LightGray)) { Text("Local", color = if (matchIsLocalInput) Color.White else Color.Black) }
                            Button(onClick = { matchIsLocalInput = false }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = if (!matchIsLocalInput) Color.Red else Color.LightGray)) { Text("Visitante", color = if (!matchIsLocalInput) Color.White else Color.Black) }
                        }
                        4 -> {
                            OutlinedTextField(value = matchLocationInput, onValueChange = { matchLocationInput = it }, label = { Text("Polideportivo") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(value = matchOpponentInput, onValueChange = { matchOpponentInput = it }, label = { Text("Equipo Rival") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (matchStep > 1) {
                            Button(onClick = { matchStep-- }, modifier = Modifier.weight(1f), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) { Text("Atrás", color = Color.White, fontSize = sp(13), maxLines = 1) }
                        }
                        Button(onClick = { showMatchDialog = false }, modifier = Modifier.weight(1f), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp), colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceRed)) { Text("Cancelar", color = Color.White, fontSize = sp(13), maxLines = 1) }
                        if (matchStep < 4) {
                            Button(onClick = { matchStep++ }, modifier = Modifier.weight(1f), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp), colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceGreen)) { Text("Siguiente", color = Color.Black, fontSize = sp(13), maxLines = 1) }
                        } else {
                            Button(
                                onClick = {
                                    val parts = matchDateInput.split("-"); val dateForDb = "${parts[2]}-${parts[1]}-${parts[0]}"; val newMatch = com.example.entrenamientos.data.Match(id = matchToEdit?.id ?: 0, date = dateForDb, time = matchTimeInput, isLocal = matchIsLocalInput, location = matchLocationInput, opponent = matchOpponentInput, teamYear = selectedTeam); viewModel.addOrUpdateMatch(newMatch); showMatchDialog = false
                                },
                                modifier = Modifier.weight(1f),
                                enabled = matchLocationInput.isNotBlank() && matchOpponentInput.isNotBlank(),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceGreen)
                            ) { Text(if (matchToEdit == null) "Guardar" else "Actualizar", color = Color.Black, fontSize = sp(13), maxLines = 1) }
                        }
                    }
                }
            }
        }
    }

    // --- POP-UPS DE CONFIRMACIÓN DE ELIMINACIÓN ---
    if (teamToDelete != null) {
        AlertDialog(
            onDismissRequest = { teamToDelete = null },
            title = { Text("Eliminar Equipo", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
            text = { Text("¿Estás seguro de que quieres eliminar este equipo? Se borrarán también sus jugadoras y horarios.", textAlign = TextAlign.Center) },
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
            title = { Text("Eliminar Jugadora", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
            text = { Text("¿Estás seguro de que quieres eliminar a esta jugadora?", textAlign = TextAlign.Center) },
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

    if (showTeamWizard) {
        AddTeamWizard(viewModel) { showTeamWizard = false }
    }
}

@Composable
fun FullColorPicker(
    colorHex: String,
    onColorChanged: (String) -> Unit
) {
    val initialColorInt = try { android.graphics.Color.parseColor(colorHex) } catch (e: Exception) { android.graphics.Color.parseColor("#2196F3") }
    val hsl = FloatArray(3)
    androidx.core.graphics.ColorUtils.colorToHSL(initialColorInt, hsl)

    var hue by remember { mutableFloatStateOf(hsl[0]) }
    var lightness by remember { mutableFloatStateOf(hsl[2]) }

    // Usamos el HSL de Android directamente para evitar grises por conversiones de Compose
    val argb = androidx.core.graphics.ColorUtils.HSLToColor(floatArrayOf(hue, 1f, lightness))
    val currentColor = Color(argb)
    val pureArgb = androidx.core.graphics.ColorUtils.HSLToColor(floatArrayOf(hue, 1f, 0.5f))
    val pureColor = Color(pureArgb)

    val hex = String.format("#%06X", 0xFFFFFF and argb)

    androidx.compose.runtime.LaunchedEffect(hex) {
        if (hex != colorHex) {
            onColorChanged(hex)
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(MaterialTheme.shapes.small)
                .background(currentColor)
                .border(1.dp, Color.Black, MaterialTheme.shapes.small)
        )
        Spacer(modifier = Modifier.height(12.dp))

        Text("Color", style = MaterialTheme.typography.labelSmall)
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp) // Alinea el degradado exactamente con los topes del deslizador
                    .height(12.dp)
                    .clip(CircleShape)
                    .background(
                        androidx.compose.ui.graphics.Brush.horizontalGradient(
                            listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red)
                        )
                    )
            )
            Slider(
                value = hue,
                onValueChange = { hue = it },
                valueRange = 0f..360f,
                colors = SliderDefaults.colors(
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent,
                    thumbColor = Color.DarkGray
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text("Luminosidad", style = MaterialTheme.typography.labelSmall)
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp) // Alinea el degradado exactamente con los topes del deslizador
                    .height(12.dp)
                    .clip(CircleShape)
                    .background(
                        androidx.compose.ui.graphics.Brush.horizontalGradient(
                            listOf(Color.Black, pureColor, Color.White)
                        )
                    )
                    .border(0.5.dp, Color.Gray, CircleShape)
            )
            Slider(
                value = lightness,
                onValueChange = { lightness = it },
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent,
                    thumbColor = Color.Gray // Gris para que siempre sea visible en los extremos
                )
            )
        }
    }
}

@Composable
fun AddTeamWizard(viewModel: BasketViewModel, onDismiss: () -> Unit) {
    var step by remember { mutableIntStateOf(1) }

    var teamName by remember { mutableStateOf("") }
    var teamCategoryStr by remember { mutableStateOf("") }
    var selectedColorHex by remember { mutableStateOf("#2196F3") }

    var tempSchedules by remember { mutableStateOf(listOf<com.example.entrenamientos.data.TrainingSchedule>()) }
    val daysOfWeek = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
    val availableDays = (1..5).filter { d -> tempSchedules.none { it.dayOfWeek == d } }
    var schedDay by remember { mutableIntStateOf(availableDays.firstOrNull() ?: 1) }
    var schedStart by remember { mutableStateOf("17:00") }
    var schedEnd by remember { mutableStateOf("18:30") }
    val context = androidx.compose.ui.platform.LocalContext.current

    androidx.compose.runtime.LaunchedEffect(availableDays) {
        if (availableDays.isNotEmpty() && !availableDays.contains(schedDay)) {
            schedDay = availableDays.first()
        }
    }

    var tempPlayers by remember { mutableStateOf(listOf<com.example.entrenamientos.data.Player>()) }
    var pName by remember { mutableStateOf("") }
    var pLast by remember { mutableStateOf("") }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss, properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)) {
        Card(modifier = Modifier.fillMaxWidth(0.98f).padding(8.dp), shape = MaterialTheme.shapes.medium) {
            Column(modifier = Modifier.padding(16.dp)) {
                val title = when(step) { 1 -> "Datos del Equipo"; 2 -> "Entrenamientos"; else -> "Plantilla" }
                Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(16.dp))

                when (step) {
                    1 -> {
                        OutlinedTextField(value = teamName, onValueChange = { teamName = it }, label = { Text("Nombre del Equipo (ej: Infantil)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(value = teamCategoryStr, onValueChange = { if (it.all { char -> char.isDigit() }) teamCategoryStr = it }, label = { Text("Año de categoría (opcional)") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))
                        Spacer(modifier = Modifier.height(16.dp))

                        FullColorPicker(colorHex = selectedColorHex, onColorChanged = { selectedColorHex = it })
                    }
                    2 -> {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = {
                                    val idx = availableDays.indexOf(schedDay)
                                    if (idx > 0) schedDay = availableDays[idx - 1] else if(availableDays.isNotEmpty()) schedDay = availableDays.last()
                                },
                                enabled = availableDays.isNotEmpty()
                            ) { Text("-") }

                            Text(if (availableDays.isEmpty()) "Máximo Añadido" else daysOfWeek[schedDay - 1], fontWeight = FontWeight.Bold)

                            Button(
                                onClick = {
                                    val idx = availableDays.indexOf(schedDay)
                                    if (idx < availableDays.size - 1) schedDay = availableDays[idx + 1] else if(availableDays.isNotEmpty()) schedDay = availableDays.first()
                                },
                                enabled = availableDays.isNotEmpty()
                            ) { Text("+") }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            androidx.compose.material3.OutlinedButton(onClick = {
                                val parts = schedStart.split(":"); val h = parts.getOrNull(0)?.toIntOrNull() ?: 17; val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
                                android.app.TimePickerDialog(context, { _, hour, minute -> schedStart = String.format(java.util.Locale.getDefault(), "%02d:%02d", hour, minute) }, h, m, true).show()
                            }, enabled = availableDays.isNotEmpty()) { Text("Inicio: $schedStart", color = if(availableDays.isNotEmpty()) Color.Black else Color.Gray) }
                            androidx.compose.material3.OutlinedButton(onClick = {
                                val parts = schedEnd.split(":"); val h = parts.getOrNull(0)?.toIntOrNull() ?: 18; val m = parts.getOrNull(1)?.toIntOrNull() ?: 30
                                android.app.TimePickerDialog(context, { _, hour, minute -> schedEnd = String.format(java.util.Locale.getDefault(), "%02d:%02d", hour, minute) }, h, m, true).show()
                            }, enabled = availableDays.isNotEmpty()) { Text("Fin: $schedEnd", color = if(availableDays.isNotEmpty()) Color.Black else Color.Gray) }
                        }
                        Button(onClick = {
                            tempSchedules = tempSchedules + com.example.entrenamientos.data.TrainingSchedule(teamYear = 0, dayOfWeek = schedDay, startTime = schedStart, endTime = schedEnd)
                        }, modifier = Modifier.fillMaxWidth(), enabled = availableDays.isNotEmpty()) { Text("Añadir Entrenamiento") }

                        Spacer(modifier = Modifier.height(8.dp))
                        // Modificador que hace que crezca progresivamente y quita el hueco vacío
                        LazyColumn(modifier = Modifier.wrapContentHeight().heightIn(max = 150.dp)) {
                            items(tempSchedules) { s ->
                                Row(modifier = Modifier.fillMaxWidth().padding(4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("${daysOfWeek[s.dayOfWeek - 1]} ${s.startTime}-${s.endTime}")
                                    Icon(Icons.Default.Delete, null, modifier = Modifier.clickable { tempSchedules = tempSchedules - s }, tint = Color.Red)
                                }
                            }
                        }
                    }
                    3 -> {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = pName, onValueChange = { pName = it }, label = { Text("Nombre") }, singleLine = true, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = pLast, onValueChange = { pLast = it }, label = { Text("Apellido") }, singleLine = true, modifier = Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            if (pName.isNotBlank()) {
                                tempPlayers = tempPlayers + com.example.entrenamientos.data.Player(name = pName, lastName = pLast, teamYear = 0)
                                pName = ""; pLast = ""
                            }
                        }, modifier = Modifier.fillMaxWidth()) { Text("Añadir Jugadora") }
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyColumn(modifier = Modifier.wrapContentHeight().heightIn(max = 150.dp)) {
                            items(tempPlayers) { p ->
                                Row(modifier = Modifier.fillMaxWidth().padding(4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("${p.name} ${p.lastName}")
                                    Icon(Icons.Default.Delete, null, modifier = Modifier.clickable { tempPlayers = tempPlayers - p }, tint = Color.Red)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (step > 1) {
                            Button(onClick = { step-- }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) { Text("Atrás", color = Color.White) }
                        }
                        if (step < 3) {
                            Button(onClick = {
                                if (step == 1 && teamName.isBlank()) {
                                    android.widget.Toast.makeText(context, "Rellena nombre del equipo", android.widget.Toast.LENGTH_SHORT).show()
                                } else if (step == 2 && tempSchedules.isEmpty()) {
                                    android.widget.Toast.makeText(context, "Añade al menos un entrenamiento", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    step++
                                }
                            }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceGreen)) { Text("Siguiente", color = Color.Black) }
                        } else {
                            Button(onClick = {
                                viewModel.addTeamWithDetails(teamName, teamCategoryStr, selectedColorHex, tempPlayers, tempSchedules)
                                onDismiss()
                            }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceGreen)) { Text("Finalizar", color = Color.Black) }
                        }
                    }
                    Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceRed)) { Text("Cancelar", color = Color.White) }
                }
            }
        }
    }
}

@Composable
fun FestivosSettingsTab(viewModel: BasketViewModel) {
    val holidays by viewModel.holidays.collectAsState()
    var newHolidayDate by remember { mutableStateOf("") }
    var holidayToDelete by remember { mutableStateOf<com.example.entrenamientos.data.Holiday?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val scale = (configuration.screenWidthDp / 360f).coerceIn(0.85f, 1.25f)
    fun sp(base: Int) = (base * scale).sp

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Añadir Día Festivo", style = MaterialTheme.typography.titleMedium.copy(fontSize = sp(16)))
        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.material3.OutlinedButton(
                onClick = {
                    val cal = java.util.Calendar.getInstance()
                    if (newHolidayDate.isNotEmpty()) {
                        try {
                            val parts = newHolidayDate.split("-")
                            cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
                        } catch (_: Exception) {}
                    }

                    val y = cal.get(java.util.Calendar.YEAR)
                    val m = cal.get(java.util.Calendar.MONTH)
                    val d = cal.get(java.util.Calendar.DAY_OF_MONTH)

                    android.app.DatePickerDialog(context, { _, year, month, day ->
                        newHolidayDate = String.format(java.util.Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day)
                    }, y, m, d).show()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = newHolidayDate.ifEmpty { "Seleccionar fecha" },
                    color = if (newHolidayDate.isEmpty()) Color.Gray else Color.Black,
                    fontSize = sp(13)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (newHolidayDate.isNotEmpty()) {
                        viewModel.addHoliday(newHolidayDate)
                        newHolidayDate = ""
                    } else {
                        android.widget.Toast.makeText(context, "Por favor, selecciona una fecha primero", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.DarkGray,
                    disabledContainerColor = Color.LightGray
                ),
                enabled = newHolidayDate.isNotEmpty()
            ) {
                Text("Añadir", color = if (newHolidayDate.isNotEmpty()) Color.White else Color.DarkGray, fontSize = sp(13))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Festivos Registrados", style = MaterialTheme.typography.titleMedium.copy(fontSize = sp(16)))
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(holidays.sortedBy { it.date }) { holiday ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(Color.LightGray.copy(alpha = 0.2f), MaterialTheme.shapes.small)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val hDate = java.time.LocalDate.parse(holiday.date)
                    val formatted = "${hDate.dayOfMonth} de ${hDate.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES"))} ${hDate.year}"

                    Text(formatted, style = MaterialTheme.typography.bodyLarge.copy(fontSize = sp(15)), color = Color.Red, fontWeight = FontWeight.Bold)

                    IconButton(onClick = { holidayToDelete = holiday }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = com.example.entrenamientos.ui.theme.AttendanceRed)
                    }
                }
            }
        }
    }

    // --- POP-UP DE CONFIRMACIÓN DE ELIMINACIÓN ---
    if (holidayToDelete != null) {
        AlertDialog(
            onDismissRequest = { holidayToDelete = null },
            title = { Text("Eliminar Festivo", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
            text = { Text("¿Estás seguro de que quieres eliminar este día festivo?", textAlign = TextAlign.Center) },
            confirmButton = {},
            dismissButton = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { holidayToDelete = null }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) { Text("Cancelar", color = Color.White) }
                    Button(onClick = { viewModel.removeHoliday(holidayToDelete!!); holidayToDelete = null }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceRed)) { Text("Eliminar", color = Color.White) }
                }
            }
        )
    }
}