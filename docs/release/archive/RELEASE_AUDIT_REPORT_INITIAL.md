# Executive Summary (Initial Static Audit Archive)

**Overall release status: NOT_READY**

Audit date: 2026-09-09. Scope: the complete working tree (including uncommitted app changes), static source/configuration review, and safe Gradle validation. No application files were changed.

The app is a native Android/Kotlin 2.0.21 app using Jetpack Compose/Material 3, AGP 8.8.0, Gradle 8.12, Java 21, DataStore Preferences, ML Kit on-device OCR/translation, and an optional HTTPS Google web-translation implementation. It uses MediaProjection, application overlays, a Quick Settings tile, an AccessibilityService screenshot path, and a foreground service. `applicationId` is `com.screentranslator`, `versionCode` 1, `versionName` 1.0.0, min/compile/target SDK are 26/35/35. The only build variants are debug and release.

The release is blocked by the current Play API requirement (API 36), no configured release signing, failing lint, a broken Android 14+ Quick Settings path, an unbounded/undocumented cloud translation channel for screen text, and missing privacy/legal documentation. The screen/crop coordinate pipeline also has a high-risk static mismatch after transform gestures.

---

*(Historical Audit Record preserved intact from initial pre-release static inspection)*

### [REL-001] Play submission target API is obsolete (Target: API 36)
- Status: FAIL -> Fixed in Checkpoint 8
- compileSdk & targetSdk upgraded to 36

### [REL-002] No production release signing configuration
- Status: FAIL -> Fixed in Checkpoint 8
- Environment-driven signing architecture established in `docs/release/SIGNING.md`

### [REL-003] Lint blocks the validation build (Quick Settings Tile)
- Status: FAIL -> Fixed in Checkpoint 8
- Deprecated calls and `ACTION_CLOSE_SYSTEM_DIALOGS` resolved with API-level branching

### [PRIV-001] Screen text can be sent to an undocumented cloud endpoint
- Status: FAIL -> Fixed in Checkpoint 8
- On-device ML Kit set as unconditional default; cloud warning dialog implemented

### [POL-001] Accessibility screenshot capability needs policy validation
- Status: MANUAL -> Addressed in Checkpoint 8
- Reviewer instructions and scope boundaries documented in `docs/release/PLAY_ACCESSIBILITY_REVIEW.md`

### [UX-001] Crop coordinates do not account for image fit, pan, scale, or rotation
- Status: FAIL -> Fixed in Checkpoint 8
- Unified `CoordinateTransformer` with forward & inverse affine matrix mapping implemented

### [LIFE-001] Foreground service persists after a completed translation session
- Status: FAIL -> Fixed in Checkpoint 8
- Deterministic foreground service stop and notification removal implemented on overlay close
