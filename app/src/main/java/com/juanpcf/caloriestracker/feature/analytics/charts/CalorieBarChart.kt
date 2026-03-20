package com.juanpcf.caloriestracker.feature.analytics.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.juanpcf.caloriestracker.domain.model.DayMacros
import java.time.format.TextStyle as DateTextStyle
import java.util.Locale
import kotlin.math.max

@Deprecated(
    message = "Replaced by CalorieTrendLineChart. AnalyticsScreen now uses CalorieTrendLineChart for calorie trend visualization.",
    replaceWith = ReplaceWith(
        expression = "CalorieTrendLineChart(data = data, goalCalories = goalCalories, modifier = modifier)",
        imports = ["com.juanpcf.caloriestracker.feature.analytics.charts.CalorieTrendLineChart"]
    )
)
@Composable
fun CalorieBarChart(
    data: List<DayMacros>,
    goalCalories: Int,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error
    val outlineVariantColor = MaterialTheme.colorScheme.outlineVariant
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textMeasurer = rememberTextMeasurer()

    val labelStyle = TextStyle(
        fontSize = 10.sp,
        color = onSurfaceVariantColor
    )
    val valueLabelStyle = TextStyle(
        fontSize = 9.sp,
        color = onSurfaceVariantColor
    )

    val maxValue = remember(data, goalCalories) {
        max(goalCalories.toDouble(), data.maxOfOrNull { it.calories } ?: goalCalories.toDouble())
            .coerceAtLeast(1.0)
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        if (data.isEmpty()) return@Canvas

        val paddingTop = 24.dp.toPx()
        val paddingBottom = 32.dp.toPx()
        val paddingHorizontal = 8.dp.toPx()

        val chartWidth = size.width - paddingHorizontal * 2
        val chartHeight = size.height - paddingTop - paddingBottom
        val chartTop = paddingTop
        val chartBottom = size.height - paddingBottom

        val barCount = data.size
        val totalBarWidth = chartWidth / barCount
        val barWidth = totalBarWidth * 0.6f
        val barSpacing = totalBarWidth * 0.4f / 2f

        // Goal line Y position
        val goalY = chartTop + chartHeight * (1f - (goalCalories.toDouble() / maxValue).toFloat())

        // Draw goal line (dashed)
        drawLine(
            color = errorColor.copy(alpha = 0.5f),
            start = Offset(paddingHorizontal, goalY),
            end = Offset(size.width - paddingHorizontal, goalY),
            strokeWidth = 1.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
        )

        // Draw bars
        data.forEachIndexed { index, dayMacros ->
            val barLeft = paddingHorizontal + index * totalBarWidth + barSpacing
            val barHeightRatio = (dayMacros.calories / maxValue).toFloat().coerceIn(0f, 1f)
            val barHeight = chartHeight * barHeightRatio
            val barTop = chartBottom - barHeight

            val barColor = if (dayMacros.calories > goalCalories) errorColor else primaryColor

            if (dayMacros.calories > 0) {
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(barLeft, barTop),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )

                // Value label above bar
                val valueText = "${dayMacros.calories.toInt()}"
                val measured = textMeasurer.measure(valueText, valueLabelStyle)
                val labelX = barLeft + barWidth / 2f - measured.size.width / 2f
                val labelY = barTop - measured.size.height - 2.dp.toPx()
                if (labelY > 0) {
                    drawText(
                        textLayoutResult = measured,
                        topLeft = Offset(labelX, labelY)
                    )
                }
            } else {
                // Empty bar placeholder
                drawRoundRect(
                    color = outlineVariantColor.copy(alpha = 0.3f),
                    topLeft = Offset(barLeft, chartBottom - 4.dp.toPx()),
                    size = Size(barWidth, 4.dp.toPx()),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )
            }

            // Day label below bar
            val dayLabel = dayMacros.date.dayOfWeek.getDisplayName(DateTextStyle.SHORT, Locale.getDefault())
            val measured = textMeasurer.measure(dayLabel, labelStyle)
            val labelX = barLeft + barWidth / 2f - measured.size.width / 2f
            drawText(
                textLayoutResult = measured,
                topLeft = Offset(labelX, chartBottom + 4.dp.toPx())
            )
        }
    }
}
