package app.privateaudio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AssistantAutomaticRoutingContractTest {
    @Test
    fun assistantRoutingHasNoPreferenceOrAdvancedSetting() {
        assertFalse(service.contains("assistant_routing_experimental_enabled"))
        assertFalse(service.contains("AssistantRoutingExperimental"))
        assertFalse(settings.contains("settings_gemini_routing_experimental"))
        assertFalse(defaultStrings.contains("settings_gemini_routing_experimental"))
    }

    @Test
    fun triggerClassesRemainSeparateAndProviderIndependent() {
        val trigger = observer.method("private fun evaluateExperimentTrigger()")
        assertTrue(trigger.contains("val normalTrigger = mode == AudioManager.MODE_IN_COMMUNICATION"))
        assertTrue(trigger.contains("qualifyingPlaybackCount(configs) >"))
        assertTrue(trigger.contains("TYPE_BUILTIN_SPEAKER"))
        assertTrue(trigger.contains("val assistantTrigger = assistantCount > 0"))
        val assistantClassifier = observer.method("private fun assistantQualifyingPlaybackCount(")
        assertTrue(assistantClassifier.contains("USAGE_ASSISTANT"))
        assertTrue(assistantClassifier.contains("CONTENT_TYPE_SPEECH"))
        assertFalse(assistantClassifier.contains("USAGE_MEDIA"))
        assertFalse(assistantClassifier.contains("CONTENT_TYPE_SONIFICATION"))
        assertFalse(trigger.contains("packageName"))
        assertEquals(
            listOf(observerFile),
            productionSources.kotlinMemberCallSites("setCommunicationDevice").map(KotlinCallSite::file),
        )
    }

    @Test
    fun bothTriggersUseTheSameProtectedPoc5BodyAndCycleSpecificCleanup() {
        val trigger = observer.method("private fun evaluateExperimentTrigger()")
        assertTrue(trigger.contains("TriggerOrigin.ASSISTANT"))
        assertTrue(trigger.contains("TriggerOrigin.COMMUNICATION"))
        assertEquals(1, trigger.occurrences("startProtectedPoc5Probe("))
        val body = observer.method("private fun startProtectedPoc5Probe(")
        assertInOrder(body, "collectSnapshot()", "startSilentCommunicationTrack()", "PLAYSTATE_PLAYING", "requestCommunicationMode()", "performRoutingAttempt(earpiece")
        assertTrue(observer.method("private fun performRoutingAttempt(").contains("experiment.attempts.isNotEmpty()"))
        assertTrue(observer.method("private fun handlePlaybackConfigurations(").contains("TriggerOrigin.ASSISTANT"))
        assertTrue(observer.method("private fun scheduleEndConfirmation(").contains("assistantQualifyingPlaybackCount"))
    }

    @Test
    fun diagnosticsExposeOriginWithoutChangingProximity() {
        listOf(
            "Assistant qualifying playback count:",
            "Trigger origin:",
            "Mode immediately before assistant participation:",
            "Final reported communication device:",
            "Cleanup result:",
        ).forEach { assertTrue(observer.contains(it)) }
        assertFalse(source("app/src/main/java/app/privateaudio/ProximityScreenController.kt").contains("TriggerOrigin"))
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
        val observerFile = File(root, "app/src/main/java/app/privateaudio/diagnostic/AudioDiagnosticObserver.kt")
        val observer = observerFile.readText()
        val defaultStrings = source("app/src/main/res/values/strings.xml")
        val productionSources = File(root, "app/src/main/java").walkTopDown()
            .filter { it.isFile && it.extension == "kt" }.toList()
    }
}
