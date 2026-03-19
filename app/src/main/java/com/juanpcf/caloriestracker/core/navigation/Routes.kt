package com.juanpcf.caloriestracker.core.navigation

import kotlinx.serialization.Serializable

@Serializable object AuthGraph
@Serializable object Login
@Serializable object Register

@Serializable object MainGraph
@Serializable object Home
@Serializable object Search
@Serializable object Scanner
@Serializable object CameraAi
@Serializable object AiResult
@Serializable object Analytics
@Serializable object Settings
@Serializable object Goals

@Serializable
data class FoodDetail(val foodId: String, val selectedDate: String)
