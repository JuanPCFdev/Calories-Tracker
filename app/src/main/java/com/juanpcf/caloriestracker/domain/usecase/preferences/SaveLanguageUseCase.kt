package com.juanpcf.caloriestracker.domain.usecase.preferences

import com.juanpcf.caloriestracker.domain.model.Language
import com.juanpcf.caloriestracker.domain.repository.AppPreferencesRepository
import javax.inject.Inject

class SaveLanguageUseCase @Inject constructor(private val repository: AppPreferencesRepository) {
    suspend operator fun invoke(language: Language) = repository.saveLanguage(language)
}