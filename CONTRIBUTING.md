# Contributing to GestureNav SDK

Thank you for your interest in contributing. This project is open
to public contributions while remaining source-available for
commercial use. Please read this guide before submitting.

---

## Before You Start

By submitting a pull request or any contribution to this repository,
you agree to the **Contributor Terms** in Section 10 of the LICENSE
file. Please read it — the key points are:

- You retain copyright in your own contribution
- You grant the project maintainer a perpetual license to use and
  relicense your contribution (including commercially)
- Your contribution must be your own original work

If you cannot agree to these terms, please do not submit contributions.

---

## What We Welcome

**Bug fixes**
If gesture detection misbehaves, crashes, or produces wrong results —
please open an issue first describing the bug, then submit a fix.

**Performance improvements**
The core constraint of this project is zero UI lag. PRs that improve
frame processing speed, reduce memory allocation, or improve GPU
utilization are highly valued.

**New gesture detection algorithms**
Better finger state classification, improved pinch tracking, or new
gesture types. Include unit tests with hardcoded landmark coordinates.

**Documentation improvements**
Better README, inline code comments, architecture diagrams, or
training guides.

**Test coverage**
Unit tests for PinchDetector, LandmarkMapper, or ViewModel logic.

---

## What We Do Not Accept

- Code that adds dependencies under non-permissive licenses (GPL, etc.)
- Changes that break the multi-module SDK-ready architecture
- UI/design PRs without prior discussion in an issue
- AI-generated code submitted without review or understanding

---

## How to Contribute

1. **Fork** this repository

2. **Create a branch** with a descriptive name:
   ```bash
   git checkout -b fix/pinch-detection-mirroring
   git checkout -b feat/swipe-gesture
   git checkout -b perf/reduce-bitmap-allocation
   ```

3. **Make your changes** following the code style below

4. **Write or update tests** — PRs without tests for logic changes
   will not be merged

5. **Run tests locally**:
   ```bash
   ./gradlew :gesture-core:test
   ./gradlew :ui:test
   ```

6. **Submit a Pull Request** with:
   - Clear description of what changed and why
   - Screenshots or screen recordings if the change affects UI
   - Test results

---

## Code Style

- Kotlin only — no Java files
- Follow existing module boundaries strictly:
  - `:gesture-core` must have zero Android UI dependencies
  - All implementation classes must be `internal`
  - Only domain entities and GestureRepository are `public`
- No `GlobalScope` — all coroutines scoped to ViewModel or injected scope
- No hardcoded values — use `GestureNavConfig`
- KDoc comments on all public API classes and functions

---

## Reporting Issues

Open a GitHub Issue with:

- Device model and Android version
- Steps to reproduce
- Expected vs actual behaviour
- Logcat output if relevant (filter tag: `GestureNav`)

---

## Commercial Use

This project is **not free for commercial or enterprise use**.
If you represent a company that wants to use or integrate this SDK,
please contact the maintainer for a commercial license instead of
contributing as a workaround.

See LICENSE Section 15 for contact details.

---

## Questions

Open a GitHub Discussion or reach out via the contact in the LICENSE.
