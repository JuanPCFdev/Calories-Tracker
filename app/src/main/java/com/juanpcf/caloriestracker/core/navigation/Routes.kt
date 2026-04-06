package com.juanpcf.caloriestracker.core.navigation

import kotlinx.serialization.Serializable

@Serializable object AuthGraph
@Serializable object Login
@Serializable object Register

@Serializable object MainGraph
@Serializable object Home
@Serializable object AddFood

@Serializable
data class AiResult(
    val foodId: String,
    val foodName: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val servingSize: Double,
    val servingUnit: String,
    val isUnrecognized: Boolean = false
)

@Serializable object Analytics
@Serializable object Settings
@Serializable object Goals

@Serializable
data class DiaryEntryEdit(val entryId: String)
