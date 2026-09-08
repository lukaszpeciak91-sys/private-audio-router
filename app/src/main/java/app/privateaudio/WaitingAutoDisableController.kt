package app.privateaudio

internal fun interface CancellableDelay {
    fun cancel()
}

internal fun interface DelayScheduler {
    fun schedule(delayMillis: Long, action: () -> Unit): CancellableDelay
}

/** Owns the single in-memory deadline associated with a continuous WAITING period. */
internal class WaitingAutoDisableController(
    private val scheduler: DelayScheduler,
    private val timeoutMillis: Long = WAITING_AUTO_DISABLE_MILLIS,
    private val onTimeout: () -> Unit,
) {
    private var state = PrivateAudioState.READY
    private var generation = 0L
    private var pending: CancellableDelay? = null

    fun onStateChanged(newState: PrivateAudioState) {
        if (newState == state) return

        generation += 1
        pending?.cancel()
        pending = null
        state = newState

        if (newState == PrivateAudioState.WAITING) {
            val waitingGeneration = generation
            pending = scheduler.schedule(timeoutMillis) {
                if (state != PrivateAudioState.WAITING || generation != waitingGeneration) return@schedule
                pending = null
                generation += 1
                onTimeout()
            }
        }
    }

    fun cancel() {
        generation += 1
        pending?.cancel()
        pending = null
        state = PrivateAudioState.READY
    }

    companion object {
        const val WAITING_AUTO_DISABLE_MILLIS = 30L * 60L * 1_000L
    }
}
