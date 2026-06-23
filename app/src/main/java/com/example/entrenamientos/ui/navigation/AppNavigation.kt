package com.example.entrenamientos.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Calendar : Screen("calendar", "Calendario", Icons.Default.DateRange)
    object Stats : Screen("stats", "Estadísticas", Icons.Default.Star)
    object Settings : Screen("settings", "Ajustes", Icons.Default.Settings)
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val items = listOf(Screen.Calendar, Screen.Stats, Screen.Settings)

    Scaffold(
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
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->

        val sharedViewModel: com.example.entrenamientos.ui.BasketViewModel = androidx.hilt.navigation.compose.hiltViewModel()

        NavHost(
            navController = navController,
            startDestination = Screen.Calendar.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Calendar.route) {
                com.example.entrenamientos.ui.screens.CalendarScreen(viewModel = sharedViewModel, navController = navController)
            }
            composable(Screen.Stats.route) {
                com.example.entrenamientos.ui.screens.StatsScreen(viewModel = sharedViewModel)
            }
            composable(Screen.Settings.route) {
                com.example.entrenamientos.ui.screens.SettingsScreen(viewModel = sharedViewModel)
            }
            composable("attendance") {
                com.example.entrenamientos.ui.screens.AttendanceScreen(viewModel = sharedViewModel, navController = navController)
            }
            composable("notes/{type}") { backStackEntry ->
                val type = backStackEntry.arguments?.getString("type") ?: "ENTRENAMIENTO"
                com.example.entrenamientos.ui.screens.TrainingNoteScreen(viewModel = sharedViewModel, navController = navController, noteType = type)
            }
            // Rutas añadidas
            composable("convocatoria") {
                com.example.entrenamientos.ui.screens.ConvocatoriaScreen(viewModel = sharedViewModel, navController = navController)
            }
            composable("resultado") {
                com.example.entrenamientos.ui.screens.ResultadoScreen(viewModel = sharedViewModel, navController = navController)
            }
        }
    }
}