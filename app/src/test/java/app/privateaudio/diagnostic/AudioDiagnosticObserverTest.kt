package app.privateaudio.diagnostic

import app.privateaudio.kotlinDeclaration
import app.privateaudio.kotlinMemberCallSites
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
        assertTrue(audioSourceName(999).startsWith("Unknown / redacted / not exposed"))
    }

    @Test
    fun recordingTransitionsSuppressIdenticalAndDistinguishMeaningfulChanges() {
        val initial = ObservedRecording("VOICE_RECOGNITION", "Unknown / redacted / not exposed", "Unknown", null, null)
        assertTrue(recordingChanges(emptyList(), emptyList()).descriptions.isEmpty())
        assertEquals(listOf("RECORDING appeared", "recording count changed 0 → 1"), recordingChanges(emptyList(), listOf(initial)).descriptions)
        assertTrue(recordingChanges(listOf(initial), listOf(initial)).descriptions.isEmpty())
        assertTrue(recordingChanges(listOf(initial), listOf(initial.copy(audioSource = "VOICE_COMMUNICATION"))).descriptions.contains("audio source changed"))
        assertEquals(listOf("RECORDING disappeared", "recording count changed 1 → 0"), recordingChanges(listOf(initial), emptyList()).descriptions)
    }

    @Test
    fun recordingObservationLifecycleIsIndependentFromControllerAndCannotRoute() {
        val start = observerSource.method("fun start()")
        val stop = observerSource.method("fun stop(reason: String)")
        val disable = observerSource.method("fun disableController()")
        val callback = observerSource.method("private fun handleRecordingConfigurations(")
        assertTrue(start.contains("registerRecordingCallback()"))
        assertTrue(stop.contains("unregisterRecordingCallback()"))
        assertFalse(disable.contains("unregisterRecordingCallback"))
        assertFalse(callback.contains("evaluateExperimentTrigger"))
        assertFalse(callback.contains("setCommunicationDevice"))
        assertFalse(callback.contains("experiment ="))
        assertTrue(observerSource.method("private fun registerRecordingCallback()").contains("if (recordingCallbackRegistered) return"))
        assertTrue(observerSource.method("private fun unregisterRecordingCallback()").contains("if (!recordingCallbackRegistered) return"))
    }

    @Test
    fun recordingDiagnosticsRemainMetadataOnlyAndGenerationScoped() {
        assertFalse(observerSource.contains("AudioRecord("))
        assertFalse(observerSource.contains("RECORD_AUDIO"))
        assertTrue(observerSource.contains("AudioManager.AudioRecordingCallback"))
        assertTrue(observerSource.contains("activeRecordingConfigurations"))
        assertTrue(observerSource.contains("it.generation == cycleGeneration"))
        val manifest = File("src/main/AndroidManifest.xml").readText()
        assertFalse(manifest.contains("android.permission.RECORD_AUDIO"))
    }

    @Test
    fun changeDescriptionReportsOnlyObservedDifference() {
        val previous = DiagnosticSnapshot("MODE_NORMAL", null, emptyList(), "Off (directly observed)")
        val current = previous.copy(mode = "MODE_IN_COMMUNICATION")

        assertTrue(describeChanges(previous, current).contains("MODE_NORMAL → MODE_IN_COMMUNICATION"))
    }

    @Test
    fun syntheticSnapshotsDoNotPretendToHaveObservedProcessIdentity() {
        val synthetic = DiagnosticSnapshot("MODE_NORMAL", null, emptyList(), "Off (directly observed)")

        assertEquals(null, synthetic.processId)
        assertEquals(null, synthetic.userId)
        assertEquals(null, DiagnosticSnapshot.Empty.processId)
        assertEquals(null, DiagnosticSnapshot.Empty.userId)
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
                    flags = "FLAGS_NONE (0x0)",
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
            startupAudioTrace = listOf(
                "12:34:55.123  playback callback — previous=0; current=1; added=1; removed=0; unchanged=0",
                "  playback appeared/started — usage=USAGE_VOICE_COMMUNICATION",
            ),
            environment = DiagnosticEnvironment("0.1.0", 1, "13", 33, "Xiaomi", "Test model", "2201117TY"),
            startupTraceEntriesDropped = 7,
            eventEntriesDropped = 3,
            redundantPlaybackCallbacksSuppressed = 11,
            supportSummary = supportSummary(),
        )

        assertTrue(report.contains("Timestamp: 2026-08-12T12:34:56Z"))
        assertTrue(report.contains("Diagnostic report format: 3"))
        assertTrue(report.contains("DIAGNOSTIC ENVIRONMENT"))
        assertTrue(report.contains("SUPPORT SUMMARY"))
        assertTrue(report.indexOf("SUPPORT SUMMARY") < report.indexOf("DIAGNOSTIC ENVIRONMENT"))
        assertTrue(report.contains("Puzru version: 0.1.0 (1)"))
        assertTrue(report.contains("Android: 13"))
        assertTrue(report.contains("API level: 33"))
        assertTrue(report.contains("Manufacturer: Xiaomi"))
        assertTrue(report.contains("Model: Test model"))
        assertTrue(report.contains("Product: 2201117TY"))
        assertTrue(report.contains("Puzru enabled: true"))
        assertTrue(report.contains("Puzru state: ACTIVE"))
        assertTrue(report.contains("Built-in earpiece available: true"))
        assertTrue(report.contains("Current audio route: EARPIECE"))
        assertTrue(report.contains("Proximity supported: true"))
        assertTrue(report.contains("Floating control permission: granted"))
        assertTrue(report.contains("Last routing result: SUCCESS"))
        assertTrue(report.contains("Last routing error: NONE"))
        assertTrue(report.contains("Startup trace capacity: 240"))
        assertTrue(report.contains("Startup trace entries dropped: 7"))
        assertTrue(report.contains("Event log capacity: 100"))
        assertTrue(report.contains("Event log entries dropped: 3"))
        assertTrue(report.contains("Redundant playback callbacks suppressed: 11"))
        assertTrue(report.contains("Experiment state: REQUEST ATTEMPTED"))
        assertTrue(report.contains("Routing request attempted: true"))
        assertTrue(report.contains("Mode before participation: MODE_IN_COMMUNICATION"))
        assertTrue(report.contains("Silent communication AudioTrack created: true"))
        assertTrue(report.contains("Silent AudioTrack started: true"))
        assertTrue(report.contains("Puzru active VOICE_COMMUNICATION playback observed: true"))
        assertTrue(report.contains("Silent VOICE_COMMUNICATION AudioTrack active before mode request: true"))
        assertTrue(report.contains("Mode request issued after silent playback became active: true"))
        assertTrue(report.contains("Explicit Puzru setMode(MODE_IN_COMMUNICATION) invoked: true"))
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
        assertTrue(report.contains("flags=FLAGS_NONE (0x0); capture policy=ALLOW_CAPTURE_BY_ALL"))
        assertFalse(report.contains("active="))
        assertTrue(report.contains("STARTUP AUDIO TRACE"))
        assertTrue(report.contains("12:34:55.123  playback callback"))
        assertTrue(report.contains("player/session identity=Not exposed by the public AudioPlaybackConfiguration API"))
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
        assertTrue(report.contains("COMPLETED ROUTING CYCLE HISTORY (NEWEST FIRST)\nNone recorded"))
    }

    @Test
    fun completedCycleReportSurvivesFreshArmedExperimentForBothTriggerOrigins() {
        val finalCleanup = DiagnosticSnapshot(
            mode = "MODE_NORMAL",
            communicationDevice = null,
            availableCommunicationDevices = emptyList(),
            speakerphoneState = "Off (directly observed)",
            timestamp = "2026-08-19T12:35:00Z",
        )
        TriggerOrigin.entries.forEach { origin ->
            val completedExperiment = EarpieceExperiment(
                state = ExperimentState.CLEARED,
                requestAttempted = true,
                triggerOrigin = origin,
                modeBeforeParticipation = "MODE_NORMAL",
                selectedTarget = ObservedDevice(1, "Built-in earpiece", "Phone earpiece"),
                requestAccepted = true,
                attempts = listOf(
                    RoutingAttempt(
                        1, "12:34:56.000", "protected POC-5", "MODE_IN_COMMUNICATION",
                        null, true, ObservedDevice(1, "Built-in earpiece", "Phone earpiece"),
                        "Off (directly observed)",
                    ),
                ),
                earpieceReportedDuringSession = true,
                silentTrackCreated = true,
                silentTrackCleanupCompleted = true,
                preOwnership = finalCleanup.copy(mode = "MODE_NORMAL"),
                postSilentTrackStart = finalCleanup.copy(mode = "MODE_NORMAL"),
                postModeOwnership = finalCleanup.copy(mode = "MODE_IN_COMMUNICATION"),
                postRoutingRequest = finalCleanup.copy(mode = "MODE_IN_COMMUNICATION"),
                shortObservation = finalCleanup.copy(mode = "MODE_IN_COMMUNICATION"),
            )
            val report = buildDiagnosticReport(
                timestamp = "2026-08-19T12:36:00Z",
                experiment = EarpieceExperiment(state = ExperimentState.ARMED, armed = true),
                lastCompletedExperiment = CompletedRoutingCycle(
                    experiment = completedExperiment,
                    finalCleanupObservation = finalCleanup,
                    completionReason = "External communication playback ended",
                    completedAt = "2026-08-19T12:35:00Z",
                ),
                snapshot = finalCleanup,
                events = emptyList(),
            )

            assertTrue(report.contains("Experiment state: ARMED"))
            assertTrue(report.contains("Armed: true"))
            assertTrue(report.contains("LAST COMPLETED ROUTING CYCLE"))
            assertTrue(report.contains("Completion reason: External communication playback ended"))
            assertTrue(report.contains("Trigger origin: $origin"))
            assertTrue(report.contains("Routing request attempted: true"))
            assertTrue(report.contains("Routing request accepted: true"))
            assertTrue(report.contains("Total routing attempts: 1"))
            assertTrue(report.contains("Silent AudioTrack cleanup completed: true"))
            assertTrue(report.contains("Final mode after cleanup: MODE_NORMAL"))
            assertTrue(report.contains("COMPLETED FINAL CLEANUP OBSERVATION"))
        }
    }

    @Test
    fun completedCycleRetentionIsNewestFirstBoundedAndGenerationDeduplicated() {
        fun cycle(generation: Long) = CompletedRoutingCycle(
            experiment = EarpieceExperiment(routingGeneration = generation, requestAttempted = true),
            finalCleanupObservation = DiagnosticSnapshot.Empty,
            completionReason = "cycle-$generation",
            completedAt = "2026-09-07T00:00:0${generation}Z",
        )
        var retained = emptyList<CompletedRoutingCycle>()
        (1L..4L).forEach { retained = retainCompletedRoutingCycle(retained, cycle(it)) }
        assertEquals(listOf(4L, 3L, 2L), retained.map { it.experiment.routingGeneration })
        retained = retainCompletedRoutingCycle(retained, cycle(3))
        assertEquals(listOf(3L, 4L, 2L), retained.map { it.experiment.routingGeneration })

        val report = buildDiagnosticReport(
            timestamp = "now",
            experiment = EarpieceExperiment(routingGeneration = 5),
            completedRoutingCycles = retained,
            snapshot = DiagnosticSnapshot.Empty,
            events = emptyList(),
        )
        assertTrue(report.indexOf("cycle-3") < report.indexOf("cycle-4"))
        assertTrue(report.indexOf("cycle-4") < report.indexOf("cycle-2"))
        assertTrue(report.contains("Routing cycle/generation: 5"))
    }

    @Test
    fun reportIncludesNoTriggerPlaybackRouteLossAndBuildEvidence() {
        val playback = PublicPlaybackObservation(
            timestamp = "2026-09-07T12:00:00Z",
            mode = "MODE_NORMAL",
            communicationDevice = ObservedDevice(2, "Built-in speaker", "speaker"),
            speakerphoneState = "On (directly observed)",
            playbackConfigurations = listOf(ObservedPlayback("USAGE_MEDIA", "CONTENT_TYPE_SPEECH", allowedCapturePolicy = "ALLOW_CAPTURE_BY_ALL", device = null)),
            communicationQualifierCount = 0,
            assistantQualifierCount = 0,
            browserCommunicationQualifierCount = 0,
            selectedTriggerFamily = null,
        )
        val report = buildDiagnosticReport(
            timestamp = "now",
            experiment = EarpieceExperiment(
                routingGeneration = 9,
                earpieceReportedDuringSession = true,
                routeLostAfterEarpiece = true,
                firstReplacementCommunicationDevice = ObservedDevice(8, "USB headset", "USB"),
                routeLossTimestamp = "2026-09-07T12:00:01Z",
            ),
            snapshot = DiagnosticSnapshot.Empty,
            events = emptyList(),
            playbackObservation = playback,
            environment = DiagnosticEnvironment("0.1.0", 1, "13", 33, "OEM", "model", "product", "TKQ1", "build-display", "fingerprint", "2026-08-01"),
        )
        assertTrue(report.contains("MOST RECENT MEANINGFUL PUBLIC PLAYBACK OBSERVATION"))
        assertTrue(report.contains("Communication qualifier count: 0"))
        assertTrue(report.contains("Selected existing trigger family: None"))
        assertTrue(report.contains("application/client ownership is not attributed"))
        assertTrue(report.contains("Route lost after earpiece: true"))
        assertTrue(report.contains("type=USB headset"))
        assertTrue(report.contains("Route-loss timestamp: 2026-09-07T12:00:01Z"))
        assertTrue(report.contains("Android build ID: TKQ1"))
        assertTrue(report.contains("Android build fingerprint: fingerprint"))
        assertTrue(report.contains("Android security patch: 2026-08-01"))
    }

    @Test
    fun meaningfulPlaybackRetentionIgnoresEmptyCallbacksAndAcceptsLaterPlayback() {
        fun observation(timestamp: String, playback: List<ObservedPlayback>) = PublicPlaybackObservation(
            timestamp = timestamp,
            mode = "MODE_NORMAL",
            communicationDevice = ObservedDevice(2, "Built-in speaker", "speaker"),
            speakerphoneState = "On (directly observed)",
            playbackConfigurations = playback,
            communicationQualifierCount = 0,
            assistantQualifierCount = 0,
            browserCommunicationQualifierCount = 0,
            selectedTriggerFamily = null,
        )
        val unqualified = ObservedPlayback(
            usage = "USAGE_MEDIA",
            contentType = "CONTENT_TYPE_SPEECH",
            allowedCapturePolicy = "ALLOW_CAPTURE_BY_ALL",
            device = null,
        )
        val later = unqualified.copy(contentType = "CONTENT_TYPE_MUSIC")

        val retained = retainMeaningfulPlaybackObservation(null, observation("first", listOf(unqualified)))
        assertEquals("first", retained?.timestamp)
        assertEquals(
            retained,
            retainMeaningfulPlaybackObservation(retained, observation("empty", emptyList())),
        )
        val replaced = retainMeaningfulPlaybackObservation(retained, observation("later", listOf(later)))
        assertEquals("later", replaced?.timestamp)
        assertEquals(listOf(later), replaced?.playbackConfigurations)
    }

    @Test
    fun activeRouteLossIsGenericAndCleanupIsNotMisclassified() {
        val established = EarpieceExperiment(earpieceReportedDuringSession = true)
        val replacement = DiagnosticSnapshot(
            "MODE_IN_COMMUNICATION",
            ObservedDevice(7, "Wired headset", "USB-C headset"),
            emptyList(),
            "Off",
            timestamp = "loss-time",
        )
        val lost = observeActiveRouteLoss(established, replacement, cleanupInProgress = false)
        assertTrue(lost.routeLostAfterEarpiece)
        assertEquals("Wired headset", lost.firstReplacementCommunicationDevice?.type)
        assertEquals("loss-time", lost.routeLossTimestamp)
        assertFalse(lost.revertedToSpeaker)

        val cleanup = observeActiveRouteLoss(established, replacement, cleanupInProgress = true)
        assertFalse(cleanup.routeLostAfterEarpiece)
        assertEquals(established, cleanup)
    }

    @Test
    fun reportCapturePathIsFreshAndObservationallyReadOnly() {
        val report = serviceSource.method("fun diagnosticReport(): String")
        assertTrue(report.contains("observer.currentObservation()"))
        assertFalse(report.contains("observer.snapshot("))
        assertTrue(
            observerSource.contains(
                "internal fun currentObservation(): DiagnosticSnapshot = collectSnapshot()",
            ),
        )
        val observation = observerSource.method("private fun collectSnapshot()")
        listOf("snapshot =", "addEvent(", "prepareSilentCommunicationTrack", "evaluateExperimentTrigger", "setCommunicationDevice", "audioManager.mode =").forEach {
            assertFalse("Read-only observation unexpectedly contains $it", observation.contains(it))
        }
    }

    @Test
    fun representativeWorstCaseReportHasConservativeUtf8SizeGuard() {
        val device = ObservedDevice(1234, "Bluetooth LE headset", "Representative OEM accessory")
        val playback = ObservedPlayback(
            usage = "USAGE_VOICE_COMMUNICATION",
            contentType = "CONTENT_TYPE_SPEECH",
            flags = "FLAG_AUDIBILITY_ENFORCED (0x1)",
            allowedCapturePolicy = "ALLOW_CAPTURE_BY_SYSTEM",
            device = device,
        )
        val snapshot = DiagnosticSnapshot(
            mode = "MODE_IN_COMMUNICATION",
            communicationDevice = device,
            availableCommunicationDevices = List(8) { device.copy(id = it + 1, productName = "Representative device $it") },
            speakerphoneState = "Off (directly observed)",
            timestamp = "2026-09-07T12:34:56.123456789Z",
            processId = 12345,
            userId = 10123,
            lifecycleState = "Foreground",
            activePlaybackConfigurations = List(8) { playback },
        )
        fun cycle(generation: Long) = CompletedRoutingCycle(
            experiment = EarpieceExperiment(
                routingGeneration = generation,
                state = ExperimentState.CLEARED,
                requestAttempted = true,
                triggerOrigin = TriggerOrigin.ASSISTANT,
                selectedTarget = device,
                requestAccepted = true,
                attempts = listOf(RoutingAttempt(1, "12:34:56.123", "representative protected request", "MODE_IN_COMMUNICATION", device, true, device, "Off (directly observed)")),
                earpieceReportedDuringSession = true,
                routeLostAfterEarpiece = true,
                firstReplacementCommunicationDevice = device,
                routeLossTimestamp = "2026-09-07T12:34:57Z",
                preOwnership = snapshot,
                postSilentTrackStart = snapshot,
                postModeOwnership = snapshot,
                postRoutingRequest = snapshot,
                shortObservation = snapshot,
                silentTrackCreated = true,
                silentTrackCleanupCompleted = true,
            ),
            finalCleanupObservation = snapshot.copy(mode = "MODE_NORMAL", communicationDevice = null),
            completionReason = "Representative completed protected routing cycle",
            completedAt = "2026-09-07T12:35:00Z",
        )
        val recording = ObservedRecording("VOICE_RECOGNITION", "123", "PCM_16_BIT/48000 Hz/mono", device, false)
        val recordingContext = RecordingContext(true, "MODE_IN_COMMUNICATION", device, "Off", true, TriggerOrigin.ASSISTANT, "PLAYSTATE_PLAYING", 4)
        val report = buildDiagnosticReport(
            timestamp = "2026-09-07T12:35:00Z",
            experiment = cycle(4).experiment.copy(state = ExperimentState.REQUEST_ATTEMPTED),
            completedRoutingCycles = listOf(cycle(3), cycle(2), cycle(1)),
            snapshot = snapshot,
            events = List(MAX_EVENTS) { "12:34:56.123  Representative retained diagnostic event $it with bounded routing evidence" },
            baseline = snapshot,
            startupAudioTrace = List(MAX_STARTUP_TRACE_EVENTS) { "12:34:56.123  playback callback $it — public metadata changed with representative bounded detail" },
            playbackObservation = PublicPlaybackObservation("2026-09-07T12:34:56Z", snapshot.mode, device, snapshot.speakerphoneState, snapshot.activePlaybackConfigurations, 2, 1, 1, TriggerOrigin.COMMUNICATION),
            environment = DiagnosticEnvironment("0.1.0", 1, "16", 36, "Representative OEM", "Representative model", "representative_product", "BUILD-ID", "display-build-string", "oem/product/device:16/BUILD-ID/123456:user/release-keys", "2026-08-01"),
            eventEntriesDropped = 999,
            startupTraceEntriesDropped = 999,
            recordingCallbackRegistered = true,
            currentRecordingConfigurations = List(4) { recording },
            recordingTrace = List(MAX_RECORDING_TRACE_EVENTS) { RecordingTraceEntry("2026-09-07T12:34:56Z", it.toLong(), "RECORDING configuration changed; input device changed", List(4) { recording }, recordingContext) },
            recordingTraceEntriesDropped = 999,
            recordingStartupObservation = RecordingStartupObservation(4, 1, List(4) { recording }, List(4) { recording }, List(4) { recording }, List(4) { recording }, List(4) { recording }),
            supportSummary = supportSummary(),
        )
        val utf8Bytes = report.toByteArray(Charsets.UTF_8).size
        println("REPRESENTATIVE_WORST_CASE_DIAGNOSTIC_UTF8_BYTES=$utf8Bytes")
        assertTrue("Representative report unexpectedly omitted evidence", utf8Bytes > 50_000)
        assertTrue("Representative report exceeded 512 KiB: $utf8Bytes bytes", utf8Bytes <= 512 * 1024)
    }

    @Test
    fun playbackTimelinePreservesOverlapsAndExactPublicValues() {
        val media = ObservedPlayback(
            usage = "USAGE_MEDIA",
            contentType = "CONTENT_TYPE_MUSIC",
            flags = "0x1",
            allowedCapturePolicy = "ALLOW_CAPTURE_BY_ALL",
            device = ObservedDevice(9, "Built-in speaker", "Speaker"),
        )
        val voice = media.copy(usage = "USAGE_VOICE_COMMUNICATION", contentType = "CONTENT_TYPE_SPEECH")

        val added = playbackChanges(listOf(media), listOf(media, voice))
        assertEquals("previous=1; current=2; added=1; removed=0; unchanged=1", added.summary)
        assertTrue(added.entries.single().contains("playback appeared/started"))
        assertTrue(added.entries.single().contains("usage=USAGE_VOICE_COMMUNICATION"))
        assertTrue(added.entries.single().contains("contentType=CONTENT_TYPE_SPEECH; flags=0x1"))
        assertTrue(added.entries.single().contains("device=type=Built-in speaker"))

        val removed = playbackChanges(listOf(media, voice), listOf(voice))
        assertTrue(removed.entries.single().contains("playback disappeared/stopped"))
        assertTrue(removed.entries.single().contains("usage=USAGE_MEDIA"))
        val unchanged = playbackChanges(listOf(media, voice), listOf(media, voice))
        assertEquals("previous=2; current=2; added=0; removed=0; unchanged=2", unchanged.summary)
        assertTrue(unchanged.entries.isEmpty())
        assertEquals("USAGE_ASSISTANCE_SONIFICATION", audioUsageName(android.media.AudioAttributes.USAGE_ASSISTANCE_SONIFICATION))
        assertEquals("FLAGS_NONE (0x0)", audioFlagsName(0))
    }

    @Test
    fun controllerEnableWaitsAndRegistersPublicPlaybackObservation() {
        val enable = observerSource.method("fun enableController()")
        assertInOrder(enable, "controllerEnabled = true", "registerAudioPlaybackCallback", "Controller ON", "handlePlaybackConfigurations")
        assertTrue(enable.contains("prepareSilentCommunicationTrack()"))
        assertFalse(enable.contains("setCommunicationDevice"))
        assertFalse(enable.contains("AudioManager.MODE_IN_COMMUNICATION"))
    }

    @Test
    fun onlyQualifyingCommunicationPlaybackCanStartRouting() {
        val callback = observerSource.method("private fun handlePlaybackConfigurations(")
        val matcher = observerSource.method("private fun qualifyingPlaybackCount(")
        assertTrue(matcher.contains("USAGE_VOICE_COMMUNICATION"))
        assertTrue(matcher.contains("CONTENT_TYPE_SPEECH"))
        assertFalse(matcher.contains("USAGE_MEDIA"))
        assertInOrder(callback, "if (!controllerEnabled) return", "count > 0 || assistantCount > 0", "evaluateExperimentTrigger()")
    }

    @Test
    fun poc5PlaybackModeAndSingleRouteRequestOrderingRemainsProtected() {
        val protectedBody = observerSource.method("private fun startProtectedPoc5Probe(")
        assertInOrder(protectedBody, "startSilentCommunicationTrack()", "PLAYSTATE_PLAYING", "requestCommunicationMode()", "performRoutingAttempt(earpiece")
        val creation = observerSource.method("private fun createSilentCommunicationTrack(): AudioTrack?")
        val track = observerSource.method("private fun startSilentCommunicationTrack(): Boolean")
        assertInOrder(creation, ".setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)", ".setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)", ".setEncoding(AudioFormat.ENCODING_PCM_16BIT)", ".setChannelMask(AudioFormat.CHANNEL_OUT_MONO)")
        assertInOrder(
            track,
            "val silence = ShortArray(",
            "track.play()",
            "track.playState == AudioTrack.PLAYSTATE_PLAYING",
            "if (started) startSilenceWriter(track, silence)",
            "return started",
        )
        val writer = observerSource.method("private fun startSilenceWriter(")
        assertTrue(writer.contains("track.write(silence"))
        assertEquals(1, observerSource.occurrences("audioManager.setCommunicationDevice(earpiece)"))
        assertTrue(observerSource.method("private fun performRoutingAttempt(").contains("experiment.attempts.isNotEmpty()"))
    }

    @Test
    fun endInferenceRequiresEstablishedExternalContributionAndStableDelay() {
        val callback = observerSource.method("private fun handlePlaybackConfigurations(")
        assertInOrder(callback, "count >= 2", "externalContributionEstablished = true", "else if (externalContributionEstablished)", "scheduleEndConfirmation()")
        val end = observerSource.method("private fun scheduleEndConfirmation()")
        assertTrue(end.contains("END_CONFIRMATION_DELAY_MS"))
        assertTrue(end.contains("generation != cycleGeneration"))
        assertTrue(end.contains("qualifyingPlaybackCount(audioManager.activePlaybackConfigurations) > 1"))
        assertInOrder(end, "External communication end confirmed", "clearExperiment", "returnToWaiting()")
    }

    @Test
    fun cleanupReturnsEnabledControllerToFreshWaitingCycle() {
        val cleanup = observerSource.method("private fun clearExperiment(")
        assertInOrder(cleanup, "cancelPendingEndConfirmation()", "cancelPendingObservation()", "audioManager.clearCommunicationDevice()", "audioManager.mode = AudioManager.MODE_NORMAL", "stopSilentCommunicationTrack()", "armed = false", "snapshot(\"Post-cleanup observation\")", "val completed = CompletedRoutingCycle(")
        assertTrue(cleanup.contains("experiment.requestAttempted || experiment.triggerOrigin != null"))
        val waiting = observerSource.method("private fun returnToWaiting()")
        assertInOrder(
            waiting,
            "if (!controllerEnabled) return",
            "cycleGeneration++",
            "EarpieceExperiment(",
            "state = ExperimentState.ARMED",
            "armed = true",
            "routingGeneration = cycleGeneration",
            "returned to clean waiting",
        )
        assertFalse(serviceSource.contains("onCompletedExperimentCleared"))
        assertFalse(serviceSource.contains("armFreshExperimentIfSafe"))
    }

    @Test
    fun powerOffAndDestructionFailClosedBeforeCleanup() {
        val disableService = serviceSource.method("fun disarmAndStopStartedLifetime()")
        assertInOrder(disableService, "isPrivateAudioEnabled = false", "observer.disableController()", "stopForeground", "stopSelf()")
        val disableController = observerSource.method("fun disableController()")
        assertInOrder(disableController, "controllerEnabled = false", "invalidatePendingControllerWork()", "unregisterPlaybackCallback()", "clearExperiment")
        val destroy = serviceSource.method("override fun onDestroy()")
        assertInOrder(destroy, "shuttingDown = true", "isPrivateAudioEnabled = false", "observer.stop")
        assertTrue(serviceSource.contains("return START_NOT_STICKY"))
    }

    @Test
    fun serviceIsSoleOwnerAndDetectionIsProviderIndependent() {
        assertEquals(0, mainActivitySource.occurrences("AudioDiagnosticObserver("))
        assertEquals(1, serviceSource.occurrences("AudioDiagnosticObserver("))
        val routingAndClassification = listOf(
            "private fun handlePlaybackConfigurations(",
            "private fun qualifyingPlaybackCount(",
            "private fun assistantQualifyingPlaybackCount(",
            "private fun browserQualifyingPlaybackCount(",
            "private fun evaluateExperimentTrigger()",
            "private fun startProtectedPoc5Probe(",
        ).joinToString("\n") { observerSource.method(it) }
        listOf("ChatGPT", "Gemini", "packageName").forEach {
            assertFalse(it, routingAndClassification.contains(it))
        }
        assertTrue(routingAndClassification.contains("AudioAttributes.USAGE_VOICE_COMMUNICATION"))
        assertTrue(routingAndClassification.contains("AudioAttributes.USAGE_ASSISTANT"))
        assertTrue(routingAndClassification.contains("startProtectedPoc5Probe(earpiece, triggerOrigin"))
        assertEquals(
            listOf(observerSourceFile),
            productionSources.kotlinMemberCallSites("setCommunicationDevice").map { it.file },
        )
    }

    @Test
    fun reportCopyStillUsesSingleFormatter() {
        assertEquals(1, observerSource.occurrences("internal fun buildDiagnosticReport("))
        assertEquals(0, mainActivitySource.occurrences("buildDiagnosticReport("))
        assertTrue(serviceSource.method("fun diagnosticReport(): String").contains("append(observer.report(supportSummary, currentObservation))"))
    }

    private fun supportSummary() = DiagnosticsSummary(
        earpiece = DiagnosticsAvailability.AVAILABLE,
        proximitySensor = DiagnosticsAvailability.AVAILABLE,
        floatingControlPermission = DiagnosticsPermission.GRANTED,
        routing = DiagnosticsRouting.ON,
        status = app.privateaudio.PrivateAudioState.ACTIVE,
        audioRoute = DiagnosticsRoute.EARPIECE,
        lastRoutingResult = DiagnosticsRoutingResult.SUCCESS,
        lastError = DiagnosticsError.NONE,
    )

    private fun assertInOrder(source: String, vararg fragments: String) {
        var previous = -1
        fragments.forEach { fragment ->
            val current = source.indexOf(fragment)
            assertTrue("Missing expected source fragment: $fragment", current >= 0)
            assertTrue("Expected '$fragment' after '${fragments.getOrNull(fragments.indexOf(fragment) - 1)}'", current > previous)
            previous = current
        }
    }

    private fun String.method(signature: String) = kotlinDeclaration(signature)

    private fun String.occurrences(needle: String): Int = windowed(needle.length).count { it == needle }

    private companion object {
        val observerSource = projectFile(
            "app/src/main/java/app/privateaudio/diagnostic/AudioDiagnosticObserver.kt",
        ).readText()
        val observerSourceFile = projectFile(
            "app/src/main/java/app/privateaudio/diagnostic/AudioDiagnosticObserver.kt",
        )
        val productionSources = observerSourceFile.parentFile.parentFile.parentFile.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }.toList()
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
