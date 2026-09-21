package com.example.entrenamientos.logic

/**
 * Catálogo de categorías de los equipos y utilidades para mostrarlas.
 *
 * La categoría de un equipo se guarda como texto ("Infantil 1ª", "Benjamin 3x3"):
 * una categoría base y, en algunas, una división. Antes este catálogo y el código
 * para descomponer ese texto estaban repetidos en Ajustes, Asistencia y Notas.
 * Las reglas de juego de cada categoría están en [CategoryRules].
 */
object CategoryCatalog {

    const val DEFAULT_DIVISION = "1ª"

    val DIVISIONS: List<String> = listOf("1ª", "2ª")

    /** Categorías base, en el orden del desplegable al crear un equipo. */
    val OPTIONS: List<String> = listOf(
        "Pre-Benjamin 3x3", "Benjamin 3x3", "Benjamin 5x5",
        "PreMinibasket", "Minibasket", "Preinfantil",
        "Infantil", "Cadete", "Junior", "Senior"
    )

    /** Orden en el que se listan los equipos: de la categoría mayor a la menor. */
    val DISPLAY_ORDER: List<String> = listOf(
        "Senior", "Junior", "Cadete", "Infantil", "Preinfantil",
        "Minibasket", "PreMinibasket", "Benjamin 5x5", "Benjamin 3x3", "Pre-Benjamin 3x3"
    )

    private val CATEGORIES_WITH_DIVISION: Set<String> = setOf(
        "PreMinibasket", "Minibasket", "Preinfantil", "Infantil", "Cadete", "Junior", "Senior"
    )

    /** Categoría base y división (null si esa categoría no la tiene). */
    data class ParsedCategory(val base: String, val division: String?)

    fun requiresDivision(baseCategory: String): Boolean =
        baseCategory in CATEGORIES_WITH_DIVISION

    /** "Infantil 1ª" -> ("Infantil", "1ª"); "Benjamin 3x3" -> ("Benjamin 3x3", null). */
    fun parse(categoryYear: String): ParsedCategory {
        val parts = categoryYear.split(" ")
        val last = parts.last()

        return if (parts.size >= 2 && last in DIVISIONS) {
            ParsedCategory(base = parts.dropLast(1).joinToString(" "), division = last)
        } else {
            ParsedCategory(base = categoryYear, division = null)
        }
    }

    /** Texto que se guarda en el equipo: añade la división solo si la categoría la lleva. */
    fun compose(baseCategory: String, division: String): String =
        if (requiresDivision(baseCategory)) "$baseCategory $division" else baseCategory

    /**
     * Categoría y división que muestra el formulario de equipo: las del equipo que
     * se edita, o las de por defecto si no hay (o no se reconocen).
     */
    fun initialSelection(savedCategoryYear: String?): Pair<String, String> {
        if (savedCategoryYear.isNullOrBlank()) {
            return OPTIONS.first() to DEFAULT_DIVISION
        }

        val parsed = parse(savedCategoryYear)
        val base = if (parsed.base in OPTIONS) parsed.base else OPTIONS.first()

        return base to (parsed.division ?: DEFAULT_DIVISION)
    }

    /** Posición para ordenar equipos; las categorías desconocidas van al final. */
    fun displayOrder(categoryYear: String): Int {
        val index = DISPLAY_ORDER.indexOfFirst { categoryYear.startsWith(it) }

        return if (index == -1) 99 else index
    }

    fun genderLabel(gender: String?): String =
        when (gender) {
            "M" -> "Masculino"
            "F" -> "Femenino"
            else -> "Mixto"
        }

    /** "Infantil Masculino 1ª", "Benjamin 3x3 Femenino", o solo el género si no hay categoría. */
    fun displayName(categoryYear: String?, gender: String?): String {
        val genderText = genderLabel(gender)

        if (categoryYear.isNullOrBlank()) return genderText

        val parsed = parse(categoryYear)

        return if (parsed.division != null) {
            "${parsed.base} $genderText ${parsed.division}"
        } else {
            "$categoryYear $genderText"
        }
    }
}