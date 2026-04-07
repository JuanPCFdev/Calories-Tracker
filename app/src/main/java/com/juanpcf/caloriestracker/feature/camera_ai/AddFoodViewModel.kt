package com.juanpcf.caloriestracker.feature.camera_ai

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.juanpcf.caloriestracker.core.util.NetworkMonitor
import com.juanpcf.caloriestracker.domain.model.Food
import com.juanpcf.caloriestracker.domain.usecase.food.RecognizeFoodFromImageUseCase
import com.juanpcf.caloriestracker.domain.usecase.food.RecognizeFoodFromTextUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

enum class CaptureState { IDLE, CAPTURING, ANALYZING, DONE, ERROR }
enum class InputMode { CAMERA, TEXT }

data class AddFoodUiState(
    val inputMode: InputMode = InputMode.CAMERA,
    val captureState: CaptureState = CaptureState.IDLE,
    val textQuery: String = "",
    val isOffline: Boolean = false,
    val error: String? = null
)

sealed interface AddFoodUiEvent {
    data class NavigateToAiResult(val food: Food) : AddFoodUiEvent
    data object ShowUnrecognized : AddFoodUiEvent
    data class ShowError(val message: String) : AddFoodUiEvent
}

@HiltViewModel
class AddFoodViewModel @Inject constructor(
    private val recognizeFoodFromImage: RecognizeFoodFromImageUseCase,
    private val recognizeFoodFromText: RecognizeFoodFromTextUseCase,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddFoodUiState())
    val uiState: StateFlow<AddFoodUiState> = _uiState.asStateFlow()

    private val _uiEvents = Channel<AddFoodUiEvent>(Channel.BUFFERED)
    val uiEvents = _uiEvents.receiveAsFlow()

    init {
        viewModelScope.launch {
            networkMonitor.isOnline.collect { isOnline ->
                _uiState.update { it.copy(isOffline = !isOnline) }
            }
        }
    }

    fun setInputMode(mode: InputMode) {
        _uiState.update { it.copy(inputMode = mode, captureState = CaptureState.IDLE, error = null) }
    }

    fun onTextQueryChange(text: String) {
        _uiState.update { it.copy(textQuery = text) }
    }

    fun analyzeTextFood() {
        val query = _uiState.value.textQuery.trim()
        if (query.isBlank() || _uiState.value.captureState == CaptureState.ANALYZING) return
        viewModelScope.launch {
            _uiState.update { it.copy(captureState = CaptureState.ANALYZING, error = null) }
            runAnalysis { recognizeFoodFromText(query) }
        }
    }

    fun onCaptureImage(bitmap: Bitmap) {
        val current = _uiState.value
        if (current.captureState == CaptureState.CAPTURING ||
            current.captureState == CaptureState.ANALYZING) return
        _uiState.update { it.copy(captureState = CaptureState.CAPTURING, error = null) }
        viewModelScope.launch {
            _uiState.update { it.copy(captureState = CaptureState.ANALYZING) }
            runAnalysis { recognizeFoodFromImage(bitmap) }
        }
    }

    private suspend fun runAnalysis(block: suspend () -> Result<Food>) {
        val result = retryWithBackoff(maxAttempts = 3) {
            try {
                withTimeout(90_000L) { block() }
            } catch (e: TimeoutCancellationException) {
                Result.failure(IllegalStateException("Request timed out. Please try again."))
            }
        }
        result
            .onSuccess { food ->
                _uiState.update { it.copy(captureState = CaptureState.DONE, error = null) }
                _uiEvents.send(AddFoodUiEvent.NavigateToAiResult(food))
            }
            .onFailure { throwable ->
                val isUnrecognized = throwable.message?.contains("UNRECOGNIZED", ignoreCase = true) == true
                val errorMsg = when {
                    throwable.message?.contains("429") == true ||
                    throwable.message?.contains("rate", ignoreCase = true) == true ->
                        "All AI models are busy right now. Please try again in a moment."
                    else -> throwable.message
                }
                _uiState.update { it.copy(captureState = CaptureState.ERROR, error = errorMsg) }
                if (isUnrecognized) {
                    _uiEvents.send(AddFoodUiEvent.ShowUnrecognized)
                } else {
                    _uiEvents.send(AddFoodUiEvent.ShowError(errorMsg ?: "Unknown error"))
                }
            }
    }

    // Retries only on 429 / rate-limit errors; propagates immediately on all other failures.
    private suspend fun retryWithBackoff(
        maxAttempts: Int,
        block: suspend () -> Result<Food>
    ): Result<Food> {
        repeat(maxAttempts) { attempt ->
            val result = block()
            val isRateLimit = result.exceptionOrNull()?.message?.let {
                it.contains("429") || it.contains("rate", ignoreCase = true)
            } == true
            if (result.isSuccess || !isRateLimit) return result
            if (attempt < maxAttempts - 1) delay(2_000L * (attempt + 1)) // 2s, 4s
        }
        return block()
    }

    fun reset() {
        _uiState.update { current ->
            AddFoodUiState(inputMode = current.inputMode, isOffline = current.isOffline)
        }
    }
}
