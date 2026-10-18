package com.gesture.slider.ui

import com.gesture.slider.core.domain.entity.HandLandmark

enum class CursorMode { POINTER, PINCH, DRAGGING }

data class SliderUiState(
    val sliderValue: Float = 0.5f,
    val cursorX: Float = 0f,
    val cursorY: Float = 0f,
    val cursorVisible: Boolean = false,
    val cursorMode: CursorMode = CursorMode.POINTER,
    val isDragging: Boolean = false,
    val clickPulse: Int = 0,
    val trailPoints: List<Pair<Float, Float>> = emptyList(),
    val gestureLabel: String = "Show your hand",
    val gridRow: Int = 1,
    val gridCol: Int = 1,
    val rawLandmarks: List<HandLandmark> = emptyList()
)
