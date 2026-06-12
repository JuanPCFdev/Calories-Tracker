package com.juanpcf.caloriestracker.domain.util

import com.juanpcf.caloriestracker.domain.model.DiaryEntry
import com.juanpcf.caloriestracker.domain.model.Food
import com.juanpcf.caloriestracker.domain.model.FoodSource
import com.juanpcf.caloriestracker.domain.model.MealType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class DiaryEntryMacrosTest {

    private fun entry(servings: Double) = DiaryEntry(
        id = "e1",
        userId = "u1",
        food = Food(
            id = "f1", name = "Pollo", calories = 100.0, protein = 20.0,
            carbs = 0.0, fat = 5.0, servingSize = 100.0, servingUnit = "g", source = FoodSource.USDA
        ),
        date = LocalDate.of(2026, 1, 1),
        mealType = MealType.LUNCH,
        servings = servings,
        caloriesSnapshot = 100.0 * servings,
        proteinSnapshot = 20.0 * servings,
        carbsSnapshot = 0.0,
        fatSnapshot = 5.0 * servings,
        sugarSnapshot = 8.0 * servings
    )

    @Test
    fun `escala proporcionalmente al duplicar porciones`() {
        val scaled = entry(servings = 1.0).macrosScaledTo(2.0)
        assertEquals(200.0, scaled.calories, 0.0001)
        assertEquals(40.0, scaled.protein, 0.0001)
        assertEquals(0.0, scaled.carbs, 0.0001)
        assertEquals(10.0, scaled.fat, 0.0001)
        assertEquals(16.0, scaled.sugar, 0.0001)
    }

    @Test
    fun `escala hacia abajo al reducir porciones`() {
        val scaled = entry(servings = 2.0).macrosScaledTo(1.0)
        assertEquals(100.0, scaled.calories, 0.0001)
        assertEquals(20.0, scaled.protein, 0.0001)
    }

    @Test
    fun `porciones nuevas invalidas devuelven los snapshots actuales sin escalar`() {
        val base = entry(servings = 2.0)
        val scaled = base.macrosScaledTo(0.0)
        assertEquals(base.caloriesSnapshot, scaled.calories, 0.0001)
        assertEquals(base.proteinSnapshot, scaled.protein, 0.0001)
    }

    @Test
    fun `porciones originales en cero no dividen por cero`() {
        val base = entry(servings = 0.0)
        val scaled = base.macrosScaledTo(3.0)
        assertEquals(base.caloriesSnapshot, scaled.calories, 0.0001)
    }
}
