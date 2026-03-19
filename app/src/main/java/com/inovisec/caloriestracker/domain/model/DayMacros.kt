package com.inovisec.caloriestracker.domain.model

import java.time.LocalDate

data class DayMacros(
    val date: LocalDate,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double
)