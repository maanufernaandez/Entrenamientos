package com.example.entrenamientos.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.entrenamientos.ui.BasketViewModel

@Composable
fun ResultadoScreen(
    viewModel: BasketViewModel,
    navController: NavController
) {
    val selectedDateStr by viewModel.selectedDate.collectAsState()
    val teamYear by viewModel.selectedTeamYear.collectAsState()

    val allMatches by viewModel.matches.collectAsState()

    // Buscamos SIEMPRE el partido exacto por fecha + equipo.
    val match = allMatches.find {
        it.date == selectedDateStr && it.teamYear == teamYear
    }

    // Si por cualquier motivo no existe el partido, volvemos al calendario.
    if (match == null) {
        LaunchedEffect(Unit) {
            navController.navigate("calendar") {
                popUpTo("calendar") {
                    inclusive = false
                }
                launchSingleTop = true
            }
        }
        return
    }

    // ------------------------------------------------------------
    // DATOS DEL RESULTADO
    // ------------------------------------------------------------
    // match.id es la clave del estado local.
    // De esta forma, al cambiar de partido nunca se arrastran los
    // datos del partido anterior.
    // ------------------------------------------------------------

    var resLocal by remember(match.id) {
        mutableStateOf(
            match.resultLocal?.toString() ?: ""
        )
    }

    var resVisitor by remember(match.id) {
        mutableStateOf(
            match.resultVisitor?.toString() ?: ""
        )
    }

    var ftMade by remember(match.id) {
        mutableStateOf(
            match.ftMade.toString()
        )
    }

    var ftAttempted by remember(match.id) {
        mutableStateOf(
            match.ftAttempted.toString()
        )
    }

    var observaciones by remember(match.id) {
        mutableStateOf(
            match.observations ?: ""
        )
    }

    // ------------------------------------------------------------
    // FUNCIÓN CENTRAL PARA VOLVER AL CALENDARIO
    // ------------------------------------------------------------

    fun goToCalendar() {
        navController.navigate("calendar") {
            popUpTo("calendar") {
                inclusive = false
            }
            launchSingleTop = true
        }
    }

    BackHandler {
        goToCalendar()
    }

    // ------------------------------------------------------------
    // PANTALLA
    // ------------------------------------------------------------

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Resultado del Partido",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // --------------------------------------------------------
        // MARCADOR
        // --------------------------------------------------------

        Text(
            text = "Marcador Final",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            OutlinedTextField(
                value = resLocal,
                onValueChange = {
                    resLocal = it
                },
                label = {
                    Text("Local")
                },
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            OutlinedTextField(
                value = resVisitor,
                onValueChange = {
                    resVisitor = it
                },
                label = {
                    Text("Visitante")
                },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // --------------------------------------------------------
        // TIROS LIBRES
        // --------------------------------------------------------

        Text(
            text = "Tiros Libres",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            OutlinedTextField(
                value = ftMade,
                onValueChange = {
                    ftMade = it
                },
                label = {
                    Text("Convertidos")
                },
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            OutlinedTextField(
                value = ftAttempted,
                onValueChange = {
                    ftAttempted = it
                },
                label = {
                    Text("Intentados")
                },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        // --------------------------------------------------------
        // OBSERVACIONES
        // --------------------------------------------------------

        Text(
            text = "Observaciones del partido",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        OutlinedTextField(
            value = observaciones,
            onValueChange = {
                observaciones = it
            },
            label = {
                Text("Escribe aquí las observaciones...")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            maxLines = 4
        )

        Spacer(
            modifier = Modifier.weight(1f)
        )

        // --------------------------------------------------------
        // BOTONES
        // --------------------------------------------------------

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // VOLVER
            Button(
                onClick = {
                    goToCalendar()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor =
                    com.example.entrenamientos.ui.theme.AttendanceRed
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Volver",
                    color = Color.White
                )
            }

            // GUARDAR / ACTUALIZAR
            Button(
                onClick = {

                    val localScore =
                        resLocal.toIntOrNull()

                    val visitorScore =
                        resVisitor.toIntOrNull()

                    // ------------------------------------------------
                    // VALIDACIÓN DEL RESULTADO
                    // ------------------------------------------------

                    if (
                        localScore != null &&
                        visitorScore != null &&
                        localScore == visitorScore
                    ) {
                        android.widget.Toast.makeText(
                            navController.context,
                            "El resultado no puede ser empate",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()

                        return@Button
                    }

                    // ------------------------------------------------
                    // GUARDAMOS SOBRE EL MISMO MATCH
                    // ------------------------------------------------

                    val updatedMatch = match.copy(
                        resultLocal = localScore,
                        resultVisitor = visitorScore,
                        ftMade = ftMade.toIntOrNull() ?: 0,
                        ftAttempted = ftAttempted.toIntOrNull() ?: 0,
                        observations = observaciones
                    )

                    // Actualizamos el partido.
                    viewModel.addOrUpdateMatch(
                        match = updatedMatch,
                        onSuccess = {
                            // ----------------------------------------
                            // MUY IMPORTANTE:
                            // después de guardar, vamos al CALENDARIO.
                            // ----------------------------------------
                            goToCalendar()
                        },
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
                colors = ButtonDefaults.buttonColors(
                    containerColor =
                    com.example.entrenamientos.ui.theme.AttendanceGreen
                )
            ) {
                Text(
                    text =
                    if (match.resultLocal != null) {
                        "Actualizar"
                    } else {
                        "Guardar"
                    },
                    color = Color.Black
                )
            }
        }
    }
}