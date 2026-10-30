package pl.charlie.repertuar.data

import java.io.IOException
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ScheduleRepositoryTest {
    @get:Rule val temporary = TemporaryFolder()
    private val date = LocalDate.parse("2026-10-30")
    private val film = Film("123", "Przykładowy film", "https://www.charlie.pl/index.php?i=film&id=123")
    private val day = ScheduleDay(date, listOf(film), listOf(Screening("123", date, "18:00", "Studyjna",
        "https://bilety.charlie.pl/MSI/Default.aspx?event_id=123")), 1_000L)
    private val offline = object : CinemaSource {
        override suspend fun day(date: LocalDate): ScheduleDay = throw IOException("offline")
        override suspend fun posters(day: ScheduleDay): Map<String, String> = throw IOException("offline")
    }

    @Test fun `today is durable and available before tomorrow finishes downloading`() = runTest {
        val nextDayStarted = CompletableDeferred<Unit>()
        val releaseTomorrow = CompletableDeferred<Unit>()
        val tomorrow = date.plusDays(1)
        val source = object : CinemaSource {
            override suspend fun day(date: LocalDate): ScheduleDay {
                if (date == tomorrow) { nextDayStarted.complete(Unit); releaseTomorrow.await() }
                return day.copy(date = date, screenings = day.screenings.map { it.copy(date = date) })
            }
        }
        val directory = temporary.newFolder()
        val repository = ScheduleRepository(directory, source)
        val refresh = launch { repository.refresh(date, 2) }
        nextDayStarted.await()
        assertEquals("18:00", repository.library.value.days[date]?.screenings?.single()?.time)
        assertFalse(repository.library.value.days.containsKey(date.plusDays(1)))
        val reopened = ScheduleRepository(directory, offline)
        reopened.load()
        assertEquals("Studyjna", reopened.library.value.days[date]?.screenings?.single()?.room)
        releaseTomorrow.complete(Unit)
        refresh.join()
        assertTrue(repository.library.value.days.containsKey(date.plusDays(1)))
    }

    @Test fun `supplemental poster survives refresh restart and failed imagery while keeping exact screening`() = runTest {
        val image = "https://bilety.charlie.pl/MSI/ImageData.ashx?id=2980&mode=thumb"
        val directory = temporary.newFolder()
        val source = object : CinemaSource {
            override suspend fun day(date: LocalDate) = day
            override suspend fun posters(day: ScheduleDay) = mapOf("123" to image)
        }
        val repository = ScheduleRepository(directory, source)
        repository.refresh(date, 1)
        repository.enrichPosters()
        repository.refresh(date, 1)
        val reopened = ScheduleRepository(directory, offline)
        reopened.load()
        reopened.enrichPosters()
        assertEquals(image, reopened.library.value.films["123"]?.ticketPosterUrl)
        assertEquals(day.screenings, reopened.library.value.days[date]?.screenings)
    }

    @Test fun `schedule and bookmarks survive closing app and reopening without network`() = runTest {
        val directory = temporary.newFolder()
        val online = object : CinemaSource { override suspend fun day(date: LocalDate) = day }
        val first = ScheduleRepository(directory, online)
        first.load()
        first.refresh(date, 1)
        first.toggleSaved("123")
        val reopened = ScheduleRepository(directory, offline)
        reopened.load()
        assertEquals("Przykładowy film", reopened.library.value.films["123"]?.title)
        assertEquals("18:00", reopened.library.value.days[date]?.screenings?.single()?.time)
        assertEquals(setOf("123"), reopened.library.value.savedIds)
    }

    @Test fun `failed refresh preserves cached screenings bookmarks and original timestamp`() = runTest {
        val directory = temporary.newFolder()
        val online = object : CinemaSource { override suspend fun day(date: LocalDate) = day }
        val first = ScheduleRepository(directory, online)
        first.load(); first.refresh(date, 1); first.toggleSaved("123")
        val reopened = ScheduleRepository(directory, offline)
        reopened.load()
        val result = reopened.refresh(date, 14)
        assertEquals(0, result.updatedDays)
        assertEquals(14, result.failedDays)
        assertEquals(day.screenings, reopened.library.value.days[date]?.screenings)
        assertEquals(1_000L, reopened.library.value.lastUpdated)
        assertEquals(setOf("123"), reopened.library.value.savedIds)
        val again = ScheduleRepository(directory, offline)
        again.load()
        assertEquals(reopened.library.value, again.library.value)
    }
}
