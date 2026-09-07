# Project Roadmap & Milestones

The project is structured into 11 rigorous checkpoints (`CP-00` through `CP-10`). Each checkpoint defines its objective, dependencies, acceptance criteria, and verification steps.

| Checkpoint | Title | Scope / Goal | Status |
|---|---|---|---|
| **CP-00** | Architecture, Requirements & Tech Constraints | System specifications, Android API constraints, AAS skills, project memory structure, ADRs. | **COMPLETED** |
| **CP-01** | Project Skeleton, Architecture Core & Settings | Gradle build setup, package hierarchy, Clean Architecture interfaces (`OcrEngine`, `TranslationEngine`), DataStore Settings repository & Settings UI. | **COMPLETED** |
| **CP-02** | Screen Capture Pipeline & MediaProjection | Foreground service, `MediaProjectionManager`, permission intent flow, `VirtualDisplay` + `ImageReader`, Bitmap acquisition. | **COMPLETED** |
| **CP-03** | Overlay Image Viewer & Pan/Zoom/Rotation | Floating / full-screen overlay window, interactive gesture canvas, matrix transformations, scale/bound constraints. | **COMPLETED** |
| **CP-04** | OCR Pipeline Integration | ML Kit Text Recognition integration with multi-script support (Latin, Japanese, Chinese, Korean), bounding box extraction. | **COMPLETED** |
| **CP-05** | Translation Subsystem & Language Selection | Modular translation engine (ML Kit on-device + Google fallback), batch translation, auto-detect & language picker UI. | **COMPLETED** |
| **CP-06** | In-Place Text Rendering & Masking (Lens-style) | Original text background masking, auto-sized typography fitting bounding boxes, transformation matrix synchronization. | **COMPLETED** |
| **CP-07** | Crop Mode & Re-OCR / Retranslation | Crop rectangle UI with draggable corner/edge handles, high-res sub-bitmap extraction, re-running OCR & translation. | **COMPLETED** |
| **CP-08** | Quick Access & System Shortcuts | Quick Settings Tile (`TileService`), Notification action, App shortcut, optional floating trigger bubble. | **COMPLETED** |
| **CP-09** | Reliability, Privacy, Error Handling & Lifecycle | Ephemeral memory cleanup, graceful error banners, network/offline fallbacks, screen orientation & memory management. | **COMPLETED** |
| **CP-10** | Performance Optimization, Testing & Release Build | Profile pipeline latency, cancel stale jobs, unit tests, end-to-end verification, production release build. | **COMPLETED** |

---

## Detailed Checkpoint Specifications

### CP-00: Architecture, Requirements & Tech Constraints
- **Objective**: Establish solid architectural foundation, verify toolchain, install project-local AAS skills, configure project memory.
- **Acceptance Criteria**:
  - JDK 21, Gradle 8.12, Android SDK 35/36 verified.
  - Selected AAS skills installed into `.agents/skills`.
  - `.project/` memory files created (`PROJECT.md`, `STATE.md`, `ROADMAP.md`, `DECISIONS.md`, `SESSION_LOG.md`, `TODO.md`, `skills.md`).
  - Technical risks identified and mitigated in design.

### CP-01: Project Skeleton, Architecture Core & Settings
- **Objective**: Build clean Gradle project with Compose and AndroidX dependencies, establish Clean Architecture packages, create DataStore settings repository and Settings UI.
- **Acceptance Criteria**:
  - Project compiles cleanly (`./gradlew assembleDebug` or equivalent).
  - Domain models (`TranslationSession`, `OcrBlock`, `Language`, `TranslationResult`) and interfaces (`OcrEngine`, `TranslationEngine`) defined.
  - Settings screen allows configuring default source/target languages, translation engine, OCR options, overlay opacity, and privacy settings.

### CP-02: Screen Capture Pipeline & MediaProjection Flow
- **Objective**: Implement robust, Android 14/15-compliant screen capture mechanism.
- **Acceptance Criteria**:
  - `ScreenCaptureService` configured with `foregroundServiceType="mediaProjection"`.
  - Permission activity seamlessly requests screen capture token.
  - Screen bitmap captured accurately without visual corruption or memory leaks.

### CP-03: Overlay Image Viewer & Pan/Zoom/Rotation
- **Objective**: Display captured screenshot in an interactive overlay above the current screen.
- **Acceptance Criteria**:
  - Overlay renders smoothly over underlying apps using `TYPE_APPLICATION_OVERLAY`.
  - Supports 2-finger pinch-to-zoom, drag-to-pan, and 2-finger rotation gestures.
  - Initial presentation is slightly scaled down for optimal ergonomics.

### CP-04: OCR Pipeline Integration & Bounding Box Extraction
- **Objective**: Integrate multi-script OCR engine capable of detecting Latin, Japanese, Chinese, and Korean text.
- **Acceptance Criteria**:
  - OCR recognizes text blocks with exact pixel bounding boxes in bitmap coordinates.
  - Orientation and script detection handled properly.
  - Non-blocking execution on background coroutine dispatcher.

### CP-05: Translation Subsystem & Live Language Controls
- **Objective**: Connect translation engine and top-bar language selector.
- **Acceptance Criteria**:
  - Auto-detection of source language or manual override.
  - Batch translation of OCR blocks into target language (e.g. Japanese -> Vietnamese).
  - Changing source/target language triggers immediate retranslation of existing blocks without re-capturing screen.

### CP-06: In-Place Text Rendering & Masking (Lens-Style)
- **Objective**: Render translated text directly over original text locations on the transformed image.
- **Acceptance Criteria**:
  - Background of original text is masked cleanly with matching color/patch.
  - Translated text fits inside the bounding box with dynamic font size calculation.
  - Text stays locked in place when the user zooms, pans, or rotates the image.

### CP-07: Crop Mode & Re-OCR / Retranslation
- **Objective**: Scissors tool for cropping specific regions.
- **Acceptance Criteria**:
  - Tapping scissors enters crop mode with interactive handles.
  - Confirming crop extracts sub-region from original high-resolution screenshot.
  - Automatically re-runs OCR and translation specifically on the cropped region.

### CP-08: Quick Access Shortcuts
- **Objective**: Enable triggering translation from anywhere on device.
- **Acceptance Criteria**:
  - Quick Settings Tile configured and functional.
  - Persistent notification action "Translate Screen" available.
  - Optional floating overlay trigger bubble.

### CP-09: Privacy, Error Handling & Lifecycle
- **Objective**: Bulletproof lifecycle management, zero leftover files, user-friendly error banners.
- **Acceptance Criteria**:
  - Temporary screenshots deleted immediately upon closing overlay.
  - Handles permission denials, offline state, empty OCR results gracefully.
  - No crashes during app switching or orientation changes.

### CP-10: Optimization, Testing & Release
- **Objective**: Automated tests, pipeline latency optimization, production packaging.
- **Acceptance Criteria**:
  - Automated unit tests for coordinate transformations, translation batching, and settings.
  - Build succeeds with release optimization / shrinker.
  - End-to-end verification across test language pairs (Japanese -> Vietnamese, English -> Vietnamese, etc.).
