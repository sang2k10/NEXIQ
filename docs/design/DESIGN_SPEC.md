# NEXIQ — Product Design Specification (Refactored)

## 1. Visual Philosophy & Brand Identity

**NEXIQ** is an **instant on-screen visual translation utility**.  
Tagline: **"Understand what's on your screen."**

The design communicates:
- **Technical Minimalism**: Clean, quiet surfaces, sharp typographic hierarchy, and deliberate whitespace.
- **Precision & Speed**: Immediate capture and translation with zero visual friction or clutter.
- **85/15 Rule**: 85% calm neutral surfaces and legible typography, 15% brand accent reserved strictly for active states, primary actions, and meaningful interaction.
- **Android-Native Polish**: Full alignment with Material 3 interaction principles without generic Material 3 or SaaS dashboard tropes.
- **Transparent Privacy**: Honest, implementation-accurate disclosures replacing marketing hyperbole.

---

## 2. Brand Identity: The Viewframe 'N' Monogram

The logo is the visual origin of the entire product identity:
- **Metaphor**: Precision Screen Viewport & Aperture Framing.
- **Geometry**: Two parallel vertical pillars representing device screen boundaries, bridged by an assertive 45° diagonal traverse with clean optical facets.
- **Colorway**: Pure Optical White (`#FFFFFF`) pillars paired with Precision Azure (`#38BDF8`) diagonal scan traverse on Deep Obsidian Slate (`#090D14` to `#0F172A`).
- **Simplicity**: Vector-first, zero micro-decorations, recognizable at 48px in notifications or floating bubbles and at 512px on store listings.

---

## 3. Color System

A WCAG AA compliant **Precision Azure & Slate** palette:

| Token Name | Hex Code (Dark) | Hex Code (Light) | Role / Usage |
|---|---|---|---|
| `SurfaceBase` | `#090D14` | `#F8FAFC` | Root window background |
| `SurfaceContainer` | `#0F172A` | `#FFFFFF` | Primary content panels, modal sheets |
| `SurfaceElevated` | `#1E293B` | `#F1F5F9` | Elevated controls, segmented option chips |
| `BrandPrimary` | `#38BDF8` | `#0284C7` | Key brand accent, active controls, focus |
| `BrandPrimaryContainer` | `rgba(#38BDF8, 0.14)` | `rgba(#0284C7, 0.10)` | Selected item backgrounds, subtle tags |
| `TextPrimary` | `#F8FAFC` | `#0F172A` | Primary titles, active values, readable labels |
| `TextSecondary` | `#94A3B8` | `#475569` | Descriptions, supporting metadata |
| `TextTertiary` | `#64748B` | `#94A3B8` | Section headers, inactive icons, hints |
| `BorderSubtle` | `#1E293B` | `#E2E8F0` | Hairline dividers, subtle borders |
| `BorderMedium` | `#334155` | `#CBD5E1` | Interactive control borders, switch tracks |
| `Success` | `#10B981` | `#059669` | Positive readiness checkmarks, active status |
| `Warning` | `#F59E0B` | `#D97706` | Notices, guidance alerts |
| `Error` | `#EF4444` | `#DC2626` | Error banners, critical alerts |

---

## 4. Typography Scale

Disciplined weight and scale hierarchy:

| Style | Size | Weight | Line Height | Tracking | Usage |
|---|---|---|---|---|---|
| `headlineSmall` | 20sp | SemiBold (600) | 26sp | -0.2sp | Screen titles |
| `titleLarge` | 17sp | SemiBold (600) | 22sp | 0 | Dialog titles, hero headings |
| `titleMedium` | 15sp | Medium (500) | 20sp | 0 | Setting item titles |
| `titleSmall` | 13sp | SemiBold (600) | 18sp | 0 | Grouped subtitles, highlighted rows |
| `bodyMedium` | 13sp | Normal (400) | 18sp | 0 | Item descriptions, explanations |
| `bodySmall` | 12sp | Normal (400) | 16sp | 0 | Supporting hints, secondary metadata |
| `labelSmall` | 11sp | Bold (700) | 14sp | +1.0sp | Uppercase section headers |

---

## 5. Spacing Scale

Rhythm based on logical grouping rather than mechanical gaps:

| Token | Value | Applied To |
|---|---|---|
| `xxs` | 2dp | Micro vertical margins |
| `xs` | 4dp | Icon-to-text spacing, chip padding |
| `sm` | 8dp | In-group item spacing, switch gaps |
| `md` | 12dp | Setting row vertical padding |
| `lg` | 16dp | Outer screen margins, container padding |
| `xl` | 20dp | Dialog interior padding |
| `xxl` | 24dp | Top margin above section headers |
| `section` | 28dp | Separation between unrelated feature domains |

---

## 6. Shape System

Restrained, non-bubbly shapes:
- **Small Controls (`8dp`)**: Buttons, text fields, chips, selectors.
- **Medium Containers (`10–12dp`)**: Informative status banners, grouped utility cards.
- **Dialogs & Sheets (`20dp`)**: Modals and full-screen bottom sheets.
- **Avoided**: Blanket 16–20dp on everyday list items and cards.
