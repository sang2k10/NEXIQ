# NEXIQ Architecture Specification

NEXIQ follows Clean Architecture principles, ensuring strict decoupling between the user interface, business logic, and platform hardware services.

---

## High-Level Architecture Diagram

```
+-------------------------------------------------------------------------+
|                           Presentation Layer                            |
|  - Jetpack Compose Screens (SettingsScreen, MainActivity)               |
|  - Floating Window Subsystem (FloatingBubbleManager, OverlayManager)    |
|  - Lens In-Place Rendering Engine (LensInteractiveCanvas,               |
|    LensTextRenderer, TextBackgroundMasker)                              |
|  - Gesture & Transform Matrix (GestureTransformState,                   |
|    CoordinateTransformer)                                               |
+-------------------------------------------------------------------------+
                                    │
                                    ▼
+-------------------------------------------------------------------------+
|                              Domain Layer                               |
|  - Core Models: BoundingBox, OcrBlock, TranslatedBlock, Language,       |
|    CropRect, AppSettings, TranslationSession                            |
|  - Algorithms: TextParagraphClusterer (Geometric & Typographic Grouping)|
|  - Interfaces: OcrEngine, TranslationEngine, ScreenCaptureManager,      |
|    SettingsRepository, SessionRepository                                |
|  - Use Cases: ExecuteOcrUseCase, TranslateBlocksUseCase,                |
|    RetranslateCropUseCase                                               |
+-------------------------------------------------------------------------+
                                    │
                                    ▼
+-------------------------------------------------------------------------+
|                               Data Layer                                |
|  - OCR Implementation: MlKitOcrEngine (Google ML Kit Vision)            |
|  - Translation Engines: MlKitTranslationEngine (Offline on-device),     |
|    GoogleWebTranslationEngine (Optional cloud),                         |
|    DelegatingTranslationEngine (Dynamic selector)                       |
|  - Screen Capture: ScreenCaptureManagerImpl (MediaProjection +          |
|    VirtualDisplay + ImageReader)                                        |
|  - Persistence: SettingsRepositoryImpl (AndroidX DataStore Preferences),|
|    SessionRepositoryImpl (In-memory StateFlow)                          |
+-------------------------------------------------------------------------+
```

---

## Execution Pipeline Trace

```
1. TRIGGER:
   - User taps floating bubble or Quick Settings tile.
   - ScreenCaptureService starts foreground with FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION.

2. CAPTURE:
   - MediaProjection creates VirtualDisplay pointing to an ImageReader.
   - Single static Bitmap is extracted in private RAM.
   - VirtualDisplay and MediaProjection are stopped immediately.

3. OPTICAL CHARACTER RECOGNITION:
   - Bitmap is processed by MlKitOcrEngine.
   - Raw recognized TextBlocks and Lines are extracted.

4. PARAGRAPH CLUSTERING:
   - TextParagraphClusterer processes extracted lines.
   - Checks vertical gap (leading <= 1.35x line height), font height consistency (ratio >= 0.55),
     and horizontal overlap (overlap >= 25%).
   - Unwraps end-of-line hyphens (e.g. "trans-" + "lation" -> "translation").
   - Joins CJK characters without whitespace and Latin text with spaces.
   - Produces composite semantic OcrBlocks with unified bounding boxes.

5. TRANSLATION:
   - TranslateBlocksUseCase routes texts to DelegatingTranslationEngine.
   - By default, MlKitTranslationEngine translates text completely offline on-device.

6. BACKGROUND INPAINTING:
   - TextBackgroundMasker samples perimeter pixels around each bounding box.
   - Computes dominant background color and WCAG high-contrast text color (dark vs. light).

7. LENS OVERLAY RENDERING:
   - LensTextRenderer renders rounded pill background patches.
   - Dynamically calculates optimal font size via binary search to fill box without overflow.
   - Draws translated text directly over original coordinates on the Canvas.
```

---

## Decoupled Coordinate Pipeline

To support zooming, panning, and rotation while keeping translated overlays perfectly aligned:
1. **Source Image Space**: Absolute pixel coordinates of the original display capture $[0, 0, W_{img}, H_{img}]$.
2. **Display Canvas Space**: The interactive viewport where affine transformations are applied:
   $$P_{canvas} = \mathbf{M} \cdot P_{image}$$
3. **Inverse Transformation**: When the user crops a sub-region in canvas coordinates:
   $$P_{image} = \mathbf{M}^{-1} \cdot P_{canvas}$$
   This guarantees that cropping while zoomed 2x or rotated 90° extracts the exact intended pixels from the original image.
