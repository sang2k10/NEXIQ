# Changelog

All notable changes to the NEXIQ Android application will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.0.0] - 2026-09-09

### Added
- **Official Brand Identity**: Transitioned to NEXIQ brand with custom chubby vector logo, iceberg cyan viewfinder, and warm coral accents across launcher icons and floating bubbles.
- **In-Place Screen Translation**: Full-screen translation overlay replacing original text in-place using dominant background inpainting.
- **On-Device OCR & Translation**: Multi-script text recognition (Latin, Chinese, Japanese, Korean, Devanagari) and offline translation powered by Google ML Kit.
- **Smart Paragraph Clustering**: Geometry and typography clustering algorithm (`TextParagraphClusterer`) that merges multi-line sentences into cohesive semantic units while preserving isolated UI buttons.
- **Interactive Lens Canvas**: Pinch-to-zoom, pan, and rotation with persistent coordinate alignment.
- **Crop & Re-Translate**: Real-time bounding box region selection with inverse transformation to extract exact sub-bitmaps.
- **Floating Shortcut Bubble**: Snap-to-edge floating bubble with customizable themes (Brand Azure, Obsidian Dark, Pearl Light, Indigo Royale), adjustable size (40–64dp), and idle dimming.
- **Quick Settings Tile**: Status bar tile for instant translation access across all running applications.
- **Save to Gallery**: Direct export of composited translation overlays to device photo storage.
- **Transparent Onboarding & Privacy Disclosure**: Honest first-launch disclosure explaining on-device vs. cloud processing with a direct link to the open-source GitHub repository.
- **Android 16 Compatibility**: Updated `targetSdk` and `compileSdk` to 36.

### Fixed
- **REL-003**: Resolved Quick Settings tile deprecated calls and `ACTION_CLOSE_SYSTEM_DIALOGS` security restrictions across Android 8 to 16.
- **PRIV-001**: Mandated on-device ML Kit as default translation engine; added explicit user disclosures when opting into cloud translation.
- **UX-001**: Created `CoordinateTransformer` implementing canonical forward and inverse affine matrix transforms for zoom/pan/rotation and crop regions.
- **LIFE-001**: Enforced deterministic foreground service shutdown and notification removal upon overlay dismissal.
- **Long Text Fragmentation**: Eliminated single-line OCR fragmentation to provide natural reading translations for articles and books.
