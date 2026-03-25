package com.juanpcf.caloriestracker.feature.diary.edit

import com.juanpcf.caloriestracker.domain.model.DiaryEntry
import com.juanpcf.caloriestracker.domain.model.MealType

sealed interface DiaryEntryEditUiState {
    data object Loading : DiaryEntryEditUiState

    data class Loaded(
        val entry: DiaryEntry,
        val editedServings: String,
        val editedMealType: MealType,
        val editedCalories: String,
        val editedProtein: String,
        val editedCarbs: String,
        val editedFat: String,
        val isSaving: Boolean = false,
        val showDeleteDialog: Boolean = false
    ) : DiaryEntryEditUiState

    data class Error(val message: String) : DiaryEntryEditUiState
}
