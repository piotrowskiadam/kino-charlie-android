package pl.charlie.repertuar.updates

import java.io.ByteArrayInputStream
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ApkDownloadRepositoryTest {
    @get:Rule val temporary = TemporaryFolder()
    private val release = AppRelease(AppVersion.parse("0.1.6"), 3L,
        "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad")
    private val installed = AppVersion.parse("0.1.5")

    @Test fun `verified download is ready offline after reopening`() = runTest {
        val directory = temporary.newFolder()
        val repository = ApkDownloadRepository(directory, installed,
            ApkTransport { ByteArrayInputStream("abc".toByteArray()) }, { _, _ -> })
        repository.download(release)
        assertEquals(DownloadPhase.Ready, repository.state.value.phase)
        val reopened = ApkDownloadRepository(directory, installed, ApkTransport { error("offline") }, { _, _ -> })
        reopened.load()
        assertEquals(DownloadPhase.Ready, reopened.state.value.phase)
        assertEquals(release, reopened.state.value.release)
        assertEquals("abc", reopened.preparedApk()!!.readText())
    }
    @Test fun `wrong hash incomplete data and rejected APK identity never become installable`() = runTest {
        for ((data, rejectIdentity) in listOf("abd" to false, "ab" to false, "abcd" to false, "abc" to true)) {
            val repository = ApkDownloadRepository(temporary.newFolder(), installed,
                ApkTransport { ByteArrayInputStream(data.toByteArray()) }, { _, _ ->
                    if (rejectIdentity) throw SecurityException("Wrong package or signature")
                })
            repository.download(release)
            assertEquals(DownloadPhase.Failed, repository.state.value.phase)
            assertNotNull(repository.state.value.error)
            assertNull(repository.preparedApk())
        }
    }

    @Test fun `progress follows received bytes and network failure can be retried`() = runTest {
        var fail = true
        lateinit var repository: ApkDownloadRepository
        repository = ApkDownloadRepository(temporary.newFolder(), installed, ApkTransport {
            object : ByteArrayInputStream("abc".toByteArray()) {
                override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
                    if (pos == 1) {
                        assertEquals(1L, repository.state.value.bytes)
                        assertEquals(DownloadPhase.Downloading, repository.state.value.phase)
                        if (fail) throw java.io.IOException("connection lost")
                    }
                    return super.read(buffer, offset, minOf(1, length))
                }
            }
        }, { _, _ -> assertEquals(DownloadPhase.Verifying, repository.state.value.phase) })
        repository.download(release)
        assertEquals(DownloadPhase.Failed, repository.state.value.phase)
        assertNull(repository.preparedApk())
        fail = false
        repository.download(release)
        assertEquals(3L, repository.state.value.bytes)
        assertEquals(DownloadPhase.Ready, repository.state.value.phase)
    }

    @Test fun `cancelling a transfer never leaves an installable partial file`() = runTest {
        val entered = CompletableDeferred<Unit>()
        val repository = ApkDownloadRepository(temporary.newFolder(), installed, ApkTransport {
            entered.complete(Unit)
            awaitCancellation()
        }, { _, _ -> })
        val job = launch { repository.download(release) }
        entered.await()
        job.cancelAndJoin()
        assertEquals(DownloadPhase.Failed, repository.state.value.phase)
        assertNull(repository.preparedApk())
    }

    @Test fun `unfinished transfer is recovered as retryable after reopening`() = runTest {
        val directory = temporary.newFolder()
        val entered = CompletableDeferred<Unit>()
        val first = ApkDownloadRepository(directory, installed, ApkTransport {
            entered.complete(Unit)
            awaitCancellation()
        }, { _, _ -> })
        val job = launch { first.download(release) }
        entered.await()
        // Reopen the persisted public state before the old transfer can execute any cleanup.
        val reopened = ApkDownloadRepository(directory, installed,
            ApkTransport { ByteArrayInputStream("abc".toByteArray()) }, { _, _ -> })
        reopened.load()
        assertEquals(DownloadPhase.Failed, reopened.state.value.phase)
        assertNull(reopened.preparedApk())
        job.cancelAndJoin()
        reopened.download(release)
        assertEquals(DownloadPhase.Ready, reopened.state.value.phase)
    }

    @Test fun `missing integrity data and older releases are rejected before downloading`() = runTest {
        for (candidate in listOf(release.copy(sha256 = null), release.copy(size = 0),
            release.copy(size = 70_000_000), release.copy(version = installed))) {
            val repository = ApkDownloadRepository(temporary.newFolder(), installed,
                ApkTransport { throw AssertionError("must not request invalid release") }, { _, _ -> })
            repository.download(candidate)
            assertEquals(DownloadPhase.Failed, repository.state.value.phase)
            assertNull(repository.preparedApk())
        }
    }

}
