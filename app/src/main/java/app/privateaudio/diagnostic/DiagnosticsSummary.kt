package app.privateaudio.diagnostic

import app.privateaudio.PrivateAudioState

internal enum class DiagnosticsAvailability { AVAILABLE, NOT_AVAILABLE }
internal enum class DiagnosticsPermission { GRANTED, NOT_GRANTED }
internal enum class DiagnosticsRouting { ON, OFF }
internal enum class DiagnosticsRoute { EARPIECE, SPEAKER, BLUETOOTH, OTHER, UNKNOWN }
internal enum class DiagnosticsRoutingResult { SUCCESS, FAILED, NONE }
internal enum class DiagnosticsError {
    NONE,
    BLOCKED_BY_SYSTEM,
    SESSION_ENDED,
    AUDIO_ROUTING_START_FAILED,
    COMMUNICATION_AUDIO_PREPARATION_FAILED,
    EARPIECE_REQUEST_REJECTED,
    ROUTING_NOT_COMPLETED,
}

internal data class DiagnosticsSummary(
    val earpiece: DiagnosticsAvailability,
    val proximitySensor: DiagnosticsAvailability,
    val floatingControlPermission: DiagnosticsPermission,
    val routing: DiagnosticsRouting,
    val status: PrivateAudioState,
    val audioRoute: DiagnosticsRoute,
    val lastRoutingResult: DiagnosticsRoutingResult,
    val lastError: DiagnosticsError,
)

internal fun projectDiagnosticsSummary(
    snapshot: DiagnosticSnapshot,
    lastCompletedCycle: CompletedRoutingCycle?,
    privateAudioEnabled: Boolean,
    privateAudioState: PrivateAudioState,
    proximitySupported: Boolean,
    overlayPermissionGranted: Boolean,
): DiagnosticsSummary {
    val completedExperiment = lastCompletedCycle?.experiment
    val completedProtectedFailure = completedExperiment?.let {
        it.state == ExperimentState.BLOCKED || it.requestAccepted == false || it.modeRequestException != null
    } == true
    val result = when {
        completedExperiment == null -> DiagnosticsRoutingResult.NONE
        completedProtectedFailure -> DiagnosticsRoutingResult.FAILED
        completedExperiment.earpieceReportedDuringSession -> DiagnosticsRoutingResult.SUCCESS
        else -> DiagnosticsRoutingResult.FAILED
    }
    return DiagnosticsSummary(
        earpiece = if (snapshot.availableCommunicationDevices.any { it.type == "Built-in earpiece" }) {
            DiagnosticsAvailability.AVAILABLE
        } else {
            DiagnosticsAvailability.NOT_AVAILABLE
        },
        proximitySensor = if (proximitySupported) DiagnosticsAvailability.AVAILABLE else DiagnosticsAvailability.NOT_AVAILABLE,
        floatingControlPermission = if (overlayPermissionGranted) DiagnosticsPermission.GRANTED else DiagnosticsPermission.NOT_GRANTED,
        routing = if (privateAudioEnabled) DiagnosticsRouting.ON else DiagnosticsRouting.OFF,
        status = privateAudioState,
        audioRoute = snapshot.communicationDevice.toDiagnosticsRoute(),
        lastRoutingResult = result,
        lastError = if (result == DiagnosticsRoutingResult.FAILED) {
            projectDiagnosticsError(lastCompletedCycle, completedExperiment)
        } else {
            DiagnosticsError.NONE
        },
    )
}

private fun ObservedDevice?.toDiagnosticsRoute(): DiagnosticsRoute = when (this?.type) {
    null -> DiagnosticsRoute.UNKNOWN
    "Built-in earpiece" -> DiagnosticsRoute.EARPIECE
    "Built-in speaker" -> DiagnosticsRoute.SPEAKER
    "Bluetooth SCO", "Bluetooth A2DP", "Bluetooth LE headset", "Bluetooth LE speaker",
    "Bluetooth LE broadcast", "Hearing aid" -> DiagnosticsRoute.BLUETOOTH
    else -> DiagnosticsRoute.OTHER
}

private fun projectDiagnosticsError(
    cycle: CompletedRoutingCycle?,
    experiment: EarpieceExperiment?,
): DiagnosticsError {
    val reason = cycle?.completionReason.orEmpty()
    return when {
        reason.contains("system/telephony-priority", ignoreCase = true) -> DiagnosticsError.BLOCKED_BY_SYSTEM
        reason.contains("session ended", ignoreCase = true) ||
            reason.contains("playback ended", ignoreCase = true) -> DiagnosticsError.SESSION_ENDED
        reason.contains("AudioTrack", ignoreCase = true) -> DiagnosticsError.AUDIO_ROUTING_START_FAILED
        reason.contains("MODE_IN_COMMUNICATION", ignoreCase = true) || experiment?.modeRequestException != null ->
            DiagnosticsError.COMMUNICATION_AUDIO_PREPARATION_FAILED
        experiment?.requestAccepted == false -> DiagnosticsError.EARPIECE_REQUEST_REJECTED
        else -> DiagnosticsError.ROUTING_NOT_COMPLETED
    }
}

internal fun DiagnosticsSummary.supportSummary(): String = buildString {
    appendLine("SUPPORT SUMMARY")
    appendLine("Private Audio enabled: ${routing == DiagnosticsRouting.ON}")
    appendLine("Private Audio state: ${status.name}")
    appendLine("Built-in earpiece available: ${earpiece == DiagnosticsAvailability.AVAILABLE}")
    appendLine("Current audio route: ${audioRoute.name}")
    appendLine("Proximity supported: ${proximitySensor == DiagnosticsAvailability.AVAILABLE}")
    appendLine("Floating control permission: ${if (floatingControlPermission == DiagnosticsPermission.GRANTED) "granted" else "not granted"}")
    appendLine("Last routing result: ${lastRoutingResult.name}")
    append("Last routing error: ${lastError.name}")
}
