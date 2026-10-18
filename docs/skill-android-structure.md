# Android Project Structure Skill — GestureSliderPoC

---

## 1. Scope & Guidelines

This skill defines the folder organization, package hierarchy, and architectural boundaries for the **GestureSliderPoC** multi-module Android project.

---

## 2. Multi-Module Separation of Concerns

```
GestureSliderPoC/
├── app/                  # Application assembly, DI bindings, Navigation
├── gesture-core/         # Pure hardware vision processing & gesture classification
└── ui/                   # Jetpack Compose UI, ViewModels, and visual components
```

### 2.1 The `:gesture-core` Module (`com.gesture.slider.core`)
This module is strictly decoupled from presentation:
- **`data/camera/`**: CameraX hardware lifecycle, image analysis setup.
- **`data/detector/`**: Mathematical classifiers (`PinchDetector`, `SwipeDetector`).
- **`data/mediapipe/`**: MediaPipe HandLandmarker wrapper, coordinate mapper.
- **`data/repository/`**: `GestureRepositoryImpl` orchestrating the vision loop.
- **`domain/entity/`**: Data classes (`HandLandmark`, `GestureNavConfig`), sealed states (`GestureState`), landmark constants (`LandmarkIndex`).
- **`domain/repository/`**: Contracts (`GestureRepository`) and pure factory (`GestureCoreFactory`).

> [!IMPORTANT]
> **No UI or Compose imports are ever permitted in `gesture-core`.**

### 2.2 The `:ui` Module (`com.gesture.slider.ui`)
This module contains everything related to Jetpack Compose:
- **Root package (`com.gesture.slider.ui`)**:
  - `SliderUiState.kt`: Immutable UI state model.
  - `SliderViewModel.kt`: ViewModel coordinating gestures into state.
- **`screen/` package (`com.gesture.slider.ui.screen`)**:
  - All `@Composable` screens (`GestureSliderScreen`, `SwipeGestureScreen`).
  - All `@Composable` widgets and layers (`CameraPreviewLayer`, `GestureCursorOverlay`, `GestureSliderWidget`, etc.).

### 2.3 The `:app` Module (`com.gesture.slider.poc`)
This module acts as the glue:
- **Root package (`com.gesture.slider.poc`)**:
  - `GestureSliderApp.kt`: Hilt `@HiltAndroidApp` declaration.
  - `MainActivity.kt`: Hardware permission launcher and `AppNavigation`.
- **`di/` package (`com.gesture.slider.poc.di`)**:
  - `AppModule.kt`: Hilt module providing `GestureNavConfig` and `GestureRepository`.

---

## 3. Class & Package Naming Rules

| Element | Pattern | Example |
|---|---|---|
| Gesture Classifiers | `[GestureName]Detector` | `PinchDetector`, `SwipeDetector` |
| Domain States | `GestureState.[Name]` | `GestureState.Swiping`, `GestureState.Pointing` |
| UI State Classes | `[Feature]UiState` | `SliderUiState` |
| ViewModels | `[Feature]ViewModel` | `SliderViewModel` |
| Top-level Screens | `[Feature]Screen` | `GestureSliderScreen`, `SwipeGestureScreen` |
| Sub-components / Overlays | `[Feature][Role]` | `CameraPreviewLayer`, `GestureCursorOverlay` |
| Hilt DI Modules | `[Feature]Module` | `AppModule` |

---

## 4. Where to Place New Files

- **Adding a new gesture calculation?** $\rightarrow$ `gesture-core/src/main/java/com/gesture/slider/core/data/detector/`
- **Adding a new gesture state?** $\rightarrow$ `gesture-core/src/main/java/com/gesture/slider/core/domain/entity/GestureState.kt`
- **Adding a new screen?** $\rightarrow$ `ui/src/main/java/com/gesture/slider/ui/screen/`
- **Adding a new UI state field?** $\rightarrow$ `ui/src/main/java/com/gesture/slider/ui/SliderUiState.kt`
- **Adding DI bindings?** $\rightarrow$ `app/src/main/java/com/gesture/slider/poc/di/AppModule.kt`

---

## 5. Resource File Placement & Zero Hardcoding Rules

### 5.1 Strings Scoped by Module
- Never hardcode strings in Kotlin files.
- Each module maintains its own strings file under:
  ```
  [module]/src/main/res/values/strings.xml
  ```
  - `:ui/src/main/res/values/strings.xml` $\rightarrow$ UI buttons, status messages, slider titles.
  - `:app/src/main/res/values/strings.xml` $\rightarrow$ App name, launcher labels, permission toast messages.
  - In Compose: consume via `stringResource(R.string.my_string)`.

### 5.2 Centralized Dimensions Shared Across Modules
- Never inline dimension literals (`16.dp`, `8.dp`) directly across screen files.
- All spacing, margins, paddings, sizes, and corner radiuses must be placed in a shared dimensions resource:
  - `dimens.xml` or a shared `Dimens.kt` accessible by UI modules.
  - In Compose: consume via `dimensionResource(R.dimen.my_dimen)` or `Dimens.SpacingMedium`.
