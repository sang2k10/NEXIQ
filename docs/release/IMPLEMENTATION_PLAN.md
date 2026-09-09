# NEXIQ — Master Release Implementation Plan

**Target Repository:** `https://github.com/sang2k10/NEXIQ`  
**Brand Identity:** **NEXIQ** ("Translate what's on your screen.")  
**Date:** 2026-09-09  
**Status:** DRAFT — PENDING USER REVIEW & APPROVAL  

---

## Executive Summary & Scope

This implementation plan outlines the transformation of the current Android screen-translation project into a production-grade, privacy-transparent, open-source Android application ready for public GitHub publication and Google Play Store distribution.

The plan strictly adheres to:
1. **Preserving existing architecture and working functionality**: No gratuitous rewrites.
2. **Strict repository safety**: No history rewriting, no force pushes, no destructive operations, no secrets in Git.
3. **Checkpointed execution**: Each phase is verified independently before proceeding.
4. **Resolution of all 7 High-Priority Release Blockers** (`REL-001` through `LIFE-001`) from `RELEASE_AUDIT_REPORT.md`.
5. **Implementation-driven truth in privacy**: Eliminating marketing hyperbole ("Zero Data Collection Guarantee") and replacing it with transparent, legally accurate disclosures matching the Kotlin/C++ codebase.

---

## PHASE 0: Repository Safety & Organization

### Current Git State
- **Branch**: `main` (tracked to remote `origin/main`).
- **Remote**: `origin` currently points to `https://github.com/sang2k10/screen-translator.git`. Target remote is `https://github.com/sang2k10/NEXIQ.git`.
- **Working Tree**: Clean. No uncommitted tracked changes.

### File Mutation Strategy
- **Files to Move**:
  - `RELEASE_AUDIT_REPORT.md` $\to$ `docs/release/archive/RELEASE_AUDIT_REPORT_INITIAL.md` (preserves historical audit record).
  - `DESIGN.md` $\to$ `docs/design/DESIGN_SPEC.md` (migrates internal tokens to structured documentation).
- **Files to Create (Public Repository & Documentation)**:
  - `README.md` (root, comprehensive public documentation).
  - `PRIVACY.md` (root, implementation-accurate legal privacy policy).
  - `SECURITY.md` (root, responsible disclosure policy).
  - `CHANGELOG.md` (root, version history).
  - `CONTRIBUTING.md` (root, contribution guidelines).
  - `NOTICE.md` (root, third-party library licenses & notices).
  - `LICENSE` (root, Apache License 2.0).
  - `docs/architecture/ARCHITECTURE.md`
  - `docs/development/BUILDING.md`
  - `docs/privacy/PRIVACY_MODEL.md`
  - `docs/release/SIGNING.md`
  - `docs/release/PLAY_ACCESSIBILITY_REVIEW.md`
  - `docs/release/RELEASE_CHECKLIST.md`
  - `docs/release/MANUAL_RELEASE_TESTS.md`
  - `docs/release/RELEASE_AUDIT_FINAL.md`
- **Files to Modify**:
  - `app/build.gradle.kts` (compileSdk/targetSdk 36, signing configuration architecture, release R8 rules).
  - `gradle/libs.versions.toml` (target/compile SDK alignment).
  - `app/src/main/AndroidManifest.xml` (cleanup comments, tighten permissions).
  - `app/src/main/java/com/screentranslator/service/ScreenTranslateTileService.kt` (fix API 26-36 branches, eliminate lint errors).
  - `app/src/main/java/com/screentranslator/data/engine/web/GoogleWebTranslationEngine.kt` (isolate cloud endpoint, document limitations, ensure safe body encoding).
  - `app/src/main/java/com/screentranslator/data/engine/DelegatingTranslationEngine.kt` (ensure on-device is strict default, clear cloud opt-in).
  - `app/src/main/java/com/screentranslator/presentation/overlay/FloatingBubbleManager.kt` & `OverlayManager.kt` (lifecycle deterministic stop).
  - `app/src/main/java/com/screentranslator/service/ScreenCaptureService.kt` (service stop conditions, media projection teardown).
  - `app/src/main/java/com/screentranslator/presentation/overlay/matrix/GestureTransformState.kt` & `CropOverlay.kt` (unified inverse coordinate mapping).
  - `app/src/main/java/com/screentranslator/presentation/settings/SettingsScreen.kt` & `MainActivity.kt` (commercial UI polish, honest welcome dialog).
- **Files That Must Remain Untouched**:
  - Core domain models (`BoundingBox.kt`, `Language.kt`, `OcrBlock.kt`, `TranslatedBlock.kt`, `TranslationSession.kt`).
  - Core OCR clustering (`TextParagraphClusterer.kt`).
  - ML Kit OCR and Translation integrations (`MlKitOcrEngine.kt`, `MlKitTranslationEngine.kt`).
- **Files That Must Never Be Committed**:
  - `local.properties` (contains user workstation paths).
  - Keystores (`*.jks`, `*.keystore`).
  - CI secrets / signing passwords / private tokens.
  - Build outputs (`build/`, `.gradle/`, `app/build/`).

### Before / After Repository Tree
```
BEFORE:                                     AFTER:
d:\PROJECT\Screen Translator\               d:\PROJECT\Screen Translator\
├── .agents/                                ├── .agents/
├── .idea/                                  ├── .idea/
├── .project/                               ├── .project/
├── app/                                    ├── app/
├── gradle/                                 ├── docs/
├── ANTIGRAVITY_PROJECT_STANDARD.md         │   ├── architecture/
├── DESIGN.md                               │   │   └── ARCHITECTURE.md
├── RELEASE_AUDIT_REPORT.md                 │   ├── design/
├── build.gradle.kts                        │   │   └── DESIGN_SPEC.md
├── gradle.properties                       │   ├── development/
├── gradlew                                 │   │   └── BUILDING.md
├── gradlew.bat                             │   ├── privacy/
├── settings.gradle.kts                     │   │   └── PRIVACY_MODEL.md
└── .gitignore                              │   └── release/
                                            │       ├── IMPLEMENTATION_PLAN.md
                                            │       ├── MANUAL_RELEASE_TESTS.md
                                            │       ├── PLAY_ACCESSIBILITY_REVIEW.md
                                            │       ├── RELEASE_AUDIT_FINAL.md
                                            │       ├── RELEASE_CHECKLIST.md
                                            │       ├── SIGNING.md
                                            │       └── archive/
                                            │           └── RELEASE_AUDIT_REPORT_INITIAL.md
                                            ├── .gitignore
                                            ├── ANTIGRAVITY_PROJECT_STANDARD.md
                                            ├── CHANGELOG.md
                                            ├── CONTRIBUTING.md
                                            ├── LICENSE
                                            ├── NOTICE.md
                                            ├── PRIVACY.md
                                            ├── README.md
                                            ├── SECURITY.md
                                            ├── build.gradle.kts
                                            ├── gradle.properties
                                            ├── gradlew
                                            ├── gradlew.bat
                                            └── settings.gradle.kts
```

---

## PHASE 1: Welcome & Trust / Privacy UX

### Problems Identified
- `SettingsScreen.kt:1301-1335` displays a prominent green badge claiming: *"Zero Data Collection Guarantee — Your screen captures and recognized texts are processed entirely in temporary memory and destroyed immediately after each session. We do not store, log, or transmit your personal data."*
- This claim is legally dangerous and factually incorrect because:
  1. The app includes an optional Cloud Translation engine (`GoogleWebTranslationEngine`) that sends screen-derived text over HTTPS to Google servers.
  2. The app allows users to explicitly save screenshots (`ImageSaver.kt`) to public device storage (`Pictures/ScreenTranslator`), persisting indefinitely until deleted.
- The UI looks like an internal prototype rather than a refined utility.
- There is no direct, transparent link to inspect the open-source repository on GitHub.

### Implementation Specification
- **Component to Modify**: `OnboardingPrivacyDialog` in `app/src/main/java/com/screentranslator/presentation/settings/SettingsScreen.kt`.
- **Text & Structure Alignment**:
  - Brand Header: **NEXIQ** ("Translate what's on your screen.")
  - Product description: *"NEXIQ recognizes text directly from your screen and places the translation back where the original text appears — without interrupting what you are doing."*
  - Section 1: **Screen Translation** — *"Capture and translate visible text while you continue using your phone."*
  - Section 2: **On-Device Intelligence** — *"NEXIQ uses on-device ML Kit OCR and translation when available, keeping recognized content on your device for that processing path."*
  - Section 3: **Your Privacy, Clearly Explained** — *"Screen content is processed in memory for translation sessions. Optional cloud translation may send recognized text to the selected translation provider. Images are only saved to shared storage when you explicitly choose to save them."*
  - Section 4: **No Hidden Collection** — *"NEXIQ does not intentionally collect advertising profiles or sell your screen content."*
  - Transparency Row: Button *"View source on GitHub"* opening `https://github.com/sang2k10/NEXIQ` via external browser intent.
  - Primary Action Button: *"Get Started"* (persisting `hasAcceptedOnboarding = true`).
- **Accessibility**:
  - 48dp minimum touch targets on both buttons.
  - Clear content descriptions: `"View NEXIQ open source repository on GitHub"`.
  - Proper TalkBack semantic hierarchy.

---

## PHASE 2: Public Repository Documentation

Comprehensive public documentation authored without AI marketing clichés, providing rigorous engineering specifications:

1. **`README.md` (Root, Public)**:
   - One-line description: *"NEXIQ translates text directly from your Android screen and places the result back over the original content."*
   - Overview, Actual Features, Architecture pipeline, Privacy summary, Sensitive permissions explanation (MediaProjection, Overlay, Notification, Accessibility), Build instructions (Java 21, Gradle 8.12, SDK 36), Debug-device workflow, Security reporting, License, Independent project disclaimer.
2. **`PRIVACY.md` (Root, Public)**:
   - Comprehensive legal/technical privacy policy covering Screen capture ephemerality, ML Kit on-device processing, optional cloud translation disclosure, saved screenshots in shared storage, DataStore preferences, Foreground service notifications, zero analytics/trackers, user rights, and contact information.
3. **`SECURITY.md` (Root, Public)**:
   - Vulnerability disclosure policy, response SLAs, PGP/email contact instructions, out-of-scope boundaries.
4. **`CHANGELOG.md` (Root, Public)**:
   - Full Semantic Versioning 1.0.0 release changelog detailing architecture, engines, overlays, and privacy guarantees.
5. **`CONTRIBUTING.md` (Root, Public)**:
   - Code standards, Kotlin style, test requirements, Git workflow, PR review expectations.
6. **`NOTICE.md` (Root, Public)**:
   - Legal copyright and license attributions for Android Open Source Project, Google ML Kit, Jetpack Compose, KotlinX Coroutines, Material Icons.
7. **`LICENSE` (Root, Public)**:
   - Apache License Version 2.0.
8. **`docs/architecture/ARCHITECTURE.md` (Technical)**:
   - Layered Clean Architecture diagram and execution trace: Screen $\to$ MediaProjection $\to$ ImageReader $\to$ MlKitOcrEngine $\to$ TextParagraphClusterer $\to$ TranslationEngine $\to$ LensInteractiveCanvas.
9. **`docs/development/BUILDING.md` (Technical)**:
   - Prerequisites, Android Studio Ladybug/Meerkats setup, SDK 36 CLI installation, Gradle daemon tips, device testing instructions.
10. **`docs/privacy/PRIVACY_MODEL.md` (Technical)**:
    - In-depth technical privacy specification detailing memory buffer lifecycles, bitmap recycling, and transport security.
11. **`docs/release/SIGNING.md` (REL-002 Document)**:
    - Secure release signing architecture, upload key generation instructions, environment variables, CI configuration, Play App Signing.
12. **`docs/release/PLAY_ACCESSIBILITY_REVIEW.md` (POL-001 Document)**:
    - Specific review document for Google Play Console reviewers explaining the optional Accessibility screenshot mode, user disclosure, strict lack of keystroke logging or UI tree inspection, and reviewer test credentials.
13. **`docs/release/RELEASE_CHECKLIST.md` (Quality Gate)**:
    - Pre-flight checklist covering API 36, R8, lint, permissions, Data Safety, manual tests.
14. **`docs/release/MANUAL_RELEASE_TESTS.md` (QA Suite)**:
    - Structured test scripts for `MANUAL-001` through `MANUAL-016`.

---

## PHASE 3: Repository Reorganization

To ensure the repository is clean and professional for GitHub:
1. Move `RELEASE_AUDIT_REPORT.md` into `docs/release/archive/RELEASE_AUDIT_REPORT_INITIAL.md`.
2. Move/reformat `DESIGN.md` into `docs/design/DESIGN_SPEC.md`.
3. Create the `docs/` subdirectories: `architecture/`, `design/`, `development/`, `privacy/`, `release/`, `release/archive/`.
4. Preserve `.project/` and `.agents/` in their standard locations for IDE and agent continuity.
5. Verify that `.gitignore` includes `local.properties`, `*.jks`, `*.keystore`, `.gradle/`, `build/`, `app/build/`.
6. Audit tracked files using `git status` to ensure zero stray artifacts exist.

---

## PHASE 4: UI & UX Redesign (Utility Aesthetic)

### Visual Philosophy
Shift from "AI dashboard" styling (glowing cyan borders, oversized cards, gradients) to a **quiet, technical, trustworthy commercial Android utility**:
- **Palette**: Clean slate neutrals (`#0F172A`, `#1E293B`, `#F8FAFC`, `#334155`) with restrained Brand Azure/Deep Cyan (`#0284C7`) used strictly as an intentional focal accent.
- **Typography**: Clean Material 3 typography with clear hierarchy (HeadlineSmall for page titles, TitleMedium for section anchors, BodySmall for technical details).
- **Cards & Containers**: Flat surfaces with subtle 1dp borders (`BorderSubtle`) instead of nested floating cards.
- **Information Density**: Compact, scannable rows with standardized 48dp touch targets.

### Screen-by-Screen Redesign
1. **Welcome / Onboarding Dialog**:
   - Clean brand icon, clear headline, scannable feature rows, honest privacy disclosure, dual action buttons ("View source on GitHub" and "Get Started").
2. **Main / Dashboard Screen (`SettingsScreen.kt`)**:
   - Status & Service Switch: Streamlined banner indicating whether the Floating Shortcut or Tile Service is active.
   - Translation Engine Selection: Visual badges distinguishing **"On-Device (Private, Offline)"** vs **"Cloud (Optional)"**.
   - Language Selection: Compact chips with clear directional arrow (`Source` $\to$ `Target`).
   - Floating Bubble Customizer: Live preview with real dimensions (40dp-64dp) and direct theme selection (Brand Azure, Obsidian, Pearl, Indigo).
3. **Session Overlay UI (`SessionOverlayScreen.kt`, `OverlayTopBar.kt`)**:
   - Compact floating capsule toolbar with 44-48dp touch targets for Language picker, Crop mode, Rotate, Save, and Dismiss.
   - Clear visual indication when Crop Mode is active.
4. **Empty / Loading / Error States**:
   - Informative, user-friendly guidance (e.g. *"No text detected — try adjusting your crop selection"*) without raw stack traces.

---

## PHASE 5: [REL-001] Target API 36 Upgrade

### Gradle Configuration
- In `app/build.gradle.kts`:
  - Update `compileSdk = 36`
  - Update `targetSdk = 36`
- In `gradle/libs.versions.toml`:
  - Ensure SDK versions reflect 36.

### Android 16 (API 36) Compatibility Audit
- **Edge-to-Edge**: On API 35/36, edge-to-edge is enforced by default. Verify `MainActivity.kt` and `SessionOverlayScreen.kt` handle window insets correctly using `WindowInsetsCompat` and Compose `safeDrawingPadding()`.
- **Predictive Back**: Ensure predictive back gesture animations do not glitch or close active translation sessions prematurely.
- **Foreground Service Types**: Verify `FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION` declarations and runtime requirements conform to API 36 enforcement.
- **Verification**: Run `./gradlew.bat compileDebugKotlin` and verify clean build with zero API 36 warnings.

---

## PHASE 6: [REL-002] Release Signing Architecture

### Security Rules
- **No private keys in Git**: Never commit `*.jks`, `*.keystore`, or password strings.
- **Safe Fallback**: If signing environment variables are absent, Gradle must configure the release build to skip signing or assemble an unsigned release AAB, without failing debug builds.

### Gradle Signing Implementation
In `app/build.gradle.kts`:
```kotlin
signingConfigs {
    create("release") {
        storeFile = System.getenv("KEYSTORE_PATH")?.let { file(it) }
        storePassword = System.getenv("KEYSTORE_PASSWORD")
        keyAlias = System.getenv("KEY_ALIAS")
        keyPassword = System.getenv("KEY_PASSWORD")
    }
}
buildTypes {
    release {
        isMinifyEnabled = true
        signingConfig = signingConfigs.findByName("release")?.takeIf {
            it.storeFile?.exists() == true
        } ?: signingConfigs.getByName("debug") // fallback for local dry-run
        proguardFiles(...)
    }
}
```
### Documentation (`docs/release/SIGNING.md`)
- Step-by-step key generation command using `keytool`.
- Secure environment variable configuration for GitHub Actions / Bitrise.
- Play App Signing enrollment procedure (uploading public cert, Play manages app signing key).

---

## PHASE 7: [REL-003] Quick Settings Tile Lint & Compatibility

### Root Cause Analysis
In `ScreenTranslateTileService.kt`:
1. Line 58 calls `startActivityAndCollapse(intent)` which is deprecated in API 34.
2. Line 74 calls `sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))`. On Android 12+ (API 31+), third-party apps calling this broadcast receive a `SecurityException`!
3. On API 34+, `startActivityAndCollapse(PendingIntent)` is the official, lint-approved method.

### Compatibility Fix Strategy
In `ScreenTranslateTileService.kt`:
- **API 34+ (Android 14+)**:
  - Use `PendingIntent.getActivity(this, 0, captureIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)`.
  - Call `startActivityAndCollapse(pendingIntent)`.
- **API 26–33 (Android 8.0 – 13)**:
  - Call `startActivityAndCollapse(intent)` with `@Suppress("DEPRECATION")` scoped narrowly to that single legacy API call.
  - Do NOT call `sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))` on API 31+ to prevent crashes.
- **Verification**: Run `./gradlew.bat lintDebug` to verify **zero lint errors**.

---

## PHASE 8: [PRIV-001] Cloud Translation Privacy & Architecture

### Current Trace
`Capture` $\to$ `OcrEngine` $\to$ `TranslateBlocksUseCase` $\to$ `DelegatingTranslationEngine` $\to$ `GoogleWebTranslationEngine` $\to$ HTTPS GET with screen text in query string:
`https://translate.googleapis.com/translate_a/single?client=gtx&sl=$sl&tl=$tl&dt=t&q=$encodedQuery`

### Remediation Plan
1. **Engine Segregation & Transparency**:
   - Rename display name in UI to: `"Google Translate (Web Scraping — Non-Production)"` or mark as `"Experimental Cloud Engine"`.
   - On-device ML Kit (`MlKitTranslationEngine`) remains the **unconditional default** engine in `AppSettings`.
   - If the user selects the Cloud engine, show an explicit warning dialog:
     *"Cloud translation transmits recognized screen text over the internet. Do not use for sensitive, confidential, or banking information."*
2. **Safer Transport**:
   - Update `GoogleWebTranslationEngine` to use HTTP POST with url-encoded form body rather than GET query parameters, preventing screen text from being logged in transit proxies.
3. **Legal Disclaimer**:
   - Clearly document in `PRIVACY.md` and `docs/privacy/PRIVACY_MODEL.md` that the web-translation endpoint is an unofficial fallback, not endorsed by Google, and requires manual verification before commercial release.

---

## PHASE 9: [POL-001] AccessibilityService Policy Compliance

### Analysis of Purpose
`ScreenTranslatorAccessibilityService` exists as a user-optional shortcut to capture static screenshots without displaying the OS-level `MediaProjection` "Start recording?" consent dialog on every single translation tap.

### Risk & Remediation
- Google Play strictly regulates `AccessibilityService`. Using it to bypass system recording prompts can result in immediate Play Store suspension.
- **Actions**:
  1. Remove misleading comments in `AndroidManifest.xml` (e.g. *"Zero Recording Prompts"*).
  2. Maintain `MediaProjection` as the **primary, standard, recommended** capture engine.
  3. Clearly label Accessibility capture as an *"Alternative Instant Screenshot Method (Optional, requires accessibility permission)"*.
  4. Ensure `ScreenTranslatorAccessibilityService` has no other capabilities: no `FLAG_REQUEST_FILTER_KEY_EVENTS`, no window content scraping, `canRetrieveWindowContent = false`. It only performs `takeScreenshot()`.
  5. Author `docs/release/PLAY_ACCESSIBILITY_REVIEW.md` containing full reviewer instructions, policy justifications, and a video demo reference for Play Console submission.

---

## PHASE 10: [UX-001] Unified Crop & Transform Coordinate Pipeline

### Current Problem
In `LensInteractiveCanvas.kt`:
The user can pan, zoom, and rotate the screen image:
$$\begin{pmatrix} x_{canvas} \\ y_{canvas} \end{pmatrix} = \mathbf{M} \begin{pmatrix} x_{image} \\ y_{image} \end{pmatrix}$$
When the user enters Crop Mode (`CropOverlay.kt`), the drag selection handles produce normalized coordinates based on the display viewport. `OverlayManager.kt` naively multiplies normalized coordinates by original bitmap dimensions, ignoring the canvas zoom, pan, and offset!

### Canonical Coordinate Transformation Architecture
Create `com.screentranslator.presentation.overlay.matrix.CoordinateTransformer`:
- **Forward Mapping ($\text{Image} \to \text{Canvas}$)**:
  $$P_{canvas} = \mathbf{S}(scale) \cdot \mathbf{R}(rotation) \cdot \mathbf{T}(panX, panY) \cdot (P_{image} + Offset_{base})$$
- **Inverse Mapping ($\text{Canvas} \to \text{Image}$)**:
  $$P_{image} = \mathbf{M}^{-1} \cdot P_{canvas}$$
- In `OverlayManager.kt` / `CropOverlay.kt`:
  When the crop is confirmed, invert the 4 corners of the crop box using $\mathbf{M}^{-1}$, compute the axis-aligned bounding rectangle in bitmap space, and clamp to $[0, 0, Bitmap_{width}, Bitmap_{height}]$.
- **Testing**: Add unit tests in `CoordinateTransformerTest.kt` verifying identity, scale (2x), pan (+100px), rotation (90°), and boundary clamping.

---

## PHASE 11: [LIFE-001] Foreground Service Lifecycle Management

### State Machine & Lifecycle Rules
`ScreenCaptureService` must only run while an active operation is executing:
1. **Initial Capture**:
   - `startForeground()` called.
   - Screenshot captured via `MediaProjection`.
   - `MediaProjection` stopped immediately upon frame capture.
   - `OverlayManager.showOverlay()` opened.
   - Service is transitioned or kept alive solely as an overlay anchor.
2. **Session Termination**:
   - When user taps "Close" / "Dismiss" on the overlay:
     - `OverlayManager.hideOverlay()` is invoked.
     - An explicit intent with `ACTION_STOP` is sent to `ScreenCaptureService`.
     - `ScreenCaptureService` calls `stopForeground(STOP_FOREGROUND_REMOVE)` and `stopSelf()`.
     - Ongoing notification is immediately removed from status bar.
3. **Error & Edge Paths**:
   - If capture fails $\to$ `stopSelf()`.
   - If user denies permission in `CapturePermissionActivity` $\to$ service does not start.
   - If user force-closes overlay $\to$ `onTaskRemoved()` stops the service.

---

## PHASE 12: Secondary Issues Remediation

### MUST FIX
- **[PERM-001] POST_NOTIFICATIONS runtime handling**: On Android 13+ (API 33+), check `ContextCompat.checkSelfPermission(POST_NOTIFICATIONS)` and provide runtime permission rationale.
- **[STOR-001] Android 8–9 Image Saving**: In `ImageSaver.kt`, ensure API 26-28 checks `WRITE_EXTERNAL_STORAGE` or use app-specific external storage (`context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)`) which requires no runtime permission.

### SHOULD FIX
- **[SEC-001] R8 Hardening**: Configure ProGuard rules in `app/proguard-rules.pro` for ML Kit, Coroutines, and Compose; enable `isMinifyEnabled = true` for release build.
- **[LOC-001] User-facing Strings**: Migrate hardcoded strings from `CropOverlay.kt` and `ImageSaver.kt` into `res/values/strings.xml`.
- **[CAP-001] Screen Configuration Handling**: Handle device rotation while overlay is active.

### OPTIONAL
- Localization into Spanish, Chinese, Japanese, Vietnamese strings.xml.

---

## PHASE 13: Automated Testing Matrix

1. **Unit Tests (`app/src/test/`)**:
   - `TextParagraphClustererTest` (Existing, 8 tests — multiline, hyphenation, CJK, headers).
   - `CoordinateTransformerTest` (New — forward/inverse transform, zoom, pan, rotation).
   - `TranslateBlocksUseCaseTest` (Existing — block translation).
   - `SessionRepositoryTest` (Existing — state updates).
   - `RetranslateCropUseCaseTest` (Existing — crop sub-bitmap extraction).
2. **Lint Verification**:
   - Run `./gradlew.bat lintDebug` $\to$ Zero errors.
3. **Compilation & Packaging**:
   - Run `./gradlew.bat testDebugUnitTest` $\to$ All pass.
   - Run `./gradlew.bat assembleDebug` $\to$ Successful APK.

---

## PHASE 14: Manual Test Mapping

All 16 manual test cases from `RELEASE_AUDIT_REPORT.md` will be documented in `docs/release/MANUAL_RELEASE_TESTS.md`:
- `MANUAL-001` (Clean install & Welcome disclosure): Verified in emulator / physical device.
- `MANUAL-002` (Quick Settings tile API 26-36): Verified on Android 8, 13, 14, 15, 16.
- `MANUAL-003` (MediaProjection consent flow): Verified on Android 14/15.
- `MANUAL-004` (Accessibility screenshot flow): Verified in Accessibility settings.
- `MANUAL-005` (Floating bubble drag, snap, dismiss): Physical touch testing.
- `MANUAL-006` (Overlay gestures: pinch, zoom, pan, rotate): Multitouch test.
- `MANUAL-007` (Crop coordinate accuracy with 2x zoom): Crop inverse transform test.
- `MANUAL-008` (Lifecycle: Foreground service dismiss): Notification drawer verification.
- `MANUAL-009` (Notification permission deny/grant): Android 13+ test.
- `MANUAL-010` (Offline translation): Airplane mode test.
- `MANUAL-011` (Cloud translation opt-in & warning): Settings toggle test.
- `MANUAL-012` (Image save to gallery): API 26 vs API 34 gallery test.
- `MANUAL-013` (Display cutout & landscape): Pixel emulator cutout test.
- `MANUAL-014` (TalkBack & 48dp touch targets): Accessibility scanner test.
- `MANUAL-015` (Light / Dark theme parity): System theme switch.
- `MANUAL-016` (Long text paragraph clustering): Verified in novel / article reader.

---

## PHASE 15: Verification & Release Gates

A phase is only marked **DONE** when:
1. **Compilation Gate**: `./gradlew.bat compileDebugKotlin compileReleaseKotlin` succeeds with 0 errors.
2. **Lint Gate**: `./gradlew.bat lintDebug` finishes with **0 errors**.
3. **Unit Test Gate**: `./gradlew.bat testDebugUnitTest` passes 100% of tests.
4. **Packaging Gate**: `./gradlew.bat assembleDebug` succeeds.
5. **Security Gate**: `git diff` contains no secrets, tokens, or machine paths.
6. **Documentation Gate**: All public markdown files accurately reflect Kotlin code behavior.

---

## PHASE 16: Git Commit & Publication Strategy

### Commit Sequence
1. `docs: establish public documentation and reorganization plan`
   - Root README.md, PRIVACY.md, SECURITY.md, CHANGELOG.md, CONTRIBUTING.md, NOTICE.md, LICENSE.
   - `docs/` technical specifications.
2. `feat(ux): redesign onboarding welcome screen with transparent privacy disclosure`
   - Honest privacy text, GitHub source link, accessibility updates.
3. `fix(tile): resolve quick settings API 26-36 compatibility and lint errors`
   - Fixes `REL-003` in `ScreenTranslateTileService.kt`.
4. `fix(arch): upgrade targetSdk to 36 and configure release signing architecture`
   - Fixes `REL-001` and `REL-002` in `app/build.gradle.kts`.
5. `fix(privacy): isolate cloud translation and mandate on-device default`
   - Fixes `PRIV-001` in `GoogleWebTranslationEngine.kt` and `SettingsScreen.kt`.
6. `fix(accessibility): restrict accessibility service scope and author reviewer guide`
   - Fixes `POL-001` in `AndroidManifest.xml` and `PLAY_ACCESSIBILITY_REVIEW.md`.
7. `fix(coords): implement inverse matrix coordinate transformer for crop and overlay`
   - Fixes `UX-001` in `CoordinateTransformer.kt`, `OverlayManager.kt`, and unit tests.
8. `fix(lifecycle): enforce deterministic foreground service and notification dismissal`
   - Fixes `LIFE-001` in `ScreenCaptureService.kt` and `OverlayManager.kt`.
9. `refactor(ui): polish commercial utility visual design and typography`
   - Design system tokens, scannable layouts, responsive spacing.

### Remote Reconciliation
- Update git remote origin to `https://github.com/sang2k10/NEXIQ.git`.
- Fetch origin, verify branch state, commit in logical stages.
- No force pushes (`git push origin main` cleanly).

---

## Recommended Execution Order

1. **Repository Safety & Organization** (Phase 0 & 3): Directory preparation.
2. **Public Documentation** (Phase 2): Establish ground-truth public contracts.
3. **Welcome / Privacy UX** (Phase 1): Fix onboarding over-claims.
4. **REL-003 (Quick Settings Tile Lint)**: Unblock validation builds.
5. **REL-001 (Target SDK 36)**: Ensure Gradle targets Android 16.
6. **REL-002 (Release Signing Architecture)**: Set up secure keystore configs.
7. **PRIV-001 (Cloud Translation Privacy)**: Secure transport, warning dialog.
8. **POL-001 (Accessibility Policy & Reviewer Docs)**: Scope tightening.
9. **UX-001 (Coordinate Pipeline)**: Inverse transform for zoom/pan/crop.
10. **LIFE-001 (Foreground Service Lifecycle)**: Deterministic stop.
11. **Secondary Fixes (Phase 12)**: Notifications, image saving on Android 8-9, strings.
12. **UI Polish (Phase 4)**: Material 3 visual refinement.
13. **Automated & Manual Test Verification (Phases 13 & 14)**.
14. **Final Release Audit & Review (`RELEASE_AUDIT_FINAL.md`)**.
15. **Git Commit & Push (Phase 16)**.
