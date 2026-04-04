package com.gesture.slider.core.data.detector

import com.gesture.slider.core.domain.entity.GestureState
import com.gesture.slider.core.domain.entity.HandLandmark
import com.gesture.slider.core.domain.entity.LandmarkIndex
import kotlin.math.pow
import kotlin.math.sqrt

internal class PinchDetector(
    private val pinchThreshold: Float = 0.06f,
    private val dragActivationMs: Long = 300L
) {
    private var pinchStartTime: Long = 0L
    private var pinchStartX: Float = 0f
    private var wasPinched: Boolean = false
    private val trailPoints = ArrayDeque<Pair<Float, Float>>(10)

    fun process(landmarks: List<HandLandmark>, currentTimeMs: Long): GestureState {
        if (landmarks.isEmpty() || landmarks.size <= LandmarkIndex.PINKY_TIP) {
            return GestureState.NoHand
        }

        val thumbTip = landmarks[LandmarkIndex.THUMB_TIP]
        val indexTip = landmarks[LandmarkIndex.INDEX_TIP]
        
        val midX = (thumbTip.x + indexTip.x) / 2f
        val midY = (thumbTip.y + indexTip.y) / 2f
        
        val distance = sqrt(
            (thumbTip.x - indexTip.x).pow(2) +
            (thumbTip.y - indexTip.y).pow(2)
        )
        val isPinched = distance < pinchThreshold

        return when {
            isPinched && !wasPinched -> {
                wasPinched = true
                pinchStartTime = currentTimeMs
                pinchStartX = midX
                trailPoints.clear()
                GestureState.PinchClick(midX, midY)
            }

            isPinched && wasPinched -> {
                val holdDuration = currentTimeMs - pinchStartTime
                if (holdDuration >= dragActivationMs) {
                    if (trailPoints.size >= 10) trailPoints.removeFirst()
                    trailPoints.addLast(Pair(midX, midY))
                    
                    GestureState.Dragging(
                        cursorX = midX,
                        cursorY = midY,
                        dragDeltaX = midX - pinchStartX,
                        dragStartX = pinchStartX,
                        trailPoints = trailPoints.toList()
                    )
                } else {
                    GestureState.PinchDetected(
                        cursorX = midX,
                        cursorY = midY,
                        pinchStrength = (1f - (distance / pinchThreshold)).coerceIn(0f, 1f),
                        pinchStartTime = pinchStartTime
                    )
                }
            }

            else -> {
                wasPinched = false
                pinchStartTime = 0L
                trailPoints.clear()
                classifyPointing(landmarks)
            }
        }
    }

    private fun classifyPointing(landmarks: List<HandLandmark>): GestureState {
        val indexTip   = landmarks[LandmarkIndex.INDEX_TIP]
        val indexPip   = landmarks[LandmarkIndex.INDEX_PIP]
        val middleTip  = landmarks[LandmarkIndex.MIDDLE_TIP]
        val ringTip    = landmarks[LandmarkIndex.RING_TIP]
        val pinkyTip   = landmarks[LandmarkIndex.PINKY_TIP]

        val indexExtended = indexTip.y < indexPip.y
        val othersCurled  = middleTip.y > indexPip.y &&
                            ringTip.y   > indexPip.y &&
                            pinkyTip.y  > indexPip.y

        return if (indexExtended && othersCurled) {
            GestureState.Pointing(indexTip.x, indexTip.y, 0.9f)
        } else {
            GestureState.NoHand
        }
    }
}
