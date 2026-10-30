# Źródło uzupełniające: system biletowy Charlie

Użytkownik wskazał to źródło jako dozwolone uzupełnienie, gdy dane lub struktura `charlie.pl` utrudniają implementację.

Źródło: [lista wydarzeń MSI](https://bilety.charlie.pl/MSI/mvc/pl). Sprawdzono 30.10.2026 przez odczyt publicznej strony; odpowiedź HTTP 200, HTML UTF-8. Nie wybierano miejsc ani nie rozpoczynano transakcji.

## Status w aplikacji

To zweryfikowana możliwość przyszłej integracji. Wersja 0.1.1 nadal importuje katalog przez `CharlieSource` z głównej strony kina; subdomena biletowa obsługuje przekazane przez źródło linki zakupu. Automatyczny importer zapasowy z listy MSI nie został jeszcze wdrożony.

## Zaobserwowane dane

| Informacja | Wynik odczytu |
| --- | --- |
| Dni i sortowanie | Linki z parametrami `date=YYYY-MM-DD`, `sort=Name`, `NameDesc` lub `Date`; widoczne zakładki od 30.10 do 05.11.2026 |
| Filmy | Tytuły i deklarowany czas trwania; część opisów także w danych osadzonych w stronie |
| Seanse | Godzina, data i identyfikator `event_id` / `data-event` |
| Zakup | Bezpośrednie linki do `MSI/Default.aspx?event_id=...` z dodatkowymi parametrami źródłowymi |
| Obrazy | Osobny endpoint `MSI/ImageData.ashx?id=...&mode=thumb`; rozdzielczości i dostępności samych obrazów w tym sprawdzeniu nie mierzono |
| Sale | W przejrzanym listingu nie znaleziono nazw sal; ewentualne dane w szczegółach wymagają osobnej weryfikacji |

Przykład dopasowania: Lalka, 30.10.2026 o 13:45, ma `event_id=97086` zarówno na tej liście, jak i w zachowanym fixture głównego repertuaru. Identyfikator obrazu MSI to odrębne pole; nie jest identyfikatorem filmu z `charlie.pl`.

## Warunki bezpiecznego połączenia źródeł

- Dopasowywać przede wszystkim po zweryfikowanym identyfikatorze seansu, sprawdzając datę, godzinę i film. Sam podobny tytuł nie wystarcza do podmiany sali lub linku.
- Usuwać duplikaty prezentacyjne: te same seanse i linki występują w HTML więcej niż raz. Zachować rozróżnienie seansów w różnych salach.
- Nazwę sali zachować z potwierdzonego źródła. Brak nazwy w listingu MSI nie potwierdza, że sala jest nieznana również w szczegółach; nie daje też podstawy do zgadywania sali po godzinie.
- Jawnie rozstrzygnąć konflikty pól przed wdrożeniem. W odczycie Lalka ma 166 min na MSI, a zachowany fixture głównej strony daje 162 min; Mistyczka odpowiednio 89 i 80 min. Nie nadpisywać automatycznie jednej wartości drugą bez ustalenia pochodzenia i reguły pierwszeństwa.
- Zachować pełne źródłowe adresy zakupu. Przykładowe linki MSI i głównej strony różnią się wartością `typetran`; jego znaczenia nie potwierdzono w tym sprawdzeniu.
- Ewentualne dołączenie obrazów z subdomeny wymaga jawnej obsługi tego hosta w regułach obrazów. Obecna walidacja adresów katalogu/obrazów dopuszcza `charlie.pl` i `www.charlie.pl`, a `bilety.charlie.pl` jest dopuszczone dla zakupu.
- Import dodatkowego źródła powinien przejść regresje poprawnego dopasowania seansu/sali/linku, deduplikacji, konfliktów metadanych i zachowania cache przy błędzie.

To jednorazowe rozpoznanie publicznej listy, nie potwierdzenie stabilnego API, pełnego horyzontu 14 dni ani kompletności danych o salach.
