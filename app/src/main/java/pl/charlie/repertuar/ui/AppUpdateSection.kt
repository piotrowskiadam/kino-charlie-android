package pl.charlie.repertuar.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.charlie.repertuar.BuildConfig
import pl.charlie.repertuar.updates.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class AppUpdateUi(
    val state: UpdateState = UpdateState(),
    val check: () -> Unit = {},
    val download: (AppRelease) -> Unit = {},
    val transfer: ApkDownloadState = ApkDownloadState(),
    val install: () -> Unit = {},
    val cancel: () -> Unit = {},
)

@Composable
internal fun AppUpdateSection(updates: AppUpdateUi, online: Boolean, openUrl: (String) -> Unit) {
    val state = updates.state
    val transfer = updates.transfer
    val release = transfer.release?.takeIf { transfer.busy || transfer.phase == DownloadPhase.Ready } ?: state.available ?: transfer.release
    val active = transfer.takeIf { it.release?.version == release?.version } ?: ApkDownloadState()
    Column(Modifier.fillMaxWidth()) {
        Text("Aktualizacje aplikacji", style = MaterialTheme.typography.titleLarge)
        Text("Zainstalowana wersja ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp))
        if (release != null) {
            Text("Dostępna wersja ${release.version}", style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 16.dp))
            when (active.phase) {
                DownloadPhase.Downloading -> {
                    val progress = (active.bytes.toFloat() / release.size.coerceAtLeast(1)).coerceIn(0f, 1f)
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().padding(top = 12.dp))
                    Text("Pobieranie aktualizacji… ${(progress * 100).toInt()}%",
                        modifier = Modifier.padding(top = 8.dp))
                    ProgramTextButton(onClick = updates.cancel) { Text("Anuluj pobieranie") }
                }
                DownloadPhase.Verifying -> {
                    LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 12.dp))
                    Text("Sprawdzamy pobrany plik…", modifier = Modifier.padding(top = 8.dp))
                }
                DownloadPhase.Ready -> {
                    ProgramButton(onClick = updates.install,
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp).heightIn(min = 52.dp)) {
                        Text("Zainstaluj aktualizację")
                    }
                    Text("Plik jest gotowy. Możesz zainstalować go także offline. Android poprosi o potwierdzenie instalacji.",
                        style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
                    state.available?.takeIf { it.version > release.version }?.let { newer ->
                        ProgramTextButton(onClick = { updates.download(newer) }, enabled = online && !state.checking) {
                            Text("Pobierz nowszą wersję ${newer.version}")
                        }
                    }
                }
                else -> {
                    ProgramButton(onClick = { updates.download(release) }, enabled = online && !state.checking,
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp).heightIn(min = 52.dp)) {
                        Text(if (active.phase == DownloadPhase.Failed) "Pobierz ponownie" else "Pobierz aktualizację")
                    }
                    Text("Pobierz plik tutaj, a potem wybierz „Zainstaluj”. Przy pierwszej instalacji Android poprosi o zgodę dla Charlie.",
                        style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
                }
            }
            ProgramTextButton(onClick = { openUrl(release.pageUrl) }, enabled = online) { Text("Co nowego w tej wersji") }
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
            color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(top = 8.dp)) }
        transfer.error?.takeIf { transfer.release == null || transfer.release?.version == release?.version }?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
        }
        if (!online) Text("Połącz się z internetem, aby sprawdzić lub pobrać aktualizację.",
            style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 12.dp))
        ProgramOutlinedButton(onClick = updates.check, enabled = online && !state.checking && !transfer.busy,
            modifier = Modifier.padding(top = 8.dp)) {
            Icon(Icons.Outlined.SystemUpdate, null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Sprawdź aktualizacje")
        }
    }
}
