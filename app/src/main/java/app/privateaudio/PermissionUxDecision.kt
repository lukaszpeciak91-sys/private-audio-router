package app.privateaudio

internal enum class NotificationPowerDecision { START, EXPLAIN }
internal enum class OverlayMiniDecision { SHOW, EXPLAIN, OPEN_SETTINGS }

internal fun notificationPowerDecision(
    apiLevel: Int,
    permissionGranted: Boolean,
    explanationResolved: Boolean,
): NotificationPowerDecision = if (
    apiLevel < 33 || permissionGranted || explanationResolved
) NotificationPowerDecision.START else NotificationPowerDecision.EXPLAIN

internal fun overlayMiniDecision(
    permissionGranted: Boolean,
    explanationResolved: Boolean,
): OverlayMiniDecision = when {
    permissionGranted -> OverlayMiniDecision.SHOW
    explanationResolved -> OverlayMiniDecision.OPEN_SETTINGS
    else -> OverlayMiniDecision.EXPLAIN
}
