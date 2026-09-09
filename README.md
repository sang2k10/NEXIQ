# NEXIQ

NEXIQ translates text directly from your Android screen and places the result back over the original content.

[![Android API](https://img.shields.io/badge/API-26%20to%2036-blue.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-green.svg)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

---

## Overview

When using foreign-language mobile apps, social media feeds, comic readers, games, or web pages, users typically have to switch apps, capture manual screenshots, or paste text into external translation tools. This interrupts the user experience and loses visual layout context.

**NEXIQ** solves this problem by providing an in-place translation overlay. With a single tap from a persistent floating bubble or Quick Settings tile, NEXIQ captures the visible screen, performs optical character recognition, clusters multi-line paragraphs, translates the text, and renders the translation directly over the original words with color-matched background masking.

---

## Features

- **In-Place Screen Translation**: Replaces source text directly over the original screen layout with intelligent background color estimation and high-contrast typography.
- **On-Device Machine Learning**: Utilizes Google ML Kit for on-device multi-script OCR (Latin, Chinese, Japanese, Korean, Devanagari) and offline translation.
- **Smart Paragraph Clustering**: Uses typography and geometry heuristics to group continuous lines into cohesive sentences rather than translating line-by-line fragments.
- **Automatic Language Detection**: Identifies dominant source language automatically with support for manual overrides.
- **Interactive Lens Canvas**: Pinch to zoom, drag to pan, and rotate screen captures to read fine print or rotated text comfortably.
- **Crop & Re-Translate**: Focus translation on a specific sub-region with interactive bounding handles.
- **Customizable Floating Shortcut**: Perimeter snap-to-edge floating bubble with configurable sizing (40dp–64dp), themes (Brand Azure, Obsidian, Pearl, Indigo), and idle auto-dimming.
- **Quick Settings Integration**: Status bar tile (`ScreenTranslateTileService`) for instant translation access from any app.
- **Save to Gallery**: Export composited translations or original screen captures directly to your photo library when needed.
- **Configurable Translation Engine**: Switch between on-device offline translation and optional cloud translation.

---

## How It Works

NEXIQ processes screen content through a decoupled, unidirectional pipeline:

```
[Screen Content]
       │
       ▼
1. Screen Capture (MediaProjection API / ImageReader)
       │
       ▼
2. Optical Character Recognition (Google ML Kit On-Device Vision)
       │
       ▼
3. Paragraph Clustering (TextParagraphClusterer — geometry & typography grouping)
       │
       ▼
4. Translation (Google ML Kit On-Device Translate / Optional Cloud Engine)
       │
       ▼
5. Background Inpainting & Masking (TextBackgroundMasker — dominant color analysis)
       │
       ▼
6. Overlay Rendering (LensInteractiveCanvas — affine matrix in-place typography)
```

### Local vs. Cloud Processing
- **On-Device Processing (Default)**: OCR and translation models run locally inside your device application sandbox. Screen pixels and text never leave the device.
- **Cloud Translation (Optional)**: If you explicitly select cloud translation in Settings, recognized text snippets are transmitted over TLS/HTTPS to translation endpoints.

---

## Privacy

Privacy is a foundational engineering requirement for NEXIQ. Screen captures and recognized text are kept in volatile memory for the active translation session only and are wiped when the overlay is dismissed. NEXIQ contains no advertising SDKs, no behavioral tracking frameworks, and no third-party analytics.

For full technical and legal details, please read our [PRIVACY.md](PRIVACY.md).

---

## Permissions

NEXIQ declares only permissions essential to its core utility:

| Permission | Purpose |
| :--- | :--- |
| `android.permission.SYSTEM_ALERT_WINDOW` | Displays the floating shortcut bubble and the in-place translated text overlay above other applications. |
| `android.permission.FOREGROUND_SERVICE` & `FOREGROUND_SERVICE_MEDIA_PROJECTION` | Complies with Android 10+ foreground service requirements while capturing the display via `MediaProjection`. |
| `android.permission.POST_NOTIFICATIONS` | Displays the mandatory ongoing system notification while the screen capture foreground service is active. |
| `android.permission.INTERNET` & `ACCESS_NETWORK_STATE` | Downloads on-device ML Kit language models and executes optional cloud translation requests when enabled. |
| `android.permission.VIBRATE` | Provides subtle haptic feedback for bubble docking and gesture operations. |
| `android.permission.BIND_ACCESSIBILITY_SERVICE` *(Optional)* | Provides an alternative single-tap static screenshot capture path without repeated recording prompts. |

---

## Architecture

NEXIQ is built according to Clean Architecture principles:

- **Presentation Layer**: Built with Jetpack Compose and Material 3. Handles user interaction, gesture transforms, floating window management, and native Canvas text rendering (`LensTextRenderer`, `LensInteractiveCanvas`).
- **Domain Layer**: Pure Kotlin business models (`BoundingBox`, `OcrBlock`, `TranslatedBlock`, `Language`, `AppSettings`), use cases (`ExecuteOcrUseCase`, `TranslateBlocksUseCase`, `RetranslateCropUseCase`), and clustering algorithms (`TextParagraphClusterer`).
- **Data Layer**: Concrete implementations of `OcrEngine` (ML Kit multi-script) and `TranslationEngine` (ML Kit On-Device, Web Cloud), `ScreenCaptureManager` (`MediaProjection`), and `SettingsRepository` (AndroidX DataStore Preferences).

For detailed architectural diagrams and data flows, see [docs/architecture/ARCHITECTURE.md](docs/architecture/ARCHITECTURE.md).

---

## Technology Stack

- **Language**: Kotlin 2.0.21
- **UI Framework**: Jetpack Compose + Material 3
- **Image Processing & ML**: Google ML Kit Vision Text Recognition, ML Kit Translate
- **State & Asynchrony**: Kotlin Coroutines & StateFlow
- **Storage**: AndroidX DataStore Preferences
- **Build System**: Gradle 8.12, Android Gradle Plugin 8.8.0+, Java 21

---

## Requirements

- **Minimum SDK**: Android 8.0 (API level 26)
- **Target SDK**: Android 16 (API level 36)
- **Compile SDK**: Android 16 (API level 36)
- **Java Compatibility**: JDK 21

---

## Building

### Prerequisites
- Android Studio Ladybug (2024.2.1+) or command-line Android SDK.
- JDK 21 configured as `JAVA_HOME`.
- Android SDK Platform 36 and Build-Tools 36.0.0.

### Build Commands
Clone the repository and build using the Gradle wrapper:

```bash
# Clone the repository
git clone https://github.com/sang2k10/NEXIQ.git
cd NEXIQ

# Run automated unit tests
./gradlew testDebugUnitTest

# Run Android Lint
./gradlew lintDebug

# Assemble Debug APK
./gradlew assembleDebug
```

The output APK will be generated at:  
`app/build/outputs/apk/debug/app-debug.apk`

For detailed setup instructions, see [docs/development/BUILDING.md](docs/development/BUILDING.md).

---

## Running on a Device

1. Connect your Android device via USB and enable **USB Debugging** in Developer Options.
2. Install the debug APK:
   ```bash
   ./gradlew installDebug
   ```
3. Launch **NEXIQ** from your application drawer.
4. Complete the one-time onboarding disclosure and grant the **Display over other apps** permission.
5. Tap **Start Translation** or enable the **Floating Shortcut** to translate any screen content.

---

## Security & Vulnerability Reporting

If you discover a security vulnerability or privacy concern in NEXIQ, please review our reporting instructions in [SECURITY.md](SECURITY.md). Please do not disclose security issues through public GitHub issue threads.

---

## Contributing

We welcome contributions from the community. Please review [CONTRIBUTING.md](CONTRIBUTING.md) for branch management rules, code standards, and testing expectations.

---

## Changelog

For a record of changes across versions, see [CHANGELOG.md](CHANGELOG.md).

---

## License

NEXIQ is open-source software licensed under the [Apache License, Version 2.0](LICENSE).

---

## Disclaimer

NEXIQ is an independent open-source software project. It is not affiliated with, endorsed by, or sponsored by Google LLC, Google Translate, or Alphabet Inc. All product names, logos, and brands are property of their respective owners.

---

## Repository

Official GitHub repository:  
[https://github.com/sang2k10/NEXIQ](https://github.com/sang2k10/NEXIQ)
