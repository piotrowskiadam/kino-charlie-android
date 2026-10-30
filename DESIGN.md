---
name: Charlie — repertuar
description: Native Android pocket cinema program in warm ivory and burgundy.
colors:
  light-primary: '#742D3A'
  light-on-primary: '#FFFFFF'
  light-primary-container: '#F2DDE0'
  light-on-primary-container: '#52212C'
  light-secondary: '#705447'
  light-secondary-container: '#F1E8DB'
  light-on-secondary-container: '#42362E'
  light-surface: '#F8F4EC'
  light-on-surface: '#28201F'
  light-on-surface-variant: '#665952'
  light-surface-container: '#F1EAE0'
  light-surface-container-low: '#F5EFE6'
  light-surface-container-highest: '#EDE5DB'
  light-outline: '#84726B'
  light-outline-variant: '#DCD0C7'
  light-error: '#AD2530'
  dark-primary: '#F0B8C2'
  dark-on-primary: '#4A1723'
  dark-primary-container: '#5D2633'
  dark-on-primary-container: '#FFD9DF'
  dark-secondary: '#D6BAA6'
  dark-secondary-container: '#4D4036'
  dark-on-secondary-container: '#F2E4D4'
  dark-surface: '#1D1918'
  dark-on-surface: '#F3EAE2'
  dark-on-surface-variant: '#CEBEB4'
  dark-surface-container: '#292321'
  dark-surface-container-low: '#241F1D'
  dark-surface-container-highest: '#38302C'
  dark-outline: '#A79890'
  dark-outline-variant: '#4D413C'
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
    backgroundColor: '{colors.light-primary}'
    textColor: '{colors.light-on-primary}'
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
    backgroundColor: '{colors.light-primary}'
    textColor: '{colors.light-on-primary}'
  navigation:
    backgroundColor: '{colors.light-surface-container-low}'
    textColor: '{colors.light-primary}'
---

# Design System: Charlie — repertuar

## Overview

**Creative North Star: "Pocket cinema program"**

Charlie uses the approved pocket cinema program: literary headings, warm paper surfaces, restrained burgundy accents and native Android controls. One continuous sheet keeps complete film titles and exact screening times easy to scan. It is a native Kotlin/Jetpack Compose Material 3 implementation; dp and sp in the frontmatter are Android units, not CSS pixels.

The user selected variant A in `.impeccable/mocks/decision/variants.png`. The shipped interface adapts that board to Android. Actual source images replace illustrative decision-board art; no fabricated film artwork ships.

**Key Characteristics:**
- Literata headings paired with native sans controls and numerals.
- Continuous program rows, restrained separators and a 2:3 poster silhouette.
- Truthful cached status, complete titles and directly actionable screening times.
- System dark theme, font scaling, Android Back and adaptive navigation.

The preserved direction contract is: **THESIS:** choose a film and its exact screening quickly from a locally saved schedule. **OWN-WORLD:** printed pocket program translated into Literata, warm ivory, burgundy and native sans. **STORY:** saved status → date → film → time → focused screening sheet → source-owned checkout. **FIRST VIEWPORT:** compact brand and refresh, “Wybierz swój seans.”, cache status, day picker, search and film rows. **FORM:** portrait image at left, complete title and times at right; phone navigation becomes a tablet rail.

Decision evidence: concept seed `86bd95f5`, direction/operate, successfully retried through the API with approved pool `c3b204a1eed6`, assigned grounded candidate 6 (pocket program). Grounded alternatives were cinema gazette, velvet poster gallery, Swiss poster grid, tram timetable, filmstrip archive, pocket program and film-club ledger. Poster gallery reduced schedule density; departure table repeated films. This record preserves the approved choice, not additional shipped variants.

The native adaptation uses emulator capture evidence. No automated pixel diff or comp-build-state gate was run; HTML detection is inapplicable to this Android artifact. The JSON sidecar's HTML/CSS snippets are illustrative light-theme panel previews translated from Compose, not application code or evidence of pixel equivalence. Their one-dp-to-one-CSS-pixel mapping cannot reproduce device density, font scaling or Material interaction rendering.

## Colors

Warm ivory and burgundy set a quiet printed-program character. The frontmatter records the explicit source colors for both system themes; unlisted Material roles retain library defaults.

### Primary

Burgundy (light primary) marks headings, selected dates, screening outlines and actions. Pale rose is the dark primary; its darker on-primary value keeps selected controls readable. The primary-container pair supports navigation selection and Material surfaces.

### Secondary

Brown and warm beige secondary roles support restrained Material accents. Their dark-theme counterparts retain the same warm relationship.

### Neutral

Paper surface and dark ink carry the main program. Surface-container-low supports date tiles and navigation; surface-container-highest backs poster placeholders. On-surface-variant quiets metadata, while outline-variant draws row separators. Outline is the stronger field/control stroke. The explicit light error color is reserved for errors; remaining error roles inherit Material defaults.

**The Selection Rule.** Use filled primary for the selected day and primary-container for the compact navigation indicator; leave film rows on the common surface.

## Typography

**Display Font:** bundled Literata (`app/src/main/res/font/literata.ttf`, SIL OFL).
**Body Font:** native Android sans, inherited from Material 3 Typography.

Literata supplies the literary program voice. Native sans keeps metadata, controls and numerals familiar. The frontmatter records only explicit heading overrides, rather than fabricating tokens for inherited library defaults.

### Hierarchy

- **Headline large:** main page heading; explicit semibold. The compact “charlie” wordmark locally overrides its font size to 34sp and retains the heading's line height.
- **Headline small:** film detail and screening sheet title; other properties inherit Material headlineSmall.
- **Title large:** section and empty-state headings; explicit semibold. Date numerals locally use this role with native sans.
- **Title medium:** film titles, date heading and detail toolbar; explicit semibold.
- **Body and labels:** Material bodyLarge/bodyMedium/bodySmall and labelLarge/labelMedium, inherited without local scale changes. Screening times locally use semibold labelLarge.

**The Complete Title Rule.** Critical film names wrap; the implementation sets no title truncation or maximum line count.

## Layout

The phone uses a single scrolling program with 24dp horizontal gutters. Film rows use a fixed 82×123dp portrait poster, a 14dp image/text gap and 14dp vertical padding. Detail posters enlarge to 110×165dp. Headings and supporting sections use the frontmatter spacing rhythm; small metadata offsets of 3dp and status gaps of 6dp are local exceptions rather than new shared scales.

Screening controls flow into additional rows with 8dp horizontal and vertical spacing and 4dp top padding. Their minimum height is 48dp; checkout uses a 52dp minimum. Day tiles have a 60dp minimum width and a horizontally scrolling 14-day picker. Long text can increase height, including under Android font scaling.

At available width ≥600dp, the app uses a navigation rail and a two-column film list with a 24dp column gap. Below that threshold it uses bottom navigation. Detail and screening views remain focused single flows. Scaffold and status-bar insets preserve safe drawing areas.

## Elevation & Depth

The program is flat at rest: paper tones, separators and grouping carry depth instead of custom shadows or gradients. Bottom navigation explicitly sets tonal elevation to zero. Material 3 supplies the standard modal sheet and control state treatment; those defaults are not an app-specific shadow or animation vocabulary. No custom entrance animation is implemented.

**The Continuous Sheet Rule.** Separate films with outline-variant dividers rather than nesting each film in a raised card.

## Shapes

Posters use the 8dp corner token. Screening outlines use 10dp corners; date tiles and error notices use 12dp corners; search uses 14dp corners. Default filled, text and general outlined buttons keep their inherited Material shapes. Preserve the repeated portrait silhouette and avoid extrapolating one universal radius from these distinct controls.

## Components

### Buttons

Screening times are outlined primary actions with a 1dp primary stroke, semibold Material labelLarge and the recorded screening padding/corner tokens. A time opens a focused modal sheet; the row itself opens film details. Checkout is a full-width Material filled button with a confirmation icon and explicit Polish action, disabled when offline or without a permitted source URL. General external actions use Material buttons, outlined buttons or text buttons as implemented. Android supplies pressed, focus, ripple and disabled states; no custom hover animation is defined.

### Date tiles

The selected day uses primary/on-primary; idle days use surface-container-low/on-surface. Compact localized labels sit above native sans date numerals. A semantic selected state accompanies the visible fill change.

### Program rows and posters

Rows share the app surface, with a top divider and no raised-card shell. Poster cropping is centered with ContentScale.Crop. The same title/metadata/time order persists in saved films, with a date prefix on saved screening times. Known source limitation: some full poster URLs return HTTP 404. Poster first requests the source poster, then switches to the source thumbnail on failure or missing URL. If neither image loads, the tonal backing and outlined theater icon remain; no fabricated art is generated or shipped.

### Search

A full-width, single-line outlined field uses the search corner token, a search icon, “Szukaj filmu” placeholder and an accessible clear button when populated. Matching normalizes Polish accents. Material provides field outline and focus states.

### Navigation

“Repertuar”, “Zapisane” and “Kino” pair Material outlined icons with Polish labels. Phone navigation rests on surface-container-low with primary selected label/icon and primary-container indicator. The wide rail inherits Material rail item states; do not assume its untouched defaults have the same explicit overrides as the bottom bar.

### Status and screening sheet

A factual label pairs cache age or connection state with an outlined status icon; refresh shows a small progress indicator. Errors retain cached content and offer retry. The standard Material modal sheet lists the film, date, time and room before the online-purchase explanation and checkout action. Keep offline and missing-link states explicit.

## Do's and Don'ts

### Do:
- **Do** use Literata for the recorded heading roles and native sans for body, controls and date numerals.
- **Do** preserve full Polish titles, localized dates and explicit action labels.
- **Do** keep source imagery at a 2:3 ratio and fall back to the source thumbnail when a poster fails.
- **Do** retain 8dp spacing between wrapped screening buttons and a minimum 48dp screening target.
- **Do** distinguish unpublished days, empty search, first download, cached content and connection errors.

### Don't:
- **Don't** substitute invented artwork, screenings, prices or availability for missing source data.
- **Don't** promote the decision board's illustrative imagery into production assets.
- **Don't** add ornamental shadows, gradients or entrance animations to the continuous program sheet.
- **Don't** truncate critical film names or make checkout look like an in-app payment flow.
