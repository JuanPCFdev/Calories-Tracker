package com.juanpcf.caloriestracker.feature.camera_ai

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpcf.caloriestracker.core.util.NetworkMonitor
import com.juanpcf.caloriestracker.domain.model.Food
import com.juanpcf.caloriestracker.domain.usecase.food.RecognizeFoodFromImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class CaptureState { IDLE, CAPTURING, ANALYZING, DONE, ERROR }

data class CameraAiUiState(
    val captureState: CaptureState = CaptureState.IDLE,
    val analyzedFood: Food? = null,
    val rawAiResponse: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isOffline: Boolean = false
)

sealed interface CameraAiUiEvent {
    data class NavigateToAiResult(val food: Food) : CameraAiUiEvent
    data class ShowError(val message: String) : CameraAiUiEvent
    data object ShowRawResponseFallback : CameraAiUiEvent
}

@HiltViewModel
class CameraAiViewModel @Inject constructor(
    private val recognizeFoodFromImage: RecognizeFoodFromImageUseCase,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _uiState = MutableStateFlow(CameraAiUiState())
    val uiState: StateFlow<CameraAiUiState> = _uiState.asStateFlow()

    private val _uiEvents = Channel<CameraAiUiEvent>(Channel.BUFFERED)
    val uiEvents = _uiEvents.receiveAsFlow()

    init {
        viewModelScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                _uiState.update { it.copy(isOffline = !isOnline) }
            }
        }
    }

    fun onCaptureImage(bitmap: Bitmap) {
        val current = _uiState.value
        if (current.captureState == CaptureState.CAPTURING ||
            current.captureState == CaptureState.ANALYZING
        ) return

        _uiState.update {
            it.copy(captureState = CaptureState.CAPTURING, error = null)
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(captureState = CaptureState.ANALYZING, isLoading = true)
            }

            recognizeFoodFromImage(bitmap)
                .onSuccess { food ->
                    _uiState.update {
                        it.copy(
                            captureState = CaptureState.DONE,
                            analyzedFood = food,
                            isLoading = false
                        )
                    }
                    _uiEvents.send(CameraAiUiEvent.NavigateToAiResult(food))
                }
                .onFailure { throwable ->
                    val isUnrecognized = throwable.message?.contains("UNRECOGNIZED", ignoreCase = true) == true
                    _uiState.update {
                        it.copy(
                            captureState = CaptureState.ERROR,
                            isLoading = false,
                            error = throwable.message
                        )
                    }
                    if (isUnrecognized) {
                        _uiEvents.send(CameraAiUiEvent.ShowRawResponseFallback)
                    } else {
                        _uiEvents.send(CameraAiUiEvent.ShowError(throwable.message ?: "Unknown error"))
                    }
                }
        }
    }

    fun reset() {
        _uiState.update { current ->
            CameraAiUiState(isOffline = current.isOffline)
        }
    }
}
