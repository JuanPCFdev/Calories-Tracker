package com.juanpcf.caloriestracker.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.juanpcf.caloriestracker.R
import com.juanpcf.caloriestracker.core.designsystem.theme.MacroColors
import java.util.Locale

/**
 * ElevatedCard displaying a calorie headline and 3 horizontal macro progress bars.
 *
 * @param calories      Total calories consumed.
 * @param caloriesGoal  Daily calorie goal (for future use — not shown as bar, only as context).
 * @param protein       Protein consumed in grams.
 * @param proteinGoal   Protein goal in grams.
 * @param carbs         Carbs consumed in grams.
 * @param carbsGoal     Carbs goal in grams.
 * @param fat           Fat consumed in grams.
 * @param fatGoal       Fat goal in grams.
 * @param modifier      Modifier applied to the card.
 * @param title         Optional card title override. If null, defaults to "ESTIMATED ENERGY".
 */
@Composable
fun NutritionBarsCard(
    calories: Double,
    caloriesGoal: Double,
    protein: Double,
    proteinGoal: Double,
    carbs: Double,
    carbsGoal: Double,
    fat: Double,
    fatGoal: Double,
    modifier: Modifier = Modifier,
    title: String? = null
) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Card title
            Text(
                text = title ?: "ESTIMATED ENERGY",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Calorie headline
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "${calories.toInt()}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = " kcal",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Macro progress bars
            MacroBar(
                label = stringResource(R.string.label_protein),
                value = protein,
                goal = proteinGoal,
                color = MacroColors.protein,
                trackColor = MacroColors.proteinTrack
            )

            Spacer(modifier = Modifier.height(8.dp))

            MacroBar(
                label = stringResource(R.string.label_carbs),
                value = carbs,
                goal = carbsGoal,
                color = MacroColors.carbs,
                trackColor = MacroColors.carbsTrack
            )

            Spacer(modifier = Modifier.height(8.dp))

            MacroBar(
                label = stringResource(R.string.label_fat),
                value = fat,
                goal = fatGoal,
                color = MacroColors.fat,
                trackColor = MacroColors.fatTrack
            )
        }
    }
}

@Composable
private fun MacroBar(
    label: String,
    value: Double,
    goal: Double,
    color: Color,
    trackColor: Color
) {
    val progress = (value / goal.coerceAtLeast(1.0)).toFloat().coerceIn(0f, 1f)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .weight(2f)
                .clip(CircleShape),
            color = color,
            trackColor = trackColor
        )

        val formatted = if (value == value.toLong().toDouble()) value.toLong().toString() else String.format(Locale.US, "%.1f", value)
        Text(
            text = "${formatted}g",
            style = MaterialTheme.typography.bodyMedium,
            color = color
        )
    }
}
