package com.juanpcf.caloriestracker.feature.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpcf.caloriestracker.domain.model.DiaryEntry
import com.juanpcf.caloriestracker.domain.model.MealType
import com.juanpcf.caloriestracker.domain.repository.AuthRepository
import com.juanpcf.caloriestracker.domain.usecase.diary.AddDiaryEntryUseCase
import com.juanpcf.caloriestracker.domain.usecase.food.GetFoodByBarcodeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val getFoodByBarcode: GetFoodByBarcodeUseCase,
    private val addDiaryEntry: AddDiaryEntryUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<ScannerState>(ScannerState.Scanning)
    val state: StateFlow<ScannerState> = _state.asStateFlow()

    private val _addSuccess = MutableStateFlow(false)
    val addSuccess: StateFlow<Boolean> = _addSuccess.asStateFlow()

    private var lastScannedBarcode: String? = null

    fun onBarcodeDetected(barcode: String) {
        val current = _state.value
        if (barcode == lastScannedBarcode || current is ScannerState.Loading) return

        lastScannedBarcode = barcode
        _state.value = ScannerState.Loading

        viewModelScope.launch {
            getFoodByBarcode(barcode)
                .onSuccess { food ->
                    _state.value = ScannerState.Found(food)
                }
                .onFailure { throwable ->
                    val msg = throwable.message ?: ""
                    if (msg.contains("not found", ignoreCase = true) ||
                        msg.contains("404", ignoreCase = true)
                    ) {
                        _state.value = ScannerState.NotFound(barcode)
                    } else {
                        _state.value = ScannerState.Error(msg)
                    }
                }
        }
    }

    fun reset() {
        lastScannedBarcode = null
        _state.value = ScannerState.Scanning
        _addSuccess.value = false
    }

    fun addToLog(mealType: MealType, servings: Double) {
        val current = _state.value
        if (current !is ScannerState.Found) return

        val userId = authRepository.currentUser?.uid ?: return
        val food = current.food

        viewModelScope.launch {
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
            try {
                addDiaryEntry(entry)
                _addSuccess.value = true
            } catch (e: Exception) {
                _state.value = ScannerState.Error(e.message ?: "Failed to add entry")
            }
        }
    }
}
