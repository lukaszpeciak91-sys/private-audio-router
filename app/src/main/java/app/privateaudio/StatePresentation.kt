package app.privateaudio

import androidx.annotation.StringRes

internal enum class StatusSymbol { OFF, WAITING, EARPIECE, WARNING }

internal data class StatePresentation(
    @StringRes val mainLabel: Int,
    @StringRes val miniLabel: Int,
    val symbol: StatusSymbol,
    @StringRes val notificationTitle: Int?,
    @StringRes val notificationText: Int?,
)

internal fun statePresentation(state: PrivateAudioState): StatePresentation = when (state) {
    PrivateAudioState.READY -> StatePresentation(
        R.string.status_main_ready,
        R.string.status_mini_ready,
        StatusSymbol.OFF,
        null,
        null,
    )
    PrivateAudioState.WAITING -> StatePresentation(
        R.string.status_main_waiting,
        R.string.status_mini_waiting,
        StatusSymbol.WAITING,
        R.string.routing_notification_waiting_title,
        R.string.routing_notification_waiting_text,
    )
    PrivateAudioState.ACTIVE -> StatePresentation(
        R.string.status_main_active,
        R.string.status_mini_active,
        StatusSymbol.EARPIECE,
        R.string.routing_notification_active_title,
        R.string.routing_notification_active_text,
    )
    PrivateAudioState.ERROR -> StatePresentation(
        R.string.status_main_error,
        R.string.status_mini_error,
        StatusSymbol.WARNING,
        R.string.routing_notification_error_title,
        R.string.routing_notification_error_text,
    )
}

internal fun foregroundNotificationPresentation(state: PrivateAudioState): StatePresentation? =
    statePresentation(state).takeIf { it.notificationTitle != null && it.notificationText != null }
