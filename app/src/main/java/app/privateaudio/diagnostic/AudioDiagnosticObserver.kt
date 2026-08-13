package app.privateaudio.diagnostic

import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executor

data class ObservedDevice(
    val id: Int,
    val type: String,
    val productName: String,
)

data class DiagnosticSnapshot(
    val mode: String,
    val communicationDevice: ObservedDevice?,
    val availableCommunicationDevices: List<ObservedDevice>,
    val speakerphoneState: String,
) {
    companion object {
        val Empty = DiagnosticSnapshot(
            mode = "Unavailable — not observed yet",
            communicationDevice = null,
            availableCommunicationDevices = emptyList(),
            speakerphoneState = "Unavailable — not observed yet",
        )
    }
}

enum class ExperimentState(val label: String) {
    IDLE("IDLE"),
    ARMED("ARMED"),
    REQUEST_ATTEMPTED("REQUEST ATTEMPTED"),
    CLEARED("CLEARED"),
    BLOCKED("BLOCKED / FAILED"),
}

data class EarpieceExperiment(
    val state: ExperimentState = ExperimentState.IDLE,
    val armed: Boolean = false,
    val requestAttempted: Boolean = false,
    val selectedTarget: ObservedDevice? = null,
    val modeParticipationRequested: Boolean = false,
    val modeBeforeParticipation: String? = null,
    val modeAfterParticipation: String? = null,
    val requestAccepted: Boolean? = null,
)

class AudioDiagnosticObserver(
    private val audioManager: AudioManager,
    private val callbackExecutor: Executor,
) {
    var snapshot by mutableStateOf(DiagnosticSnapshot.Empty)
        private set

    var experiment by mutableStateOf(EarpieceExperiment())
        private set

    val events = mutableStateListOf<String>()

    private var started = false
    private var routingActionInProgress = false
    private var modeParticipationActive = false
    private val communicationDeviceListener = AudioManager.OnCommunicationDeviceChangedListener {
        snapshot("Communication device callback")
    }
    private val audioDeviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>) {
            snapshot("Audio device callback: ${addedDevices.size} added")
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>) {
            snapshot("Audio device callback: ${removedDevices.size} removed")
        }
    }

    fun start() {
        if (started) return
        started = true
        audioManager.addOnCommunicationDeviceChangedListener(
            callbackExecutor,
            communicationDeviceListener,
        )
        audioManager.registerAudioDeviceCallback(audioDeviceCallback, Handler(Looper.getMainLooper()))
        snapshot("Baseline")
    }

    fun stop() {
        if (!started) return
        clearExperiment("Activity destroyed", ExperimentState.CLEARED)
        audioManager.removeOnCommunicationDeviceChangedListener(communicationDeviceListener)
        audioManager.unregisterAudioDeviceCallback(audioDeviceCallback)
        started = false
    }

    fun snapshot(reason: String) {
        val observed = DiagnosticSnapshot(
            mode = audioModeName(audioManager.mode),
            communicationDevice = audioManager.communicationDevice?.toObservedDevice(),
            availableCommunicationDevices = audioManager.availableCommunicationDevices
                .map(AudioDeviceInfo::toObservedDevice),
            speakerphoneState = observedSpeakerphoneState(),
        )
        val changes = describeChanges(snapshot, observed)
        snapshot = observed
        addEvent("$reason — $changes")
        if (!routingActionInProgress) evaluateExperimentTrigger()
    }

    fun armEarpieceTest() {
        if (experiment.requestAttempted || experiment.armed) {
            clearExperiment("Re-arm requested", ExperimentState.CLEARED)
        }
        experiment = EarpieceExperiment(state = ExperimentState.ARMED, armed = true)
        addEvent("Earpiece test armed — waiting for qualifying Android audio state")
        snapshot("Arm snapshot")
    }

    fun disarmAndClear() {
        clearExperiment("User disarmed experiment", ExperimentState.CLEARED)
    }

    fun report(): String = buildDiagnosticReport(
        timestamp = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        experiment = experiment,
        snapshot = snapshot,
        events = events,
    )

    private fun addEvent(message: String) {
        events.add(0, "${LocalTime.now().format(TIME_FORMAT)}  $message")
        while (events.size > MAX_EVENTS) events.removeAt(events.lastIndex)
    }

    private fun evaluateExperimentTrigger() {
        if (!experiment.armed) return

        val mode = audioManager.mode
        if (mode.isTelephonyOrSystemPriorityMode()) {
            clearExperiment(
                reason = "Blocked by system/telephony-priority mode ${audioModeName(mode)}",
                finalState = ExperimentState.BLOCKED,
            )
            return
        }

        if (experiment.requestAttempted && mode != AudioManager.MODE_IN_COMMUNICATION) {
            clearExperiment(
                reason = "Observed communication session left MODE_IN_COMMUNICATION",
                finalState = ExperimentState.CLEARED,
            )
            return
        }

        if (experiment.requestAttempted || mode != AudioManager.MODE_IN_COMMUNICATION) return
        val currentDevice = audioManager.communicationDevice
        if (currentDevice?.type != AudioDeviceInfo.TYPE_BUILTIN_SPEAKER) return
        val earpiece = audioManager.availableCommunicationDevices.firstOrNull {
            it.type == AudioDeviceInfo.TYPE_BUILTIN_EARPIECE
        } ?: return

        val target = earpiece.toObservedDevice()
        val modeBeforeParticipation = audioManager.mode
        addEvent("Qualifying trigger observed — complete pre-change state: ${snapshot.inlineDescription()}")
        // Set both guards before changing Android state: even a synchronous callback cannot cause a retry.
        experiment = experiment.copy(
            state = ExperimentState.REQUEST_ATTEMPTED,
            requestAttempted = true,
            selectedTarget = target,
            modeParticipationRequested = true,
            modeBeforeParticipation = audioModeName(modeBeforeParticipation),
        )
        routingActionInProgress = true
        addEvent("Private Audio mode change requested — AudioManager.mode=MODE_IN_COMMUNICATION")
        modeParticipationActive = true
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        val modeAfterParticipation = audioManager.mode
        experiment = experiment.copy(modeAfterParticipation = audioModeName(modeAfterParticipation))
        addEvent(
            "Post-mode-request state — requested=MODE_IN_COMMUNICATION; " +
                "Android-reported mode=${audioModeName(modeAfterParticipation)}; ${currentStateDescription()}",
        )
        val accepted = audioManager.setCommunicationDevice(earpiece)
        routingActionInProgress = false
        experiment = experiment.copy(requestAccepted = accepted)
        addEvent("One-shot setCommunicationDevice attempt — request accepted=$accepted; target=${target.shortName()}")
        snapshot("Post-request observation")
    }

    private fun clearExperiment(reason: String, finalState: ExperimentState) {
        routingActionInProgress = true
        audioManager.clearCommunicationDevice()
        addEvent("clearCommunicationDevice called — $reason")
        if (modeParticipationActive) {
            // MODE_NORMAL relinquishes this process's mode ownership. Android may then expose the
            // mode still owned by an external communication or telephony session.
            modeParticipationActive = false
            audioManager.mode = AudioManager.MODE_NORMAL
            addEvent(
                "Private Audio mode participation relinquished with MODE_NORMAL — " +
                    "pre-participation mode=${experiment.modeBeforeParticipation}; " +
                    "Android-reported mode=${audioModeName(audioManager.mode)}",
            )
        }
        routingActionInProgress = false
        experiment = experiment.copy(state = finalState, armed = false)
        snapshot("Post-cleanup observation")
    }

    private fun currentStateDescription() =
        "communication device=${audioManager.communicationDevice?.toObservedDevice().reportDescription()}; " +
            "speakerphone=${observedSpeakerphoneState()}"

    @Suppress("DEPRECATION")
    private fun observedSpeakerphoneState() =
        if (audioManager.isSpeakerphoneOn) "On (directly observed)" else "Off (directly observed)"
}

internal fun buildDiagnosticReport(
    timestamp: String,
    experiment: EarpieceExperiment,
    snapshot: DiagnosticSnapshot,
    events: List<String>,
) = buildString {
    appendLine("PRIVATE AUDIO — DIAGNOSTIC REPORT")
    appendLine("Timestamp: $timestamp")
    appendLine()
    appendLine("EARPIECE EXPERIMENT")
    appendLine("Experiment state: ${experiment.state.label}")
    appendLine("Armed: ${experiment.armed}")
    appendLine("Routing request attempted: ${experiment.requestAttempted}")
    appendLine("Private Audio mode change requested: ${experiment.modeParticipationRequested}")
    appendLine("Mode before participation: ${experiment.modeBeforeParticipation ?: "Not attempted"}")
    appendLine("Mode after participation request: ${experiment.modeAfterParticipation ?: "Not attempted"}")
    appendLine("Selected target: ${experiment.selectedTarget.reportDescription()}")
    appendLine("setCommunicationDevice return value: ${experiment.requestAccepted ?: "Not attempted"}")
    appendLine("Audible ChatGPT audio moved to earpiece: UNKNOWN — requires human physical-device confirmation")
    appendLine()
    appendLine("CURRENT STATE")
    appendLine("AudioManager mode: ${snapshot.mode}")
    appendLine("Communication device: ${snapshot.communicationDevice.reportDescription()}")
    appendLine("Speakerphone: ${snapshot.speakerphoneState}")
    appendLine()
    appendLine("AVAILABLE COMMUNICATION DEVICES")
    if (snapshot.availableCommunicationDevices.isEmpty()) {
        appendLine("None reported by Android")
    } else {
        snapshot.availableCommunicationDevices.forEachIndexed { index, device ->
            appendLine("${index + 1}. ${device.reportDescription()}")
        }
    }
    appendLine()
    appendLine("OBSERVATION / EVENT LOG")
    if (events.isEmpty()) {
        append("No observations recorded")
    } else {
        append(events.joinToString(separator = "\n"))
    }
}

private fun DiagnosticSnapshot.inlineDescription() =
    "mode=$mode; communication device=${communicationDevice.reportDescription()}; " +
        "speakerphone=$speakerphoneState; available devices=" +
        availableCommunicationDevices.joinToString(prefix = "[", postfix = "]") { it.reportDescription() }

private fun Int.isTelephonyOrSystemPriorityMode() = when (this) {
    AudioManager.MODE_RINGTONE,
    AudioManager.MODE_IN_CALL,
    AudioManager.MODE_CALL_SCREENING,
    AudioManager.MODE_CALL_REDIRECT,
    AudioManager.MODE_COMMUNICATION_REDIRECT -> true
    else -> false
}

internal fun describeChanges(previous: DiagnosticSnapshot, current: DiagnosticSnapshot): String {
    if (previous == DiagnosticSnapshot.Empty) return "state recorded"
    val changes = buildList {
        if (previous.mode != current.mode) add("mode: ${previous.mode} → ${current.mode}")
        if (previous.communicationDevice != current.communicationDevice) {
            add("communication device: ${previous.communicationDevice.shortName()} → ${current.communicationDevice.shortName()}")
        }
        if (previous.availableCommunicationDevices != current.availableCommunicationDevices) {
            add("available devices changed")
        }
        if (previous.speakerphoneState != current.speakerphoneState) {
            add("speakerphone: ${previous.speakerphoneState} → ${current.speakerphoneState}")
        }
    }
    return changes.joinToString().ifEmpty { "no observable change" }
}

private fun AudioDeviceInfo.toObservedDevice() = ObservedDevice(
    id = id,
    type = audioDeviceTypeName(type),
    productName = productName.toString().ifBlank { "Unavailable" },
)

private fun ObservedDevice?.shortName() = this?.let { "${it.type} (${it.productName})" } ?: "None reported"

private fun ObservedDevice?.reportDescription() = this?.let {
    "type=${it.type}; product=${it.productName}; Android device ID=${it.id}"
} ?: "None reported by Android"

internal fun audioModeName(mode: Int) = when (mode) {
    AudioManager.MODE_NORMAL -> "MODE_NORMAL"
    AudioManager.MODE_RINGTONE -> "MODE_RINGTONE"
    AudioManager.MODE_IN_CALL -> "MODE_IN_CALL"
    AudioManager.MODE_IN_COMMUNICATION -> "MODE_IN_COMMUNICATION"
    AudioManager.MODE_CALL_SCREENING -> "MODE_CALL_SCREENING"
    AudioManager.MODE_CALL_REDIRECT -> "MODE_CALL_REDIRECT"
    AudioManager.MODE_COMMUNICATION_REDIRECT -> "MODE_COMMUNICATION_REDIRECT"
    else -> "Unknown value ($mode)"
}

internal fun audioDeviceTypeName(type: Int) = when (type) {
    AudioDeviceInfo.TYPE_BUILTIN_EARPIECE -> "Built-in earpiece"
    AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> "Built-in speaker"
    AudioDeviceInfo.TYPE_BUILTIN_SPEAKER_SAFE -> "Built-in speaker (safe)"
    AudioDeviceInfo.TYPE_WIRED_HEADSET -> "Wired headset"
    AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> "Wired headphones"
    AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> "Bluetooth SCO"
    AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> "Bluetooth A2DP"
    AudioDeviceInfo.TYPE_BLE_HEADSET -> "Bluetooth LE headset"
    AudioDeviceInfo.TYPE_BLE_SPEAKER -> "Bluetooth LE speaker"
    AudioDeviceInfo.TYPE_BLE_BROADCAST -> "Bluetooth LE broadcast"
    AudioDeviceInfo.TYPE_USB_DEVICE -> "USB device"
    AudioDeviceInfo.TYPE_USB_HEADSET -> "USB headset"
    AudioDeviceInfo.TYPE_USB_ACCESSORY -> "USB accessory"
    AudioDeviceInfo.TYPE_HEARING_AID -> "Hearing aid"
    AudioDeviceInfo.TYPE_HDMI -> "HDMI"
    AudioDeviceInfo.TYPE_HDMI_ARC -> "HDMI ARC"
    AudioDeviceInfo.TYPE_HDMI_EARC -> "HDMI eARC"
    AudioDeviceInfo.TYPE_DOCK -> "Dock"
    AudioDeviceInfo.TYPE_LINE_ANALOG -> "Analog line"
    AudioDeviceInfo.TYPE_LINE_DIGITAL -> "Digital line"
    AudioDeviceInfo.TYPE_AUX_LINE -> "Aux line"
    AudioDeviceInfo.TYPE_IP -> "IP device"
    AudioDeviceInfo.TYPE_BUS -> "Audio bus"
    AudioDeviceInfo.TYPE_REMOTE_SUBMIX -> "Remote submix"
    AudioDeviceInfo.TYPE_TELEPHONY -> "Telephony"
    AudioDeviceInfo.TYPE_FM -> "FM"
    else -> "Unknown type ($type)"
}

private val TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
private const val MAX_EVENTS = 100
