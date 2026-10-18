package com.gesture.slider.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gesture.slider.ui.SliderViewModel
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.border

import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap

val HAND_CONNECTIONS = listOf(
    // Palm
    0 to 1, 0 to 5, 0 to 17, 5 to 9, 9 to 13, 13 to 17,
    // Thumb
    1 to 2, 2 to 3, 3 to 4,
    // Index finger
    5 to 6, 6 to 7, 7 to 8,
    // Middle finger
    9 to 10, 10 to 11, 11 to 12,
    // Ring finger
    13 to 14, 14 to 15, 15 to 16,
    // Pinky
    17 to 18, 18 to 19, 19 to 20
)

@Composable
fun SwipeGestureScreen(
    viewModel: SliderViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Layer 1: Camera
        CameraPreviewLayer(
            previewUseCase = viewModel.gestureRepository.getPreviewUseCase(),
            modifier = Modifier.fillMaxSize()
        )

        // Layer 2: Hand Skeleton Overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val landmarks = uiState.rawLandmarks

            if (landmarks.isNotEmpty()) {
                // Draw lines
                for (connection in HAND_CONNECTIONS) {
                    if (connection.first < landmarks.size && connection.second < landmarks.size) {
                        val start = landmarks[connection.first]
                        val end = landmarks[connection.second]

                        drawLine(
                            color = Color.Green,
                            start = Offset(start.x * canvasWidth, start.y * canvasHeight),
                            end = Offset(end.x * canvasWidth, end.y * canvasHeight),
                            strokeWidth = 4.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }

                // Draw points
                for (landmark in landmarks) {
                    drawCircle(
                        color = Color.Red,
                        radius = 6.dp.toPx(),
                        center = Offset(landmark.x * canvasWidth, landmark.y * canvasHeight)
                    )
                }
            }
        }

        // Layer 3: Scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
        )

        // Layer 4: Content (3x3 Grid)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            for (row in 0..2) {
                Row {
                    for (col in 0..2) {
                        val isSelected = (row == uiState.gridRow && col == uiState.gridCol)
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .size(80.dp)
                                .background(if (isSelected) Color.Green else Color.DarkGray)
                                .border(2.dp, Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Text("V", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                            }
                        }
                    }
                }
            }

            Button(onClick = onNavigateBack, modifier = Modifier.padding(top = 32.dp)) {
                Text("Back to Slider")
            }
        }
    }
}
