package com.inovisec.caloriestracker.domain.usecase.diary

import com.inovisec.caloriestracker.domain.model.DiaryEntry
import com.inovisec.caloriestracker.domain.repository.DiaryRepository
import javax.inject.Inject

class AddDiaryEntryUseCase @Inject constructor(private val repository: DiaryRepository) {
    suspend operator fun invoke(entry: DiaryEntry) = repository.addEntry(entry)
}