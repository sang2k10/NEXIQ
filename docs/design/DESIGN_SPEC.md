# NEXIQ — Product Design Specification

## 1. Visual Philosophy & Brand Identity

**NEXIQ** is an **instant on-screen visual translation utility**.  
Tagline: **"Translate what's on your screen."**

The design communicates:
- **Speed & Precision**: Immediate capture and translation with minimal visual distraction.
- **Calm & Focus**: Dynamic Light and Dark modes that respect user context and prevent visual fatigue.
- **Intelligent Simplicity**: Clear, human-centric status communication without developer jargon.
- **Android-Native Polish**: Full alignment with Material 3 principles while retaining an ownable brand silhouette.
- **Transparent Privacy**: Clear, implementation-driven disclosure of RAM-only processing and optional cloud pathways.

---

## 2. Color System

The color system uses a WCAG AA compliant **Deep Slate & Azure** palette:

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

NEXIQ uses Android's native system typeface (`Roboto` / System Sans) with disciplined weight and scale distribution:

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

All layouts adhere strictly to an **8dp baseline grid** with **4dp micro-increments**:

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

Adheres to the nested radius law: `inner = outer - padding`:

| Token | Radius | Applied To |
|---|---|---|
| `RadiusLarge` | 16dp | Grouped feature cards, bottom sheet containers |
| `RadiusMedium` | 12dp | Primary action buttons, dialogs, status badges |
| `RadiusSmall` | 8dp | Inset chips, dropdown triggers, inner thumbnail previews |
| `RadiusPill` | 999dp | Floating bubble, language capsule, status indicator pills |

---

## 6. Iconography & Brand Standards

- **Brand Symbol**: Warm Coral plump pill "N" nestled inside an Iceberg Cyan viewfinder, paired with an Champagne Gold magnifying glass translating `イ` $\to$ `I`.
- **Optical Size**: Standard 20dp for inline action icons, 24dp for prominent headers, 16dp for auxiliary indicators.
- **Stroke Weight**: Optical consistency of 1.75dp - 2.0dp stroke.
- **Touch Targets**: Standard 48dp minimum touch target bounding boxes.
