package pl.charlie.repertuar.data

import java.io.IOException
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
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
