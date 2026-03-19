package com.inovisec.caloriestracker.domain.repository

import com.inovisec.caloriestracker.domain.model.AppPreferences
import com.inovisec.caloriestracker.domain.model.Language
import com.inovisec.caloriestracker.domain.model.Theme
import kotlinx.coroutines.flow.Flow

interface AppPreferencesRepository {
    val preferences: Flow<AppPreferences>
    suspend fun saveTheme(theme: Theme)
    suspend fun saveLanguage(language: Language)
}