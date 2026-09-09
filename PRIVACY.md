# NEXIQ Privacy Policy

**Effective Date:** September 9, 2026  
**Application:** NEXIQ (Android)  
**Package:** `com.screentranslator`  
**Repository:** [https://github.com/sang2k10/NEXIQ](https://github.com/sang2k10/NEXIQ)  

---

## 1. Introduction

NEXIQ ("we", "our", or "the application") is an open-source Android utility designed to recognize and translate text appearing on your device screen. We believe that screen content is inherently private. This Privacy Policy provides an honest, implementation-accurate explanation of how NEXIQ handles device data, what processing happens locally, when network communication occurs, and how you maintain control over your information.

NEXIQ is an independent open-source project and is **not** affiliated with, endorsed by, or sponsored by Google LLC or Google Translate.

---

## 2. Core Privacy Principles

1. **Memory-First Ephemeral Processing**: Screen captures exist solely in private device RAM for the duration of a translation session and are recycled upon dismissal.
2. **On-Device by Default**: Optical Character Recognition (OCR) and Translation default to on-device Google ML Kit models that run entirely offline without internet connectivity.
3. **No Background Surreptitious Tracking**: NEXIQ contains no advertising SDKs, no behavioral trackers, no third-party analytics (e.g. Firebase Analytics, AppsFlyer), and no user profiling.
4. **Explicit User Action**: Translation sessions and persistent image saves only occur when you explicitly initiate them.

---

## 3. Data Processing Architecture & Flow

### A. Screen Capture
- **Mechanism**: Screen capture is initiated only upon your explicit action (tapping the floating bubble, the Quick Settings tile, or an in-app action).
- **MediaProjection**: By default, Android's official `MediaProjection` API captures a single static frame of your visible display. Android displays a system-level permission confirmation before projection begins.
- **Accessibility Service (Optional)**: If you explicitly enable the NEXIQ Accessibility Service in Android Settings, the application can take a static screenshot using Android's `takeScreenshot()` API. This service has no window-scraping capabilities, does not inspect UI trees, and does not record keystrokes.
- **RAM Lifecycle**: The captured bitmap is held in volatile memory. As soon as the translation session is dismissed or closed, the bitmap is explicitly recycled and memory is released.

### B. Optical Character Recognition (OCR)
- **Engine**: Google ML Kit Text Recognition (`com.google.mlkit:text-recognition`).
- **Processing**: ML Kit executes on-device computer vision models to recognize text blocks and coordinates. All OCR recognition runs locally on your device hardware; recognized screen text is not transmitted over the network for OCR.

### C. Translation Processing
NEXIQ provides two distinct translation execution paths:

#### 1. On-Device Translation (Default & Recommended)
- **Engine**: Google ML Kit On-Device Translation (`com.google.mlkit:translate`).
- **Data Flow**: Text is translated entirely on your device using downloaded offline language packs.
- **Network Access**: Network access is only utilized when downloading language pack models directly from Google's on-device model CDN. Once downloaded, translation runs completely offline without internet connectivity.

#### 2. Cloud Translation (Optional & Experimental)
- **Engine**: Google Web Translation (`GoogleWebTranslationEngine`).
- **Data Flow**: If you manually change the Translation Engine setting from ML Kit to Cloud Translation, recognized text snippets are transmitted over HTTPS to external translation servers (`translate.googleapis.com`).
- **Disclosure & Release Warning**: The current web translation endpoint uses an unauthenticated web query mechanism. Because web requests transmit recognized screen text to external servers, **do not use cloud translation for sensitive, financial, or personal information**.

---

## 4. Storage & Persistence

| Data Category | Storage Location | Retention / Lifecycle |
| :--- | :--- | :--- |
| **Screen Bitmaps** | RAM (Volatile Memory) | Destroyed immediately when session closes |
| **Recognized / Translated Text** | RAM (Session State) | Destroyed immediately when session closes |
| **Saved Screenshots** | Public Storage (`Pictures/ScreenTranslator`) | **Persistent**: Saved only when you explicitly tap "Save Image". Remains until you delete it. |
| **User Preferences** | AndroidX DataStore (`app_preferences`) | Preserved locally (target language, bubble size, theme). Never exported. |
| **System Logs** | Android Logcat (Debug builds only) | Memory ring buffer; stripped in release builds. |

---

## 5. Android Permissions & Justification

NEXIQ requests only the permissions strictly necessary for its functionality:

- **`SYSTEM_ALERT_WINDOW` ("Display over other apps")**: Required to draw the floating shortcut bubble and the in-place translated text overlay directly over your screen.
- **`FOREGROUND_SERVICE` & `FOREGROUND_SERVICE_MEDIA_PROJECTION`**: Required by Android 10+ to capture screen content via `MediaProjection` while displaying an ongoing system notification.
- **`POST_NOTIFICATIONS`**: Required on Android 13+ to show the mandatory foreground service notification informing you that screen translation is active.
- **`INTERNET` & `ACCESS_NETWORK_STATE`**: Used strictly to download offline ML Kit language models and, if optionally enabled by you, to perform cloud translation requests.
- **`VIBRATE`**: Provides subtle haptic feedback when dragging the floating bubble or tapping action buttons (can be disabled in Settings).
- **`BIND_ACCESSIBILITY_SERVICE` (Optional)**: Enables the optional single-tap static screenshot capture without repeated MediaProjection prompts.

---

## 6. Third-Party Services & Libraries

- **Google ML Kit**: On-device machine learning libraries provided by Google under the Apache 2.0 license. Models operate locally.
- **Jetpack Compose & AndroidX**: Core Android UI and architectural frameworks maintained by Google.

NEXIQ does **not** integrate advertising networks, analytics trackers, crash-reporting daemons (e.g., Sentry, Firebase Crashlytics), or cloud synchronization backends.

---

## 7. Security

- **Network Security**: All optional network communication utilizes Transport Layer Security (TLS 1.3 / HTTPS).
- **Sandboxing**: Application preferences and temporary image allocations reside in private application sandboxed storage inaccessible to other apps.

---

## 8. Open Source Transparency

NEXIQ's entire source code, build scripts, and documentation are publicly available on GitHub for independent review and verification:  
[https://github.com/sang2k10/NEXIQ](https://github.com/sang2k10/NEXIQ)

---

## 9. Contact & Inquiries

For questions regarding this Privacy Policy or to report a privacy concern, please open an issue or security advisory on the GitHub repository:  
[https://github.com/sang2k10/NEXIQ/security](https://github.com/sang2k10/NEXIQ/security)
