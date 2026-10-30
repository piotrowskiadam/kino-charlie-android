package pl.charlie.repertuar.data

import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

/** Versioned local snapshot; source HTML never enters the UI or a WebView. */
object LibraryJson {
    fun encode(library: Library): String = JSONObject().apply {
        put("version", 1)
        put("saved", JSONArray(library.savedIds.toList()))
        put("films", JSONArray(library.films.values.map { film -> JSONObject().apply {
            put("id", film.id); put("title", film.title); put("source", film.sourceUrl)
            put("poster", film.posterUrl); put("genre", film.genre); put("production", film.production)
            put("duration", film.durationMinutes); put("director", film.director)
            put("description", film.description); put("detailsFetched", film.detailsFetched)
            put("thumbnail", film.thumbnailUrl)
            put("ticketPoster", film.ticketPosterUrl)
        } }))
        put("days", JSONArray(library.days.values.map { day -> JSONObject().apply {
            put("date", day.date.toString()); put("fetchedAt", day.fetchedAt)
            put("films", JSONArray(day.films.map { it.id }))
            put("screenings", JSONArray(day.screenings.map { screening -> JSONObject().apply {
                put("film", screening.filmId); put("time", screening.time); put("room", screening.room)
                put("booking", screening.bookingUrl); put("version", screening.version)
            } }))
        } }))
    }.toString()

    fun decode(text: String): Library {
        val root = JSONObject(text)
        require(root.getInt("version") == 1)
        val films = root.getJSONArray("films").objects().map { film ->
            Film(film.getString("id"), film.getString("title"), film.getString("source"),
                film.optString("poster"), film.optString("genre"), film.optString("production"),
                film.optInt("duration"), film.optString("director"), film.optString("description"),
                film.optBoolean("detailsFetched"), film.optString("thumbnail"),
                SourceLinks.poster(film.optString("ticketPoster")))
        }.associateBy { it.id }
        val days = root.getJSONArray("days").objects().map { day ->
            val date = LocalDate.parse(day.getString("date"))
            val ids = day.getJSONArray("films")
            ScheduleDay(date, (0 until ids.length()).mapNotNull { films[ids.getString(it)] },
                day.getJSONArray("screenings").objects().map { screening ->
                    Screening(screening.getString("film"), date, screening.getString("time"),
                        screening.getString("room"), SourceLinks.booking(screening.optString("booking")),
                        screening.optString("version"))
                }, day.getLong("fetchedAt"))
        }.associateBy { it.date }
        val saved = root.getJSONArray("saved")
        return Library(days, films, (0 until saved.length()).map { saved.getString(it) }.toSet())
    }

    private fun JSONArray.objects(): List<JSONObject> = (0 until length()).map { getJSONObject(it) }
}
