package com.example.entrenamientos.logic

/**
 * Reglas de contraseña compartidas por el registro y el cambio de contraseña,
 * para que ambas pantallas exijan exactamente lo mismo.
 */
object PasswordValidator {

    const val MIN_LENGTH = 8
    const val MAX_LENGTH = 20

    const val RULES_MESSAGE =
        "La contraseña debe tener entre 8 y 20 caracteres, con al menos una mayúscula y una minúscula."

    fun isValid(password: String): Boolean {
        if (password.length !in MIN_LENGTH..MAX_LENGTH) return false
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        return hasUpperCase && hasLowerCase
    }
}