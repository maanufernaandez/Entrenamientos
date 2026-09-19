package com.example.entrenamientos.logic

enum class CategoryType { SENIOR, INFANTIL, MINI, THREE_BY_THREE, OTHER }

/** Resultado de comprobar cuántos jugadores se quieren convocar. */
enum class RosterCheck {
    OK,

    /** Se supera el máximo de la categoría. */
    TOO_MANY,

    /** No se llega ni al mínimo absoluto: no se puede guardar. */
    BELOW_MINIMUM,

    /** Se puede guardar, pero no se cumple la normativa: hay que confirmarlo. */
    NEEDS_CONFIRMATION
}

/**
 * Reglas que dependen de la categoría del equipo (texto del tipo
 * "Infantil 1ª", "Minibasket", "Benjamin 3x3"...). Antes estaban repetidas con
 * `startsWith(...)` en varias pantallas.
 */
data class CategoryRules(
    val type: CategoryType,
    val totalQuarters: Int,
    val playersPerQuarter: Int,
    /** Mínimo de jugadores que marca la normativa. */
    val minPlayers: Int,
    /** Mínimo por debajo del cual no se permite guardar la convocatoria. */
    val absoluteMinPlayers: Int,
    val maxPlayers: Int
) {

    val isSenior: Boolean get() = type == CategoryType.SENIOR
    val isInfantil: Boolean get() = type == CategoryType.INFANTIL
    val isMini: Boolean get() = type == CategoryType.MINI
    val is3x3: Boolean get() = type == CategoryType.THREE_BY_THREE

    fun checkRosterSize(size: Int): RosterCheck =
        when {
            size > maxPlayers -> RosterCheck.TOO_MANY
            size < absoluteMinPlayers -> RosterCheck.BELOW_MINIMUM
            size < minPlayers -> RosterCheck.NEEDS_CONFIRMATION
            else -> RosterCheck.OK
        }

    companion object {

        fun fromCategory(category: String): CategoryRules {

            val type = when {
                category.startsWithAny("Cadete", "Junior", "Senior") ->
                    CategoryType.SENIOR

                category.startsWithAny("Infantil", "Preinfantil") ->
                    CategoryType.INFANTIL

                category.startsWithAny("Minibasket", "PreMinibasket", "Benjamin 5x5") ->
                    CategoryType.MINI

                category.startsWithAny("Benjamin 3x3", "Pre-Benjamin 3x3") ->
                    CategoryType.THREE_BY_THREE

                else -> CategoryType.OTHER
            }

            val minPlayers = when (type) {
                CategoryType.THREE_BY_THREE -> 4
                CategoryType.SENIOR -> 5
                else -> 8
            }

            return CategoryRules(
                type = type,
                totalQuarters = when (type) {
                    CategoryType.SENIOR -> 1
                    CategoryType.INFANTIL -> 4
                    CategoryType.MINI -> 6
                    CategoryType.THREE_BY_THREE -> 8
                    CategoryType.OTHER -> 4
                },
                playersPerQuarter = if (type == CategoryType.THREE_BY_THREE) 3 else 5,
                minPlayers = minPlayers,
                absoluteMinPlayers = if (type == CategoryType.INFANTIL) 5 else minPlayers,
                maxPlayers = if (type == CategoryType.MINI) 15 else 12
            )
        }

        private fun String.startsWithAny(vararg prefixes: String): Boolean =
            prefixes.any { startsWith(it) }
    }
}