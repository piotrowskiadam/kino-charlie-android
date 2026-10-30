---
name: Charlie — repertuar
description: Native cinema program with monochrome text, ivory light surfaces and true-black OLED dark surfaces.
colors:
  light-primary: '#000000'
  light-on-primary: '#FFFFFF'
  light-primary-container: '#742D3A'
  light-on-primary-container: '#FFFFFF'
  light-secondary: '#000000'
  light-on-secondary: '#FFFFFF'
  light-secondary-container: '#F1E8DB'
  light-on-secondary-container: '#000000'
  light-surface: '#F8F4EC'
  light-on-surface: '#000000'
  light-on-surface-variant: '#000000'
  light-background: '#F8F4EC'
  light-on-background: '#000000'
  light-on-error-container: '#000000'
  light-surface-container: '#F1EAE0'
  light-surface-container-low: '#F5EFE6'
  light-surface-container-highest: '#EDE5DB'
  light-outline: '#84726B'
  light-outline-variant: '#DCD0C7'
  light-error: '#AD2530'
  dark-primary: '#FFFFFF'
  dark-on-primary: '#000000'
  dark-primary-container: '#A83E57'
  dark-on-primary-container: '#FFFFFF'
  dark-secondary: '#FFFFFF'
  dark-on-secondary: '#000000'
  dark-secondary-container: '#000000'
  dark-on-secondary-container: '#FFFFFF'
  dark-tertiary: '#FFFFFF'
  dark-on-tertiary: '#000000'
  dark-tertiary-container: '#000000'
  dark-on-tertiary-container: '#FFFFFF'
  dark-background: '#000000'
  dark-on-background: '#FFFFFF'
  dark-surface: '#000000'
  dark-on-surface: '#FFFFFF'
  dark-on-surface-variant: '#FFFFFF'
  dark-surface-variant: '#000000'
  dark-surface-tint: '#000000'
  dark-surface-dim: '#000000'
  dark-surface-bright: '#000000'
  dark-surface-container-lowest: '#000000'
  dark-surface-container-low: '#000000'
  dark-surface-container: '#000000'
  dark-surface-container-high: '#000000'
  dark-surface-container-highest: '#000000'
  dark-inverse-surface: '#000000'
  dark-inverse-on-surface: '#FFFFFF'
  dark-inverse-primary: '#FFFFFF'
  dark-error: '#FFFFFF'
  dark-on-error: '#000000'
  dark-error-container: '#000000'
  dark-on-error-container: '#FFFFFF'
  dark-outline: '#999999'
  dark-outline-variant: '#444444'
  dark-scrim: '#000000'
typography:
  headline-large:
    fontFamily: Literata
    fontSize: 30sp
    fontWeight: 600
    lineHeight: 38sp
  headline-small:
    fontFamily: Literata
    fontSize: 24sp
    lineHeight: 32sp
  title-large:
    fontFamily: Literata
    fontSize: 22sp
    fontWeight: 600
    lineHeight: 29sp
  title-medium:
    fontFamily: Literata
    fontSize: 18sp
    fontWeight: 600
    lineHeight: 25sp
rounded:
  r8: 8dp
  r10: 10dp
  r12: 12dp
  r14: 14dp
spacing:
  s4: 4dp
  s8: 8dp
  s12: 12dp
  s14: 14dp
  s16: 16dp
  s20: 20dp
  s24: 24dp
  s32: 32dp
components:
  screening-button:
    textColor: '{colors.light-primary}'
    rounded: '{rounded.r10}'
    padding: 8dp 12dp
  date-selected:
    backgroundColor: '{colors.light-primary-container}'
    textColor: '{colors.light-on-primary-container}'
    rounded: '{rounded.r12}'
    padding: 10dp 12dp
  date-idle:
    backgroundColor: '{colors.light-surface-container-low}'
    textColor: '{colors.light-on-surface}'
    rounded: '{rounded.r12}'
    padding: 10dp 12dp
  poster:
    backgroundColor: '{colors.light-surface-container-highest}'
    rounded: '{rounded.r8}'
    width: 82dp
    height: 123dp
  search:
    rounded: '{rounded.r14}'
  checkout-button:
    backgroundColor: '{colors.light-primary-container}'
    textColor: '{colors.light-on-primary-container}'
  navigation:
    backgroundColor: '{colors.light-surface-container-low}'
    textColor: '{colors.light-primary}'
---

# Design System: Charlie — repertuar

## Overview

**Creative North Star: "Pocket cinema program"**

Charlie uses the approved pocket cinema program: literary headings, warm paper surfaces, restrained burgundy accents and native Android controls. One continuous sheet keeps complete film titles and exact screening times easy to scan. It is a native Kotlin/Jetpack Compose Material 3 implementation; dp and sp in the frontmatter are Android units, not CSS pixels.

The user selected variant A in `docs/design/approved-mvp-variants.png`. The shipped interface adapts that board to Android. Actual source images replace illustrative decision-board art; no fabricated film artwork ships.

**Key Characteristics:**
- Literata headings paired with native sans controls and numerals.
- Continuous program rows, restrained separators and a 2:3 poster silhouette.
- Truthful cached status, complete titles and visibly named room groups with directly actionable screening times.
- System dark theme, font scaling, Android Back and adaptive navigation.

The preserved direction contract is: **THESIS:** choose a film and its exact screening quickly from a locally saved schedule. **OWN-WORLD:** printed pocket program translated into Literata, warm ivory, burgundy and native sans. **STORY:** saved status → date → film → time → focused screening sheet → source-owned checkout. **FIRST VIEWPORT (cached):** compact cinema logo and refresh, “Wybierz swój seans.”, cache status, day picker, search and film rows. **FORM:** portrait image at left, complete title and times at right; phone navigation becomes a tablet rail.

Decision evidence: concept seed `86bd95f5`, direction/operate, successfully retried through the API with approved pool `c3b204a1eed6`, assigned grounded candidate 6 (pocket program). Grounded alternatives were cinema gazette, velvet poster gallery, Swiss poster grid, tram timetable, filmstrip archive, pocket program and film-club ledger. Poster gallery reduced schedule density; departure table repeated films. This record preserves the approved choice, not additional shipped variants.

The native adaptation uses emulator capture evidence. No automated pixel diff or comp-build-state gate was run; HTML detection is inapplicable to this Android artifact. The JSON sidecar's HTML/CSS snippets are illustrative light-theme panel previews translated from Compose, not application code or evidence of pixel equivalence. Their one-dp-to-one-CSS-pixel mapping cannot reproduce device density, font scaling or Material interaction rendering.

## Colors

Warm ivory and burgundy set a quiet printed-program character. The frontmatter records the explicit source colors for both system themes; unlisted Material roles retain library defaults.

### Primary

Primary and ordinary foreground roles are pure black in light mode and pure white in dark mode. Burgundy primary-container carries selected dates, filled actions and the navigation indicator; its white foreground preserves contrast. Dark burgundy is lightened enough to remain distinct from OLED black. ProgramButton and ProgramTextButton apply this policy consistently, including neutral backgrounds for disabled actions.

### Secondary

Secondary foregrounds follow the same black/white policy. Light secondary-container remains warm beige; dark neutral containers are black.

### Neutral

Paper surfaces and pure black text carry the light program; pure black surfaces and white text carry OLED dark mode. Surface-container-low supports date tiles and navigation; surface-container-highest backs poster placeholders. On-surface-variant equals the pure foreground; metadata hierarchy uses size and weight. Outline-variant draws row separators. Outline is the stronger field/control stroke. The explicit light error color is reserved for errors; remaining error roles inherit Material defaults.

**The Selection Rule.** Use primary-container/on-primary-container for the selected day, filled actions and compact navigation indicator; leave film rows on the common surface.

## Typography

**Text color:** ordinary headings, body, metadata and links use #000000 in light mode and #FFFFFF in OLED mode. Inverse white captions on filled controls remain a contrast exception in light mode.

**Display Font:** bundled Literata (`app/src/main/res/font/literata.ttf`, SIL OFL).
**Body Font:** native Android sans, inherited from Material 3 Typography.

Literata supplies the literary program voice. Native sans keeps metadata, controls and numerals familiar. The frontmatter records only explicit heading overrides, rather than fabricating tokens for inherited library defaults.

### Hierarchy

- **Headline large:** main page heading; explicit semibold. The header uses the official cinema mark rather than a typed wordmark.
- **Headline small:** film detail and screening sheet title; other properties inherit Material headlineSmall.
- **Title large:** section and empty-state headings and the selected room in the screening sheet; explicit semibold. Date numerals locally use this role with native sans.
- **Title medium:** film titles, date heading, detail toolbar and room names in detail screening rows; explicit semibold.
- **Body and labels:** Material bodyLarge/bodyMedium/bodySmall and labelLarge/labelMedium, inherited without local scale changes. Screening times and room group labels locally use semibold labelLarge.

**The Complete Title Rule.** Critical film names wrap; the implementation sets no title truncation or maximum line count.

## Layout

The phone uses a single scrolling program with 24dp horizontal gutters. Film rows use a fixed 82×123dp portrait poster, a 14dp image/text gap and 14dp vertical padding. Detail posters enlarge to 110×165dp. Headings and supporting sections use the frontmatter spacing rhythm; small metadata offsets of 3dp and status gaps of 6dp are local exceptions rather than new shared scales.

Screenings are grouped by room, with 8dp above the group collection, 12dp between groups and 4dp from a visible semibold Material labelLarge room name to its controls. Screening controls flow into additional rows with 8dp horizontal and vertical spacing. Their minimum height is 48dp; checkout uses a 52dp minimum. Day tiles have a 60dp minimum width and a horizontally scrolling 14-day picker. Long text can increase height, including under Android font scaling.

**The Room Rule.** Show the room name before the user chooses a screening time, on both schedule and saved-film lists. Repeat it prominently in film details and the screening sheet; equal times in different rooms remain separate choices.

At available width ≥600dp, the app uses a navigation rail and a two-column film list with a 24dp column gap. Below that threshold it uses bottom navigation. Detail and screening views remain focused single flows. Scaffold and status-bar insets preserve safe drawing areas.

## Elevation & Depth

The program is flat at rest: paper tones, separators and grouping carry depth instead of custom shadows or gradients. Bottom navigation explicitly sets tonal elevation to zero. Material 3 supplies the standard modal sheet and control state treatment; those defaults are not an app-specific shadow or animation vocabulary. No custom entrance animation is implemented.

**The Continuous Sheet Rule.** Separate films with outline-variant dividers rather than nesting each film in a raised card.

## Shapes

Posters use the 8dp corner token. Screening outlines use 10dp corners; date tiles and error notices use 12dp corners; search uses 14dp corners. Default filled, text and general outlined buttons keep their inherited Material shapes. Preserve the repeated portrait silhouette and avoid extrapolating one universal radius from these distinct controls.

## Components

### Buttons

Screening times are outlined monochrome actions with a 1dp primary stroke, semibold Material labelLarge and the recorded screening padding/corner tokens. Each room's times sit under a visible “Sala <name>” label; the button's accessible name includes the time and room. A time opens a focused modal sheet; the row itself opens film details. Checkout is a full-width Material filled button with a confirmation icon and explicit Polish action, disabled when offline or without a permitted source URL. General external actions use Material buttons, outlined buttons or text buttons as implemented. Android supplies pressed, focus, ripple and disabled states; no custom hover animation is defined.

### Date tiles

The selected day uses primary-container/on-primary-container; idle days use surface-container-low/on-surface. Compact localized labels sit above native sans date numerals. A semantic selected state accompanies the visible fill change.

### Program rows and posters

Rows share the app surface, with a top divider and no raised-card shell. Poster cropping is centered with ContentScale.Crop. The same title/metadata/room/time order persists in saved films, with a date prefix on saved screening times and up to three upcoming screenings per room, preserving every available room. Known source limitation: some full poster URLs return HTTP 404. Poster first requests the source poster, then a ticketing-system image matched by event, date, time and title. Tiny legacy thumbnails are not enlarged. If neither image loads, the tonal backing and outlined theater icon remain; no fabricated art is generated or shipped.

### Search

A full-width, single-line outlined field uses the search corner token, a search icon, “Szukaj filmu” placeholder and an accessible clear button when populated. Matching normalizes Polish accents. Material provides field outline and focus states.

### Navigation

“Repertuar”, “Zapisane” and “Kino” pair Material outlined icons with Polish labels. Phone navigation rests on surface-container-low with an on-surface selected label, white selected icon and burgundy primary-container indicator. The wide rail uses the same explicit selection colors.

### Status and screening sheet

A factual label pairs cache age or connection state with an outlined status icon; refresh shows a small progress indicator. Errors retain cached content and offer retry. Detail screening rows use primary titleMedium room names above the date and time. The standard Material modal sheet lists the film and date/time, then emphasizes the selected room with primary titleLarge before the online-purchase explanation and checkout action. Keep offline and missing-link states explicit.

### Welcome and cinema identity

Read local cache before choosing a first-run surface; returning cached users see their program directly. With no cache, use the same paper surface, official 104dp-high mark, Literata headline, native body text and an honest indeterminate preparation indicator. Center a scrollable column capped at 480dp, with 24dp safe gutters and 32/12dp grouping gaps. No forced duration, invented percentage or blocking animation. Release today immediately after its valid snapshot, including a published empty day; future days continue in the background. First-use failure has an explicit connection/import message and a 52dp-minimum retry action.

The main header uses a 56dp-high official logo in its original black positive, with an accessible cinema name. On dark surfaces it rests on a white plaque with 6dp corners, a 1dp outline-variant border and 4dp padding inside the unchanged outer height; the artwork preserves its aspect ratio. Do not reverse its black and white regions. `CinemaLogo.kt` displays the mark region from the original bundled source JPEG, excluding the surrounding website captions at draw time. It samples the bitmap to avoid decoding the full original on each composition. Provenance: [asset notice](NOTICE.md). The adaptive launcher icon is a separate crisp black vector bowler-hat motif with a narrow 8/108-wide toothbrush moustache on paper, with a monochrome variant on Android 13+. It is an app icon, not a replacement cinema logo.

### App updates

The Kino destination contains a flat “Aktualizacje aplikacji” section separated by the existing 24dp divider rhythm. Title uses titleLarge; installed version, progress, recovery and browser handoff use native body text. A newer release has primary titleMedium emphasis, a full-width 52dp-minimum download button, release-notes link and manual check. Offline keeps known release information but disables network actions and explains recovery. A small primary badge on Kino has an accessible “Dostępna aktualizacja aplikacji” description on both bottom bar and rail. No modal prompt, forced update or schedule-blocking overlay.

### OLED surfaces

Every dark neutral surface role, background, container tier and tint is #000000. This includes inactive day tiles, navigation, poster backing, field fills and screening sheets; sheet tonal elevation is zero. MainActivity selects explicit black navigation styling and disables Android's navigation contrast scrim; night window resources also start on black. Thin outlines, text, image artwork, active burgundy fills and the white positive-logo plaque are intentional foreground/selection exceptions, not neutral backgrounds. Disabled actions retain pure foreground text; their background, interaction semantics and offline explanations distinguish availability.

## Do's and Don'ts

### Do:
- **Do** use Literata for the recorded heading roles and native sans for body, controls and date numerals.
- **Do** preserve full Polish titles, localized dates and explicit action labels.
- **Do** expose room names before time selection, preserve all room groups in saved-film previews and repeat the selected room before checkout.
- **Do** keep source imagery at a 2:3 ratio and use the verified ticket image when the primary poster fails.
- **Do** retain 8dp spacing between wrapped screening buttons and a minimum 48dp screening target.
- **Do** distinguish unpublished days, empty search, first download, cached content and connection errors.

### Don't:
- **Don't** substitute invented artwork, screenings, prices or availability for missing source data.
- **Don't** promote the decision board's illustrative imagery into production assets.
- **Don't** add ornamental shadows, gradients or entrance animations to the continuous program sheet.
- **Don't** truncate critical film names or make checkout look like an in-app payment flow.
