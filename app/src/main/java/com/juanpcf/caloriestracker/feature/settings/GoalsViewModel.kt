package com.juanpcf.caloriestracker.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpcf.caloriestracker.domain.model.UserGoals
import com.juanpcf.caloriestracker.domain.repository.AuthRepository
import com.juanpcf.caloriestracker.domain.usecase.goals.GetUserGoalsUseCase
import com.juanpcf.caloriestracker.domain.usecase.goals.SaveUserGoalsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GoalsUiState(
    val calories: String = "",
    val protein: String = "",
    val carbs: String = "",
    val fat: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed interface GoalsUiEvent {
    data object NavigateBack : GoalsUiEvent
    data class ShowError(val message: String) : GoalsUiEvent
}

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val getUserGoals: GetUserGoalsUseCase,
    private val saveUserGoals: SaveUserGoalsUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _events = Channel<GoalsUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val _uiState = MutableStateFlow(GoalsUiState(isLoading = true))
    val uiState: StateFlow<GoalsUiState> = _uiState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GoalsUiState(isLoading = true)
    )

    init {
        loadGoals()
    }

    private fun loadGoals() {
        viewModelScope.launch {
            val userId = authRepository.currentUser?.uid ?: return@launch
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                val goals = getUserGoals(userId).first()
                _uiState.update {
                    it.copy(
                        calories = goals?.dailyCalories?.toString() ?: "",
                        protein = goals?.dailyProtein?.toString() ?: "",
                        carbs = goals?.dailyCarbs?.toString() ?: "",
                        fat = goals?.dailyFat?.toString() ?: "",
                        isLoading = false,
                        error = null
                    )
                }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onCaloriesChange(value: String) {
        if (value.all { it.isDigit() }) {
            _uiState.update { it.copy(calories = value, error = null) }
        }
    }

    fun onProteinChange(value: String) {
        if (value.all { it.isDigit() }) {
            _uiState.update { it.copy(protein = value, error = null) }
        }
    }

    fun onCarbsChange(value: String) {
        if (value.all { it.isDigit() }) {
            _uiState.update { it.copy(carbs = value, error = null) }
        }
    }

    fun onFatChange(value: String) {
        if (value.all { it.isDigit() }) {
            _uiState.update { it.copy(fat = value, error = null) }
        }
    }

    fun saveGoals() {
        viewModelScope.launch {
            val userId = authRepository.currentUser?.uid ?: return@launch
            val state = _uiState.value

            val calories = state.calories.toIntOrNull() ?: 0
            val protein = state.protein.toIntOrNull() ?: 0
            val carbs = state.carbs.toIntOrNull() ?: 0
            val fat = state.fat.toIntOrNull() ?: 0

            _uiState.update { it.copy(isLoading = true, error = null) }

            runCatching {
                saveUserGoals(
                    UserGoals(
                        userId = userId,
                        dailyCalories = calories,
                        dailyProtein = protein,
                        dailyCarbs = carbs,
                        dailyFat = fat
                    )
                )
            }.onSuccess {
                _uiState.update { it.copy(isLoading = false) }
                _events.send(GoalsUiEvent.NavigateBack)
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message) }
                _events.send(GoalsUiEvent.ShowError(e.message ?: ""))
            }
        }
    }
}
