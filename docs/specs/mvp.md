# MVP: Charlie — repertuar

Deliver an installable Android APK using Kotlin and Jetpack Compose.

## Acceptance

1. Schedule imports actual film titles, dates, times, rooms, source posters and exact booking links from Charlie's public HTML. Parse ISO-8859-2 correctly.
2. Date picker covers today and 13 upcoming days, showing an honest unpublished/empty state where appropriate.
3. Persistent local data appears without waiting for a request. Scheduled network-constrained background refresh, foreground refresh when stale, and manual refresh all use the same importer. A failed refresh cannot erase last valid data.
4. Film details include source metadata, cached description when available, all cached showtimes, save toggle, and clearly labelled Filmweb search.
5. Bottom navigation: Repertuar, Zapisane, Kino. Search and persistent saved films. Adaptive navigation rail on large displays.
6. Ticket selection opens the exact cinema checkout in a browser Custom Tab, falling back to an external browser, with an offline recovery message. Do not handle card data.
7. Loading, offline, stale cache, no results, unpublished dates and network errors have actionable, readable states.
8. Polish language, accessible touch targets, font scaling, dark and light themes, system Back support and safe insets.
9. Automated parser, repository failure/offline, and Compose interaction tests. Build, lint, test and emulator evidence documented honestly.

## Verification seams agreed with user

- HTML import public API: film, showtime, poster and checkout association; source-shape regressions and unsafe links.
- Repository public API: durable offline reads and safe refresh failure.
- UI: date selection, search, saved films and detail/back behavior.

## Source scope

No external poster API key. No invented ratings, verified Filmweb IDs or seat availability. Background execution timing follows Android power and network policy.
