package com.gesture.slider.ui.screen

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun GestureSliderWidget(
    value: Float,
    isDragging: Boolean,
    modifier: Modifier = Modifier
) {
    val thumbSizeDp by animateDpAsState(
        targetValue = if (isDragging) 32.dp else 24.dp,
        label = "thumb_size"
    )

    Box(modifier = modifier.fillMaxWidth().height(48.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxWidth().height(48.dp)) {
            val trackHeight = 8.dp.toPx()
            val thumbRadius = thumbSizeDp.toPx() / 2f
            
            // Background track
            drawRoundRect(
                color = Color.White.copy(alpha = 0.3f),
                topLeft = Offset(0f, center.y - trackHeight / 2f),
                size = Size(size.width, trackHeight),
                cornerRadius = CornerRadius(trackHeight / 2f, trackHeight / 2f)
            )

            val fillWidth = value * size.width
            val fillColor = if (isDragging) Color(0xFFFF9800) else Color(0xFF2196F3)

            // Fill track
            drawRoundRect(
                color = fillColor,
                topLeft = Offset(0f, center.y - trackHeight / 2f),
                size = Size(fillWidth, trackHeight),
                cornerRadius = CornerRadius(trackHeight / 2f, trackHeight / 2f)
            )

            // Thumb
            val thumbX = (value * size.width).coerceIn(thumbRadius, size.width - thumbRadius)
            
            drawCircle(
                color = Color.White,
                radius = thumbRadius,
                center = Offset(thumbX, center.y),
                style = Fill
            )
            
            // Soft shadow outline
            drawCircle(
                color = Color.Black.copy(alpha = 0.2f),
                radius = thumbRadius,
                center = Offset(thumbX, center.y),
                style = Stroke(width = 2f)
            )
        }
    }
}
