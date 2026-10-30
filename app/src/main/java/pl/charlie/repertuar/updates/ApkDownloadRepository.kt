package pl.charlie.repertuar.updates

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject

enum class DownloadPhase { Idle, Downloading, Verifying, Ready, Failed }
data class ApkDownloadState(val release: AppRelease? = null, val phase: DownloadPhase = DownloadPhase.Idle,
    val bytes: Long = 0L, val error: String? = null) {
    val busy get() = phase == DownloadPhase.Downloading || phase == DownloadPhase.Verifying
}

fun interface ApkTransport { suspend fun open(release: AppRelease): InputStream }

/** Owns the complete transfer and its durable result. Android APK identity validation is an adapter. */
class ApkDownloadRepository(private val directory: File, private val installed: AppVersion,
    private val transport: ApkTransport, private val verifyIdentity: (File, AppRelease) -> Unit) {
    private val mutableState = MutableStateFlow(ApkDownloadState())
    val state = mutableState.asStateFlow()
    private val lock = Mutex()
    private var loaded = false
    private val record get() = File(directory, "download.json")
    private val apk get() = File(directory, "update.apk")
    private val partial get() = File(directory, "update.part.apk")

    suspend fun load() = withContext(Dispatchers.IO) { lock.withLock { loadOnce() } }

    suspend fun download(release: AppRelease) = withContext(Dispatchers.IO) {
        if (!lock.tryLock()) return@withContext
        try {
            loadOnce()
            require(release.version > installed) { "Ta aktualizacja jest już zainstalowana." }
            require(release.size in 1..MAX_APK_BYTES && release.sha256?.matches(Regex("[a-f0-9]{64}")) == true) {
                "Brakuje danych do sprawdzenia pliku. Sprawdź aktualizacje ponownie."
            }
            directory.mkdirs()
            clearFiles()
            persist(release, ready = false)
            mutableState.value = ApkDownloadState(release, DownloadPhase.Downloading)
            transport.open(release).use { input ->
                FileOutputStream(partial).use { output ->
                    val buffer = ByteArray(32 * 1024)
                    var bytes = 0L
                    while (true) {
                        currentCoroutineContext().ensureActive()
                        val count = input.read(buffer)
                        if (count < 0) break
                        bytes += count
                        require(bytes <= release.size) { "Plik aktualizacji ma nieprawidłowy rozmiar. Pobierz ponownie." }
                        output.write(buffer, 0, count)
                        mutableState.value = mutableState.value.copy(bytes = bytes)
                    }
                    output.fd.sync()
                }
            }
            currentCoroutineContext().ensureActive()
            mutableState.value = mutableState.value.copy(phase = DownloadPhase.Verifying)
            verify(partial, release)
            currentCoroutineContext().ensureActive()
            Files.move(partial.toPath(), apk.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
            persist(release, ready = true)
            mutableState.value = ApkDownloadState(release, DownloadPhase.Ready, release.size)
        } catch (cancelled: CancellationException) {
            clearFiles()
            mutableState.value = ApkDownloadState(release, DownloadPhase.Failed, error = "Anulowano pobieranie. Możesz pobrać ponownie.")
            throw cancelled
        } catch (error: Exception) {
            clearFiles()
            mutableState.value = ApkDownloadState(release, DownloadPhase.Failed, error = when (error) {
                is IllegalArgumentException, is SecurityException -> error.message
                else -> "Nie udało się pobrać aktualizacji. Sprawdź połączenie i wolne miejsce, a potem spróbuj ponownie."
            })
        } finally { lock.unlock() }
    }

    /** Revalidates the private file immediately before giving read access to Android's installer. */
    suspend fun preparedApk(): File? = withContext(Dispatchers.IO) { lock.withLock {
        loadOnce()
        val release = mutableState.value.release ?: return@withLock null
        if (mutableState.value.phase != DownloadPhase.Ready) return@withLock null
        try {
            verify(apk, release)
            apk
        } catch (_: Exception) {
            clearFiles()
            mutableState.value = ApkDownloadState(release, DownloadPhase.Failed,
                error = "Nie udało się zweryfikować pobranego pliku. Pobierz aktualizację ponownie.")
            null
        }
    } }

    fun installationError(message: String?) { mutableState.value = mutableState.value.copy(error = message) }

    private fun verify(file: File, release: AppRelease) {
        require(file.length() == release.size) { "Plik aktualizacji jest niekompletny. Pobierz ponownie." }
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(32 * 1024)
            while (true) { val n = input.read(buffer); if (n < 0) break; digest.update(buffer, 0, n) }
        }
        val actual = digest.digest().joinToString("") { "%02x".format(it) }
        require(actual == release.sha256) { "Plik aktualizacji jest uszkodzony. Pobierz ponownie." }
        verifyIdentity(file, release)
    }

    private fun loadOnce() {
        if (loaded) return
        loaded = true
        if (!record.exists()) return
        try {
            val json = JSONObject(record.readText())
            require(json.getInt("schema") == 1)
            val release = releaseFromJson(json)
            if (release.version <= installed) { clearFiles(); return }
            if (!json.getBoolean("ready")) {
                clearFiles()
                mutableState.value = ApkDownloadState(release, DownloadPhase.Failed,
                    error = "Poprzednie pobieranie zostało przerwane. Pobierz aktualizację ponownie.")
                return
            }
            verify(apk, release)
            mutableState.value = ApkDownloadState(release, DownloadPhase.Ready, release.size)
        } catch (_: Exception) {
            clearFiles()
            mutableState.value = ApkDownloadState(error = "Nie udało się odczytać pobranej aktualizacji. Pobierz ją ponownie.")
        }
    }

    private fun persist(release: AppRelease, ready: Boolean) {
        val temporary = File(directory, "download.tmp")
        FileOutputStream(temporary).use { output ->
            output.write(release.toJson().put("schema", 1).put("ready", ready).toString().toByteArray())
            output.fd.sync()
        }
        Files.move(temporary.toPath(), record.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
    }
    private fun clearFiles() { partial.delete(); apk.delete(); record.delete() }
    companion object { const val MAX_APK_BYTES = 64L * 1024 * 1024 }
}
