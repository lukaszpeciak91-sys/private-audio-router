package app.privateaudio.ui

import app.privateaudio.PrivateAudioState

/** Presentation-only semantics shared by Main and Mini. */
internal enum class StatusSymbol {
    OFF,
    WAITING_CLOCK,
    EARPIECE,
    WARNING,
}

internal data class StatusVisualStyle(
    val symbol: StatusSymbol,
    val colorArgb: Long,
)

internal fun statusVisualStyle(state: PrivateAudioState): StatusVisualStyle = when (state) {
    PrivateAudioState.READY -> StatusVisualStyle(StatusSymbol.OFF, 0xFFB8B8BCL)
    PrivateAudioState.WAITING -> StatusVisualStyle(StatusSymbol.WAITING_CLOCK, 0xFFEEAC36L)
    PrivateAudioState.ACTIVE -> StatusVisualStyle(StatusSymbol.EARPIECE, 0xFF22DA70L)
    PrivateAudioState.ERROR -> StatusVisualStyle(StatusSymbol.WARNING, 0xFFEE4B4BL)
}
