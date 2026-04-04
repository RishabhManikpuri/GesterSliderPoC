package com.gesture.slider.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.gesture.slider.ui.SliderViewModel

@Composable
fun GestureSliderScreen(
    viewModel: SliderViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                viewModel.gestureRepository.start(lifecycleOwner)
            } else if (event == Lifecycle.Event.ON_STOP) {
                viewModel.gestureRepository.stop()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.gestureRepository.stop()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Layer 1: Camera
        CameraPreviewLayer(
            previewUseCase = viewModel.gestureRepository.getPreviewUseCase(),
            modifier = Modifier.fillMaxSize()
        )

        // Layer 2: Scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
        )

        // Layer 3: Content
        GestureSliderContent(
            sliderValue = uiState.sliderValue,
            isDragging = uiState.isDragging,
            modifier = Modifier.fillMaxSize()
        )

        // Layer 4: Overlay
        if (uiState.cursorVisible) {
            GestureCursorOverlay(
                cursorX = uiState.cursorX,
                cursorY = uiState.cursorY,
                cursorMode = uiState.cursorMode,
                clickPulse = uiState.clickPulse,
                trailPoints = uiState.trailPoints,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Layer 5: Label
        GestureStatusLabel(
            text = uiState.gestureLabel,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp)
        )
    }
}
