package pl.charlie.repertuar.data

import java.time.LocalDate

data class Film(
    val id: String,
    val title: String,
    val sourceUrl: String,
    val posterUrl: String = "",
    val genre: String = "",
    val production: String = "",
    val durationMinutes: Int = 0,
    val director: String = "",
    val description: String = "",
    val detailsFetched: Boolean = false,
    val thumbnailUrl: String = "",
    val ticketPosterUrl: String = "",
) {
    val posterCandidates: List<String> get() = listOf(posterUrl, ticketPosterUrl).filter { it.isNotBlank() }.distinct()
}

data class Screening(
    val filmId: String,
    val date: LocalDate,
    val time: String,
    val room: String,
    val bookingUrl: String = "",
    val version: String = "",
) {
    val key: String get() = "$date|$time|$filmId|$room"
}

data class ScheduleDay(val date: LocalDate, val films: List<Film>, val screenings: List<Screening>, val fetchedAt: Long)

data class Library(
    val days: Map<LocalDate, ScheduleDay> = emptyMap(),
    val films: Map<String, Film> = emptyMap(),
    val savedIds: Set<String> = emptySet(),
) {
    val lastUpdated: Long get() = days.values.maxOfOrNull { it.fetchedAt } ?: 0L
    fun needsPosters(day: ScheduleDay): Boolean = day.screenings.any {
        it.bookingUrl.isNotBlank() && films[it.filmId]?.ticketPosterUrl.isNullOrBlank()
    }
    fun screenings(filmId: String): List<Screening> = days.values.flatMap { it.screenings }
        .filter { it.filmId == filmId }.sortedWith(compareBy({ it.date }, { it.time }))
}
