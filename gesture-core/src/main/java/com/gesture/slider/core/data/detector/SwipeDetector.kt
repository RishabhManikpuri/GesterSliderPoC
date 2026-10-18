package com.gesture.slider.core.data.detector

import com.gesture.slider.core.domain.entity.GestureState
import com.gesture.slider.core.domain.entity.HandLandmark
import com.gesture.slider.core.domain.entity.LandmarkIndex
import kotlin.math.abs

internal class SwipeDetector(
    private val swipeThreshold: Float = 0.2f, // Reduced to 20%
    private val maxWindowMs: Long = 300L, // Reduced from 500
    private val minWindowMs: Long = 50L, // Reduced from 100 
    private val debounceMs: Long = 400L // Reduced from 1000
) {
    private data class PointRecord(val x: Float, val y: Float, val timestamp: Long)
    private val history = mutableListOf<PointRecord>()
    private var lastSwipeTime = 0L

    fun process(landmarks: List<HandLandmark>, currentTimeMs: Long): GestureState? {
        if (landmarks.isEmpty() || landmarks.size <= LandmarkIndex.WRIST) {
            history.clear()
            return null
        }

        if (currentTimeMs - lastSwipeTime < debounceMs) {
            history.clear() // Clear to prevent buildup during debounce
            return null
        }

        val wrist = landmarks[LandmarkIndex.WRIST] // Using wrist center for stable gesture detection
        history.add(PointRecord(wrist.x, wrist.y, currentTimeMs))

        // Remove old history points to keep window tight
        history.removeAll { currentTimeMs - it.timestamp > maxWindowMs }

        if (history.size < 2) return null

        val oldest = history.first()
        val newest = history.last()
        val timeDiff = newest.timestamp - oldest.timestamp

        // If enough time has passed to qualify taking a solid difference, compute diff
        if (timeDiff in minWindowMs..maxWindowMs) {
            val dx = newest.x - oldest.x
            val dy = newest.y - oldest.y

            if (abs(dx) > swipeThreshold || abs(dy) > swipeThreshold) {
                lastSwipeTime = currentTimeMs
                history.clear()

                val direction = if (abs(dx) > abs(dy)) {
                    if (dx > 0) "RIGHT" else "LEFT"
                } else {
                    if (dy > 0) "DOWN" else "UP"
                }
                
                return GestureState.Swiping(direction)
            }
        }

        return null
    }
}
