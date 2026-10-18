package com.gesture.slider.core.domain.repository

import android.content.Context
import com.gesture.slider.core.data.detector.PinchDetector
import com.gesture.slider.core.data.mediapipe.HandLandmarkerHelper
import com.gesture.slider.core.data.mediapipe.LandmarkMapper
import com.gesture.slider.core.data.repository.GestureRepositoryImpl
import com.gesture.slider.core.domain.entity.GestureNavConfig

object GestureCoreFactory {
    fun createRepository(
        context: Context,
        config: GestureNavConfig
    ): GestureRepository {
        return GestureRepositoryImpl(
            context,
            HandLandmarkerHelper(context, config),
            LandmarkMapper(),
            PinchDetector(config.pinchThreshold, config.dragActivationMs),
            com.gesture.slider.core.data.detector.SwipeDetector(),
            config
        )
    }
}
