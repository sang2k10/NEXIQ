# Project Task Backlog (TODO)

## High Priority (Active & Immediate)
- [x] CP-00: Verify development toolchains (Java 21, Android SDK 35/36, Gradle 8.12).
- [x] CP-00: Install targeted AAS skills to `.agents/skills`.
- [x] CP-00: Initialize persistent project memory in `.project/`.
- [x] CP-01: Bootstrap Android project with Gradle wrapper, AGP 8.8.x, Compose, Kotlin 2.0.x.
- [x] CP-01: Define Domain models (`TranslationSession`, `OcrBlock`, `BoundingBox`, `Language`, `TranslationResult`).
- [x] CP-01: Define Domain interfaces (`OcrEngine`, `TranslationEngine`, `ScreenCaptureManager`, `OverlayManager`).
- [x] CP-01: Implement Settings repository with DataStore and Settings UI screen in Compose.
- [x] CP-02: Implement `ScreenCaptureService` with `mediaProjection` foreground service type and permission flow.
- [x] CP-03: Implement `ScreenTranslationOverlayService` / WindowManager overlay with gesture canvas (pinch/zoom/pan/rotate).
- [x] CP-04: Implement `MlKitOcrEngine` supporting Latin, Japanese, Chinese, and Korean scripts.
- [x] CP-05: Implement `TranslationEngine` (ML Kit On-Device + Google Web fallback) and live language selector.
- [x] CP-06: Implement in-place Lens-style text rendering with background masking and matrix alignment.
- [x] CP-07: Implement Crop mode with interactive rectangle and automatic re-OCR & retranslation.
- [x] CP-08: Implement Quick Settings Tile, persistent notification action, and floating trigger bubble.
- [x] CP-09: Implement lifecycle hardening, ephemeral screenshot cleanup, and error feedback banners.
- [x] CP-10: Write automated tests, verify latency, and create release build.
