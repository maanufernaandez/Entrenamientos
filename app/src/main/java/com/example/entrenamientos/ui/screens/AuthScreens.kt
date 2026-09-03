package com.example.entrenamientos.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.entrenamientos.R
import com.example.entrenamientos.ui.AuthViewModel

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showForgotPassDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Logo oficial de tu app (Tamaño aumentado a 160.dp)
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo de la app",
            modifier = Modifier.size(160.dp).clip(CircleShape)
        )
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    authViewModel.login(email.trim(), password,
                        onSuccess = { onLoginSuccess() },
                        onError = { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
                    )
                } else {
                    Toast.makeText(context, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text("Iniciar Sesión", color = Color.White, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = { showForgotPassDialog = true },
            contentPadding = PaddingValues(0.dp)
        ) {
            Text("He olvidado la contraseña", color = Color(0xFF2196F3), fontWeight = FontWeight.Bold)
        }

        TextButton(
            onClick = onNavigateToRegister,
            contentPadding = PaddingValues(0.dp)
        ) {
            Text("¿No tienes cuenta? Regístrate", color = Color(0xFF2196F3), fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(48.dp))
    }

    if (showForgotPassDialog) {
        var resetEmail by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showForgotPassDialog = false },
            title = { Text("Recuperar contraseña") },
            text = {
                Column {
                    Text("Introduce tu correo y te enviaremos un enlace para restablecerla.")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Correo electrónico") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    authViewModel.resetPassword(resetEmail.trim(),
                        onSuccess = {
                            Toast.makeText(context, "Correo enviado", Toast.LENGTH_SHORT).show()
                            showForgotPassDialog = false
                        },
                        onError = { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
                    )
                }) { Text("Enviar") }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPassDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var club by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Variables reactivas para mostrar los mensajes de error en rojo
    val isPasswordLengthError = password.isNotEmpty() && (password.length < 8 || password.length > 20)
    val isPasswordMatchError = confirmPassword.isNotEmpty() && password != confirmPassword

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        Text("Crear Cuenta", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Apellidos") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = club, onValueChange = { club = it }, label = { Text("Nombre del Club") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Correo electrónico") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            isError = isPasswordLengthError,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        if (isPasswordLengthError) {
            Text(
                text = "La contraseña debe tener entre 8 y 20 caracteres",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth().padding(start = 4.dp, top = 2.dp),
                textAlign = TextAlign.Start
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Repetir Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            isError = isPasswordMatchError,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        if (isPasswordMatchError) {
            Text(
                text = "Las contraseñas no coinciden",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth().padding(start = 4.dp, top = 2.dp),
                textAlign = TextAlign.Start
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (name.isBlank() || lastName.isBlank() || club.isBlank() || email.isBlank() || password.isBlank()) {
                    Toast.makeText(context, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
                } else if (isPasswordLengthError) {
                    Toast.makeText(context, "La contraseña debe tener entre 8 y 20 caracteres", Toast.LENGTH_SHORT).show()
                } else if (isPasswordMatchError) {
                    Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                } else {
                    authViewModel.register(email.trim(), password, name.trim(), lastName.trim(), club.trim(),
                        onSuccess = {
                            // Se cambia el texto del aviso y se quita lo del correo de verificación
                            Toast.makeText(context, "Cuenta creada correctamente", Toast.LENGTH_SHORT).show()
                            onRegisterSuccess()
                        },
                        onError = { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = com.example.entrenamientos.ui.theme.AttendanceGreen)
        ) {
            Text("Registrarse", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateBack) {
            Text("¿Ya tienes cuenta? Inicia sesión", color = Color.DarkGray)
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}