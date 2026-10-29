package pl.charlie.repertuar.ui

import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import pl.charlie.repertuar.data.*
import java.time.LocalDate

class CharlieInteractionsTest {
    @get:Rule val compose = createComposeRule()
    private val today = LocalDate.parse("2026-10-30")
    private val film = Film("one", "Lalka", "https://www.charlie.pl/", genre = "dramat", durationMinutes = 162)
    private val second = Film("two", "Pieśni lasu", "https://www.charlie.pl/", genre = "dokumentalny")

    private fun launch() {
        compose.setContent {
            var library by remember { mutableStateOf(Library(
                days = mapOf(today to ScheduleDay(today, listOf(film, second), listOf(
                    Screening("one", today, "13:45", "Studyjna", "https://bilety.charlie.pl/MSI/?event_id=1"),
                    Screening("two", today, "16:20", "Kameralna")), 1_000L),
                    today.plusDays(1) to ScheduleDay(today.plusDays(1), listOf(second), listOf(
                        Screening("two", today.plusDays(1), "17:00", "Kameralna")), 1_000L)),
                films = mapOf("one" to film, "two" to second))) }
            CharlieTheme {
                CharlieApp(library, false, null, false, {}, { id -> library = library.copy(
                    savedIds = if (id in library.savedIds) library.savedIds - id else library.savedIds + id)
                }, {}, {}, today)
            }
        }
    }

    @Test fun dateAndSearchFilterTheCachedSchedule() {
        launch()
        compose.onNodeWithTag("film-one").assertExists()
        compose.onNodeWithTag("date-${today.plusDays(1)}").performClick()
        compose.onNodeWithTag("film-one").assertDoesNotExist()
        compose.onNodeWithTag("film-two").assertExists()
        compose.onNodeWithTag("search").performTextInput("PIESNI")
        compose.onNodeWithTag("film-two").assertExists()
        compose.onNodeWithTag("search").performTextReplacement("nieistniejący")
        compose.onNodeWithText("Nie znaleziono filmu").assertIsDisplayed()
    }

    @Test fun filmCanBeSavedFromDetailsAndFoundInSavedTabOffline() {
        launch()
        compose.onNodeWithTag("film-one").performClick()
        compose.onNodeWithContentDescription("Zapisz film").performClick()
        compose.onNodeWithContentDescription("Wróć do repertuaru").performClick()
        compose.onNodeWithTag("tab-saved").performClick()
        compose.onNodeWithTag("film-one").assertExists()
        compose.onNodeWithTag("film-two").assertDoesNotExist()
    }

    @Test fun screeningSelectionShowsExactRoomAndExplainsOnlineCheckout() {
        launch()
        compose.onNodeWithTag("screening-${today}|13:45|one|Studyjna").performClick()
        compose.onNodeWithText("Sala Studyjna").assertIsDisplayed()
        compose.onNodeWithText("Do zakupu biletu potrzebujesz internetu.").assertIsDisplayed()
        compose.onNodeWithText("Przejdź do kasy kina").assertIsNotEnabled()
    }
}
