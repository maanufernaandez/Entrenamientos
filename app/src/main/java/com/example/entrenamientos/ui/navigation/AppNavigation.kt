package com.example.entrenamientos.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.entrenamientos.ui.BasketViewModel
import com.example.entrenamientos.ui.screens.AttendanceScreen
import com.example.entrenamientos.ui.screens.CalendarScreen
import com.example.entrenamientos.ui.screens.ConvocatoriaScreen
import com.example.entrenamientos.ui.screens.QuintetosScreen
import com.example.entrenamientos.ui.screens.ResultadoScreen
import com.example.entrenamientos.ui.screens.SettingsScreen
import com.example.entrenamientos.ui.screens.StatsScreen
import com.example.entrenamientos.ui.screens.TrainingNoteScreen

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    data object Calendar : Screen("calendar", "Calendario", Icons.Default.DateRange)
    data object Stats : Screen("stats", "Estadísticas", Icons.Default.BarChart)
    data object Settings : Screen("settings", "Ajustes", Icons.Default.Settings)
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val items = listOf(Screen.Calendar, Screen.Stats, Screen.Settings)

    val sharedViewModel: BasketViewModel = hiltViewModel()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            val selectedBlue = Color(0xFF2196F3)
            val unselectedLightBlue = Color(0xFFE3F2FD)

            // Contenedor principal en formato columna para colocar las líneas arriba y abajo
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(unselectedLightBlue)
                    .navigationBarsPadding()
            ) {
                // --- LÍNEA HORIZONTAL SUPERIOR ---
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.Black))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEachIndexed { index, screen ->
                        val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

                        Button(
                            onClick = {
                                if (!isSelected) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) selectedBlue else unselectedLightBlue
                            ),
                            shape = RectangleShape
                        ) {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                                tint = if (isSelected) Color.White else Color.Black,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        // --- LÍNEAS VERTICALES INTERNAS (Solo entre botones) ---
                        if (index < items.size - 1) {
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(64.dp)
                                    .background(Color.Black)
                            )
                        }
                    }
                }

                // --- LÍNEA HORIZONTAL INFERIOR ---
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.Black))
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = Screen.Calendar.route,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
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
            composable("quintetos") {
                QuintetosScreen(viewModel = sharedViewModel, navController = navController)
            }
        }
    }
}