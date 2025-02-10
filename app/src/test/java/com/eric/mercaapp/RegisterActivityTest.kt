package com.eric.mercaapp

import org.junit.Assert.*
import org.junit.Test
import java.util.regex.Pattern

class RegisterActivityTest {

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
        )
        return emailPattern.matcher(email).matches()
    }

    private fun validateInputs(
        name: String, email: String, password: String, population: String, province: String, role: String
    ): Boolean {
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || population.isEmpty() || province.isEmpty() || role.isEmpty()) {
            return false
        }
        if (!isValidEmail(email)) {
            return false
        }
        if (password.length < 6) {
            return false
        }
        return true
    }

    @Test
    fun `validar email incorrecto devuelve false`() {
        val result = validateInputs("Juan", "correo_invalido", "123456", "Madrid", "Madrid", "Cliente")
        assertFalse(result)
    }

    @Test
    fun `validar campos vacíos devuelve false`() {
        val result = validateInputs("", "test@gmail.com", "123456", "Madrid", "Madrid", "Cliente")
        assertFalse(result)
    }

    @Test
    fun `validar contraseña menor de 6 caracteres devuelve false`() {
        val result = validateInputs("Juan", "test@gmail.com", "123", "Madrid", "Madrid", "Cliente")
        assertFalse(result)
    }

    @Test
    fun `validar sin seleccionar rol devuelve false`() {
        val result = validateInputs("Juan", "test@gmail.com", "123456", "Madrid", "Madrid", "")
        assertFalse(result)
    }

    @Test
    fun `validar datos correctos devuelve true`() {
        val result = validateInputs("Juan", "test@gmail.com", "123456", "Madrid", "Madrid", "Cliente")
        assertTrue(result)
    }
}
