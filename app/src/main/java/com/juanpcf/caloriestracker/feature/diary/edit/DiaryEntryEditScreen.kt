package com.juanpcf.caloriestracker.feature.diary.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juanpcf.caloriestracker.R
import com.juanpcf.caloriestracker.domain.model.MealType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryEntryEditScreen(
    onNavigateBack: () -> Unit,
    viewModel: DiaryEntryEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is DiaryEntryEditUiEvent.NavigateBack -> onNavigateBack()
                is DiaryEntryEditUiEvent.ShowError -> snackbarHostState.showSnackbar(context.getString(event.resId))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_edit_entry)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.btn_cancel)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::onShowDeleteDialog) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.btn_delete)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when (val state = uiState) {
            is DiaryEntryEditUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is DiaryEntryEditUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            is DiaryEntryEditUiState.Loaded -> {
                DiaryEntryEditContent(
                    state = state,
                    onServingsChange = viewModel::onServingsChange,
                    onMealTypeChange = viewModel::onMealTypeChange,
                    onCaloriesChange = viewModel::onCaloriesChange,
                    onProteinChange = viewModel::onProteinChange,
                    onCarbsChange = viewModel::onCarbsChange,
                    onFatChange = viewModel::onFatChange,
                    onSave = viewModel::onSave,
                    modifier = Modifier.padding(paddingValues)
                )

                if (state.showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = viewModel::onDismissDeleteDialog,
                        title = { Text(stringResource(R.string.dialog_delete_entry_title)) },
                        text = { Text(stringResource(R.string.dialog_delete_entry_body)) },
                        confirmButton = {
                            TextButton(onClick = viewModel::onDelete) {
                                Text(stringResource(R.string.btn_delete))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = viewModel::onDismissDeleteDialog) {
                                Text(stringResource(R.string.btn_cancel))
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DiaryEntryEditContent(
    state: DiaryEntryEditUiState.Loaded,
    onServingsChange: (String) -> Unit,
    onMealTypeChange: (MealType) -> Unit,
    onCaloriesChange: (String) -> Unit,
    onProteinChange: (String) -> Unit,
    onCarbsChange: (String) -> Unit,
    onFatChange: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    var mealTypeExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Food name — read-only
        Text(
            text = state.entry.food.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        // Editable macro fields — row 1: Calories + Protein
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = state.editedCalories,
                onValueChange = onCaloriesChange,
                label = { Text(stringResource(R.string.label_calories)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = state.editedProtein,
                onValueChange = onProteinChange,
                label = { Text(stringResource(R.string.label_protein)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        // Editable macro fields — row 2: Carbs + Fat
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = state.editedCarbs,
                onValueChange = onCarbsChange,
                label = { Text(stringResource(R.string.label_carbs)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = state.editedFat,
                onValueChange = onFatChange,
                label = { Text(stringResource(R.string.label_fat)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        // Servings field
        OutlinedTextField(
            value = state.editedServings,
            onValueChange = onServingsChange,
            label = { Text(stringResource(R.string.edit_entry_servings_label)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Meal type dropdown
        ExposedDropdownMenuBox(
            expanded = mealTypeExpanded,
            onExpandedChange = { mealTypeExpanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = mealTypeLabel(state.editedMealType),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.edit_entry_meal_type_label)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = mealTypeExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(
                expanded = mealTypeExpanded,
                onDismissRequest = { mealTypeExpanded = false }
            ) {
                MealType.entries.forEach { mealType ->
                    DropdownMenuItem(
                        text = { Text(mealTypeLabel(mealType)) },
                        onClick = {
                            onMealTypeChange(mealType)
                            mealTypeExpanded = false
                        }
                    )
                }
            }
        }

        // Save button
        Button(
            onClick = onSave,
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.btn_save))
        }
    }
}

@Composable
private fun mealTypeLabel(mealType: MealType): String = when (mealType) {
    MealType.BREAKFAST -> stringResource(R.string.meal_breakfast)
    MealType.LUNCH -> stringResource(R.string.meal_lunch)
    MealType.DINNER -> stringResource(R.string.meal_dinner)
    MealType.SNACK -> stringResource(R.string.meal_snack)
}

