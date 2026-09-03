package com.example.entrenamientos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.entrenamientos.ui.AuthViewModel
import com.example.entrenamientos.ui.navigation.AppNavigation
import com.example.entrenamientos.ui.screens.LoginScreen
import com.example.entrenamientos.ui.screens.RegisterScreen
import com.example.entrenamientos.ui.theme.EntrenamientosTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EntrenamientosTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 1. Instanciamos el ViewModel de Autenticación
                    val authViewModel: AuthViewModel = viewModel()

                    // 2. Observamos si el usuario ya tiene la sesión iniciada
                    val isUserLoggedIn by authViewModel.isUserLoggedIn.collectAsState()

                    // 3. Comprobamos la condición
                    if (!isUserLoggedIn) {
                        // SI NO ESTÁ LOGUEADO: Mostramos navegación de Login / Registro
                        val authNavController = rememberNavController()

                        NavHost(navController = authNavController, startDestination = "login") {
                            composable("login") {
                                LoginScreen(
                                    authViewModel = authViewModel,
                                    onNavigateToRegister = { authNavController.navigate("register") },
                                    onLoginSuccess = {
                                        // No hace falta hacer nada aquí porque el ViewModel
                                        // ya cambia isUserLoggedIn a true y la pantalla se recargará sola.
                                    }
                                )
                            }
                            composable("register") {
                                RegisterScreen(
                                    authViewModel = authViewModel,
                                    onNavigateBack = { authNavController.popBackStack() },
                                    onRegisterSuccess = {
                                        // Al igual que arriba, el estado cambia solo.
                                    }
                                )
                            }
                        }
                    } else {
                        // SI ESTÁ LOGUEADO: Mostramos la App Principal (Calendario, etc.)
                        // Le pasamos la función de hacer Logout para el botón de Ajustes
                        AppNavigation(
                            onLogout = { authViewModel.logout() }
                        )
                    }
                }
            }
        }
    }
}