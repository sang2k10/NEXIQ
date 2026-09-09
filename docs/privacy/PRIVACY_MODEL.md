# NEXIQ Technical Privacy Specification

This document provides an engineering-level breakdown of the data lifecycle, hardware boundaries, and transport security implemented in NEXIQ.

---

## 1. Threat Model & Privacy Guarantees

NEXIQ treats visible phone display content as potentially containing:
- Authentication tokens, passwords, and 2FA codes.
- Private personal chat logs and medical records.
- Banking details and payment card information.

### Primary Engineering Commitments:
1. **Volatile-Only Image Lifetime**: Screen capture bitmaps are never written to disk during translation sessions.
2. **Deterministic Bit-Level Reclamation**: Captured bitmaps are recycled using `bitmap.recycle()` as soon as the session closes.
3. **No Hidden Telemetry**: The application code contains zero analytics SDKs, advertising beacons, or crash dump uploads.

---

## 2. Component-by-Component Data Flow

### A. Screen Capture Buffer
- **Origin**: `MediaProjection.createVirtualDisplay()` feeds an `ImageReader` configured with `PixelFormat.RGBA_8888`.
- **Buffer Retention**: A single frame is acquired via `acquireLatestImage()`. Once converted to an in-memory `android.graphics.Bitmap`, the hardware image buffer is closed (`image.close()`) and the `VirtualDisplay` is released (`virtualDisplay.release()`).
- **Storage Location**: Android process heap memory (RAM).

### B. Machine Learning Inference (ML Kit)
- **OCR**: `InputImage.fromBitmap(bitmap, 0)` is passed to Google ML Kit Vision. ML Kit runs bundled C++ TFLite models locally on the device CPU/NNAPI. No image bytes are sent over the network.
- **On-Device Translation**: Translation models are downloaded to the app's internal cache directory (`/data/user/0/com.screentranslator/no_backup/`). Inferences execute on-device in native code without network egress.

### C. Optional Cloud Translation Channel
- **Implementation**: `GoogleWebTranslationEngine` (optional fallback).
- **Protocol**: HTTPS / TLS 1.3.
- **Transmitted Data**: String text snippets only (no images or pixel data).
- **Disclosure**: Disabled by default. When enabled by the user, a prominent warning dialog is presented.

### D. Exported Images (User-Initiated)
- **Path**: When the user taps "Save Image" or "Save Translated Image" in the overlay toolbar:
  - The bitmap is saved via Android `MediaStore` to `Pictures/ScreenTranslator`.
  - This is an explicit user action and is not automated.
  - The saved files remain in shared storage until the user chooses to delete them.

---

## 3. Sandboxed Application State

NEXIQ persists minimal user preferences in `AndroidX DataStore`:
- Target and source language selections.
- Floating bubble size, theme, and position.
- Haptic feedback toggles.
- Onboarding acceptance flag.

No personal user data, translation text history, or usage statistics are saved in DataStore.
