package app.privateaudio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AssistantSessionLingerContractTest {
    @Test
    fun assistantEndUsesTwoSeparateBoundedStages() {
        assertTrue(observer.contains("private const val END_CONFIRMATION_DELAY_MS = 1_500L"))
        assertTrue(observer.contains("private const val ASSISTANT_SESSION_LINGER_MS = 7_000L"))
        val confirmation = observer.method("private fun scheduleEndConfirmation(")
        assertInOrder(
            confirmation,
            "postDelayed(runnable, END_CONFIRMATION_DELAY_MS)",
        )
        assertTrue(confirmation.contains("experiment.triggerOrigin == TriggerOrigin.ASSISTANT"))
        assertTrue(confirmation.contains("startAssistantSessionLinger(generation)"))
        assertInOrder(confirmation, "else {", "clearExperiment", "returnToWaiting()")
        val linger = observer.method("private fun startAssistantSessionLinger(")
        assertTrue(linger.contains("postDelayed(runnable, ASSISTANT_SESSION_LINGER_MS)"))
        assertInOrder(linger, "Protected session linger expired", "clearExperiment", "returnToWaiting()")
        assertEquals(1, linger.occurrences("clearExperiment("))
        assertEquals(1, linger.occurrences("returnToWaiting()"))
    }

    @Test
    fun resumedAssistantReusesTheSameProtectedCycle() {
        val callback = observer.method("private fun handlePlaybackConfigurations(")
        assertInOrder(
            callback,
            "externalContributionEstablished = true",
            "cancelPendingEndConfirmation()",
            "resumeAssistantDuringLinger()",
        )
        val resume = observer.method("private fun resumeAssistantDuringLinger(")
        assertInOrder(
            resume,
            "ASSISTANT/SPEECH resumed during linger",
            "cancelPendingSessionLinger()",
            "Protected context reused without new routing attempt",
        )
        assertFalse(resume.contains("startSilentCommunicationTrack"))
        assertFalse(resume.contains("requestCommunicationMode"))
        assertFalse(resume.contains("setCommunicationDevice"))
        assertFalse(resume.contains("cycleGeneration++"))
        assertFalse(resume.contains("returnToWaiting"))
        assertEquals(1, observer.occurrences("audioManager.setCommunicationDevice(earpiece)"))
    }

    @Test
    fun lingerRetainsActiveResourcesAndASecondGapCanStartFreshLinger() {
        val linger = observer.method("private fun startAssistantSessionLinger(")
        assertFalse(linger.contains("clearCommunicationDevice"))
        assertFalse(linger.contains("MODE_NORMAL"))
        assertFalse(linger.contains("stopSilentCommunicationTrack"))
        assertTrue(observer.method("private fun protectedContextDescription(").contains("public state=ACTIVE"))
        assertTrue(observer.method("private fun handlePlaybackConfigurations(").contains("scheduleEndConfirmation()"))
        assertTrue(linger.contains("cancelPendingSessionLinger()"))
        assertTrue(linger.contains("val token = ++lingerGeneration"))
    }

    @Test
    fun cleanupOverridesAndStaleWorkAreSafe() {
        val invalidate = observer.method("private fun invalidatePendingControllerWork(")
        assertInOrder(invalidate, "cycleGeneration++", "cancelPendingEndConfirmation()", "cancelPendingSessionLinger()")
        val linger = observer.method("private fun startAssistantSessionLinger(")
        listOf("token != lingerGeneration", "generation != cycleGeneration", "!controllerEnabled").forEach {
            assertTrue(linger.contains(it))
        }
        val abort = observer.method("private fun abortAssistantLingerIfContextLost(")
        listOf(
            "isTelephonyOrSystemPriorityMode",
            "silent track failure",
            "required earpiece unavailable",
            "communication mode ownership lost",
            "protected earpiece route lost",
        ).forEach { assertTrue(abort.contains(it)) }
        val cleanup = observer.method("private fun clearExperiment(")
        assertInOrder(cleanup, "cancelPendingEndConfirmation()", "cancelPendingSessionLinger()", "clearCommunicationDevice")
        listOf("fun stop(", "fun disableController()").forEach { signature ->
            assertTrue(observer.method(signature).contains("invalidatePendingControllerWork()"))
        }
    }

    @Test
    fun nonAssistantOriginsAndClassifiersRemainUnchanged() {
        val confirmation = observer.method("private fun scheduleEndConfirmation(")
        assertInOrder(confirmation, "TriggerOrigin.ASSISTANT", "else {", "clearExperiment", "returnToWaiting()")
        assertTrue(confirmation.contains("TriggerOrigin.BROWSER_COMMUNICATION"))
        assertTrue(confirmation.contains("qualifyingPlaybackCount(audioManager.activePlaybackConfigurations) > 1"))
        val assistant = observer.method("private fun assistantQualifyingPlaybackCount(")
        assertTrue(assistant.contains("USAGE_ASSISTANT"))
        assertTrue(assistant.contains("CONTENT_TYPE_SPEECH"))
        assertFalse(assistant.contains("CONTENT_TYPE_SONIFICATION"))
        assertFalse(assistant.contains("USAGE_MEDIA"))
        assertFalse(observer.method("private fun evaluateExperimentTrigger(").contains("ASSISTANT_SESSION_LINGER_MS"))
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
        val observer = File(
            root,
            "app/src/main/java/app/privateaudio/diagnostic/AudioDiagnosticObserver.kt",
        ).readText()
    }
}
