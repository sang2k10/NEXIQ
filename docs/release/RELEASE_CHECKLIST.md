# NEXIQ Release Verification Checklist

Every item in this checklist must be completed and verified before marking a build ready for Google Play release.

---

## 1. Google Play & Platform Compliance
- [ ] **Target SDK**: Configured with `compileSdk = 36` and `targetSdk = 36` in `app/build.gradle.kts`.
- [ ] **Min SDK**: Set to `minSdk = 26` (Android 8.0 Oreo).
- [ ] **Android 16 Edge-to-Edge**: Full edge-to-edge window insets verified without clipping.
- [ ] **Predictive Back**: Gesture navigation does not cause visual artifacts or premature session cancellation.
- [ ] **64-bit Architecture**: ML Kit binaries packaged with 64-bit native libraries (`arm64-v8a`, `x86_64`).

---

## 2. Code Quality & Static Analysis
- [ ] **Lint Clean**: `./gradlew lintDebug` and `./gradlew lintRelease` pass with **0 errors**.
- [ ] **Unit Test Suite**: `./gradlew testDebugUnitTest` passes 100% of test cases.
- [ ] **Compiler Warnings**: Zero critical Kotlin compiler warnings.
- [ ] **Deprecation Audit**: No unsupported deprecated APIs in supported SDK ranges.

---

## 3. Privacy & Data Transparency
- [ ] **No Unverified Marketing Claims**: "Zero Data Collection Guarantee" removed; honest, implementation-accurate disclosures present.
- [ ] **On-Device Default**: `AppSettings.selectedTranslationEngineId` defaults to `mlkit` (offline, on-device).
- [ ] **Cloud Opt-In Warning**: Warning dialog displayed if user attempts to switch to Cloud translation.
- [ ] **Screenshot Storage**: Disclosed that images saved via the overlay persist in `Pictures/ScreenTranslator` until deleted.
- [ ] **Zero Telemetry / Trackers**: Verified no advertising or analytics SDKs present in dependency tree (`./gradlew app:dependencies`).

---

## 4. Sensitive Permissions & Declarations
- [ ] **MediaProjection**: Declared with `FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION`.
- [ ] **Foreground Service Lifecycle**: Service stops deterministically and notification disappears when overlay is closed (`LIFE-001`).
- [ ] **POST_NOTIFICATIONS**: Runtime permission handled properly on Android 13+ (`PERM-001`).
- [ ] **AccessibilityService Declaration**: Prepared reviewer guide in `docs/release/PLAY_ACCESSIBILITY_REVIEW.md` (`POL-001`).

---

## 5. Build, Signing & Security
- [ ] **Release Signing Architecture**: Documented in `docs/release/SIGNING.md` (`REL-002`).
- [ ] **Keystore Exclusion**: Verified zero keystores (`*.jks`, `*.keystore`) committed to Git.
- [ ] **local.properties Exclusion**: Confirmed `local.properties` is not tracked.
- [ ] **R8 / ProGuard Shrinking**: Release build enables code minification and resource shrinking with verified keep rules.

---

## 6. Manual Hardware QA Matrix
- [ ] Completed `MANUAL-001` through `MANUAL-016` documented in `docs/release/MANUAL_RELEASE_TESTS.md`.
