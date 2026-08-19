package app.privateaudio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AssistantExperimentalRoutingContractTest {
    @Test
    fun defaultOffPreferenceIsServiceOwnedAndDisablesImmediately() {
        assertTrue(service.contains("var isAssistantRoutingExperimentalEnabled by mutableStateOf(false)"))
        assertTrue(service.contains(".getBoolean(ASSISTANT_ROUTING_EXPERIMENTAL_KEY, false)"))
        val setter = observer.method("fun updateAssistantExperimentalEnabled(")
        assertInOrder(setter, "assistantExperimentalEnabled = enabled", "if (!enabled", "clearExperiment(", "returnToWaiting()")
        assertFalse(settings.contains("SharedPreferences"))
    }

    @Test
    fun assistantPathIsSeparateAndProviderIndependent() {
        val trigger = observer.method("private fun evaluateExperimentTrigger()")
        assertTrue(trigger.contains("val normalTrigger = mode == AudioManager.MODE_IN_COMMUNICATION"))
        assertTrue(trigger.contains("qualifyingPlaybackCount(configs) > 0"))
        assertTrue(trigger.contains("TYPE_BUILTIN_SPEAKER"))
        assertTrue(trigger.contains("val assistantTrigger = assistantExperimentalEnabled && assistantCount > 0"))
        assertFalse(observer.method("private fun assistantQualifyingPlaybackCount(").contains("USAGE_MEDIA"))
        assertTrue(observer.method("private fun assistantQualifyingPlaybackCount(").contains("USAGE_ASSISTANT"))
        assertFalse(trigger.contains("packageName"))
        assertEquals(1, productionSources.sumOf { it.readText().occurrences("setCommunicationDevice(") })
    }

    @Test
    fun bothTriggersUseTheSameProtectedPoc5BodyAndCycleSpecificCleanup() {
        val body = observer.method("private fun startProtectedPoc5Probe(")
        assertInOrder(body, "collectSnapshot()", "startSilentCommunicationTrack()", "PLAYSTATE_PLAYING", "audioManager.mode = AudioManager.MODE_IN_COMMUNICATION", "performRoutingAttempt(earpiece")
        assertTrue(observer.method("private fun performRoutingAttempt(").contains("experiment.attempts.isNotEmpty()"))
        assertTrue(observer.method("private fun handlePlaybackConfigurations(").contains("experiment.assistantExperimentalTriggerFired"))
        assertTrue(observer.method("private fun scheduleEndConfirmation(").contains("assistantQualifyingPlaybackCount"))
    }

    @Test
    fun advancedUiAndDiagnosticsExposeTheProbeWithoutChangingProximity() {
        assertInOrder(settings, "settings_proximity_screen", "settings_gemini_routing_experimental")
        assertTrue(settings.contains("role = Role.Switch"))
        assertTrue(observer.contains("Experimental assistant speech detected — starting protected POC-5 probe"))
        listOf(
            "Gemini/assistant experimental routing enabled:",
            "Assistant qualifying playback count:",
            "Assistant experimental trigger fired:",
            "Mode immediately before experimental participation:",
            "Final reported communication device:",
            "Cleanup result:",
        ).forEach { assertTrue(observer.contains(it)) }
        assertFalse(source("app/src/main/java/app/privateaudio/ProximityScreenController.kt").contains("Gemini"))
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
            val current = source.indexOf(fragment)
            assertTrue("Missing or out of order: $fragment", current > previous)
            previous = current
        }
    }
    private companion object {
        val root = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
            .first { File(it, "app/src/main").isDirectory }
        fun source(path: String) = File(root, path).readText()
        val service = source("app/src/main/java/app/privateaudio/PrivateAudioService.kt")
        val settings = source("app/src/main/java/app/privateaudio/ui/SettingsSheet.kt")
        val observer = source("app/src/main/java/app/privateaudio/diagnostic/AudioDiagnosticObserver.kt")
        val productionSources = File(root, "app/src/main/java").walkTopDown()
            .filter { it.isFile && it.extension == "kt" }.toList()
    }
}
