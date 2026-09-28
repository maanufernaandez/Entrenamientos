package com.example.entrenamientos.ui

import androidx.lifecycle.ViewModel
import com.example.entrenamientos.logic.PasswordValidator
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : ViewModel() {

    // Estado para saber si el usuario ya tiene sesión iniciada
    private val _isUserLoggedIn = MutableStateFlow(auth.currentUser != null)
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn

    /**
     * Traduce los errores más habituales de Firebase Auth a mensajes claros en
     * español. Para credenciales incorrectas usa un mensaje único (no revela si
     * el correo existe o no).
     */
    private fun authErrorMessage(error: Exception, fallback: String): String {
        if (error is FirebaseNetworkException) {
            return "Sin conexión. Comprueba tu conexión a internet e inténtalo de nuevo."
        }

        return when ((error as? FirebaseAuthException)?.errorCode) {
            "ERROR_INVALID_CREDENTIAL",
            "ERROR_INVALID_LOGIN_CREDENTIALS",
            "ERROR_WRONG_PASSWORD",
            "ERROR_USER_NOT_FOUND" -> "Correo o contraseña incorrectos."
            "ERROR_INVALID_EMAIL" -> "El correo electrónico no es válido."
            "ERROR_EMAIL_ALREADY_IN_USE" -> "Ya existe una cuenta con este correo."
            "ERROR_WEAK_PASSWORD" -> "La contraseña es demasiado débil."
            "ERROR_USER_DISABLED" -> "Esta cuenta está deshabilitada."
            "ERROR_TOO_MANY_REQUESTS" -> "Demasiados intentos. Espera unos minutos e inténtalo de nuevo."
            else -> fallback
        }
    }

    fun login(email: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Credenciales incorrectas o error al iniciar sesión.")
            }
    }

    private fun isValidEmail(email: String): Boolean {
        // Comprueba que el formato sea texto@texto.dominio(de 2 a 6 letras)
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$".toRegex()
        return email.matches(emailRegex)
    }

    fun register(email: String, pass: String, name: String, lastName: String, club: String, onSuccess: () -> Unit, onError: (String) -> Unit) {

        // 1. Comprobación estricta de formato de correo
        if (!isValidEmail(email)) {
            onError("El formato del correo electrónico no es válido.")
            return
        }

        // 2. Creación directa (Firebase también verificará que no esté ya registrado)
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                val userId = user?.uid ?: return@addOnSuccessListener

                val userData = mapOf(
                    "name" to name,
                    "lastName" to lastName,
                    "club" to club
                )

                db.collection("users").document(userId).set(userData)
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        onError(e.message ?: "Error al guardar los datos del perfil.")
                    }
            }
            .addOnFailureListener { e ->
                onError(e.message ?: "Error al crear la cuenta (es posible que el correo ya esté en uso).")
            }
    }

    /**
     * Si la cuenta se creó pero el perfil no se pudo guardar, se elimina la
     * cuenta para no dejar un usuario a medias (que al reintentar el registro
     * daría "correo ya en uso").
     */
    private fun rollbackRegistration(user: FirebaseUser, onError: (String) -> Unit) {
        user.delete().addOnCompleteListener { deletion ->
            if (deletion.isSuccessful) {
                onError("No se pudo guardar tu perfil, así que la cuenta no se ha creado. Inténtalo de nuevo.")
            } else {
                auth.signOut()
                _isUserLoggedIn.value = false
                onError(
                    "No se pudo guardar tu perfil. Si al reintentar el registro te indica que el " +
                            "correo ya existe, inicia sesión con él."
                )
            }
        }
    }

    fun resetPassword(email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        // Firebase lanza IllegalArgumentException si el correo está vacío.
        if (email.isBlank()) {
            onError("Introduce tu correo electrónico.")
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { error ->
                // No revelamos si el correo existe o no: se responde igual que si se hubiera enviado.
                if ((error as? FirebaseAuthException)?.errorCode == "ERROR_USER_NOT_FOUND") {
                    onSuccess()
                } else {
                    onError(authErrorMessage(error, "Error al enviar el correo"))
                }
            }
    }

    fun logout() {
        auth.signOut()
        _isUserLoggedIn.value = false
    }
}