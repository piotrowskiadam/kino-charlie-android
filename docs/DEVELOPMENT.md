# Dalszy development

## Przygotowanie środowiska

Potrzebne są JDK 21, Android SDK z platformą wymaganą przez `compileSdk`, zaakceptowane licencje oraz emulator lub telefon do testów interakcji. Wrapper dostarcza właściwy Gradle. Wersje zależności i SDK sprawdzaj w [konfiguracji aplikacji](../app/build.gradle.kts), [konfiguracji głównej](../build.gradle.kts) i [wrapperze](../gradle/wrapper/gradle-wrapper.properties).

Ustaw `JAVA_HOME` na pełne JDK z kompilatorem, a lokalizację Android SDK przez `ANDROID_HOME` lub nieśledzony `local.properties` z `sdk.dir`. Systemowe JRE bez `javac` nie wystarcza. Ścieżki z poprzedniej sesji pod `/tmp` i cudzy katalog domowy nie są przenośną konfiguracją.

## Budowanie i testy

Z katalogu głównego:

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest :app:lintDebug
./gradlew :app:connectedDebugAndroidTest
./gradlew :app:assembleRelease
```

Przed testami urządzeniowymi wybierz właściwy emulator/telefon; przy kilku urządzeniach ustaw `ANDROID_SERIAL`. Raporty są w `app/build/reports/`. Bieżące testy znajdują się w `app/src/test/` i `app/src/androidTest/`. Fixture HTML w `app/src/test/resources/` zachowują kodowanie źródłowe.

Opcjonalna kontrola żywej strony wymaga sieci:

```bash
CHARLIE_LIVE=1 ./gradlew :app:testDebugUnitTest --tests '*LiveImportTest' --rerun-tasks
```

Eksport snapshotu można skierować zmienną `CHARLIE_EXPORT` do wybranego katalogu. Test live zależy od stanu strony; jego wynik opisujemy osobno od testów deterministycznych. Powrót z błędem importu nie powinien usuwać działającego cache.

Przy większej zmianie uruchom odpowiedni pojedynczy test podczas implementacji, a przed wydaniem pełny zestaw jednostkowy, lint i testy interakcji. Dla zmian wyłącznie dokumentacyjnych wystarcza weryfikacja linków, ścieżek i zgodności opisu z kodem.

## Zmiany interfejsu

Najpierw przeczytaj [DESIGN](../DESIGN.md), a następnie sprawdź `CharlieTheme.kt` i zmieniany fragment `CharlieApp.kt`. Po zmianie reguł systemu wizualnego zaktualizuj też `.impeccable/design.json`.

Kontrola na urządzeniu obejmuje układ telefonu, ciemny motyw, powiększoną czcionkę oraz szeroki ekran z nawigacją boczną. Sprawdź pełne tytuły, odstępy przy zawijaniu godzin, systemowe obszary bezpieczne i Back. Zrzut wykonuj po zakończeniu zmiany konfiguracji i ustabilizowaniu UI; obraz ze starego rozmiaru lub motywu nie jest dowodem dla nowego stanu.

## Zmiany importu i cache

Publiczne granice testów to import HTML, repozytorium i interakcje Compose, opisane w [specyfikacji](specs/mvp.md). Zachowuj fixture odtwarzający konkretny problem, zamiast opierać regresję na dostępności strony. Przy zmianie formatu zapisu pokryj migrację istniejącego katalogu i zapisanych filmów. Wspólne reguły dotyczące daty kina, atomowego zapisu i adresów źródłowych są opisane w [architekturze](ARCHITECTURE.md).

Nową integrację, np. bazę plakatów, opisujemy z jej wymaganymi kluczami, zasadami dopasowania filmu, cache i zachowaniem po błędzie. Sekrety pozostają poza repozytorium. Proponowane integracje nie są funkcjami działającego MVP.

## Przygotowanie APK do testów

Wynik buildu release: `app/build/outputs/apk/release/app-release.apk`. Obecna konfiguracja używa R8, zmniejszania zasobów i podpisu testowego `debug`. Zależność RE2J uzupełnia opcjonalny kod jsoup wymagany podczas optymalizacji. Ustawienia pamięci/równoległości Gradle ograniczają presję pamięci przy pakowaniu bibliotek Compose.

Przed przekazaniem nowej wersji:

1. Ustal `versionCode` i `versionName` w konfiguracji aplikacji; zwiększ kod przy kolejnym wydaniu.
2. Zbuduj release i zainstaluj dokładnie ten plik na urządzeniu testowym.
3. Sprawdź świeży import, zapis filmu i ponowne uruchomienie bez sieci; zakup sprawdzaj do granicy otwarcia właściwej strony, bez wykonywania płatności.
4. Zweryfikuj podpis narzędziem `apksigner` z Android Build Tools i zapisz SHA-256 dostarczanego pliku.
5. Dodaj datowany raport w `docs/qa/`, link z [indeksu](README.md), zakres urządzeń i testów oraz rzeczywiste ograniczenia. Historyczny [raport MVP](QA.md) pozostaje przypisany do APK 0.1.0.

Kopie plików do przekazania można trzymać w ignorowanym `output/`. Zapisz nazwę i sumę pliku w raporcie, ponieważ ten katalog nie jest częścią historii Git. Podpis debug służy lokalnym testom; publikacja wymaga własnego trwałego klucza i odrębnej konfiguracji podpisywania.

## Domknięcie zmiany

Sprawdź tabelę utrzymania dokumentów w [indeksie](README.md). Zapisz kod, odpowiednie dokumenty i wynik weryfikacji razem. W raporcie odróżnij test wykonany od planowanego; gdy zmiana nie wpływa na dany obszar dokumentacji, zachowaj istniejący opis zamiast dopisywać pozorne aktualizacje.
