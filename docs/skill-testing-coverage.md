# Testing & Coverage Skill — GestureSliderPoC

---

## 1. Scope & Purpose

This skill guides writing unit tests for new features, algorithms, detectors, mappers, and ViewModels in the **GestureSliderPoC** application. It enforces:
1. **Mandatory use of MockK** (`io.mockk`) for all mocking, stubbing, and verification.
2. A **75% to 80% code coverage target** for all newly added or modified business logic.
3. **Automated test execution** (`./gradlew testDebugUnitTest`) before any task or feature is marked complete.

---

## 2. MockK Mandate & Standards

> [!IMPORTANT]
> **Always use MockK (`io.mockk:mockk`) for unit tests.** Never use Mockito or manual fake classes when testing dependencies, flows, or repository interactions.

### 2.1 Standard MockK Imports
```kotlin
import io.mockk.mockk
import io.mockk.every
import io.mockk.coEvery
import io.mockk.verify
import io.mockk.coVerify
import io.mockk.slot
import io.mockk.spyk
import io.mockk.clearMocks
import io.mockk.unmockkAll
```

### 2.2 Core MockK Patterns in GestureSliderPoC

#### Pattern A: Mocking Repository Contracts & Flows
```kotlin
// Create a relaxed mock or strict mock
val repository = mockk<GestureRepository>(relaxed = true)

// Stub stream returns
val testGestureFlow = MutableSharedFlow<GestureState>()
every { repository.gestureStateStream() } returns testGestureFlow
every { repository.rawLandmarkStream() } returns flowOf(emptyList())

// Verify lifecycle invocations
verify(exactly = 1) { repository.start(any()) }
verify(exactly = 1) { repository.stop() }
```

#### Pattern B: Verifying Interactions & Capturing Arguments
```kotlin
val slot = slot<LifecycleOwner>()
every { repository.start(capture(slot)) } returns Unit

// Assert captured argument
assertEquals(expectedLifecycleOwner, slot.captured)
```

#### Pattern C: Spying on Real Detectors
When testing integration between `GestureRepositoryImpl` and detectors:
```kotlin
val realDetector = PinchDetector()
val spyDetector = spyk(realDetector)

// Optionally override or verify calls
verify { spyDetector.process(any(), any()) }
```

---

## 3. Code Coverage Mandate (75% – 80% Target)

Every newly added or modified business logic component must have comprehensive unit tests achieving at least **75% – 80% branch & line coverage**.

| Layer / Component | Target Coverage | What to Test | Testing Stack |
|---|---|---|---|
| **Detectors (`gesture-core`)** | **85%+** | State transitions, boundary clamping, thresholds, gesture classification, empty landmark lists | JUnit 4, MockK |
| **ViewModels (`:ui`)** | **80%+** | `StateFlow` state transitions via Turbine, event handling, bounds limits, input transformations | MockK, Turbine, Coroutines Test (`runTest`, `StandardTestDispatcher`) |
| **Mappers & Entities** | **85%+** | DTO-to-Entity conversions, axis mirroring, edge cases | JUnit 4, MockK |
| **Compose UI** | Screen visual tests / verification | Compose layout and Canvas rendering are verified visually on device | Physical device verification |

---

## 4. End-to-End Testing Templates

### 4.1 ViewModel Testing with MockK & Turbine (`:ui`)
```kotlin
package com.gesture.slider.ui

import app.cash.turbine.test
import com.gesture.slider.core.domain.entity.GestureState
import com.gesture.slider.core.domain.repository.GestureRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SliderViewModelTest {

    private lateinit var viewModel: SliderViewModel
    private lateinit var repository: GestureRepository
    private val gestureFlow = MutableSharedFlow<GestureState>()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        // MockK relaxed mock for repository
        repository = mockk(relaxed = true)
        every { repository.gestureStateStream() } returns gestureFlow
        viewModel = SliderViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test state updates to swiping down correctly`() = runTest {
        viewModel.uiState.test {
            val initial = awaitItem()
            assertEquals(1, initial.gridRow)

            gestureFlow.emit(GestureState.Swiping("DOWN"))

            val updated = awaitItem()
            assertEquals(2, updated.gridRow)
        }
    }

    @Test
    fun `test repository stream subscription`() {
        // MockK verification
        verify { repository.gestureStateStream() }
        verify { repository.rawLandmarkStream() }
    }
}
```

### 4.2 Detector Unit Testing (`gesture-core`)
```kotlin
package com.gesture.slider.core.data.detector

import com.gesture.slider.core.domain.entity.GestureState
import com.gesture.slider.core.domain.entity.HandLandmark
import com.gesture.slider.core.domain.entity.LandmarkIndex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SwipeDetectorTest {

    private lateinit var detector: SwipeDetector

    @Before
    fun setup() {
        detector = SwipeDetector(
            swipeThreshold = 0.2f,
            maxWindowMs = 300L,
            minWindowMs = 50L,
            debounceMs = 400L
        )
    }

    private fun mockLandmarks(wristX: Float, wristY: Float): List<HandLandmark> {
        val list = MutableList(21) { HandLandmark(it, 0f, 0f, 0f) }
        list[LandmarkIndex.WRIST] = HandLandmark(LandmarkIndex.WRIST, wristX, wristY, 0f)
        return list
    }

    @Test
    fun `empty landmarks returns null`() {
        val state = detector.process(emptyList(), currentTimeMs = 100L)
        assertEquals(null, state)
    }

    @Test
    fun `wrist swipe right exceeds threshold detects RIGHT`() {
        detector.process(mockLandmarks(wristX = 0.2f, wristY = 0.5f), currentTimeMs = 0L)
        val state = detector.process(mockLandmarks(wristX = 0.5f, wristY = 0.5f), currentTimeMs = 150L)

        assertTrue(state is GestureState.Swiping)
        assertEquals("RIGHT", (state as GestureState.Swiping).direction)
    }
}
```

---

## 5. Mandatory Verification Protocol

Before declaring any feature complete:

1. **Write Unit Tests with MockK:** Ensure all collaborators and flows are mocked and verified using MockK.
2. **Execute Test Suite:**
   ```bash
   ./gradlew testDebugUnitTest
   ```
3. **Verify Zero Failures:** All unit tests must pass with `BUILD SUCCESSFUL`.
4. **Coverage Audit (75%–80%):** Confirm that newly introduced detectors, ViewModel actions, and state transitions are properly exercised.
