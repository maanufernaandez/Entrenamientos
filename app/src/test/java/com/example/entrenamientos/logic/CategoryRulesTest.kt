package com.example.entrenamientos.logic

import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryRulesTest {

    private fun rules(category: String) = CategoryRules.fromCategory(category)

    @Test
    fun `cada categoria se clasifica en su tipo`() {
        assertEquals(CategoryType.SENIOR, rules("Cadete 1ª").type)
        assertEquals(CategoryType.SENIOR, rules("Junior 2ª").type)
        assertEquals(CategoryType.SENIOR, rules("Senior").type)
        assertEquals(CategoryType.INFANTIL, rules("Infantil 1ª").type)
        assertEquals(CategoryType.INFANTIL, rules("Preinfantil 2ª").type)
        assertEquals(CategoryType.MINI, rules("Minibasket 1ª").type)
        assertEquals(CategoryType.MINI, rules("PreMinibasket 2ª").type)
        assertEquals(CategoryType.MINI, rules("Benjamin 5x5").type)
        assertEquals(CategoryType.THREE_BY_THREE, rules("Benjamin 3x3").type)
        assertEquals(CategoryType.THREE_BY_THREE, rules("Pre-Benjamin 3x3").type)
    }

    @Test
    fun `una categoria vacia o desconocida usa los valores por defecto`() {
        val r = rules("")

        assertEquals(CategoryType.OTHER, r.type)
        assertEquals(4, r.totalQuarters)
        assertEquals(5, r.playersPerQuarter)
        assertEquals(8, r.minPlayers)
        assertEquals(8, r.absoluteMinPlayers)
        assertEquals(12, r.maxPlayers)
    }

    @Test
    fun `numero de cuartos por categoria`() {
        assertEquals(1, rules("Senior").totalQuarters)
        assertEquals(4, rules("Infantil").totalQuarters)
        assertEquals(6, rules("Minibasket").totalQuarters)
        assertEquals(8, rules("Benjamin 3x3").totalQuarters)
    }

    @Test
    fun `solo el 3x3 juega con tres jugadores por cuarto`() {
        assertEquals(3, rules("Benjamin 3x3").playersPerQuarter)
        assertEquals(5, rules("Minibasket").playersPerQuarter)
        assertEquals(5, rules("Senior").playersPerQuarter)
    }

    @Test
    fun `minimos y maximos de convocatoria`() {
        assertEquals(4, rules("Benjamin 3x3").minPlayers)
        assertEquals(5, rules("Cadete").minPlayers)
        assertEquals(8, rules("Minibasket").minPlayers)
        assertEquals(15, rules("Minibasket").maxPlayers)
        assertEquals(12, rules("Infantil").maxPlayers)
        assertEquals(8, rules("Infantil").minPlayers)
        assertEquals(5, rules("Infantil").absoluteMinPlayers)
    }

    @Test
    fun `infantil - de 5 a 7 jugadores hay que confirmar`() {
        val r = rules("Infantil 1ª")

        assertEquals(RosterCheck.BELOW_MINIMUM, r.checkRosterSize(4))
        assertEquals(RosterCheck.NEEDS_CONFIRMATION, r.checkRosterSize(5))
        assertEquals(RosterCheck.NEEDS_CONFIRMATION, r.checkRosterSize(7))
        assertEquals(RosterCheck.OK, r.checkRosterSize(8))
        assertEquals(RosterCheck.OK, r.checkRosterSize(12))
        assertEquals(RosterCheck.TOO_MANY, r.checkRosterSize(13))
    }

    @Test
    fun `minibasket admite hasta 15`() {
        val r = rules("Minibasket")

        assertEquals(RosterCheck.BELOW_MINIMUM, r.checkRosterSize(7))
        assertEquals(RosterCheck.OK, r.checkRosterSize(15))
        assertEquals(RosterCheck.TOO_MANY, r.checkRosterSize(16))
    }

    @Test
    fun `senior y 3x3 no tienen zona de confirmacion`() {
        assertEquals(RosterCheck.BELOW_MINIMUM, rules("Senior").checkRosterSize(4))
        assertEquals(RosterCheck.OK, rules("Senior").checkRosterSize(5))
        assertEquals(RosterCheck.BELOW_MINIMUM, rules("Benjamin 3x3").checkRosterSize(3))
        assertEquals(RosterCheck.OK, rules("Benjamin 3x3").checkRosterSize(4))
    }
}