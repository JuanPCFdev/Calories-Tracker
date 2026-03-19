package com.juanpcf.caloriestracker.domain.usecase.preferences

import com.juanpcf.caloriestracker.domain.model.AppPreferences
import com.juanpcf.caloriestracker.domain.repository.AppPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAppPreferencesUseCase @Inject constructor(private val repository: AppPreferencesRepository) {
    operator fun invoke(): Flow<AppPreferences> = repository.preferences
}