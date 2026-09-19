package com.example.entrenamientos.logic

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordValidatorTest {

    @Test
    fun `una contrasena correcta es valida`() {
        assertTrue(PasswordValidator.isValid("Abcdefg1"))
    }

    @Test
    fun `menos de 8 caracteres no es valida`() {
        assertFalse(PasswordValidator.isValid("Abcdef1"))
    }

    @Test
    fun `mas de 20 caracteres no es valida`() {
        assertFalse(PasswordValidator.isValid("Abcdefghijklmnopqrstu"))
    }

    @Test
    fun `los limites de longitud 8 y 20 son validos`() {
        assertTrue(PasswordValidator.isValid("Abcdefgh"))
        assertTrue(PasswordValidator.isValid("Abcdefghijklmnopqrst"))
    }

    @Test
    fun `sin mayuscula no es valida`() {
        assertFalse(PasswordValidator.isValid("abcdefg1"))
    }

    @Test
    fun `sin minuscula no es valida`() {
        assertFalse(PasswordValidator.isValid("ABCDEFG1"))
    }

    @Test
    fun `cadena vacia no es valida`() {
        assertFalse(PasswordValidator.isValid(""))
    }
}