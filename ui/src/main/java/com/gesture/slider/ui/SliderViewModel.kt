package com.gesture.slider.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gesture.slider.core.domain.entity.GestureState
import com.gesture.slider.core.domain.repository.GestureRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SliderViewModel @Inject constructor(
    val gestureRepository: GestureRepository // Expose repository for lifecycle start/stop
) : ViewModel() {

    private val _uiState = MutableStateFlow(SliderUiState())
    val uiState: StateFlow<SliderUiState> = _uiState.asStateFlow()

    private var sliderValueAtDragStart: Float = 0f

    init {
        viewModelScope.launch {
            gestureRepository.gestureStateStream().collect { gestureState ->
                processGesture(gestureState)
            }
        }
        viewModelScope.launch {
            gestureRepository.rawLandmarkStream().collect { landmarks ->
                _uiState.update { it.copy(rawLandmarks = landmarks) }
            }
        }
    }

    private fun processGesture(state: GestureState) {
        when (state) {
            is GestureState.NoHand -> {
                _uiState.update { 
                    it.copy(
                        cursorVisible = false,
                        isDragging = false,
                        gestureLabel = "Show your hand"
                    )
                }
            }

            is GestureState.Pointing -> {
                _uiState.update {
                    it.copy(
                        cursorVisible = true,
                        cursorX = state.cursorX,
                        cursorY = state.cursorY,
                        cursorMode = CursorMode.POINTER,
                        isDragging = false,
                        gestureLabel = "☝\uFE0F Pointing"
                    )
                }
            }

            is GestureState.PinchDetected -> {
                _uiState.update {
                    it.copy(
                        cursorVisible = true,
                        cursorX = state.cursorX,
                        cursorY = state.cursorY,
                        cursorMode = CursorMode.PINCH,
                        isDragging = false,
                        gestureLabel = "\uD83E\uDD0C Pinch Hold..."
                    )
                }
            }

            is GestureState.Dragging -> {
                _uiState.update { currentState ->
                    if (!currentState.isDragging) {
                        sliderValueAtDragStart = currentState.sliderValue
                    }
                    
                    val sliderDelta = state.dragDeltaX * 2f
                    val newValue = (sliderValueAtDragStart + sliderDelta).coerceIn(0f, 1f)
                    
                    currentState.copy(
                        cursorVisible = true,
                        cursorX = state.cursorX,
                        cursorY = state.cursorY,
                        cursorMode = CursorMode.DRAGGING,
                        isDragging = true,
                        sliderValue = newValue,
                        trailPoints = state.trailPoints,
                        gestureLabel = "\uD83E\uDEF3 Sliding: ${(newValue * 100).toInt()}%"
                    )
                }
            }

            is GestureState.PinchClick -> {
                _uiState.update { currentState ->
                    val isCursorOverSlider = state.cursorY in 0.50f..0.70f
                    val updatedSliderValue = if (isCursorOverSlider) state.cursorX else currentState.sliderValue

                    currentState.copy(
                        cursorVisible = true,
                        cursorX = state.cursorX,
                        cursorY = state.cursorY,
                        clickPulse = currentState.clickPulse + 1,
                        sliderValue = updatedSliderValue,
                        gestureLabel = "\uD83E\uDD0C Click!"
                    )
                }
            }
            is GestureState.Swiping -> {
                _uiState.update { uiState ->
                    var newRow = uiState.gridRow
                    var newCol = uiState.gridCol
                    
                    when (state.direction) {
                        "UP" -> newRow = (newRow - 1).coerceAtLeast(0)
                        "DOWN" -> newRow = (newRow + 1).coerceAtMost(2)
                        "LEFT" -> newCol = (newCol - 1).coerceAtLeast(0)
                        "RIGHT" -> newCol = (newCol + 1).coerceAtMost(2)
                    }

                    uiState.copy(
                        gridRow = newRow,
                        gridCol = newCol
                    )
                }
            }
        }
    }
}
