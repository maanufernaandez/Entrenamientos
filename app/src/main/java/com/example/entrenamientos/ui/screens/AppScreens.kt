package com.example.entrenamientos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
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
fun CalendarScreen(viewModel: BasketViewModel = hiltViewModel()) {
    var currentMonth by remember { mutableStateOf(YearMonth.of(2026, 9)) }
    val minMonth = YearMonth.of(2026, 9)
    val maxMonth = YearMonth.of(2027, 5)

    // Estados para el Menú Inferior (Bottom Sheet)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }
    val selectedDateStr by viewModel.selectedDate.collectAsState()
    val selectedDate = LocalDate.parse(selectedDateStr)

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // (1 y 2: Cabecera y Días de la semana se mantienen igual)
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

        // (3 y 4: Cálculo y Cuadrícula)
        val days = mutableListOf<LocalDate?>()
        val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value
        for (i in 1 until firstDayOfWeek) days.add(null)
        for (i in 1..currentMonth.lengthOfMonth()) days.add(currentMonth.atDay(i))

        LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.fillMaxSize()) {
            items(days) { date ->
                if (date != null) {
                    DayCell(
                        date = date,
                        viewModel = viewModel,
                        onClick = {
                            viewModel.setSelectedDate(date.toString())
                            // Solo abrimos el menú si hay algo en ese día (entrenamiento o partido)
                            if (viewModel.getTeamsForDate(date).isNotEmpty() || viewModel.hasMatchOnDate(date)) {
                                showBottomSheet = true
                            }
                        }
                    )
                } else {
                    Spacer(modifier = Modifier.padding(8.dp))
                }
            }
        }
    }

    // Modal Bottom Sheet que se despliega al pulsar un día válido
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            DayOptionsMenu(
                date = selectedDate,
                viewModel = viewModel,
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
            .aspectRatio(0.7f) // Proporción rectangular para que quepan las franjas
            .padding(2.dp)
            .background(Color.White, shape = MaterialTheme.shapes.small)
            .clickable { onClick() }
            .padding(2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Número del día y símbolo de partido
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 2.dp)
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

            Spacer(modifier = Modifier.weight(1f))

            // Franja Rosa (Prebenjamín 2018)
            if (teams.contains(2018)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .background(PrebenjaminPink, shape = MaterialTheme.shapes.extraSmall),
                    contentAlignment = Alignment.Center
                ) {
                    Text("2018", color = Color.White, fontSize = 9.sp)
                }
            }

            // Franja Azul (Infantil 2013)
            if (teams.contains(2013)) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .background(InfantilBlue, shape = MaterialTheme.shapes.extraSmall),
                    contentAlignment = Alignment.Center
                ) {
                    Text("2013", color = Color.White, fontSize = 9.sp)
                }
            }
        }
    }
}

@Composable
fun DayOptionsMenu(date: LocalDate, viewModel: BasketViewModel, onClose: () -> Unit) {
    val teams = viewModel.getTeamsForDate(date)
    val hasMatch = viewModel.hasMatchOnDate(date)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Opciones para el ${date.dayOfMonth} de ${date.month.getDisplayName(TextStyle.FULL, Locale("es", "ES"))}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Opciones de Entrenamiento (Si entrena algún equipo)
        if (teams.isNotEmpty()) {
            Text("Entrenamiento", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { /* Navegar a Asistencia */ }) { Text("Asistencia") }
                Button(onClick = { /* Navegar a Notas Entrenamiento */ }) { Text("Entrenamiento") }
                Button(onClick = { /* Navegar a Otras Notas */ }) { Text("Otros") }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Opciones de Partido (Solo Infantiles los sábados)
        if (hasMatch) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text("Partido (Infantiles)", style = MaterialTheme.typography.labelMedium, color = InfantilBlue)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { /* Editar fecha/hora */ }, colors = ButtonDefaults.buttonColors(containerColor = InfantilBlue)) { Text("Editar Hora") }
                Button(onClick = { /* Navegar a Convocatoria */ }, colors = ButtonDefaults.buttonColors(containerColor = InfantilBlue)) { Text("Convocatoria") }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { /* Navegar a Resultados */ }, colors = ButtonDefaults.buttonColors(containerColor = InfantilBlue)) { Text("Resultado") }
                Button(onClick = { /* Lógica para eliminar partido */ }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("Eliminar Partido") }
            }
        }
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
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Pantalla de Configuración en construcción", style = MaterialTheme.typography.titleMedium)
    }
}