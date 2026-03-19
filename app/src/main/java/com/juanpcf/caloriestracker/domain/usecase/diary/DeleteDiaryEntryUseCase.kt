package com.juanpcf.caloriestracker.domain.usecase.diary

import com.juanpcf.caloriestracker.domain.repository.DiaryRepository
import javax.inject.Inject

class DeleteDiaryEntryUseCase @Inject constructor(private val repository: DiaryRepository) {
    suspend operator fun invoke(entryId: String) = repository.deleteEntry(entryId)
}