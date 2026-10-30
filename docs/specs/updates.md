# Sprawdzanie wydań aplikacji

- Automatycznie sprawdzaj najnowsze zwykłe opublikowane wydanie projektu na GitHubie, najwyżej raz na 24 godziny. Odrzuć draft, prerelease i brak właściwego APK. Dostępne ręczne sprawdzenie.
- Porównuj numery major.minor.patch liczbowo. Nie proponuj ponownego instalowania tej samej ani starszej wersji.
- Zachowaj ostatnią poprawną informację lokalnie i nie blokuj repertuaru ani jego synchronizacji przy błędzie lub braku sieci.
- Zakładka Kino pokazuje zainstalowaną wersję, stan sprawdzenia i dostępną aktualizację. Przycisk pobierania działa online; stan offline wyjaśnia wymaganą łączność. Dyskretna kropka na zakładce sygnalizuje nowe wydanie.
- Od 0.1.6 pobieranie dokładnego APK własnego repozytorium odbywa się wewnątrz aplikacji, z prawdziwym postępem, anulowaniem i ponowieniem po błędzie. Gotowy plik pozostaje dostępny po restarcie i offline. Pobieranie rozpoczęte przez użytkownika nie blokuje repertuaru ani nawigacji. Przerwanie procesu wymaga ponowienia częściowego transferu; nie obiecuj wznawiania od bajtu.
- Przed udostępnieniem instalatorowi sprawdź rozmiar i SHA-256 GitHuba, nazwę pakietu, docelową wersję, wyższy versionCode i ten sam podpis. Pobieraj tylko przez HTTPS ze stałego adresu wydania, dopuszczając ograniczone przekierowanie do CDN GitHuba.
- „Zainstaluj aktualizację” otwiera instalator Androida z prywatnego pliku, bez przeglądarki i menedżera plików. Pierwsza taka instalacja wymaga systemowej zgody dla Charlie; powrót bez zgody zachowuje gotowy plik i pozwala spróbować ponownie. Android nadal wymaga potwierdzenia instalacji. Anulowanie instalatora nie uruchamia go ponownie automatycznie.
- Automatyczne i ręczne sprawdzenie korzystają z tego samego repozytorium. Testy na uzgodnionych granicach publicznego repozytorium i interakcji Compose obejmują dostępność, brak aktualizacji, cache offline, błędy, porównanie wersji i akcję pobierania.
- Każde kolejne wydanie musi podnieść versionCode, zachować klucz podpisu i używać pliku Charlie-MVP-<major.minor.patch>.apk oraz tagu v<major.minor.patch>.
