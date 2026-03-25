package com.juanpcf.caloriestracker.feature.diary.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.juanpcf.caloriestracker.R
import com.juanpcf.caloriestracker.core.navigation.DiaryEntryEdit
import com.juanpcf.caloriestracker.domain.usecase.diary.DeleteDiaryEntryUseCase
import com.juanpcf.caloriestracker.domain.usecase.diary.GetDiaryEntryUseCase
import com.juanpcf.caloriestracker.domain.usecase.diary.UpdateDiaryEntryUseCase
import com.juanpcf.caloriestracker.domain.model.MealType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiaryEntryEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getEntryUseCase: GetDiaryEntryUseCase,
    private val updateEntryUseCase: UpdateDiaryEntryUseCase,
    private val deleteEntryUseCase: DeleteDiaryEntryUseCase
) : ViewModel() {

    private val entryId: String = savedStateHandle.toRoute<DiaryEntryEdit>().entryId

    private val _uiState = MutableStateFlow<DiaryEntryEditUiState>(DiaryEntryEditUiState.Loading)
    val uiState: StateFlow<DiaryEntryEditUiState> = _uiState.asStateFlow()

    private val _uiEvents = Channel<DiaryEntryEditUiEvent>(Channel.BUFFERED)
    val uiEvents = _uiEvents.receiveAsFlow()

    init {
        fetchEntry()
    }

    private fun fetchEntry() {
        viewModelScope.launch {
            val entry = getEntryUseCase(entryId)
            if (entry == null) {
                _uiEvents.send(DiaryEntryEditUiEvent.ShowError(R.string.error_entry_not_found))
                _uiEvents.send(DiaryEntryEditUiEvent.NavigateBack)
            } else {
                _uiState.value = DiaryEntryEditUiState.Loaded(
                    entry = entry,
                    editedServings = formatServings(entry.servings),
                    editedMealType = entry.mealType,
                    editedCalories = formatSnapshot(entry.caloriesSnapshot),
                    editedProtein = formatSnapshot(entry.proteinSnapshot),
                    editedCarbs = formatSnapshot(entry.carbsSnapshot),
                    editedFat = formatSnapshot(entry.fatSnapshot)
                )
            }
        }
    }

    fun onServingsChange(value: String) {
        _uiState.update { state ->
            if (state !is DiaryEntryEditUiState.Loaded) return@update state
            val newServings = value.trim().replace(",", ".").toDoubleOrNull()
            if (newServings != null && newServings > 0.0 && state.entry.servings > 0.0) {
                val factor = newServings / state.entry.servings
                state.copy(
                    editedServings = value,
                    editedCalories = formatSnapshot(state.entry.caloriesSnapshot * factor),
                    editedProtein  = formatSnapshot(state.entry.proteinSnapshot  * factor),
                    editedCarbs    = formatSnapshot(state.entry.carbsSnapshot    * factor),
                    editedFat      = formatSnapshot(state.entry.fatSnapshot      * factor)
                )
            } else {
                state.copy(editedServings = value)
            }
        }
    }

    fun onCaloriesChange(value: String) {
        _uiState.update { if (it is DiaryEntryEditUiState.Loaded) it.copy(editedCalories = value) else it }
    }

    fun onProteinChange(value: String) {
        _uiState.update { if (it is DiaryEntryEditUiState.Loaded) it.copy(editedProtein = value) else it }
    }

    fun onCarbsChange(value: String) {
        _uiState.update { if (it is DiaryEntryEditUiState.Loaded) it.copy(editedCarbs = value) else it }
    }

    fun onFatChange(value: String) {
        _uiState.update { if (it is DiaryEntryEditUiState.Loaded) it.copy(editedFat = value) else it }
    }

    fun onMealTypeChange(mealType: MealType) {
        _uiState.update { if (it is DiaryEntryEditUiState.Loaded) it.copy(editedMealType = mealType) else it }
    }

    fun onShowDeleteDialog() {
        _uiState.update { if (it is DiaryEntryEditUiState.Loaded) it.copy(showDeleteDialog = true) else it }
    }

    fun onDismissDeleteDialog() {
        _uiState.update { if (it is DiaryEntryEditUiState.Loaded) it.copy(showDeleteDialog = false) else it }
    }

    fun onSave() {
        val state = _uiState.value as? DiaryEntryEditUiState.Loaded ?: return

        val servings = state.editedServings.trim().replace(",", ".").toDoubleOrNull()
        if (servings == null || servings <= 0) {
            viewModelScope.launch {
                _uiEvents.send(DiaryEntryEditUiEvent.ShowError(R.string.error_invalid_servings))
            }
            return
        }

        val calories = state.editedCalories.trim().replace(",", ".").replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.0
        val protein  = state.editedProtein.trim().replace(",", ".").replace(Regex("[^0-9.]"), "").toDoubleOrNull()  ?: 0.0
        val carbs    = state.editedCarbs.trim().replace(",", ".").replace(Regex("[^0-9.]"), "").toDoubleOrNull()    ?: 0.0
        val fat      = state.editedFat.trim().replace(",", ".").replace(Regex("[^0-9.]"), "").toDoubleOrNull()      ?: 0.0

        val updatedEntry = state.entry.copy(
            servings = servings,
            mealType = state.editedMealType,
            caloriesSnapshot = calories,
            proteinSnapshot  = protein,
            carbsSnapshot    = carbs,
            fatSnapshot      = fat
        )

        viewModelScope.launch {
            _uiState.update { if (it is DiaryEntryEditUiState.Loaded) it.copy(isSaving = true) else it }
            try {
                updateEntryUseCase(updatedEntry)
                _uiEvents.send(DiaryEntryEditUiEvent.NavigateBack)
            } catch (e: Exception) {
                _uiState.update { if (it is DiaryEntryEditUiState.Loaded) it.copy(isSaving = false) else it }
                _uiEvents.send(DiaryEntryEditUiEvent.ShowError(R.string.error_generic))
            }
        }
    }

    fun onDelete() {
        viewModelScope.launch {
            try {
                deleteEntryUseCase(entryId)
                _uiEvents.send(DiaryEntryEditUiEvent.NavigateBack)
            } catch (e: Exception) {
                _uiEvents.send(DiaryEntryEditUiEvent.ShowError(R.string.error_generic))
            }
        }
    }

    private fun formatServings(value: Double): String =
        if (value == value.toLong().toDouble()) value.toLong().toString()
        else value.toString()

    private fun formatSnapshot(value: Double): String =
        if (value == value.toLong().toDouble()) value.toLong().toString()
        else String.format(java.util.Locale.US, "%.1f", value)
}
