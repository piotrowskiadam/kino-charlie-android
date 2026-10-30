# Filmweb: wyszukiwanie tytułu

Przycisk „Szukaj w Filmweb” przekazuje pełny tytuł filmu jako prawidłowo zakodowany parametr `query` w `https://www.filmweb.pl/search?query=...`. Parametr `q` jest ignorowany przez aktualną aplikację Filmweb i prowadzi do pustej bazy. Nie dobieramy samodzielnie identyfikatorów filmów ani nie udajemy potwierdzonej karty konkretnej adaptacji.

Rozpoznanie 30.10.2026: publiczny skrypt BpANE4HB_005.js pobiera URLSearchParams.get("query") i migruje go do `#/all?query=...`. Przeglądarka po przejściu z `query=Lalka` pokazała „Wynik wyszukiwania: Lalka” i 148 znalezionych pozycji, w tym różne adaptacje. Regresja interakcji ma sprawdzać cały tytuł z polskimi znakami oraz znakami `&` i `?`, nie tylko otwarcie dowolnego adresu Filmweb.
