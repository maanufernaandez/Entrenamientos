package com.example.entrenamientos.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryCatalogTest {

    // ---------------- Coherencia del catálogo ----------------

    @Test
    fun `el orden de listado contiene las mismas categorias que el desplegable`() {
        assertEquals(CategoryCatalog.OPTIONS.toSet(), CategoryCatalog.DISPLAY_ORDER.toSet())
        assertEquals(CategoryCatalog.OPTIONS.size, CategoryCatalog.DISPLAY_ORDER.size)
    }

    @Test
    fun `todas las categorias del catalogo tienen reglas de juego`() {
        CategoryCatalog.OPTIONS.forEach { base ->
            val rules = CategoryRules.fromCategory(CategoryCatalog.compose(base, "1ª"))

            assertNotEquals("Sin reglas para $base", CategoryType.OTHER, rules.type)
        }
    }

    // ---------------- Divisiones ----------------

    @Test
    fun `solo algunas categorias llevan division`() {
        listOf("PreMinibasket", "Minibasket", "Preinfantil", "Infantil", "Cadete", "Junior", "Senior")
            .forEach { assertTrue(it, CategoryCatalog.requiresDivision(it)) }

        listOf("Pre-Benjamin 3x3", "Benjamin 3x3", "Benjamin 5x5")
            .forEach { assertFalse(it, CategoryCatalog.requiresDivision(it)) }
    }

    @Test
    fun `parse separa la categoria y la division`() {
        val infantil = CategoryCatalog.parse("Infantil 1ª")
        assertEquals("Infantil", infantil.base)
        assertEquals("1ª", infantil.division)

        val benjamin = CategoryCatalog.parse("Benjamin 3x3")
        assertEquals("Benjamin 3x3", benjamin.base)
        assertNull(benjamin.division)
    }

    @Test
    fun `parse de un texto sin espacios o vacio no falla`() {
        assertNull(CategoryCatalog.parse("Senior").division)
        assertNull(CategoryCatalog.parse("1ª").division)
        assertEquals("", CategoryCatalog.parse("").base)
    }

    @Test
    fun `compose anade la division solo si la categoria la lleva`() {
        assertEquals("Cadete 2ª", CategoryCatalog.compose("Cadete", "2ª"))
        assertEquals("Benjamin 5x5", CategoryCatalog.compose("Benjamin 5x5", "2ª"))
    }

    @Test
    fun `parse y compose son inversos`() {
        CategoryCatalog.OPTIONS.forEach { base ->
            CategoryCatalog.DIVISIONS.forEach { division ->
                val saved = CategoryCatalog.compose(base, division)
                val parsed = CategoryCatalog.parse(saved)

                assertEquals(base, parsed.base)
                assertEquals(if (CategoryCatalog.requiresDivision(base)) division else null, parsed.division)
            }
        }
    }

    // ---------------- Selección inicial del formulario ----------------

    @Test
    fun `sin equipo se propone la primera categoria y la division 1`() {
        assertEquals("Pre-Benjamin 3x3" to "1ª", CategoryCatalog.initialSelection(null))
        assertEquals("Pre-Benjamin 3x3" to "1ª", CategoryCatalog.initialSelection(""))
        assertEquals("Pre-Benjamin 3x3" to "1ª", CategoryCatalog.initialSelection("   "))
    }

    @Test
    fun `al editar se recuperan la categoria y la division guardadas`() {
        assertEquals("Cadete" to "2ª", CategoryCatalog.initialSelection("Cadete 2ª"))
        assertEquals("Benjamin 3x3" to "1ª", CategoryCatalog.initialSelection("Benjamin 3x3"))
    }

    @Test
    fun `una categoria desconocida vuelve a la primera y conserva la division`() {
        assertEquals("Pre-Benjamin 3x3" to "2ª", CategoryCatalog.initialSelection("Otra 2ª"))
        assertEquals("Pre-Benjamin 3x3" to "1ª", CategoryCatalog.initialSelection("Otra"))
    }

    // ---------------- Orden de listado ----------------

    @Test
    fun `los equipos se ordenan de la categoria mayor a la menor`() {
        val order = listOf(
            "Senior 1ª", "Junior 1ª", "Cadete 2ª", "Infantil 1ª", "Preinfantil 1ª",
            "Minibasket 1ª", "PreMinibasket 1ª", "Benjamin 5x5", "Benjamin 3x3", "Pre-Benjamin 3x3"
        ).map { CategoryCatalog.displayOrder(it) }

        assertEquals(order.sorted(), order)
        assertEquals(order.size, order.toSet().size)
    }

    @Test
    fun `infantil y preinfantil no se confunden y lo desconocido va al final`() {
        assertEquals(3, CategoryCatalog.displayOrder("Infantil 1ª"))
        assertEquals(4, CategoryCatalog.displayOrder("Preinfantil 1ª"))
        assertEquals(99, CategoryCatalog.displayOrder(""))
        assertEquals(99, CategoryCatalog.displayOrder("Veteranos"))
    }

    // ---------------- Textos ----------------

    @Test
    fun `etiqueta de genero`() {
        assertEquals("Masculino", CategoryCatalog.genderLabel("M"))
        assertEquals("Femenino", CategoryCatalog.genderLabel("F"))
        assertEquals("Mixto", CategoryCatalog.genderLabel("X"))
        assertEquals("Mixto", CategoryCatalog.genderLabel(null))
    }

    @Test
    fun `nombre visible con categoria genero y division`() {
        assertEquals("Infantil Masculino 1ª", CategoryCatalog.displayName("Infantil 1ª", "M"))
        assertEquals("Benjamin 3x3 Femenino", CategoryCatalog.displayName("Benjamin 3x3", "F"))
    }

    @Test
    fun `nombre visible sin categoria muestra solo el genero`() {
        assertEquals("Masculino", CategoryCatalog.displayName("", "M"))
        assertEquals("Masculino", CategoryCatalog.displayName("  ", "M"))
        assertEquals("Mixto", CategoryCatalog.displayName(null, null))
    }
}