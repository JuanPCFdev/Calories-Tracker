package com.juanpcf.caloriestracker.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EstimateConfidenceTest {

    @Test
    fun `mapea valores validos sin importar mayusculas ni espacios`() {
        assertEquals(EstimateConfidence.HIGH, EstimateConfidence.fromApi("high"))
        assertEquals(EstimateConfidence.HIGH, EstimateConfidence.fromApi("HIGH"))
        assertEquals(EstimateConfidence.MEDIUM, EstimateConfidence.fromApi(" Medium "))
        assertEquals(EstimateConfidence.LOW, EstimateConfidence.fromApi("low"))
    }

    @Test
    fun `valores invalidos o nulos devuelven null`() {
        assertNull(EstimateConfidence.fromApi(null))
        assertNull(EstimateConfidence.fromApi(""))
        assertNull(EstimateConfidence.fromApi("muy alta"))
    }
}
