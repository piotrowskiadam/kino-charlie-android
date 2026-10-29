package pl.charlie.repertuar.data

import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.Duration
import java.time.format.DateTimeFormatter
import java.util.Locale
import org.jsoup.Jsoup
import org.json.JSONObject

class CharlieParser {
    fun parseDetails(html: String, film: Film): Film {
        val document = Jsoup.parse(html, film.sourceUrl)
        require(document.select("h1.duzytytul").any { it.text() == film.title }) { "Unexpected film page" }
        fun credit(label: String): String = document.select("tr").firstOrNull { row ->
            row.children().firstOrNull()?.text() == label
        }?.children()?.lastOrNull()?.text().orEmpty()
        val description = document.select("div[style]").firstOrNull { element ->
            element.attr("style").replace(" ", "").contains("font-size:15px") &&
                element.attr("style").contains("padding") && element.text().length > 100
        }?.text()?.replace('\u00A0', ' ').orEmpty()
        return film.copy(
            director = credit("Reżyseria").ifBlank { film.director },
            durationMinutes = Regex("\\d+").find(credit("Czas trwania"))?.value?.toIntOrNull() ?: film.durationMinutes,
            description = description, detailsFetched = description.isNotBlank(),
        )
    }

    fun parseDay(html: String, date: LocalDate, fetchedAt: Long): ScheduleDay {
        val document = Jsoup.parse(html, "https://www.charlie.pl/")
        val dateHeading = date.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("pl")))
        require(document.select("h1").any { it.text().contains("Repertuar $dateHeading") }) {
            "Unexpected schedule page for $date"
        }
        val structured = document.select("script[type=application/ld+json]").mapNotNull {
            runCatching { JSONObject(it.data()) }.getOrNull()
        }.filter { it.optString("@type") == "ScreeningEvent" }
        val films = linkedMapOf<String, Film>()
        val screenings = mutableListOf<Screening>()
        var room = ""
        for (element in document.allElements) {
            if (element.tagName() == "h2" && element.text().startsWith("SALA ")) {
                room = element.text().removePrefix("SALA ").substringBefore(" (")
                    .lowercase(Locale.forLanguageTag("pl")).replaceFirstChar { it.titlecase() }
            }
            if (element.tagName() != "h3") continue
            val link = element.closest("a[href]") ?: continue
            val sourceUrl = SourceLinks.cinema(link.absUrl("href"))
            val id = Regex("[?&]id=(\\d+)").find(sourceUrl)?.groupValues?.get(1) ?: continue
            if (!sourceUrl.contains("i=film")) continue
            val row = element.closest("tr") ?: continue
            val time = row.select("b").firstOrNull { it.text().matches(Regex("\\d{1,2}[.:]\\d{2}")) }
                ?.text()?.replace('.', ':')?.padStart(5, '0') ?: continue
            require(room.isNotBlank()) { "Screening without a room" }
            val event = structured.firstOrNull {
                it.optString("url") == sourceUrl &&
                    it.optString("startDate").startsWith("${date}T$time")
            }
            val work = event?.optJSONObject("workPresented")
            val duration = runCatching {
                Duration.between(OffsetDateTime.parse(event!!.getString("startDate")),
                    OffsetDateTime.parse(event.getString("endDate"))).toMinutes().toInt()
            }.getOrDefault(0).coerceIn(0, 600)
            val metadata = element.closest("td")?.clone()?.apply {
                select("span, a, img, font, i").remove()
                select("br").forEach { it.after("\n") }
            }?.wholeText()?.lines()?.map { it.trim() }?.filter { it.isNotEmpty() }.orEmpty()
            val booking = row.select("a[href]").map { SourceLinks.booking(it.absUrl("href")) }
                .firstOrNull { it.isNotBlank() }.orEmpty()
            val film = Film(
                id = id, title = element.text(), sourceUrl = sourceUrl,
                posterUrl = SourceLinks.cinema(work?.optString("image").orEmpty()),
                genre = metadata.firstOrNull().orEmpty(),
                production = metadata.getOrNull(1).orEmpty(),
                durationMinutes = duration, director = work?.optString("director").orEmpty(),
                thumbnailUrl = SourceLinks.cinema(row.selectFirst("img[src*=grafika/filmy/male]")?.absUrl("src").orEmpty()),
            )
            films[id] = film
            screenings += Screening(id, date, time, room, booking,
                row.select("span[title]").map { it.attr("title") }
                    .filter { it in listOf("napisy", "dubbing", "lektor") }.distinct().joinToString(" · "))
        }
        // A structural change must not silently replace a useful cache with an empty day.
        require(screenings.size >= structured.size) { "Some screenings could not be read" }
        return ScheduleDay(date, films.values.toList(), screenings.distinctBy { it.key }
            .sortedBy { it.time }, fetchedAt)
    }
}
