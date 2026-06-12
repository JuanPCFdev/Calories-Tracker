package com.juanpcf.caloriestracker.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpcf.caloriestracker.R
import com.juanpcf.caloriestracker.domain.model.ActivityLevel
import com.juanpcf.caloriestracker.domain.model.Gender
import com.juanpcf.caloriestracker.domain.model.UserPhysicalProfile
import com.juanpcf.caloriestracker.domain.repository.AuthRepository
import com.juanpcf.caloriestracker.domain.usecase.profile.GetUserPhysicalProfileUseCase
import com.juanpcf.caloriestracker.domain.usecase.profile.SaveUserPhysicalProfileUseCase
import com.juanpcf.caloriestracker.domain.util.NutritionCalculator
import com.juanpcf.caloriestracker.domain.util.toDecimalOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

data class PhysicalProfileUiState(
    val height: String = "",
    val weight: String = "",
    val birthDate: LocalDate? = null,
    val gender: Gender = Gender.MALE,
    val activityLevel: ActivityLevel = ActivityLevel.MODERATE,
    val estimatedTdee: Int? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

sealed interface PhysicalProfileUiEvent {
    data object NavigateBack : PhysicalProfileUiEvent
    data class ShowError(val resId: Int) : PhysicalProfileUiEvent
}

@HiltViewModel
class PhysicalProfileViewModel @Inject constructor(
    private val getProfile: GetUserPhysicalProfileUseCase,
    private val saveProfile: SaveUserPhysicalProfileUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PhysicalProfileUiState())
    val uiState: StateFlow<PhysicalProfileUiState> = _uiState.asStateFlow()

    private val _events = Channel<PhysicalProfileUiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            val userId = authRepository.currentUser?.uid ?: return@launch
            runCatching { getProfile(userId).first() }
                .onSuccess { profile ->
                    _uiState.update {
                        if (profile == null) it.copy(isLoading = false)
                        else it.copy(
                            height = formatNumber(profile.heightCm),
                            weight = formatNumber(profile.weightKg),
                            birthDate = profile.birthDate,
                            gender = profile.gender,
                            activityLevel = profile.activityLevel,
                            isLoading = false
                        )
                    }
                    recomputeTdee()
                }
                .onFailure { e ->
                    Timber.e(e, "No se pudo cargar el perfil físico")
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun onHeightChange(value: String) { _uiState.update { it.copy(height = value, error = null) }; recomputeTdee() }
    fun onWeightChange(value: String) { _uiState.update { it.copy(weight = value, error = null) }; recomputeTdee() }
    fun onBirthDateChange(date: LocalDate) { _uiState.update { it.copy(birthDate = date, error = null) }; recomputeTdee() }
    fun onGenderChange(gender: Gender) { _uiState.update { it.copy(gender = gender) }; recomputeTdee() }
    fun onActivityLevelChange(level: ActivityLevel) { _uiState.update { it.copy(activityLevel = level) }; recomputeTdee() }

    /** Recalcula el TDEE de preview cuando los datos son válidos; si no, lo limpia. */
    private fun recomputeTdee() {
        val profile = currentValidProfile()
        _uiState.update {
            it.copy(estimatedTdee = profile?.let { p -> NutritionCalculator.tdee(p, LocalDate.now()).toInt() })
        }
    }

    fun save() {
        val profile = currentValidProfile()
        if (profile == null) {
            viewModelScope.launch { _events.send(PhysicalProfileUiEvent.ShowError(R.string.error_invalid_profile)) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { saveProfile(profile) }
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.send(PhysicalProfileUiEvent.NavigateBack)
                }
                .onFailure { e ->
                    Timber.e(e, "No se pudo guardar el perfil físico")
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                    _events.send(PhysicalProfileUiEvent.ShowError(R.string.error_generic))
                }
        }
    }

    /** Construye un perfil válido a partir del estado, o null si falta algo / está fuera de rango. */
    private fun currentValidProfile(): UserPhysicalProfile? {
        val userId = authRepository.currentUser?.uid ?: return null
        val state = _uiState.value
        val height = state.height.toDecimalOrNull() ?: return null
        val weight = state.weight.toDecimalOrNull() ?: return null
        val birthDate = state.birthDate ?: return null
        if (height !in 100.0..250.0 || weight !in 30.0..300.0) return null
        val age = NutritionCalculator.ageYears(birthDate, LocalDate.now())
        if (age !in 13..100) return null
        return UserPhysicalProfile(
            userId = userId,
            heightCm = height,
            weightKg = weight,
            birthDate = birthDate,
            gender = state.gender,
            activityLevel = state.activityLevel
        )
    }

    private fun formatNumber(v: Double): String =
        if (v == v.toLong().toDouble()) v.toLong().toString() else v.toString()
}
