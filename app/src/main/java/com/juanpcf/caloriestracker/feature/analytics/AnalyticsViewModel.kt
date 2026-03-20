package com.juanpcf.caloriestracker.feature.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpcf.caloriestracker.domain.repository.AuthRepository
import com.juanpcf.caloriestracker.domain.usecase.analytics.GetWeeklyCalorieTrendsUseCase
import com.juanpcf.caloriestracker.domain.usecase.goals.GetUserGoalsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val getWeeklyCalorieTrends: GetWeeklyCalorieTrendsUseCase,
    private val getUserGoals: GetUserGoalsUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val userId: String
        get() = authRepository.currentUser?.uid ?: ""

    private val _selectedRange = MutableStateFlow(7)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<AnalyticsUiState> = _selectedRange
        .flatMapLatest { days ->
            combine(
                flow { emit(getWeeklyCalorieTrends(userId, days)) },
                getUserGoals(userId)
            ) { weeklyData, goals ->
                AnalyticsUiState(
                    weeklyData = weeklyData,
                    goals = goals,
                    isLoading = false,
                    selectedRange = days
                )
            }
        }
        .catch { emit(AnalyticsUiState(isLoading = false)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AnalyticsUiState(isLoading = true)
        )

    fun selectRange(days: Int) {
        _selectedRange.value = days
    }
}
