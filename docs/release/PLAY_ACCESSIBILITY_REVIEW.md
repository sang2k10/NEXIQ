# Google Play Accessibility Policy Review Guide (NEXIQ)

**Application:** NEXIQ (`com.screentranslator`)  
**Declared Capability:** `AccessibilityService` (`canTakeScreenshot="true"`)  
**Policy Target:** Google Play Accessibility API Policy & User Data Policy  
**Review Status:** PENDING MANUAL GOOGLE PLAY REVIEW  

---

## 1. Executive Summary for Play Policy Reviewers

NEXIQ provides in-place screen translation for foreign-language content on Android.

The application declares an optional `AccessibilityService` (`ScreenTranslatorAccessibilityService`) **strictly and exclusively** to take an on-demand static screenshot via Android's `takeScreenshot()` API when the user explicitly triggers translation.

### Strict Scope & Limitations:
1. **Single Action**: The service performs only one function: `takeScreenshot()`.
2. **Zero UI Scraping**: The service sets `canRetrieveWindowContent="false"` and `accessibilityFeedbackType="feedbackGeneric"`. It cannot and does not inspect the active view hierarchy or UI node tree.
3. **No Keystroke / Touch Monitoring**: The service does not declare `FLAG_REQUEST_FILTER_KEY_EVENTS`. It cannot log passwords, keystrokes, or touch interactions.
4. **No Background Surreptitious Capture**: Screenshots are initiated only when the user taps the floating translation shortcut or the Quick Settings tile.
5. **No Data Retention / Transmission**: Screenshots are processed in memory for the immediate translation overlay session and discarded upon dismissal.

---

## 2. Why the Service Exists vs. MediaProjection

NEXIQ supports two distinct capture mechanisms:

| Mechanism | Primary / Default? | User Experience & Policy Distinction |
| :--- | :--- | :--- |
| **MediaProjection API** | **YES (Primary)** | Standard Android screen capture. Displays the OS system recording dialog. Recommended for all users. |
| **Accessibility Service** | **NO (Optional)** | Optional advanced mode for users who translate hundreds of manga pages, documents, or foreign articles sequentially and want an uninterrupted single-tap screenshot without repeated OS prompts. |

NEXIQ does **not** bypass security controls; it requests explicit user consent via the standard Android Accessibility Settings onboarding flow before this service can become active.

---

## 3. Conspicuous In-App Disclosure & Consent Flow

Before activating the Accessibility Service, NEXIQ presents a prominent in-app disclosure meeting Google Play's Accessibility Policy requirements:
- **Title**: *"Optional Instant Screenshot Permission"*
- **Disclosure Statement**:
  *"NEXIQ requests Accessibility permission solely to take a static screenshot of your screen when you tap the translation shortcut. NEXIQ will NOT read your personal messages, will NOT inspect your view hierarchy, will NOT record your keystrokes, and will NOT track your activity in other apps."*
- **Action**: Directs the user to Android Settings with clear cancel and accept buttons.

---

## 4. Play Console Declaration & Reviewer Video Instructions

When submitting NEXIQ to the Google Play Console:

1. **App Content $\to$ Accessibility Tool Declaration**:
   - Question: *"Is your app an Accessibility tool that helps users with disabilities?"*
   - Answer: Select appropriate category; specify as a screen utility supporting multilingual users and language accessibility.
2. **Declared APIs**:
   - Check `takeScreenshot()`.
   - Explicitly confirm `canRetrieveWindowContent = false`.
3. **Reviewer Demo Video**:
   - Provide a direct link to a recorded video demonstrating:
     1. The user tapping the Accessibility setting toggle in NEXIQ.
     2. The prominent in-app disclosure dialog.
     3. The navigation to Android Accessibility Settings and user enablement.
     4. Tapping the floating shortcut to capture and translate a single static screen.
     5. Dismissal of the session and immediate memory cleanup.

---

## 5. Verification & Fallback

If Google Play policy reviewers do not accept the AccessibilityService declaration for screen translation:
- NEXIQ's architecture is fully decoupled: `MediaProjection` is the independent primary capture engine.
- The `ScreenTranslatorAccessibilityService` component can be unmerged or removed without impacting the core `MediaProjection` translation functionality.
