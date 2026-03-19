package com.inovisec.caloriestracker.domain.usecase.preferences

import com.inovisec.caloriestracker.domain.model.Theme
import com.inovisec.caloriestracker.domain.repository.AppPreferencesRepository
import javax.inject.Inject

class SaveThemeUseCase @Inject constructor(private val repository: AppPreferencesRepository) {
    suspend operator fun invoke(theme: Theme) = repository.saveTheme(theme)
}