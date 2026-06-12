package com.juanpcf.caloriestracker.domain.util

import com.juanpcf.caloriestracker.domain.model.DiaryEntry
import com.juanpcf.caloriestracker.domain.model.MacroTotals

/**
 * Recalcula los macros snapshot de una entrada para una nueva cantidad de porciones, proporcional a
 * las porciones originales. Si las porciones (la vieja o la nueva) no son válidas, devuelve los
 * snapshots actuales sin escalar.
 */
fun DiaryEntry.macrosScaledTo(newServings: Double): MacroTotals {
    val current = MacroTotals(caloriesSnapshot, proteinSnapshot, carbsSnapshot, fatSnapshot)
    if (servings <= 0.0 || newServings <= 0.0) return current
    val factor = newServings / servings
    return MacroTotals(
        calories = caloriesSnapshot * factor,
        protein = proteinSnapshot * factor,
        carbs = carbsSnapshot * factor,
        fat = fatSnapshot * factor
    )
}
