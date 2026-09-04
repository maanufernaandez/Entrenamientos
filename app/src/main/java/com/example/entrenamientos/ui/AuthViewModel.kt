package com.example.entrenamientos.ui

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Estado para saber si el usuario ya tiene sesión iniciada
    private val _isUserLoggedIn = MutableStateFlow(auth.currentUser != null)
    val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn

    // Validar contraseña: Min 8, Max 20, 1 mayúscula, 1 minúscula
    fun isPasswordValid(password: String): Boolean {
        return password.length in 8..20
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
            onError("La contraseña debe tener entre 8 y 20 caracteres.")
            return
        }

        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { result ->
                val user = result.user

                // Guardar datos en Firestore directamente
                val userProfile = hashMapOf(
                    "name" to name,
                    "lastName" to lastName,
                    "club" to club,
                    "email" to email
                )

                db.collection("users").document(user!!.uid).set(userProfile)
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