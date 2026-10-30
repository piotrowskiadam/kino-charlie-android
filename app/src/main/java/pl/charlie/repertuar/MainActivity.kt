package pl.charlie.repertuar

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Build
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.activity.viewModels
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.Lifecycle
import androidx.compose.runtime.getValue
import pl.charlie.repertuar.data.SourceLinks
import pl.charlie.repertuar.ui.*
import pl.charlie.repertuar.updates.AppRelease

class MainActivity : ComponentActivity() {
    private val model: CharlieViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val paper = Color.rgb(248, 244, 236)
        enableEdgeToEdge(
            statusBarStyle = if (dark) SystemBarStyle.dark(Color.TRANSPARENT)
                else SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = if (dark) SystemBarStyle.dark(Color.BLACK) else SystemBarStyle.light(paper, Color.BLACK),
        )
        if (Build.VERSION.SDK_INT >= 29) window.isNavigationBarContrastEnforced = false
        setContent {
            val library by model.library.collectAsStateWithLifecycle()
            val refreshing by model.refreshing.collectAsStateWithLifecycle()
            val error by model.error.collectAsStateWithLifecycle()
            val online by model.online.collectAsStateWithLifecycle()
            val today by model.today.collectAsStateWithLifecycle()
            val startup by model.startup.collectAsStateWithLifecycle()
            val updates by model.updates.collectAsStateWithLifecycle()
            LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { model.onResume() }
            CharlieTheme {
                CharlieApp(library, refreshing, error, online, model::refresh, model::save,
                    { screening -> SourceLinks.booking(screening.bookingUrl).takeIf { it.isNotBlank() }?.let(::open) },
                    ::open, today, startup, AppUpdateUi(updates, model::checkUpdates, ::downloadUpdate))
            }
        }
    }

    private fun downloadUpdate(release: AppRelease) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(release.downloadUrl))
                .addCategory(Intent.CATEGORY_BROWSABLE))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, "Zainstaluj przeglądarkę, aby pobrać aktualizację.", Toast.LENGTH_LONG).show()
        }
    }

    private fun open(url: String) {
        val uri = Uri.parse(url)
        if (uri.scheme == "https") {
            try { CustomTabsIntent.Builder().setShowTitle(true).build().launchUrl(this, uri) }
            catch (_: ActivityNotFoundException) { model.browserError() }
        } else if (uri.scheme in listOf("geo", "tel", "mailto")) {
            try { startActivity(Intent(Intent.ACTION_VIEW, uri)) }
            catch (_: ActivityNotFoundException) { model.browserError() }
        }
    }
}
