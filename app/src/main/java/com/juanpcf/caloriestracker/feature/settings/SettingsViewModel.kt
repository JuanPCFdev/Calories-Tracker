package com.juanpcf.caloriestracker.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpcf.caloriestracker.domain.model.AppPreferences
import com.juanpcf.caloriestracker.domain.model.Language
import com.juanpcf.caloriestracker.domain.model.Theme
import com.juanpcf.caloriestracker.domain.model.UserGoals
import com.juanpcf.caloriestracker.domain.model.UserProfile
import com.juanpcf.caloriestracker.domain.repository.AuthRepository
import com.juanpcf.caloriestracker.domain.usecase.auth.SignOutUseCase
import com.juanpcf.caloriestracker.domain.usecase.goals.GetUserGoalsUseCase
import com.juanpcf.caloriestracker.domain.usecase.preferences.GetAppPreferencesUseCase
import com.juanpcf.caloriestracker.domain.usecase.preferences.SaveLanguageUseCase
import com.juanpcf.caloriestracker.domain.usecase.preferences.SaveThemeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val theme: Theme = Theme.SYSTEM,
    val language: Language = Language.EN,
    val userGoals: UserGoals? = null,
    val userProfile: UserProfile? = null,
    val isLoading: Boolean = false
)

sealed interface SettingsUiEvent {
    data object NavigateToGoals : SettingsUiEvent
    data object ShowLanguageDialog : SettingsUiEvent
    data object ShowThemeDialog : SettingsUiEvent
    data object SignOut : SettingsUiEvent
    data class ShowError(val message: String) : SettingsUiEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getAppPreferences: GetAppPreferencesUseCase,
    private val saveTheme: SaveThemeUseCase,
    private val saveLanguage: SaveLanguageUseCase,
    private val getUserGoals: GetUserGoalsUseCase,
    private val signOut: SignOutUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _events = Channel<SettingsUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    val uiState: StateFlow<SettingsUiState> = combine(
        getAppPreferences(),
        getUserGoals(authRepository.currentUser?.uid ?: "")
    ) { prefs: AppPreferences, goals: UserGoals? ->
        SettingsUiState(
            theme = prefs.theme,
            language = prefs.language,
            userGoals = goals,
            userProfile = authRepository.currentUser,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(
            userProfile = authRepository.currentUser,
            isLoading = true
        )
    )

    fun onThemeSelected(theme: Theme) {
        viewModelScope.launch {
            runCatching { saveTheme(theme) }
                .onFailure { _events.send(SettingsUiEvent.ShowError(it.message ?: "")) }
        }
    }

    fun onLanguageSelected(language: Language) {
        viewModelScope.launch {
            runCatching { saveLanguage(language) }
                .onFailure { _events.send(SettingsUiEvent.ShowError(it.message ?: "")) }
        }
    }

    fun onNavigateToGoals() {
        viewModelScope.launch { _events.send(SettingsUiEvent.NavigateToGoals) }
    }

    fun onShowThemeDialog() {
        viewModelScope.launch { _events.send(SettingsUiEvent.ShowThemeDialog) }
    }

    fun onShowLanguageDialog() {
        viewModelScope.launch { _events.send(SettingsUiEvent.ShowLanguageDialog) }
    }

    fun onSignOut() {
        viewModelScope.launch {
            runCatching { signOut() }
                .onSuccess { _events.send(SettingsUiEvent.SignOut) }
                .onFailure { _events.send(SettingsUiEvent.ShowError(it.message ?: "")) }
        }
    }
}
