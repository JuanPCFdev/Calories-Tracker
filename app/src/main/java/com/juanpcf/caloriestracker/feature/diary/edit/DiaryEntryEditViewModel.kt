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
import com.juanpcf.caloriestracker.domain.util.formatNutrient
import com.juanpcf.caloriestracker.domain.util.formatServings
import com.juanpcf.caloriestracker.domain.util.macrosScaledTo
import com.juanpcf.caloriestracker.domain.util.toDecimalOrNull
import com.juanpcf.caloriestracker.domain.util.toNutrientOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
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
                    editedCalories = formatNutrient(entry.caloriesSnapshot),
                    editedProtein = formatNutrient(entry.proteinSnapshot),
                    editedCarbs = formatNutrient(entry.carbsSnapshot),
                    editedFat = formatNutrient(entry.fatSnapshot),
                    editedSugar = formatNutrient(entry.sugarSnapshot)
                )
            }
        }
    }

    fun onServingsChange(value: String) {
        _uiState.update { state ->
            if (state !is DiaryEntryEditUiState.Loaded) return@update state
            val newServings = value.toDecimalOrNull()
            if (newServings != null && newServings > 0.0 && state.entry.servings > 0.0) {
                val scaled = state.entry.macrosScaledTo(newServings)
                state.copy(
                    editedServings = value,
                    editedCalories = formatNutrient(scaled.calories),
                    editedProtein  = formatNutrient(scaled.protein),
                    editedCarbs    = formatNutrient(scaled.carbs),
                    editedFat      = formatNutrient(scaled.fat)
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

    fun onSugarChange(value: String) {
        _uiState.update { if (it is DiaryEntryEditUiState.Loaded) it.copy(editedSugar = value) else it }
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

        val servings = state.editedServings.toDecimalOrNull()
        if (servings == null || servings <= 0) {
            viewModelScope.launch {
                _uiEvents.send(DiaryEntryEditUiEvent.ShowError(R.string.error_invalid_servings))
            }
            return
        }

        val calories = state.editedCalories.toNutrientOrNull() ?: 0.0
        val protein  = state.editedProtein.toNutrientOrNull()  ?: 0.0
        val carbs    = state.editedCarbs.toNutrientOrNull()    ?: 0.0
        val fat      = state.editedFat.toNutrientOrNull()      ?: 0.0
        val sugar    = state.editedSugar.toNutrientOrNull()    ?: 0.0

        val updatedEntry = state.entry.copy(
            servings = servings,
            mealType = state.editedMealType,
            caloriesSnapshot = calories,
            proteinSnapshot  = protein,
            carbsSnapshot    = carbs,
            fatSnapshot      = fat,
            sugarSnapshot    = sugar
        )

        viewModelScope.launch {
            _uiState.update { if (it is DiaryEntryEditUiState.Loaded) it.copy(isSaving = true) else it }
            try {
                updateEntryUseCase(updatedEntry)
                _uiEvents.send(DiaryEntryEditUiEvent.NavigateBack)
            } catch (e: Exception) {
                Timber.e(e, "No se pudo actualizar la entrada $entryId")
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
                Timber.e(e, "No se pudo borrar la entrada $entryId")
                _uiEvents.send(DiaryEntryEditUiEvent.ShowError(R.string.error_generic))
            }
        }
    }
}
