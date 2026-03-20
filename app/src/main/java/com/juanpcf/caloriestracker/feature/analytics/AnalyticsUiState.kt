package com.juanpcf.caloriestracker.feature.analytics

import com.juanpcf.caloriestracker.domain.model.DayMacros
import com.juanpcf.caloriestracker.domain.model.UserGoals

data class AnalyticsUiState(
    val weeklyData: List<DayMacros> = emptyList(),
    val goals: UserGoals? = null,
    val isLoading: Boolean = false,
    val selectedRange: Int = 7
)
