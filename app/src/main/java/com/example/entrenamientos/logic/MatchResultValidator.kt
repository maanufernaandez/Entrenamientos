package com.example.entrenamientos.logic

/**
 * Validación de los datos del resultado de un partido.
 * Todas las funciones devuelven el mensaje de error, o null si los datos son correctos.
 */
object MatchResultValidator {

    const val MSG_INCOMPLETE_SCORE = "Introduce el marcador de los dos equipos"
    const val MSG_TIE = "El resultado no puede ser empate"
    const val MSG_FREE_THROWS = "Los tiros libres convertidos no pueden superar los intentados"

    /**
     * Marcador: o se rellenan los dos campos, o ninguno (permite guardar solo
     * observaciones o tiros libres sin marcador). No se admiten empates.
     */
    fun validateScore(resLocal: String, resVisitor: String): String? {
        val local = resLocal.toIntOrNull()
        val visitor = resVisitor.toIntOrNull()

        if ((local == null) != (visitor == null)) return MSG_INCOMPLETE_SCORE
        if (local != null && local == visitor) return MSG_TIE

        return null
    }

    /** Los campos vacíos cuentan como 0. */
    fun validateFreeThrows(ftMade: String, ftAttempted: String): String? {
        val made = ftMade.toIntOrNull() ?: 0
        val attempted = ftAttempted.toIntOrNull() ?: 0

        return if (made > attempted) MSG_FREE_THROWS else null
    }

    fun validate(
        resLocal: String,
        resVisitor: String,
        ftMade: String,
        ftAttempted: String
    ): String? =
        validateScore(resLocal, resVisitor) ?: validateFreeThrows(ftMade, ftAttempted)
}