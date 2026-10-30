package pl.charlie.repertuar

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Build
import android.content.ClipData
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
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

class MainActivity : ComponentActivity() {
    private val model: CharlieViewModel by viewModels()
    private val app get() = application as CharlieApplication
    private var openingInstaller = false
    private val installPermission = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (packageManager.canRequestPackageInstalls()) installUpdate()
        else app.apkDownloads.installationError("Aby zainstalować aktualizację, zezwól Charlie na instalowanie aplikacji w ustawieniach Androida.")
    }
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
            val download by model.apkDownload.collectAsStateWithLifecycle()
            LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { model.onResume() }
            CharlieTheme {
                CharlieApp(library, refreshing, error, online, model::refresh, model::save,
                    { screening -> SourceLinks.booking(screening.bookingUrl).takeIf { it.isNotBlank() }?.let(::open) },
                    ::open, today, startup, AppUpdateUi(updates, model::checkUpdates, model::downloadUpdate,
                        download, ::installUpdate, model::cancelDownload))
            }
        }
    }

    private fun installUpdate() {
        if (openingInstaller) return
        openingInstaller = true
        lifecycleScope.launch {
            try {
                app.apkDownloads.installationError(null)
                val apk = app.apkDownloads.preparedApk() ?: return@launch
                if (!packageManager.canRequestPackageInstalls()) {
                    installPermission.launch(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:$packageName")))
                } else {
                    val uri = FileProvider.getUriForFile(this@MainActivity, "$packageName.updates", apk)
                    startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(uri, "application/vnd.android.package-archive")
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        .apply { clipData = ClipData.newRawUri("Aktualizacja Charlie", uri) })
                }
            } catch (_: ActivityNotFoundException) {
                app.apkDownloads.installationError("Nie udało się otworzyć instalatora Androida. Spróbuj ponownie.")
            } catch (_: SecurityException) {
                app.apkDownloads.installationError("Android nie zezwolił na instalację. Sprawdź zgodę dla Charlie i spróbuj ponownie.")
            } finally { openingInstaller = false }
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
