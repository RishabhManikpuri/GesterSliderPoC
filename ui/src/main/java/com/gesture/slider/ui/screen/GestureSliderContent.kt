package com.gesture.slider.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GestureSliderContent(
    sliderValue: Float,
    isDragging: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "${(sliderValue * 100).toInt()}",
            fontSize = 80.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Text(
            text = if (isDragging) "Dragging..." else "Gesture Slider",
            fontSize = 24.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        GestureSliderWidget(
            value = sliderValue,
            isDragging = isDragging,
            modifier = Modifier.fillMaxWidth(0.8f)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        GestureGuideRow()
    }
}

@Composable
fun GestureGuideRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Text("☝\uFE0F Point \u2192 Cursor", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
        Text("\uD83E\uDD0C Pinch \u2192 Click", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
        Text("\uD83E\uDEF3 Hold \u2192 Slide", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
    }
}
