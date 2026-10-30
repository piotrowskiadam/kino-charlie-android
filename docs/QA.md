# MVP verification — 30 October 2026

## Scope and environment

Native Kotlin / Jetpack Compose application. Minimum Android 8 (API 26), target/compile 36. Verification used an Android 11 / API 30 x86_64 emulator with KVM and software graphics; no physical phone was attached. Phone 1080×2340 at 440 dpi; expanded 1600×2560 at 240 dpi; light, dark and 1.3 font scale.

## Automated checks

| Check | Result |
| --- | --- |
| Parser and persistence unit tests | 7 passed; optional live test skipped in deterministic suite |
| Opt-in live integration import | Passed: 14 dates, 44 films, 242 screenings, 44 cached descriptions |
| Compose interaction tests | 6 passed on emulator |
| Android lint | No errors; warnings concern newer dependency/SDK versions, backup configuration advice and KTX style |
| Release APK | Built, signature verified, installed cleanly; fresh live import and offline restart passed |

Interaction coverage: date selection, accent-insensitive search, detail/save/back navigation, saved-film filtering, offline screening explanation/disabled checkout, exact online checkout URL callback, date change after midnight, and absent-day cache status. These are six tests, some covering several interactions.

Unit coverage: real legacy-encoded HTML to film/room/time/exact checkout; full poster plus fallback thumbnail; description and credits; wrong-date rejection; foreign/insecure checkout rejection; durable offline schedule/bookmarks; failed refresh preserving cached screenings and timestamp. Tests use captured public HTML, not generated schedule fixtures.

TDD evidence: parser, repository, description and poster-fallback tests were observed failing before implementation; an APK with the unimplemented UI produced the expected failing instrumentation test, followed by passing tests on the implemented UI.

## Device integration and background work

The actual installed app independently imported 14 dates, 44 films and242 screenings. Some days are valid empty published snapshots. After disabling Wi-Fi and mobile data and force-stopping/reopening the app, the real schedule, source images and saved Lalka bookmark remained available. Offline checkout displayed its explanation and disabled purchase. Network settings were restored.

WorkManager registered its periodic SystemJobService with internet and battery-not-low requirements. Asset-prefetch workers reported SUCCESS on the emulator. The natural six-hour periodic interval was not waited out; Android may defer this work. Foreground resume now checks cache freshness and Warsaw's current date through the same repository. Android force-stop pauses work until reopening the app.

## Standards review

Independent review reported two documented-standard findings and one baseline judgement call: missing foreground freshness check, another day's timestamp used for an absent day, and duplicated wide/compact screening preparation. All three were fixed and the same reviewer scored each resolved. The most consequential initial finding was the foreground freshness gap; none of those findings remains open.

## Spec review

Independent review reported one requirement mismatch: stale-cache checking only at ViewModel construction. The lifecycle-resume check and current-date StateFlow fixed it; the reviewer scored the fix resolved. No other definite requirement mismatch or scope creep was identified in that review.

## Visual review

Zachowany w Git [podgląd repertuaru wersji 0.1.0](qa/assets/0.1.0/repertuar.png) pochodzi z oryginalnej weryfikacji tego APK. Lokalne robocze zrzuty w `.impeccable/review/` są ponownie używane przy kolejnych wydaniach; nie stanowią trwałego archiwum wersji 0.1.0.

Approved direction A: pocket cinema program. Emulator evidence in `.impeccable/review/`: phone-light, phone-dark, phone-large-font, tablet, phone-detail, phone-saved, phone-offline and phone-checkout-offline PNGs. Initial captures with incomplete configuration redraws were rejected and recaptured after restart/UI idle. The review accepted type, warm ground, content topology and native adaptation, and requested 8 dp vertical separation between wrapped screening buttons. That correction was recaptured on phone, saved and large-font views. The reviewer scored both original fixes resolved and returned `ship` at the scope of those fixes; no claim of an automated pixel gate is made. No HTML detector or automated pixel-diff gate ran for this native adaptation of the comparison board.

## Practical limits

- First use needs an internet connection. Future coverage depends on what the cinema has published.
- Some source full-poster URLs return 404. The app tries the source thumbnail, then an honest placeholder; small thumbnails can be soft. No fabricated posters ship.
- Filmweb opens clearly labelled title search, not a verified film identity or downloaded ratings.
- Checkout opens the exact cinema URL in a browser Custom Tab or browser fallback. No real payment was made during testing.
- The optimized test APK uses the Android debug signing key for local installation; publishing requires a dedicated signing setup.
- Verification covers an emulator on API 30, not every Android version or physical hardware performance.

## Final artifact

`output/Charlie-MVP-0.1.0.apk`: 2,392,015 bytes (2.28 MiB), package `pl.charlie.repertuar`, version0.1.0/code1, minSDK26/targetSDK36. APK Signature Scheme v2 verified successfully. The exact delivered release file was installed on a cleared test-emulator app profile. It imported the actual schedule, then displayed it again after disabling all network connections and restarting. No AndroidRuntime errors were recorded in the final smoke check.

Observed clean release cold start:778ms in this emulator run; this is one emulator observation, not a hardware guarantee.

SHA-256: `8d1f656f294e67bf239d8315b8769d53a0e23d5a72a5f3f50340512d40d6c5ce`.

Final visual verdict: reviewer scored direction persistence and wrapped-button spacing resolved, with disposition `ship` limited to those fixes. No further material finding was opened in that verdict pass.
