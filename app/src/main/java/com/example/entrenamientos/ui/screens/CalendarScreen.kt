package com.example.entrenamientos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.entrenamientos.data.TrainingSchedule
import com.example.entrenamientos.ui.BasketViewModel

@Composable
fun CalendarScreen(
    viewModel: BasketViewModel = hiltViewModel(),
    navController: NavController
) {
    val teamsList by viewModel.teams.collectAsState()
    val schedules by viewModel.schedules.collectAsState()
    val allMatches by viewModel.matches.collectAsState()

    val minMonth = remember(teamsList) {
        val earliest = teamsList
            .mapNotNull { team ->
                team.firstTrainingDate
                    .takeIf { it.isNotBlank() }
                    ?.let {
                        try {
                            java.time.LocalDate.parse(it)
                        } catch (_: Exception) {
                            null
                        }
                    }
            }
            .minOrNull()
            ?: java.time.LocalDate.of(2026, 9, 1)

        java.time.YearMonth.from(earliest)
    }

    val maxMonth = java.time.YearMonth.of(2027, 7)

    var currentMonth by remember {
        mutableStateOf(java.time.YearMonth.of(2026, 9))
    }

    var showDayDialog by remember {
        mutableStateOf(false)
    }

    var clickedDate by remember {
        mutableStateOf<java.time.LocalDate?>(null)
    }

    LaunchedEffect(minMonth) {
        if (currentMonth.isBefore(minMonth)) {
            currentMonth = minMonth
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    currentMonth = currentMonth.minusMonths(1)
                },
                enabled = currentMonth.isAfter(minMonth)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Mes anterior"
                )
            }

            Text(
                text = "${currentMonth.month.getDisplayName(
                    java.time.format.TextStyle.FULL,
                    java.util.Locale("es", "ES")
                ).uppercase()} ${currentMonth.year}",
                style = MaterialTheme.typography.titleLarge
            )

            IconButton(
                onClick = {
                    currentMonth = currentMonth.plusMonths(1)
                },
                enabled = currentMonth.isBefore(maxMonth)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Mes siguiente"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val days = mutableListOf<java.time.LocalDate?>()

        val firstDayOfWeek =
            currentMonth.atDay(1).dayOfWeek.value

        for (i in 1 until firstDayOfWeek) {
            days.add(null)
        }

        for (i in 1..currentMonth.lengthOfMonth()) {
            days.add(currentMonth.atDay(i))
        }

        while (days.size % 7 != 0) {
            days.add(null)
        }

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
                listOf("L", "M", "X", "J", "V", "S", "D")
                    .forEach { day ->
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
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    week.forEach { dayDate ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            if (dayDate != null) {
                                DayCell(
                                    date = dayDate,
                                    viewModel = viewModel,
                                    schedules = schedules,
                                    onClick = {
                                        viewModel.setSelectedDate(
                                            dayDate.toString()
                                        )

                                        clickedDate = dayDate
                                        showDayDialog = true
                                    }
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Color(0xFFF5F5F5)
                                        )
                                )
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
            onDismiss = {
                showDayDialog = false
            }
        )
    }
}

@Composable
fun DayCell(
    date: java.time.LocalDate,
    viewModel: BasketViewModel,
    schedules: List<TrainingSchedule>,
    onClick: () -> Unit
) {
    val isHoliday = viewModel.isHoliday(date)
    val dayValue = date.dayOfWeek.value

    val teamsList by viewModel.teams.collectAsState()
    val matches by viewModel.matches.collectAsState()

    val earliestTrainingDate = teamsList
        .mapNotNull { team ->
            team.firstTrainingDate
                .takeIf { it.isNotBlank() }
                ?.let {
                    try {
                        java.time.LocalDate.parse(it)
                    } catch (_: Exception) {
                        null
                    }
                }
        }
        .minOrNull()
        ?: java.time.LocalDate.of(2026, 9, 1)

    val isBeforeSeason = date.isBefore(earliestTrainingDate)
    val isPastDay = date.isBefore(java.time.LocalDate.now())

    val cellBackgroundColor = when {
        isBeforeSeason -> Color.LightGray.copy(alpha = 0.4f)
        isHoliday -> Color(0xFFFFEBEE)
        isPastDay -> Color(0xFFFFF5F5)
        else -> Color.White
    }

    val daySchedules = if (isHoliday || isBeforeSeason) {
        emptyList()
    } else {
        schedules.filter { schedule ->
            if (schedule.dayOfWeek != dayValue) return@filter false
            val team = teamsList.find { it.year == schedule.teamYear }
            val firstDateStr = team?.firstTrainingDate ?: "2026-09-01"
            val firstDate = try { java.time.LocalDate.parse(firstDateStr) } catch (_: Exception) { java.time.LocalDate.of(2026, 9, 1) }
            !date.isBefore(firstDate)
        }.sortedBy { it.startTime }
    }

    val matchesForDay = matches.filter { it.date == date.toString() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(cellBackgroundColor)
            .clickable(enabled = !isBeforeSeason && (daySchedules.isNotEmpty() || matchesForDay.isNotEmpty())) { onClick() }
            .padding(2.dp)
    ) {
        // Día del mes siempre en su lugar arriba
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyLarge,
            color = when {
                isHoliday && !isBeforeSeason -> Color.Red
                isBeforeSeason -> Color.Gray
                else -> Color.Unspecified
            },
            fontWeight = if (isHoliday && !isBeforeSeason) FontWeight.ExtraBold else FontWeight.SemiBold
        )

        // Contenedor que usa todo el espacio restante para centrar verticalmente su contenido
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Círculos de los partidos
            if (matchesForDay.isNotEmpty() && !isBeforeSeason) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    matchesForDay.forEach { match ->
                        val matchTeam = teamsList.find { it.year == match.teamYear }
                        val circleColor = matchTeam?.colorHex?.let {
                            try { Color(android.graphics.Color.parseColor(it)) } catch (_: Exception) { Color.Gray }
                        } ?: Color.Gray

                        // Círculo de partido todavía más grande (26.dp)
                        Box(modifier = Modifier.size(26.dp).clip(CircleShape).background(circleColor))
                    }
                }
                if (daySchedules.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            // TAMAÑO FIJO PARA LOS BLOQUES DE ENTRENAMIENTO
            val boxHeight = 22.dp
            val textSize = 11.sp

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                daySchedules.forEachIndexed { index, schedule ->
                    val team = teamsList.find { it.year == schedule.teamYear }
                    val bgColor = team?.colorHex?.let {
                        try { Color(android.graphics.Color.parseColor(it)) } catch (_: Exception) { Color.Gray }
                    } ?: Color.Gray

                    val displayTitle = team?.shortName?.takeIf { it.isNotBlank() }?.take(6)?.uppercase() ?: team?.name?.take(6)?.uppercase() ?: ""

                    // Separador fino entre cajas con más margen blanco para que se aprecie la separación
                    if (index > 0) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.90f) // Ligeramente más estrecha para dar un toque elegante
                                .height(1.dp)
                                .background(Color.Black)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .height(boxHeight)
                            .background(bgColor, shape = MaterialTheme.shapes.extraSmall),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = displayTitle,
                            color = Color.White,
                            fontSize = textSize,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
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
    val allMatches by viewModel.matches.collectAsState()
    val dayMatches = allMatches.filter { it.date == date.toString() }
    val teamsList by viewModel.teams.collectAsState()

    val allAttendances by viewModel.attendances.collectAsState()

    val dayOfWeek = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES")).replaceFirstChar { it.uppercase() }
    val dayNumber = date.dayOfMonth
    val monthName = date.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES"))

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val scale = (configuration.screenWidthDp / 360f).coerceIn(0.85f, 1.25f)

    // LÓGICA DE AUTOAJUSTE DINÁMICO DE PANTALLA
    val totalBlocks = teams.size + dayMatches.size
    val isDense = totalBlocks >= 3
    val isMedium = totalBlocks == 2

    val uniformSpace = if (isDense) (6 * scale).dp else if (isMedium) (12 * scale).dp else (16 * scale).dp
    val btnInternalSpace = if (isDense) (4 * scale).dp else if (isMedium) (6 * scale).dp else (8 * scale).dp
    val btnHeight = if (isDense) (32 * scale).dp else if (isMedium) (40 * scale).dp else (48 * scale).dp
    val backBtnHeight = if (isDense) (32 * scale).dp else (36 * scale).dp
    val fontSizeTitle = if (isDense) (14 * scale).sp else (16 * scale).sp
    val fontSizeTeam = if (isDense) (14 * scale).sp else (16 * scale).sp
    val fontSizeBtn = if (isDense) (12 * scale).sp else if (isMedium) (13 * scale).sp else (14 * scale).sp
    val iconSize = if (isDense) (16 * scale).dp else (20 * scale).dp
    val btnPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)

    androidx.compose.ui.window.Dialog(
        onDismissRequest = { },
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.95f).wrapContentHeight(),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(androidx.compose.foundation.rememberScrollState())
                    .padding(uniformSpace),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(iconSize),
                        tint = Color.Black
                    )
                    Spacer(Modifier.width(8.dp * scale))
                    Text(
                        text = "$dayOfWeek $dayNumber de $monthName",
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = fontSizeTitle,
                        color = Color.Black,
                        fontWeight = FontWeight.Normal
                    )
                }

                if (teams.isNotEmpty() || dayMatches.isNotEmpty()) {
                    Spacer(Modifier.height(uniformSpace))
                    HorizontalDivider(color = Color.Black, thickness = 1.dp)
                    Spacer(Modifier.height(uniformSpace))
                }

                if (teams.isNotEmpty()) {
                    teams.forEachIndexed { index, teamYearLoop ->
                        if (index > 0) {
                            Spacer(Modifier.height(uniformSpace))
                            HorizontalDivider(color = Color.Black, thickness = 1.dp)
                            Spacer(Modifier.height(uniformSpace))
                        }

                        val team = teamsList.find { it.year == teamYearLoop }
                        val teamDisplayName = team?.shortName?.takeIf { it.isNotBlank() } ?: team?.name ?: "Equipo $teamYearLoop"
                        val teamColor = team?.colorHex?.let {
                            try { Color(android.graphics.Color.parseColor(it)) } catch (_: Exception) { Color.Gray }
                        } ?: Color.Gray

                        Text(
                            text = teamDisplayName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = fontSizeTeam,
                            color = teamColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(uniformSpace))

                        val attendanceForTeam = allAttendances.filter { it.date == date.toString() && it.teamYear == teamYearLoop }

                        val attendanceLabel = if (attendanceForTeam.isNotEmpty()) "Ver Asistencia" else "Asistencia"
                        val trainingBtnModifier = Modifier.fillMaxWidth().height(btnHeight)

                        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(btnInternalSpace)) {
                            Button(
                                onClick = {
                                    viewModel.setSelectedTeamYear(teamYearLoop)
                                    viewModel.setSelectedDate(date.toString())
                                    onDismiss()
                                    navController.navigate("notes/ENTRENAMIENTO")
                                },
                                modifier = trainingBtnModifier,
                                colors = ButtonDefaults.buttonColors(containerColor = teamColor),
                                contentPadding = btnPadding
                            ) {
                                Text("Entrenamiento", color = Color.White, maxLines = 1, fontSize = fontSizeBtn)
                            }

                            Button(
                                onClick = {
                                    viewModel.setSelectedTeamYear(teamYearLoop)
                                    viewModel.setSelectedDate(date.toString())
                                    onDismiss()
                                    navController.navigate("attendance")
                                },
                                modifier = trainingBtnModifier,
                                colors = ButtonDefaults.buttonColors(containerColor = teamColor),
                                contentPadding = btnPadding
                            ) {
                                Text(attendanceLabel, color = Color.White, maxLines = 1, fontSize = fontSizeBtn)
                            }

                            Button(
                                onClick = {
                                    viewModel.setSelectedTeamYear(teamYearLoop)
                                    viewModel.setSelectedDate(date.toString())
                                    onDismiss()
                                    navController.navigate("notes/OTROS")
                                },
                                modifier = trainingBtnModifier,
                                colors = ButtonDefaults.buttonColors(containerColor = teamColor),
                                contentPadding = btnPadding
                            ) {
                                Text("Notas", color = Color.White, maxLines = 1, fontSize = fontSizeBtn)
                            }
                        }
                    }
                }

                if (dayMatches.isNotEmpty()) {
                    dayMatches.forEachIndexed { index, match ->
                        if (teams.isNotEmpty() || index > 0) {
                            Spacer(Modifier.height(uniformSpace))
                            HorizontalDivider(color = Color.Black, thickness = 1.dp)
                            Spacer(Modifier.height(uniformSpace))
                        }

                        val matchTeam = teamsList.find { it.year == match.teamYear }
                        val matchTeamNameBase = matchTeam?.name ?: "Equipo ${match.teamYear}"
                        val matchTeamDisplayName = if (match.isLocal) "$matchTeamNameBase vs ${match.opponent}" else "${match.opponent} vs $matchTeamNameBase"
                        val matchTeamColor = matchTeam?.colorHex?.let {
                            try { Color(android.graphics.Color.parseColor(it)) } catch (_: Exception) { Color.DarkGray }
                        } ?: Color.DarkGray

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(iconSize), tint = Color.Black)
                            Spacer(Modifier.width(8.dp * scale))
                            Text(
                                text = "${match.time} - Polid. ${match.location}",
                                style = MaterialTheme.typography.titleMedium,
                                fontSize = fontSizeTitle,
                                color = Color.Black,
                                fontWeight = FontWeight.Normal
                            )
                        }

                        Spacer(Modifier.height(8.dp * scale))

                        Text(
                            text = matchTeamDisplayName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = fontSizeTeam,
                            color = matchTeamColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(uniformSpace))

                        val teamAttendances = allAttendances.filter { it.teamYear == match.teamYear }
                        val (canMake, _) = viewModel.canMakeConvocatoria(date, match.teamYear, teamAttendances)

                        val convocatoriaGuardada = match.isConvocatoriaSaved
                        val convocatoriaEnabled = convocatoriaGuardada || canMake
                        val quintetosEnabled = convocatoriaGuardada
                        val resultadoEnabled = convocatoriaGuardada
                        val matchButtonModifier = Modifier.fillMaxWidth().height(btnHeight)

                        if (!convocatoriaEnabled) {
                            Text(
                                text = "Rellena todas las asistencias anteriores para continuar",
                                color = Color.Red,
                                fontSize = (13 * scale).sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(bottom = btnInternalSpace)
                            )
                        }

                        Column(
                            Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(btnInternalSpace)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.setSelectedTeamYear(match.teamYear)
                                    viewModel.setSelectedDate(date.toString())
                                    onDismiss()
                                    navController.navigate("convocatoria")
                                },
                                enabled = convocatoriaEnabled,
                                modifier = matchButtonModifier,
                                colors = ButtonDefaults.buttonColors(containerColor = matchTeamColor, disabledContainerColor = Color.LightGray),
                                contentPadding = btnPadding
                            ) {
                                Text(
                                    text = if (convocatoriaGuardada) "Ver Convocatoria" else "Convocatoria",
                                    color = Color.White, maxLines = 1, fontSize = fontSizeBtn
                                )
                            }

                            Button(
                                onClick = {
                                    viewModel.setSelectedTeamYear(match.teamYear)
                                    viewModel.setSelectedDate(date.toString())
                                    onDismiss()
                                    navController.navigate("quintetos")
                                },
                                enabled = quintetosEnabled,
                                modifier = matchButtonModifier,
                                colors = ButtonDefaults.buttonColors(containerColor = matchTeamColor, disabledContainerColor = Color.LightGray),
                                contentPadding = btnPadding
                            ) {
                                Text("Quintetos", color = Color.White, maxLines = 1, fontSize = fontSizeBtn)
                            }

                            Button(
                                onClick = {
                                    viewModel.setSelectedTeamYear(match.teamYear)
                                    viewModel.setSelectedDate(date.toString())
                                    onDismiss()
                                    navController.navigate("resultado")
                                },
                                enabled = resultadoEnabled,
                                modifier = matchButtonModifier,
                                colors = ButtonDefaults.buttonColors(containerColor = matchTeamColor, disabledContainerColor = Color.LightGray),
                                contentPadding = btnPadding
                            ) {
                                Text(
                                    text = if (match.resultLocal != null) "Ver Resultado" else "Resultado",
                                    color = Color.White, maxLines = 1, fontSize = fontSizeBtn
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(uniformSpace))
                HorizontalDivider(color = Color.Black, thickness = 1.dp)
                Spacer(Modifier.height(uniformSpace))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(backBtnHeight),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                    contentPadding = btnPadding
                ) {
                    Text("Volver", color = Color.White, maxLines = 1, fontSize = fontSizeBtn)
                }
            }
        }
    }
}