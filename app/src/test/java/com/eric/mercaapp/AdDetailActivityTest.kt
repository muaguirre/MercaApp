package com.eric.mercaapp

import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.TimeUnit

class AdDetailActivityTest {

    private fun formatTimeRemaining(timeMillis: Long): String {
        if (timeMillis <= 0) return "Anuncio expirado"

        val hours = TimeUnit.MILLISECONDS.toHours(timeMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeMillis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timeMillis) % 60

        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun isValidBid(currentBid: Double, newBid: Double): Boolean {
        return newBid > 0 && newBid < currentBid
    }

    @Test
    fun `formato de tiempo restante correcto`() {
        val result = formatTimeRemaining(3_600_000) // 1 hora en milisegundos
        assertEquals("01:00:00", result)
    }

    @Test
    fun `cuando tiempo restante es cero devuelve anuncio expirado`() {
        val result = formatTimeRemaining(0)
        assertEquals("Anuncio expirado", result)
    }

    @Test
    fun `cuando tiempo restante es negativo devuelve anuncio expirado`() {
        val result = formatTimeRemaining(-5000)
        assertEquals("Anuncio expirado", result)
    }

    @Test
    fun `una oferta menor que la actual es válida`() {
        val result = isValidBid(currentBid = 100.0, newBid = 50.0)
        assertTrue(result)
    }

    @Test
    fun `una oferta igual o mayor a la actual es inválida`() {
        val result = isValidBid(currentBid = 100.0, newBid = 100.0)
        assertFalse(result)
    }

    @Test
    fun `una oferta negativa o cero es inválida`() {
        val result = isValidBid(currentBid = 100.0, newBid = -10.0)
        assertFalse(result)
    }
}
