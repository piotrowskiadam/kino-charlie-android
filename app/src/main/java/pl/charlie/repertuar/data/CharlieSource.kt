package pl.charlie.repertuar.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter

interface CinemaSource {
    suspend fun day(date: LocalDate): ScheduleDay
    suspend fun details(film: Film): Film = film
    suspend fun posters(day: ScheduleDay): Map<String, String> = emptyMap()
}

/** Only verified source hosts may supply checkout and poster URLs. */
object SourceLinks {
    fun booking(value: String): String = allowed(value, setOf("bilety.charlie.pl"))
    fun cinema(value: String): String = allowed(value, setOf("www.charlie.pl", "charlie.pl"))
    fun ticketListing(value: String): String = ticketResource(value, "/MSI/mvc/pl", Regex("date=\\d{4}-\\d{2}-\\d{2}"))
    fun poster(value: String): String = cinema(value).ifBlank {
        ticketResource(value, "/MSI/ImageData.ashx", Regex("id=[1-9]\\d*(?:&mode=thumb)?"))
    }
    private fun ticketResource(value: String, path: String, query: Regex): String =
        allowed(value, setOf("bilety.charlie.pl")).takeIf { it.isNotBlank() && URI(it).let { uri ->
            uri.path == path && query.matches(uri.rawQuery.orEmpty())
        } }.orEmpty()
    private fun allowed(value: String, hosts: Set<String>): String = runCatching {
        val uri = URI(value)
        if (uri.scheme == "https" && uri.host in hosts && uri.userInfo == null && uri.port in listOf(-1, 443)) value else ""
    }.getOrDefault("")
}

class CharlieSource(private val parser: CharlieParser = CharlieParser()) : CinemaSource {
    override suspend fun day(date: LocalDate): ScheduleDay = withContext(Dispatchers.IO) {
        val url = "https://www.charlie.pl/index.php?i=r&id=${date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))}"
        parser.parseDay(fetch(url), date, System.currentTimeMillis())
    }

    override suspend fun details(film: Film): Film = withContext(Dispatchers.IO) {
        parser.parseDetails(fetch(film.sourceUrl), film)
    }

    override suspend fun posters(day: ScheduleDay): Map<String, String> = withContext(Dispatchers.IO) {
        parser.ticketPosters(fetch("https://bilety.charlie.pl/MSI/mvc/pl?date=${day.date}"), day)
    }

    private fun fetch(url: String): String {
        require(SourceLinks.cinema(url).isNotEmpty() || SourceLinks.ticketListing(url).isNotEmpty())
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 12_000
        connection.readTimeout = 15_000
        connection.instanceFollowRedirects = false
        connection.setRequestProperty("User-Agent", "CharlieRepertuar/0.1 (Android; unofficial schedule reader)")
        connection.setRequestProperty("Accept", "text/html")
        try {
            check(connection.responseCode == 200) { "Cinema HTTP ${connection.responseCode}" }
            // Jsoup detects both HTTP and legacy meta charsets; never decode Polish HTML as UTF-8 blindly.
            val charset = Regex("charset=([^;\\s]+)", RegexOption.IGNORE_CASE)
                .find(connection.contentType.orEmpty())?.groupValues?.get(1)
            return connection.inputStream.use { Jsoup.parse(it, charset, url).outerHtml() }
        } finally { connection.disconnect() }
    }
}
