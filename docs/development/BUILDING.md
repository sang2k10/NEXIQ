# NEXIQ Build & Development Guide

This document details the environment configuration, dependency management, and build workflows for NEXIQ.

---

## 1. System Requirements

- **Operating System**: macOS, Linux, or Windows 10/11.
- **Java Development Kit**: JDK 21 (Temurin, Azul Zulu, or Android Studio bundled JetBrains Runtime).
- **Android SDK**:
  - `compileSdkVersion`: 36 (Android 16)
  - `buildToolsVersion`: 36.0.0
  - Android SDK Command-line Tools & Platform-Tools.
- **Android Studio**: Android Studio Ladybug (2024.2.1) or newer.

---

## 2. Environment Setup

### Verifying JDK 21
```bash
java -version
# Expected: openjdk version "21.x.x"
```

Set your `JAVA_HOME` environment variable to your JDK 21 installation path.

### Configuring Android SDK
Create or verify `local.properties` in the project root pointing to your Android SDK location:
```properties
sdk.dir=/path/to/your/android-sdk
```
*(Note: `local.properties` is automatically ignored by Git and must never be committed).*

---

## 3. Build Commands

Use the bundled Gradle wrapper (`./gradlew` on Linux/macOS or `.\gradlew.bat` on Windows).

### Clean & Assemble Debug APK
```bash
./gradlew assembleDebug
```
The compiled debug APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

### Execute Automated Unit Tests
```bash
./gradlew testDebugUnitTest
```

### Execute Android Lint Quality Audit
```bash
./gradlew lintDebug
```
Generates HTML and XML reports in `app/build/reports/lint-results-debug.html`.

### Build Release Artifacts (Unsigned / Local)
```bash
./gradlew assembleRelease
```
For production signing workflows, refer to [docs/release/SIGNING.md](../release/SIGNING.md).

---

## 4. Physical Device Debugging

1. Enable **Developer Options** and **USB Debugging** on your target device.
2. Install the debug APK via ADB:
   ```bash
   ./gradlew installDebug
   ```
3. Grant required runtime overlay permission:
   - Go to **Settings $\to$ Apps $\to$ Special app access $\to$ Display over other apps $\to$ NEXIQ $\to$ Allow**.
4. Test live screen translation sessions.
