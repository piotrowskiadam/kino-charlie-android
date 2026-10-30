# Sprawdzanie wydań aplikacji

- Automatycznie sprawdzaj najnowsze zwykłe opublikowane wydanie projektu na GitHubie, najwyżej raz na 24 godziny. Odrzuć draft, prerelease i brak właściwego APK. Dostępne ręczne sprawdzenie.
- Porównuj numery major.minor.patch liczbowo. Nie proponuj ponownego instalowania tej samej ani starszej wersji.
- Zachowaj ostatnią poprawną informację lokalnie i nie blokuj repertuaru ani jego synchronizacji przy błędzie lub braku sieci.
- Zakładka Kino pokazuje zainstalowaną wersję, stan sprawdzenia i dostępną aktualizację. Przycisk pobierania działa online; stan offline wyjaśnia wymaganą łączność. Dyskretna kropka na zakładce sygnalizuje nowe wydanie.
- Pobieranie otwiera dokładny APK własnego repozytorium w przeglądarce. Użytkownik otwiera pobrany plik i zatwierdza instalację w Androidzie. Nie dodawaj uprawnienia do instalowania pakietów ani nie obiecuj instalacji bez potwierdzenia.
- Automatyczne i ręczne sprawdzenie korzystają z tego samego repozytorium. Testy na uzgodnionych granicach publicznego repozytorium i interakcji Compose obejmują dostępność, brak aktualizacji, cache offline, błędy, porównanie wersji i akcję pobierania.
- Każde kolejne wydanie musi podnieść versionCode, zachować klucz podpisu i używać pliku Charlie-MVP-<major.minor.patch>.apk oraz tagu v<major.minor.patch>.
