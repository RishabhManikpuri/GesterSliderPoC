package com.gesture.slider.ui.screen

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.gesture.slider.ui.CursorMode

@Composable
fun GestureCursorOverlay(
    cursorX: Float,
    cursorY: Float,
    cursorMode: CursorMode,
    clickPulse: Int,
    trailPoints: List<Pair<Float, Float>>,
    modifier: Modifier = Modifier
) {
    val clickScale = remember { androidx.compose.animation.core.Animatable(1f) }

    LaunchedEffect(clickPulse) {
        if (clickPulse > 0) {
            clickScale.snapTo(1f)
            clickScale.animateTo(1.8f, tween(75))
            clickScale.animateTo(1f, tween(75))
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val cx = cursorX * size.width
        val cy = cursorY * size.height
        val cursorColor = when (cursorMode) {
            CursorMode.POINTER -> Color.White
            CursorMode.PINCH -> Color(0xFFFFEB3B)
            CursorMode.DRAGGING -> Color(0xFFFF9800)
        }

        // Draw trail
        if (cursorMode == CursorMode.DRAGGING) {
            trailPoints.forEachIndexed { index, point ->
                val tx = point.first * size.width
                val ty = point.second * size.height
                val alpha = (index.toFloat() / trailPoints.size) * 0.4f
                drawCircle(
                    color = cursorColor.copy(alpha = alpha),
                    radius = 6f,
                    center = Offset(tx, ty)
                )
            }
        }

        // Draw outer glow
        val glowRadius = 36f * pulseGlow * clickScale.value
        drawCircle(
            color = cursorColor.copy(alpha = 0.3f),
            radius = glowRadius,
            center = Offset(cx, cy)
        )

        // Draw main cursor dot
        drawCircle(
            color = cursorColor,
            radius = 14f * clickScale.value,
            center = Offset(cx, cy)
        )

        // Draw inner white dot
        drawCircle(
            color = Color.White,
            radius = 5f * clickScale.value,
            center = Offset(cx, cy)
        )
    }
}
