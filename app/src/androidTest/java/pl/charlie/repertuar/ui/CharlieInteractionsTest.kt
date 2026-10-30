package pl.charlie.repertuar.ui

import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals
import pl.charlie.repertuar.StartupPhase
import pl.charlie.repertuar.data.*
import pl.charlie.repertuar.updates.*
import java.time.LocalDate

class CharlieInteractionsTest {
    @get:Rule val compose = createComposeRule()
    private val today = LocalDate.parse("2026-10-30")
    private val film = Film("one", "Lalka", "https://www.charlie.pl/", genre = "dramat", durationMinutes = 162)
    private val second = Film("two", "Pieśni lasu", "https://www.charlie.pl/", genre = "dokumentalny")

    @Test fun cinemaTabOffersTheNewVersionAndDownloadKeepsScheduleAvailable() {
        var downloaded: AppRelease? = null
        val library = Library(films = mapOf("one" to film), days = mapOf(today to ScheduleDay(
            today, listOf(film), listOf(Screening("one", today, "13:45", "Studyjna")), 1_000L)))
        compose.setContent { CharlieTheme {
            CharlieApp(library, false, null, true, {}, {}, {}, {}, today,
                updates = AppUpdateUi(UpdateState(AppRelease(AppVersion.parse("0.1.5")), 1_000L),
                    download = { downloaded = it }))
        } }
        compose.onNodeWithTag("tab-cinema").performClick()
        compose.onNodeWithText("Dostępna wersja 0.1.5").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Pobierz aktualizację").performScrollTo().performClick()
        compose.runOnIdle {
            assertEquals("https://github.com/piotrowskiadam/kino-charlie-android/releases/download/v0.1.5/Charlie-MVP-0.1.5.apk",
                downloaded?.downloadUrl)
        }
        compose.onNodeWithTag("tab-schedule").performClick()
        compose.onNodeWithTag("film-one").assertExists()
    }

    @Test fun knownUpdateRemainsVisibleOfflineWithoutAllowingDownloadOrChecking() {
        compose.setContent { CharlieTheme {
            CharlieApp(Library(), false, null, false, {}, {}, {}, {}, today,
                updates = AppUpdateUi(UpdateState(AppRelease(AppVersion.parse("0.1.5")), 1_000L)))
        } }
        compose.onNodeWithContentDescription("Dostępna aktualizacja aplikacji", useUnmergedTree = true).assertExists()
        compose.onNodeWithTag("tab-cinema").performClick()
        compose.onNodeWithText("Dostępna wersja 0.1.5").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Pobierz aktualizację").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithText("Sprawdź aktualizacje").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithText("Połącz się z internetem, aby sprawdzić lub pobrać aktualizację.")
            .performScrollTo().assertIsDisplayed()
    }

    @Test fun manualUpdateCheckShowsProgressThenReportsNoNewerVersion() {
        val state = mutableStateOf(UpdateState(error = "Nie udało się sprawdzić aktualizacji."))
        compose.setContent { CharlieTheme {
            CharlieApp(Library(), false, null, true, {}, {}, {}, {}, today,
                updates = AppUpdateUi(state.value, check = { state.value = UpdateState(checking = true) }))
        } }
        compose.onNodeWithTag("tab-cinema").performClick()
        compose.onNodeWithText("Sprawdź aktualizacje").performScrollTo().performClick()
        compose.onNodeWithText("Sprawdzamy aktualizacje…").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Sprawdź aktualizacje").assertIsNotEnabled()
        compose.runOnIdle { state.value = UpdateState(checkedAt = 1_000L) }
        compose.onNodeWithText("Masz aktualną wersję aplikacji.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Pobierz aktualizację").assertDoesNotExist()
    }

    @Test fun firstLaunchReleasesTodayWhileFutureDaysAreStillPreparing() {
        val library = mutableStateOf(Library())
        compose.setContent {
            CharlieTheme {
                CharlieApp(library.value, true, null, true, {}, {}, {}, {}, today, StartupPhase.Preparing)
            }
        }
        compose.onNodeWithTag("startup-welcome").assertIsDisplayed()
        compose.onNodeWithTag("tab-schedule").assertDoesNotExist()
        compose.runOnIdle {
            library.value = Library(films = mapOf("one" to film), days = mapOf(today to ScheduleDay(
                today, listOf(film), listOf(Screening("one", today, "13:45", "Studyjna")), 1_000L)))
        }
        compose.onNodeWithTag("startup-welcome").assertDoesNotExist()
        compose.onNodeWithTag("film-one").assertExists()
        compose.onNodeWithTag("room-one-Studyjna", useUnmergedTree = true).assertTextEquals("Sala Studyjna")
        compose.onNodeWithTag("date-$today").assertIsSelected()
        compose.onNodeWithTag("date-${today.plusDays(1)}").performClick()
        compose.onNodeWithText("Pobieramy repertuar").performScrollTo().assertIsDisplayed()
    }

    @Test fun firstUseOfflineExplainsConnectionAndRetryRestartsPreparation() {
        val phase = mutableStateOf(StartupPhase.Failed)
        val online = mutableStateOf(false)
        compose.setContent { CharlieTheme {
            CharlieApp(Library(), false, null, online.value, { phase.value = StartupPhase.Preparing }, {}, {}, {}, today, phase.value)
        } }
        compose.onNodeWithText("Na pierwszy repertuar potrzebujesz internetu.", substring = true).assertIsDisplayed()
        compose.runOnIdle { online.value = true }
        compose.onNodeWithText("Spróbuj ponownie").performClick()
        compose.onNodeWithText("Przygotowujemy Twój repertuar…").assertIsDisplayed()
        compose.onNodeWithText("Spróbuj ponownie").assertDoesNotExist()
    }

    @Test fun aValidEmptyTodayExitsWelcomeAndExplainsThatThereAreNoScreenings() {
        val library = Library(days = mapOf(today to ScheduleDay(today, emptyList(), emptyList(), 1_000L)))
        compose.setContent { CharlieTheme {
            CharlieApp(library, false, null, true, {}, {}, {}, {}, today, StartupPhase.Preparing)
        } }
        compose.onNodeWithTag("startup-welcome").assertDoesNotExist()
        compose.onNodeWithText("Jeszcze bez seansów").performScrollTo().assertIsDisplayed()
    }

    @Test fun localCacheIsCheckedBeforeWelcomeAndCachedOfflineLaunchGoesStraightToProgram() {
        val phase = mutableStateOf(StartupPhase.CheckingCache)
        val library = mutableStateOf(Library())
        compose.setContent { CharlieTheme {
            CharlieApp(library.value, false, null, false, {}, {}, {}, {}, today, phase.value)
        } }
        compose.onNodeWithTag("startup-cache").assertIsDisplayed()
        compose.onNodeWithTag("startup-welcome").assertDoesNotExist()
        compose.runOnIdle {
            library.value = Library(films = mapOf("one" to film), days = mapOf(today to ScheduleDay(
                today, listOf(film), listOf(Screening("one", today, "13:45", "Studyjna")), 1_000L)))
            phase.value = StartupPhase.Ready
        }
        compose.onNodeWithTag("startup-welcome").assertDoesNotExist()
        compose.onNodeWithTag("film-one").assertExists()
        compose.onNodeWithContentDescription("Kino-Galeria Charlie").assertIsDisplayed()
    }

    private fun launch(online: Boolean = false, onBook: (Screening) -> Unit = {}, todayState: State<LocalDate>? = null,
        multipleRooms: Boolean = false) {
        compose.setContent {
            var library by remember { mutableStateOf(Library(
                days = mapOf(today to ScheduleDay(today, listOf(film, second), listOf(
                    Screening("one", today, "13:45", "Studyjna", "https://bilety.charlie.pl/MSI/?event_id=1"),
                    Screening("two", today, "16:20", "Kameralna")) + if (multipleRooms) listOf(
                    Screening("one", today, "14:45", "Studyjna"),
                    Screening("one", today, "15:45", "Studyjna"),
                    Screening("one", today, "16:45", "Studyjna"),
                    Screening("one", today, "13:45", "Kameralna", "https://bilety.charlie.pl/MSI/?event_id=3")) else emptyList(), 1_000L),
                    today.plusDays(1) to ScheduleDay(today.plusDays(1), listOf(second), listOf(
                        Screening("two", today.plusDays(1), "17:00", "Kameralna")), 1_000L)),
                films = mapOf("one" to film, "two" to second))) }
            CharlieTheme {
                CharlieApp(library, false, null, online, {}, { id -> library = library.copy(
                    savedIds = if (id in library.savedIds) library.savedIds - id else library.savedIds + id)
                }, onBook, {}, todayState?.value ?: today)
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
        compose.onNodeWithTag("selected-room").assertTextEquals("Sala Studyjna").assertIsDisplayed()
        compose.onNodeWithText("Do zakupu biletu potrzebujesz internetu.").assertIsDisplayed()
        compose.onNodeWithText("Przejdź do kasy kina").assertIsNotEnabled()
    }

    @Test fun onlineCheckoutReceivesTheSelectedScreeningsExactSourceUrl() {
        var selected: Screening? = null
        launch(online = true, onBook = { selected = it })
        compose.onNodeWithTag("screening-${today}|13:45|one|Studyjna").performClick()
        compose.onNodeWithText("Przejdź do kasy kina").performClick()
        compose.runOnIdle { assertEquals("https://bilety.charlie.pl/MSI/?event_id=1", selected?.bookingUrl) }
    }

    @Test fun changingCinemaDateAfterMidnightUpdatesTheSelectedDay() {
        val currentDay = mutableStateOf(today)
        launch(todayState = currentDay)
        compose.onNodeWithTag("film-one").assertExists()
        compose.runOnIdle { currentDay.value = today.plusDays(1) }
        compose.onNodeWithTag("date-${today.plusDays(1)}").assertIsSelected()
        compose.onNodeWithTag("film-one").assertDoesNotExist()
        compose.onNodeWithTag("film-two").assertExists()
    }

    @Test fun unfetchedDateDoesNotClaimAnotherDaysSavedTimestamp() {
        launch(online = true)
        compose.onNodeWithTag("date-${today.plusDays(2)}").performClick()
        compose.onAllNodesWithText("Ten dzień czeka na pobranie").onFirst().assertIsDisplayed()
        compose.onNodeWithText("Zapisano", substring = true).assertDoesNotExist()
    }

    @Test fun roomNamesAreVisibleBeforeSelectingTimeOnScheduleAndSavedFilms() {
        launch(multipleRooms = true)
        compose.onNodeWithTag("room-one-Studyjna", useUnmergedTree = true).performScrollTo().assertTextEquals("Sala Studyjna")
        compose.onNodeWithTag("room-one-Kameralna", useUnmergedTree = true).performScrollTo().assertTextEquals("Sala Kameralna")
        compose.onNodeWithText("Lalka", useUnmergedTree = true).performScrollTo().performClick()
        compose.onNodeWithTag("film-details").performScrollToNode(hasText("Sala Kameralna"))
        compose.onNodeWithText("Sala Kameralna").assertIsDisplayed()
        compose.onNodeWithContentDescription("Zapisz film").performClick()
        compose.onNodeWithContentDescription("Wróć do repertuaru").performClick()
        compose.onNodeWithTag("tab-saved").performClick()
        compose.onNodeWithTag("room-one-Studyjna", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
        compose.onNodeWithTag("room-one-Kameralna", useUnmergedTree = true).performScrollTo().assertIsDisplayed()
    }

    @Test fun identicalTimesInDifferentRoomsKeepTheSelectedRoomAndCheckoutUrl() {
        var selected: Screening? = null
        launch(online = true, onBook = { selected = it }, multipleRooms = true)
        compose.onNodeWithTag("screening-${today}|13:45|one|Kameralna").performScrollTo().performClick()
        compose.onNodeWithTag("selected-room").assertTextEquals("Sala Kameralna").assertIsDisplayed()
        compose.onNodeWithText("Przejdź do kasy kina").performClick()
        compose.runOnIdle {
            assertEquals("Kameralna", selected?.room)
            assertEquals("https://bilety.charlie.pl/MSI/?event_id=3", selected?.bookingUrl)
        }
    }
}
