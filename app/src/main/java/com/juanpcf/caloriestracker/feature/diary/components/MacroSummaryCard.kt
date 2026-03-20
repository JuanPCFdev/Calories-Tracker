package com.juanpcf.caloriestracker.feature.diary.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.juanpcf.caloriestracker.R
import com.juanpcf.caloriestracker.core.designsystem.theme.MacroColors
import com.juanpcf.caloriestracker.domain.model.MacroTotals
import com.juanpcf.caloriestracker.domain.model.UserGoals

@Composable
fun MacroSummaryCard(
    totals: MacroTotals,
    goals: UserGoals?,
    modifier: Modifier = Modifier
) {
    val caloriesTarget = goals?.dailyCalories?.toDouble() ?: 0.0
    val proteinTarget = goals?.dailyProtein?.toDouble() ?: 0.0
    val carbsTarget = goals?.dailyCarbs?.toDouble() ?: 0.0
    val fatTarget = goals?.dailyFat?.toDouble() ?: 0.0

    ElevatedCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.label_calories),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "${totals.calories.toInt()} / ${caloriesTarget.toInt()} kcal",
                style = MaterialTheme.typography.headlineMedium,
                color = if (totals.calories > caloriesTarget && caloriesTarget > 0.0) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
            LinearProgressIndicator(
                progress = { (totals.calories / caloriesTarget.coerceAtLeast(1.0)).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row {
                MacroProgressBar(
                    label = stringResource(R.string.label_protein),
                    current = totals.protein,
                    target = proteinTarget,
                    color = MacroColors.protein,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.weight(0.05f))
                MacroProgressBar(
                    label = stringResource(R.string.label_carbs),
                    current = totals.carbs,
                    target = carbsTarget,
                    color = MacroColors.carbs,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.weight(0.05f))
                MacroProgressBar(
                    label = stringResource(R.string.label_fat),
                    current = totals.fat,
                    target = fatTarget,
                    color = MacroColors.fat,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
