# Debug Skill — GestureSliderPoC

---

## 1. Scope & Purpose

Use this skill when diagnosing crashes, missing camera preview, undetected gestures, coordinate mirroring defects, or UI frame rate drops in the GestureSliderPoC application.

---

## 2. Common Failure Modes & Quick Diagnosis

### 2.1 Black Screen / Camera Feed Disappears on Screen Navigation
* **Symptom:** Camera feed is visible on `GestureSliderScreen`, but turns completely black when switching to `SwipeGestureScreen`.
* **Root Cause:** Camera lifecycle was managed via a `DisposableEffect` inside a screen-level Composable. Navigating away disposes the screen, invoking `cameraProvider.unbindAll()`.
* **Fix:** Maintain the `DisposableEffect(lifecycleOwner)` in the parent navigation container (`AppNavigation` in `MainActivity.kt`), never inside individual leaf screens.

### 2.2 Inverted Gesture Coordinates (Swipe Left registers as Right)
* **Symptom:** Waving your hand to the right moves the cursor or grid to the left.
* **Root Cause:** Front-facing cameras are mirrored by default.
* **Diagnostics Check:**
  1. Check `LandmarkMapper.kt`:
     ```kotlin
     x = 1f - normalizedLandmark.x() // Must invert X for front camera
     ```
  2. Check `SwipeDetector.kt`:
     ```kotlin
     val direction = if (abs(dx) > abs(dy)) {
         if (dx > 0) "RIGHT" else "LEFT"
     } else {
         if (dy > 0) "DOWN" else "UP"
     }
     ```
  3. If inverted, adjust the sign in `SwipeDetector.kt` or the inversion in `LandmarkMapper.kt`, but never both.

### 2.3 MediaPipe HandLandmarker Initialization Crash
* **Symptom:** App crashes immediately on launch with `IllegalStateException` or `RuntimeException: Error occurred while creating HandLandmarker`.
* **Root Causes:**
  1. `hand_landmarker.task` is missing from `app/src/main/assets/` or `gesture-core/src/main/assets/`.
  2. GPU delegate failure on unsupported emulators or specific SoC chipsets.
* **Fix in `HandLandmarkerHelper.kt`:**
  Always wrap delegate assignment with a CPU fallback:
  ```kotlin
  try {
      baseOptionsBuilder.setDelegate(Delegate.GPU)
  } catch (e: Exception) {
      baseOptionsBuilder.setDelegate(Delegate.CPU)
  }
  ```

### 2.4 High Latency & Dropped Frames (UI Freezes)
* **Symptom:** Gestures feel sluggish; skeleton lag is visible; logcat shows `Choreographer: Skipped xx frames!`.
* **Root Causes & Solutions:**
  1. **ImageProxy buffer exhaustion:** Ensure `imageProxy.close()` is called in a `finally` block in `CameraManager` or `HandLandmarkerHelper.processFrame`.
  2. **Atomic gate bypassed:** Verify `AtomicBoolean(isProcessing)` properly drops frames when MediaPipe is busy.
  3. **Main thread ML:** Ensure MediaPipe `detectAsync` is used with `RunningMode.LIVE_STREAM`, never synchronous `detect()`.
  4. **Compose Draw Allocations:** Ensure `HAND_CONNECTIONS` and `Paint`/`Stroke` values are declared statically, not allocated inside `Canvas { }`.

---

## 3. Logcat Inspection Filters

Run the following ADB commands to isolate subsystem logs:

```bash
# Filter CameraX lifecycle and errors
adb logcat -s CameraManager:E ProcessCameraProvider:E

# Filter MediaPipe hand landmarker events
adb logcat -s HandLandmarkerHelper:E

# Track gesture state emissions in real-time
adb logcat | grep -E "(Swiping|PinchClick|Dragging|Pointing)"
```

---

## 4. Verification Routine Before Submitting Fixes

1. Clean build: `./gradlew assembleDebug`
2. Run on physical device with front camera enabled.
3. Verify camera starts without prompt delay when permission is already granted.
4. Switch back and forth between `GestureSliderScreen` and `SwipeGestureScreen` 5 times:
   - Ensure camera preview remains continuous.
   - Ensure hand skeleton immediately tracks on both screens.
5. Perform 4 swipes in all 4 directions (Up, Down, Left, Right) on the 3x3 grid:
   - Ensure grid selection moves accurately and holds position.
