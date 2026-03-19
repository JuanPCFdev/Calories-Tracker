package com.juanpcf.caloriestracker.domain.usecase.diary

import com.juanpcf.caloriestracker.domain.model.DiaryEntry
import com.juanpcf.caloriestracker.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GetDiaryForDateUseCase @Inject constructor(private val repository: DiaryRepository) {
    operator fun invoke(userId: String, date: LocalDate): Flow<List<DiaryEntry>> =
        repository.getEntriesForDate(userId, date)
}