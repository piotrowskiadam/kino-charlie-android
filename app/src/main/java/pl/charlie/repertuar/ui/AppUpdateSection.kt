package pl.charlie.repertuar.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.charlie.repertuar.BuildConfig
import pl.charlie.repertuar.updates.AppRelease
import pl.charlie.repertuar.updates.UpdateState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class AppUpdateUi(
    val state: UpdateState = UpdateState(),
    val check: () -> Unit = {},
    val download: (AppRelease) -> Unit = {},
)

@Composable
internal fun AppUpdateSection(updates: AppUpdateUi, online: Boolean, openUrl: (String) -> Unit) {
    val state = updates.state
    val release = state.available
    Column(Modifier.fillMaxWidth()) {
        Text("Aktualizacje aplikacji", style = MaterialTheme.typography.titleLarge)
        Text("Zainstalowana wersja ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp))
        if (release != null) {
            Text("Dostępna wersja ${release.version}", style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 16.dp))
            Button(onClick = { updates.download(release) }, enabled = online,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp).heightIn(min = 52.dp)) {
                Text("Pobierz aktualizację")
            }
            Text("Otworzymy pobieranie APK w przeglądarce. Otwórz pobrany plik i potwierdź instalację w Androidzie.",
                style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
            TextButton(onClick = { openUrl(release.pageUrl) }, enabled = online) { Text("Co nowego w tej wersji") }
        } else if (state.checkedAt > 0 && state.error == null && !state.checking) {
            Text("Masz aktualną wersję aplikacji.", style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 12.dp))
        }
        if (state.checking) {
            LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 12.dp))
            Text("Sprawdzamy aktualizacje…", style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp))
        }
        if (state.checkedAt > 0) Text("Sprawdzono " + Instant.ofEpochMilli(state.checkedAt).atZone(ZoneId.of("Europe/Warsaw"))
            .format(DateTimeFormatter.ofPattern("d.MM, HH:mm", Locale.forLanguageTag("pl"))),
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp))
        state.error?.let { Text(it, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp)) }
        if (!online) Text("Połącz się z internetem, aby sprawdzić lub pobrać aktualizację.",
            style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 12.dp))
        TextButton(onClick = updates.check, enabled = online && !state.checking) { Text("Sprawdź aktualizacje") }
    }
}
