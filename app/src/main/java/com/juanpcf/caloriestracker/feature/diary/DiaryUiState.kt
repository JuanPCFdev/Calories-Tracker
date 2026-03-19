package com.juanpcf.caloriestracker.feature.diary

import com.juanpcf.caloriestracker.domain.model.DiaryEntry
import com.juanpcf.caloriestracker.domain.model.MacroTotals
import com.juanpcf.caloriestracker.domain.model.UserGoals
import java.time.LocalDate

data class DiaryUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val entries: List<DiaryEntry> = emptyList(),
    val totals: MacroTotals = MacroTotals(),
    val goals: UserGoals? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
