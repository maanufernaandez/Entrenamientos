package com.example.entrenamientos.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LineupRulesTest {

    private val infantil = CategoryRules.fromCategory("Infantil 1ª")
    private val mini = CategoryRules.fromCategory("Minibasket 1ª")

    private fun ids(n: Int): List<Long> = (1L..n.toLong()).toList()

    private fun banned(
        rules: CategoryRules,
        playerId: Long,
        summoned: List<Long>,
        lineups: List<List<Long>>,
        quarter: Int,
        selection: Set<Long> = emptySet()
    ) = LineupRules.isBanned(rules, playerId, summoned, lineups, quarter, selection)

    // ---------------- Infantil ----------------

    private val infantilLineups = listOf(
        listOf(1L, 2L, 3L, 4L, 5L),
        listOf(1L, 2L, 3L, 6L, 7L)
    )

    @Test
    fun `infantil - en el tercer cuarto son obligatorios quienes no han jugado`() {
        val forced = LineupRules.forcedPlayers(infantil, ids(8), infantilLineups, 3)

        assertEquals(setOf(8L), forced)
    }

    @Test
    fun `infantil - en el tercer cuarto no puede jugar quien jugo el primero y el segundo`() {
        val summoned = ids(8)

        assertTrue(banned(infantil, 1, summoned, infantilLineups, 3))
        assertTrue(banned(infantil, 3, summoned, infantilLineups, 3))
        assertFalse(banned(infantil, 4, summoned, infantilLineups, 3))
        assertFalse(banned(infantil, 6, summoned, infantilLineups, 3))
        assertFalse(banned(infantil, 8, summoned, infantilLineups, 3))
    }

    @Test
    fun `infantil - con menos de 8 convocados no se aplican las reglas`() {
        val summoned = ids(7)

        assertTrue(LineupRules.forcedPlayers(infantil, summoned, infantilLineups, 3).isEmpty())
        assertFalse(banned(infantil, 1, summoned, infantilLineups, 3))
    }

    @Test
    fun `infantil - fuera del tercer cuarto no hay obligados ni bloqueados`() {
        val summoned = ids(8)

        assertTrue(LineupRules.forcedPlayers(infantil, summoned, infantilLineups.take(1), 2).isEmpty())
        assertFalse(banned(infantil, 1, summoned, infantilLineups.take(1), 2))
    }

    // ---------------- Minibasket ----------------

    @Test
    fun `mini - con 10 jugadores nadie puede jugar mas de 3 de los 5 primeros cuartos`() {
        val lineups = listOf(
            listOf(1L, 2L, 3L, 4L, 5L),
            listOf(1L, 2L, 3L, 4L, 5L),
            listOf(1L, 6L, 7L, 8L, 9L)
        )

        assertTrue(banned(mini, 1, ids(10), lineups, 4))
        assertFalse(banned(mini, 2, ids(10), lineups, 4))
    }

    @Test
    fun `mini - con 8 jugadores se bloquea a quien ya jugo 4 cuartos`() {
        val lineups = listOf(
            listOf(1L, 2L, 3L, 4L, 5L),
            listOf(1L, 2L, 3L, 6L, 7L),
            listOf(1L, 4L, 5L, 6L, 7L),
            listOf(1L, 2L, 3L, 4L, 8L)
        )

        assertTrue(banned(mini, 1, ids(8), lineups, 5))
    }

    @Test
    fun `mini - con 8 jugadores quien lleva 3 se bloquea solo si otro ya llega a 4`() {
        val lineups = listOf(
            listOf(1L, 2L, 3L, 4L, 5L),
            listOf(1L, 2L, 3L, 6L, 7L),
            listOf(1L, 2L, 3L, 4L, 8L)
        )

        // Nadie más tiene 4 cuartos: el jugador 1 (3 cuartos) sigue disponible.
        assertFalse(banned(mini, 1, ids(8), lineups, 4))

        // Si el jugador 2 ya está elegido en este cuarto, llegaría a 4: se bloquea al 1.
        assertTrue(banned(mini, 1, ids(8), lineups, 4, selection = setOf(2L)))
    }

    @Test
    fun `mini - en el quinto cuarto son obligatorios quienes no han jugado`() {
        val lineups = listOf(
            listOf(1L, 2L, 3L, 4L, 5L),
            listOf(1L, 2L, 3L, 4L, 5L),
            listOf(6L, 7L, 8L, 1L, 2L),
            listOf(6L, 7L, 8L, 3L, 4L)
        )

        assertEquals(setOf(9L, 10L), LineupRules.forcedPlayers(mini, ids(10), lineups, 5))
    }

    @Test
    fun `mini - en el sexto cuarto son obligatorios quienes han jugado menos de 2`() {
        val lineups = listOf(
            listOf(1L, 2L, 3L, 4L, 5L),
            listOf(1L, 2L, 3L, 4L, 6L),
            listOf(1L, 2L, 3L, 5L, 7L),
            listOf(1L, 2L, 4L, 5L, 8L),
            listOf(1L, 3L, 4L, 5L, 9L)
        )

        assertEquals(
            setOf(6L, 7L, 8L, 9L, 10L),
            LineupRules.forcedPlayers(mini, ids(10), lineups, 6)
        )
    }

    @Test
    fun `mini - en el sexto cuarto con 8 jugadores se bloquea a quien jugo 4 de los 5 primeros`() {
        val lineups = listOf(
            listOf(1L, 2L, 3L, 4L, 5L),
            listOf(1L, 2L, 3L, 6L, 7L),
            listOf(1L, 4L, 5L, 6L, 7L),
            listOf(1L, 2L, 3L, 4L, 8L),
            listOf(5L, 6L, 7L, 8L, 2L)
        )

        assertTrue(banned(mini, 1, ids(8), lineups, 6))
        assertFalse(banned(mini, 3, ids(8), lineups, 6))
    }

    @Test
    fun `mini - en el sexto cuarto con 13 a 15 jugadores se bloquea a quien ya tiene 3 si otro no llega`() {
        val lineups = listOf(
            listOf(1L, 2L, 3L, 4L, 5L),
            listOf(1L, 2L, 3L, 4L, 5L),
            listOf(1L, 2L, 3L, 6L, 7L),
            listOf(8L, 9L, 10L, 11L, 12L),
            listOf(13L, 6L, 7L, 8L, 9L)
        )

        assertTrue(banned(mini, 1, ids(13), lineups, 6))
        assertFalse(banned(mini, 10, ids(13), lineups, 6))
    }

    @Test
    fun `mini - en el sexto cuarto con 10 jugadores no se bloquea a nadie`() {
        val lineups = listOf(
            listOf(1L, 2L, 3L, 4L, 5L),
            listOf(1L, 2L, 3L, 4L, 5L),
            listOf(1L, 2L, 3L, 4L, 5L),
            listOf(6L, 7L, 8L, 9L, 10L),
            listOf(6L, 7L, 8L, 9L, 10L)
        )

        assertFalse(banned(mini, 1, ids(10), lineups, 6))
    }

    // ---------------- Resto de categorías ----------------

    @Test
    fun `3x3 y senior no tienen jugadores obligatorios ni bloqueados`() {
        val trio = CategoryRules.fromCategory("Benjamin 3x3")
        val senior = CategoryRules.fromCategory("Senior")
        val lineups = listOf(listOf(1L, 2L, 3L), listOf(1L, 2L, 3L), listOf(1L, 2L, 3L))

        assertTrue(LineupRules.forcedPlayers(trio, ids(8), lineups, 4).isEmpty())
        assertFalse(banned(trio, 1, ids(8), lineups, 4))
        assertTrue(LineupRules.forcedPlayers(senior, ids(8), lineups, 1).isEmpty())
        assertFalse(banned(senior, 1, ids(8), lineups, 1))
    }
}