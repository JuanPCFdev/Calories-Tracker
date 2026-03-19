package com.inovisec.caloriestracker.domain.usecase.diary

import com.inovisec.caloriestracker.domain.model.MacroTotals
import com.inovisec.caloriestracker.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GetDailyTotalsUseCase @Inject constructor(private val repository: DiaryRepository) {
    operator fun invoke(userId: String, date: LocalDate): Flow<MacroTotals> =
        repository.getDailyTotals(userId, date)
}