# Local snapshots with source-owned checkout

Accepted 2026-10-30.

Use Kotlin/Compose and a small explicit JSON cache written with atomic replacement, exposed as StateFlow. A single repository serializes refreshes and local bookmark mutations. Each validated schedule day is committed as it arrives; failed requests preserve the prior day. Imported metadata and poster URLs belong to Charlie, without an API key or invented film matches. Daily HTML rows supply checkout/room/version; associated ScreeningEvent JSON-LD supplies full posters and durations. Header validation rejects a wrong date or generic error page.

WorkManager schedules network-constrained work every six hours, subject to Android power policy. Foreground stale reads trigger refresh; the current snapshot remains usable. Film metadata and poster prefetch are separate from the critical schedule fetch. Poster cache lives in private app files with an 80 MB limit.

Ticket checkout opens an exact allowlisted HTTPS URL in a browser Custom Tab. The browser owns cookies, payment and authentication; the app never receives card data. Filmweb uses clearly labelled search without claiming a verified film identity.

Tradeoffs: HTML is a third-party contract that can change, and first use needs a connection. No background refresh after Android force-stop until the app is opened again. Availability and prices are confirmed only by the cinema checkout.
