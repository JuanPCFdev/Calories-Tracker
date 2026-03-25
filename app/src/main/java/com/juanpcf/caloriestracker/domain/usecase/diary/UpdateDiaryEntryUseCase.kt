package com.juanpcf.caloriestracker.domain.usecase.diary

import com.juanpcf.caloriestracker.domain.model.DiaryEntry
import com.juanpcf.caloriestracker.domain.repository.DiaryRepository
import javax.inject.Inject

class UpdateDiaryEntryUseCase @Inject constructor(private val repository: DiaryRepository) {
    suspend operator fun invoke(entry: DiaryEntry) = repository.updateEntry(entry)
}
