package com.example.entrenamientos.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MatchResultValidatorTest {

    @Test
    fun `un marcador completo y sin empate es valido`() {
        assertNull(MatchResultValidator.validateScore("54", "48"))
    }

    @Test
    fun `sin marcador es valido para guardar solo observaciones`() {
        assertNull(MatchResultValidator.validateScore("", ""))
    }

    @Test
    fun `marcador con un solo equipo no es valido`() {
        assertEquals(MatchResultValidator.MSG_INCOMPLETE_SCORE, MatchResultValidator.validateScore("54", ""))
        assertEquals(MatchResultValidator.MSG_INCOMPLETE_SCORE, MatchResultValidator.validateScore("", "48"))
    }

    @Test
    fun `un empate no es valido`() {
        assertEquals(MatchResultValidator.MSG_TIE, MatchResultValidator.validateScore("50", "50"))
    }

    @Test
    fun `una derrota por cero es valida`() {
        assertNull(MatchResultValidator.validateScore("0", "20"))
    }

    @Test
    fun `tiros libres convertidos menores o iguales a intentados es valido`() {
        assertNull(MatchResultValidator.validateFreeThrows("7", "10"))
        assertNull(MatchResultValidator.validateFreeThrows("10", "10"))
    }

    @Test
    fun `tiros libres convertidos mayores que intentados no es valido`() {
        assertEquals(MatchResultValidator.MSG_FREE_THROWS, MatchResultValidator.validateFreeThrows("15", "10"))
    }

    @Test
    fun `convertidos sin intentados no es valido`() {
        assertEquals(MatchResultValidator.MSG_FREE_THROWS, MatchResultValidator.validateFreeThrows("3", ""))
    }

    @Test
    fun `tiros libres vacios es valido`() {
        assertNull(MatchResultValidator.validateFreeThrows("", ""))
    }

    @Test
    fun `validate devuelve primero el error del marcador`() {
        assertEquals(MatchResultValidator.MSG_TIE, MatchResultValidator.validate("50", "50", "9", "3"))
    }

    @Test
    fun `validate devuelve el error de tiros libres si el marcador es correcto`() {
        assertEquals(MatchResultValidator.MSG_FREE_THROWS, MatchResultValidator.validate("50", "40", "9", "3"))
    }
}