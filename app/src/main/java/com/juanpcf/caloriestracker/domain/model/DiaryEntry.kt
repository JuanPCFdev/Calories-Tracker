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
    val syncedAt: Instant? = null,
    // DB column 'created_at' already exists in DiaryEntryEntity v1. No migration required.
    // Existing entries upgraded from before this field was mapped will have createdAt = Instant.EPOCH (acceptable).
    val createdAt: Instant = Instant.now()
)