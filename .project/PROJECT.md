# Project: Screen Translator for Android

## 1. Executive Summary
Screen Translator is a production-quality, privacy-centric Android application whose primary purpose is to translate foreign-language text appearing on the user's phone screen with extreme speed and seamless visual integration.

Unlike traditional OCR copy-paste utilities or detached text-list translators, Screen Translator provides a Google Lens-style in-place translation session:
1. Triggered instantly from anywhere on the device (Quick Settings tile, persistent notification action, floating bubble, or app shortcut).
2. Captures screen using native `MediaProjection`.
3. Displays an interactive overlay where the translated text is rendered directly over the source text bounding boxes with background masking, preserving orientation, relative scale, and placement.
4. Supports pinch-to-zoom, pan, rotation, dynamic crop-and-retranslate, and live source/target language switching.
5. Emphasizes clean architecture with modular, swappable `OcrEngine` and `TranslationEngine` abstractions.
6. Guarantees zero data residue: temporary screenshots and session caches are wiped upon dismissal.

## 2. Technical Stack & Environment
- **Platform**: Android Native (Kotlin)
- **Min SDK**: 26 (Android 8.0 Oreo) / Recommended Target: 35 (Android 15)
- **Build System**: Gradle 8.12, Android Gradle Plugin (AGP) 8.8.0+, Kotlin 2.0.21
- **UI Framework**: Jetpack Compose + Material 3 + Custom Canvas Rendering Engine
- **Concurrency & State**: Kotlin Coroutines, StateFlow, Channel
- **Core Libraries**:
  - AndroidX Core KTX, Lifecycle ViewModel Compose, Activity Compose
  - Google ML Kit Text Recognition (`text-recognition`, `text-recognition-chinese`, `text-recognition-japanese`, `text-recognition-korean`, `text-recognition-devanagari`)
  - Google ML Kit On-Device Translation (`translate`) + Cloud Translation REST abstraction
  - AndroidX DataStore Preferences (for persistent settings)
  - AndroidX Annotation, Material Icons Extended

## 3. High-Level Architecture
The project follows Clean Architecture principles divided into clear layers:

```
+-------------------------------------------------------------+
|                     Presentation Layer                      |
|  - ScreenTranslationOverlayService / OverlayActivity        |
|  - TranslationSessionViewModel (StateFlow<SessionUiState>)  |
|  - Jetpack Compose UI (TopBar, LanguagePicker, CropCanvas)  |
|  - LensCanvasRenderer (Matrix transformation & in-place text)|
|  - SettingsActivity & Screens                               |
+-------------------------------------------------------------+
                              |
                              v
+-------------------------------------------------------------+
|                        Domain Layer                         |
|  - Models: TranslationSession, OcrBlock, TranslatedBlock,   |
|            Language, CropRect, RenderProperties             |
|  - Interfaces: OcrEngine, TranslationEngine,                |
|                ScreenCaptureManager, OverlayManager         |
|  - Use Cases: CaptureAndStartSessionUseCase,                |
|               ExecuteOcrUseCase, TranslateBlocksUseCase,    |
|               RecropAndTranslateUseCase                     |
+-------------------------------------------------------------+
                              |
                              v
+-------------------------------------------------------------+
|                         Data Layer                          |
|  - OcrEngineImpl (Google ML Kit with multi-script support)  |
|  - TranslationEngineImpl (ML Kit On-Device + Cloud Fallback)|
|  - ScreenCaptureManagerImpl (MediaProjection + ImageReader) |
|  - SettingsRepositoryImpl (DataStore Preferences)           |
|  - ImageProcessor (Crop, upscale, downsample, memory-safe)  |
+-------------------------------------------------------------+
```

## 4. Key Engineering Decisions
1. **Coordinate Pipeline Separation**: Original image pixel coordinates, cropped coordinates, and UI display canvas coordinates are decoupled via explicit 3x3 `Matrix` transforms to ensure zoom, pan, and rotation keep translated text perfectly locked to image features.
2. **Text Background Inpainting / Masking**: Original text is occluded with dominant background color estimation or blur/solid pill patches before rendering translated text.
3. **Replaceable Engines**: OCR and Translation are strictly abstracted. Switching between on-device ML Kit, Google Cloud, DeepL, or local LLMs requires only providing a new implementation of `OcrEngine` or `TranslationEngine`.
4. **Android 14/15 MediaProjection Compliance**: Screen capture runs within a declared `FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION` foreground service, handling short-lived tokens and user permission intents reliably.
5. **Ephemerality & Privacy**: Screen captures exist solely in private memory or temporary cache; bitmaps are explicitly recycled and files deleted when the session ends.
