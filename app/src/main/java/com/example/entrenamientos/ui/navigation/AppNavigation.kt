package com.example.entrenamientos.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.entrenamientos.ui.BasketViewModel
import com.example.entrenamientos.ui.screens.*

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    data object Calendar : Screen("calendar", "Calendario", Icons.Default.DateRange)
    data object Stats : Screen("stats", "Estadísticas", Icons.Default.BarChart)
    data object Settings : Screen("settings", "Ajustes", Icons.Default.Settings)
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val items = listOf(Screen.Calendar, Screen.Stats, Screen.Settings)

    // Declaramos una instancia única del ViewModel para ser compartida en toda la navegación
    val sharedViewModel: BasketViewModel = hiltViewModel()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background, // Aseguramos fondo opaco en el Scaffold
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            // PREVENCIÓN DE PARPADEO: Solo encola la navegación si no estamos ya en esa pantalla
                            if (currentDestination?.hierarchy?.any { it.route == screen.route } != true) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = Screen.Calendar.route,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background) // Fondo sólido para evitar superposiciones
                .padding(innerPadding),
            // --- ANULACIÓN FORZADA DE ANIMACIONES (Duración 0 milisegundos) ---
            enterTransition = { androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(0)) },
            exitTransition = { androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(0)) },
            popEnterTransition = { androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(0)) },
            popExitTransition = { androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(0)) }
        ) {
            composable(Screen.Calendar.route) {
                CalendarScreen(viewModel = sharedViewModel, navController = navController)
            }
            composable(Screen.Stats.route) {
                StatsScreen(viewModel = sharedViewModel)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(viewModel = sharedViewModel)
            }
            composable("attendance") {
                AttendanceScreen(viewModel = sharedViewModel, navController = navController)
            }
            composable("notes/{type}") { backStackEntry ->
                val type = backStackEntry.arguments?.getString("type") ?: "ENTRENAMIENTO"
                TrainingNoteScreen(viewModel = sharedViewModel, navController = navController, noteType = type)
            }
            composable("convocatoria") {
                ConvocatoriaScreen(viewModel = sharedViewModel, navController = navController)
            }
            composable("resultado") {
                ResultadoScreen(viewModel = sharedViewModel, navController = navController)
            }
        }
    }
}