package pl.charlie.repertuar.updates

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.IOException

class UpdateRepositoryTest {
    @get:Rule val temporary = TemporaryFolder()
    private val fixture get() = javaClass.getResource("/release-v0.1.3.json")!!.readText()

    @Test fun `published newer release exposes its exact official APK and release page`() = runTest {
        val repository = UpdateRepository(temporary.newFolder(), GitHubReleaseSource { fixture },
            AppVersion.parse("0.1.2"), { 100_000L })
        repository.check()
        assertEquals("0.1.3", repository.state.value.available?.version.toString())
        assertEquals("https://github.com/piotrowskiadam/kino-charlie-android/releases/download/v0.1.3/Charlie-MVP-0.1.3.apk",
            repository.state.value.available?.downloadUrl)
        assertEquals("https://github.com/piotrowskiadam/kino-charlie-android/releases/tag/v0.1.3",
            repository.state.value.available?.pageUrl)
        assertEquals(100_000L, repository.state.value.checkedAt)
    }

    @Test fun `known update survives reopening offline and failed manual check`() = runTest {
        val directory = temporary.newFolder()
        val first = UpdateRepository(directory, GitHubReleaseSource { fixture }, AppVersion.parse("0.1.2"), { 100_000L })
        first.check()
        val reopened = UpdateRepository(directory, GitHubReleaseSource { throw IOException("offline") },
            AppVersion.parse("0.1.2"), { 200_000L })
        reopened.load()
        assertEquals("0.1.3", reopened.state.value.available?.version.toString())
        reopened.check(force = true)
        assertEquals("0.1.3", reopened.state.value.available?.version.toString())
        assertEquals(100_000L, reopened.state.value.checkedAt)
        assertNotNull(reopened.state.value.error)
        assertFalse(reopened.state.value.checking)
    }

    @Test fun `versions compare numerically and never offer an equal or older APK`() = runTest {
        val tenth = UpdateRepository(temporary.newFolder(), GitHubReleaseSource { fixture.replace("0.1.3", "0.1.10") },
            AppVersion.parse("0.1.9"), { 100_000L })
        tenth.check()
        assertEquals("0.1.10", tenth.state.value.available?.version.toString())
        for (installed in listOf("0.1.3", "0.1.4", "1.0.0")) {
            val repository = UpdateRepository(temporary.newFolder(), GitHubReleaseSource { fixture },
                AppVersion.parse(installed), { 100_000L })
            repository.check()
            assertNull(repository.state.value.available)
            assertNull(repository.state.value.error)
            assertEquals(100_000L, repository.state.value.checkedAt)
        }
    }

    @Test fun `unpublished or foreign APK metadata cannot replace a known update`() = runTest {
        val invalid = listOf(
            fixture.replace("\"prerelease\":false", "\"prerelease\":true"),
            fixture.replace("\"draft\":false", "\"draft\":true"),
            fixture.replace("/releases/download/", "/other/download/"),
            fixture.replace("Charlie-MVP-0.1.3.apk", "wrong.apk"),
            fixture.replace("v0.1.3", "v0.1.3-beta"),
        )
        for (body in invalid) {
            val directory = temporary.newFolder()
            UpdateRepository(directory, GitHubReleaseSource { fixture }, AppVersion.parse("0.1.2"), { 100_000L }).check()
            val repository = UpdateRepository(directory, GitHubReleaseSource { body }, AppVersion.parse("0.1.2"), { 200_000L })
            repository.check(force = true)
            assertEquals("0.1.3", repository.state.value.available?.version.toString())
            assertNotNull(repository.state.value.error)
        }
    }

    @Test fun `automatic checks are limited to daily while manual checking can refresh sooner`() = runTest {
        var requests = 0
        var now = 100_000L
        val source = GitHubReleaseSource { requests++; fixture }
        val directory = temporary.newFolder()
        val first = UpdateRepository(directory, source, AppVersion.parse("0.1.2"), { now })
        first.check()
        first.check()
        val reopened = UpdateRepository(directory, source, AppVersion.parse("0.1.2"), { now })
        reopened.check()
        assertEquals(1, requests)
        reopened.check(force = true)
        assertEquals(2, requests)
        now += 25 * 60 * 60 * 1000L
        reopened.check()
        assertEquals(3, requests)
    }

    @Test fun `failed automatic attempt is still throttled after reopening without any successful cache`() = runTest {
        var requests = 0
        val directory = temporary.newFolder()
        val offline = GitHubReleaseSource { requests++; throw IOException("server unavailable") }
        UpdateRepository(directory, offline, AppVersion.parse("0.1.2"), { 100_000L }).check()
        val reopened = UpdateRepository(directory, offline, AppVersion.parse("0.1.2"), { 101_000L })
        reopened.check()
        assertEquals(1, requests)
        reopened.check(force = true)
        assertEquals(2, requests)
        assertNotNull(reopened.state.value.error)
    }
}
