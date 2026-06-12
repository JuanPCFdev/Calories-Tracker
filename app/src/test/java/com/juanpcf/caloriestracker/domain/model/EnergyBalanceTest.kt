package com.juanpcf.caloriestracker.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class EnergyBalanceTest {

    @Test
    fun `neto positivo es superavit`() {
        val balance = EnergyBalance(consumed = 2500.0, burned = 2000.0)
        assertEquals(500.0, balance.net, 0.0001)
    }

    @Test
    fun `neto negativo es deficit`() {
        val balance = EnergyBalance(consumed = 1800.0, burned = 2200.0)
        assertEquals(-400.0, balance.net, 0.0001)
    }

    @Test
    fun `por defecto el gasto es estimado`() {
        assertEquals(true, EnergyBalance(consumed = 0.0, burned = 0.0).burnedIsEstimated)
    }
}
