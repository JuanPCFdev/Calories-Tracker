package com.juanpcf.caloriestracker.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpcf.caloriestracker.domain.usecase.auth.RegisterWithEmailUseCase
import com.juanpcf.caloriestracker.domain.usecase.auth.SignInWithEmailUseCase
import com.juanpcf.caloriestracker.domain.usecase.auth.SignInWithGoogleUseCase
import com.juanpcf.caloriestracker.domain.usecase.auth.SignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AuthState {
    data object Idle : AuthState
    data object Loading : AuthState
    data object Success : AuthState
    data class Error(val message: String) : AuthState
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInWithEmailUseCase: SignInWithEmailUseCase,
    private val registerWithEmailUseCase: RegisterWithEmailUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = signInWithEmailUseCase(email, password)
                result.fold(
                    onSuccess = { _authState.value = AuthState.Success },
                    onFailure = { e -> _authState.value = AuthState.Error(e.message ?: "Unknown error") }
                )
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = registerWithEmailUseCase(email, password)
                result.fold(
                    onSuccess = { _authState.value = AuthState.Success },
                    onFailure = { e -> _authState.value = AuthState.Error(e.message ?: "Unknown error") }
                )
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = signInWithGoogleUseCase(idToken)
                result.fold(
                    onSuccess = { _authState.value = AuthState.Success },
                    onFailure = { e -> _authState.value = AuthState.Error(e.message ?: "Unknown error") }
                )
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun clearError() {
        _authState.value = AuthState.Idle
    }
}
