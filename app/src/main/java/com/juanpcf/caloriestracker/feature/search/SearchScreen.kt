package com.juanpcf.caloriestracker.feature.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juanpcf.caloriestracker.R
import com.juanpcf.caloriestracker.core.designsystem.components.EmptyStateView
import com.juanpcf.caloriestracker.core.designsystem.theme.MacroColors
import com.juanpcf.caloriestracker.domain.model.Food
import com.juanpcf.caloriestracker.domain.model.MealType
import java.time.LocalDate
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    selectedDate: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToScanner: () -> Unit = {},
    onNavigateToCameraAi: () -> Unit = {},
    viewModel: SearchViewModel = hiltViewModel()
) {
    val date = selectedDate?.let { LocalDate.parse(it) } ?: LocalDate.now()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showMealTypeDialog by remember { mutableStateOf(false) }

    LaunchedEffect(date) {
        viewModel.setDate(date)
    }

    if (showMealTypeDialog) {
        MealTypePickerDialog(
            onConfirm = { mealType ->
                viewModel.saveSelectedToLog(mealType)
                showMealTypeDialog = false
                onNavigateBack()
            },
            onDismiss = { showMealTypeDialog = false }
        )
    }

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Inline search bar with back arrow (no TopAppBar)
                SearchBar(
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = uiState.query,
                            onQueryChange = viewModel::onQueryChange,
                            onSearch = { viewModel.search(it) },
                            expanded = false,
                            onExpandedChange = {},
                            placeholder = { Text(stringResource(R.string.search_hint)) },
                            leadingIcon = {
                                IconButton(onClick = onNavigateBack) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = null
                                    )
                                }
                            },
                            trailingIcon = {
                                if (uiState.isOffline) {
                                    Icon(
                                        imageVector = Icons.Filled.CloudOff,
                                        contentDescription = stringResource(R.string.label_offline),
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        )
                    },
                    expanded = false,
                    onExpandedChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {}

                // Filter chips: Scan Barcode and AI Vision
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = false,
                        onClick = onNavigateToScanner,
                        label = { Text(stringResource(R.string.scan_barcode)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.QrCodeScanner,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                    FilterChip(
                        selected = false,
                        onClick = onNavigateToCameraAi,
                        label = { Text(stringResource(R.string.ai_recognition)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }

                if (uiState.isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                uiState.error?.let { errorMessage ->
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                if (!uiState.isLoading && uiState.results.isEmpty() && uiState.query.isNotBlank()) {
                    if (uiState.isOffline) {
                        EmptyStateView(
                            icon = Icons.Filled.CloudOff,
                            message = stringResource(R.string.empty_offline_no_cache)
                        )
                    } else if (uiState.error == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.no_results),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Results list — leave bottom space for the sticky bar
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 4.dp,
                        bottom = if (uiState.selectedItems.isNotEmpty()) 96.dp else 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.results, key = { it.id }) { food ->
                        FoodResultCard(
                            food = food,
                            isSelected = food.id in uiState.selectedItems,
                            onToggleSelect = { viewModel.toggleSelection(food.id) }
                        )
                    }
                }
            }

            // Sticky bottom bar — visible when items are selected
            AnimatedVisibility(
                visible = uiState.selectedItems.isNotEmpty(),
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                val totalKcal = remember(uiState.selectedItems, uiState.results) {
                    uiState.selectedItems
                        .mapNotNull { id -> uiState.results.find { it.id == id } }
                        .sumOf { it.calories }
                        .roundToInt()
                }

                Surface(shadowElevation = 8.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.label_currently_selected).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${uiState.selectedItems.size} items • $totalKcal kcal",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(
                            onClick = { showMealTypeDialog = true },
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(stringResource(R.string.btn_save_to_my_day))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FoodResultCard(
    food: Food,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors = if (isSelected) {
            CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        } else {
            CardDefaults.elevatedCardColors()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Food icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .padding(end = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Restaurant,
                    contentDescription = null,
                    tint = MacroColors.protein,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Food info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = food.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${food.servingSize.toInt()}${food.servingUnit} • ${food.calories.toInt()} kcal",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "P:${food.protein.toInt()}g  C:${food.carbs.toInt()}g  F:${food.fat.toInt()}g",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Toggle select button
            IconButton(onClick = onToggleSelect) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = MacroColors.protein
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.AddCircleOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun MealTypePickerDialog(
    onConfirm: (MealType) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMeal by remember { mutableStateOf(MealType.BREAKFAST) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_meal)) },
        text = {
            Column {
                MealType.entries.forEach { mealType ->
                    val label = when (mealType) {
                        MealType.BREAKFAST -> stringResource(R.string.meal_breakfast)
                        MealType.LUNCH     -> stringResource(R.string.meal_lunch)
                        MealType.DINNER    -> stringResource(R.string.meal_dinner)
                        MealType.SNACK     -> stringResource(R.string.meal_snack)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedMeal == mealType,
                            onClick = { selectedMeal = mealType }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedMeal) }) {
                Text(stringResource(R.string.btn_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}
