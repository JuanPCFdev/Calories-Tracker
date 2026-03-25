package com.juanpcf.caloriestracker.feature.camera_ai

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.juanpcf.caloriestracker.R
import com.juanpcf.caloriestracker.core.navigation.AiResult
import com.juanpcf.caloriestracker.domain.model.DiaryEntry
import com.juanpcf.caloriestracker.domain.model.Food
import com.juanpcf.caloriestracker.domain.model.FoodSource
import com.juanpcf.caloriestracker.domain.model.MealType
import com.juanpcf.caloriestracker.domain.repository.AuthRepository
import com.juanpcf.caloriestracker.domain.usecase.diary.AddDiaryEntryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

data class AiResultUiState(
    val foodName: String = "",
    val calories: String = "",
    val protein: String = "",
    val carbs: String = "",
    val fat: String = "",
    val servingSize: String = "",
    val servingUnit: String = "g",
    val isEditable: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface AiResultUiEvent {
    data object NavigateToDiary : AiResultUiEvent
    data class ShowError(@StringRes val resId: Int) : AiResultUiEvent
}

@HiltViewModel
class AiResultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val addDiaryEntryUseCase: AddDiaryEntryUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val args: AiResult = savedStateHandle.toRoute<AiResult>()

    private val _uiState = MutableStateFlow(
        AiResultUiState(
            foodName = args.foodName,
            calories = if (args.calories == 0.0 && args.isUnrecognized) "" else formatNutrient(args.calories),
            protein = if (args.protein == 0.0 && args.isUnrecognized) "" else formatNutrient(args.protein),
            carbs = if (args.carbs == 0.0 && args.isUnrecognized) "" else formatNutrient(args.carbs),
            fat = if (args.fat == 0.0 && args.isUnrecognized) "" else formatNutrient(args.fat),
            servingSize = if (args.servingSize == 0.0 && args.isUnrecognized) "" else args.servingSize.toString(),
            servingUnit = args.servingUnit,
            isEditable = true
        )
    )
    val uiState: StateFlow<AiResultUiState> = _uiState.asStateFlow()

    val isUnrecognized: Boolean = args.isUnrecognized

    private val _uiEvents = Channel<AiResultUiEvent>(Channel.BUFFERED)
    val uiEvents = _uiEvents.receiveAsFlow()

    fun onFoodNameChange(value: String) = _uiState.update { it.copy(foodName = value) }
    fun onCaloriesChange(value: String) = _uiState.update { it.copy(calories = value) }
    fun onProteinChange(value: String) = _uiState.update { it.copy(protein = value) }
    fun onCarbsChange(value: String) = _uiState.update { it.copy(carbs = value) }
    fun onFatChange(value: String) = _uiState.update { it.copy(fat = value) }
    fun onServingSizeChange(value: String) = _uiState.update { it.copy(servingSize = value) }
    fun onServingUnitChange(value: String) = _uiState.update { it.copy(servingUnit = value) }

    fun addToDiary(mealType: MealType, servings: Double) {
        val state = _uiState.value
        val userId = authRepository.currentUser?.uid

        if (userId == null) {
            viewModelScope.launch {
                _uiEvents.send(AiResultUiEvent.ShowError(R.string.error_generic))
            }
            return
        }

        val name = state.foodName.trim()
        val calories = state.calories.toCleanDouble()
        val protein = state.protein.toCleanDouble()
        val carbs = state.carbs.toCleanDouble()
        val fat = state.fat.toCleanDouble()
        val servingSize = state.servingSize.toCleanDouble()

        if (name.isBlank() || calories == null || protein == null || carbs == null || fat == null || servingSize == null) {
            viewModelScope.launch {
                _uiEvents.send(AiResultUiEvent.ShowError(R.string.error_fill_required_fields))
            }
            return
        }

        val food = Food(
            id = args.foodId,
            name = name,
            calories = calories,
            protein = protein,
            carbs = carbs,
            fat = fat,
            servingSize = servingSize,
            servingUnit = state.servingUnit,
            source = FoodSource.AI
        )

        val entry = DiaryEntry(
            id = UUID.randomUUID().toString(),
            userId = userId,
            food = food,
            date = LocalDate.now(),
            mealType = mealType,
            servings = servings,
            caloriesSnapshot = food.calories * servings,
            proteinSnapshot = food.protein * servings,
            carbsSnapshot = food.carbs * servings,
            fatSnapshot = food.fat * servings
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                addDiaryEntryUseCase(entry)
                _uiState.update { it.copy(isLoading = false) }
                _uiEvents.send(AiResultUiEvent.NavigateToDiary)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
                _uiEvents.send(AiResultUiEvent.ShowError(R.string.error_generic))
            }
        }
    }

    private fun formatNutrient(value: Double): String =
        if (value == value.toLong().toDouble()) value.toLong().toString()
        else String.format(Locale.US, "%.1f", value)
}

private fun String.toCleanDouble(): Double? =
    this.trim()
        .replace(",", ".")           // handle locale decimal commas
        .replace(Regex("[^0-9.]"), "") // strip remaining non-numeric chars
        .toDoubleOrNull()
