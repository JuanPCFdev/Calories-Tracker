package com.juanpcf.caloriestracker.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NutritionNumberTest {

    // ---- toDecimalOrNull (porciones: acepta coma, sin limpiar otros chars) ----

    @Test
    fun `toDecimalOrNull acepta punto`() {
        assertEquals(1.5, "1.5".toDecimalOrNull()!!, 0.0001)
    }

    @Test
    fun `toDecimalOrNull acepta coma como separador decimal`() {
        assertEquals(2.25, "2,25".toDecimalOrNull()!!, 0.0001)
    }

    @Test
    fun `toDecimalOrNull recorta espacios`() {
        assertEquals(3.0, "  3 ".toDecimalOrNull()!!, 0.0001)
    }

    @Test
    fun `toDecimalOrNull devuelve null con texto basura`() {
        assertNull("abc".toDecimalOrNull())
        assertNull("1.5g".toDecimalOrNull()) // NO limpia unidades: para porciones se espera número limpio
    }

    // ---- toNutrientOrNull (texto libre: acepta coma y descarta no-numéricos) ----

    @Test
    fun `toNutrientOrNull limpia unidades y espacios`() {
        assertEquals(150.0, "150 kcal".toNutrientOrNull()!!, 0.0001)
        assertEquals(12.5, "12,5 g".toNutrientOrNull()!!, 0.0001)
    }

    @Test
    fun `toNutrientOrNull devuelve null si no hay dígitos`() {
        assertNull("kcal".toNutrientOrNull())
        assertNull("".toNutrientOrNull())
    }

    // ---- formatNutrient ----

    @Test
    fun `formatNutrient muestra enteros sin decimales`() {
        assertEquals("150", formatNutrient(150.0))
    }

    @Test
    fun `formatNutrient usa un decimal con punto`() {
        assertEquals("12.5", formatNutrient(12.5))
    }

    @Test
    fun `formatNutrient redondea a un decimal`() {
        assertEquals("12.3", formatNutrient(12.34))
    }

    // ---- formatServings ----

    @Test
    fun `formatServings muestra enteros sin decimales`() {
        assertEquals("2", formatServings(2.0))
    }

    @Test
    fun `formatServings preserva fracciones`() {
        assertEquals("1.5", formatServings(1.5))
    }
}
