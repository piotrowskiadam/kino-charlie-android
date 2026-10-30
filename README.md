# Charlie · repertuar

Nieoficjalna aplikacja Android do repertuaru Kina Charlie w Łodzi. Kotlin, Jetpack Compose, Material 3. Android 8.0+ (API 26). Bez konta i serwera pośredniczącego.

## Funkcje

- wybór dnia z 14 kolejnych terminów, wyszukiwanie polskich tytułów bez konieczności wpisywania znaków diakrytycznych;
- prawdziwe plakaty, metadane, opis filmu, godziny i sale;
- zapisane filmy i lokalny repertuar dostępne po ponownym uruchomieniu bez sieci;
- atomowy zapis cache, zachowanie danych po błędzie importu;
- odświeżanie w tle przez WorkManager co 6 godzin, gdy system pozwoli; ręczne odświeżanie;
- źródłowy zakup biletu w karcie przeglądarki, wyszukiwanie w Filmweb;
- tryb jasny/ciemny i adaptacyjna nawigacja na większych ekranach.

Pierwsze uruchomienie wymaga internetu. Cache obejmuje faktycznie opublikowane seanse, nie gwarantowane pełne 14 dni. Dostępność miejsc i ceny potwierdza kasa kina. Po wymuszeniu zatrzymania aplikacji Android wstrzymuje zadania do ponownego otwarcia aplikacji.

## Budowanie

Przygotowanie JDK/SDK, polecenia budowania i testów oraz przekazywanie APK opisuje [przewodnik developmentu](docs/DEVELOPMENT.md).

## Dokumentacja

[Indeks dokumentacji i zasady jej utrzymywania](docs/README.md) prowadzi do [architektury](docs/ARCHITECTURE.md), [systemu designu](DESIGN.md), [produktu](PRODUCT.md), decyzji architektonicznych i raportów QA. Dokumenty aktualizujemy razem ze zmianami kodu. [Raport MVP](docs/QA.md) zachowuje wyniki testów konkretnego APK 0.1.0.

## Źródła i zależności

Repertuar, opisy i plakaty: [Kino Charlie](https://www.charlie.pl/). [Filmweb](https://www.filmweb.pl/) jest otwierany jako wyszukiwarka; aplikacja nie pobiera ocen i nie deklaruje zweryfikowanych dopasowań filmów. Font [Literata](https://github.com/google/fonts/tree/main/ofl/literata), SIL OFL — licencja w `docs/Literata-OFL.txt`.

Dokumentacja: [Android Gradle Plugin 9.2](https://developer.android.com/build/releases/agp-9-2-0-release-notes), [WorkManager](https://developer.android.com/reference/kotlin/androidx/work/CoroutineWorker), [Coil Compose](https://coil-kt.github.io/coil/compose/). Odtwarzalna konfiguracja w plikach Gradle, decyzje w `docs/adr/`.
