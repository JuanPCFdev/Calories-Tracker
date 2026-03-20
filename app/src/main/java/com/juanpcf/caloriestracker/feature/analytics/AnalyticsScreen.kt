package com.juanpcf.caloriestracker.feature.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.juanpcf.caloriestracker.R
import com.juanpcf.caloriestracker.core.designsystem.components.MacroRingChip
import com.juanpcf.caloriestracker.core.designsystem.theme.MacroColors
import com.juanpcf.caloriestracker.domain.model.DayMacros
import com.juanpcf.caloriestracker.feature.analytics.charts.MacroLineChart
import com.juanpcf.caloriestracker.feature.analytics.charts.CalorieTrendLineChart
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.title_analytics)) }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    CircularProgressIndicator()
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(4.dp)) }

                    // Period selector
                    item {
                        PeriodSelector(
                            selectedRange = uiState.selectedRange,
                            onRangeSelected = viewModel::selectRange
                        )
                    }

                    if (uiState.weeklyData.isEmpty() || uiState.weeklyData.all { it.calories == 0.0 }) {
                        item {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.empty_analytics),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        // Calorie trend line chart (replaces bar chart)
                        item {
                            ChartSection(title = stringResource(R.string.label_calories)) {
                                CalorieTrendLineChart(
                                    data = uiState.weeklyData,
                                    goalCalories = uiState.goals?.dailyCalories ?: 2000,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Macro donut row
                        item {
                            MacroDonutRow(
                                weeklyData = uiState.weeklyData,
                                proteinGoal = uiState.goals?.dailyProtein?.toDouble() ?: 150.0,
                                carbsGoal = uiState.goals?.dailyCarbs?.toDouble() ?: 250.0,
                                fatGoal = uiState.goals?.dailyFat?.toDouble() ?: 65.0
                            )
                        }

                        // Macro line chart section
                        item {
                            ChartSection(title = stringResource(R.string.analytics_macronutrients)) {
                                MacroLineChart(
                                    data = uiState.weeklyData,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Stat summary cards
                        item {
                            StatSummaryCards(
                                weeklyData = uiState.weeklyData,
                                goalCalories = uiState.goals?.dailyCalories ?: 2000
                            )
                        }

                        // Detailed summary card (best/worst day)
                        item {
                            SummaryCard(
                                weeklyData = uiState.weeklyData,
                                goalCalories = uiState.goals?.dailyCalories ?: 2000
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun PeriodSelector(
    selectedRange: Int,
    onRangeSelected: (Int) -> Unit
) {
    val ranges = listOf(
        stringResource(R.string.analytics_period_day) to 1,
        stringResource(R.string.analytics_period_week) to 7,
        stringResource(R.string.analytics_period_month) to 30
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ranges.forEach { (label, days) ->
            FilterChip(
                selected = selectedRange == days,
                onClick = { onRangeSelected(days) },
                label = { Text(label) }
            )
        }
    }
}

@Composable
private fun MacroDonutRow(
    weeklyData: List<DayMacros>,
    proteinGoal: Double,
    carbsGoal: Double,
    fatGoal: Double
) {
    val daysWithData = remember(weeklyData) { weeklyData.filter { it.calories > 0 } }
    val avgProtein = remember(daysWithData) {
        if (daysWithData.isEmpty()) 0.0 else daysWithData.sumOf { it.protein } / daysWithData.size
    }
    val avgCarbs = remember(daysWithData) {
        if (daysWithData.isEmpty()) 0.0 else daysWithData.sumOf { it.carbs } / daysWithData.size
    }
    val avgFat = remember(daysWithData) {
        if (daysWithData.isEmpty()) 0.0 else daysWithData.sumOf { it.fat } / daysWithData.size
    }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.analytics_macronutrients),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MacroRingChip(
                    label = stringResource(R.string.label_protein),
                    consumed = avgProtein,
                    goal = proteinGoal,
                    color = MacroColors.protein
                )
                MacroRingChip(
                    label = stringResource(R.string.label_carbs),
                    consumed = avgCarbs,
                    goal = carbsGoal,
                    color = MacroColors.carbs
                )
                MacroRingChip(
                    label = stringResource(R.string.label_fat),
                    consumed = avgFat,
                    goal = fatGoal,
                    color = MacroColors.fat
                )
            }
        }
    }
}

@Composable
private fun StatSummaryCards(
    weeklyData: List<DayMacros>,
    goalCalories: Int
) {
    val daysWithData = remember(weeklyData) { weeklyData.filter { it.calories > 0 } }
    val avgCalories = remember(daysWithData) {
        if (daysWithData.isEmpty()) 0.0 else daysWithData.sumOf { it.calories } / daysWithData.size
    }
    val avgProtein = remember(daysWithData) {
        if (daysWithData.isEmpty()) 0.0 else daysWithData.sumOf { it.protein } / daysWithData.size
    }
    val avgFat = remember(daysWithData) {
        if (daysWithData.isEmpty()) 0.0 else daysWithData.sumOf { it.fat } / daysWithData.size
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Avg kcal/day — primaryContainer background
        ElevatedCard(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = stringResource(R.string.analytics_avg_calories),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${avgCalories.roundToInt()}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = stringResource(R.string.label_kcal),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }

        // Avg Protein — secondaryContainer background
        ElevatedCard(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = stringResource(R.string.analytics_avg_protein),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${avgProtein.roundToInt()}g",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = stringResource(R.string.label_protein),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }
        }

        // Avg Fat — tertiaryContainer background
        ElevatedCard(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = stringResource(R.string.analytics_avg_fat),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${avgFat.roundToInt()}g",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    text = stringResource(R.string.label_fat),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun ChartSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SummaryCard(
    weeklyData: List<DayMacros>,
    goalCalories: Int
) {
    val daysWithData = remember(weeklyData) {
        weeklyData.filter { it.calories > 0 }
    }
    val avgCalories = remember(daysWithData) {
        if (daysWithData.isEmpty()) 0.0
        else daysWithData.sumOf { it.calories } / daysWithData.size
    }
    val bestDay = remember(daysWithData) {
        daysWithData.minByOrNull { kotlin.math.abs(it.calories - goalCalories) }
    }
    val worstDay = remember(daysWithData) {
        daysWithData.maxByOrNull { kotlin.math.abs(it.calories - goalCalories) }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.analytics_summary),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            SummaryRow(
                label = stringResource(R.string.analytics_avg_calories),
                value = "${avgCalories.roundToInt()} / $goalCalories ${stringResource(R.string.label_kcal)}"
            )

            bestDay?.let { day ->
                Spacer(modifier = Modifier.height(4.dp))
                SummaryRow(
                    label = stringResource(R.string.analytics_best_day),
                    value = "${day.date.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() }} — ${day.calories.roundToInt()} ${stringResource(R.string.label_kcal)}"
                )
            }

            worstDay?.let { day ->
                Spacer(modifier = Modifier.height(4.dp))
                SummaryRow(
                    label = stringResource(R.string.analytics_worst_day),
                    value = "${day.date.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() }} — ${day.calories.roundToInt()} ${stringResource(R.string.label_kcal)}"
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
