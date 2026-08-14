package app.privateaudio.diagnostic

import android.media.AudioDeviceInfo
import android.media.AudioManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

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
                modeRequestIssuedAfterPlaybackActive = true,
                modeInCommunicationObserved = true,
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
        assertTrue(report.contains("Mode request issued after silent playback became active: true"))
        assertTrue(report.contains("MODE_IN_COMMUNICATION observed after request: true"))
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
}
