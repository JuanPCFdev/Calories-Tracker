package com.juanpcf.caloriestracker.domain.repository

import com.juanpcf.caloriestracker.domain.model.AppPreferences
import com.juanpcf.caloriestracker.domain.model.Language
import com.juanpcf.caloriestracker.domain.model.Theme
import kotlinx.coroutines.flow.Flow

interface AppPreferencesRepository {
    val preferences: Flow<AppPreferences>
    suspend fun saveTheme(theme: Theme)
    suspend fun saveLanguage(language: Language)
}