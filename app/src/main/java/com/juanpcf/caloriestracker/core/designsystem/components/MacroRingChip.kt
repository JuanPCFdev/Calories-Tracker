package com.juanpcf.caloriestracker.core.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Compact donut chip showing a single macro's progress.
 * Used as satellite chips alongside the main CalorieRingChart.
 *
 * Intentionally NOT animated — renders the final value immediately.
 * Spec R-DS-003-7: animation is reserved for the main CalorieRingChart.
 *
 * @param label      Macro name displayed below the ring (e.g., "Protein").
 * @param consumed   Consumed amount in grams.
 * @param goal       Target amount in grams. When 0, only the track arc is shown.
 * @param color      Ring color (pass MacroColors.protein / .carbs / .fat).
 * @param modifier   Modifier applied to the root Column.
 * @param size       Diameter of the donut ring canvas.
 * @param strokeWidth Width of the ring stroke.
 */
@Composable
fun MacroRingChip(
    label: String,
    consumed: Double,
    goal: Double,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    strokeWidth: Dp = 6.dp
) {
    val progress = (consumed / goal.coerceAtLeast(1.0)).toFloat().coerceIn(0f, 1f)
    val trackColor = color.copy(alpha = 0.15f)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val strokeWidthPx = strokeWidth.toPx()
                val inset = strokeWidthPx / 2f
                val arcSize = androidx.compose.ui.geometry.Size(
                    width = this.size.width - inset * 2,
                    height = this.size.height - inset * 2
                )
                val topLeft = androidx.compose.ui.geometry.Offset(inset, inset)

                // Track arc — full circle
                drawArc(
                    color = trackColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )

                // Progress arc — no animation (renders final value immediately)
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = progress * 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )
            }

            // Center text: consumed value in grams
            Text(
                text = "${consumed.toInt()}g",
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Macro name label below the ring
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Goal label
        Text(
            text = "/ ${goal.toInt()}g",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}
