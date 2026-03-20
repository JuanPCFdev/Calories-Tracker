package com.juanpcf.caloriestracker.feature.analytics.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.juanpcf.caloriestracker.R
import com.juanpcf.caloriestracker.core.designsystem.theme.MacroColors
import com.juanpcf.caloriestracker.domain.model.DayMacros
import kotlin.math.max

@Composable
fun MacroLineChart(
    data: List<DayMacros>,
    modifier: Modifier = Modifier
) {
    val proteinColor = MacroColors.protein
    val carbsColor = MacroColors.carbs
    val fatColor = MacroColors.fat
    val outlineVariantColor = MaterialTheme.colorScheme.outlineVariant

    val maxValue = remember(data) {
        val maxInData = data.maxOfOrNull { maxOf(it.protein, it.carbs, it.fat) } ?: 0.0
        (maxInData * 1.2).coerceAtLeast(10.0)
    }

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            if (data.isEmpty()) return@Canvas

            val paddingTop = 12.dp.toPx()
            val paddingBottom = 24.dp.toPx()
            val paddingHorizontal = 12.dp.toPx()

            val chartWidth = size.width - paddingHorizontal * 2
            val chartHeight = size.height - paddingTop - paddingBottom
            val chartBottom = size.height - paddingBottom

            val pointSpacing = if (data.size > 1) chartWidth / (data.size - 1).toFloat() else chartWidth

            fun xForIndex(index: Int): Float =
                paddingHorizontal + index * pointSpacing

            fun yForValue(value: Double): Float =
                chartBottom - (chartHeight * (value / maxValue)).toFloat()

            // Draw subtle horizontal grid lines
            val gridLineCount = 4
            for (i in 0..gridLineCount) {
                val y = chartBottom - chartHeight * (i.toFloat() / gridLineCount)
                drawLine(
                    color = outlineVariantColor.copy(alpha = 0.3f),
                    start = Offset(paddingHorizontal, y),
                    end = Offset(size.width - paddingHorizontal, y),
                    strokeWidth = 0.5.dp.toPx()
                )
            }

            // Helper to draw a line series
            fun drawLineSeries(
                values: List<Double>,
                color: Color
            ) {
                if (values.isEmpty()) return
                val path = Path()
                var firstPoint = true

                values.forEachIndexed { index, value ->
                    val x = xForIndex(index)
                    val y = yForValue(value)
                    if (firstPoint) {
                        path.moveTo(x, y)
                        firstPoint = false
                    } else {
                        path.lineTo(x, y)
                    }
                }

                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Draw points
                values.forEachIndexed { index, value ->
                    drawCircle(
                        color = color,
                        radius = 3.5.dp.toPx(),
                        center = Offset(xForIndex(index), yForValue(value))
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 1.5.dp.toPx(),
                        center = Offset(xForIndex(index), yForValue(value))
                    )
                }
            }

            drawLineSeries(data.map { it.fat }, fatColor)
            drawLineSeries(data.map { it.carbs }, carbsColor)
            drawLineSeries(data.map { it.protein }, proteinColor)
        }

        // Legend
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LegendItem(color = proteinColor, label = stringResource(R.string.label_protein))
            LegendItem(color = carbsColor, label = stringResource(R.string.label_carbs))
            LegendItem(color = fatColor, label = stringResource(R.string.label_fat))
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(10.dp)) {
            drawCircle(color = color)
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
