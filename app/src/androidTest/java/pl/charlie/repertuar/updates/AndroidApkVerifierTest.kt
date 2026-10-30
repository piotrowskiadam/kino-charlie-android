package pl.charlie.repertuar.updates

import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.Assert.*
import org.junit.Test
import pl.charlie.repertuar.BuildConfig

class AndroidApkVerifierTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val target = instrumentation.targetContext
    private val verifier = AndroidApkVerifier(target)
    private val release = AppRelease(AppVersion.parse(BuildConfig.VERSION_NAME))

    @Test fun foreignPackageIsRejectedEvenWhenItIsAValidApk() {
        reject(File(instrumentation.context.applicationInfo.sourceDir), "innej aplikacji")
    }
    @Test fun installedVersionCannotBeOfferedAsANewerPackage() {
        reject(File(target.applicationInfo.sourceDir), "nowszej wersji")
    }
    @Test fun nonApkContentIsRejected() {
        val file = File(target.cacheDir, "invalid-update.apk")
        try { file.writeText("not an APK"); reject(file, "nie jest poprawną") } finally { file.delete() }
    }
    private fun reject(file: File, expected: String) {
        try { verifier.verify(file, release); fail("APK must be rejected") }
        catch (error: Exception) { assertTrue(error.message.orEmpty(), error.message.orEmpty().contains(expected)) }
    }
}
