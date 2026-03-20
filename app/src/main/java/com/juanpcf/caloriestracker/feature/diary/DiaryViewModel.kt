package com.juanpcf.caloriestracker.feature.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpcf.caloriestracker.core.util.NetworkMonitor
import com.juanpcf.caloriestracker.domain.model.DiaryEntry
import com.juanpcf.caloriestracker.domain.repository.AuthRepository
import com.juanpcf.caloriestracker.domain.usecase.diary.AddDiaryEntryUseCase
import com.juanpcf.caloriestracker.domain.usecase.diary.DeleteDiaryEntryUseCase
import com.juanpcf.caloriestracker.domain.usecase.diary.GetDailyTotalsUseCase
import com.juanpcf.caloriestracker.domain.usecase.diary.GetDiaryForDateUseCase
import com.juanpcf.caloriestracker.domain.usecase.goals.GetUserGoalsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val getDiaryForDate: GetDiaryForDateUseCase,
    private val getDailyTotals: GetDailyTotalsUseCase,
    private val addDiaryEntry: AddDiaryEntryUseCase,
    private val deleteDiaryEntry: DeleteDiaryEntryUseCase,
    private val getUserGoals: GetUserGoalsUseCase,
    private val authRepository: AuthRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<DiaryUiState> = _selectedDate.flatMapLatest { date ->
        val userId = authRepository.currentUser?.uid ?: ""
        combine(
            getDiaryForDate(userId, date),
            getDailyTotals(userId, date),
            getUserGoals(userId),
            networkMonitor.isOnline
        ) { entries, totals, goals, isOnline ->
            DiaryUiState(
                selectedDate = date,
                entries = entries,
                totals = totals,
                goals = goals,
                isLoading = false,
                isOffline = !isOnline
            )
        }
    }.catch { e ->
        emit(DiaryUiState(error = e.message))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DiaryUiState(isLoading = true)
    )

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun previousDay() {
        _selectedDate.value = _selectedDate.value.minusDays(1)
    }

    fun nextDay() {
        _selectedDate.value = _selectedDate.value.plusDays(1)
    }

    fun deleteEntry(entry: DiaryEntry) {
        viewModelScope.launch {
            try {
                deleteDiaryEntry(entry.id)
            } catch (e: Exception) {
                // Error handled via uiState in a future iteration
            }
        }
    }
}
