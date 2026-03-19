package com.juanpcf.caloriestracker.domain.model

import java.time.Instant
import java.time.LocalDate

data class DiaryEntry(
    val id: String,
    val userId: String,
    val food: Food,
    val date: LocalDate,
    val mealType: MealType,
    val servings: Double,
    val caloriesSnapshot: Double,
    val proteinSnapshot: Double,
    val carbsSnapshot: Double,
    val fatSnapshot: Double,
    val syncedAt: Instant? = null
)