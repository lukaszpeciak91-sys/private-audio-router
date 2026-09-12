package app.privateaudio

internal enum class NotificationPowerDecision { START, EXPLAIN }
internal enum class OverlayMiniDecision { SHOW, EXPLAIN }

internal fun notificationPowerDecision(
    apiLevel: Int,
    permissionGranted: Boolean,
    explanationResolved: Boolean,
): NotificationPowerDecision = if (
    apiLevel < 33 || permissionGranted || explanationResolved
) NotificationPowerDecision.START else NotificationPowerDecision.EXPLAIN

internal fun overlayMiniDecision(
    permissionGranted: Boolean,
): OverlayMiniDecision = if (permissionGranted) OverlayMiniDecision.SHOW else OverlayMiniDecision.EXPLAIN
