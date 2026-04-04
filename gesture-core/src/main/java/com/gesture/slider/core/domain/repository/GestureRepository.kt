package com.gesture.slider.core.domain.repository

import androidx.lifecycle.LifecycleOwner
import com.gesture.slider.core.domain.entity.GestureState
import com.gesture.slider.core.domain.entity.HandLandmark
import kotlinx.coroutines.flow.Flow

interface GestureRepository {
    fun gestureStateStream(): Flow<GestureState>
    fun rawLandmarkStream(): Flow<List<HandLandmark>>
    fun getPreviewUseCase(): androidx.camera.core.Preview
    fun start(lifecycleOwner: LifecycleOwner)
    fun stop()
}
