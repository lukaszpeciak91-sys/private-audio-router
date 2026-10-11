package app.privateaudio

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseFreezeWarningHygieneContractTest {
    @Test
    fun productionWarningCompatibilityPathsRemainExplicit() {
        assertTrue(overlaySource.contains("Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU"))
        assertTrue(overlaySource.contains("getParcelableExtra(EXTRA_SHOW_RESULT, ResultReceiver::class.java)"))
        assertTrue(overlaySource.contains("@Suppress(\"DEPRECATION\")\n    private fun Intent.legacyShowResultReceiver()"))

        assertTrue(screenSource.contains("@param:StringRes val label: Int"))

        assertTrue(observerSource.contains("Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA"))
        assertTrue(observerSource.contains("threadId()"))
        assertTrue(observerSource.contains("@Suppress(\"DEPRECATION\")\nprivate fun Thread.legacyThreadId(): Long = id"))
        assertTrue(observerSource.contains("device = observedPlaybackDevice()?.toObservedDevice()"))
        assertTrue(observerSource.contains("@Suppress(\"DEPRECATION\")\nprivate fun AudioPlaybackConfiguration.observedPlaybackDevice(): AudioDeviceInfo? = audioDeviceInfo"))
    }

    private companion object {
        val projectRoot = generateSequence(
            File(requireNotNull(System.getProperty("user.dir"))).absoluteFile,
        ) { it.parentFile }
            .first { File(it, "app/src/main").isDirectory }
        fun source(relativePath: String) = File(projectRoot, relativePath).readText()

        val overlaySource = source("app/src/main/java/app/privateaudio/overlay/OverlayService.kt")
        val screenSource = source("app/src/main/java/app/privateaudio/ui/PrivateAudioScreen.kt")
        val observerSource = source("app/src/main/java/app/privateaudio/diagnostic/AudioDiagnosticObserver.kt")
    }
}
