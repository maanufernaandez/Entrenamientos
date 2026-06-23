package com.example.entrenamientos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.entrenamientos.ui.BasketViewModel

// Colores base de la aplicación
val PrebenjaminPink = Color(0xFFFF80AB)
val InfantilBlue = Color(0xFF2196F3)

@Composable
fun CalendarScreen(viewModel: BasketViewModel = hiltViewModel()) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Vista de Calendario", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // Franjas ilustrativas temporales
        Box(modifier = Modifier.fillMaxWidth().height(50.dp).background(PrebenjaminPink), contentAlignment = Alignment.Center) {
            Text("2018 (Prebenjamín)", color = Color.White)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.fillMaxWidth().height(50.dp).background(InfantilBlue), contentAlignment = Alignment.Center) {
            Text("2013 (Infantil)", color = Color.White)
        }
    }
}

@Composable
fun StatsScreen(viewModel: BasketViewModel = hiltViewModel()) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Estadísticas (Asistencias y Partidos)", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun SettingsScreen(viewModel: BasketViewModel = hiltViewModel()) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Configuración (Gestión de Jugadoras)", style = MaterialTheme.typography.headlineMedium)
    }
}