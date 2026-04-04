# GestureSliderPoC

A production-ready Android Proof-of-Concept (PoC) application that uses the device's front camera to detect hand gestures and control an on-screen slider in real-time.

## Overview

GestureSliderPoC demonstrates a clean, high-performance integration of computer vision and UI architecture on Android. It leverages MediaPipe for accurate hand landmark tracking, CameraX for efficient camera frame processing, and Jetpack Compose for a responsive, modern user interface.

## Features

- **Real-Time Gesture Tracking**: Uses MediaPipe to track hand landmarks with high precision and low latency.
- **CameraX Integration**: Efficiently processes frames from the front-facing camera.
- **Dynamic UI Control**: Real-time slider control mapped directly to hand gestures using Jetpack Compose.
- **Multi-Module Architecture**: Built with a scalable multi-module architecture separating core logic, camera processing, and UI components.
- **Performance-First**: Designed to maintain high frame rates while running local machine learning models.

## Architecture

The application focuses on separation of concerns using the following modules:
- `app`: Main application module handling dependency injection and entry points.
- `gesture-core`: Domain models, interfaces, and core business logic for gesture processing.
- `ui`: Jetpack Compose UI components, including the gesture-controlled slider.

## Tech Stack

- **Kotlin**
- **Jetpack Compose**
- **CameraX**
- **MediaPipe Tasks Vision**
- **Coroutines & Flow**
- **Gradle Kotlin DSL**

## Getting Started

### Prerequisites
- Android Studio Ladybug or newer
- Android SDK 34+
- A physical Android device with a front-facing camera (emulators may not provide adequate performance for real-time video processing).

### Build and Run
1. Open the project in Android Studio.
2. Sync the project with Gradle files.
3. Connect a physical Android device.
4. Build and run the `app` module.
5. Grant the application camera permissions when prompted.
6. Position your hand in front of the camera to begin interacting with the slider.

## License

This project is distributed under a **Non-Commercial Open Source License**. 
Open-source contributions from the public are highly encouraged! However, enterprise, commercial, or proprietary usage of this software is strictly prohibited without explicit written permission. See the [LICENSE](LICENSE) file for more details.