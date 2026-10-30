# Dokumentacja projektu

Punkt wejścia dla kolejnych zmian w aplikacji. Opis aktualnej implementacji jest oddzielony od wymagań i od historycznych wyników testów.

| Dokument | Odpowiedzialność |
| --- | --- |
| [README](../README.md) | Szybki opis aplikacji i uruchomienia projektu |
| [PRODUCT](../PRODUCT.md) | Cel, użytkownicy, ustalone możliwości i ograniczenia |
| [Specyfikacja MVP](specs/mvp.md) | Zakres i kryteria akceptacji pierwszej wersji |
| [Powitanie, plakaty i branding](specs/first-run-and-branding.md) | Pierwsze przygotowanie, poprawa obrazów i identyfikacja kina |
| [Aktualizacje aplikacji](specs/updates.md) | Sprawdzanie wydań, cache informacji i pobieranie APK |
| [Wyszukiwanie Filmweb](specs/filmweb.md) | Poprawny parametr i tytuły ze znakami specjalnymi |
| [Przyciski odświeżania](specs/refresh-buttons.md) | Widoczny obrys, ikonki i stany akcji w Kino |
| [Monochromatyczny tekst i OLED](specs/oled.md) | Czysta czerń/biel, neutralne powierzchnie i paski systemowe |
| [Widoczność sal](specs/rooms.md) | Nazwa sali przy godzinach i jednoznaczne rozróżnianie seansów |
| [Źródła i prawa do zasobów](../NOTICE.md) | Pochodzenie materiałów i odrębne warunki podmiotów trzecich |
| [Licencja MIT](../LICENSE) | Warunki użycia własnego kodu i dokumentacji, także komercyjnego |
| [GLOSSARY](../GLOSSARY.md) | Znaczenie pojęć domenowych |
| [Architektura](ARCHITECTURE.md) | Moduły, przepływ danych, cache, synchronizacja i granice integracji |
| [Dodatkowe źródło MSI](sources/bilety-charlie.md) | Zgoda użytkownika, sprawdzone dane listy biletowej i warunki przyszłej integracji |
| [DESIGN](../DESIGN.md) | Zatwierdzony kierunek UI, tokeny, komponenty i stany |
| [Dane designu](../.impeccable/design.json) | Strukturalny zapis systemu wizualnego; aktualizowany razem z DESIGN |
| [Makieta MVP](design/approved-mvp-variants.png) | Zachowana plansza koncepcji; zatwierdzony wariant A, z ilustracyjnymi obrazami filmów |
| [Development](DEVELOPMENT.md) | Środowisko, budowanie, testowanie i przygotowanie APK |
| [Decyzje architektoniczne](adr/001-offline-schedule.md) | Uzasadnienie przyjętych kompromisów; kolejne decyzje w tym samym katalogu |
| [Raport MVP](QA.md) | Historyczne wyniki weryfikacji APK 0.1.0 z 30.10.2026 |
| [Raport 0.1.1 — sale](qa/2026-10-30-0.1.1-rooms.md) | Nazwy sal, regresje wyboru seansu i dowody wizualne |
| [Raport 0.1.2](qa/2026-10-30-0.1.2.md) | Powitanie, plakaty, branding i publikacja GitHub |
| [Raport 0.1.3](qa/2026-10-30-0.1.3.md) | Czarna ikona z węższym wąsikiem i poprawne logo na białym tle w ciemnym motywie |
| [Raport 0.1.4](qa/2026-10-30-0.1.4.md) | Sprawdzanie wydań, limit dobowy, cache offline i przycisk pobrania APK |
| [Raport 0.1.5](qa/2026-10-30-0.1.5.md) | Monochromatyczny tekst, tła OLED i pomiary pikseli |
| [Raport 0.1.6](qa/2026-10-30-0.1.6.md) | Filmweb i natywne pobieranie oraz instalacja aktualizacji |
| [Raport 0.1.7](qa/2026-10-30-0.1.7.md) | Rozpoznawalne przyciski sprawdzania i odświeżania |
| [Weryfikacja licencji](qa/2026-10-31-license.md) | Zmiana dokumentacyjna: MIT, zakres praw i odnośniki |
| [Śledzenie prac](agents/issue-tracker.md) | Miejsce zapisu zakresu zmian i ograniczeń |
| [Instrukcje dla agentów](../AGENTS.md) | Warunki sięgania do dokumentów i ich aktualizowania |

## Utrzymywanie dokumentacji

Dokumentację aktualizujemy w tej samej zmianie co odpowiadający jej kod. Opisujemy zachowanie istniejące; planowane rozszerzenia oznaczamy jako propozycje. Parametry wykonania i wersje zależności pozostają w plikach Gradle, a tokeny UI w `CharlieTheme.kt` — dokumenty wyjaśniają ich znaczenie i powody wyboru.

| Zmiana | Dokumenty do sprawdzenia i aktualizacji |
| --- | --- |
| Nowa funkcja lub zmiana zakresu | PRODUCT, odpowiednia specyfikacja; README, jeśli zmienia opis aplikacji |
| Ekrany, interakcje, dostępność, tokeny UI | DESIGN i dane designu; dowody wizualnej weryfikacji |
| Import HTML, model danych, format cache, synchronizacja, integracje | ARCHITECTURE; ADR przy zmianie istotnego kompromisu |
| Zmiana znaczenia pojęcia | GLOSSARY; odwołania używające tego pojęcia |
| Toolchain, zależności wpływające na budowanie, podpisywanie, testy | DEVELOPMENT; odpowiednie pliki konfiguracyjne |
| Licencja i charakter projektu | LICENSE, README, NOTICE, PRODUCT oraz opis i wykrywana licencja GitHuba |
| Wydanie lub ponowna weryfikacja APK | Nowy datowany raport QA i jego link w tym indeksie |

Przy istotnej decyzji architektonicznej zachowujemy poprzedni ADR i dodajemy kolejny z kontekstem, wyborem, alternatywami i skutkami. Zmianę zastępującą wcześniejszą decyzję łączymy linkami w obu dokumentach. Drobna zmiana implementacyjna wystarcza w opisie bieżącej architektury.

Specyfikacja MVP zachowuje zaakceptowany zakres pierwszego wydania. Większe kolejne funkcje otrzymują własne specyfikacje w `docs/specs/`, połączone linkami z tym indeksem.

Raport `QA.md` jest dowodem dla konkretnego pliku APK, nie obietnicą, że późniejszy kod przeszedł te same testy. Kolejne raporty umieszczamy pod `docs/qa/` z wersją i datą w nazwie; nie nadpisujemy historii wyników.

Zmiana dokumentacji jest gotowa, gdy jej ścieżki działają w repozytorium, opis zgadza się z kodem, a wyniki testów mają wskazany rzeczywisty zakres. Dla zmian wyłącznie opisowych sprawdzamy linki i diff; przebudowa APK jest potrzebna przy zmianach aplikacji lub jej konfiguracji.
