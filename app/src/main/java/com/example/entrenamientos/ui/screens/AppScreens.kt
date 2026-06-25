package com.example.entrenamientos.ui.screens

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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.entrenamientos.ui.BasketViewModel

val PrebenjaminPink = Color(0xFFFF80AB)
val InfantilBlue = Color(0xFF2196F3)

@Composable
fun CalendarScreen(
    viewModel: BasketViewModel = hiltViewModel(),
    navController: NavController
) {
    var currentMonth by remember { mutableStateOf(java.time.YearMonth.of(2026, 9)) }
    val minMonth = java.time.YearMonth.of(2026, 9)
    val maxMonth = java.time.YearMonth.of(2027, 5)

    // Estados para controlar el diálogo
    var showDayDialog by remember { mutableStateOf(false) }
    var clickedDate by remember { mutableStateOf<java.time.LocalDate?>(null) }

    // Observamos los cambios en los horarios
    val schedules by viewModel.schedules.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // --- Navegación de Meses ---
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

        // --- Cabecera de días ---
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("L", "M", "X", "J", "V", "S", "D").forEach { day ->
                Text(day, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // --- Generación de días ---
        val days = mutableListOf<java.time.LocalDate?>()
        val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value
        for (i in 1 until firstDayOfWeek) days.add(null)
        for (i in 1..currentMonth.lengthOfMonth()) days.add(currentMonth.atDay(i))
        while (days.size % 7 != 0) days.add(null)

        Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
            val weeks = days.chunked(7)
            weeks.forEach { week ->
                Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    week.forEach { date ->
                        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                            if (date != null) {
                                DayCell(
                                    date = date,
                                    viewModel = viewModel,
                                    schedules = schedules,
                                    onClick = {
                                        // Guardamos la fecha clicada y activamos el diálogo
                                        viewModel.setSelectedDate(date.toString())
                                        clickedDate = date
                                        showDayDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Diálogo emergente (Popup centrado) ---
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
    val teams = if (isHoliday) emptyList() else schedules.filter { it.dayOfWeek == dayValue }.map { it.teamYear }.distinct()
    val match = viewModel.getMatchForDate(date)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(2.dp)
            .background(if (isHoliday) Color(0xFFFFEBEE) else Color.White, shape = MaterialTheme.shapes.small)
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        // Número del día
        Text(
            text = date.dayOfMonth.toString(),
            modifier = Modifier.align(Alignment.TopStart),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isHoliday) Color.Red else Color.Unspecified,
            fontWeight = if (isHoliday) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
        )

        // Contenido centrado con espaciado uniforme
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // 1. Círculo del partido
            if (match != null) {
                val circleColor = viewModel.getMatchColor(match)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(circleColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (match.isLocal) "L" else "V",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }
            }

            // 2. Entrenamiento 2018 (Rosa)
            if (teams.contains(2018)) {
                Box(
                    modifier = Modifier.fillMaxWidth(0.95f).height(24.dp).background(com.example.entrenamientos.ui.theme.PrebenjaminPink, shape = MaterialTheme.shapes.extraSmall),
                    contentAlignment = Alignment.Center
                ) {
                    Text("2018", color = Color.White, fontSize = 12.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
            }

            // 3. Línea divisoria (solo si hay ambos)
            if (teams.contains(2018) && teams.contains(2013)) {
                Box(modifier = Modifier.fillMaxWidth(0.8f).height(1.dp).background(Color.Black))
            }

            // 4. Entrenamiento 2013 (Azul)
            if (teams.contains(2013)) {
                Box(
                    modifier = Modifier.fillMaxWidth(0.95f).height(24.dp).background(com.example.entrenamientos.ui.theme.InfantilBlue, shape = MaterialTheme.shapes.extraSmall),
                    contentAlignment = Alignment.Center
                ) {
                    Text("2013", color = Color.White, fontSize = 12.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
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

    val dayOfWeek = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES")).replaceFirstChar { it.uppercase() }
    val monthName = date.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES"))

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                // --- CABECERA ---
                if (hasMatch && match != null) {
                    Column(horizontalAlignment = androidx.compose.ui.Alignment.Start) {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Icon(androidx.compose.material.icons.Icons.Default.DateRange, null, Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("$dayOfWeek ${date.dayOfMonth} de $monthName - ${match.time}", style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Icon(androidx.compose.material.icons.Icons.Default.Sports, null, Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (match.isLocal) "CD Huerto - ${match.opponent}" else "${match.opponent} - CD Huerto",
                                style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Icon(androidx.compose.material.icons.Icons.Default.LocationOn, null, Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Polideportivo ${match.location}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        }
                    }
                } else {
                    Text("Opciones del $dayOfWeek ${date.dayOfMonth} de $monthName", style = MaterialTheme.typography.titleMedium)
                }

                Spacer(Modifier.height(16.dp))

                // --- ENTRENAMIENTOS ---
                if (teams.isNotEmpty()) {
                    teams.forEachIndexed { index, teamYear ->
                        // Línea separadora si hay más de un entrenamiento
                        if (index > 0) {
                            HorizontalDivider(color = Color.Black, thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))
                        }

                        val teamName = if (teamYear == 2018) "Prebenjamines (2018)" else "Infantiles (2013)"
                        val teamColor = if (teamYear == 2018) com.example.entrenamientos.ui.theme.PrebenjaminPink else com.example.entrenamientos.ui.theme.InfantilBlue

                        Text(teamName, style = MaterialTheme.typography.bodyMedium, color = teamColor)

                        // Fila 1: Entrenamiento y Asistencia
                        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                            Button(onClick = { viewModel.setSelectedTeamYear(teamYear); onDismiss(); navController.navigate("notes/ENTRENAMIENTO") }, colors = ButtonDefaults.buttonColors(containerColor = teamColor)) { Text("Entrenamiento", color = Color.White) }
                            Button(onClick = { viewModel.setSelectedTeamYear(teamYear); onDismiss(); navController.navigate("attendance") }, colors = ButtonDefaults.buttonColors(containerColor = teamColor)) { Text("Asistencia", color = Color.White) }
                        }

                        // Fila 2: Notas (Centrado)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            Button(onClick = { viewModel.setSelectedTeamYear(teamYear); onDismiss(); navController.navigate("notes/OTROS") }, colors = ButtonDefaults.buttonColors(containerColor = teamColor)) { Text("Notas", color = Color.White) }
                        }
                    }
                }

                // --- PARTIDOS ---
                if (hasMatch && match != null) {
                    HorizontalDivider(color = Color.Black, thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))

                    val (canMake, _) = viewModel.canMakeConvocatoria(date)
                    val isConvocatoriaEnabled = canMake || match.isConvocatoriaSaved

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Button(
                            onClick = { viewModel.setSelectedDate(date.toString()); onDismiss(); navController.navigate("convocatoria") },
                            enabled = isConvocatoriaEnabled,
                            colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.InfantilBlue, disabledContainerColor = Color.LightGray)
                        ) { Text(if (match.isConvocatoriaSaved) "Ver Convocatoria" else "Convocatoria") }

                        Button(
                            onClick = { viewModel.setSelectedDate(date.toString()); onDismiss(); navController.navigate("resultado") },
                            enabled = match.isConvocatoriaSaved,
                            colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.InfantilBlue, disabledContainerColor = Color.LightGray)
                        ) { Text(if (match.resultLocal != null) "Ver Resultado" else "Resultado") }
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

    val teamName = if (teamYear == 2018) "Prebenjamines" else "Infantiles"

    val date = java.time.LocalDate.parse(dateStr)
    val dayOfWeek = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES")).replaceFirstChar { it.uppercase() }
    val monthName = date.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES"))

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Asistencia $teamYear:", style = MaterialTheme.typography.headlineMedium)
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

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(bgColor)
                        .clickable {
                            attendanceState[player.id] = viewModel.getNextAttendanceStatus(status)
                        }
                        .padding(16.dp)
                ) {
                    Text(text = player.name, style = MaterialTheme.typography.bodyLarge, color = Color.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        com.example.entrenamientos.ui.components.ActionButtonsRow(
            onCancel = { navController.popBackStack() },
            onSave = {
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
            }
        )
    }
}

@Composable
fun ConvocatoriaScreen(viewModel: BasketViewModel, navController: NavController) {
    val selectedDateStr by viewModel.selectedDate.collectAsState()
    val date = java.time.LocalDate.parse(selectedDateStr)
    val match = viewModel.getMatchForDate(date) ?: return

    val players by viewModel.getPlayersForTeam(2013).collectAsState(initial = emptyList())
    val context = androidx.compose.ui.platform.LocalContext.current

    var isEditMode by remember { mutableStateOf(!match.isConvocatoriaSaved) }

    var summonedIds by remember(players, match) {
        mutableStateOf(
            if (match.isConvocatoriaSaved) match.summonedPlayers.toSet()
            else players.map { it.id }.toSet()
        )
    }
    var reasonsMap by remember { mutableStateOf(match.unsummonedReasons.toMutableMap()) }

    var playerToUnsummon by remember { mutableStateOf<com.example.entrenamientos.data.Player?>(null) }
    val reasonOptions = listOf("Rotación", "Lesión", "Falta a entrenamientos", "Castigada", "No puede ir")

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(if (isEditMode) "Crear/Editar Convocatoria" else "Convocatoria Oficial", style = MaterialTheme.typography.headlineMedium)
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
                    Text("• ${p.name}", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 16.dp, bottom = 4.dp))
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("❌ DESCONVOCADAS", style = MaterialTheme.typography.titleMedium, color = com.example.entrenamientos.ui.theme.AttendanceRed)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(desconvocadas) { p ->
                    Column(modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)) {
                        Text("• ${p.name}", style = MaterialTheme.typography.bodyLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        Text("Motivo: ${match.unsummonedReasons[p.id]?.takeIf { it.isNotBlank() } ?: "Sin motivo especificado"}", style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(
                    onClick = { navController.popBackStack() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    modifier = Modifier.weight(1f)
                ) { Text("Volver") }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        val resetMatch = match.copy(isConvocatoriaSaved = false, summonedPlayers = emptyList(), unsummonedReasons = emptyMap())
                        viewModel.addOrUpdateMatch(resetMatch)
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceRed),
                    modifier = Modifier.weight(1f)
                ) { Text("Eliminar") }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { isEditMode = true },
                    colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.InfantilBlue),
                    modifier = Modifier.weight(1f)
                ) { Text("Editar") }
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
                        Text(player.name, style = MaterialTheme.typography.bodyLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = containerColor)

                        if (isSummoned) {
                            Icon(imageVector = androidx.compose.material.icons.Icons.Default.Check, contentDescription = null, tint = containerColor)
                        } else {
                            Text(reasonsMap[player.id] ?: "", style = MaterialTheme.typography.bodyMedium, color = containerColor, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
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
            ) { Text("Guardar Convocatoria", color = Color.Black) }
        }
    }

    if (playerToUnsummon != null) {
        AlertDialog(
            onDismissRequest = { playerToUnsummon = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .fillMaxWidth(0.95f) // Ocupa el 95% del ancho de la pantalla
                .padding(16.dp),
            title = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Motivo de desconvocatoria:", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = playerToUnsummon?.name ?: "",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
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
                                fontSize = 18.sp,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
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
fun ResultadoScreen(viewModel: BasketViewModel, navController: NavController) {
    val selectedDateStr by viewModel.selectedDate.collectAsState()
    val date = java.time.LocalDate.parse(selectedDateStr)
    val match = viewModel.getMatchForDate(date) ?: return // Si no hay partido, salimos

    // Estados para los campos de texto
    var resLocal by remember { mutableStateOf(match.resultLocal?.toString() ?: "") }
    var resVisitor by remember { mutableStateOf(match.resultVisitor?.toString() ?: "") }
    var ftMade by remember { mutableStateOf(match.ftMade.toString()) }
    var ftAttempted by remember { mutableStateOf(match.ftAttempted.toString()) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Resultado del Partido", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // Marcador
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

        // Tiros Libres
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
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceGreen)
        ) {
            Text(if (match.resultLocal != null) "Actualizar Resultado" else "Guardar Resultado", color = Color.Black)
        }
    }
}

@Composable
fun TrainingNoteScreen(viewModel: BasketViewModel = hiltViewModel(), navController: NavController, noteType: String) {
    val dateStr by viewModel.selectedDate.collectAsState()
    val teamYear by viewModel.selectedTeamYear.collectAsState()

    val existingNote by viewModel.getTrainingNoteForDateAndTeam(dateStr, teamYear, noteType).collectAsState(initial = null)

    var noteContent by remember(existingNote) { mutableStateOf(existingNote?.content ?: "") }

    val titlePrefix = if (noteType == "ENTRENAMIENTO") "Entrenamiento" else "Notas"

    val date = java.time.LocalDate.parse(dateStr)
    val dayOfWeek = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES")).replaceFirstChar { it.uppercase() }
    val monthName = date.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES"))

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "$titlePrefix $teamYear:", style = MaterialTheme.typography.headlineMedium)
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

        com.example.entrenamientos.ui.components.ActionButtonsRow(
            onCancel = { navController.popBackStack() },
            onSave = {
                viewModel.saveTrainingNote(date = dateStr, teamYear = teamYear, type = noteType, content = noteContent)
                navController.popBackStack()
            }
        )
    }
}

@Composable
fun StatsScreen(viewModel: BasketViewModel) {
    var selectedTeam by remember { mutableStateOf(2013) }

    val attendances by viewModel.getAllAttendancesByTeam(selectedTeam).collectAsState(initial = emptyList())
    val allMatches by viewModel.matches.collectAsState()

    val players by viewModel.getPlayersForTeam(selectedTeam).collectAsState(initial = emptyList())

    val expandedAttendanceMonths = remember { androidx.compose.runtime.mutableStateMapOf<java.time.YearMonth, Boolean>() }
    val expandedMatchMonths = remember { androidx.compose.runtime.mutableStateMapOf<java.time.YearMonth, Boolean>() }
    var isSeasonAttendanceExpanded by remember { mutableStateOf(false) }
    var isSeasonMatchesExpanded by remember { mutableStateOf(false) }

    var isSeasonDetailsExpanded by remember { mutableStateOf(false) }
    val expandedWeekDetails = remember { androidx.compose.runtime.mutableStateMapOf<java.time.LocalDate, Boolean>() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Panel de Estadísticas", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // Selector de Equipo
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(
                onClick = { selectedTeam = 2018 },
                colors = ButtonDefaults.buttonColors(containerColor = if (selectedTeam == 2018) com.example.entrenamientos.ui.theme.PrebenjaminPink else Color.Gray)
            ) { Text("Prebenjamines") }

            Button(
                onClick = { selectedTeam = 2013 },
                colors = ButtonDefaults.buttonColors(containerColor = if (selectedTeam == 2013) com.example.entrenamientos.ui.theme.InfantilBlue else Color.Gray)
            ) { Text("Infantiles") }
        }
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {

            // --- 1. ASISTENCIAS A ENTRENAMIENTOS ---
            item {
                Text("Asistencia Mensual / Semanal", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (attendances.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                            .clickable { isSeasonAttendanceExpanded = !isSeasonAttendanceExpanded }
                            .padding(vertical = 4.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "TEMPORADA",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (selectedTeam == 2018) com.example.entrenamientos.ui.theme.PrebenjaminPink else com.example.entrenamientos.ui.theme.InfantilBlue
                        )
                        Icon(
                            imageVector = if (isSeasonAttendanceExpanded) androidx.compose.material.icons.Icons.Default.KeyboardArrowUp else androidx.compose.material.icons.Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expandir/Colapsar",
                            tint = Color.Gray
                        )
                    }
                }

                if (isSeasonAttendanceExpanded) {
                    val trainingsInSeason = attendances.groupBy { it.date }.size

                    val totalPresent = attendances.count { it.status == 0 }
                    val totalJustified = attendances.count { it.status == 1 }
                    val totalUnjustified = attendances.count { it.status == 2 }

                    val avgPresent = if (trainingsInSeason > 0) totalPresent.toFloat() / trainingsInSeason else 0f
                    val avgJustified = if (trainingsInSeason > 0) totalJustified.toFloat() / trainingsInSeason else 0f
                    val avgUnjustified = if (trainingsInSeason > 0) totalUnjustified.toFloat() / trainingsInSeason else 0f

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Media de Asistencia Global", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    // Fila de estadísticas dividida en 3 partes exactas (33/33/33)
                                    Row(modifier = Modifier.weight(1f)) {
                                        Text("✅ ${String.format(java.util.Locale.US, "%.1f", avgPresent)}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                        Text("⚠️ ${String.format(java.util.Locale.US, "%.1f", avgJustified)}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                        Text("❌ ${String.format(java.util.Locale.US, "%.1f", avgUnjustified)}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                    }

                                    // Botón Detalles
                                    Text(
                                        text = if (isSeasonDetailsExpanded) "Ocultar" else "Detalles",
                                        color = com.example.entrenamientos.ui.theme.InfantilBlue,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                        modifier = Modifier.clickable { isSeasonDetailsExpanded = !isSeasonDetailsExpanded }.padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
                                    )
                                }

                                if (isSeasonDetailsExpanded) {
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                    Text("Entrenamientos en la temporada: $trainingsInSeason", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(4.dp))

                                    players.sortedBy { it.name }.forEach { player ->
                                        val pAtts = attendances.filter { it.playerId == player.id }
                                        val pPres = pAtts.count { it.status == 0 }
                                        val pJust = pAtts.count { it.status == 1 }
                                        val pUnjust = pAtts.count { it.status == 2 }

                                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("• ${player.name}", style = MaterialTheme.typography.bodyMedium)
                                            Text("$pPres | $pJust | $pUnjust", style = MaterialTheme.typography.bodyMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
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
                val targetMonth = java.time.YearMonth.from(weekStart.plusDays(3))
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
                            color = if (selectedTeam == 2018) com.example.entrenamientos.ui.theme.PrebenjaminPink else com.example.entrenamientos.ui.theme.InfantilBlue
                        )
                        Icon(
                            imageVector = if (isExpanded) androidx.compose.material.icons.Icons.Default.KeyboardArrowUp else androidx.compose.material.icons.Icons.Default.KeyboardArrowDown,
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
                        val trainingsInWeek = weekAttendances.groupBy { it.date }.size

                        val wPresent = weekAttendances.count { it.status == 0 }
                        val wJustified = weekAttendances.count { it.status == 1 }
                        val wUnjustified = weekAttendances.count { it.status == 2 }

                        val avgWPresent = if (trainingsInWeek > 0) wPresent.toFloat() / trainingsInWeek else 0f
                        val avgWJustified = if (trainingsInWeek > 0) wJustified.toFloat() / trainingsInWeek else 0f
                        val avgWUnjustified = if (trainingsInWeek > 0) wUnjustified.toFloat() / trainingsInWeek else 0f

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.2f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Semana del ${weekStart.dayOfMonth} al ${weekEnd.dayOfMonth} de ${weekEnd.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES")).lowercase()}", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                        // Fila de estadísticas dividida en 3 partes exactas (33/33/33)
                                        Row(modifier = Modifier.weight(1f)) {
                                            Text("✅ ${String.format(java.util.Locale.US, "%.1f", avgWPresent)}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                            Text("⚠️ ${String.format(java.util.Locale.US, "%.1f", avgWJustified)}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                            Text("❌ ${String.format(java.util.Locale.US, "%.1f", avgWUnjustified)}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                        }

                                        // Botón Detalles
                                        val isWeekDetailsExpanded = expandedWeekDetails[weekStart] ?: false
                                        Text(
                                            text = if (isWeekDetailsExpanded) "Ocultar" else "Detalles",
                                            color = com.example.entrenamientos.ui.theme.InfantilBlue,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                            modifier = Modifier.clickable { expandedWeekDetails[weekStart] = !isWeekDetailsExpanded }.padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
                                        )
                                    }

                                    if (expandedWeekDetails[weekStart] == true) {
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                        Text("Entrenamientos esta semana: $trainingsInWeek", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                                        Spacer(modifier = Modifier.height(4.dp))

                                        players.sortedBy { it.name }.forEach { player ->
                                            val pAtts = weekAttendances.filter { it.playerId == player.id }
                                            val pPres = pAtts.count { it.status == 0 }
                                            val pJust = pAtts.count { it.status == 1 }
                                            val pUnjust = pAtts.count { it.status == 2 }

                                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("• ${player.name}", style = MaterialTheme.typography.bodyMedium)
                                                Text("$pPres | $pJust | $pUnjust", style = MaterialTheme.typography.bodyMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- 2. ESTADÍSTICAS DE PARTIDOS Y CONVOCATORIAS (Solo Infantiles) ---
            if (selectedTeam == 2013) {
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    Text("Medias de Temporada", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                val playedMatches = allMatches.filter { it.resultLocal != null && it.resultVisitor != null }

                if (playedMatches.isEmpty()) {
                    item { Text("No hay partidos jugados todavía.", color = Color.Gray) }
                } else {
                    var totalScored = 0
                    var totalReceived = 0
                    var totalFtMade = 0
                    var totalFtAttempted = 0

                    playedMatches.forEach { m ->
                        if (m.isLocal) {
                            totalScored += m.resultLocal ?: 0
                            totalReceived += m.resultVisitor ?: 0
                        } else {
                            totalScored += m.resultVisitor ?: 0
                            totalReceived += m.resultLocal ?: 0
                        }
                        totalFtMade += m.ftMade
                        totalFtAttempted += m.ftAttempted
                    }

                    val avgScored = if (playedMatches.isNotEmpty()) totalScored.toFloat() / playedMatches.size else 0f
                    val avgReceived = if (playedMatches.isNotEmpty()) totalReceived.toFloat() / playedMatches.size else 0f
                    val ftPercentage = if (totalFtAttempted > 0) (totalFtMade.toFloat() / totalFtAttempted) * 100 else 0f

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = com.example.entrenamientos.ui.theme.InfantilBlue.copy(alpha = 0.15f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Puntos Anotados / partido: ${String.format(java.util.Locale.US, "%.1f", avgScored)} pts", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Puntos Recibidos / partido: ${String.format(java.util.Locale.US, "%.1f", avgReceived)} pts", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Tiros Libres (%): ${String.format(java.util.Locale.US, "%.1f", ftPercentage)}% ($totalFtMade/$totalFtAttempted)", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Resumen de Convocatorias", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                val matchesWithConvocatoria = allMatches.filter { it.isConvocatoriaSaved }

                if (matchesWithConvocatoria.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.small)
                                .clickable { isSeasonMatchesExpanded = !isSeasonMatchesExpanded }
                                .padding(vertical = 4.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "TEMPORADA",
                                style = MaterialTheme.typography.titleMedium,
                                color = com.example.entrenamientos.ui.theme.InfantilBlue
                            )
                            Icon(
                                imageVector = if (isSeasonMatchesExpanded) androidx.compose.material.icons.Icons.Default.KeyboardArrowUp else androidx.compose.material.icons.Icons.Default.KeyboardArrowDown,
                                contentDescription = "Expandir/Colapsar",
                                tint = Color.Gray
                            )
                        }
                    }

                    if (isSeasonMatchesExpanded) {
                        val totalMatches = matchesWithConvocatoria.size
                        val totalSummoned = matchesWithConvocatoria.sumOf { it.summonedPlayers.size }
                        val totalUnsummoned = matchesWithConvocatoria.sumOf { it.unsummonedReasons.size }

                        val avgSummoned = totalSummoned.toFloat() / totalMatches
                        val avgUnsummoned = totalUnsummoned.toFloat() / totalMatches

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = com.example.entrenamientos.ui.theme.InfantilBlue.copy(alpha = 0.15f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Media Convocadas / partido: ${String.format(java.util.Locale.US, "%.1f", avgSummoned)}", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = com.example.entrenamientos.ui.theme.SuccessGreen)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Media Desconvocadas / partido: ${String.format(java.util.Locale.US, "%.1f", avgUnsummoned)}", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = com.example.entrenamientos.ui.theme.AttendanceRed)

                                    // --- HISTORIAL DE DESCONVOCATORIAS ---
                                    Spacer(modifier = Modifier.height(16.dp))
                                    HorizontalDivider(color = Color.Gray, thickness = 1.dp, modifier = Modifier.padding(bottom = 8.dp))
                                    Text("Historial de Desconvocatorias", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    val unsummonedStats = remember(matchesWithConvocatoria) {
                                        val stats = mutableMapOf<Long, MutableMap<String, Int>>()
                                        matchesWithConvocatoria.forEach { match ->
                                            match.unsummonedReasons.forEach { (playerId, reason) ->
                                                val playerStats = stats.getOrPut(playerId) { mutableMapOf() }
                                                playerStats[reason] = playerStats.getOrDefault(reason, 0) + 1
                                            }
                                        }
                                        stats
                                    }

                                    if (unsummonedStats.isEmpty()) {
                                        Text("No hay desconvocatorias registradas.", style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
                                    } else {
                                        players.sortedBy { it.name }.forEach { player ->
                                            val playerStats = unsummonedStats[player.id]
                                            if (playerStats != null && playerStats.isNotEmpty()) {
                                                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                                                    val totalPlayerUnsummoned = playerStats.values.sum()
                                                    Text(
                                                        text = "• ${player.name} ($totalPlayerUnsummoned ausencias totales)",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                                        color = com.example.entrenamientos.ui.theme.AttendanceRed
                                                    )
                                                    playerStats.forEach { (reason, count) ->
                                                        Text(
                                                            text = "  - $reason: $count",
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            color = Color.DarkGray
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                if (allMatches.isEmpty()) {
                    item { Text("No hay partidos programados.", color = Color.Gray) }
                } else {
                    val matchesByMonth = allMatches.groupBy {
                        java.time.YearMonth.from(java.time.LocalDate.parse(it.date))
                    }
                    val sortedMatchMonths = matchesByMonth.toSortedMap(compareByDescending { it })

                    sortedMatchMonths.forEach { (month, matchesInMonth) ->
                        val isExpanded = expandedMatchMonths[month] ?: false

                        item {
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
                                    color = com.example.entrenamientos.ui.theme.InfantilBlue
                                )
                                Icon(
                                    imageVector = if (isExpanded) androidx.compose.material.icons.Icons.Default.KeyboardArrowUp else androidx.compose.material.icons.Icons.Default.KeyboardArrowDown,
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
                                val formattedDate = "$matchDayOfWeek ${matchDateObj.dayOfMonth} de $matchMonthName"

                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.2f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("vs ${match.opponent}", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                            Text(formattedDate, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                        }

                                        if (match.isConvocatoriaSaved) {
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = "${match.summonedPlayers.size} Convocadas",
                                                    color = com.example.entrenamientos.ui.theme.SuccessGreen,
                                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                Text(
                                                    text = "${match.unsummonedReasons.size} Desconvocadas",
                                                    color = com.example.entrenamientos.ui.theme.AttendanceRed,
                                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                        } else {
                                            Text(
                                                text = "Pendiente",
                                                color = Color.Gray,
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
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

@Composable
fun SettingsScreen(viewModel: BasketViewModel = hiltViewModel()) {
    var activeTab by remember { mutableStateOf("JUGADORAS") } // "JUGADORAS", "HORARIOS", "PARTIDOS" o "FESTIVOS"
    var selectedTeam by remember { mutableStateOf(2013) }

    val players by viewModel.getPlayersForTeam(selectedTeam).collectAsState(initial = emptyList())
    val allSchedules by viewModel.schedules.collectAsState()
    val allMatches by viewModel.matches.collectAsState()
    val teamSchedules = allSchedules.filter { it.teamYear == selectedTeam }.sortedBy { it.dayOfWeek }

    // Estados Jugadoras
    var showPlayerDialog by remember { mutableStateOf(false) }
    var playerToEdit by remember { mutableStateOf<com.example.entrenamientos.data.Player?>(null) }
    var playerNameInput by remember { mutableStateOf("") }

    // Estados Horarios
    var showScheduleDialog by remember { mutableStateOf(false) }
    var scheduleToEdit by remember { mutableStateOf<com.example.entrenamientos.data.TrainingSchedule?>(null) }
    var scheduleDayInput by remember { mutableStateOf(1) }
    var scheduleStartInput by remember { mutableStateOf("17:00") }
    var scheduleEndInput by remember { mutableStateOf("18:30") }
    var scheduleError by remember { mutableStateOf("") }

    // Estados Partidos (WIZARD)
    var showMatchDialog by remember { mutableStateOf(false) }
    var matchStep by remember { mutableStateOf(1) }
    var matchToEdit by remember { mutableStateOf<com.example.entrenamientos.data.Match?>(null) }
    var matchDateInput by remember { mutableStateOf("05-09-2026") }
    var matchTimeInput by remember { mutableStateOf("10:00") }
    var matchIsLocalInput by remember { mutableStateOf(true) }
    var matchLocationInput by remember { mutableStateOf("") }
    var matchOpponentInput by remember { mutableStateOf("") }

    val daysOfWeek = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Ajustes", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // Selector de Pestañas
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { activeTab = "JUGADORAS" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = if (activeTab == "JUGADORAS") Color.DarkGray else Color.LightGray)
                ) { Text("Jugadoras", color = if (activeTab == "JUGADORAS") Color.White else Color.Black) }

                Button(
                    onClick = { activeTab = "HORARIOS" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = if (activeTab == "HORARIOS") Color.DarkGray else Color.LightGray)
                ) { Text("Horarios", color = if (activeTab == "HORARIOS") Color.White else Color.Black) }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { activeTab = "PARTIDOS" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = if (activeTab == "PARTIDOS") com.example.entrenamientos.ui.theme.InfantilBlue else Color.LightGray)
                ) { Text("Partidos", color = if (activeTab == "PARTIDOS") Color.White else Color.Black) }

                Button(
                    onClick = { activeTab = "FESTIVOS" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = if (activeTab == "FESTIVOS") Color.Red else Color.LightGray)
                ) { Text("Festivos", color = if (activeTab == "FESTIVOS") Color.White else Color.Black) }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cabeceras según la pestaña
        if (activeTab == "JUGADORAS" || activeTab == "HORARIOS") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(
                    onClick = { selectedTeam = 2018 },
                    colors = ButtonDefaults.buttonColors(containerColor = if (selectedTeam == 2018) com.example.entrenamientos.ui.theme.PrebenjaminPink else Color.Gray)
                ) { Text("Prebenjamines") }

                Button(
                    onClick = { selectedTeam = 2013 },
                    colors = ButtonDefaults.buttonColors(containerColor = if (selectedTeam == 2013) com.example.entrenamientos.ui.theme.InfantilBlue else Color.Gray)
                ) { Text("Infantiles") }
            }
            Spacer(modifier = Modifier.height(16.dp))
        } else if (activeTab == "PARTIDOS") {
            Text("Gestión de Partidos (Infantiles 2013)", style = MaterialTheme.typography.labelMedium, color = com.example.entrenamientos.ui.theme.InfantilBlue, modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 16.dp))
        }

        // CONTENIDO
        if (activeTab == "JUGADORAS") {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(players) { player ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(MaterialTheme.shapes.small).background(Color.LightGray.copy(alpha = 0.2f)).padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = player.name, style = MaterialTheme.typography.bodyLarge)
                        Row {
                            IconButton(onClick = { playerToEdit = player; playerNameInput = player.name; showPlayerDialog = true }) { Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.DarkGray) }
                            IconButton(onClick = { viewModel.deletePlayer(player) }) { Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = com.example.entrenamientos.ui.theme.AttendanceRed) }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { playerToEdit = null; playerNameInput = ""; showPlayerDialog = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceGreen)) { Text("Añadir Nueva Jugadora", color = Color.Black) }
        } else if (activeTab == "HORARIOS") {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(teamSchedules) { schedule ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(MaterialTheme.shapes.small).background(Color.LightGray.copy(alpha = 0.2f)).padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(text = daysOfWeek[schedule.dayOfWeek - 1], style = MaterialTheme.typography.bodyLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            Text(text = "${schedule.startTime} - ${schedule.endTime}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        }
                        Row {
                            IconButton(onClick = { scheduleToEdit = schedule; scheduleDayInput = schedule.dayOfWeek; scheduleStartInput = schedule.startTime; scheduleEndInput = schedule.endTime; scheduleError = ""; showScheduleDialog = true }) { Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.DarkGray) }
                            IconButton(onClick = { viewModel.deleteSchedule(schedule) }) { Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = com.example.entrenamientos.ui.theme.AttendanceRed) }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { scheduleToEdit = null; scheduleDayInput = 1; scheduleStartInput = "17:00"; scheduleEndInput = "18:00"; scheduleError = ""; showScheduleDialog = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceGreen)) { Text("Añadir Horario", color = Color.Black) }
        } else if (activeTab == "PARTIDOS") {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(allMatches) { match ->
                    val dateObj = java.time.LocalDate.parse(match.date)
                    val dayOfWeekName = dateObj.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES")).replaceFirstChar { it.uppercase() }
                    val monthName = dateObj.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES")).replaceFirstChar { it.uppercase() }

                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(MaterialTheme.shapes.small).background(Color.LightGray.copy(alpha = 0.2f)).padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "$dayOfWeekName ${dateObj.dayOfMonth} de $monthName - ${match.time}", style = MaterialTheme.typography.bodyLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            Text(text = "Polideportivo ${match.location}", style = MaterialTheme.typography.bodyMedium)
                            Text(text = if (match.isLocal) "CD Huerto - ${match.opponent}" else "${match.opponent} - CD Huerto", style = MaterialTheme.typography.bodyMedium)
                        }
                        Row {
                            IconButton(onClick = { matchToEdit = match; matchStep = 1; val parts = match.date.split("-"); matchDateInput = "${parts[2]}-${parts[1]}-${parts[0]}"; matchTimeInput = match.time; matchIsLocalInput = match.isLocal; matchLocationInput = match.location; matchOpponentInput = match.opponent; showMatchDialog = true }) { Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.DarkGray) }
                            IconButton(onClick = { viewModel.deleteMatch(match) }) { Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = com.example.entrenamientos.ui.theme.AttendanceRed) }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { matchToEdit = null; matchStep = 1; matchDateInput = "05-09-2026"; matchTimeInput = "10:00"; matchIsLocalInput = true; matchLocationInput = ""; matchOpponentInput = ""; showMatchDialog = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceGreen)) { Text("Añadir Partido", color = Color.Black) }
        } else if (activeTab == "FESTIVOS") {
            FestivosSettingsTab(viewModel)
        }
    }

    // DIÁLOGOS (Jugadoras / Horarios)
    if (showPlayerDialog) {
        AlertDialog(
            onDismissRequest = { showPlayerDialog = false },
            title = { Text(if (playerToEdit == null) "Añadir Jugadora" else "Editar Jugadora") },
            text = { OutlinedTextField(value = playerNameInput, onValueChange = { playerNameInput = it }, label = { Text("Nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth()) },
            confirmButton = { Button(onClick = { if (playerNameInput.isNotBlank()) { if (playerToEdit == null) viewModel.addPlayer(playerNameInput, selectedTeam) else viewModel.updatePlayer(playerToEdit!!.copy(name = playerNameInput)); showPlayerDialog = false } }, colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceGreen)) { Text("Guardar", color = Color.Black) } },
            dismissButton = { TextButton(onClick = { showPlayerDialog = false }) { Text("Cancelar", color = Color.Gray) } }
        )
    }

    if (showScheduleDialog) {
        AlertDialog(
            onDismissRequest = { showScheduleDialog = false },
            title = { Text(if (scheduleToEdit == null) "Nuevo Horario" else "Editar Horario") },
            text = {
                Column {
                    if (scheduleError.isNotEmpty()) { Text(text = scheduleError, color = com.example.entrenamientos.ui.theme.AttendanceRed, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 8.dp)) }
                    Text("Día de la semana:", style = MaterialTheme.typography.labelSmall)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Button(onClick = { if (scheduleDayInput > 1) scheduleDayInput-- else scheduleDayInput = 5 }) { Text("-") }
                        Text("${daysOfWeek[scheduleDayInput - 1]}", modifier = Modifier.align(Alignment.CenterVertically))
                        Button(onClick = { if (scheduleDayInput < 5) scheduleDayInput++ else scheduleDayInput = 1 }) { Text("+") }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    androidx.compose.material3.OutlinedButton(onClick = { val parts = scheduleStartInput.split(":"); val h = parts.getOrNull(0)?.toIntOrNull() ?: 17; val m = parts.getOrNull(1)?.toIntOrNull() ?: 0; android.app.TimePickerDialog(context, { _, hour, minute -> scheduleStartInput = String.format(java.util.Locale.getDefault(), "%02d:%02d", hour, minute) }, h, m, true).show() }, modifier = Modifier.fillMaxWidth()) { Text("Hora Inicio: $scheduleStartInput", color = Color.Black) }
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.material3.OutlinedButton(onClick = { val parts = scheduleEndInput.split(":"); val h = parts.getOrNull(0)?.toIntOrNull() ?: 18; val m = parts.getOrNull(1)?.toIntOrNull() ?: 30; android.app.TimePickerDialog(context, { _, hour, minute -> scheduleEndInput = String.format(java.util.Locale.getDefault(), "%02d:%02d", hour, minute) }, h, m, true).show() }, modifier = Modifier.fillMaxWidth()) { Text("Hora Fin: $scheduleEndInput", color = Color.Black) }
                }
            },
            confirmButton = { Button(onClick = { val newSchedule = com.example.entrenamientos.data.TrainingSchedule(id = scheduleToEdit?.id ?: 0, teamYear = selectedTeam, dayOfWeek = scheduleDayInput, startTime = scheduleStartInput, endTime = scheduleEndInput); viewModel.addOrUpdateSchedule(newSchedule, onSuccess = { showScheduleDialog = false }, onError = { errorMsg -> scheduleError = errorMsg }) }, colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceGreen)) { Text("Guardar", color = Color.Black) } },
            dismissButton = { TextButton(onClick = { showScheduleDialog = false }) { Text("Cancelar", color = Color.Gray) } }
        )
    }

    // WIZARD DE PARTIDOS PERSONALIZADO
    if (showMatchDialog) {
        val titleStep = when(matchStep) {
            1 -> "Seleccionar Día"
            2 -> "Seleccionar Hora"
            3 -> "¿Local o Visitante?"
            4 -> "Polideportivo y Equipo Rival"
            else -> ""
        }

        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showMatchDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false) // OCUPA CASI TODO EL ANCHO
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.98f) // MÁXIMO ANCHO
                    .padding(8.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(titleStep, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Contenido del paso
                    when (matchStep) {
                        1 -> {
                            androidx.compose.material3.OutlinedButton(
                                onClick = {
                                    val parts = matchDateInput.split("-")
                                    val d = parts.getOrNull(0)?.toIntOrNull() ?: 5
                                    val m = (parts.getOrNull(1)?.toIntOrNull() ?: 9) - 1
                                    val y = parts.getOrNull(2)?.toIntOrNull() ?: 2026
                                    android.app.DatePickerDialog(context, { _, year, month, day -> matchDateInput = String.format(java.util.Locale.getDefault(), "%02d-%02d-%04d", day, month + 1, year) }, y, m, d).show()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text(matchDateInput, color = Color.Black) }
                        }
                        2 -> {
                            androidx.compose.material3.OutlinedButton(
                                onClick = {
                                    val parts = matchTimeInput.split(":")
                                    val h = parts.getOrNull(0)?.toIntOrNull() ?: 10
                                    val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
                                    android.app.TimePickerDialog(context, { _, hour, minute -> matchTimeInput = String.format(java.util.Locale.getDefault(), "%02d:%02d", hour, minute) }, h, m, true).show()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text(matchTimeInput, color = Color.Black) }
                        }
                        3 -> {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { matchIsLocalInput = true }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = if (matchIsLocalInput) com.example.entrenamientos.ui.theme.InfantilBlue else Color.Gray)) { Text("Local") }
                                Button(onClick = { matchIsLocalInput = false }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = if (!matchIsLocalInput) com.example.entrenamientos.ui.theme.InfantilBlue else Color.Gray)) { Text("Visitante") }
                            }
                        }
                        4 -> {
                            OutlinedTextField(value = matchLocationInput, onValueChange = { matchLocationInput = it }, label = { Text("Polideportivo") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(value = matchOpponentInput, onValueChange = { matchOpponentInput = it }, label = { Text("Equipo Rival") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // FILA DE BOTONES
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                        if (matchStep > 1) {
                            Button(onClick = { matchStep-- }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) { Text("Atrás", color = Color.White) }
                        }

                        Button(onClick = { showMatchDialog = false }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceRed)) { Text("Cancelar", color = Color.White) }

                        if (matchStep < 4) {
                            Button(onClick = { matchStep++ }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.InfantilBlue)) { Text("Siguiente", color = Color.White) }
                        } else {
                            Button(
                                onClick = {
                                    val parts = matchDateInput.split("-")
                                    val dateForDb = "${parts[2]}-${parts[1]}-${parts[0]}"
                                    val newMatch = com.example.entrenamientos.data.Match(id = matchToEdit?.id ?: 0, date = dateForDb, time = matchTimeInput, isLocal = matchIsLocalInput, location = matchLocationInput, opponent = matchOpponentInput)
                                    viewModel.addOrUpdateMatch(newMatch); showMatchDialog = false
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceGreen)
                            ) { Text("Guardar", color = Color.Black) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FestivosSettingsTab(viewModel: BasketViewModel) {
    val holidays by viewModel.holidays.collectAsState()
    var newHolidayDate by remember { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Añadir Día Festivo", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Sustituimos el campo de texto por un botón que abre el calendario
            androidx.compose.material3.OutlinedButton(
                onClick = {
                    val cal = java.util.Calendar.getInstance()
                    // Si ya había una fecha seleccionada, abrimos el calendario en esa fecha
                    if (newHolidayDate.isNotEmpty()) {
                        try {
                            val parts = newHolidayDate.split("-")
                            cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
                        } catch (e: Exception) {}
                    }

                    val y = cal.get(java.util.Calendar.YEAR)
                    val m = cal.get(java.util.Calendar.MONTH)
                    val d = cal.get(java.util.Calendar.DAY_OF_MONTH)

                    android.app.DatePickerDialog(context, { _, year, month, day ->
                        // Guardamos la fecha en formato YYYY-MM-DD necesario para la BD
                        newHolidayDate = String.format(java.util.Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day)
                    }, y, m, d).show()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (newHolidayDate.isEmpty()) "Seleccionar fecha" else newHolidayDate,
                    color = if (newHolidayDate.isEmpty()) Color.Gray else Color.Black
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (newHolidayDate.isNotEmpty()) {
                        viewModel.addHoliday(newHolidayDate)
                        newHolidayDate = "" // Reseteamos el valor tras guardar
                    } else {
                        android.widget.Toast.makeText(context, "Por favor, selecciona una fecha primero", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = com.example.entrenamientos.ui.theme.InfantilBlue,
                    disabledContainerColor = Color.LightGray
                ),
                enabled = newHolidayDate.isNotEmpty() // El botón solo se activa si hay fecha seleccionada
            ) {
                Text("Añadir", color = if (newHolidayDate.isNotEmpty()) Color.White else Color.DarkGray)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Festivos Registrados", style = MaterialTheme.typography.titleMedium)
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

                    Text(formatted, style = MaterialTheme.typography.bodyLarge, color = Color.Red, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)

                    IconButton(onClick = { viewModel.removeHoliday(holiday) }) {
                        Icon(androidx.compose.material.icons.Icons.Default.Delete, contentDescription = "Eliminar", tint = com.example.entrenamientos.ui.theme.AttendanceRed)
                    }
                }
            }
        }
    }
}