package com.juanpcf.caloriestracker.domain.usecase.analytics

import com.juanpcf.caloriestracker.domain.model.DayMacros
import com.juanpcf.caloriestracker.domain.repository.DiaryRepository
import java.time.LocalDate
import javax.inject.Inject

class GetWeeklyCalorieTrendsUseCase @Inject constructor(private val repository: DiaryRepository) {
    suspend operator fun invoke(userId: String): List<DayMacros> {
        val today = LocalDate.now()
        val fromDate = today.minusDays(6)
        val entries = repository.getEntriesForDateRange(userId, fromDate, today)
        val byDate = entries.groupBy { it.date }
        return (0..6).map { offset ->
            val date = fromDate.plusDays(offset.toLong())
            val dayEntries = byDate[date] ?: emptyList()
            DayMacros(
                date = date,
                calories = dayEntries.sumOf { it.caloriesSnapshot },
                protein = dayEntries.sumOf { it.proteinSnapshot },
                carbs = dayEntries.sumOf { it.carbsSnapshot },
                fat = dayEntries.sumOf { it.fatSnapshot }
            )
        }
    }
}