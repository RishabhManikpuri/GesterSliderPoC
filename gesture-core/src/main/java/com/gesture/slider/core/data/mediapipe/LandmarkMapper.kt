package com.gesture.slider.core.data.mediapipe

import com.gesture.slider.core.domain.entity.HandLandmark
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult

internal class LandmarkMapper {
    fun map(result: HandLandmarkerResult): List<HandLandmark> {
        val hands = result.landmarks()
        if (hands.isEmpty()) {
            return emptyList()
        }
        
        // Take the first hand
        val firstHand = hands[0]
        
        return firstHand.mapIndexed { index, normalizedLandmark ->
            HandLandmark(
                index = index,
                x = 1f - normalizedLandmark.x(), // Mirror X for front camera
                y = normalizedLandmark.y(),
                z = normalizedLandmark.z()
            )
        }
    }
}
