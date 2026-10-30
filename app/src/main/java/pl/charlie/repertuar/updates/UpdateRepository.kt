package pl.charlie.repertuar.updates

import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject

class UpdateRepository(private val directory: File, private val source: GitHubReleaseSource,
    private val installed: AppVersion, private val clock: () -> Long = System::currentTimeMillis) {
    private val mutableState = MutableStateFlow(UpdateState())
    val state = mutableState.asStateFlow()
    private val lock = Mutex()
    private var loaded = false
    private var lastAttempt = 0L
    private var lastRelease: AppRelease? = null
    private val cache get() = File(directory, "latest.json")

    suspend fun load() = withContext(Dispatchers.IO) { lock.withLock { loadOnce() } }

    suspend fun check(force: Boolean = false) = withContext(Dispatchers.IO) {
        if (!lock.tryLock()) return@withContext
        try {
            loadOnce()
            val now = clock()
            if (!force && lastAttempt != 0L && now >= lastAttempt && now - lastAttempt < 24 * 60 * 60 * 1000L) return@withContext
            lastAttempt = now
            try {
                persist(lastRelease, mutableState.value.checkedAt)
            } catch (_: Exception) {
                if (!force) {
                    mutableState.value = mutableState.value.copy(error = "Nie udało się zapisać czasu sprawdzenia. Spróbuj sprawdzić ręcznie.")
                    return@withContext
                }
            }
            mutableState.value = mutableState.value.copy(checking = true, error = null)
            try {
                val release = source.latest()
                lastRelease = release
                val checkedAt = clock()
                var next = UpdateState(available = release.takeIf { it.version > installed }, checkedAt = checkedAt)
                try {
                    persist(release, checkedAt)
                } catch (_: Exception) {
                    next = next.copy(error = "Sprawdzono aktualizacje, ale nie udało się zapisać wyniku na telefonie.")
                }
                mutableState.value = next
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) {
                mutableState.value = mutableState.value.copy(error = "Nie udało się sprawdzić aktualizacji. Spróbuj ponownie później.")
            }
        } finally {
            mutableState.value = mutableState.value.copy(checking = false)
            lock.unlock()
        }
    }

    private fun loadOnce() {
        if (loaded) return
        loaded = true
        runCatching {
            val json = JSONObject(cache.readText())
            require(json.getInt("schema") == 1)
            val release = json.optString("version").takeIf { it.isNotBlank() }?.let { AppRelease(AppVersion.parse(it)) }
            val checkedAt = json.optLong("checkedAt").coerceAtLeast(0L)
            lastRelease = release
            mutableState.value = UpdateState(release?.takeIf { it.version > installed }, checkedAt)
            lastAttempt = json.optLong("lastAttempt", checkedAt).coerceAtLeast(0L)
        }
    }

    private fun persist(release: AppRelease?, checkedAt: Long) {
        directory.mkdirs()
        val temporary = File(directory, "latest.tmp")
        temporary.writeText(JSONObject().put("schema", 1).put("version", release?.version?.toString())
            .put("checkedAt", checkedAt).put("lastAttempt", lastAttempt).toString())
        Files.move(temporary.toPath(), cache.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
    }
}
