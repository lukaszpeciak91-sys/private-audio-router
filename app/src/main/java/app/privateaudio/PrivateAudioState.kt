package app.privateaudio

enum class PrivateAudioState {
    READY,
    WAITING,
    ACTIVE,
    ERROR,
}

internal data class PrivateAudioStateEvidence(
    val controllerEnabled: Boolean,
    val currentProtectedFailure: Boolean = false,
    val currentCycleParticipating: Boolean = false,
    val modeInCommunication: Boolean = false,
    val builtInEarpieceIsCurrent: Boolean = false,
)

internal fun projectPrivateAudioState(evidence: PrivateAudioStateEvidence): PrivateAudioState = when {
    !evidence.controllerEnabled -> PrivateAudioState.READY
    evidence.currentProtectedFailure -> PrivateAudioState.ERROR
    evidence.currentCycleParticipating &&
        evidence.modeInCommunication &&
        evidence.builtInEarpieceIsCurrent -> PrivateAudioState.ACTIVE
    else -> PrivateAudioState.WAITING
}
