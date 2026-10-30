# Development context

This repository is a native Kotlin / Jetpack Compose Android app. Start with [docs/README.md](docs/README.md) for the documentation map.

- Before changing behavior, read [PRODUCT.md](PRODUCT.md), [GLOSSARY.md](GLOSSARY.md) and the relevant requirement in [docs/specs/mvp.md](docs/specs/mvp.md).
- Before changing data import, persistence, synchronization or checkout, read [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) and the relevant [ADRs](docs/adr/).
- Before changing UI, read [DESIGN.md](DESIGN.md); update it and [.impeccable/design.json](.impeccable/design.json) together when design rules or tokens change.
- For build, tests, emulator checks and APK delivery, follow [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md).

Update affected documentation in the same change as the implementation, using the maintenance table in [docs/README.md](docs/README.md). Describe current behavior separately from proposed work. Record verification with its date, artifact and actual scope; retain historical release evidence.

Keep schedule reads available during refresh and preserve cached data on import failure. Checkout belongs to the cinema's browser flow. Code and configuration determine executable behavior; when documentation disagrees, inspect the implementation and resolve the discrepancy explicitly.
