package app.privateaudio.diagnostic

import android.media.AudioDeviceInfo
import android.media.AudioManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AudioDiagnosticObserverTest {
    @Test
    fun knownModeAndDeviceTypesHaveReadableNames() {
        assertEquals("MODE_IN_COMMUNICATION", audioModeName(AudioManager.MODE_IN_COMMUNICATION))
        assertEquals("Built-in earpiece", audioDeviceTypeName(AudioDeviceInfo.TYPE_BUILTIN_EARPIECE))
    }

    @Test
    fun unknownPlatformValuesAreNotInferred() {
        assertEquals("Unknown value (999)", audioModeName(999))
        assertEquals("Unknown type (999)", audioDeviceTypeName(999))
    }

    @Test
    fun changeDescriptionReportsOnlyObservedDifference() {
        val previous = DiagnosticSnapshot("MODE_NORMAL", null, emptyList(), "Off (directly observed)")
        val current = previous.copy(mode = "MODE_IN_COMMUNICATION")

        assertTrue(describeChanges(previous, current).contains("MODE_NORMAL → MODE_IN_COMMUNICATION"))
    }

    @Test
    fun reportContainsCompleteStructuredEvidence() {
        val snapshot = DiagnosticSnapshot(
            mode = "MODE_IN_COMMUNICATION",
            communicationDevice = ObservedDevice(1, "Built-in earpiece", "Phone earpiece"),
            availableCommunicationDevices = listOf(
                ObservedDevice(1, "Built-in earpiece", "Phone earpiece"),
                ObservedDevice(2, "Built-in speaker", "Phone speaker"),
            ),
            speakerphoneState = "Off (directly observed)",
            activePlaybackConfigurations = listOf(
                ObservedPlayback(
                    usage = "USAGE_VOICE_COMMUNICATION",
                    contentType = "CONTENT_TYPE_SPEECH",
                    allowedCapturePolicy = "ALLOW_CAPTURE_BY_ALL",
                    device = ObservedDevice(2, "Built-in speaker", "Phone speaker"),
                ),
            ),
        )
        val report = buildDiagnosticReport(
            timestamp = "2026-08-12T12:34:56Z",
            experiment = EarpieceExperiment(
                state = ExperimentState.REQUEST_ATTEMPTED,
                armed = true,
                requestAttempted = true,
                selectedTarget = ObservedDevice(1, "Built-in earpiece", "Phone earpiece"),
                modeBeforeParticipation = "MODE_IN_COMMUNICATION",
                silentTrackCreated = true,
                silentTrackStarted = true,
                silentTrackSampleRate = 48_000,
                silentTrackBufferBytes = 1_024,
                silentTrackPlayState = "PLAYSTATE_PLAYING",
                activeVoiceCommunicationPlaybackObserved = true,
                silentTrackActiveBeforeModeRequest = true,
                modeRequestIssuedAfterPlaybackActive = true,
                explicitModeRequestInvoked = true,
                modeRequestTimestamp = "2026-08-12T12:34:56Z",
                modeRequestThread = "main (id=1)",
                modeImmediatelyBeforeRequest = "MODE_IN_COMMUNICATION",
                modeImmediatelyAfterRequest = "MODE_IN_COMMUNICATION",
                modeInCommunicationObserved = true,
                earpieceRequestAfterExplicitModeRequest = true,
                requestAccepted = true,
                attempts = listOf(
                    RoutingAttempt(
                        1, "12:34:56.000", "initial qualifying state observed",
                        "MODE_IN_COMMUNICATION", ObservedDevice(2, "Built-in speaker", "Phone speaker"),
                        true, ObservedDevice(1, "Built-in earpiece", "Phone earpiece"),
                        "Off (directly observed)",
                    ),
                ),
                earpieceReportedDuringSession = true,
                shortObservation = snapshot,
            ),
            snapshot = snapshot,
            events = listOf("12:34:55.000  Baseline — state recorded", "12:34:56.000  Manual snapshot"),
        )

        assertTrue(report.contains("Timestamp: 2026-08-12T12:34:56Z"))
        assertTrue(report.contains("Experiment state: REQUEST ATTEMPTED"))
        assertTrue(report.contains("Routing request attempted: true"))
        assertTrue(report.contains("Mode before participation: MODE_IN_COMMUNICATION"))
        assertTrue(report.contains("Silent communication AudioTrack created: true"))
        assertTrue(report.contains("Silent AudioTrack started: true"))
        assertTrue(report.contains("Private Audio active VOICE_COMMUNICATION playback observed: true"))
        assertTrue(report.contains("Silent VOICE_COMMUNICATION AudioTrack active before mode request: true"))
        assertTrue(report.contains("Mode request issued after silent playback became active: true"))
        assertTrue(report.contains("Explicit Private Audio setMode(MODE_IN_COMMUNICATION) invoked: true"))
        assertTrue(report.contains("Mode request timestamp: 2026-08-12T12:34:56Z"))
        assertTrue(report.contains("Mode request thread: main (id=1)"))
        assertTrue(report.contains("Mode immediately before request: MODE_IN_COMMUNICATION"))
        assertTrue(report.contains("Mode immediately after request: MODE_IN_COMMUNICATION"))
        assertTrue(report.contains("Mode request exception: None"))
        assertTrue(report.contains("MODE_IN_COMMUNICATION observed after request: true"))
        assertTrue(report.contains("Earpiece request occurred after explicit mode request: true"))
        assertTrue(report.contains("Routing request accepted: true"))
        assertTrue(report.contains("Total routing attempts: 1"))
        assertTrue(report.contains("DELAYED OBSERVATION"))
        assertTrue(report.contains("Active playback configurations:"))
        assertTrue(report.contains("usage=USAGE_VOICE_COMMUNICATION; content=CONTENT_TYPE_SPEECH"))
        assertTrue(report.contains("ADB CORRELATION"))
        assertTrue(report.contains("This report does not claim actual mode ownership"))
        assertTrue(report.contains("Attempt 1: timestamp=12:34:56.000"))
        assertTrue(report.contains("Audible result requiring human confirmation: UNKNOWN"))
        assertTrue(report.contains("AudioManager mode: MODE_IN_COMMUNICATION"))
        assertTrue(report.contains("type=Built-in earpiece; product=Phone earpiece; Android device ID=1"))
        assertTrue(report.contains("type=Built-in speaker; product=Phone speaker; Android device ID=2"))
        assertTrue(report.contains("Speakerphone: Off (directly observed)"))
        assertTrue(report.contains("12:34:55.000  Baseline — state recorded"))
        assertTrue(report.contains("12:34:56.000  Manual snapshot"))
    }

    @Test
    fun poc5ArmingAndQualifyingTriggerRemainMutationFreeUntilEligible() {
        val arm = observerSource.method("fun armEarpieceTest()")
        assertFalse(arm.contains("startSilentCommunicationTrack"))
        assertFalse(arm.contains("setCommunicationDevice"))
        assertFalse(arm.contains("AudioManager.MODE_IN_COMMUNICATION"))

        val trigger = observerSource.method("private fun evaluateExperimentTrigger()")
        assertInOrder(
            trigger,
            "if (!experiment.armed) return",
            "mode.isTelephonyOrSystemPriorityMode()",
            "mode != AudioManager.MODE_IN_COMMUNICATION",
            "currentDevice?.type != AudioDeviceInfo.TYPE_BUILTIN_SPEAKER",
            "it.type == AudioDeviceInfo.TYPE_BUILTIN_EARPIECE",
            "startSilentCommunicationTrack()",
        )
        assertInOrder(
            observerSource.method("fun snapshot(reason: String)"),
            "observeExperimentOutcome(observed, reason)",
            "if (!routingActionInProgress) evaluateExperimentTrigger()",
        )
    }

    @Test
    fun poc5PlaybackModeAndSingleRouteRequestOrderingRemainsProtected() {
        val trigger = observerSource.method("private fun evaluateExperimentTrigger()")
        assertInOrder(
            trigger,
            "if (!startSilentCommunicationTrack())",
            "silentTrack?.playState == AudioTrack.PLAYSTATE_PLAYING",
            "audioManager.mode = AudioManager.MODE_IN_COMMUNICATION",
            "modeRequestFailure != null",
            "modeAfterParticipation != AudioManager.MODE_IN_COMMUNICATION",
            "performRoutingAttempt(earpiece",
        )

        val track = observerSource.method("private fun startSilentCommunicationTrack(): Boolean")
        assertInOrder(
            track,
            ".setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)",
            ".setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)",
            ".setEncoding(AudioFormat.ENCODING_PCM_16BIT)",
            ".setChannelMask(AudioFormat.CHANNEL_OUT_MONO)",
            ".setTransferMode(AudioTrack.MODE_STREAM)",
            "ShortArray(bufferBytes / Short.SIZE_BYTES)",
            "track.write(silence",
            "track.play()",
            "track.playState == AudioTrack.PLAYSTATE_PLAYING",
            "return started",
        )

        val route = observerSource.method("private fun performRoutingAttempt(")
        assertTrue(route.startsWith("private fun performRoutingAttempt"))
        assertTrue(route.contains("if (!experiment.armed || experiment.attempts.isNotEmpty()) return"))
        assertEquals(1, route.occurrences("audioManager.setCommunicationDevice(earpiece)"))
    }

    @Test
    fun poc5CallbacksAndDelayedObservationCannotRetryRouting() {
        val route = observerSource.method("private fun performRoutingAttempt(")
        assertInOrder(
            route,
            "routingActionInProgress = true",
            "audioManager.setCommunicationDevice(earpiece)",
            "experiment.attempts + attempt",
            "routingActionInProgress = false",
            "snapshot(\"Immediate post-request observation\")",
            "scheduleShortObservation()",
        )

        val delayed = observerSource.method("private fun scheduleShortObservation()")
        assertTrue(delayed.contains("if (!experiment.armed || experiment.attempts.size != 1) return@Runnable"))
        assertTrue(delayed.contains("snapshot(\"Short post-request observation period elapsed\")"))
        assertFalse(delayed.contains("performRoutingAttempt"))
        assertFalse(delayed.contains("setCommunicationDevice"))

        assertEquals(1, observerSource.occurrences("audioManager.setCommunicationDevice(earpiece)"))
    }

    @Test
    fun poc5FailureExitAndPriorityPathsRetainCleanup() {
        val trigger = observerSource.method("private fun evaluateExperimentTrigger()")

        val sessionExit = trigger
            .substringAfter("if (experiment.attempts.isNotEmpty() && mode != AudioManager.MODE_IN_COMMUNICATION)")
            .substringBefore("if (experiment.attempts.isNotEmpty() || mode != AudioManager.MODE_IN_COMMUNICATION) return")
        assertInOrder(
            sessionExit,
            "clearExperiment(",
            "Observed communication session left MODE_IN_COMMUNICATION",
            "finalState = ExperimentState.CLEARED",
            "return",
        )
        assertInOrder(
            trigger,
            "Observed communication session left MODE_IN_COMMUNICATION",
            "return",
            "startSilentCommunicationTrack()",
        )

        val silentTrackFailure = trigger
            .substringAfter("if (!startSilentCommunicationTrack())")
            .substringBefore("val postTrackStart = collectSnapshot()")
        assertInOrder(
            silentTrackFailure,
            "clearExperiment(\"Silent communication AudioTrack could not be started\", ExperimentState.BLOCKED)",
            "return",
        )

        val modeRequestFailure = trigger
            .substringAfter("if (modeRequestFailure != null)")
            .substringBefore("if (modeAfterParticipation != AudioManager.MODE_IN_COMMUNICATION)")
        assertInOrder(
            modeRequestFailure,
            "clearExperiment(",
            "Explicit setMode failed",
            "ExperimentState.BLOCKED",
            "return",
        )

        val modeConfirmationFailure = trigger
            .substringAfter("if (modeAfterParticipation != AudioManager.MODE_IN_COMMUNICATION)")
            .substringBefore("performRoutingAttempt(earpiece")
        assertInOrder(
            modeConfirmationFailure,
            "clearExperiment(\"MODE_IN_COMMUNICATION was not re-established\", ExperimentState.BLOCKED)",
            "return",
        )
        assertInOrder(
            trigger,
            "if (!startSilentCommunicationTrack())",
            "Silent communication AudioTrack could not be started",
            "if (modeRequestFailure != null)",
            "Explicit setMode failed",
            "if (modeAfterParticipation != AudioManager.MODE_IN_COMMUNICATION)",
            "MODE_IN_COMMUNICATION was not re-established",
            "performRoutingAttempt(earpiece",
        )
        assertEquals(4, trigger.occurrences("ExperimentState.BLOCKED"))
        assertTrue(
            observerSource.method("private fun performRoutingAttempt(")
                .contains("Routing attempt blocked by system/telephony-priority mode"),
        )
        assertTrue(
            observerSource.method("fun disarmAndClear()")
                .contains("clearExperiment(\"User disarmed experiment\", ExperimentState.CLEARED)"),
        )
    }

    @Test
    fun poc5CleanupOrderCancellationAndIdempotenceRemainProtected() {
        val cleanup = observerSource.method("private fun clearExperiment(")
        assertInOrder(
            cleanup,
            "cancelPendingObservation()",
            "audioManager.clearCommunicationDevice()",
            "if (modeParticipationActive)",
            "audioManager.mode = AudioManager.MODE_NORMAL",
            "stopSilentCommunicationTrack()",
            "armed = false",
        )

        val stopTrack = observerSource.method("private fun stopSilentCommunicationTrack(): Boolean")
        assertInOrder(
            stopTrack,
            "val track = silentTrack ?: return true",
            "silentWriterRunning.set(false)",
            "silentWriterThread?.interrupt()",
            "silentWriterThread?.join",
            "track.stop()",
            "track.flush()",
            "track.release()",
            "silentTrack = null",
        )
        val cancel = observerSource.method("private fun cancelPendingObservation()")
        assertInOrder(cancel, "observationHandler::removeCallbacks", "pendingObservation = null")
    }

    @Test
    fun reportCopyStillUsesTheSingleExistingFormatterAfterAReportSnapshot() {
        val report = observerSource.method("fun report(): String")
        assertTrue(report.contains("buildDiagnosticReport("))

        val serviceReport = serviceSource.method("fun diagnosticReport(): String")
        assertInOrder(
            serviceReport,
            "observer.snapshot(\"Report snapshot\")",
            "return observer.report()",
        )
        val copyAction = mainActivitySource.substringAfter("onCopyReport = {")
            .substringBefore("},\n                    )")
        assertInOrder(
            copyAction,
            "val report = connectedService.diagnosticReport()",
            "ClipData.newPlainText(\"Private Audio diagnostic report\", report)",
        )
        assertEquals(1, observerSource.occurrences("internal fun buildDiagnosticReport("))
        assertEquals(0, mainActivitySource.occurrences("buildDiagnosticReport("))
        assertEquals(0, serviceSource.occurrences("buildDiagnosticReport("))
    }

    @Test
    fun serviceIsTheSoleProductionObserverOwner() {
        assertEquals(0, mainActivitySource.occurrences("AudioDiagnosticObserver("))
        assertEquals(1, serviceSource.occurrences("AudioDiagnosticObserver("))
        assertTrue(serviceSource.contains("observer.start()"))
        assertTrue(
            serviceSource.method("override fun onDestroy()")
                .contains("observer.stop(\"Service destroyed\")"),
        )
        assertFalse(mainActivitySource.contains("observer.stop()"))
        assertTrue(serviceSource.contains("return START_NOT_STICKY"))
    }

    private fun assertInOrder(source: String, vararg fragments: String) {
        var previous = -1
        fragments.forEach { fragment ->
            val current = source.indexOf(fragment)
            assertTrue("Missing expected source fragment: $fragment", current >= 0)
            assertTrue("Expected '$fragment' after '${fragments.getOrNull(fragments.indexOf(fragment) - 1)}'", current > previous)
            previous = current
        }
    }

    private fun String.method(signature: String): String {
        val start = indexOf(signature)
        check(start >= 0) { "Missing method signature: $signature" }
        val openingBrace = indexOf('{', start)
        check(openingBrace >= 0) { "Missing method body: $signature" }
        var depth = 0
        for (index in openingBrace until length) {
            when (this[index]) {
                '{' -> depth++
                '}' -> if (--depth == 0) return substring(start, index + 1)
            }
        }
        error("Unterminated method body: $signature")
    }

    private fun String.occurrences(needle: String): Int = windowed(needle.length).count { it == needle }

    private companion object {
        val observerSource = projectFile(
            "app/src/main/java/app/privateaudio/diagnostic/AudioDiagnosticObserver.kt",
        ).readText()
        val mainActivitySource = projectFile(
            "app/src/main/java/app/privateaudio/MainActivity.kt",
        ).readText()
        val serviceSource = projectFile(
            "app/src/main/java/app/privateaudio/PrivateAudioService.kt",
        ).readText()

        fun projectFile(relativePath: String): File {
            var directory = File(System.getProperty("user.dir")).absoluteFile
            while (true) {
                val candidate = File(directory, relativePath)
                if (candidate.isFile) return candidate
                directory = directory.parentFile ?: error("Could not locate repository file: $relativePath")
            }
        }
    }
}
