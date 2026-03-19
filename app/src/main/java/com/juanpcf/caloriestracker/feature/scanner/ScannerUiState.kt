package com.juanpcf.caloriestracker.feature.scanner

import com.juanpcf.caloriestracker.domain.model.Food

sealed interface ScannerState {
    data object Scanning : ScannerState
    data object Loading : ScannerState
    data class Found(val food: Food) : ScannerState
    data class NotFound(val barcode: String) : ScannerState
    data class Error(val message: String) : ScannerState
}
