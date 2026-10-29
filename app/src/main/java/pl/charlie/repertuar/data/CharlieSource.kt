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
}

/** Only verified source hosts may supply checkout and poster URLs. */
object SourceLinks {
    fun booking(value: String): String = allowed(value, setOf("bilety.charlie.pl"))
    fun cinema(value: String): String = allowed(value, setOf("www.charlie.pl", "charlie.pl"))
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

    private fun fetch(url: String): String {
        require(SourceLinks.cinema(url).isNotEmpty())
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 12_000
        connection.readTimeout = 15_000
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
