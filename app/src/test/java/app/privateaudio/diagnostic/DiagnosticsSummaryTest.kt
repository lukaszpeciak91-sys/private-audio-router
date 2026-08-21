package app.privateaudio.diagnostic

import app.privateaudio.PrivateAudioState
import org.junit.Assert.assertEquals
import org.junit.Test

class DiagnosticsSummaryTest {
    @Test
    fun noCompletedCycleHasNoResultAndNoError() {
        val summary = summary()

        assertEquals(DiagnosticsRoutingResult.NONE, summary.lastRoutingResult)
        assertEquals(DiagnosticsError.NONE, summary.lastError)
    }

    @Test
    fun acceptedRequestWithoutEarpieceEvidenceIsNotSuccess() {
        val summary = summary(completed = cycle(EarpieceExperiment(requestAccepted = true)))

        assertEquals(DiagnosticsRoutingResult.FAILED, summary.lastRoutingResult)
    }

    @Test
    fun completedCycleWithEarpieceEvidenceSucceeds() {
        val summary = summary(
            completed = cycle(
                EarpieceExperiment(
                    state = ExperimentState.CLEARED,
                    requestAccepted = true,
                    earpieceReportedDuringSession = true,
                ),
            ),
        )

        assertEquals(DiagnosticsRoutingResult.SUCCESS, summary.lastRoutingResult)
        assertEquals(DiagnosticsError.NONE, summary.lastError)
    }

    @Test
    fun blockedCycleFailsAndMapsItsReasonToAConciseError() {
        val summary = summary(
            completed = cycle(
                EarpieceExperiment(state = ExperimentState.BLOCKED),
                "Routing attempt blocked by system/telephony-priority mode",
            ),
        )

        assertEquals(DiagnosticsRoutingResult.FAILED, summary.lastRoutingResult)
        assertEquals(DiagnosticsError.BLOCKED_BY_SYSTEM, summary.lastError)
    }

    @Test
    fun failedCycleDerivesLastErrorFromCompletionReason() {
        val summary = summary(
            completed = cycle(
                EarpieceExperiment(state = ExperimentState.BLOCKED),
                "Silent communication AudioTrack could not be started",
            ),
        )

        assertEquals(DiagnosticsError.AUDIO_ROUTING_START_FAILED, summary.lastError)
    }

    @Test
    fun currentRouteMapsReportedAndroidDeviceAndDoesNotInventUnknownRoute() {
        assertEquals(DiagnosticsRoute.EARPIECE, summary(deviceType = "Built-in earpiece").audioRoute)
        assertEquals(DiagnosticsRoute.SPEAKER, summary(deviceType = "Built-in speaker").audioRoute)
        assertEquals(DiagnosticsRoute.BLUETOOTH, summary(deviceType = "Bluetooth LE headset").audioRoute)
        assertEquals(DiagnosticsRoute.OTHER, summary(deviceType = "USB headset").audioRoute)
        assertEquals(DiagnosticsRoute.UNKNOWN, summary(deviceType = null).audioRoute)
    }

    @Test
    fun supportSummaryUsesTheReleaseFacingProjectionAndStableValues() {
        val summary = summary(
            completed = cycle(
                EarpieceExperiment(
                    state = ExperimentState.CLEARED,
                    requestAccepted = true,
                    earpieceReportedDuringSession = true,
                ),
            ),
            deviceType = "Built-in earpiece",
        )

        assertEquals(
            """SUPPORT SUMMARY
Private Audio enabled: true
Private Audio state: WAITING
Built-in earpiece available: true
Current audio route: EARPIECE
Proximity supported: true
Floating control permission: not granted
Last routing result: SUCCESS
Last routing error: NONE""",
            summary.supportSummary(),
        )
    }

    private fun summary(
        completed: CompletedRoutingCycle? = null,
        deviceType: String? = null,
    ) = projectDiagnosticsSummary(
        snapshot = DiagnosticSnapshot(
            mode = "MODE_NORMAL",
            communicationDevice = deviceType?.let { ObservedDevice(1, it, "Test") },
            availableCommunicationDevices = listOf(ObservedDevice(2, "Built-in earpiece", "Test")),
            speakerphoneState = "Off",
        ),
        lastCompletedCycle = completed,
        privateAudioEnabled = true,
        privateAudioState = PrivateAudioState.WAITING,
        proximitySupported = true,
        overlayPermissionGranted = false,
    )

    private fun cycle(
        experiment: EarpieceExperiment,
        reason: String = "External communication playback ended",
    ) = CompletedRoutingCycle(
        experiment = experiment,
        finalCleanupObservation = DiagnosticSnapshot.Empty,
        completionReason = reason,
        completedAt = "2026-08-21T00:00:00Z",
    )
}
