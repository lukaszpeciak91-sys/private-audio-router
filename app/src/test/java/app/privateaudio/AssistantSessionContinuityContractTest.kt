package app.privateaudio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AssistantSessionContinuityContractTest {
    @Test
    fun settingIsDefaultOffServiceOwnedAndPersisted() {
        assertTrue(service.contains("var isAssistantSessionContinuityEnabled by mutableStateOf(false)"))
        assertTrue(service.contains("getBoolean(ASSISTANT_SESSION_CONTINUITY_KEY, false)"))
        assertTrue(service.contains("putBoolean(ASSISTANT_SESSION_CONTINUITY_KEY, enabled)"))
        assertTrue(service.contains("observer.updateAssistantSessionContinuityEnabled(enabled)"))
        assertTrue(settings.contains("testTag(\"settings_assistant_session_continuity\")"))
        assertTrue(strings.contains("name=\"settings_assistant_session_continuity\" translatable=\"false\""))
        assertTrue(strings.contains("name=\"settings_assistant_session_continuity_description\" translatable=\"false\""))
    }

    @Test
    fun normalSevenSecondLingerIsUnchangedAndOffKeepsExistingCleanup() {
        assertTrue(observer.contains("private const val ASSISTANT_SESSION_LINGER_MS = 7_000L"))
        assertTrue(observer.contains("private const val ASSISTANT_SESSION_CONTINUITY_MS = 20_000L"))
        val linger = observer.kotlinDeclaration("private fun startAssistantSessionLinger(")
        assertInOrder(linger, "Protected session linger expired", "startAssistantSessionContinuity(generation)", "clearExperiment", "returnToWaiting()")
        val continuity = observer.kotlinDeclaration("private fun startAssistantSessionContinuity(")
        assertTrue(continuity.contains("!assistantSessionContinuity.featureEnabled"))
        assertTrue(continuity.contains("postDelayed(runnable, ASSISTANT_SESSION_CONTINUITY_MS)"))
    }

    @Test
    fun continuityRequiresSameHealthyPublicRecordingAndProtectedContext() {
        val matching = observer.kotlinDeclaration("private fun assistantContinuityRecordingMatches(")
        assertTrue(matching.contains("baseline.isNotEmpty()"))
        assertTrue(matching.contains("clientSilenced == true"))
        assertTrue(matching.contains("currentVoiceRecognitionConfigurations() == baseline"))
        val validation = observer.kotlinDeclaration("private fun assistantContinuityContextFailure(")
        listOf(
            "VOICE_RECOGNITION absent",
            "VOICE_RECOGNITION clientSilenced=true",
            "VOICE_RECOGNITION configuration/session changed during linger",
            "isTelephonyOrSystemPriorityMode",
            "silent track failure",
            "communication mode ownership lost",
            "protected earpiece route lost",
        ).forEach { assertTrue(it, validation.contains(it)) }
        val recordingCallback = observer.kotlinDeclaration("private fun handleRecordingConfigurations(")
        assertTrue(recordingCallback.contains("pendingAssistantSessionContinuity != null"))
        assertTrue(recordingCallback.contains("abortAssistantSessionContinuity"))
    }

    @Test
    fun resumeReusesCycleAndNeverRequestsRouteOrModeAgain() {
        val playback = observer.kotlinDeclaration("private fun handlePlaybackConfigurations(")
        assertTrue(playback.contains("pendingAssistantSessionContinuity != null"))
        val resume = observer.kotlinDeclaration("private fun resumeAssistantDuringLinger(")
        assertTrue(resume.contains("cancelPendingAssistantSessionContinuity(\"ASSISTANT/SPEECH resumed\")"))
        assertTrue(resume.contains("Protected context reused without new routing attempt"))
        listOf("setCommunicationDevice", "requestCommunicationMode", "audioManager.mode =", "cycleGeneration++", "startSilentCommunicationTrack")
            .forEach { assertFalse(it, resume.contains(it)) }
        assertEquals(1, observer.occurrences("audioManager.setCommunicationDevice(earpiece)"))
    }

    @Test
    fun timeoutAndAbortInvalidateStaleCallbacksAndCleanExactlyOnce() {
        val continuity = observer.kotlinDeclaration("private fun startAssistantSessionContinuity(")
        listOf("token != continuityGeneration", "generation != cycleGeneration", "!controllerEnabled")
            .forEach { assertTrue(it, continuity.contains(it)) }
        assertEquals(1, continuity.occurrences("clearExperiment("))
        assertEquals(1, continuity.occurrences("returnToWaiting()"))
        val cancellation = observer.kotlinDeclaration("private fun cancelPendingAssistantSessionContinuity(")
        assertTrue(cancellation.contains("continuityGeneration++"))
        val invalidation = observer.kotlinDeclaration("private fun invalidatePendingControllerWork(")
        assertTrue(invalidation.contains("cancelPendingAssistantSessionContinuity()"))
        val toggle = observer.kotlinDeclaration("fun updateAssistantSessionContinuityEnabled(")
        assertTrue(toggle.contains("abortAssistantSessionContinuity(\"experiment toggle OFF\")"))
    }

    @Test
    fun onlyAssistantGetsContinuityAndActiveStillDrivesProximity() {
        val confirmation = observer.kotlinDeclaration("private fun scheduleEndConfirmation(")
        assertTrue(confirmation.contains("experiment.triggerOrigin == TriggerOrigin.ASSISTANT"))
        assertInOrder(confirmation, "TriggerOrigin.ASSISTANT", "else {", "clearExperiment", "returnToWaiting()")
        val continuity = observer.kotlinDeclaration("private fun startAssistantSessionContinuity(")
        assertFalse(continuity.contains("TriggerOrigin.COMMUNICATION"))
        assertFalse(continuity.contains("TriggerOrigin.BROWSER_COMMUNICATION"))
        assertFalse(continuity.contains("PrivateAudioState"))
        assertTrue(observer.kotlinDeclaration("private fun protectedContextDescription(").contains("public state=ACTIVE"))
        assertFalse(service.contains("assistantSessionContinuity.active &&"))
    }

    private fun String.occurrences(needle: String) = windowed(needle.length).count { it == needle }

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
        val observer = File(root, "app/src/main/java/app/privateaudio/diagnostic/AudioDiagnosticObserver.kt").readText()
        val service = File(root, "app/src/main/java/app/privateaudio/PrivateAudioService.kt").readText()
        val settings = File(root, "app/src/main/java/app/privateaudio/ui/SettingsSheet.kt").readText()
        val strings = File(root, "app/src/main/res/values/strings.xml").readText()
    }
}
