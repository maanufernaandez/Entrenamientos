package com.example.entrenamientos.ui

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
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

    // Validar contraseña: entre 8 y 20 caracteres, con al menos 1 mayúscula
    // y 1 minúscula.
    private fun isPasswordValid(password: String): Boolean {
        if (password.length !in 8..20) return false
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        return hasUpperCase && hasLowerCase
    }

    fun login(email: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                _isUserLoggedIn.value = true
                onSuccess()
            }
            .addOnFailureListener { onError(it.message ?: "Error al iniciar sesión") }
    }

    fun register(email: String, pass: String, name: String, lastName: String, club: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (!isPasswordValid(pass)) {
            onError("La contraseña debe tener entre 8 y 20 caracteres, con al menos una mayúscula y una minúscula.")
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
                    .addOnFailureListener { onError("Error al guardar el perfil: ${it.message}") }
            }
            .addOnFailureListener { onError(it.message ?: "Error en el registro") }
    }

    fun resetPassword(email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it.message ?: "Error al enviar el correo") }
    }

    fun logout() {
        auth.signOut()
        _isUserLoggedIn.value = false
    }
}