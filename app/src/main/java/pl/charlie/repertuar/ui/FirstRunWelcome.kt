package pl.charlie.repertuar.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
internal fun FirstRunWelcome(failed: Boolean, online: Boolean, error: String?, retry: () -> Unit) {
    Surface(Modifier.fillMaxSize().testTag("startup-welcome")) {
        Box(Modifier.safeDrawingPadding().padding(24.dp), contentAlignment = Alignment.Center) {
            Column(Modifier.widthIn(max = 480.dp).fillMaxWidth().verticalScroll(rememberScrollState())) {
                CinemaLogo(104.dp)
                Spacer(Modifier.height(32.dp))
                Text("Dobry seans\nzaczyna się tutaj.", style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary, modifier = Modifier.semantics { heading() })
                Spacer(Modifier.height(12.dp))
                Text("Filmy, godziny i sale w jednym miejscu.", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(32.dp))
                if (failed) {
                    Text(if (!online) "Na pierwszy repertuar potrzebujesz internetu. Połącz się i spróbuj ponownie."
                        else error ?: "Nie udało się przygotować repertuaru. Spróbuj ponownie.",
                        style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = retry, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                        Text("Spróbuj ponownie")
                    }
                } else {
                    Text("Przygotowujemy Twój repertuar…", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}
