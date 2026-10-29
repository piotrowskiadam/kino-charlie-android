# Charlie: pocket cinema program

Approved by the user: variant A in `.impeccable/mocks/decision/variants.png`. Native Android Material 3, with a literary cinema-program voice. Illustrative posters in the comp are replaced with actual source posters.

## Tokens

Light: surface #F8F4EC, onSurface #28201F, primary #742D3A, onPrimary #FFFFFF, primaryContainer #F2DDE0, outline #CFC4BF. Dark: surface #1D1918, onSurface #F3EAE2, primary #F0B8C2, onPrimary #4A1723, primaryContainer #5D2633, outline #92847F.

Display and film titles: bundled Literata (SIL OFL). Body, labels and numerals: native sans. Material roles: headlineLarge 30sp, headlineSmall 24sp, titleLarge 22sp, titleMedium 18sp, bodyLarge 16sp, bodyMedium 14sp, labelLarge 14sp. Text uses sp and wraps rather than truncating critical film names.

Spacing: 4/8/12/16/20/24/32dp. Screen gutter 24dp, compact film poster 82×123dp, content gap 16dp. Corners 12dp for images and fields; Material rounded buttons. No ornamental shadows or gradients.

## First viewport contract

Compact brand header and refresh control. A two-line maximum serif headline “Wybierz swój seans.”, factual cache status, horizontal 14-day picker, search, selected date and number of films. Each row pairs a real 2:3 poster with a complete film title, concise metadata, and directly actionable screening times. Content, rather than a promotional hero, owns the viewport. Thin separators group rows without nested cards.

## Interaction

Date selection updates the film list in place. Search matches Polish titles with accent-insensitive matching. A film opens a full detail view; Back restores list position. Save is a local bookmark. Each time leads to a focused screening sheet explaining the room/date and online purchase, then opens the exact source checkout. Bottom destinations retain selected day. At ≥600dp, use a navigation rail and a two-column film list. Long labels wrap, 48dp minimum targets.

## States

Show cached content immediately, keep refresh progress quiet, show age and connection status truthfully. Empty unpublished day is distinct from search with no results, initial fetch, or failed first fetch. Offline checkout is explained with a retry path. Honor dark theme, font scaling, system Back, display cutouts and system insets. Only Material motion, no decorative entrance animation.

## Alternatives considered

Poster gallery offered visual browsing but lower schedule density. Departure table offered chronological scanning but repeated films. The chosen program preserves clear film grouping and multiple times. Other experimental forms (dance score, coupon wallet, media feed, reference rail, marketing grid) did not improve both audience fit and clarity; their useful disciplines are aligned times, precise status, and content-first composition.
