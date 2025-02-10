package com.eric.mercaapp

import org.junit.Assert.*
import org.junit.Test

class PostAdActivityTest {

    private fun isValidPrice(price: Double?): Boolean {
        return price != null && price > 0
    }

    private fun isValidAdInput(
        title: String, description: String, category: String, provinceOrigin: String,
        townOrigin: String, provinceDestination: String, townDestination: String, price: Double?
    ): Boolean {
        if (title.isEmpty() || description.isEmpty() || category.isEmpty() || provinceOrigin.isEmpty()
            || townOrigin.isEmpty() || provinceDestination.isEmpty() || townDestination.isEmpty()
        ) {
            return false
        }
        return isValidPrice(price)
    }

    private fun calculateEndTime(durationHours: Int, startTime: Long): Long {
        return startTime + (durationHours * 60 * 60 * 1000)
    }

    @Test
    fun `precio válido devuelve true`() {
        assertTrue(isValidPrice(50.0))
    }

    @Test
    fun `precio nulo o negativo devuelve false`() {
        assertFalse(isValidPrice(null))
        assertFalse(isValidPrice(-10.0))
        assertFalse(isValidPrice(0.0))
    }

    @Test
    fun `datos de anuncio válidos devuelve true`() {
        val result = isValidAdInput(
            "Título", "Descripción", "Categoría", "Madrid",
            "Centro", "Barcelona", "Eixample", 100.0
        )
        assertTrue(result)
    }

    @Test
    fun `datos de anuncio con campo vacío devuelve false`() {
        val result = isValidAdInput(
            "", "Descripción", "Categoría", "Madrid",
            "Centro", "Barcelona", "Eixample", 100.0
        )
        assertFalse(result)
    }

    @Test
    fun `cálculo de tiempo de expiración correcto`() {
        val startTime = System.currentTimeMillis()
        val result = calculateEndTime(24, startTime)
        assertEquals(startTime + (24 * 60 * 60 * 1000), result)
    }
}
