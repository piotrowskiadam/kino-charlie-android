package pl.charlie.repertuar.data

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class CharlieParserTest {
    @Test fun `ticket poster matches the exact event date time and film without replacing room metadata`() {
        val parser = CharlieParser()
        val day = parser.parseDay(javaClass.getResource("/schedule-2026-10-30.html")!!.readText(charset("ISO-8859-2")),
            LocalDate.parse("2026-10-30"), 100L)
        val html = javaClass.getResource("/ticket-posters-2026-10-30.html")!!.readText()
        assertEquals("https://bilety.charlie.pl/MSI/ImageData.ashx?id=2980&mode=thumb",
            parser.ticketPosters(html, day)["1783855134"])
        assertTrue(parser.ticketPosters(html.replace("97086", "100000").replace("97087", "100001")
            .replace("97196", "100002").replace("97197", "100003"), day).isEmpty())
        assertTrue(parser.ticketPosters(html.replace("30 października 2026", "31 października 2026"), day).isEmpty())
        assertTrue(parser.ticketPosters(html.replace("bilety.charlie.pl/MSI/ImageData", "evil.example/MSI/ImageData"), day).isEmpty())
    }
    @Test fun `source thumbnail is retained as fallback when structured poster is broken`() {
        val html = javaClass.getResource("/schedule-2026-10-30.html")!!.readText(charset("ISO-8859-2"))
        val day = CharlieParser().parseDay(html, LocalDate.parse("2026-10-30"), 100L)
        assertEquals("https://www.charlie.pl/grafika/filmy/male/8434.jpg", day.films.single { it.id == "1783855134" }.thumbnailUrl)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `wrong date or generic homepage cannot overwrite a cached schedule`() {
        val html = javaClass.getResource("/schedule-2026-10-30.html")!!.readText(charset("ISO-8859-2"))
        CharlieParser().parseDay(html, LocalDate.parse("2026-10-31"), 100L)
    }

    @Test fun `foreign or insecure checkout is never accepted from source markup`() {
        val html = javaClass.getResource("/schedule-2026-10-30.html")!!.readText(charset("ISO-8859-2"))
            .replace("https://bilety.charlie.pl/", "https://bilety.charlie.pl.evil.example/")
        val day = CharlieParser().parseDay(html, LocalDate.parse("2026-10-30"), 100L)
        assertTrue(day.screenings.isNotEmpty())
        assertTrue(day.screenings.all { it.bookingUrl.isBlank() })
        assertEquals("", SourceLinks.booking("http://bilety.charlie.pl/MSI/"))
        assertEquals("", SourceLinks.booking("https://other@bilety.charlie.pl/MSI/"))
    }

    @Test fun `film description and credits are readable offline metadata`() {
        val html = javaClass.getResource("/film-1787938780.html")!!.readText(charset("ISO-8859-2"))
        val film = CharlieParser().parseDetails(html, Film("1787938780", "Czas, który nie nadszedł",
            "https://www.charlie.pl/index.php?i=film&id=1787938780"))
        assertEquals("Julia Rogowska", film.director)
        assertEquals(105, film.durationMinutes)
        assertTrue(film.description.startsWith("W świecie, w którym starzenie się"))
        assertTrue(film.description.contains("Sara i Oskar"))
        assertTrue(film.detailsFetched)
    }

    @Test fun `live cinema HTML associates each screening with its exact film room and checkout`() {
        val html = javaClass.getResource("/schedule-2026-10-30.html")!!.readText(charset("ISO-8859-2"))
        val day = CharlieParser().parseDay(html, LocalDate.parse("2026-10-30"), 100L)
        val lalka = day.films.single { it.title == "Lalka" }
        assertEquals("1783855134", lalka.id)
        assertEquals(162, lalka.durationMinutes)
        assertEquals("https://www.charlie.pl/grafika/filmy/plakaty/9/9067.jpg", lalka.posterUrl)
        assertEquals(4, day.screenings.count { it.filmId == lalka.id })
        val screenings = day.screenings.filter { it.filmId == lalka.id && it.room == "Studyjna" }
        assertEquals(listOf("13:45", "16:50"), screenings.map { it.time })
        assertTrue(screenings.all { it.room == "Studyjna" })
        assertTrue(screenings[0].bookingUrl.contains("event_id=97086&"))
        assertTrue(screenings[1].bookingUrl.contains("event_id=97087&"))
        assertTrue(day.films.any { it.title == "Pieśni lasu" })
    }
}
