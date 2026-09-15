package com.example.entrenamientos.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.entrenamientos.ui.BasketViewModel
import com.example.entrenamientos.ui.theme.AttendanceGreen
import com.example.entrenamientos.ui.theme.AttendanceRed

@Composable
fun ResultadoScreen(
    viewModel: BasketViewModel,
    navController: NavController
) {
    val selectedDateStr by viewModel.selectedDate.collectAsState()
    val teamYear by viewModel.selectedTeamYear.collectAsState()

    val allMatches by viewModel.matches.collectAsState()
    val teamsList by viewModel.teams.collectAsState()

    // Buscamos SIEMPRE el partido exacto por fecha + equipo.
    val match = allMatches.find {
        it.date == selectedDateStr && it.teamYear == teamYear
    }

    // Si por cualquier motivo no existe el partido, volvemos al calendario.
    if (match == null) {
        LaunchedEffect(Unit) {
            navController.navigate("calendar") {
                popUpTo("calendar") { inclusive = false }
                launchSingleTop = true
            }
        }
        return
    }

    val ownTeam = teamsList.find { it.year == teamYear }
    val ownTeamColor = ownTeam?.colorHex?.let {
        try { Color(android.graphics.Color.parseColor(it)) } catch (_: Exception) { Color(0xFF2196F3) }
    } ?: Color(0xFF2196F3)
    val ownTeamName = ownTeam?.shortName?.ifBlank { ownTeam.name } ?: "Tu equipo"
    val opponentName = match.opponent.ifBlank { "Rival" }

    // A la izquierda del marcador siempre va el equipo local, a la derecha el visitante.
    val leftName = if (match.isLocal) ownTeamName else opponentName
    val rightName = if (match.isLocal) opponentName else ownTeamName

    val matchDate = try { java.time.LocalDate.parse(match.date) } catch (_: Exception) { null }
    val formattedDate = matchDate?.let {
        val dow = it.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES")).replaceFirstChar { c -> c.uppercase() }
        val month = it.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES"))
        "$dow ${it.dayOfMonth} de $month"
    } ?: match.date

    // ------------------------------------------------------------
    // DATOS DEL RESULTADO
    // match.id es la clave del estado local: al cambiar de partido
    // nunca se arrastran los datos del partido anterior.
    // ------------------------------------------------------------

    var resLocal by remember(match.id) { mutableStateOf(match.resultLocal?.toString() ?: "") }
    var resVisitor by remember(match.id) { mutableStateOf(match.resultVisitor?.toString() ?: "") }
    var ftMade by remember(match.id) { mutableStateOf(if (match.ftMade == 0) "" else match.ftMade.toString()) }
    var ftAttempted by remember(match.id) { mutableStateOf(if (match.ftAttempted == 0) "" else match.ftAttempted.toString()) }
    var observaciones by remember(match.id) { mutableStateOf(match.observations ?: "") }

    val ftPercentage = remember(ftMade, ftAttempted) {
        val made = ftMade.toIntOrNull() ?: 0
        val attempted = ftAttempted.toIntOrNull() ?: 0
        if (attempted > 0) (made * 100f / attempted).let { "%.0f".format(it) } else null
    }

    fun goToCalendar() {
        navController.navigate("calendar") {
            popUpTo("calendar") { inclusive = false }
            launchSingleTop = true
        }
    }

    BackHandler { goToCalendar() }

    // ------------------------------------------------------------
    // PANTALLA
    // ------------------------------------------------------------

    Column(modifier = Modifier.fillMaxSize()) {

        // --- CABECERA: rival, fecha y sede, con el color del equipo ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ownTeamColor.copy(alpha = 0.12f))
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = { goToCalendar() }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = ownTeamColor)
                }
                Text(
                    text = "Resultado del partido",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ownTeamColor
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "$ownTeamName vs $opponentName",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Text(
                    text = "${if (match.isLocal) "En casa" else "Fuera"} · $formattedDate" + if (match.time.isNotBlank()) " · ${match.time}" else "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {

            // --------------------------------------------------------
            // MARCADOR: el elemento protagonista de la pantalla
            // --------------------------------------------------------
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ScoreColumn(
                        teamLabel = leftName,
                        value = resLocal,
                        onValueChange = { if (it.length <= 3 && it.all(Char::isDigit)) resLocal = it },
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "–",
                        style = MaterialTheme.typography.displaySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    ScoreColumn(
                        teamLabel = rightName,
                        value = resVisitor,
                        onValueChange = { if (it.length <= 3 && it.all(Char::isDigit)) resVisitor = it },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --------------------------------------------------------
            // TIROS LIBRES
            // --------------------------------------------------------
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Tiros libres", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        if (ftPercentage != null) {
                            Text(
                                text = "$ftPercentage %",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = ownTeamColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = ftMade,
                            onValueChange = { if (it.length <= 3 && it.all(Char::isDigit)) ftMade = it },
                            label = { Text("Convertidos") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = ftAttempted,
                            onValueChange = { if (it.length <= 3 && it.all(Char::isDigit)) ftAttempted = it },
                            label = { Text("Intentados") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --------------------------------------------------------
            // OBSERVACIONES
            // --------------------------------------------------------
            Text(
                text = "Observaciones del partido",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = observaciones,
                onValueChange = { observaciones = it },
                placeholder = { Text("Escribe aquí las observaciones...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // --------------------------------------------------------
        // BOTONES (fijos abajo, fuera del scroll)
        // --------------------------------------------------------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { goToCalendar() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AttendanceRed)
            ) {
                Text("Volver")
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
                        return@Button
                    }

                    val updatedMatch = match.copy(
                        resultLocal = localScore,
                        resultVisitor = visitorScore,
                        ftMade = ftMade.toIntOrNull() ?: 0,
                        ftAttempted = ftAttempted.toIntOrNull() ?: 0,
                        observations = observaciones
                    )

                    viewModel.addOrUpdateMatch(
                        match = updatedMatch,
                        onSuccess = { goToCalendar() },
                        onError = { errorMessage ->
                            android.widget.Toast.makeText(
                                navController.context,
                                errorMessage,
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = AttendanceGreen)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Black)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (match.resultLocal != null) "Actualizar" else "Guardar",
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
private fun ScoreColumn(
    teamLabel: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = teamLabel,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        BasicTextFieldScore(value = value, onValueChange = onValueChange)
    }
}

@Composable
private fun BasicTextFieldScore(value: String, onValueChange: (String) -> Unit) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        textStyle = MaterialTheme.typography.displayMedium.copy(
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 48.sp
        ),
        modifier = Modifier.width(90.dp),
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.Center) {
                if (value.isEmpty()) {
                    Text(
                        text = "–",
                        style = MaterialTheme.typography.displayMedium,
                        color = Color.LightGray,
                        fontSize = 48.sp,
                        textAlign = TextAlign.Center
                    )
                }
                innerTextField()
            }
        }
    )
}