package com.gesture.slider.core.domain.entity

sealed class GestureState {
    object NoHand : GestureState()

    data class Pointing(
        val cursorX: Float,
        val cursorY: Float,
        val confidence: Float
    ) : GestureState()

    data class PinchDetected(
        val cursorX: Float,
        val cursorY: Float,
        val pinchStrength: Float,
        val pinchStartTime: Long
    ) : GestureState()

    data class Dragging(
        val cursorX: Float,
        val cursorY: Float,
        val dragDeltaX: Float,
        val dragStartX: Float,
        val trailPoints: List<Pair<Float, Float>>
    ) : GestureState()

    data class PinchClick(
        val cursorX: Float,
        val cursorY: Float
    ) : GestureState()

    data class Swiping(
        val direction: String
    ) : GestureState()
}
