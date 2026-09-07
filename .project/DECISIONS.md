# Architecture & Technical Decisions

## ADR-001: Separation of OCR, Translation, and Rendering Coordinates
- **Context**: Screen translation requires overlaying translated text directly over original text while the user can dynamically pan, pinch-to-zoom, rotate, or crop the screenshot.
- **Decision**: Define three explicit coordinate spaces:
  1. `ImageCoordinates`: Absolute pixel coordinates relative to the original captured bitmap `(0, 0, width, height)`.
  2. `CropCoordinates`: Local pixel coordinates inside the active crop rectangle `(left, top, right, bottom)`.
  3. `CanvasCoordinates`: Transformed view coordinates after applying scale, rotation, and translation transforms via a 2D affine matrix `android.graphics.Matrix`.
- **Consequences**: OCR bounding boxes are always stored in `ImageCoordinates`. When rendering on canvas or during transform gestures, they are mapped to screen pixels through `matrix.mapRect()`. This guarantees zero drift during gestures.

## ADR-002: Modular OCR Subsystem (`OcrEngine`)
- **Context**: The app must recognize diverse scripts (Latin, Japanese, Chinese, Korean, Cyrillic, etc.) and allow swapping OCR engines later without rewriting application code.
- **Decision**: Define `OcrEngine` interface in domain layer returning structured blocks `OcrResult` (containing `OcrBlock`, `BoundingBox`, `confidence`, `detectedLanguage`).
- **Initial Implementation**: Google ML Kit Text Recognition with script-specific sub-recognizers (`TextRecognition.getClient(...)` configured for Latin, Chinese, Japanese, Korean).
- **Alternatives Considered**: Tesseract (larger native binary footprint, slower on mobile devices), Cloud Vision (slower response time, requires active network and API keys).

## ADR-003: Replaceable Translation Subsystem (`TranslationEngine`)
- **Context**: Translation must be fast, responsive, and support switching between on-device ML Kit, Google Translate, DeepL, or LLM APIs in the future.
- **Decision**: Define `TranslationEngine` interface:
  ```kotlin
  interface TranslationEngine {
      val id: String
      val displayName: String
      suspend fun translate(text: String, sourceLang: Language, targetLang: Language): TranslationResult
      suspend fun translateBatch(texts: List<String>, sourceLang: Language, targetLang: Language): List<TranslationResult>
      suspend fun getSupportedLanguages(): List<Language>
  }
  ```
- **Initial Implementations**:
  1. `MlKitTranslationEngine`: Fast on-device translation using Google ML Kit On-Device Translate (zero network latency for downloaded models, free, fully private).
  2. `GoogleWebTranslationEngine` / `GoogleCloudTranslationEngine`: Fallback / cloud engine for extended language sets or dynamic translation.

## ADR-004: Screen Capture Pipeline with MediaProjection & Foreground Service
- **Context**: Android 10+ requires a Foreground Service with `foregroundServiceType="mediaProjection"`. Android 14+ (API 34) enforces that `MediaProjection` must be started immediately following the user's consent prompt and cannot be cached indefinitely.
- **Decision**: Implement `ScreenCaptureService` managing the `MediaProjection` lifecycle. When triggered:
  1. If projection token exists and is valid, capture immediately via `VirtualDisplay` + `ImageReader`.
  2. If token is not yet granted, launch a lightweight transparent prompt activity `CapturePermissionActivity` to request `createScreenCaptureIntent()`.
  3. Immediately upon capture, destroy the `VirtualDisplay` to free display buffer memory and pass bitmap to the active session.

## ADR-005: Lens-Style In-Place Text Masking & Rendering
- **Context**: Displaying translation in a separate list destroys visual context. Text must appear directly in-place on the image.
- **Decision**: For each translated text block:
  1. Extract color statistics or sample the background perimeter of the bounding box to compute the dominant background color.
  2. Render a smoothed background patch over the original text to completely mask it.
  3. Render the translated text with auto-scaled typography fitting the original bounding box dimensions, maintaining contrast, padding, and orientation.

## ADR-006: Quick-Access Multi-Trigger Mechanism
- **Context**: The user needs to trigger translation rapidly from any screen.
- **Decision**: Implement multiple official Android mechanisms:
  1. **Quick Settings Tile** (`ScreenTranslateTileService`): One-tap pull down from status bar.
  2. **Persistent Notification Action**: Notification with "Translate Screen" action when service is active.
  3. **Floating Overlay Shortcut Button** (optional user preference): A draggable floating icon on screen edge.
  4. **App Shortcuts & Launcher Shortcut**: Directly triggers capture upon launch.
