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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.entrenamientos.ui.theme.InfantilBlue
import com.example.entrenamientos.ui.theme.PrebenjaminPink
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

val PrebenjaminPink = Color(0xFFFF80AB)
val InfantilBlue = Color(0xFF2196F3)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(viewModel: BasketViewModel = hiltViewModel(), navController: androidx.navigation.NavController) {
    var currentMonth by remember { mutableStateOf(YearMonth.of(2026, 9)) }
    val minMonth = YearMonth.of(2026, 9)
    val maxMonth = YearMonth.of(2027, 5)

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }
    val selectedDateStr by viewModel.selectedDate.collectAsState()
    val selectedDate = LocalDate.parse(selectedDateStr)

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }, enabled = currentMonth.isAfter(minMonth)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Mes anterior")
            }
            Text("${currentMonth.month.getDisplayName(TextStyle.FULL, Locale("es", "ES")).uppercase()} ${currentMonth.year}", style = MaterialTheme.typography.titleLarge)
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

        val days = mutableListOf<LocalDate?>()
        val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value
        for (i in 1 until firstDayOfWeek) days.add(null)
        for (i in 1..currentMonth.lengthOfMonth()) days.add(currentMonth.atDay(i))

        // Rellenamos con nulos al final para que la cuadrícula siempre sea perfecta (múltiplo de 7)
        while (days.size % 7 != 0) {
            days.add(null)
        }

        // Cuadrícula flexible que se estira para ocupar el 100% de la pantalla restante
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
                                    onClick = {
                                        viewModel.setSelectedDate(date.toString())
                                        if (viewModel.getTeamsForDate(date).isNotEmpty() || viewModel.hasMatchOnDate(date)) {
                                            showBottomSheet = true
                                        }
                                    }
                                )
                            } else {
                                Spacer(modifier = Modifier.fillMaxSize().padding(2.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            DayOptionsMenu(
                date = selectedDate,
                viewModel = viewModel,
                navController = navController,
                onClose = { showBottomSheet = false }
            )
        }
    }
}

@Composable
fun DayCell(date: LocalDate, viewModel: BasketViewModel, onClick: () -> Unit) {
    val teams = viewModel.getTeamsForDate(date)
    val hasMatch = viewModel.hasMatchOnDate(date)

    Box(
        modifier = Modifier
            .fillMaxSize() // Ahora se estira ocupando todo el espacio disponible
            .padding(2.dp)
            .background(Color.White, shape = MaterialTheme.shapes.small)
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        // Número del día y el círculo del partido fijados arriba
        Row(
            modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
            )
            if (hasMatch) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(InfantilBlue)
                )
            }
        }

        // Franjas de los equipos centradas perfectamente en el cuadrado
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (teams.contains(2018)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp) // Franja más ancha
                        .background(PrebenjaminPink, shape = MaterialTheme.shapes.extraSmall),
                    contentAlignment = Alignment.Center // Centrado vertical y horizontal del texto
                ) {
                    Text(
                        text = "2018",
                        color = Color.White,
                        fontSize = 12.sp, // Letra más grande
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold // En negrita para que se lea claro
                    )
                }
            }

            if (teams.contains(2013)) {
                if (teams.contains(2018)) {
                    Spacer(modifier = Modifier.height(4.dp)) // Espacio de separación si coinciden los dos
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp) // Franja más ancha
                        .background(InfantilBlue, shape = MaterialTheme.shapes.extraSmall),
                    contentAlignment = Alignment.Center // Centrado vertical y horizontal del texto
                ) {
                    Text(
                        text = "2013",
                        color = Color.White,
                        fontSize = 12.sp, // Letra más grande
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold // En negrita para que se lea claro
                    )
                }
            }
        }
    }
}

@Composable
fun DayOptionsMenu(date: LocalDate, viewModel: BasketViewModel, navController: androidx.navigation.NavController, onClose: () -> Unit) {
    val teams = viewModel.getTeamsForDate(date)
    val hasMatch = viewModel.hasMatchOnDate(date)

    val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("es", "ES")).replaceFirstChar { it.uppercase() }
    val monthName = date.month.getDisplayName(TextStyle.FULL, Locale("es", "ES"))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Entrenamientos $dayOfWeek ${date.dayOfMonth} de $monthName",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (teams.isNotEmpty()) {
            teams.forEach { teamYear ->
                val teamName = if (teamYear == 2018) "Prebenjamines (2018)" else "Infantiles (2013)"
                val teamColor = if (teamYear == 2018) com.example.entrenamientos.ui.theme.PrebenjaminPink else com.example.entrenamientos.ui.theme.InfantilBlue

                Text(teamName, style = MaterialTheme.typography.bodyMedium, color = teamColor, modifier = Modifier.padding(top = 8.dp))

                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(
                        onClick = {
                            viewModel.setSelectedTeamYear(teamYear)
                            onClose()
                            navController.navigate("notes/ENTRENAMIENTO")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = teamColor)
                    ) { Text("Entrenamiento", color = Color.White) }

                    Button(
                        onClick = {
                            viewModel.setSelectedTeamYear(teamYear)
                            onClose()
                            navController.navigate("attendance")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = teamColor)
                    ) { Text("Asistencia", color = Color.White) }

                    Button(
                        onClick = {
                            viewModel.setSelectedTeamYear(teamYear)
                            onClose()
                            navController.navigate("notes/OTROS")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = teamColor)
                    ) { Text("Notas", color = Color.White) }
                }
            }
        }

        if (hasMatch) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text("Partido (Infantiles)", style = MaterialTheme.typography.labelLarge, color = com.example.entrenamientos.ui.theme.InfantilBlue)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { /* Editar hora */ }, colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.InfantilBlue)) { Text("Hora") }

                Button(
                    onClick = {
                        onClose()
                        navController.navigate("convocatoria")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.InfantilBlue)
                ) { Text("Convocatoria") }

                Button(onClick = { /* Resultado */ }, colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.InfantilBlue)) { Text("Resultado") }
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
fun ConvocatoriaScreen(viewModel: BasketViewModel = hiltViewModel(), navController: androidx.navigation.NavController) {
    val dateStr by viewModel.selectedDate.collectAsState()
    // Las infantiles siempre son de la categoría 2013
    val players by viewModel.getPlayersForTeam(2013).collectAsState(initial = emptyList())

    var step by remember { mutableStateOf(1) }
    val selectedPlayerIds = remember { androidx.compose.runtime.mutableStateListOf<Long>() }
    val reasons = remember { androidx.compose.runtime.mutableStateMapOf<Long, String>() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Convocatoria Infantiles", style = MaterialTheme.typography.headlineMedium, color = InfantilBlue)
        Text(text = "Partido: $dateStr", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))

        if (step == 1) {
            // --- PASO 1: Selección de jugadoras (Mínimo 8, Máximo 12) ---
            Text("Selecciona entre 8 y 12 jugadoras:", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "Seleccionadas: ${selectedPlayerIds.size}",
                color = if (selectedPlayerIds.size in 8..12) com.example.entrenamientos.ui.theme.AttendanceGreen else com.example.entrenamientos.ui.theme.AttendanceRed
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(players) { player ->
                    val isSelected = selectedPlayerIds.contains(player.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(if (isSelected) InfantilBlue.copy(alpha = 0.2f) else Color.LightGray.copy(alpha = 0.3f))
                            .clickable {
                                if (isSelected) selectedPlayerIds.remove(player.id)
                                else if (selectedPlayerIds.size < 12) selectedPlayerIds.add(player.id)
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = player.name, style = MaterialTheme.typography.bodyLarge)
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = "Seleccionada", tint = InfantilBlue)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { navController.popBackStack() }, colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceRed)) {
                    Text("Cancelar")
                }
                Button(
                    onClick = { step = 2 },
                    enabled = selectedPlayerIds.size in 8..12,
                    colors = ButtonDefaults.buttonColors(containerColor = InfantilBlue)
                ) {
                    Text("Siguiente")
                }
            }
        } else {
            // --- PASO 2: Motivos de las desconvocadas ---
            val unselectedPlayers = players.filter { !selectedPlayerIds.contains(it.id) }

            if (unselectedPlayers.isEmpty()) {
                Text("Todas las jugadoras han sido convocadas. ¡Equipazo!", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.weight(1f))
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(unselectedPlayers) { player ->
                        OutlinedTextField(
                            value = reasons[player.id] ?: "",
                            onValueChange = { reasons[player.id] = it },
                            label = { Text("¿Por qué ${player.name} ha sido desconvocada?") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { step = 1 }, colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) {
                    Text("Atrás")
                }
                Button(
                    onClick = {
                        viewModel.saveConvocatoria(dateStr, selectedPlayerIds.toSet(), reasons)
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceGreen)
                ) {
                    Text("Guardar", color = Color.Black)
                }
            }
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
fun StatsScreen(viewModel: BasketViewModel = hiltViewModel()) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Pantalla de Estadísticas en construcción", style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun SettingsScreen(viewModel: BasketViewModel = hiltViewModel()) {
    var activeTab by remember { mutableStateOf("JUGADORAS") } // "JUGADORAS", "HORARIOS" o "PARTIDOS"
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
    var matchDateInput by remember { mutableStateOf("2026-09-05") }
    var matchTimeInput by remember { mutableStateOf("10:00") }
    var matchIsLocalInput by remember { mutableStateOf(true) }
    var matchLocationInput by remember { mutableStateOf("") }
    var matchOpponentInput by remember { mutableStateOf("") }

    val daysOfWeek = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Ajustes del Club", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // Selector de Pestañas
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(
                onClick = { activeTab = "JUGADORAS" },
                colors = ButtonDefaults.buttonColors(containerColor = if (activeTab == "JUGADORAS") Color.DarkGray else Color.LightGray)
            ) { Text("Jugadoras", color = if (activeTab == "JUGADORAS") Color.White else Color.Black) }

            Button(
                onClick = { activeTab = "HORARIOS" },
                colors = ButtonDefaults.buttonColors(containerColor = if (activeTab == "HORARIOS") Color.DarkGray else Color.LightGray)
            ) { Text("Horarios", color = if (activeTab == "HORARIOS") Color.White else Color.Black) }

            Button(
                onClick = { activeTab = "PARTIDOS" },
                colors = ButtonDefaults.buttonColors(containerColor = if (activeTab == "PARTIDOS") com.example.entrenamientos.ui.theme.InfantilBlue else Color.LightGray)
            ) { Text("Partidos", color = if (activeTab == "PARTIDOS") Color.White else Color.Black) }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Selector de Equipo (Oculto en Partidos, porque solo aplica a Infantiles)
        if (activeTab != "PARTIDOS") {
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
        } else {
            Text("Gestión de Partidos (Infantiles 2013)", style = MaterialTheme.typography.labelMedium, color = com.example.entrenamientos.ui.theme.InfantilBlue, modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 16.dp))
        }

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

        } else {
            // PESTAÑA PARTIDOS
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(allMatches) { match ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(MaterialTheme.shapes.small).background(Color.LightGray.copy(alpha = 0.2f)).padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "${match.date} a las ${match.time}", style = MaterialTheme.typography.bodyMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            Text(text = "vs ${match.opponent} (${if (match.isLocal) "Local" else "Visitante"})", style = MaterialTheme.typography.bodyMedium)
                        }
                        Row {
                            IconButton(onClick = {
                                matchToEdit = match; matchStep = 1
                                matchDateInput = match.date; matchTimeInput = match.time
                                matchIsLocalInput = match.isLocal; matchLocationInput = match.location; matchOpponentInput = match.opponent
                                showMatchDialog = true
                            }) { Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.DarkGray) }
                            IconButton(onClick = { viewModel.deleteMatch(match) }) { Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = com.example.entrenamientos.ui.theme.AttendanceRed) }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    matchToEdit = null; matchStep = 1; matchDateInput = "2026-09-05"; matchTimeInput = "10:00"
                    matchIsLocalInput = true; matchLocationInput = ""; matchOpponentInput = ""
                    showMatchDialog = true
                },
                modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceGreen)
            ) { Text("Añadir Partido", color = Color.Black) }
        }
    }

    // Diálogos de Jugadoras y Horarios (Mantenidos igual)
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
                                    val y = parts.getOrNull(0)?.toIntOrNull() ?: 2026
                                    val m = (parts.getOrNull(1)?.toIntOrNull() ?: 9) - 1
                                    val d = parts.getOrNull(2)?.toIntOrNull() ?: 1

                                    val dpd = android.app.DatePickerDialog(context, { _, year, month, day ->
                                        matchDateInput = String.format(java.util.Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day)
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
                            val newMatch = com.example.entrenamientos.data.Match(
                                id = matchToEdit?.id ?: 0, date = matchDateInput, time = matchTimeInput,
                                isLocal = matchIsLocalInput, location = matchLocationInput, opponent = matchOpponentInput
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