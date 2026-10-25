package com.vrddhih.app.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vrddhih.app.core.theme.VrddhihBrushes
import com.vrddhih.app.core.theme.VrddhihColors
import com.vrddhih.app.core.theme.VrddhihDimens

private const val PROGRESS_ANIMATION_MILLIS = 600

@Composable
fun SystemProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = VrddhihDimens.ProgressBarHeight,
    brush: Brush = VrddhihBrushes.Energy,
) {
    val target = progress.coerceIn(0f, 1f)
    val animated by animateFloatAsState(target, tween(PROGRESS_ANIMATION_MILLIS), label = "progressBar")
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(CircleShape)
            .background(VrddhihColors.Outline)
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(target, 0f..1f) },
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animated)
                .clip(CircleShape)
                .background(brush),
        )
    }
}

@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    diameter: Dp = 88.dp,
    strokeWidth: Dp = 8.dp,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val target = progress.coerceIn(0f, 1f)
    val animated by animateFloatAsState(target, tween(PROGRESS_ANIMATION_MILLIS), label = "progressRing")
    Box(
        modifier = modifier
            .size(diameter)
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(target, 0f..1f) },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val stroke = strokeWidth.toPx()
            val topLeft = Offset(stroke / 2, stroke / 2)
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(
                color = VrddhihColors.Outline,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke),
            )
            drawArc(
                brush = Brush.linearGradient(listOf(VrddhihColors.Electric, VrddhihColors.Ice)),
                startAngle = -90f,
                sweepAngle = 360f * animated,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
        }
        content()
    }
}
