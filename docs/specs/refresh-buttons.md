# Rozpoznawalne akcje odświeżania

„Odśwież teraz” i „Sprawdź aktualizacje” w Kino mają obrys pigułki 1dp, ikonę oraz minimum 48dp wysokości. Obrys jest czarny w jasnym motywie i biały w OLED, z neutralnym tłem. Nie zmieniaj pełnej czerni/bieli podpisów. Wyłączenie zachowuje semantykę i zmienia obrys na outline-variant. Pozostają dotychczasowe warunki online, checking, transfer.busy i refreshing oraz te same callbacki.

Zweryfikuj oba motywy, większą czcionkę i szeroki ekran. Użyj istniejących testów kliknięcia sprawdzenia, stanu pracy i offline; nie dodawaj testów powtarzających stałe stylu.
