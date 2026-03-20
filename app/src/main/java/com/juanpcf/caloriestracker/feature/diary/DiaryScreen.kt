package com.juanpcf.caloriestracker.feature.diary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juanpcf.caloriestracker.R
import com.juanpcf.caloriestracker.core.designsystem.components.CalorieRingChart
import com.juanpcf.caloriestracker.core.designsystem.components.EmptyStateView
import com.juanpcf.caloriestracker.core.designsystem.components.MacroRingChip
import com.juanpcf.caloriestracker.core.designsystem.theme.MacroColors
import com.juanpcf.caloriestracker.domain.model.DiaryEntry
import com.juanpcf.caloriestracker.domain.model.MealType
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val DATE_FORMATTER = DateTimeFormatter.ofPattern("EEE, MMM d")
private val TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a").withZone(ZoneId.systemDefault())

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(
    viewModel: DiaryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = viewModel::previousDay,
                            modifier = Modifier.align(Alignment.CenterStart)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous day"
                            )
                        }
                        Text(
                            text = uiState.selectedDate.format(DATE_FORMATTER),
                            style = MaterialTheme.typography.titleMedium
                        )
                        IconButton(
                            onClick = viewModel::nextDay,
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next day"
                            )
                        }
                    }
                },
                actions = {
                    if (uiState.isOffline) {
                        AssistChip(
                            onClick = {},
                            label = { Text(stringResource(R.string.label_offline)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.CloudOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        val goals = uiState.goals
        val totals = uiState.totals

        // Sort entries by createdAt descending (most recent first)
        val sortedEntries = uiState.entries.sortedByDescending { it.createdAt }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // --- Calorie Ring Chart ---
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CalorieRingChart(
                        consumed = totals.calories,
                        goal = goals?.dailyCalories?.toDouble() ?: 0.0,
                        ringColor = MacroColors.protein,
                        strokeWidth = 16.dp,
                        modifier = Modifier.size(200.dp),
                        centerContent = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "${totals.calories.toInt()}",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "/ ${goals?.dailyCalories ?: 0} kcal",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // --- Macro Ring Chips ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MacroRingChip(
                            label = stringResource(R.string.label_protein),
                            consumed = totals.protein,
                            goal = goals?.dailyProtein?.toDouble() ?: 0.0,
                            color = MacroColors.protein
                        )
                        MacroRingChip(
                            label = stringResource(R.string.label_carbs),
                            consumed = totals.carbs,
                            goal = goals?.dailyCarbs?.toDouble() ?: 0.0,
                            color = MacroColors.carbs
                        )
                        MacroRingChip(
                            label = stringResource(R.string.label_fat),
                            consumed = totals.fat,
                            goal = goals?.dailyFat?.toDouble() ?: 0.0,
                            color = MacroColors.fat
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // --- Recent Activity Section Header ---
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.diary_recent_activity),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = {}) {
                        Text(text = stringResource(R.string.diary_view_all))
                    }
                }
            }

            // --- Activity Feed Items ---
            if (sortedEntries.isEmpty() && !uiState.isLoading && uiState.error == null) {
                item {
                    if (uiState.isOffline) {
                        EmptyStateView(
                            icon = Icons.Filled.CloudOff,
                            message = stringResource(R.string.empty_offline_no_cache)
                        )
                    } else {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.empty_diary),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            items(sortedEntries, key = { it.id }) { entry ->
                ActivityFeedItem(
                    entry = entry,
                    onDelete = { viewModel.deleteEntry(entry) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // --- Loading indicator ---
            if (uiState.isLoading) {
                item {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp)
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            // --- Error state ---
            if (uiState.error != null) {
                item {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Bottom spacing
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun ActivityFeedItem(
    entry: DiaryEntry,
    onDelete: (DiaryEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val mealLabel = when (entry.mealType) {
        MealType.BREAKFAST -> stringResource(R.string.meal_breakfast)
        MealType.LUNCH -> stringResource(R.string.meal_lunch)
        MealType.DINNER -> stringResource(R.string.meal_dinner)
        MealType.SNACK -> stringResource(R.string.meal_snack)
    }

    val timeLabel = try {
        TIME_FORMATTER.format(entry.createdAt)
    } catch (e: Exception) {
        TIME_FORMATTER.format(Instant.EPOCH)
    }

    ElevatedCard(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Leading icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MacroColors.protein.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = MacroColors.protein,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title + subtitle
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.food.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = "$mealLabel • $timeLabel",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Calories trailing
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${entry.caloriesSnapshot.toInt()}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MacroColors.protein
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "KCAL",
                        style = MaterialTheme.typography.labelSmall,
                        color = MacroColors.protein.copy(alpha = 0.7f)
                    )
                }
            }

            // Delete button
            IconButton(
                onClick = { onDelete(entry) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.btn_delete),
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
