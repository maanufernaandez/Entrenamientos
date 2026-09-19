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
                _isUserLoggedIn.value = true
                onSuccess()
            }
            .addOnFailureListener { onError(authErrorMessage(it, "Error al iniciar sesión")) }
    }

    fun register(email: String, pass: String, name: String, lastName: String, club: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (!PasswordValidator.isValid(pass)) {
            onError(PasswordValidator.RULES_MESSAGE)
            return
        }

        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { result ->
                val user = result.user

                if (user == null) {
                    onError("No se ha podido completar el registro. Inténtalo de nuevo.")
                    return@addOnSuccessListener
                }

                // Guardar datos en Firestore directamente
                val userProfile = hashMapOf(
                    "name" to name,
                    "lastName" to lastName,
                    "club" to club,
                    "email" to email
                )

                db.collection("users").document(user.uid).set(userProfile)
                    .addOnSuccessListener {
                        _isUserLoggedIn.value = true
                        onSuccess()
                    }
                    .addOnFailureListener { rollbackRegistration(user, onError) }
            }
            .addOnFailureListener { onError(authErrorMessage(it, "Error en el registro")) }
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