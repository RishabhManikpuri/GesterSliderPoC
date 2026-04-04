package com.gesture.slider.core.data.detector

import com.gesture.slider.core.domain.entity.GestureState
import com.gesture.slider.core.domain.entity.HandLandmark
import com.gesture.slider.core.domain.entity.LandmarkIndex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PinchDetectorTest {

    private lateinit var detector: PinchDetector

    @Before
    fun setup() {
        detector = PinchDetector(pinchThreshold = 0.06f, dragActivationMs = 300L)
    }

    private fun createMockLandmarks(
        thumbX: Float = 0.5f, thumbY: Float = 0.5f,
        indexX: Float = 0.5f, indexY: Float = 0.5f,
        indexPipY: Float = 0.6f,
        middleY: Float = 0.8f, ringY: Float = 0.8f, pinkyY: Float = 0.8f
    ): List<HandLandmark> {
        val list = MutableList(21) { HandLandmark(it, 0f, 0f, 0f) }
        list[LandmarkIndex.THUMB_TIP] = HandLandmark(LandmarkIndex.THUMB_TIP, thumbX, thumbY, 0f)
        list[LandmarkIndex.INDEX_TIP] = HandLandmark(LandmarkIndex.INDEX_TIP, indexX, indexY, 0f)
        list[LandmarkIndex.INDEX_PIP] = HandLandmark(LandmarkIndex.INDEX_PIP, indexX, indexPipY, 0f)
        list[LandmarkIndex.MIDDLE_TIP] = HandLandmark(LandmarkIndex.MIDDLE_TIP, 0.6f, middleY, 0f)
        list[LandmarkIndex.RING_TIP] = HandLandmark(LandmarkIndex.RING_TIP, 0.7f, ringY, 0f)
        list[LandmarkIndex.PINKY_TIP] = HandLandmark(LandmarkIndex.PINKY_TIP, 0.8f, pinkyY, 0f)
        return list
    }

    @Test
    fun `test pointer detected`() {
        val landmarks = createMockLandmarks(
            thumbX = 0f, thumbY = 0f, // far away
            indexX = 0.5f, indexY = 0.2f, indexPipY = 0.5f, // Extended
            middleY = 0.8f, ringY = 0.8f, pinkyY = 0.8f // Curled
        )
        val state = detector.process(landmarks, 0L)
        assertTrue(state is GestureState.Pointing)
    }

    @Test
    fun `test pinch click fires once`() {
        val landmarks = createMockLandmarks(
            thumbX = 0.5f, thumbY = 0.5f,
            indexX = 0.52f, indexY = 0.52f // Distance < 0.06f
        )
        val state1 = detector.process(landmarks, 0L)
        assertTrue(state1 is GestureState.PinchClick)

        val state2 = detector.process(landmarks, 100L)
        assertTrue(state2 is GestureState.PinchDetected)
    }

    @Test
    fun `test drag activates after 300ms`() {
        val landmarks = createMockLandmarks(
            thumbX = 0.5f, thumbY = 0.5f,
            indexX = 0.52f, indexY = 0.52f
        )
        detector.process(landmarks, 0L) // Click
        val state = detector.process(landmarks, 300L) // Dragging
        assertTrue("Expected Dragging, but got ${state.javaClass.simpleName}", state is GestureState.Dragging)
    }

    @Test
    fun `test release exits drag mode`() {
        val landmarksPinched = createMockLandmarks(
            thumbX = 0.5f, thumbY = 0.5f,
            indexX = 0.52f, indexY = 0.52f
        )
        detector.process(landmarksPinched, 0L)
        val stateDrag = detector.process(landmarksPinched, 301L)
        assertTrue(stateDrag is GestureState.Dragging)

        val landmarksOpen = createMockLandmarks(
            thumbX = 0.2f, thumbY = 0.5f, // Far apart
            indexX = 0.8f, indexY = 0.2f, indexPipY = 0.5f,
            middleY = 0.8f, ringY = 0.8f, pinkyY = 0.8f
        )
        val stateOpen = detector.process(landmarksOpen, 400L)
        assertTrue(stateOpen is GestureState.Pointing)
    }
}
