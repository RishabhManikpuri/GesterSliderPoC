# Compose Navigation Pattern Skill — GestureSliderPoC

---

## 1. Scope & Guidelines

This skill defines the navigation paradigm used in **GestureSliderPoC**, explaining why state-driven navigation is preferred over heavy navigation libraries and how to preserve the CameraX lifecycle across screen transitions.

---

## 2. Established Navigation Architecture

The app uses **State-Driven Compose Navigation** in `MainActivity.kt`:

```kotlin
@Composable
fun AppNavigation(viewModel: SliderViewModel) {
    var currentScreen by remember { mutableStateOf("slider") }
    val lifecycleOwner = LocalLifecycleOwner.current

    // CRITICAL: Lifecycle observer is mounted at the AppNavigation level!
    // This keeps the camera active across screen transitions.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                viewModel.gestureRepository.start(lifecycleOwner)
            } else if (event == Lifecycle.Event.ON_STOP) {
                viewModel.gestureRepository.stop()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.gestureRepository.stop()
        }
    }

    when (currentScreen) {
        "slider" -> GestureSliderScreen(
            viewModel = viewModel,
            onNavigateToSwipe = { currentScreen = "swipe" }
        )
        "swipe" -> SwipeGestureScreen(
            viewModel = viewModel,
            onNavigateBack = { currentScreen = "slider" }
        )
    }
}
```

---

## 3. Why State-Driven Navigation?

1. **Zero Camera Tear-Down / Black Screen:**
   Using standard Navigation Component or NavHost often recreates the screen tree, leading to unbinding and rebinding CameraX. Rebinding takes $300\text{ms} - 800\text{ms}$, resulting in a jarring black flash.
2. **Instant Screen Switching:**
   Switching screens via a simple state toggle (`currentScreen = "swipe"`) is instantaneous and has zero overhead.
3. **Shared ViewModel Scope:**
   A single `SliderViewModel` instance created at the Activity level via `by viewModels()` is passed to each screen, allowing persistent state between screens without complicated shared ViewModel stores.

---

## 4. Navigation Rules & Anti-Patterns

### ✅ DO:
- Keep `DisposableEffect(lifecycleOwner)` in `AppNavigation` so camera lifecycle spans all child screens.
- Use simple lambda callbacks (`onNavigateToSwipe: () -> Unit`, `onNavigateBack: () -> Unit`) on child screens.
- Use `BackHandler` if custom back button handling on Android hardware back button is needed:
  ```kotlin
  import androidx.activity.compose.BackHandler

  BackHandler(enabled = currentScreen != "slider") {
      currentScreen = "slider"
  }
  ```

### ❌ DO NOT:
- Do not add complex navigation libraries (`androidx.navigation:navigation-compose`, Voyager, etc.) unless the app grows beyond 5+ distinct navigation graphs.
- Never place `viewModel.gestureRepository.start(lifecycleOwner)` or `stop()` inside child screen composables.
- Do not instantiate new ViewModels inside individual screens if they need to share camera streams.
