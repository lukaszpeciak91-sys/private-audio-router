package app.privateaudio

import android.annotation.SuppressLint
import android.os.PowerManager

internal data class ProximityStatus(
    val supported: Boolean,
    val held: Boolean,
    val lastAcquireReason: String? = null,
    val lastReleaseReason: String? = null,
    val stateAtLastTransition: PrivateAudioState? = null,
    val routeAtLastTransition: String? = null,
)

/** Owns only the public PowerManager wake-lock mechanics; routing eligibility stays service-owned. */
internal class ProximityScreenController(
    powerManager: PowerManager,
    private val recordEvent: (String) -> Unit,
) {
    private val supported: Boolean
    private val wakeLock: PowerManager.WakeLock?
    private var held = false
    private var operationsAvailable = true

    private var lastAcquireReason: String? = null
    private var lastReleaseReason: String? = null
    private var stateAtLastTransition: PrivateAudioState? = null
    private var routeAtLastTransition: String? = null

    init {
        val supportResult = runCatching {
            powerManager.isWakeLockLevelSupported(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK)
        }
        supported = supportResult.getOrDefault(false)
        wakeLock = if (supported) {
            runCatching {
                powerManager.newWakeLock(
                    PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
                    "PrivateAudio:CallLikeProximity",
                ).apply { setReferenceCounted(false) }
            }.onFailure {
                operationsAvailable = false
                recordEventSafely("Proximity wake lock creation failed — ${it.javaClass.simpleName}")
            }.getOrNull()
        } else {
            operationsAvailable = false
            null
        }
        when {
            supportResult.isFailure -> recordEventSafely(
                "Proximity wake lock support check failed — ${supportResult.exceptionOrNull()?.javaClass?.simpleName}",
            )
            !supported -> recordEventSafely("Proximity wake lock unsupported — feature remains inactive")
        }
    }

    @SuppressLint("WakelockTimeout")
    fun acquire(reason: String, state: PrivateAudioState, route: String) {
        val lock = wakeLock ?: return
        if (!operationsAvailable || held) return
        // This is a call-like proximity session, explicitly released at every service/routing boundary.
        val failure = runCatching { lock.acquire() }.exceptionOrNull()
        if (failure != null) {
            operationsAvailable = false
            recordEventSafely("Proximity wake lock acquire failed — ${failure.javaClass.simpleName}")
            return
        }
        held = true
        lastAcquireReason = reason
        stateAtLastTransition = state
        routeAtLastTransition = route
        recordEventSafely("Proximity acquired — state=$state; route=$route")
    }

    fun release(reason: String, state: PrivateAudioState, route: String?) {
        val lock = wakeLock ?: return
        if (!held) return
        val failure = runCatching { lock.release() }.exceptionOrNull()
        if (failure != null) {
            operationsAvailable = false
            recordEventSafely("Proximity wake lock release failed — ${failure.javaClass.simpleName}")
            return
        }
        held = false
        lastReleaseReason = reason
        stateAtLastTransition = state
        routeAtLastTransition = route
        recordEventSafely("Proximity released — reason=$reason; state=$state; route=${route ?: "Unknown"}")
    }

    fun status() = ProximityStatus(
        supported = supported,
        held = held,
        lastAcquireReason = lastAcquireReason,
        lastReleaseReason = lastReleaseReason,
        stateAtLastTransition = stateAtLastTransition,
        routeAtLastTransition = routeAtLastTransition,
    )

    private fun recordEventSafely(message: String) {
        runCatching { recordEvent(message) }
    }
}

internal fun proximityEligible(
    featureEnabled: Boolean,
    controllerEnabled: Boolean,
    state: PrivateAudioState,
    mode: String,
    route: String?,
    supported: Boolean,
) = featureEnabled && controllerEnabled && supported && state == PrivateAudioState.ACTIVE &&
    mode == "MODE_IN_COMMUNICATION" && route == "Built-in earpiece"
