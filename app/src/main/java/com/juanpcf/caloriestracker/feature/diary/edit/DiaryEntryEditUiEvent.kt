package com.juanpcf.caloriestracker.feature.diary.edit

import androidx.annotation.StringRes

sealed interface DiaryEntryEditUiEvent {
    data object NavigateBack : DiaryEntryEditUiEvent
    data class ShowError(@StringRes val resId: Int) : DiaryEntryEditUiEvent
}
