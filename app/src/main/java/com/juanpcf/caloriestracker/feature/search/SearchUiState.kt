package com.juanpcf.caloriestracker.feature.search

import com.juanpcf.caloriestracker.domain.model.Food
import java.time.LocalDate

data class SearchUiState(
    val query: String = "",
    val results: List<Food> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedDate: LocalDate = LocalDate.now()
)
