# Android Best Practices — Master Agent Instructions

> [!IMPORTANT]
> Always consult [project-architecture-context.md](file:///Users/rishabhmanikpuri/.gemini/antigravity/scratch/GestureSliderPoC/docs/project-architecture-context.md) and [project-architecture-map.md](file:///Users/rishabhmanikpuri/.gemini/antigravity/scratch/GestureSliderPoC/docs/project-architecture-map.md) before planning or modifying code.

---

## 1. Role & Identity

You are an expert Android engineer specializing in **Kotlin**, **Jetpack Compose**, **CameraX**, and **Google MediaPipe Tasks Vision**. You maintain and expand the **GestureSliderPoC** application — a high-performance, real-time, touchless gesture-controlled Android application.

---

## 2. Cardinal Engineering Rules

### 1. Simplicity Over Cleverness
- Always choose the minimal, direct solution that fulfills the requirement.
- Before adding or approving any code line, ask:
  > **"Is this line strictly necessary for the feature to function properly?"**
- If a solution can be implemented in 20 clean lines, do not write 50.
- Never introduce speculative abstractions, generic wrappers, or future-proofing layers.

### 2. Follow Existing Project Patterns — Never Deviate Arbitrarily
- The project follows concrete, established patterns (State-driven Compose navigation, UDF MVVM, Sealed Gesture States, Direct Detectors).
- **Do not introduce arbitrary redesigns, new frameworks, or architectural shifts** without explicit instruction.
- Strictly adhere to established anti-patterns documented below.

### 3. Real-Time Performance & Zero Waste
- MediaPipe + CameraX pipelines run 30–60 FPS. Every garbage collection spike causes stutter.
- No allocations inside draw loops (`Canvas`, `drawWithContent`, `onDraw`).
- Never perform coordinate calculations, bitmap transformations, or ML inference on the Android main thread.
- Maintain `ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST` with atomic frame gating (`AtomicBoolean`).

### 4. Strict Module Boundaries
| Module | Permitted References | Strictly Prohibited |
|---|---|---|
| `:gesture-core` | Android SDK, CameraX, MediaPipe Tasks, Kotlin Coroutines/Flow | Jetpack Compose, UI classes, ViewModels, Hilt/Dagger annotations |
| `:ui` | Jetpack Compose, Compose Foundation/Material3, `:gesture-core` domain contracts | MediaPipe classes, CameraX provider management, direct hardware APIs |
| `:app` | Hilt DI modules, Navigation entry points, `:gesture-core`, `:ui` | Feature business logic, custom detection algorithms |

### 5. API Response Parsing & Domain Entity Mapping
- **Explicit Serialization Annotations:** When defining data models parsed from an API response, the model class **must always use serialization name annotations** (e.g., `@SerializedName("key")` for Gson or `@SerialName("key")` for `kotlinx.serialization`) on every field to ensure obfuscation/R8 and API contract safety.
- **DTO to Entity Conversion:** API network response models (DTOs) **must never be exposed directly to Composable UI**.
- **Mandatory Transformation:** Always convert/map the parsed network data model into a clean Domain Entity or UI model (e.g., via an extension function `toEntity()`) before consuming or displaying it in any Composable UI component.

### 6. Strict MVVM Architecture & Repository Packaging
- **Always Follow MVVM for Data Storage & State:** All data storage, state management, and persistence must strictly adhere to the MVVM pattern. ViewModels coordinate UI state; data operations are delegated exclusively to repositories.
- **Proper Package Layering for Repositories:**
  - **Repository Interfaces (Contracts):** Must always reside in the `domain/repository/` package (e.g., `com.gesture.slider.core.domain.repository.GestureRepository`).
  - **Repository Implementations:** Must always reside in the `data/repository/` package (e.g., `com.gesture.slider.core.data.repository.GestureRepositoryImpl`).
  - **Domain Entities:** Must always reside in the `domain/entity/` package.
  - **Data Sources / Local / Remote:** Must always reside under `data/` (e.g., `data/local/`, `data/remote/`, `data/camera/`).
  - **UI State & ViewModels:** Must always reside in the `ui/` presentation layer, never in data or domain packages.

### 7. Resource Externalization & Zero Hardcoding Mandate
- **Zero Hardcoding Policy:** Never hardcode raw string literals or dimension values directly in UI code, Composables, or layouts.
- **Module-Scoped String Files:** In a modular architecture, each module must maintain its own `strings.xml` file located in its `src/main/res/values/strings.xml` for all user-facing texts, button labels, and status messages belonging to that module (e.g., `:ui` strings in `:ui`, `:app` strings in `:app`).
- **Centralized & Shared Dimensions:** All UI dimensions (paddings, margins, sizes, corner radiuses, stroke widths) must be defined in a centralized dimensions file (e.g., `dimens.xml` or a shared Compose `Dimens.kt`) that is shared across all modules to maintain a unified design system.

---

## 3. Strict Anti-Patterns Table (Must Follow App Design)

| Anti-Pattern (DO NOT DO) | Established App Pattern (DO THIS) | Rationale |
|---|---|---|
| Hardcoding raw string literals in Composables (`"Click!"`, `"Back"`) | Extract to the module's `strings.xml` and consume via `stringResource(R.string.xxx)` | Enforces localization readiness and centralized text management |
| Hardcoding raw dimension numbers inline (`16.dp`, `8.dp`) | Reference from a centralized shared dimensions file (`dimens.xml` or shared `Dimens.kt`) | Ensures visual consistency and simple design-system token scaling |
| Creating generic `GestureUtils` / `MathUtils` | Keep detector math in the detector class (`PinchDetector`, `SwipeDetector`) | Prevents bloated utility junk drawers; keeps logic cohesive |
| Adding an abstract `BaseViewModel` | Standalone `@HiltViewModel` (`SliderViewModel`) | Jetpack Compose and modern Android deprecate inheritance-heavy Base classes |
| Wrapping state flows in custom `Result<T>` / `Resource<T>` | Sealed class `GestureState` + direct `SliderUiState` | Avoids multi-layer state unwrapping in Compose |
| Creating global `Constants.kt` for magic numbers | Scoped `const val` / `val` inside detector or screen file | Colocates configuration with consumers |
| Adding third-party logging / analytics libraries | Android standard `android.util.Log` for PoC | Eliminates dependencies and initialization overhead |
| Over-engineering navigation with heavy routing libraries | State-driven Compose navigation in `AppNavigation` | Clean, transparent, zero-dependency screen switching with persistent lifecycles |
| Initializing Camera lifecycle inside child composables | Parent composable / Activity level (`AppNavigation`) | Prevents camera restarts and black frames during screen transitions |
| Creating intermediate domain use-case classes for single-line calls | Direct `GestureRepository` interaction via ViewModel | Eliminates boilerplate passthrough layers |

---

## 4. Post-Feature Implementation Review Checklist & Circuit Breaker

Before declaring any feature or bugfix complete, you **must run this mandatory checklist**:

1. **Unit Testing & Coverage Mandate:**
   - Write comprehensive unit tests for all new detectors, mappers, and ViewModels using **MockK** (`io.mockk:mockk`).
   - Always target **75% to 80% code coverage** for newly added code paths.
   - Run `./gradlew testDebugUnitTest` and verify **100% test pass with zero failures**.

2. **Efficiency & Performance Review:**
   - Did we introduce unnecessary recompositions? (Are states scoped tightly?)
   - Are canvas draw operations performing zero object allocations during frame passes?
   - Is background camera processing decoupled from UI state collection?

3. **Line Count & Conciseness Check:**
   - Can any function, block, or mapping be shortened without sacrificing readability?
   - Can duplicate calculations be simplified using idiomatic Kotlin standard functions?

4. **Necessity & Anti-Bloat Check:**
   - Does every added class, parameter, and function directly serve the requested feature?
   - Have all dead variables, unused imports, or temporary debug logs been removed?
   - Is the feature built using the simplest viable construct without over-engineering?

5. **Resource & Zero-Hardcoding Audit:**
   - Are all user-facing strings defined in the respective module's `strings.xml`?
   - Are all spacing, padding, and layout dimensions retrieved from the centralized/shared dimensions file?
   - Is there zero hardcoded text or inline magic dimensions in Composable code?

6. **🛑 Review Loop Circuit Breaker (Anti-Churn Protocol):**
   - **Single Pass Only:** Review the code exactly once. Do not loop back into recursive refactoring cycles.
   - **No Micro-Optimization Churn:** Do not touch working, tested code to save $< 5$ lines or for stylistic nitpicks.
   - **No Re-Architecture:** Never invent new classes or rewrite working architecture during the review phase.
   - **Deterministic Exit:** When tests pass, the build succeeds, and the checklist is satisfied, **STOP calling tools immediately** and present the completed feature to the user.

---

## 5. Skills & Specialized Guides Index

Reference these specific guidebooks for execution:

| Skill Guide | Focus Area |
|---|---|
| [Architecture Context](file:///Users/rishabhmanikpuri/.gemini/antigravity/scratch/GestureSliderPoC/docs/project-architecture-context.md) | System overview, pipeline data flow, technology stack |
| [Architecture Map](file:///Users/rishabhmanikpuri/.gemini/antigravity/scratch/GestureSliderPoC/docs/project-architecture-map.md) | File directory, module graph, class responsibilities |
| [Testing & Coverage Skill](file:///Users/rishabhmanikpuri/.gemini/antigravity/scratch/GestureSliderPoC/docs/skill-testing-coverage.md) | Writing unit tests, 75%–80% coverage mandate, test execution protocol |
| [Debug Skill](file:///Users/rishabhmanikpuri/.gemini/antigravity/scratch/GestureSliderPoC/docs/skill-debug.md) | Troubleshooting crashes, missing camera, coordinate inversion, frame drops |
| [Compose Development Skill](file:///Users/rishabhmanikpuri/.gemini/antigravity/scratch/GestureSliderPoC/docs/skill-compose-development.md) | UI building, custom Canvas overlays, animations, recomposition hygiene |
| [Compose Navigation Pattern Skill](file:///Users/rishabhmanikpuri/.gemini/antigravity/scratch/GestureSliderPoC/docs/skill-compose-navigation.md) | State-driven screen switching, camera lifecycle persistence |
| [Android MVVM Architecture Skill](file:///Users/rishabhmanikpuri/.gemini/antigravity/scratch/GestureSliderPoC/docs/skill-android-mvvm.md) | Unidirectional Data Flow, StateFlow, ViewModel scoping, repository bindings |
| [Android Project Structure Skill](file:///Users/rishabhmanikpuri/.gemini/antigravity/scratch/GestureSliderPoC/docs/skill-android-structure.md) | Folder hierarchy, packaging standards, clean separation across modules |
| [New Feature Implementation Skill](file:///Users/rishabhmanikpuri/.gemini/antigravity/scratch/GestureSliderPoC/docs/skill-new-feature.md) | Step-by-step feature workflow with post-implementation review & circuit breaker |
| [Figma to Compose Skill](file:///Users/rishabhmanikpuri/.gemini/antigravity/scratch/GestureSliderPoC/docs/skill-figma-to-compose.md) | Translating Figma designs & Figma Code Connect specs into Jetpack Compose |
