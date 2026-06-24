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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.example.entrenamientos.ui.BasketViewModel

val PrebenjaminPink = Color(0xFFFF80AB)
val InfantilBlue = Color(0xFF2196F3)

@Composable
fun SplashScreen(navController: androidx.navigation.NavController) {
    // Efecto para esperar 2 segundos y luego saltar al calendario
    androidx.compose.runtime.LaunchedEffect(key1 = true) {
        kotlinx.coroutines.delay(2000) // 2000 milisegundos = 2 segundos
        navController.navigate("calendar") {
            // Esto evita que al darle a "Atrás" en el móvil, vuelva a la pantalla de carga
            popUpTo("splash") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White), // Si el fondo de tu logo no es blanco, cambia este color
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = com.example.entrenamientos.R.drawable.logo),
            contentDescription = "Logo de la App",
            modifier = Modifier
                .size(250.dp) // Tamaño del logo en la pantalla de carga
                .clip(androidx.compose.foundation.shape.CircleShape), // Esto fuerza a que la imagen sea un círculo perfecto
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(viewModel: BasketViewModel = hiltViewModel(), navController: androidx.navigation.NavController) {
    var currentMonth by remember { mutableStateOf(java.time.YearMonth.of(2026, 9)) }
    val minMonth = java.time.YearMonth.of(2026, 9)
    val maxMonth = java.time.YearMonth.of(2027, 5)

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }
    val selectedDateStr by viewModel.selectedDate.collectAsState()

    // 1. Observamos los cambios en los horarios
    val schedules by viewModel.schedules.collectAsState()
    val selectedDate = java.time.LocalDate.parse(selectedDateStr)

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // ... (Tu código de botones de mes, se mantiene igual)
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
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("L", "M", "X", "J", "V", "S", "D").forEach { day ->
                Text(day, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

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
                                    schedules = schedules, // Pasamos los horarios observados
                                    onClick = {
                                        viewModel.setSelectedDate(date.toString())
                                        if (viewModel.getTeamsForDate(date).isNotEmpty() || viewModel.hasMatchOnDate(date)) {
                                            showBottomSheet = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(onDismissRequest = { showBottomSheet = false }, sheetState = sheetState) {
            DayOptionsMenu(date = selectedDate, viewModel = viewModel, navController = navController, onClose = { showBottomSheet = false })
        }
    }
}

@Composable
fun DayCell(date: java.time.LocalDate, viewModel: BasketViewModel, schedules: List<com.example.entrenamientos.data.TrainingSchedule>, onClick: () -> Unit) {
    val isHoliday = viewModel.isHoliday(date)
    val dayValue = date.dayOfWeek.value
    // Si es festivo, las franjas desaparecen dinámicamente porque teams estará vacío
    val teams = if (isHoliday) emptyList() else schedules.filter { it.dayOfWeek == dayValue }.map { it.teamYear }.distinct()

    val match = viewModel.getMatchForDate(date)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(2.dp)
            .background(if (isHoliday) Color(0xFFFFEBEE) else Color.White, shape = MaterialTheme.shapes.small) // Fondo rojizo si es festivo
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        // Número del día en Rojo si es Festivo
        Text(
            text = date.dayOfMonth.toString(),
            modifier = Modifier.align(Alignment.TopStart),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isHoliday) Color.Red else Color.Unspecified,
            fontWeight = if (isHoliday) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
        )

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Círculo del partido
            if (match != null) {
                val circleColor = viewModel.getMatchColor(match)
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(circleColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = if (match.isLocal) "L" else "V", color = Color.White, fontSize = 12.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Franjas de entrenamiento (Automáticamente ocultas si isHoliday == true)
            if (teams.contains(2018)) {
                Box(modifier = Modifier.fillMaxWidth(0.95f).height(26.dp).background(com.example.entrenamientos.ui.theme.PrebenjaminPink, shape = MaterialTheme.shapes.extraSmall), contentAlignment = Alignment.Center) {
                    Text("2018", color = Color.White, fontSize = 12.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
            }
            if (teams.contains(2013)) {
                if (teams.contains(2018)) Spacer(modifier = Modifier.height(2.dp))
                Box(modifier = Modifier.fillMaxWidth(0.95f).height(26.dp).background(com.example.entrenamientos.ui.theme.InfantilBlue, shape = MaterialTheme.shapes.extraSmall), contentAlignment = Alignment.Center) {
                    Text("2013", color = Color.White, fontSize = 12.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DayOptionsMenu(
    date: java.time.LocalDate,
    viewModel: BasketViewModel,
    navController: androidx.navigation.NavController,
    onClose: () -> Unit
) {
    val teams = viewModel.getTeamsForDate(date)
    val match = viewModel.getMatchForDate(date)
    val hasMatch = match != null

    val dayOfWeek = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES")).replaceFirstChar { it.uppercase() }
    val monthName = date.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES"))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Opciones para el $dayOfWeek ${date.dayOfMonth} de $monthName",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // --- ENTRENAMIENTOS ---
        if (teams.isNotEmpty()) {
            teams.forEach { teamYear ->
                val teamName = if (teamYear == 2018) "Prebenjamines (2018)" else "Infantiles (2013)"
                val teamColor = if (teamYear == 2018) com.example.entrenamientos.ui.theme.PrebenjaminPink else com.example.entrenamientos.ui.theme.InfantilBlue

                Text(teamName, style = MaterialTheme.typography.bodyMedium, color = teamColor, modifier = Modifier.padding(top = 8.dp))

                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(
                        onClick = { viewModel.setSelectedTeamYear(teamYear); onClose(); navController.navigate("notes/ENTRENAMIENTO") },
                        colors = ButtonDefaults.buttonColors(containerColor = teamColor)
                    ) { Text("Entrenamiento", color = Color.White) }

                    Button(
                        onClick = { viewModel.setSelectedTeamYear(teamYear); onClose(); navController.navigate("attendance") },
                        colors = ButtonDefaults.buttonColors(containerColor = teamColor)
                    ) { Text("Asistencia", color = Color.White) }

                    Button(
                        onClick = { viewModel.setSelectedTeamYear(teamYear); onClose(); navController.navigate("notes/OTROS") },
                        colors = ButtonDefaults.buttonColors(containerColor = teamColor)
                    ) { Text("Notas", color = Color.White) }
                }
            }
        }

        // --- PARTIDOS ---
        if (hasMatch && match != null) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            Text("Partido (Infantiles)", style = MaterialTheme.typography.labelLarge, color = com.example.entrenamientos.ui.theme.InfantilBlue)
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(
                    onClick = {
                        viewModel.setSelectedDate(date.toString())
                        onClose()
                        navController.navigate("convocatoria")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.InfantilBlue)
                ) {
                    Text(if (match.isConvocatoriaSaved) "Ver Convocatoria" else "Convocatoria")
                }

                Button(
                    onClick = {
                        viewModel.setSelectedDate(date.toString())
                        onClose()
                        navController.navigate("resultado")
                    },
                    enabled = match.isConvocatoriaSaved, // <-- LÓGICA DE BLOQUEO AQUÍ
                    colors = ButtonDefaults.buttonColors(
                        containerColor = com.example.entrenamientos.ui.theme.InfantilBlue,
                        disabledContainerColor = Color.LightGray, // Color de fondo cuando está bloqueado
                        disabledContentColor = Color.DarkGray // Color del texto cuando está bloqueado
                    )
                ) {
                    Text(if (match.resultLocal != null) "Ver Resultado" else "Resultado")
                }
            }
        }
    }
}

@Composable
fun AttendanceScreen(viewModel: BasketViewModel = hiltViewModel(), navController: androidx.navigation.NavController) {
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
fun ConvocatoriaScreen(viewModel: BasketViewModel, navController: androidx.navigation.NavController) {
    val selectedDateStr by viewModel.selectedDate.collectAsState()
    val date = java.time.LocalDate.parse(selectedDateStr)
    val match = viewModel.getMatchForDate(date) ?: return

    val players by viewModel.getPlayersForTeam(2013).collectAsState(initial = emptyList())
    val context = androidx.compose.ui.platform.LocalContext.current

    var isEditMode by remember { mutableStateOf(!match.isConvocatoriaSaved) }
    var summonedIds by remember { mutableStateOf(match.summonedPlayers.toSet()) }
    var reasonsMap by remember { mutableStateOf(match.unsummonedReasons.toMutableMap()) }

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
            // AQUI ESTÁN LOS 3 BOTONES AHORA
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
            Text("Seleccionadas: ${summonedIds.size}/12", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(players) { player ->
                    val isSummoned = summonedIds.contains(player.id)
                    val containerColor = if (isSummoned) com.example.entrenamientos.ui.theme.SuccessGreen else com.example.entrenamientos.ui.theme.AttendanceRed

                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.small)
                                .background(containerColor.copy(alpha = 0.2f))
                                .clickable {
                                    if (isSummoned) {
                                        summonedIds = summonedIds - player.id
                                        if (!reasonsMap.containsKey(player.id)) reasonsMap[player.id] = ""
                                    } else {
                                        if (summonedIds.size >= 12) {
                                            android.widget.Toast.makeText(context, "No puedes convocar a más de 12 jugadoras", android.widget.Toast.LENGTH_SHORT).show()
                                        } else {
                                            summonedIds = summonedIds + player.id
                                            reasonsMap.remove(player.id)
                                        }
                                    }
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(player.name, style = MaterialTheme.typography.bodyLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = containerColor)
                            Icon(imageVector = if (isSummoned) androidx.compose.material.icons.Icons.Default.Check else androidx.compose.material.icons.Icons.Default.Close, contentDescription = null, tint = containerColor)
                        }

                        if (!isSummoned) {
                            OutlinedTextField(
                                value = reasonsMap[player.id] ?: "",
                                onValueChange = { newText ->
                                    reasonsMap = reasonsMap.toMutableMap().apply { put(player.id, newText) }
                                },
                                label = { Text("Motivo de la ausencia") },
                                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 4.dp, end = 16.dp),
                                singleLine = true
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val unsummonedPlayers = players.filter { !summonedIds.contains(it.id) }
                    val hasMissingReasons = unsummonedPlayers.any { p -> reasonsMap[p.id].isNullOrBlank() }

                    if (hasMissingReasons) {
                        android.widget.Toast.makeText(
                            context,
                            "Debes añadir un motivo para TODAS las jugadoras desconvocadas",
                            android.widget.Toast.LENGTH_SHORT
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
}

@Composable
fun ResultadoScreen(viewModel: BasketViewModel, navController: androidx.navigation.NavController) {
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
fun TrainingNoteScreen(viewModel: BasketViewModel = hiltViewModel(), navController: androidx.navigation.NavController, noteType: String) {
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

    // Estados para controlar qué meses/secciones están expandidos (ahora inician en false)
    val expandedAttendanceMonths = remember { androidx.compose.runtime.mutableStateMapOf<java.time.YearMonth, Boolean>() }
    val expandedMatchMonths = remember { androidx.compose.runtime.mutableStateMapOf<java.time.YearMonth, Boolean>() }
    var isSeasonAttendanceExpanded by remember { mutableStateOf(false) }
    var isSeasonMatchesExpanded by remember { mutableStateOf(false) }

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

            // --- 1. ASISTENCIAS A ENTRENAMIENTOS (Ambas categorías) ---
            item {
                Text("Asistencia Mensual / Semanal", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Desplegable de TEMPORADA (Asistencia)
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
                    val totalAtts = attendances.size
                    val totalPresent = attendances.count { it.status == 0 }
                    val seasonPercentage = if (totalAtts > 0) (totalPresent * 100) / totalAtts else 0

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Media de Asistencia Global", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Asistencia: $seasonPercentage%",
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                    color = if (seasonPercentage >= 80) com.example.entrenamientos.ui.theme.SuccessGreen else com.example.entrenamientos.ui.theme.AttendanceRed
                                )
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
                val weekEnd = weekStart.plusDays(4)
                val monthStart = java.time.YearMonth.from(weekStart)
                val monthEnd = java.time.YearMonth.from(weekEnd)

                monthToWeeksMap.getOrPut(monthStart) { mutableListOf() }.add(weekStart to weekAtts)
                if (monthStart != monthEnd) {
                    monthToWeeksMap.getOrPut(monthEnd) { mutableListOf() }.add(weekStart to weekAtts)
                }
            }

            val sortedMonths = monthToWeeksMap.toSortedMap(compareByDescending { it })
            val formatterMonth = java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy", java.util.Locale("es", "ES"))

            if (sortedMonths.isEmpty()) {
                item { Text("No hay datos de asistencia todavía.", color = Color.Gray) }
            }

            sortedMonths.forEach { (month, weeks) ->
                val isExpanded = expandedAttendanceMonths[month] ?: false // Ahora el valor por defecto es false

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
                        val total = weekAttendances.size
                        val asistieron = weekAttendances.count { it.status == 0 }
                        val justificadas = weekAttendances.count { it.status == 1 }
                        val noJustificadas = weekAttendances.count { it.status == 2 }

                        val porcentaje = if (total > 0) (asistieron * 100) / total else 0

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.2f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Semana del ${weekStart.dayOfMonth} al ${weekEnd.dayOfMonth} de ${weekEnd.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES")).lowercase()}", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Asistencia: $porcentaje%", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = if (porcentaje >= 80) com.example.entrenamientos.ui.theme.SuccessGreen else com.example.entrenamientos.ui.theme.AttendanceRed)
                                        Text("✅ $asistieron  |  ⚠️ $justificadas  |  ❌ $noJustificadas", style = MaterialTheme.typography.bodyMedium)
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

                // Desplegable de TEMPORADA (Convocatorias)
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
                        val isExpanded = expandedMatchMonths[month] ?: false // Ahora el valor por defecto es false

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
fun SettingsScreen(viewModel: BasketViewModel = androidx.hilt.navigation.compose.hiltViewModel()) {
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

        // Selector de Pestañas organizado en 2 líneas (Grid 2x2)
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

        // Cabeceras según la pestaña seleccionada
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

        // CONTENIDO DE LAS PESTAÑAS
        if (activeTab == "JUGADORAS") {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(players) { player ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(MaterialTheme.shapes.small).background(Color.LightGray.copy(alpha = 0.2f)).padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = player.name, style = MaterialTheme.typography.bodyLarge)
                        Row {
                            IconButton(onClick = { playerToEdit = player; playerNameInput = player.name; showPlayerDialog = true }) { Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.DarkGray) }
                            IconButton(onClick = { viewModel.deletePlayer(player) }) { Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = com.example.entrenamientos.ui.theme.AttendanceRed) }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { playerToEdit = null; playerNameInput = ""; showPlayerDialog = true },
                modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceGreen)
            ) { Text("Añadir Nueva Jugadora", color = Color.Black) }

        } else if (activeTab == "HORARIOS") {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(teamSchedules) { schedule ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(MaterialTheme.shapes.small).background(Color.LightGray.copy(alpha = 0.2f)).padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = daysOfWeek[schedule.dayOfWeek - 1], style = MaterialTheme.typography.bodyLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            Text(text = "${schedule.startTime} - ${schedule.endTime}", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        }
                        Row {
                            IconButton(onClick = {
                                scheduleToEdit = schedule; scheduleDayInput = schedule.dayOfWeek
                                scheduleStartInput = schedule.startTime; scheduleEndInput = schedule.endTime
                                scheduleError = ""; showScheduleDialog = true
                            }) { Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.DarkGray) }
                            IconButton(onClick = { viewModel.deleteSchedule(schedule) }) { Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = com.example.entrenamientos.ui.theme.AttendanceRed) }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    scheduleToEdit = null; scheduleDayInput = 1; scheduleStartInput = "17:00"; scheduleEndInput = "18:00"
                    scheduleError = ""; showScheduleDialog = true
                },
                modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceGreen)
            ) { Text("Añadir Horario", color = Color.Black) }

        } else if (activeTab == "PARTIDOS") {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(allMatches) { match ->
                    val dateObj = java.time.LocalDate.parse(match.date)
                    val dayOfWeekName = dateObj.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES")).replaceFirstChar { it.uppercase() }
                    val monthName = dateObj.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES")).replaceFirstChar { it.uppercase() }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(Color.LightGray.copy(alpha = 0.2f))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "$dayOfWeekName ${dateObj.dayOfMonth} de $monthName - ${match.time}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                            Text(
                                text = "Polideportivo ${match.location}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = if (match.isLocal) "Huerto - ${match.opponent}" else "${match.opponent} - Huerto",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Row {
                            IconButton(onClick = {
                                matchToEdit = match; matchStep = 1
                                val parts = match.date.split("-")
                                matchDateInput = "${parts[2]}-${parts[1]}-${parts[0]}"
                                matchTimeInput = match.time
                                matchIsLocalInput = match.isLocal; matchLocationInput = match.location; matchOpponentInput = match.opponent
                                showMatchDialog = true
                            }) { Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.DarkGray) }

                            IconButton(onClick = { viewModel.deleteMatch(match) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = com.example.entrenamientos.ui.theme.AttendanceRed)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    matchToEdit = null; matchStep = 1; matchDateInput = "05-09-2026"; matchTimeInput = "10:00"
                    matchIsLocalInput = true; matchLocationInput = ""; matchOpponentInput = ""
                    showMatchDialog = true
                },
                modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceGreen)
            ) { Text("Añadir Partido", color = Color.Black) }
        } else if (activeTab == "FESTIVOS") {
            FestivosSettingsTab(viewModel)
        }
    }

    // Diálogos de Jugadoras y Horarios
    if (showPlayerDialog) {
        AlertDialog(
            onDismissRequest = { showPlayerDialog = false },
            title = { Text(if (playerToEdit == null) "Añadir Jugadora" else "Editar Jugadora") },
            text = { OutlinedTextField(value = playerNameInput, onValueChange = { playerNameInput = it }, label = { Text("Nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth()) },
            confirmButton = {
                Button(onClick = {
                    if (playerNameInput.isNotBlank()) {
                        if (playerToEdit == null) viewModel.addPlayer(playerNameInput, selectedTeam)
                        else viewModel.updatePlayer(playerToEdit!!.copy(name = playerNameInput))
                        showPlayerDialog = false
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceGreen)) { Text("Guardar", color = Color.Black) }
            },
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
                    androidx.compose.material3.OutlinedButton(onClick = {
                        val parts = scheduleStartInput.split(":"); val h = parts.getOrNull(0)?.toIntOrNull() ?: 17; val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
                        android.app.TimePickerDialog(context, { _, hour, minute -> scheduleStartInput = String.format(java.util.Locale.getDefault(), "%02d:%02d", hour, minute) }, h, m, true).show()
                    }, modifier = Modifier.fillMaxWidth()) { Text("Hora Inicio: $scheduleStartInput", color = Color.Black) }
                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.material3.OutlinedButton(onClick = {
                        val parts = scheduleEndInput.split(":"); val h = parts.getOrNull(0)?.toIntOrNull() ?: 18; val m = parts.getOrNull(1)?.toIntOrNull() ?: 30
                        android.app.TimePickerDialog(context, { _, hour, minute -> scheduleEndInput = String.format(java.util.Locale.getDefault(), "%02d:%02d", hour, minute) }, h, m, true).show()
                    }, modifier = Modifier.fillMaxWidth()) { Text("Hora Fin: $scheduleEndInput", color = Color.Black) }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val newSchedule = com.example.entrenamientos.data.TrainingSchedule(id = scheduleToEdit?.id ?: 0, teamYear = selectedTeam, dayOfWeek = scheduleDayInput, startTime = scheduleStartInput, endTime = scheduleEndInput)
                    viewModel.addOrUpdateSchedule(newSchedule, onSuccess = { showScheduleDialog = false }, onError = { errorMsg -> scheduleError = errorMsg })
                }, colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceGreen)) { Text("Guardar", color = Color.Black) }
            },
            dismissButton = { TextButton(onClick = { showScheduleDialog = false }) { Text("Cancelar", color = Color.Gray) } }
        )
    }

    // WIZARD DE PARTIDOS
    if (showMatchDialog) {
        AlertDialog(
            onDismissRequest = { showMatchDialog = false },
            title = { Text(if (matchToEdit == null) "Nuevo Partido - Paso $matchStep/4" else "Editar Partido - Paso $matchStep/4") },
            text = {
                Column {
                    when (matchStep) {
                        1 -> {
                            Text("Selecciona la fecha:", style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            androidx.compose.material3.OutlinedButton(
                                onClick = {
                                    val parts = matchDateInput.split("-")
                                    val d = parts.getOrNull(0)?.toIntOrNull() ?: 5
                                    val m = (parts.getOrNull(1)?.toIntOrNull() ?: 9) - 1
                                    val y = parts.getOrNull(2)?.toIntOrNull() ?: 2026

                                    val dpd = android.app.DatePickerDialog(context, { _, year, month, day ->
                                        matchDateInput = String.format(java.util.Locale.getDefault(), "%02d-%02d-%04d", day, month + 1, year)
                                    }, y, m, d)

                                    val minCal = java.util.Calendar.getInstance().apply { set(2026, 8, 1) }
                                    dpd.datePicker.minDate = minCal.timeInMillis

                                    val maxCal = java.util.Calendar.getInstance().apply { set(2027, 4, 31) }
                                    dpd.datePicker.maxDate = maxCal.timeInMillis

                                    dpd.show()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(matchDateInput, color = Color.Black)
                            }
                        }
                        2 -> {
                            Text("Selecciona la hora:", style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            androidx.compose.material3.OutlinedButton(
                                onClick = {
                                    val parts = matchTimeInput.split(":")
                                    val h = parts.getOrNull(0)?.toIntOrNull() ?: 10
                                    val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
                                    android.app.TimePickerDialog(context, { _, hour, minute ->
                                        matchTimeInput = String.format(java.util.Locale.getDefault(), "%02d:%02d", hour, minute)
                                    }, h, m, true).show()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(matchTimeInput, color = Color.Black)
                            }
                        }
                        3 -> {
                            Text("¿Dónde se juega?", style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                Button(
                                    onClick = { matchIsLocalInput = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (matchIsLocalInput) com.example.entrenamientos.ui.theme.InfantilBlue else Color.Gray)
                                ) { Text("Local") }
                                Button(
                                    onClick = { matchIsLocalInput = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (!matchIsLocalInput) com.example.entrenamientos.ui.theme.InfantilBlue else Color.Gray)
                                ) { Text("Visitante") }
                            }
                        }
                        4 -> {
                            OutlinedTextField(value = matchLocationInput, onValueChange = { matchLocationInput = it }, label = { Text("Polideportivo") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(value = matchOpponentInput, onValueChange = { matchOpponentInput = it }, label = { Text("Equipo Rival") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            },
            confirmButton = {
                if (matchStep < 4) {
                    Button(onClick = { matchStep++ }, colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.InfantilBlue)) { Text("Siguiente") }
                } else {
                    Button(
                        onClick = {
                            val parts = matchDateInput.split("-")
                            val dateForDb = "${parts[2]}-${parts[1]}-${parts[0]}"

                            // --- VALIDACIÓN DE DÍA FESTIVO ---
                            val dateObj = java.time.LocalDate.parse(dateForDb)
                            if (viewModel.isHoliday(dateObj)) {
                                android.widget.Toast.makeText(context, "No puedes programar partidos en días festivos", android.widget.Toast.LENGTH_SHORT).show()
                                return@Button // Bloquea el guardado si es festivo
                            }

                            val newMatch = com.example.entrenamientos.data.Match(
                                id = matchToEdit?.id ?: 0,
                                date = dateForDb,
                                time = matchTimeInput,
                                isLocal = matchIsLocalInput,
                                location = matchLocationInput,
                                opponent = matchOpponentInput
                            )
                            viewModel.addOrUpdateMatch(newMatch)
                            showMatchDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceGreen)
                    ) { Text("Guardar", color = Color.Black) }
                }
            },
            dismissButton = {
                Row {
                    if (matchStep > 1) {
                        TextButton(onClick = { matchStep-- }) { Text("Atrás", color = Color.Gray) }
                    }
                    TextButton(onClick = { showMatchDialog = false }) { Text("Cancelar", color = Color.Gray) }
                }
            }
        )
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