package com.juanpcf.caloriestracker.domain.usecase.preferences

import com.juanpcf.caloriestracker.domain.model.Theme
import com.juanpcf.caloriestracker.domain.repository.AppPreferencesRepository
import javax.inject.Inject

class SaveThemeUseCase @Inject constructor(private val repository: AppPreferencesRepository) {
    suspend operator fun invoke(theme: Theme) = repository.saveTheme(theme)
}