package com.gesture.slider.core.data.mediapipe

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageProxy
import com.gesture.slider.core.domain.entity.GestureNavConfig
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

internal class HandLandmarkerHelper(
    private val context: Context,
    private val config: GestureNavConfig
) {
    private var handLandmarker: HandLandmarker? = null
    
    private val _resultsFlow = MutableSharedFlow<HandLandmarkerResult>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val resultsFlow: SharedFlow<HandLandmarkerResult> = _resultsFlow.asSharedFlow()

    private val _errorsFlow = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val errorsFlow: SharedFlow<String> = _errorsFlow.asSharedFlow()

    init {
        setupHandLandmarker()
    }

    private fun setupHandLandmarker() {
        val baseOptionsBuilder = BaseOptions.builder()
            .setModelAssetPath(config.modelPath)

        // Try to use GPU, fallback to CPU
        try {
            baseOptionsBuilder.setDelegate(Delegate.GPU)
        } catch (e: Exception) {
            baseOptionsBuilder.setDelegate(Delegate.CPU)
        }

        val optionsBuilder = HandLandmarker.HandLandmarkerOptions.builder()
            .setBaseOptions(baseOptionsBuilder.build())
            .setMinHandDetectionConfidence(config.confidenceThreshold)
            .setMinTrackingConfidence(config.confidenceThreshold)
            .setMinHandPresenceConfidence(config.confidenceThreshold)
            .setNumHands(1)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setResultListener(::onResults)
            .setErrorListener(::onError)

        try {
            handLandmarker = HandLandmarker.createFromOptions(context, optionsBuilder.build())
        } catch (e: Exception) {
            _errorsFlow.tryEmit("Hand Landmarker failed to initialize: ${e.message}")
        }
    }

    fun processFrame(imageProxy: ImageProxy) {
        val handLandmarker = this.handLandmarker ?: return
        
        try {
            val bitmapBuffer = Bitmap.createBitmap(
                imageProxy.width,
                imageProxy.height,
                Bitmap.Config.ARGB_8888
            )
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees

            // Convert ImageProxy to Bitmap using MediaPipe utility or standard Android
            // In a real implementation we would convert YUV_420_888 to ARGB_8888. 
            // CameraX 1.3+ offers imageProxy.toBitmap()
            val bitmap = imageProxy.toBitmap()
            
            // Rotate the bitmap if needed based on rotationDegrees
            val matrix = Matrix().apply {
                postRotate(rotationDegrees.toFloat())
            }
            val rotatedBitmap = Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
            )

            val mpImage = BitmapImageBuilder(rotatedBitmap).build()
            val timestamp = imageProxy.imageInfo.timestamp / 1_000_000

            handLandmarker.detectAsync(mpImage, timestamp)
        } catch (e: Exception) {
            _errorsFlow.tryEmit("Error processing frame: ${e.message}")
        } finally {
            imageProxy.close()
        }
    }

    private fun onResults(result: HandLandmarkerResult, mpImage: MPImage) {
        _resultsFlow.tryEmit(result)
    }

    private fun onError(error: RuntimeException) {
        _errorsFlow.tryEmit(error.message ?: "Unknown error")
    }

    fun clear() {
        handLandmarker?.close()
        handLandmarker = null
    }
}
