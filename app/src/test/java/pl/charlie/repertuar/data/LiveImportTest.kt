package pl.charlie.repertuar.data

import java.io.File
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.runBlocking
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.Assert.*

/** Opt-in integration check; deterministic tests do not depend on cinema uptime. */
class LiveImportTest {
    @Test fun importPublishedFortnightFromRealCinema() = runBlocking {
        assumeTrue(System.getenv("CHARLIE_LIVE") == "1")
        val directory = File(System.getenv("CHARLIE_EXPORT") ?: "build/live-snapshot")
        val repository = ScheduleRepository(directory, CharlieSource())
        val today = LocalDate.now(ZoneId.of("Europe/Warsaw"))
        repository.load()
        val result = repository.refresh(today, 14)
        assertTrue("No live schedule days imported", result.updatedDays > 0)
        assertTrue("No published screenings found", repository.library.value.days.values.any { it.screenings.isNotEmpty() })
        repository.enrichFilms()
        println("LIVE IMPORT: ${result.updatedDays} days; ${repository.library.value.films.size} films; " +
            "${repository.library.value.days.values.sumOf { it.screenings.size }} screenings")
    }
}
