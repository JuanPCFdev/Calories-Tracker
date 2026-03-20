package com.juanpcf.caloriestracker.feature.camera_ai

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.juanpcf.caloriestracker.R
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juanpcf.caloriestracker.core.navigation.AiResult
import com.juanpcf.caloriestracker.domain.model.Food
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@androidx.camera.core.ExperimentalGetImage
@Composable
fun CameraAiScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAiResult: (AiResult) -> Unit,
    viewModel: CameraAiViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var hasCameraPermission by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        val permission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        hasCameraPermission = permission == PackageManager.PERMISSION_GRANTED
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is CameraAiUiEvent.NavigateToAiResult -> {
                    onNavigateToAiResult(event.food.toAiResultRoute())
                }
                is CameraAiUiEvent.ShowRawResponseFallback -> {
                    // Navigate to AiResult with empty/default fields + isUnrecognized flag
                    val emptyRoute = AiResult(
                        foodId = UUID.randomUUID().toString(),
                        foodName = "",
                        calories = 0.0,
                        protein = 0.0,
                        carbs = 0.0,
                        fat = 0.0,
                        servingSize = 100.0,
                        servingUnit = "g",
                        isUnrecognized = true
                    )
                    onNavigateToAiResult(emptyRoute)
                }
                is CameraAiUiEvent.ShowError -> {
                    // Error is already in uiState; no navigation needed
                }
            }
        }
    }

    val imageCaptureUseCase = remember { ImageCapture.Builder().build() }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            CameraPreviewWithCapture(
                imageCaptureUseCase = imageCaptureUseCase
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Camera permission is required to use AI food recognition.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Grant Permission")
                }
            }
        }

        // Top bar overlay
        TopAppBar(
            title = { Text("AI Camera") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White
            ),
            modifier = Modifier.align(Alignment.TopStart)
        )

        // Loading overlay for ANALYZING / CAPTURING state
        if (uiState.captureState == CaptureState.ANALYZING ||
            uiState.captureState == CaptureState.CAPTURING
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (uiState.captureState == CaptureState.ANALYZING)
                            "Analyzing food…" else "Capturing…",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        // Bottom controls bar — always visible when camera is ready
        if (hasCameraPermission) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .navigationBarsPadding()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Offline banner inside controls bar
                if (uiState.isOffline) {
                    Text(
                        text = stringResource(R.string.label_offline_ai_disabled),
                        color = Color(0xFFFFCC00),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Error message inside controls bar
                if (uiState.captureState == CaptureState.ERROR && uiState.error != null) {
                    Text(
                        text = uiState.error ?: "",
                        color = Color(0xFFFF6B6B),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Shutter button
                if (uiState.captureState == CaptureState.IDLE ||
                    uiState.captureState == CaptureState.ERROR
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(80.dp)
                            .border(3.dp, Color.White, CircleShape)
                            .padding(6.dp)
                    ) {
                        IconButton(
                            onClick = {
                                if (!uiState.isOffline) {
                                    captureImage(
                                        imageCapture = imageCaptureUseCase,
                                        executor = ContextCompat.getMainExecutor(context),
                                        onBitmapReady = { bitmap -> viewModel.onCaptureImage(bitmap) }
                                    )
                                }
                            },
                            enabled = !uiState.isOffline,
                            modifier = Modifier
                                .size(64.dp)
                                .background(
                                    if (uiState.isOffline) Color.Gray else Color.White,
                                    CircleShape
                                )
                        ) {}
                    }
                }

                // Try Again button when in error state
                if (uiState.captureState == CaptureState.ERROR) {
                    Button(onClick = { viewModel.reset() }) {
                        Text("Try Again")
                    }
                }
            }
        }
    }
}

@androidx.camera.core.ExperimentalGetImage
@Composable
private fun CameraPreviewWithCapture(
    imageCaptureUseCase: ImageCapture
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }

    LaunchedEffect(lifecycleOwner) {
        val cameraProvider = ProcessCameraProvider.awaitInstance(context)
        val preview = Preview.Builder().build().also {
            it.surfaceProvider = previewView.surfaceProvider
        }
        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageCaptureUseCase
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize()
    )
}

private fun captureImage(
    imageCapture: ImageCapture,
    executor: java.util.concurrent.Executor,
    onBitmapReady: (Bitmap) -> Unit
) {
    imageCapture.takePicture(
        executor,
        object : ImageCapture.OnImageCapturedCallback() {
            @androidx.camera.core.ExperimentalGetImage
            override fun onCaptureSuccess(image: ImageProxy) {
                val bitmap = image.toBitmap()
                image.close()
                onBitmapReady(bitmap)
            }

            override fun onError(exception: ImageCaptureException) {
                exception.printStackTrace()
            }
        }
    )
}

private fun Food.toAiResultRoute(): AiResult = AiResult(
    foodId = id,
    foodName = name,
    calories = calories,
    protein = protein,
    carbs = carbs,
    fat = fat,
    servingSize = servingSize,
    servingUnit = servingUnit,
    isUnrecognized = false
)
