package app.privateaudio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AssistantEarlyRouteContractTest {
    @Test fun newPreferenceDefaultsOffAndDoesNotReadStaleFakePhoneKey() {
        assertTrue(service.contains(".getBoolean(ASSISTANT_EARLY_ROUTE_KEY, false)"))
        assertTrue(service.contains("\"assistant_early_route_enabled\""))
        assertFalse(service.contains("fake_phone_pre_arm_enabled"))
    }

    @Test fun qualificationRequiresVoiceRecognitionAndExactAssistantSonification() {
        val callback = observer.method("private fun handlePlaybackConfigurations(")
        assertTrue(callback.contains("USAGE_ASSISTANT"))
        assertTrue(callback.contains("CONTENT_TYPE_SONIFICATION"))
        assertTrue(callback.contains("voiceRecognitionPresent()"))
        assertTrue(observer.method("private fun voiceRecognitionPresent()").contains("VOICE_RECOGNITION"))
        assertFalse(observer.method("private fun assistantQualifyingPlaybackCount(").contains("SONIFICATION"))
    }

    @Test fun earlyPhaseStartsTrackThenModeAndLeavesProtectedStateUntouched() {
        val start = observer.method("private fun startAssistantEarlyPreArm(")
        assertTrue(start.contains("startSilentCommunicationTrack()"))
        assertInOrder(start, "startSilentCommunicationTrack()", "PLAYSTATE_PLAYING", "requestCommunicationMode()")
        assertEquals(1, start.occurrences("requestCommunicationMode()"))
        listOf("requestCommunicationDevice", "clearCommunicationDevice", "ExperimentState.REQUEST_ATTEMPTED").forEach {
            assertFalse("Early start contains $it", start.contains(it))
        }
        assertFalse(start.contains("proximity"))
        assertTrue(start.contains("audioManager.mode != AudioManager.MODE_NORMAL"))
    }

    @Test fun healthyAssistantPromotionDoesNotPlayAgainAndProtectedOrderIsPreserved() {
        val promote = observer.method("private fun promoteAssistantEarlyPreArm(")
        assertFalse(promote.contains("startSilentCommunicationTrack"))
        assertFalse(promote.contains("play()"))
        assertFalse(promote.contains("requestCommunicationMode"))
        val probe = observer.method("private fun startProtectedPoc5Probe(")
        assertTrue(probe.contains("if (!reuseEarlyTrack) {"))
        assertInOrder(probe, "if (reuseEarlyTrack) promoteAssistantEarlyPreArm()", "if (!reuseEarlyTrack) {", "performRoutingAttempt(earpiece")
        assertEquals(1, observer.method("private fun requestCommunicationDevice(").occurrences("setCommunicationDevice(earpiece)"))
    }

    @Test fun modeNormalDuringCurrentRequestIsNotOwnershipLossAndTrackStaysAlive() {
        val abort = observer.method("private fun abortAssistantEarlyPreArmIfContextLost(")
        assertTrue(abort.contains("AssistantEarlyRoutePhase.MODE_REQUEST_IN_FLIGHT"))
        assertTrue(abort.contains("Assistant early MODE_NORMAL ignored while request IN_FLIGHT"))
        assertInOrder(abort, "MODE_REQUEST_IN_FLIGHT", "audioManager.mode == AudioManager.MODE_NORMAL", "return")
        assertFalse(abort.substringBefore("MODE_REQUEST_IN_FLIGHT").contains("early communication mode ownership lost"))
        val start = observer.method("private fun startAssistantEarlyPreArm(")
        assertInOrder(start, "phase = AssistantEarlyRoutePhase.MODE_REQUEST_IN_FLIGHT", "requestCommunicationMode()")
        assertFalse(start.substringAfter("phase = AssistantEarlyRoutePhase.MODE_REQUEST_IN_FLIGHT")
            .substringBefore("requestCommunicationMode()").contains("stopSilentCommunicationTrack"))
    }

    @Test fun currentCompletionAloneBecomesModeReadyAndPromotionRequiresIt() {
        val start = observer.method("private fun startAssistantEarlyPreArm(")
        assertTrue(start.contains("generation == assistantEarlyRouteGeneration"))
        assertTrue(start.contains("assistantEarlyRoute.generation == generation"))
        assertInOrder(start, "generationStillCurrent", "if (!generationStillCurrent)", "MODE_READY")
        val healthy = observer.method("private fun isAssistantEarlyPreArmHealthy(")
        assertTrue(healthy.contains("AssistantEarlyRoutePhase.MODE_READY"))
        assertTrue(healthy.contains("AudioTrack.PLAYSTATE_PLAYING"))
    }

    @Test fun cancellationInvalidatesInflightGenerationAndStaleCompletionCannotTouchANewerOne() {
        val cleanup = observer.method("private fun cleanupAssistantEarlyPreArm(")
        assertInOrder(cleanup, "cancelledWhileModeInFlight", "invalidateAssistantEarlyRouteDelayedWork()", "stopSilentCommunicationTrack()")
        assertTrue(cleanup.contains("abortRequestedWhileModeInFlight = cancelledWhileModeInFlight"))
        assertTrue(cleanup.contains("modeParticipationActive && !cancelledWhileModeInFlight"))
        val reconcile = observer.method("private fun reconcileModeAfterStaleAssistantEarlyCompletion(")
        assertTrue(reconcile.contains("if (assistantEarlyRoute.generation == generation)"))
        assertTrue(reconcile.contains("!experiment.requestAttempted && !assistantEarlyRoute.active"))
        assertFalse(reconcile.contains("requestCommunicationDevice"))
        assertFalse(reconcile.contains("stopSilentCommunicationTrack"))
    }

    @Test fun communicationAndBrowserCancelRatherThanPromoteEarlyPreArm() {
        val callback = observer.method("private fun handlePlaybackConfigurations(")
        assertTrue(callback.contains("qualifyingPlaybackCount(configs) >= 2 || browserCount > 0"))
        assertTrue(callback.contains("cleanupAssistantEarlyPreArm(\"incompatible COMMUNICATION/BROWSER session began\""))
        val probe = observer.method("private fun startProtectedPoc5Probe(")
        assertTrue(probe.contains("triggerOrigin == TriggerOrigin.ASSISTANT && isAssistantEarlyPreArmHealthy()"))
    }

    @Test fun cleanupIsBoundedGenerationSafeAndRelinquishesOnlyOwnedMode() {
        assertTrue(observer.contains("private const val ASSISTANT_EARLY_ROUTE_TIMEOUT_MS = 10_000L"))
        val timeout = observer.method("private fun scheduleAssistantEarlyRouteTimeout(")
        assertTrue(timeout.contains("generation != assistantEarlyRouteGeneration"))
        assertTrue(timeout.contains("postDelayed(runnable, ASSISTANT_EARLY_ROUTE_TIMEOUT_MS)"))
        val cleanup = observer.method("private fun cleanupAssistantEarlyPreArm(")
        assertTrue(cleanup.contains("stopSilentCommunicationTrack()"))
        assertTrue(cleanup.contains("prepareSilentCommunicationTrack()"))
        assertFalse(cleanup.contains("clearCommunicationDevice"))
        assertTrue(cleanup.contains("if (modeParticipationActive)"))
        assertTrue(cleanup.contains("audioManager.mode = AudioManager.MODE_NORMAL"))
    }

    @Test fun allAbortInputsAndFailOpenFallbackRemainConnected() {
        assertTrue(observer.method("fun disableController()").contains("cleanupAssistantEarlyPreArm(\"Power OFF\")"))
        assertTrue(observer.method("fun stop(reason: String)").contains("cleanupAssistantEarlyPreArm(reason)"))
        assertTrue(observer.method("fun updateAssistantEarlyRouteEnabled(").contains("cleanupAssistantEarlyPreArm(\"Preference disabled\")"))
        assertTrue(observer.method("private fun handleRecordingConfigurations(").contains("VOICE_RECOGNITION disappeared, silenced, or changed"))
        assertTrue(observer.method("private fun abortAssistantEarlyPreArmIfContextLost(").contains("system/telephony-priority"))
        assertTrue(observer.method("private fun startProtectedPoc5Probe(").contains("!reuseEarlyTrack && !startSilentCommunicationTrack()"))
    }

    @Test fun publicStateProximityAndAssistantLingerContractsRemainUnchanged() {
        assertFalse(service.contains("assistantEarlyRoute.active"))
        assertTrue(observer.contains("private const val ASSISTANT_SESSION_LINGER_MS = 7_000L"))
        assertTrue(observer.method("private fun startAssistantSessionLinger(").contains("ASSISTANT_SESSION_LINGER_MS"))
        assertFalse(observer.method("private fun startAssistantEarlyPreArm(").contains("onEvidenceChanged"))
        assertFalse(observer.method("private fun startAssistantEarlyPreArm(").contains("proximity"))
    }

    @Test fun noCapturePermissionOrAudioCaptureWasAdded() {
        assertFalse(manifest.contains("RECORD_AUDIO"))
        assertFalse(productionSources.any { it.readText().contains("AudioRecord(") })
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
        fragments.forEach { fragment -> previous = source.indexOf(fragment).also { assertTrue("Missing/out of order: $fragment", it > previous) } }
    }
    private companion object {
        val root = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }.first { File(it, "app/src/main").isDirectory }
        fun source(path: String) = File(root, path).readText()
        val service = source("app/src/main/java/app/privateaudio/PrivateAudioService.kt")
        val observer = source("app/src/main/java/app/privateaudio/diagnostic/AudioDiagnosticObserver.kt")
        val manifest = source("app/src/main/AndroidManifest.xml")
        val productionSources = File(root, "app/src/main/java").walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()
    }
}
