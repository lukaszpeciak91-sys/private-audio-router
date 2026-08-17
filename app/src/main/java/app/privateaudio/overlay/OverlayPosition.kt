package app.privateaudio.overlay

internal data class OverlayPosition(val x: Int, val y: Int)

internal fun clampOverlayPosition(
    x: Int,
    y: Int,
    screenWidth: Int,
    screenHeight: Int,
    overlayWidth: Int,
    overlayHeight: Int,
): OverlayPosition = OverlayPosition(
    x = x.coerceIn(0, (screenWidth - overlayWidth).coerceAtLeast(0)),
    y = y.coerceIn(0, (screenHeight - overlayHeight).coerceAtLeast(0)),
)
