package com.inovisec.caloriestracker.domain.usecase.preferences

import com.inovisec.caloriestracker.domain.model.Language
import com.inovisec.caloriestracker.domain.repository.AppPreferencesRepository
import javax.inject.Inject

class SaveLanguageUseCase @Inject constructor(private val repository: AppPreferencesRepository) {
    suspend operator fun invoke(language: Language) = repository.saveLanguage(language)
}