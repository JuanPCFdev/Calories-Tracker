package com.juanpcf.caloriestracker.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.juanpcf.caloriestracker.domain.model.AppPreferences
import com.juanpcf.caloriestracker.domain.model.Language
import com.juanpcf.caloriestracker.domain.model.Theme
import com.juanpcf.caloriestracker.domain.repository.AppPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

private object PreferencesKeys {
    val THEME    = stringPreferencesKey("theme")
    val LANGUAGE = stringPreferencesKey("language")
}

class AppPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : AppPreferencesRepository {

    override val preferences: Flow<AppPreferences> = dataStore.data
        .catch { e -> if (e is IOException) emit(androidx.datastore.preferences.core.emptyPreferences()) else throw e }
        .map { prefs ->
            AppPreferences(
                theme = prefs[PreferencesKeys.THEME]
                    ?.let { runCatching { Theme.valueOf(it) }.getOrNull() } ?: Theme.SYSTEM,
                language = prefs[PreferencesKeys.LANGUAGE]
                    ?.let { runCatching { Language.valueOf(it) }.getOrNull() } ?: Language.EN
            )
        }

    override suspend fun saveTheme(theme: Theme) {
        dataStore.edit { it[PreferencesKeys.THEME] = theme.name }
    }

    override suspend fun saveLanguage(language: Language) {
        dataStore.edit { it[PreferencesKeys.LANGUAGE] = language.name }
    }
}
