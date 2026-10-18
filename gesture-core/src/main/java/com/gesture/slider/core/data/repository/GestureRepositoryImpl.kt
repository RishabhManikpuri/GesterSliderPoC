package com.gesture.slider.core.data.repository

import android.content.Context
import androidx.camera.core.Preview
import androidx.lifecycle.LifecycleOwner
import com.gesture.slider.core.data.camera.CameraManager
import com.gesture.slider.core.data.detector.PinchDetector
import com.gesture.slider.core.data.mediapipe.HandLandmarkerHelper
import com.gesture.slider.core.data.mediapipe.LandmarkMapper
import com.gesture.slider.core.domain.entity.GestureNavConfig
import com.gesture.slider.core.domain.entity.GestureState
import com.gesture.slider.core.domain.entity.HandLandmark
import com.gesture.slider.core.domain.repository.GestureRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

internal class GestureRepositoryImpl @Inject constructor(
    private val context: Context,
    private val handLandmarkerHelper: HandLandmarkerHelper,
    private val landmarkMapper: LandmarkMapper,
    private val pinchDetector: PinchDetector,
    private val swipeDetector: com.gesture.slider.core.data.detector.SwipeDetector,
    private val config: GestureNavConfig
) : GestureRepository {

    private val isProcessing = AtomicBoolean(false)
    private val repositoryScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _gestureStateFlow = MutableSharedFlow<GestureState>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
    )
    
    private val _rawLandmarkFlow = MutableSharedFlow<List<HandLandmark>>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
    )

    private val cameraManager = CameraManager(context) { imageProxy ->
        // Drop frames if MediaPipe is busy
        if (isProcessing.compareAndSet(false, true)) {
            // Processing happens in the HandLandmarkerHelper which is async
            handLandmarkerHelper.processFrame(imageProxy)
        } else {
            imageProxy.close()
        }
    }

    init {
        _gestureStateFlow.tryEmit(GestureState.NoHand)
        
        // Listen to MediaPipe results
        repositoryScope.launch {
            handLandmarkerHelper.resultsFlow.collect { result ->
                isProcessing.set(false) // Release the gate as MediaPipe completed processing
                
                val mappedLandmarks = landmarkMapper.map(result)
                _rawLandmarkFlow.tryEmit(mappedLandmarks)
                
                val currentTime = System.currentTimeMillis()
                
                val swipeState = swipeDetector.process(mappedLandmarks, currentTime)
                if (swipeState != null) {
                    _gestureStateFlow.tryEmit(swipeState)
                } else {
                    val nextState = pinchDetector.process(mappedLandmarks, currentTime)
                    _gestureStateFlow.tryEmit(nextState)
                }
            }
        }
        
        repositoryScope.launch {
            handLandmarkerHelper.errorsFlow.collect { errorMsg ->
                isProcessing.set(false) // Release the gate on error
                // Could emit an error state here, but logging is fine for PoC
            }
        }
    }

    override fun getPreviewUseCase(): Preview = cameraManager.previewUseCase

    override fun gestureStateStream(): Flow<GestureState> = _gestureStateFlow.asSharedFlow()

    @OptIn(FlowPreview::class)
    override fun rawLandmarkStream(): Flow<List<HandLandmark>> = _rawLandmarkFlow.asSharedFlow().sample(16)

    override fun start(lifecycleOwner: LifecycleOwner) {
        cameraManager.startCamera(lifecycleOwner)
    }

    override fun stop() {
        cameraManager.stopCamera()
    }
}
