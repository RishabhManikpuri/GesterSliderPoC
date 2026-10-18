# Compose Development Skill — GestureSliderPoC

---

## 1. Scope & Guidelines

This guide establishes the rules and patterns for developing and modifying UI screens, custom canvas overlays, and animations in the `:ui` module using Jetpack Compose.

---

## 2. Core Principles for High-FPS Compose UI

### 2.1 Zero Allocations in Draw Scope
In high-frequency rendering (such as drawing 21 hand landmarks and 21 interconnecting bones at 60 FPS), creating objects inside `Canvas { }` triggers frequent Garbage Collection (GC) pauses.
- **Rule:** Static data structures (like `HAND_CONNECTIONS` or default paints) must be top-level constants or remembered outside the draw scope.
- **Bad:**
  ```kotlin
  Canvas(modifier = Modifier.fillMaxSize()) {
      val connections = listOf(0 to 1, 1 to 2) // ALLOCATED EVERY FRAME!
      val stroke = 4.dp.toPx() // Recalculated constantly!
  }
  ```
- **Good:**
  ```kotlin
  val HAND_CONNECTIONS = listOf(0 to 1, 1 to 2) // Top-level constant

  Canvas(modifier = Modifier.fillMaxSize()) {
      val strokeWidthPx = 4.dp.toPx() // Density read is fine, but avoid complex objects
      for (conn in HAND_CONNECTIONS) { ... }
  }
  ```

### 2.2 Layering Architecture for Camera + Overlay Screens
All gesture screens follow a standard 5-layer layout inside a `Box`:

```kotlin
Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
    // Layer 1: Hardware Camera Preview (AndroidView hosting PreviewView)
    CameraPreviewLayer(previewUseCase = previewUseCase, modifier = Modifier.fillMaxSize())

    // Layer 2: Custom Real-time Visuals / Skeletal Overlay
    Canvas(modifier = Modifier.fillMaxSize()) { ... }

    // Layer 3: Semi-transparent Scrim (Protects contrast of UI controls)
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))

    // Layer 4: Interactive Controls / Grid / Slider
    InteractiveContentLayer(...)

    // Layer 5: Top-Level Floating Status Pill
    GestureStatusLabel(text = uiState.gestureLabel, modifier = Modifier.align(Alignment.TopCenter))
}
```

### 2.3 Recomposition Optimization
- Only collect states where needed.
- If a child composable only requires `sliderValue`, pass `sliderValue: Float`, not the entire `SliderUiState`.
- Use `derivedStateOf` when calculating expensive values derived from frequently changing states.
- Annotate data classes with `@Immutable` if passing complex collections across modules.

---

## 3. UI Component Catalog

| Composable | Responsibility | Key Parameters |
|---|---|---|
| `CameraPreviewLayer` | Embeds CameraX `PreviewView` with `ImplementationMode.PERFORMANCE` | `previewUseCase: Preview` |
| `GestureCursorOverlay` | Renders animated pointer, pinch pulse, and motion trail | `cursorX`, `cursorY`, `cursorMode`, `trailPoints` |
| `GestureSliderWidget` | Canvas-drawn custom touchless slider with animated thumb size | `value: Float`, `isDragging: Boolean` |
| `GestureStatusLabel` | High-contrast pill showing current state ("☝️ Pointing", "Sliding") | `text: String` |
| `SwipeGestureScreen` | 3x3 Grid with persistent selection state and skeletal overlay | `viewModel: SliderViewModel`, `onNavigateBack: () -> Unit` |

---

## 4. Compose Review Checklist

- [ ] Does any list, collection, or path get allocated inside `Canvas`?
- [ ] Are animations using `rememberInfiniteTransition` or `Animatable` properly remembered?
- [ ] Is `Modifier` always accepted as the first optional parameter on custom UI functions?
- [ ] Are previews or layout previews isolated from Hardware/Camera dependencies?
