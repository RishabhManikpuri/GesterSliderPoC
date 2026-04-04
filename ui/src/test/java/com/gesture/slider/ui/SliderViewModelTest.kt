package com.gesture.slider.ui

import com.gesture.slider.core.domain.entity.GestureState
import com.gesture.slider.core.domain.entity.HandLandmark
import com.gesture.slider.core.domain.repository.GestureRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import app.cash.turbine.test

@OptIn(ExperimentalCoroutinesApi::class)
class SliderViewModelTest {

    private lateinit var viewModel: SliderViewModel
    private lateinit var repository: GestureRepository
    private val gestureFlow = MutableSharedFlow<GestureState>()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        every { repository.gestureStateStream() } returns gestureFlow
        viewModel = SliderViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test slider moves on drag`() = runTest {
        viewModel.uiState.test {
            val initial = awaitItem()
            assertEquals(0.5f, initial.sliderValue)

            gestureFlow.emit(GestureState.Dragging(
                cursorX = 0.6f, cursorY = 0.6f, dragDeltaX = 0.1f, dragStartX = 0.5f, trailPoints = emptyList()
            ))

            val state = awaitItem()
            assertTrue(state.isDragging)
            assertEquals(CursorMode.DRAGGING, state.cursorMode)
            assertEquals(0.7f, state.sliderValue) // 0.5 + (0.1 * 2)
        }
    }

    @Test
    fun `test slider clamps at bounds`() = runTest {
        viewModel.uiState.test {
            awaitItem()
            
            // Move way out of bounds
            gestureFlow.emit(GestureState.Dragging(
                cursorX = 0.9f, cursorY = 0.6f, dragDeltaX = 0.5f, dragStartX = 0.5f, trailPoints = emptyList()
            ))

            val state = awaitItem()
            assertEquals(1.0f, state.sliderValue)
        }
    }

    @Test
    fun `test click pulse increments on pinch click`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial
            
            gestureFlow.emit(GestureState.PinchClick(cursorX = 0.5f, cursorY = 0.6f))
            
            val state = awaitItem()
            assertEquals(1, state.clickPulse)
            assertEquals("\uD83E\uDD0C Click!", state.gestureLabel)
        }
    }
}
