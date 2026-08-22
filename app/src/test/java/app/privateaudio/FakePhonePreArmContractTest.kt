package app.privateaudio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class FakePhonePreArmContractTest {
    @Test fun persistedPreferenceAndOffPathRemainUnchanged() {
        assertTrue(service.contains(".getBoolean(FAKE_PHONE_PRE_ARM_KEY, false)"))
        assertTrue(service.contains(".putBoolean(FAKE_PHONE_PRE_ARM_KEY, enabled)"))
        assertFalse(observer.method("fun enableController()").contains("startFakePhoneMicroRoute"))
        assertFalse(observer.method("private fun returnToWaiting()").contains("startFakePhoneMicroRoute"))
        assertTrue(observer.method("private fun evaluateExperimentTrigger()").contains("startProtectedPoc5Probe"))
    }

    @Test fun onlyExactAssistantSonificationStartsOneBoundedWaitingMicroRoute() {
        val callback = observer.method("private fun handlePlaybackConfigurations(")
        assertTrue(callback.contains("USAGE_ASSISTANT"))
        assertTrue(callback.contains("CONTENT_TYPE_SONIFICATION"))
        assertTrue(callback.contains("sonificationJustAppeared"))
        assertFalse(observer.method("private fun assistantQualifyingPlaybackCount(").contains("SONIFICATION"))
        assertFalse(observer.method("private fun startFakePhoneMicroRoute(").contains("USAGE_MEDIA"))
        assertTrue(observer.method("private fun startFakePhoneMicroRoute(").contains("public state remains WAITING"))
    }

    @Test fun setupOrderIsTrackThenModeThenExactlyOneDeviceRequest() {
        val start = observer.method("private fun startFakePhoneMicroRoute(")
        assertInOrder(start, "startSilentCommunicationTrack()", "requestCommunicationMode()", "requestCommunicationDevice(earpiece)")
        assertTrue(start.contains("TYPE_BUILTIN_EARPIECE"))
        assertEquals(1, productionSources.sumOf { it.readText().occurrences("setCommunicationDevice(") })
        assertFalse(start.contains("requestAudioFocus"))
    }

    @Test fun disappearanceGraceCanBeCancelledAndHardCapCannotBeExtended() {
        assertTrue(observer.contains("private const val FAKE_PHONE_DISAPPEARANCE_GRACE_MS = 100L"))
        assertTrue(observer.contains("private const val FAKE_PHONE_HARD_CAP_MS = 2_000L"))
        val disappear = observer.method("private fun scheduleFakePhoneDisappearanceCleanup(")
        assertTrue(disappear.contains("postDelayed(runnable, FAKE_PHONE_DISAPPEARANCE_GRACE_MS)"))
        val cancel = observer.method("private fun cancelFakePhoneDisappearanceCleanup(")
        assertTrue(cancel.contains("removeCallbacks"))
        val hard = observer.method("private fun scheduleFakePhoneHardTimeout(")
        assertTrue(hard.contains("generation != fakePhoneGeneration"))
        assertTrue(hard.contains("postDelayed(runnable, FAKE_PHONE_HARD_CAP_MS)"))
        assertEquals(1, observer.occurrences("scheduleFakePhoneHardTimeout(generation)"))
        assertFalse(callback().substringAfter("fakePhonePreArm.active && sonification != null").substringBefore("when {").contains("scheduleFakePhoneHardTimeout"))
    }

    @Test fun healthyPromotionTransfersResourcesWithoutDuplicateSetup() {
        val promote = observer.method("private fun promoteFakePhonePreArm(")
        listOf("startSilentCommunicationTrack", "requestCommunicationMode", "requestCommunicationDevice", "clearCommunicationDevice", "MODE_NORMAL").forEach {
            assertFalse(promote.contains(it))
        }
        assertTrue(promote.contains("invalidateFakePhoneDelayedWork()"))
        assertTrue(promote.contains("attempts = listOf(attempt)"))
        assertTrue(promote.contains("silent track, mode participation, and earpiece request reused"))
    }

    @Test fun unhealthyHandoffFailsOpenBeforeOrdinaryPoc5Setup() {
        val probe = observer.method("private fun startProtectedPoc5Probe(")
        assertInOrder(probe, "!isFakePhoneMicroRouteHealthy()", "cleanupFakePhonePreArm", "experiment = experiment.copy", "startSilentCommunicationTrack()")
        assertTrue(probe.contains("reEvaluatePlayback = false"))
        assertFalse(observer.method("private fun cleanupFakePhonePreArm(").contains("ExperimentState.BLOCKED"))
    }

    @Test fun cleanupIsGenerationSafeOrderedImmediateAndReevaluatesNormalClassifiers() {
        val cleanup = observer.method("private fun cleanupFakePhonePreArm(")
        assertInOrder(cleanup, "invalidateFakePhoneDelayedWork()", "clearCommunicationDevice", "MODE_NORMAL", "stopSilentCommunicationTrack")
        assertTrue(cleanup.contains("handlePlaybackConfigurations"))
        val safety = observer.method("private fun abortFakePhoneMicroRouteIfContextLost(")
        listOf("system/telephony-priority", "target earpiece unavailable", "silent-track failure", "unrecoverable mode loss", "unrecoverable route/device loss").forEach {
            assertTrue(safety.contains(it))
        }
        assertTrue(observer.method("fun disableController()").contains("cleanupFakePhonePreArm(\"Power OFF\")"))
        assertTrue(observer.method("fun stop(").contains("cleanupFakePhonePreArm(reason)"))
        assertTrue(observer.method("fun updateFakePhonePreArmEnabled(").contains("cleanupFakePhonePreArm(\"Preference disabled\")"))
    }

    @Test fun existingClassifiersAndAssistantLingerRemainIsolated() {
        val trigger = observer.method("private fun evaluateExperimentTrigger()")
        listOf("TriggerOrigin.COMMUNICATION", "TriggerOrigin.ASSISTANT", "TriggerOrigin.BROWSER_COMMUNICATION").forEach { assertTrue(trigger.contains(it)) }
        assertTrue(observer.method("private fun startAssistantSessionLinger(").contains("ASSISTANT_SESSION_LINGER_MS"))
        assertFalse(observer.method("private fun startAssistantSessionLinger(").contains("Fake Phone"))
        assertTrue(observer.contains("Audible result: Requires physical confirmation"))
        assertFalse(observer.contains("ChatGPT ping"))
        assertFalse(observer.contains("Gemini ping"))
    }

    private fun callback() = observer.method("private fun handlePlaybackConfigurations(")
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
        fragments.forEach { fragment -> previous = source.indexOf(fragment).also { assertTrue("Missing/out of order: $fragment", it > previous) } }
    }
    private companion object {
        val root = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
            .first { File(it, "app/src/main").isDirectory }
        fun source(path: String) = File(root, path).readText()
        val service = source("app/src/main/java/app/privateaudio/PrivateAudioService.kt")
        val observer = source("app/src/main/java/app/privateaudio/diagnostic/AudioDiagnosticObserver.kt")
        val productionSources = File(root, "app/src/main/java").walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()
    }
}
