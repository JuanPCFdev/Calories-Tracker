package com.juanpcf.caloriestracker.core.designsystem.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.juanpcf.caloriestracker.core.designsystem.theme.MacroColors

/**
 * Canvas-drawn donut/ring chart composable for displaying calorie or macro progress.
 *
 * The progress arc animates from 0° to the target sweep angle on first composition
 * using an 800ms tween. Center content is rendered via an optional composable lambda.
 *
 * @param consumed Current consumed value (calories or grams).
 * @param goal     Target value. When 0, only the track arc is shown — no crash.
 * @param modifier Modifier applied to the outer Box.
 * @param ringColor Color of the progress arc. Defaults to MacroColors.protein (green).
 * @param trackColor Color of the background track arc. Defaults to a 15% opacity of ringColor.
 * @param strokeWidth Width of the ring stroke.
 * @param centerContent Optional composable rendered centered inside the ring.
 */
@Composable
fun CalorieRingChart(
    consumed: Double,
    goal: Double,
    modifier: Modifier = Modifier,
    ringColor: Color = MacroColors.protein,
    trackColor: Color = ringColor.copy(alpha = 0.15f),
    strokeWidth: Dp = 12.dp,
    centerContent: @Composable (BoxScope.() -> Unit)? = null
) {
    val progress = (consumed / goal.coerceAtLeast(1.0)).toFloat().coerceIn(0f, 1f)

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "ring_progress"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .aspectRatio(1f)
        ) {
            val strokeWidthPx = strokeWidth.toPx()
            val inset = strokeWidthPx / 2f
            val arcSize = androidx.compose.ui.geometry.Size(
                width = size.width - inset * 2,
                height = size.height - inset * 2
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

            // Progress arc — animated
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
        }

        if (centerContent != null) {
            centerContent()
        }
    }
}
