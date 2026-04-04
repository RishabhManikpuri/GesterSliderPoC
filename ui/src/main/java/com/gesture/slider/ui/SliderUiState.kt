package com.gesture.slider.ui

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
    val gestureLabel: String = "Show your hand"
)
