package com.juanpcf.caloriestracker.feature.analytics.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.juanpcf.caloriestracker.core.designsystem.theme.MacroColors
import com.juanpcf.caloriestracker.domain.model.DayMacros
import java.time.format.TextStyle as DateTextStyle
import java.util.Locale
import kotlin.math.max

/**
 * Canvas-drawn calorie trend line chart.
 * Replaces CalorieBarChart in the AnalyticsScreen layout.
 *
 * Draws a single calorie series as a connected line with dot markers,
 * plus an optional dashed goal line.
 *
 * @param data          List of DayMacros providing calorie values per day.
 * @param goalCalories  Daily calorie goal — drawn as a dashed horizontal line.
 * @param modifier      Modifier applied to the Canvas.
 */
@Composable
fun CalorieTrendLineChart(
    data: List<DayMacros>,
    goalCalories: Int,
    modifier: Modifier = Modifier
) {
    val lineColor = MacroColors.protein
    val goalColor = MaterialTheme.colorScheme.error
    val outlineVariantColor = MaterialTheme.colorScheme.outlineVariant
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textMeasurer = rememberTextMeasurer()

    val labelStyle = TextStyle(
        fontSize = 10.sp,
        color = onSurfaceVariantColor
    )

    val maxValue = remember(data, goalCalories) {
        max(
            goalCalories.toDouble(),
            data.maxOfOrNull { it.calories } ?: goalCalories.toDouble()
        ).coerceAtLeast(1.0) * 1.1 // 10% headroom above max
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        if (data.isEmpty()) return@Canvas

        val paddingTop = 24.dp.toPx()
        val paddingBottom = 32.dp.toPx()
        val paddingHorizontal = 16.dp.toPx()

        val chartWidth = size.width - paddingHorizontal * 2
        val chartHeight = size.height - paddingTop - paddingBottom
        val chartBottom = size.height - paddingBottom

        // Evenly space points across chart width
        val pointSpacing = if (data.size > 1) chartWidth / (data.size - 1).toFloat() else 0f

        fun xForIndex(index: Int): Float =
            paddingHorizontal + index * pointSpacing

        fun yForValue(value: Double): Float =
            chartBottom - (chartHeight * (value / maxValue)).toFloat()

        // Draw subtle horizontal grid lines
        val gridCount = 4
        for (i in 0..gridCount) {
            val y = chartBottom - chartHeight * (i.toFloat() / gridCount)
            drawLine(
                color = outlineVariantColor.copy(alpha = 0.3f),
                start = Offset(paddingHorizontal, y),
                end = Offset(size.width - paddingHorizontal, y),
                strokeWidth = 0.5.dp.toPx()
            )
        }

        // Draw goal line (dashed)
        val goalY = yForValue(goalCalories.toDouble())
        drawLine(
            color = goalColor.copy(alpha = 0.6f),
            start = Offset(paddingHorizontal, goalY),
            end = Offset(size.width - paddingHorizontal, goalY),
            strokeWidth = 1.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
        )

        // Draw calorie line
        if (data.size > 1) {
            val path = Path()
            data.forEachIndexed { index, day ->
                val x = xForIndex(index)
                val y = yForValue(day.calories)
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(
                    width = 2.5.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }

        // Draw dot markers and day labels
        data.forEachIndexed { index, day ->
            val x = xForIndex(index)
            val y = yForValue(day.calories)

            // Dot
            if (day.calories > 0) {
                drawCircle(
                    color = lineColor,
                    radius = 4.dp.toPx(),
                    center = Offset(x, y)
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.dp.toPx(),
                    center = Offset(x, y)
                )
            }

            // Day label below
            val dayLabel = day.date.dayOfWeek.getDisplayName(DateTextStyle.SHORT, Locale.getDefault())
            val measured = textMeasurer.measure(dayLabel, labelStyle)
            drawText(
                textLayoutResult = measured,
                topLeft = Offset(
                    x = x - measured.size.width / 2f,
                    y = chartBottom + 4.dp.toPx()
                )
            )
        }
    }
}
