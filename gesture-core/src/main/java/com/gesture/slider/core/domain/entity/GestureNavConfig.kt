package com.gesture.slider.core.domain.entity

data class GestureNavConfig(
    val pinchThreshold: Float = 0.06f,
    val dragActivationMs: Long = 300L,
    val smoothingWindowSize: Int = 5,
    val confidenceThreshold: Float = 0.65f,
    val modelPath: String = "hand_landmarker.task"
)
