package pl.charlie.repertuar.data

import java.io.File
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.nio.file.Files
import java.nio.file.StandardCopyOption

data class RefreshResult(val updatedDays: Int, val failedDays: Int)

class ScheduleRepository(private val directory: File, private val source: CinemaSource) {
    private val mutableLibrary = MutableStateFlow(Library())
    val library = mutableLibrary.asStateFlow()
    private val stateLock = Mutex()
    private val refreshLock = Mutex()
    private var loaded = false
    private val cache get() = File(directory, "library.json")

    suspend fun load() = withContext(Dispatchers.IO) {
        stateLock.withLock {
            if (!loaded) {
                mutableLibrary.value = runCatching { LibraryJson.decode(cache.readText()) }.getOrDefault(Library())
                loaded = true
            }
        }
    }

    suspend fun refresh(start: LocalDate, count: Int): RefreshResult = withContext(Dispatchers.IO) {
        require(count in 1..14)
        load()
        refreshLock.withLock {
            var updated = 0
            var failed = 0
            for (offset in 0 until count) {
                val date = start.plusDays(offset.toLong())
                try {
                    val day = source.day(date)
                    require(day.date == date)
                    update { current ->
                        val films = current.films.toMutableMap()
                        day.films.forEach { incoming ->
                            val old = films[incoming.id]
                            films[incoming.id] = if (old?.detailsFetched == true) incoming.copy(
                                description = old.description, director = old.director.ifBlank { incoming.director },
                                durationMinutes = old.durationMinutes.takeIf { it > 0 } ?: incoming.durationMinutes,
                                detailsFetched = true,
                            ) else incoming
                        }
                        val days = (current.days + (date to day)).filterKeys { !it.isBefore(start.minusDays(1)) && it.isBefore(start.plusDays(14)) }
                        val retained = days.values.flatMap { it.films.map(Film::id) }.toSet() + current.savedIds
                        current.copy(days = days, films = films.filterKeys { it in retained })
                    }
                    updated++
                } catch (cancelled: CancellationException) { throw cancelled }
                catch (_: Exception) {
                    failed++
                    // Avoid 14 timeout waits on a disconnected phone. Already downloaded days stay usable.
                    if (updated == 0) return@withLock RefreshResult(0, count)
                }
            }
            RefreshResult(updated, failed)
        }
    }

    suspend fun enrichFilms() = withContext(Dispatchers.IO) {
        for (film in library.value.films.values.filter { !it.detailsFetched }) {
            try {
                val enriched = source.details(film)
                update { current ->
                    if (film.id !in current.films) current else current.copy(films = current.films + (film.id to enriched))
                }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { /* Metadata failure never invalidates a usable schedule. */ }
        }
    }

    suspend fun toggleSaved(id: String) = withContext(Dispatchers.IO) {
        load()
        update { current ->
            require(id in current.films)
            current.copy(savedIds = if (id in current.savedIds) current.savedIds - id else current.savedIds + id)
        }
    }

    private suspend fun update(transform: (Library) -> Library) = stateLock.withLock {
        val next = transform(mutableLibrary.value)
        directory.mkdirs()
        val temporary = File(directory, "library.tmp")
        temporary.outputStream().use { stream ->
            stream.write(LibraryJson.encode(next).toByteArray(Charsets.UTF_8))
            stream.fd.sync()
        }
        Files.move(temporary.toPath(), cache.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        mutableLibrary.value = next
    }
}
