package app.privateaudio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class Layer3UiContractTest {
    @Test
    fun mainConsumesOnlyServiceOwnedProductStateAndExistingPowerOperations() {
        assertTrue(mainSource.contains("connectedService?.privateAudioState"))
        assertTrue(mainSource.contains("state == PrivateAudioState.READY"))
        assertTrue(mainSource.contains("setAction(PrivateAudioService.ACTION_ARM)"))
        assertTrue(mainSource.contains("connectedService?.disarmAndStopStartedLifetime()"))
        assertFalse(mainSource.contains("connectedService.observer"))
        assertFalse(mainSource.contains("MODE_IN_COMMUNICATION"))
        assertFalse(mainSource.contains("communicationDevice"))
        assertFalse(screenSource.contains("AudioDiagnosticObserver"))
        assertFalse(screenSource.contains("ExperimentState"))
    }

    @Test
    fun closeCleansUpBeforeRemovingTaskAndFutureControlsAddNoBehavior() {
        assertInOrder(
            mainSource,
            "onCloseClick = {",
            "connectedService?.disarmAndStopStartedLifetime()",
            "finishAndRemoveTask()",
        )
        assertFalse(screenSource.contains("SYSTEM_ALERT_WINDOW"))
        assertFalse(screenSource.contains("ModalBottomSheet("))
    }

    @Test
    fun protectedRoutingAndDiagnosticsRemainSingleAndPresent() {
        assertEquals(1, productionSources.sumOf { it.readText().occurrences("setCommunicationDevice(") })
        assertTrue(diagnosticScreen.isFile)
        assertTrue(observerSource.contains("internal fun buildDiagnosticReport("))
        assertTrue(serviceSource.contains("fun diagnosticReport(): String"))
        assertTrue(serviceSource.contains("return observer.report()"))
    }

    private fun assertInOrder(source: String, vararg fragments: String) {
        var previous = -1
        fragments.forEach { fragment ->
            val current = source.indexOf(fragment, previous + 1)
            assertTrue("Missing or out-of-order fragment: $fragment", current > previous)
            previous = current
        }
    }

    private fun String.occurrences(needle: String): Int = windowed(needle.length).count { it == needle }

    private companion object {
        val projectRoot = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
            .first { File(it, "app/src/main").isDirectory }
        val mainSource = projectFile("app/src/main/java/app/privateaudio/MainActivity.kt").readText()
        val screenSource = projectFile("app/src/main/java/app/privateaudio/ui/PrivateAudioScreen.kt").readText()
        val serviceSource = projectFile("app/src/main/java/app/privateaudio/PrivateAudioService.kt").readText()
        val observerSource = projectFile(
            "app/src/main/java/app/privateaudio/diagnostic/AudioDiagnosticObserver.kt",
        ).readText()
        val diagnosticScreen = projectFile("app/src/main/java/app/privateaudio/ui/DiagnosticScreen.kt")
        val productionSources = File(projectRoot, "app/src/main/java")
            .walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .toList()

        fun projectFile(relativePath: String) = File(projectRoot, relativePath)
    }
}
