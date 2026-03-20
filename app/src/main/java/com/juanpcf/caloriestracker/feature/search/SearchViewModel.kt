package com.juanpcf.caloriestracker.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpcf.caloriestracker.core.util.NetworkMonitor
import com.juanpcf.caloriestracker.domain.model.DiaryEntry
import com.juanpcf.caloriestracker.domain.model.Food
import com.juanpcf.caloriestracker.domain.model.MealType
import com.juanpcf.caloriestracker.domain.repository.AuthRepository
import com.juanpcf.caloriestracker.domain.usecase.diary.AddDiaryEntryUseCase
import com.juanpcf.caloriestracker.domain.usecase.food.SearchFoodsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchFoodsUseCase: SearchFoodsUseCase,
    private val addDiaryEntryUseCase: AddDiaryEntryUseCase,
    private val authRepository: AuthRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                _uiState.update { it.copy(isOffline = !isOnline) }
            }
        }
    }

    fun setDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            search(query)
        }
    }

    fun search(query: String) {
        if (query.isBlank()) {
            _uiState.update { it.copy(results = emptyList(), isLoading = false, error = null) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            searchFoodsUseCase(query).collect { result ->
                result.fold(
                    onSuccess = { foods ->
                        _uiState.update { it.copy(results = foods, isLoading = false, error = null) }
                    },
                    onFailure = { throwable ->
                        _uiState.update { it.copy(isLoading = false, error = throwable.message) }
                    }
                )
            }
        }
    }

    fun addToLog(food: Food, mealType: MealType, servings: Double) {
        val userId = authRepository.currentUser?.uid ?: return
        val date = _uiState.value.selectedDate
        viewModelScope.launch {
            val entry = DiaryEntry(
                id = UUID.randomUUID().toString(),
                userId = userId,
                food = food,
                date = date,
                mealType = mealType,
                servings = servings,
                caloriesSnapshot = food.calories * servings,
                proteinSnapshot = food.protein * servings,
                carbsSnapshot = food.carbs * servings,
                fatSnapshot = food.fat * servings,
                createdAt = java.time.Instant.now()
            )
            addDiaryEntryUseCase(entry)
        }
    }

    fun toggleSelection(foodId: String) {
        _uiState.update { state ->
            val current = state.selectedItems
            state.copy(
                selectedItems = if (foodId in current) current - foodId else current + foodId
            )
        }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedItems = emptySet()) }
    }

    fun saveSelectedToLog(mealType: MealType) {
        val userId = authRepository.currentUser?.uid ?: return
        val state = _uiState.value
        val date = state.selectedDate
        viewModelScope.launch {
            state.selectedItems
                .mapNotNull { id -> state.results.find { it.id == id } }
                .forEach { food ->
                    val entry = DiaryEntry(
                        id = UUID.randomUUID().toString(),
                        userId = userId,
                        food = food,
                        date = date,
                        mealType = mealType,
                        servings = 1.0,
                        caloriesSnapshot = food.calories,
                        proteinSnapshot = food.protein,
                        carbsSnapshot = food.carbs,
                        fatSnapshot = food.fat,
                        createdAt = java.time.Instant.now()
                    )
                    addDiaryEntryUseCase(entry)
                }
            _uiState.update { it.copy(selectedItems = emptySet()) }
        }
    }
}
