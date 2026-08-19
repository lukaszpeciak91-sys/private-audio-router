package app.privateaudio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class BrowserCommunicationRoutingContractTest {
    @Test
    fun browserTriggerIsExactAutomaticAndIndependent() {
        val trigger = observer.method("private fun evaluateExperimentTrigger()")
        val classifier = observer.method("private fun browserQualifyingPlaybackCount(")
        assertFalse(service.contains("browser_routing_experimental_enabled"))
        assertFalse(service.contains("isBrowserRoutingEnabled"))
        assertFalse(settings.contains("settings_browser_routing_experimental"))
        assertFalse(defaultStrings.contains("settings_browser_routing_experimental"))
        assertTrue(trigger.contains("val browserTrigger = mode == AudioManager.MODE_IN_COMMUNICATION"))
        assertTrue(trigger.contains("mode == AudioManager.MODE_IN_COMMUNICATION"))
        assertTrue(trigger.contains("TYPE_BUILTIN_SPEAKER"))
        assertTrue(classifier.contains("USAGE_VOICE_COMMUNICATION"))
        assertTrue(classifier.contains("CONTENT_TYPE_UNKNOWN"))
        assertFalse(classifier.contains("USAGE_MEDIA"))
        assertFalse(trigger.contains("packageName"))
    }

    @Test
    fun everyOriginSharesOneProtectedPoc5AndOneRequest() {
        val trigger = observer.method("private fun evaluateExperimentTrigger()")
        listOf("COMMUNICATION", "ASSISTANT", "BROWSER_COMMUNICATION").forEach {
            assertTrue(trigger.contains("TriggerOrigin.$it"))
        }
        assertEquals(1, trigger.occurrences("startProtectedPoc5Probe("))
        assertEquals(1, productionSources.sumOf { it.readText().occurrences("setCommunicationDevice(") })
        val probe = observer.method("private fun startProtectedPoc5Probe(")
        assertInOrder(
            probe,
            "collectSnapshot()",
            "startSilentCommunicationTrack()",
            "PLAYSTATE_PLAYING",
            "audioManager.mode = AudioManager.MODE_IN_COMMUNICATION",
            "performRoutingAttempt(earpiece",
        )
    }

    @Test
    fun browserLifecycleUsesUnknownContributionWithoutChangingStandardCounts() {
        val callback = observer.method("private fun handlePlaybackConfigurations(")
        val confirmation = observer.method("private fun scheduleEndConfirmation(")
        assertTrue(callback.contains("TriggerOrigin.BROWSER_COMMUNICATION -> browserCount > 0"))
        assertTrue(confirmation.contains("browserQualifyingPlaybackCount(audioManager.activePlaybackConfigurations) > 0"))
        assertTrue(confirmation.contains("END_CONFIRMATION_DELAY_MS"))
        assertTrue(observer.contains("private const val END_CONFIRMATION_DELAY_MS = 1_500L"))
        assertTrue(observer.contains("Browser qualifying VOICE_COMMUNICATION/UNKNOWN count:"))
        assertEquals(1, observer.occurrences("private fun qualifyingPlaybackCount("))
    }

    private fun String.occurrences(needle: String) = windowed(needle.length).count { it == needle }
    private fun String.method(signature: String): String {
        val start = indexOf(signature).also { check(it >= 0) { "Missing $signature" } }
        val opening = indexOf('{', start)
        var depth = 0
        for (index in opening until length) when (this[index]) {
            '{' -> depth++
            '}' -> if (--depth == 0) return substring(start, index + 1)
        }
        error("Unterminated $signature")
    }
    private fun assertInOrder(source: String, vararg fragments: String) {
        var previous = -1
        fragments.forEach { fragment ->
            previous = source.indexOf(fragment).also { assertTrue("Missing $fragment", it > previous) }
        }
    }
    private companion object {
        val root = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
            .first { File(it, "app/src/main").isDirectory }
        fun source(path: String) = File(root, path).readText()
        val observer = source("app/src/main/java/app/privateaudio/diagnostic/AudioDiagnosticObserver.kt")
        val service = source("app/src/main/java/app/privateaudio/PrivateAudioService.kt")
        val settings = source("app/src/main/java/app/privateaudio/ui/SettingsSheet.kt")
        val defaultStrings = source("app/src/main/res/values/strings.xml")
        val productionSources = File(root, "app/src/main/java").walkTopDown()
            .filter { it.isFile && it.extension == "kt" }.toList()
    }
}
