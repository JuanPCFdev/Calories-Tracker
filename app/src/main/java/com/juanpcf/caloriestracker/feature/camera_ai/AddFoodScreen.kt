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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juanpcf.caloriestracker.R
import com.juanpcf.caloriestracker.core.navigation.AiResult
import com.juanpcf.caloriestracker.domain.model.Food
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@androidx.camera.core.ExperimentalGetImage
@Composable
fun AddFoodScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAiResult: (AiResult) -> Unit,
    viewModel: AddFoodViewModel = hiltViewModel()
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
                is AddFoodUiEvent.NavigateToAiResult ->
                    onNavigateToAiResult(event.food.toAiResultRoute())
                is AddFoodUiEvent.ShowUnrecognized ->
                    onNavigateToAiResult(
                        AiResult(
                            foodId = UUID.randomUUID().toString(),
                            foodName = "", calories = 0.0, protein = 0.0,
                            carbs = 0.0, fat = 0.0, servingSize = 100.0,
                            servingUnit = "g", isUnrecognized = true
                        )
                    )
                is AddFoodUiEvent.ShowError -> Unit // error shown in uiState
            }
        }
    }

    val imageCaptureUseCase = remember { ImageCapture.Builder().build() }
    val selectedTabIndex = when (uiState.inputMode) {
        InputMode.CAMERA -> 0
        InputMode.LABEL -> 1
        InputMode.TEXT -> 2
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.add_food)) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
            }
        )
        TabRow(selectedTabIndex = selectedTabIndex) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { viewModel.setInputMode(InputMode.CAMERA) },
                text = { Text(stringResource(R.string.addfood_tab_camera)) },
                icon = { Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { viewModel.setInputMode(InputMode.LABEL) },
                text = { Text(stringResource(R.string.addfood_tab_label)) },
                icon = { Icon(Icons.Filled.Receipt, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTabIndex == 2,
                onClick = { viewModel.setInputMode(InputMode.TEXT) },
                text = { Text(stringResource(R.string.addfood_tab_text)) },
                icon = { Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (uiState.inputMode) {
                InputMode.CAMERA, InputMode.LABEL -> {
                    if (hasCameraPermission) {
                        CameraPreviewContent(
                            imageCaptureUseCase = imageCaptureUseCase,
                            uiState = uiState,
                            hint = if (uiState.inputMode == InputMode.LABEL)
                                stringResource(R.string.addfood_label_hint)
                            else stringResource(R.string.addfood_camera_hint),
                            onCapture = { bitmap -> viewModel.onCaptureImage(bitmap) },
                            onReset = viewModel::reset
                        )
                    } else {
                        NoCameraPermissionContent(
                            onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) }
                        )
                    }
                }
                InputMode.TEXT -> {
                    TextInputContent(
                        query = uiState.textQuery,
                        isOffline = uiState.isOffline,
                        error = if (uiState.captureState == CaptureState.ERROR) uiState.error else null,
                        captureState = uiState.captureState,
                        onQueryChange = viewModel::onTextQueryChange,
                        onAnalyze = viewModel::analyzeTextFood,
                        onReset = viewModel::reset
                    )
                }
            }

            // Loading overlay — shown for both modes
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
                            text = stringResource(
                                if (uiState.inputMode == InputMode.LABEL)
                                    R.string.addfood_analyzing_label
                                else R.string.addfood_analyzing_food
                            ),
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

@androidx.camera.core.ExperimentalGetImage
@Composable
private fun CameraPreviewContent(
    imageCaptureUseCase: ImageCapture,
    uiState: AddFoodUiState,
    hint: String? = null,
    onCapture: (Bitmap) -> Unit,
    onReset: () -> Unit
) {
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreviewView(imageCaptureUseCase)

        // Hint (e.g. "apuntá a la tabla nutricional") — solo cuando aplica
        if (hint != null) {
            Text(
                text = hint,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(12.dp)
            )
        }

        // Bottom controls bar
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
            if (uiState.isOffline) {
                Text(
                    text = stringResource(R.string.label_offline_ai_disabled),
                    color = Color(0xFFFFCC00),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (uiState.captureState == CaptureState.ERROR && uiState.error != null) {
                Text(
                    text = uiState.error,
                    color = Color(0xFFFF6B6B),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    textAlign = TextAlign.Center
                )
            }
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
                                    onBitmapReady = onCapture
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
            if (uiState.captureState == CaptureState.ERROR) {
                Button(onClick = onReset) { Text("Try Again") }
            }
        }
    }
}

@Composable
private fun TextInputContent(
    query: String,
    isOffline: Boolean,
    error: String?,
    captureState: CaptureState,
    onQueryChange: (String) -> Unit,
    onAnalyze: () -> Unit,
    onReset: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "What did you eat?",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Type a food name or dish and the AI will estimate its nutritional info.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text("Food name or dish") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                keyboardController?.hide()
                onAnalyze()
            }),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            enabled = !isOffline && captureState != CaptureState.ANALYZING
        )

        if (isOffline) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.label_offline_ai_disabled),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
            TextButton(onClick = onReset) { Text("Clear") }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                keyboardController?.hide()
                onAnalyze()
            },
            enabled = query.isNotBlank() && !isOffline && captureState != CaptureState.ANALYZING,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Analyze with AI")
        }
    }
}

@Composable
private fun NoCameraPermissionContent(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Camera permission is required for photo recognition.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRequestPermission) { Text("Grant Permission") }
    }
}

@androidx.camera.core.ExperimentalGetImage
@Composable
private fun CameraPreviewView(imageCaptureUseCase: ImageCapture) {
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
                lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCaptureUseCase
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
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
    foodId = id, foodName = name, calories = calories,
    protein = protein, carbs = carbs, fat = fat, sugar = sugar ?: 0.0,
    servingSize = servingSize, servingUnit = servingUnit,
    confidence = confidence?.name ?: "",
    isUnrecognized = false
)
