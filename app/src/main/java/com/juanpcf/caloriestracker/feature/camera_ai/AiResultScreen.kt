package com.juanpcf.caloriestracker.feature.camera_ai

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juanpcf.caloriestracker.R
import com.juanpcf.caloriestracker.core.designsystem.components.NutritionBarsCard
import com.juanpcf.caloriestracker.domain.model.MealType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiResultScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDiary: () -> Unit,
    viewModel: AiResultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is AiResultUiEvent.NavigateToDiary -> onNavigateToDiary()
                is AiResultUiEvent.ShowError -> snackbarHostState.showSnackbar(context.getString(event.resId))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (viewModel.isUnrecognized) stringResource(R.string.ai_result_title_unrecognized)
                        else stringResource(R.string.title_ai_result)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.btn_cancel)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            AiResultContent(
                uiState = uiState,
                isUnrecognized = viewModel.isUnrecognized,
                onFoodNameChange = viewModel::onFoodNameChange,
                onCaloriesChange = viewModel::onCaloriesChange,
                onProteinChange = viewModel::onProteinChange,
                onCarbsChange = viewModel::onCarbsChange,
                onFatChange = viewModel::onFatChange,
                onServingSizeChange = viewModel::onServingSizeChange,
                onServingUnitChange = viewModel::onServingUnitChange,
                onAddToDiary = { mealType, servings -> viewModel.addToDiary(mealType, servings) },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
private fun AiResultContent(
    uiState: AiResultUiState,
    isUnrecognized: Boolean,
    onFoodNameChange: (String) -> Unit,
    onCaloriesChange: (String) -> Unit,
    onProteinChange: (String) -> Unit,
    onCarbsChange: (String) -> Unit,
    onFatChange: (String) -> Unit,
    onServingSizeChange: (String) -> Unit,
    onServingUnitChange: (String) -> Unit,
    onAddToDiary: (MealType, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMealType by remember { mutableStateOf(MealType.BREAKFAST) }
    var servingsText by remember { mutableStateOf("1") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Unrecognized banner
        if (isUnrecognized) {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.ai_result_unrecognized_banner),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // Nutrition preview card — live preview that updates as user edits fields
        NutritionBarsCard(
            title = stringResource(R.string.ai_result_nutrition_preview),
            calories = uiState.calories.toDoubleOrNull() ?: 0.0,
            caloriesGoal = 2000.0,
            protein = uiState.protein.toDoubleOrNull() ?: 0.0,
            proteinGoal = 150.0,
            carbs = uiState.carbs.toDoubleOrNull() ?: 0.0,
            carbsGoal = 250.0,
            fat = uiState.fat.toDoubleOrNull() ?: 0.0,
            fatGoal = 65.0,
            modifier = Modifier.fillMaxWidth()
        )

        // Food details edit card
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.ai_result_food_details),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = uiState.foodName,
                    onValueChange = onFoodNameChange,
                    label = { Text(stringResource(R.string.ai_result_food_details)) },
                    enabled = uiState.isEditable,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = uiState.servingSize,
                        onValueChange = onServingSizeChange,
                        label = { Text(stringResource(R.string.label_serving_size)) },
                        enabled = uiState.isEditable,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(2f)
                    )
                    ServingUnitSelector(
                        selectedUnit = uiState.servingUnit,
                        enabled = uiState.isEditable,
                        onUnitSelected = onServingUnitChange,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = stringResource(R.string.ai_result_nutrition_per_serving),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = uiState.calories,
                        onValueChange = onCaloriesChange,
                        label = { Text(stringResource(R.string.label_calories)) },
                        enabled = uiState.isEditable,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = uiState.protein,
                        onValueChange = onProteinChange,
                        label = { Text(stringResource(R.string.label_protein)) },
                        enabled = uiState.isEditable,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = uiState.carbs,
                        onValueChange = onCarbsChange,
                        label = { Text(stringResource(R.string.label_carbs)) },
                        enabled = uiState.isEditable,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = uiState.fat,
                        onValueChange = onFatChange,
                        label = { Text(stringResource(R.string.label_fat)) },
                        enabled = uiState.isEditable,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Add to diary card
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.btn_add_to_diary),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MealType.entries.forEach { mealType ->
                        FilterChip(
                            selected = selectedMealType == mealType,
                            onClick = { selectedMealType = mealType },
                            label = {
                                Text(
                                    when (mealType) {
                                        MealType.BREAKFAST -> stringResource(R.string.meal_breakfast)
                                        MealType.LUNCH -> stringResource(R.string.meal_lunch)
                                        MealType.DINNER -> stringResource(R.string.meal_dinner)
                                        MealType.SNACK -> stringResource(R.string.meal_snack)
                                    }
                                )
                            }
                        )
                    }
                }

                OutlinedTextField(
                    value = servingsText,
                    onValueChange = { servingsText = it },
                    label = { Text(stringResource(R.string.label_servings)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        val servings = servingsText.toDoubleOrNull() ?: 1.0
                        onAddToDiary(selectedMealType, servings)
                    },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.btn_add_to_diary))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ServingUnitSelector(
    selectedUnit: String,
    enabled: Boolean,
    onUnitSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val units = listOf("g", "ml", "oz", "piece")

    Box(modifier = modifier) {
        OutlinedTextField(
            value = selectedUnit,
            onValueChange = {},
            label = { Text(stringResource(R.string.label_unit)) },
            readOnly = true,
            enabled = enabled,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = enabled,
                    onClick = {
                        val idx = units.indexOf(selectedUnit)
                        onUnitSelected(units[(idx + 1) % units.size])
                    }
                )
        )
    }
}
