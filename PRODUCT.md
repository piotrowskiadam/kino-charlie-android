# Charlie — repertuar

<!-- impeccable:product-schema 1 -->

## Platform

android

## Stack

Kotlin and Jetpack Compose, explicitly selected by the user. Standalone installable APK.

## Users

Polish-speaking visitors of Kino Charlie in Łódź who want to quickly choose a screening on a phone.

## Product Purpose

Browse the cinema schedule quickly, including without a network connection after synchronization. Open the cinema's existing checkout for a selected screening.

## Operating Context

The public cinema website is https://www.charlie.pl/. It publishes per-day HTML schedules, ScreeningEvent structured data, posters, film details and individual ticket links. Checkout remains online.

## Capabilities and Constraints

- Native schedule browsing, date selection, film details, saved films and bottom navigation.
- Quiet background synchronization and local persistence, fetching up to 14 days ahead where the cinema publishes screenings.
- Uniform poster presentation; prefer source posters for MVP, without requiring third-party API credentials.
- Filmweb link should identify whether it opens search rather than a verified film page.
- No account, custom backend or payment processing in this app.
- Unofficial project; do not imply endorsement by the cinema.
- Android 8+ is the implemented minimum; phone is the primary target, with adaptive tablet navigation.

## Evidence on Hand

Public HTML captured on 2026-10-30 is retained in `app/src/test/resources/schedule-2026-10-30.html` and `app/src/test/resources/film-1787938780.html`. Release verification is recorded in `docs/QA.md`; documentation navigation and maintenance are in `docs/README.md`.

## Product Principles

- Show cached content immediately.
- Preserve useful local data when network refresh fails.
- Never invent screenings, availability or ticket prices.
- Keep choosing a screening separate from the online payment flow.
- Test import, offline persistence and real UI interactions (confirmed by user).
