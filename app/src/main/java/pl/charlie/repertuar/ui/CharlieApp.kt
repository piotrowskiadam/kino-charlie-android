package pl.charlie.repertuar.ui

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import pl.charlie.repertuar.BuildConfig
import pl.charlie.repertuar.StartupPhase
import pl.charlie.repertuar.data.*
import java.time.LocalDate
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.text.Normalizer
import java.util.Locale

private val Polish = Locale.forLanguageTag("pl")
private val DateLabel = DateTimeFormatter.ofPattern("EEEE, d MMMM", Polish)
private fun String.sentenceCase() = replaceFirstChar { it.titlecase(Polish) }
private fun String.searchKey() = Normalizer.normalize(lowercase(Polish).replace('ł', 'l'), Normalizer.Form.NFD)
    .replace(Regex("\\p{M}+"), "")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharlieApp(library: Library, refreshing: Boolean, error: String?, online: Boolean,
    onRefresh: () -> Unit, onSave: (String) -> Unit, onBook: (Screening) -> Unit,
    onOpenUrl: (String) -> Unit, today: LocalDate, startupPhase: StartupPhase = StartupPhase.Ready,
    updates: AppUpdateUi = AppUpdateUi()) {
    if (startupPhase == StartupPhase.CheckingCache) {
        Surface(Modifier.fillMaxSize().testTag("startup-cache")) {
            Box(Modifier.safeDrawingPadding(), contentAlignment = Alignment.Center) { CinemaLogo() }
        }
        return
    }
    if (startupPhase != StartupPhase.Ready && today !in library.days) {
        FirstRunWelcome(startupPhase == StartupPhase.Failed, online, error, onRefresh)
        return
    }
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var selected by rememberSaveable(today.toString()) { mutableStateOf(today.toString()) }
    var query by rememberSaveable { mutableStateOf("") }
    var detailId by rememberSaveable { mutableStateOf<String?>(null) }
    var booking by remember { mutableStateOf<Screening?>(null) }
    val date = LocalDate.parse(selected)
    val detail = library.films[detailId]
    val listState = rememberLazyListState()
    val savedState = rememberLazyListState()
    BackHandler(detail != null) { detailId = null }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val wide = maxWidth >= 600.dp
        Scaffold(
            containerColor = MaterialTheme.colorScheme.surface,
            topBar = {
                if (detail != null) TopAppBar(title = { Text("O filmie", style = MaterialTheme.typography.titleMedium) },
                    navigationIcon = { IconButton(onClick = { detailId = null }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Wróć do repertuaru")
                    } }, actions = { IconButton(onClick = { onSave(detail.id) }) {
                        Icon(if (detail.id in library.savedIds) Icons.Outlined.BookmarkAdded else Icons.Outlined.BookmarkBorder,
                            if (detail.id in library.savedIds) "Usuń zapisany film" else "Zapisz film")
                    } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface))
                else Row(Modifier.fillMaxWidth().statusBarsPadding().padding(start = 24.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    CinemaLogo()
                    Spacer(Modifier.width(16.dp))
                    Text("Łódź", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onRefresh, enabled = !refreshing, modifier = Modifier.testTag("refresh")) {
                        Icon(Icons.Outlined.Refresh, "Odśwież repertuar")
                    }
                }
            },
            bottomBar = {
                if (!wide && detail == null) NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainerLow, tonalElevation = 0.dp) {
                    destinations.forEachIndexed { index, destination ->
                        NavigationBarItem(selected = tab == index, onClick = { tab = index },
                            icon = { DestinationIcon(destination, updates.state.available != null) }, label = { Text(destination.label) },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary, indicatorColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier.testTag(destination.tag))
                    }
                }
            },
        ) { insets ->
            Row(Modifier.padding(insets).fillMaxSize()) {
                if (wide && detail == null) NavigationRail(containerColor = MaterialTheme.colorScheme.surfaceContainerLow) {
                    Spacer(Modifier.height(16.dp))
                    destinations.forEachIndexed { index, destination -> NavigationRailItem(
                        selected = tab == index, onClick = { tab = index }, icon = { DestinationIcon(destination, updates.state.available != null) },
                        label = { Text(destination.label) }, modifier = Modifier.testTag(destination.tag)) }
                }
                if (detail != null) {
                    FilmDetails(detail, library, today, onOpenUrl, { booking = it }, onRefresh)
                } else if (tab == 2) {
                    CinemaInfo(library, online, refreshing, onRefresh, onOpenUrl, updates)
                } else {
                    val screenings = library.days[date]?.screenings.orEmpty()
                    val films = if (tab == 1) library.savedIds.mapNotNull { library.films[it] }.sortedBy { it.title }
                    else screenings.map { it.filmId }.distinct().mapNotNull { library.films[it] }
                        .filter { it.title.searchKey().contains(query.searchKey()) }
                    val timesByFilm = if (tab == 1) films.associate { film -> film.id to
                        library.screenings(film.id).filter { !it.date.isBefore(today) }
                            .groupBy { it.room }.values.flatMap { it.take(3) }
                    } else screenings.groupBy { it.filmId }
                    LazyColumn(state = if (tab == 1) savedState else listState,
                        modifier = Modifier.weight(1f).testTag("film-list"),
                        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 24.dp)) {
                        item("heading") {
                            Text(if (tab == 0) "Wybierz swój seans." else "Na Twojej liście.",
                                style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp))
                            CacheStatus(if (tab == 0) library.days[date]?.fetchedAt ?: 0L else library.lastUpdated,
                                refreshing, online, "Ten dzień czeka na pobranie")
                            Spacer(Modifier.height(16.dp))
                        }
                        if (error != null) item("error") { ErrorNotice(error, onRefresh, refreshing) }
                        if (tab == 0) {
                            item("dates") {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 12.dp)) {
                                    items(14, key = { today.plusDays(it.toLong()).toString() }) { offset ->
                                        val candidate = today.plusDays(offset.toLong())
                                        val active = date == candidate
                                        Surface(color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLow,
                                            contentColor = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                            shape = RoundedCornerShape(12.dp), modifier = Modifier.widthIn(min = 60.dp)
                                                .testTag("date-$candidate").semantics { this.selected = active }
                                                .clickable { selected = candidate.toString() }) {
                                            Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(when (offset) { 0 -> "Dziś"; 1 -> "Jutro"; else -> candidate.format(DateTimeFormatter.ofPattern("EEE", Polish)).sentenceCase().removeSuffix(".") },
                                                    style = MaterialTheme.typography.labelMedium)
                                                Text(candidate.dayOfMonth.toString(), style = MaterialTheme.typography.titleLarge.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Default))
                                            }
                                        }
                                    }
                                }
                                OutlinedTextField(value = query, onValueChange = { query = it }, singleLine = true,
                                    placeholder = { Text("Szukaj filmu") },
                                    leadingIcon = { Icon(Icons.Outlined.Search, null) },
                                    trailingIcon = { if (query.isNotEmpty()) IconButton(onClick = { query = "" }) { Icon(Icons.Outlined.Close, "Wyczyść wyszukiwanie") } },
                                    shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().testTag("search")
                                        .semantics { contentDescription = "Szukaj filmu" })
                                Row(Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(date.format(DateLabel).sentenceCase(), style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                                    Spacer(Modifier.width(8.dp))
                                    Text("${films.size} ${filmCountLabel(films.size)}", style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        if (films.isEmpty()) item("empty") {
                            when {
                                tab == 1 -> EmptyState(Icons.Outlined.BookmarkBorder, "Dobry film warto zapisać",
                                    "Otwórz film i dotknij zakładki. Wrócisz do niego tutaj, także bez internetu.")
                                query.isNotBlank() -> EmptyState(Icons.Outlined.Search, "Nie znaleziono filmu", "Spróbuj innego tytułu lub wybierz inny dzień.")
                                refreshing && date !in library.days -> EmptyState(Icons.Outlined.CloudDownload, "Pobieramy repertuar", "Za chwilę będzie dostępny także bez internetu.")
                                !online && date !in library.days -> EmptyState(Icons.Outlined.WifiOff, "Ten dzień czeka na pobranie", "Połącz się z internetem i odśwież repertuar.", "Spróbuj ponownie", onRefresh)
                                error != null && date !in library.days -> EmptyState(Icons.Outlined.CloudOff, "Repertuar jest niedostępny", "Sprawdź połączenie i spróbuj ponownie.", "Odśwież repertuar", onRefresh)
                                date !in library.days -> EmptyState(Icons.Outlined.CloudDownload, "Ten dzień czeka na pobranie", "Odśwież repertuar, aby sprawdzić seanse na wybrany dzień.", "Odśwież repertuar", onRefresh)
                                else -> EmptyState(Icons.Outlined.EventAvailable, "Jeszcze bez seansów", "Kino nie opublikowało seansów na ten dzień. Sprawdź inny termin lub wróć później.", "Odśwież repertuar", onRefresh)
                            }
                        }
                        if (wide) {
                            items(films.chunked(2), key = { it.first().id }) { pair ->
                                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                                    pair.forEach { film -> Box(Modifier.weight(1f)) {
                                        FilmRow(film, timesByFilm[film.id].orEmpty(), { detailId = film.id }, { booking = it }, tab == 1)
                                    } }
                                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                                }
                            }
                        } else items(films, key = { it.id }) { film ->
                            FilmRow(film, timesByFilm[film.id].orEmpty(), { detailId = film.id }, { booking = it }, tab == 1)
                        }
                        if (films.isNotEmpty()) item("source") {
                            Text("Repertuar: charlie.pl · godziny i dostępność potwierdza kasa kina.",
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 20.dp))
                        }
                    }
                }
            }
        }
    }
    booking?.let { screening ->
        val film = library.films[screening.filmId]
        ModalBottomSheet(onDismissRequest = { booking = null }, containerColor = MaterialTheme.colorScheme.surface) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 24.dp).verticalScroll(rememberScrollState())) {
                Text(film?.title.orEmpty(), style = MaterialTheme.typography.headlineSmall)
                Text("${screening.date.format(DateLabel).sentenceCase()} · ${screening.time}",
                    style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 12.dp))
                Text("Sala ${screening.room}", style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp).testTag("selected-room"))
                if (screening.version.isNotBlank()) Text(screening.version, style = MaterialTheme.typography.bodyMedium)
                Text(if (online) "Wybór miejsc i płatność odbędą się na stronie Kina Charlie. Ceny i dostępność sprawdzisz w kasie online."
                    else "Do zakupu biletu potrzebujesz internetu.", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(vertical = 20.dp))
                Button(onClick = { booking = null; onBook(screening) }, enabled = online && SourceLinks.booking(screening.bookingUrl).isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                    Icon(Icons.Outlined.ConfirmationNumber, null, Modifier.size(20.dp)); Spacer(Modifier.width(10.dp)); Text("Przejdź do kasy kina")
                }
                if (screening.bookingUrl.isBlank()) Text("Kino nie udostępniło linku do zakupu tego seansu.",
                    style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 12.dp))
                TextButton(onClick = { booking = null }, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Wróć do przeglądania") }
            }
        }
    }
}

private data class Destination(val label: String, val icon: ImageVector, val tag: String)

@Composable
private fun DestinationIcon(destination: Destination, updateAvailable: Boolean) {
    BadgedBox(badge = {
        if (updateAvailable && destination.tag == "tab-cinema") Badge(containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.semantics { contentDescription = "Dostępna aktualizacja aplikacji" })
    }) { Icon(destination.icon, null) }
}
private val destinations = listOf(Destination("Repertuar", Icons.Outlined.CalendarMonth, "tab-schedule"),
    Destination("Zapisane", Icons.Outlined.BookmarkBorder, "tab-saved"), Destination("Kino", Icons.Outlined.Info, "tab-cinema"))

@Composable
private fun CacheStatus(timestamp: Long, refreshing: Boolean, online: Boolean, emptyLabel: String = "Repertuar zapisze się na telefonie") {
    val date = Instant.ofEpochMilli(timestamp).atZone(ZoneId.of("Europe/Warsaw"))
    val text = when {
        refreshing -> "Odświeżanie repertuaru"
        timestamp == 0L -> if (online) emptyLabel else "Brak połączenia z internetem"
        else -> (if (!online) "Offline · " else "") + "Zapisano ${date.format(DateTimeFormatter.ofPattern("dd.MM, HH:mm", Polish))}"
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        if (refreshing) CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 1.5.dp)
        else Icon(if (online && timestamp > 0) Icons.Outlined.CheckCircle else if (!online) Icons.Outlined.WifiOff else Icons.Outlined.Download,
            null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
        Text(text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ErrorNotice(error: String, retry: () -> Unit, refreshing: Boolean) {
    Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(bottom = 12.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text(error, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer)
            TextButton(onClick = retry, enabled = !refreshing) { Text("Spróbuj ponownie") }
        }
    }
}

@Composable
private fun Poster(film: Film, modifier: Modifier) {
    val candidates = remember(film.posterUrl, film.ticketPosterUrl) {
        film.posterCandidates
    }
    var candidate by remember(candidates) { mutableIntStateOf(0) }
    Box(modifier.clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceContainerHighest), contentAlignment = Alignment.Center) {
        Icon(Icons.Outlined.Theaters, null, Modifier.size(32.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        AsyncImage(model = candidates.getOrNull(candidate),
            contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.matchParentSize(),
            onError = { if (candidate < candidates.size) candidate++ })
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilmRow(film: Film, screenings: List<Screening>, open: () -> Unit, book: (Screening) -> Unit, includeDate: Boolean) {
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(Modifier.fillMaxWidth().testTag("film-${film.id}").clickable(onClickLabel = "Otwórz film ${film.title}", onClick = open)
            .padding(vertical = 14.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Poster(film, Modifier.width(82.dp).height(123.dp))
            Column(Modifier.weight(1f)) {
                Text(film.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                if (film.genre.isNotBlank()) Text(film.genre, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 3.dp))
                val meta = listOf(film.production, if (film.durationMinutes > 0) "${film.durationMinutes} min" else "").filter { it.isNotBlank() }.joinToString(" · ")
                if (meta.isNotEmpty()) Text(meta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
                    screenings.groupBy { it.room }.forEach { (room, roomScreenings) ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Sala $room", style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.testTag("room-${film.id}-$room"))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                roomScreenings.forEach { screening ->
                                    OutlinedButton(onClick = { book(screening) }, shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                        modifier = Modifier.heightIn(min = 48.dp).testTag("screening-${screening.key}")
                                            .semantics { contentDescription = "${screening.time}, sala ${screening.room}" }) {
                                        Text((if (includeDate) screening.date.format(DateTimeFormatter.ofPattern("dd.MM")) + " · " else "") + screening.time,
                                            fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                                    }
                                }
                            }
                        }
                    }
                }
                if (screenings.isEmpty()) Text("Brak nadchodzących seansów", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

@Composable
private fun FilmDetails(film: Film, library: Library, today: LocalDate, openUrl: (String) -> Unit, book: (Screening) -> Unit, refresh: () -> Unit) {
    LazyColumn(contentPadding = PaddingValues(24.dp), modifier = Modifier.fillMaxSize().testTag("film-details")) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Poster(film, Modifier.width(110.dp).height(165.dp))
                Column(Modifier.weight(1f)) {
                    Text(film.title, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                    if (film.genre.isNotBlank()) Text(film.genre, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 12.dp))
                    if (film.production.isNotBlank()) Text(film.production, style = MaterialTheme.typography.bodyMedium)
                    if (film.durationMinutes > 0) Text("${film.durationMinutes} min", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
                }
            }
            if (film.director.isNotBlank()) Text("Reżyseria: ${film.director}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 20.dp))
            Text(film.description.ifBlank { "Opis nie został jeszcze pobrany. Po połączeniu z internetem aplikacja uzupełni go w tle." },
                style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 16.dp))
            Row(Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { openUrl("https://www.filmweb.pl/search?q=${Uri.encode(film.title)}") }) {
                    Icon(Icons.Outlined.OpenInNew, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Szukaj w Filmweb")
                }
            }
            Text("Link otwiera wyszukiwanie po tytule.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = { openUrl(film.sourceUrl) }) { Text("Film na stronie kina") }
            Text("Najbliższe seanse", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 24.dp, bottom = 12.dp))
        }
        val upcoming = library.screenings(film.id).filter { !it.date.isBefore(today) }
        if (upcoming.isEmpty()) item { EmptyState(Icons.Outlined.EventAvailable, "Brak zaplanowanych seansów", "Sprawdź repertuar ponownie później.", "Odśwież", refresh) }
        items(upcoming, key = { it.key }) { screening ->
            ListItem(headlineContent = { Text("Sala ${screening.room}", style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary) },
                supportingContent = { Text("${screening.date.format(DateLabel).sentenceCase()} · ${screening.time}" +
                    if (screening.version.isNotBlank()) " · ${screening.version}" else "") },
                trailingContent = { Icon(Icons.Outlined.ConfirmationNumber, null, tint = MaterialTheme.colorScheme.primary) },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.clickable { book(screening) }.heightIn(min = 64.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Composable
private fun CinemaInfo(library: Library, online: Boolean, refreshing: Boolean, refresh: () -> Unit,
    openUrl: (String) -> Unit, updates: AppUpdateUi) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
        Text("Do zobaczenia w kinie.", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
        Text("Kino-Galeria Charlie", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 32.dp))
        Text("Piotrkowska 203/205\n90-451 Łódź", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 12.dp))
        Button(onClick = { openUrl("geo:51.7550156,19.458785?q=${Uri.encode("Kino Charlie, Piotrkowska 203/205, Łódź")}") }, modifier = Modifier.padding(top = 20.dp)) {
            Icon(Icons.Outlined.Directions, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text("Wyznacz trasę")
        }
        TextButton(onClick = { openUrl("tel:+48426360092") }) { Text("Zadzwoń: 42 636 00 92") }
        TextButton(onClick = { openUrl("https://www.charlie.pl/") }) { Text("Otwórz stronę kina") }
        HorizontalDivider(Modifier.padding(vertical = 24.dp))
        Text("Repertuar pod ręką", style = MaterialTheme.typography.titleLarge)
        Text("Zapisujemy do 14 dni repertuaru, opisy i plakaty, gdy kino je udostępni. Aplikacja odświeża dane w tle, gdy pozwalają na to połączenie i ustawienia baterii telefonu.",
            style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(vertical = 16.dp))
        CacheStatus(library.lastUpdated, refreshing, online)
        TextButton(onClick = refresh, enabled = !refreshing) { Text("Odśwież teraz") }
        Text("Kupno biletu wymaga internetu. Ceny, miejsca i płatność obsługuje strona Kina Charlie.", style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 12.dp))
        HorizontalDivider(Modifier.padding(vertical = 24.dp))
        AppUpdateSection(updates, online, openUrl)
        HorizontalDivider(Modifier.padding(vertical = 24.dp))
        Text("Nieoficjalna aplikacja · MVP ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.labelLarge)
        Text("Repertuar, opisy i plakaty pochodzą z charlie.pl. Aplikacja nie jest powiązana z kinem. Bez konta i reklam.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp, bottom = 24.dp))
    }
}

@Composable
private fun EmptyState(icon: ImageVector, title: String, description: String, action: String? = null, onAction: () -> Unit = {}) {
    Column(Modifier.fillMaxWidth().padding(vertical = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary)
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 16.dp))
        Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp))
        if (action != null) TextButton(onClick = onAction, modifier = Modifier.padding(top = 12.dp)) { Text(action) }
    }
}

private fun filmCountLabel(count: Int): String = when {
    count == 1 -> "film"
    count % 10 in 2..4 && count % 100 !in 12..14 -> "filmy"
    else -> "filmów"
}
