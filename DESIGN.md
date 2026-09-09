# NEXIQ — Product Design System

## 1. Visual Philosophy & Brand Identity

**NEXIQ** is an **instant on-screen visual translation utility**.
Tagline: **"Understand what's on your screen."**

The design communicates:
- **Speed & Precision**: Immediate capture and translation with minimal visual distraction.
- **Calm & Focus**: Dynamic Light and Dark modes that respect user context and prevent visual fatigue.
- **Intelligent Simplicity**: Clear, human-centric status communication without developer jargon.
- **Android-Native Polish**: Full alignment with Material 3 principles while retaining an ownable brand silhouette.
- **Zero-Knowledge Privacy**: Explicit guarantee of RAM-only processing with zero data collection.

---

## 2. Color System

The color system transitions away from harsh monolithic navy and electric cyan overload toward a curated, WCAG AA compliant **Deep Slate & Azure** palette.

| Token Name | Hex Code | Role / Usage |
|---|---|---|
| `SurfaceBase` | `#0B0F17` | Root window background, deep calm backdrop |
| `SurfaceContainer` | `#151D2A` | Primary content panels, app bars, dialog containers |
| `SurfaceCard` | `#1E293B` | Grouped preference cards, elevated control surfaces |
| `SurfaceCardSubtle` | `#182234` | Inset setting rows, secondary containers |
| `Primary` | `#0284C7` | Key brand accent, primary CTA background, focus indicators |
| `PrimaryHover` | `#0369A1` | Pressed / active CTA state |
| `PrimaryLighter` | `#38BDF8` | Text links, active glyph highlights, subtle status accents |
| `TextPrimary` | `#F8FAFC` | Screen titles, setting row titles, primary labels |
| `TextSecondary` | `#94A3B8` | Descriptions, supporting metadata, inactive switch labels |
| `TextTertiary` | `#64748B` | Footers, subtle hints, disabled states |
| `BorderSubtle` | `#26354A` | Card borders, horizontal item dividers |
| `BorderFocus` | `#38BDF8` | Active element outline, focused selection state |
| `SuccessContainer` | `#064E3B` | Reassuring permission / status active background |
| `Success` | `#10B981` | Positive readiness checkmarks, active status dots |
| `WarningContainer` | `#451A03` | Attention / notice container |
| `Warning` | `#F59E0B` | Guide banners, security unlock guidance |
| `ErrorContainer` | `#4C0519` | Critical failure container |
| `Error` | `#EF4444` | Overlay error banner, missing essential permission |

---

## 3. Typography Scale

Screen Translator uses Android's native system typeface (`Roboto` / System Sans) with disciplined weight and scale distribution to avoid making every label bold.

| Style | Size | Weight | Line Height | Tracking | Usage |
|---|---|---|---|---|---|
| `DisplayTitle` | 20sp | SemiBold (600) | 26sp | -0.01em | App bar title, major feature headings |
| `SectionHeader` | 13sp | Bold (700) | 18sp | +0.03em | Uppercase grouped section markers |
| `ItemTitle` | 15sp | Medium (500) | 20sp | 0 | Setting item titles, card headings |
| `BodyText` | 13sp | Normal (400) | 18sp | 0 | Descriptions, status explanations, guide steps |
| `SupportingText` | 12sp | Normal (400) | 16sp | 0 | Subtitles, secondary metadata, tips |
| `BadgeText` | 11sp | SemiBold (600) | 14sp | +0.02em | Language capsules, status badges, chip tags |

---

## 4. Spacing & Grid System

All layouts adhere strictly to an **8dp baseline grid** with **4dp micro-increments**.

| Token | Value | Applied To |
|---|---|---|
| `SpaceXSmall` | 4dp | Icon-to-text spacing, micro badge padding |
| `SpaceSmall` | 8dp | Between adjacent items in a row, chip gaps |
| `SpaceMedium` | 12dp | Content padding inside rows, dialog item spacing |
| `SpaceLarge` | 16dp | Outer screen margin, grouped card inner padding |
| `SpaceXLarge` | 20dp | Section separation between unrelated feature cards |
| `SpaceSection` | 24dp | Major layout segment spacing |

---

## 5. Corner Radius Hierarchy

Adheres to the nested radius law: `inner = outer - padding`.

| Token | Radius | Applied To |
|---|---|---|
| `RadiusLarge` | 16dp | Grouped feature cards, bottom sheet containers |
| `RadiusMedium` | 12dp | Primary action buttons, dialogs, status badges |
| `RadiusSmall` | 8dp | Inset chips, dropdown triggers, inner thumbnail previews |
| `RadiusPill` | 999dp | Floating bubble, language capsule, status indicator pills |

---

## 6. Iconography Standards

- **Optical Size**: Standard 20dp for inline action icons, 24dp for prominent headers, 16dp for auxiliary indicators.
- **Stroke Weight**: Optical consistency of 1.75dp - 2.0dp stroke.
- **Fill / Tone**: Default icons inherit `TextSecondary` (`#94A3B8`); interactive primary actions use `PrimaryLighter` (`#38BDF8`); destructive / warning use semantic tokens.
- **Brand Glyph**: A distinct dual-lens viewport bracket with a flowing translation bridge — representing seamless on-screen text transformation.

---

## 7. Floating Bubble System

The floating bubble is an always-accessible gateway to on-screen translation:
- **Size**: 52dp diameter with refined ambient elevation (6dp).
- **Icon**: Crisp brand transformation glyph centered within 26dp viewport.
- **Interaction States**:
  - **Idle**: 100% opacity for 3 seconds, gracefully fading to 35% opacity to stay unobtrusive over full-screen content.
  - **Touched / Dragged**: Restores to 100% opacity with a snappy 1.08x scale up.
  - **Over Dismiss Target**: Dismiss target scales up to 64dp with a magnetic red glow (`#EF4444`); bubble pulses subtly.
  - **Released**: Snaps smoothly to the nearest screen edge with a damped overshoot curve.
- **Themes**:
  1. **Brand Azure** (`cyan`): Primary `#0284C7` gradient with luminous `#38BDF8` ring.
  2. **Obsidian Dark** (`dark`): Deep `#1E293B` slate with subtle `#475569` border — ideal for bright apps.
  3. **Pearl Light** (`pearl`): Porcelain `#F8FAFC` background with `#0F172A` glyph — ideal for dark mode apps.
  4. **Indigo Royale** (`indigo`): Royal `#4F46E5` gradient with `#818CF8` ring.
- **Customization**:
  - **Dynamic Size Slider**: 40dp to 72dp (default 52dp) with real-time preview and live window manager overlay synchronization.
  - **Icon Styles**:
    - `brand`: **N-Lens Monogram** (Fusion of 'N' aperture + refraction prism + 'Q' focus iris).
    - `lens`: Minimal camera lens.
    - `glyph`: International translation glyph.
    - `aperture`: Geometric 6-blade camera shutter.

---

## 8. Translation Overlay Controls

- **Top Bar**: Floating blurred capsule (`#151D2AE6`) containing:
  - Source Language Badge (with Auto-detection feedback)
  - Transformation Arrow (`#38BDF8`)
  - Target Language Badge
  - Scissors Crop Toggle (distinct active state)
  - Save Image Action (Original vs Translated selector)
  - Circular Close Action
- **Touch Targets**: Minimum 48dp x 48dp touch areas for all interactive controls.
- **Z-Index Order**: Top bar always elevated above the crop canvas and lens text renderer to guarantee the user can exit or adjust settings at any moment.

---

## 9. NEXIQ Official Brand Identity: Option 1 (Chubby Flat Vector & Ice-Cyan Theme)

The official NEXIQ brand mark establishes an unmistakable, playful yet professional connection between the app name and visual identity:
- **Background**: Fresh Bright Ice-Cyan / Soft Aquamarine (`#E0F7FA` to `#CCFBF1`), providing a colorful, cheerful, non-white and non-dark canvas.
- **Chunky Viewfinder Brackets (Deep Ocean Cyan `#0284C7`)**: 4 thick rounded corner brackets representing real-time screen capture and scanning.
- **Chubby Pill-Rounded Letter 'N' (Vibrant Warm Coral `#FF5A36`)**: Bold, plump, friendly pill-geometry representing **NEX** in **NEXIQ**, positioned slightly upper-left to balance the composition.
- **Magnifying Glass forming 'Q' (Warm Champagne Gold `#E5B869`)**: Positioned below and to the right of 'N', inspecting text with a chunky 45° handle forming the letter **Q** (Intelligence / IQ).
- **Bilingual Translation Glyphs (`イ` ➔ `I`)**: Larger Japanese Katakana **「イ」** translating directly into a smaller Latin serif **「I」**, incorporating **N**, **I**, and **Q** (`NIQ` -> **NEXIQ**).
- **Color System Harmonization**:
  - `SurfaceBase`: Fresh Ice-Cyan Tint (`#F0F9FB` Light) / Ocean Midnight Slate (`#0B131B` Dark).
  - `BrandPrimary`: Deep Ocean Cyan (`#0284C7`), pressed (`#0369A1`), lighter (`#00AEEF`).
  - `BrandAccent`: Vibrant Warm Coral (`#FF5A36`).
  - `BrandGold`: Warm Champagne Gold (`#E5B869`).
- **Brand Resonance**: Unified across launcher icons, Android 13+ monochrome themed icons, Quick Settings tile, top app bar logo squircle, welcome modal hero, and floating overlay bubble.



