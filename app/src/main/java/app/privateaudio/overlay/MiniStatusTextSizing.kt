package app.privateaudio.overlay

internal val MINI_STATUS_TEXT_SIZE_CANDIDATES = listOf(16f, 15f, 14f)

internal fun selectMiniStatusTextSize(
    labels: List<String>,
    measureWidth: (label: String, textSize: Float) -> Float,
): Float = MINI_STATUS_TEXT_SIZE_CANDIDATES.firstOrNull { textSize ->
    labels.all { label -> measureWidth(label, textSize) <= MINI_STATUS_NON_ELLIPSIS_WIDTH }
} ?: MINI_STATUS_TEXT_SIZE_CANDIDATES.last()

internal const val MINI_STATUS_NON_ELLIPSIS_WIDTH = 96f
