package app.privateaudio.diagnostic

import android.app.ActivityManager
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioPlaybackConfiguration
import android.media.AudioRecordingConfiguration
import android.media.AudioTrack
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.os.SystemClock
import app.privateaudio.BuildConfig
import app.privateaudio.PrivateAudioState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean

data class ObservedDevice(
    val id: Int,
    val type: String,
    val productName: String,
)

internal data class DiagnosticEnvironment(
    val versionName: String,
    val versionCode: Long,
    val androidRelease: String,
    val apiLevel: Int,
    val manufacturer: String,
    val model: String,
    val product: String,
) {
    companion object {
        fun from(context: Context): DiagnosticEnvironment {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            return DiagnosticEnvironment(
                versionName = info.versionName ?: BuildConfig.VERSION_NAME,
                versionCode = info.longVersionCode,
                androidRelease = Build.VERSION.RELEASE,
                apiLevel = Build.VERSION.SDK_INT,
                manufacturer = Build.MANUFACTURER,
                model = Build.MODEL,
                product = Build.PRODUCT,
            )
        }
    }
}

data class DiagnosticSnapshot(
    val mode: String,
    val communicationDevice: ObservedDevice?,
    val availableCommunicationDevices: List<ObservedDevice>,
    val speakerphoneState: String,
    val timestamp: String = "Unavailable — not observed yet",
    val processId: Int = Process.myPid(),
    val userId: Int = Process.myUid(),
    val lifecycleState: String = "Unknown",
    val activePlaybackConfigurations: List<ObservedPlayback> = emptyList(),
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

data class ObservedPlayback(
    val usage: String,
    val contentType: String,
    val flags: String = "FLAGS_NONE (0x0)",
    val allowedCapturePolicy: String,
    val playerState: String = "Not exposed by the public AudioPlaybackConfiguration API",
    val playerIdentity: String = "Not exposed by the public AudioPlaybackConfiguration API",
    val device: ObservedDevice?,
)

data class ObservedRecording(
    val audioSource: String,
    val clientSessionId: String,
    val clientFormat: String,
    val device: ObservedDevice?,
    val clientSilenced: Boolean?,
) {
    internal fun reportDescription() =
        "source=$audioSource; client session ID=$clientSessionId; client format=$clientFormat; " +
            "input device=${device.reportDescription()}; client silenced=${clientSilenced?.toString() ?: "Not exposed"}"
}

data class RecordingContext(
    val controllerEnabled: Boolean,
    val mode: String,
    val communicationDevice: ObservedDevice?,
    val speakerphoneState: String,
    val protectedCycleRequested: Boolean,
    val triggerOrigin: TriggerOrigin?,
    val silentTrackPlayState: String,
    val routingGeneration: Long?,
)

data class RecordingTraceEntry(
    val timestamp: String,
    val elapsedRealtimeNanos: Long,
    val description: String,
    val recordings: List<ObservedRecording>,
    val context: RecordingContext,
)

internal data class RecordingChanges(val descriptions: List<String>)

internal fun recordingChanges(
    previous: List<ObservedRecording>,
    current: List<ObservedRecording>,
): RecordingChanges {
    if (previous == current) return RecordingChanges(emptyList())
    val descriptions = mutableListOf<String>()
    if (previous.isEmpty() && current.isNotEmpty()) descriptions += "RECORDING appeared"
    if (previous.isNotEmpty() && current.isEmpty()) descriptions += "RECORDING disappeared"
    if (previous.size != current.size) descriptions += "recording count changed ${previous.size} → ${current.size}"
    if (previous.isNotEmpty() && current.isNotEmpty()) {
        descriptions += "RECORDING configuration changed"
        if (previous.map { it.audioSource } != current.map { it.audioSource }) descriptions += "audio source changed"
        if (previous.map { it.device } != current.map { it.device }) descriptions += "input device changed"
        if (previous.map { it.clientSilenced } != current.map { it.clientSilenced }) descriptions += "client silenced changed"
    }
    return RecordingChanges(descriptions)
}

data class RecordingStartupObservation(
    val generation: Long,
    val routingTriggerElapsedRealtimeNanos: Long,
    val atRoutingTrigger: List<ObservedRecording>,
    var atPostSilentTrackStart: List<ObservedRecording>? = null,
    var atPostModeRequest: List<ObservedRecording>? = null,
    var atPostRoutingRequest: List<ObservedRecording>? = null,
    var atFirstEarpiece: List<ObservedRecording>? = null,
    var firstEarpieceElapsedRealtimeNanos: Long? = null,
    var transitionObserved: Boolean = false,
    var disappearanceObserved: Boolean = false,
    var reappearanceObserved: Boolean = false,
    var silencedTransitionObserved: Boolean = false,
    var inputDeviceTransitionObserved: Boolean = false,
)

enum class ExperimentState(val label: String) {
    IDLE("IDLE"),
    ARMED("ARMED"),
    REQUEST_ATTEMPTED("REQUEST ATTEMPTED"),
    CLEARED("CLEARED"),
    BLOCKED("BLOCKED / FAILED"),
}

enum class TriggerOrigin {
    COMMUNICATION,
    ASSISTANT,
    BROWSER_COMMUNICATION,
}

data class EarpieceExperiment(
    val state: ExperimentState = ExperimentState.IDLE,
    val armed: Boolean = false,
    val requestAttempted: Boolean = false,
    val selectedTarget: ObservedDevice? = null,
    val modeBeforeParticipation: String? = null,
    val silentTrackCreated: Boolean = false,
    val silentTrackStarted: Boolean = false,
    val silentTrackSampleRate: Int? = null,
    val silentTrackBufferBytes: Int? = null,
    val silentTrackPlayState: String = "Not created",
    val activeVoiceCommunicationPlaybackObserved: Boolean = false,
    val silentTrackActiveBeforeModeRequest: Boolean = false,
    val modeRequestIssuedAfterPlaybackActive: Boolean = false,
    val explicitModeRequestInvoked: Boolean = false,
    val modeRequestTimestamp: String? = null,
    val modeRequestThread: String? = null,
    val modeImmediatelyBeforeRequest: String? = null,
    val modeImmediatelyAfterRequest: String? = null,
    val modeRequestException: String? = null,
    val modeInCommunicationObserved: Boolean = false,
    val earpieceRequestAfterExplicitModeRequest: Boolean = false,
    val requestAccepted: Boolean? = null,
    val attempts: List<RoutingAttempt> = emptyList(),
    val earpieceReportedDuringSession: Boolean = false,
    val revertedToSpeaker: Boolean = false,
    val shortObservation: DiagnosticSnapshot? = null,
    val preOwnership: DiagnosticSnapshot? = null,
    val postModeOwnership: DiagnosticSnapshot? = null,
    val postRoutingRequest: DiagnosticSnapshot? = null,
    val postSilentTrackStart: DiagnosticSnapshot? = null,
    val silentTrackCleanupCompleted: Boolean = false,
    val externalVoicePlaybackDeviceAfterRequest: ObservedDevice? = null,
    val assistantQualifyingPlaybackCount: Int = 0,
    val browserQualifyingPlaybackCount: Int = 0,
    val triggerOrigin: TriggerOrigin? = null,
    val modeBeforeAssistantParticipation: String? = null,
    val preparedTrackCreated: Boolean = false,
    val preparedTrackPrefilled: Boolean = false,
    val prefillBytesWritten: Int? = null,
    val prefillCompletedTimestamp: String? = null,
    val preparationCompletedTimestamp: String? = null,
    val preparedTrackState: String = "Not prepared",
    val preparedTrackActivePlaybackWhileWaiting: Boolean = false,
    val routingTriggerTimestamp: String? = null,
    val preparedTrackReused: Boolean = false,
    val playInvocationTimestamp: String? = null,
    val playReturnedTimestamp: String? = null,
    val writerStartRequestedTimestamp: String? = null,
    val firstSuccessfulWriteTimestamp: String? = null,
    val playingTimestamp: String? = null,
    val modeObservedTimestamp: String? = null,
    val deviceRequestStartedTimestamp: String? = null,
    val deviceRequestReturnedTimestamp: String? = null,
    val earpieceFirstObservedTimestamp: String? = null,
    val triggerToPlayingElapsedMs: Long? = null,
    val triggerToPlayInvocationElapsedMs: Long? = null,
    val playCallDurationMs: Long? = null,
    val playingToModeObservedElapsedMs: Long? = null,
    val modeObservedToDeviceRequestReturnElapsedMs: Long? = null,
    val deviceRequestToEarpieceObservedElapsedMs: Long? = null,
    val triggerToEarpieceObservedElapsedMs: Long? = null,
    val startupTimingGeneration: Long? = null,
    val fallbackCreationUsed: Boolean = false,
)

data class CompletedRoutingCycle(
    val experiment: EarpieceExperiment,
    val finalCleanupObservation: DiagnosticSnapshot,
    val completionReason: String,
    val completedAt: String,
)

data class RoutingAttempt(
    val number: Int,
    val timestamp: String,
    val trigger: String,
    val mode: String,
    val deviceBefore: ObservedDevice?,
    val accepted: Boolean,
    val deviceImmediatelyAfter: ObservedDevice?,
    val speakerphoneImmediatelyAfter: String,
)

data class AssistantEarlyRouteStatus(
    val featureEnabled: Boolean = false,
    val active: Boolean = false,
    val voiceRecognitionPresent: Boolean = false,
    val assistantSonificationObserved: Boolean = false,
    val startedAt: String? = null,
    val preparedTrackReused: Boolean = false,
    val prefillCompleted: Boolean = false,
    val playInvocationAt: String? = null,
    val playReturnedAt: String? = null,
    val playingAt: String? = null,
    val playDurationMs: Long? = null,
    val modeDuringEarlyPlaying: String? = null,
    val communicationDeviceDuringEarlyPlaying: ObservedDevice? = null,
    val explicitEarlySetModeAttempted: Boolean = false,
    val explicitEarlySetCommunicationDeviceAttempted: Boolean = false,
    val assistantSpeechArrivalAt: String? = null,
    val playingToAssistantSpeechHeadStartMs: Long? = null,
    val promoted: Boolean = false,
    val promotionAt: String? = null,
    val recordingBeforePreArm: List<ObservedRecording>? = null,
    val recordingAfterPlaying: List<ObservedRecording>? = null,
    val recordingAtAssistantSpeech: List<ObservedRecording>? = null,
    val cleanupCompleted: Boolean = false,
    val cleanupReason: String? = null,
    val generation: Long = 0,
    val timeoutDeadline: String? = null,
    val playingElapsedRealtimeNanos: Long? = null,
)

private data class StartupTiming(
    val generation: Long,
    val triggerNanos: Long,
    var playInvocationNanos: Long? = null,
    var playReturnedNanos: Long? = null,
    var playingObservedNanos: Long? = null,
    var modeRequestStartedNanos: Long? = null,
    var modeObservedNanos: Long? = null,
    var deviceRequestStartedNanos: Long? = null,
    var deviceRequestReturnedNanos: Long? = null,
    var earpieceObservedNanos: Long? = null,
)

class AudioDiagnosticObserver(
    private val context: Context,
    private val audioManager: AudioManager,
    private val callbackExecutor: Executor,
    private val onEvidenceChanged: (String) -> Unit = {},
) {
    var snapshot by mutableStateOf(DiagnosticSnapshot.Empty)
        private set

    var experiment by mutableStateOf(EarpieceExperiment())
        private set

    var lastCompletedExperiment by mutableStateOf<CompletedRoutingCycle?>(null)
        private set

    var assistantEarlyRoute by mutableStateOf(AssistantEarlyRouteStatus())
        private set

    val events = mutableStateListOf<String>()

    private val startupAudioTrace = ArrayDeque<String>()
    private var previousPlaybackObservation: List<ObservedPlayback> = emptyList()
    private var previousPlaybackControllerState: String? = null
    private var previousQualifierCounts: Triple<Int, Int, Int>? = null
    private var redundantPlaybackCallbacksSuppressed = 0
    private var eventEntriesDropped = 0
    private var startupTraceEntriesDropped = 0

    val isRoutingActionInProgress: Boolean
        get() = routingActionInProgress

    private var started = false
    private var controllerEnabled = false
    private var currentAssistantQualifyingPlaybackCount = 0
    private var currentBrowserQualifyingPlaybackCount = 0
    private var playbackCallbackRegistered = false
    private var recordingCallbackRegistered = false
    private var currentRecordingConfigurations: List<ObservedRecording> = emptyList()
    private val recordingTrace = ArrayDeque<RecordingTraceEntry>()
    private var recordingTraceEntriesDropped = 0
    private var redundantRecordingCallbacksSuppressed = 0
    private var recordingStartupObservation: RecordingStartupObservation? = null
    private var lastRecordingStartupObservation: RecordingStartupObservation? = null
    private var cycleGeneration = 0L
    private var externalContributionEstablished = false
    private var activeEvidenceRecorded = false
    private var routingActionInProgress = false
    private var modeParticipationActive = false
    private var silentTrack: AudioTrack? = null
    private var preparedSilentTrack = false
    private var startupTiming: StartupTiming? = null
    private var silentWriterThread: Thread? = null
    private val silentWriterRunning = AtomicBoolean(false)
    private val firstWriterWriteRecorded = AtomicBoolean(false)
    private val observationHandler = Handler(Looper.getMainLooper())
    private var pendingObservation: Runnable? = null
    private var pendingEndConfirmation: Runnable? = null
    private var pendingSessionLinger: Runnable? = null
    private var pendingAssistantEarlyRouteTimeout: Runnable? = null
    private var assistantEarlyRouteGeneration = 0L
    private var lingerGeneration = 0L
    private var baseline: DiagnosticSnapshot? = null
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
    private val playbackCallback = object : AudioManager.AudioPlaybackCallback() {
        override fun onPlaybackConfigChanged(configs: MutableList<AudioPlaybackConfiguration>) {
            handlePlaybackConfigurations(configs)
        }
    }
    private val recordingCallback = object : AudioManager.AudioRecordingCallback() {
        override fun onRecordingConfigChanged(configs: MutableList<AudioRecordingConfiguration>) {
            handleRecordingConfigurations(configs.map(AudioRecordingConfiguration::toObservedRecording))
        }
    }

    fun start() {
        if (started) return
        started = true
        registerRecordingCallback()
        audioManager.addOnCommunicationDeviceChangedListener(
            callbackExecutor,
            communicationDeviceListener,
        )
        audioManager.registerAudioDeviceCallback(audioDeviceCallback, Handler(Looper.getMainLooper()))
        snapshot("Baseline")
        baseline = snapshot
        handleRecordingConfigurations(readActiveRecordingConfigurations())
    }

    fun stop(reason: String) {
        if (!started) return
        controllerEnabled = false
        invalidatePendingControllerWork()
        unregisterRecordingCallback()
        unregisterPlaybackCallback()
        if (experiment.requestAttempted) clearExperiment(reason, ExperimentState.CLEARED)
        else {
            cleanupAssistantEarlyPreArm(reason)
            releasePreparedSilentTrack(reason)
        }
        audioManager.removeOnCommunicationDeviceChangedListener(communicationDeviceListener)
        audioManager.unregisterAudioDeviceCallback(audioDeviceCallback)
        started = false
    }

    fun snapshot(reason: String) {
        val observed = DiagnosticSnapshot(
            timestamp = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
            lifecycleState = processLifecycleState(),
            mode = audioModeName(audioManager.mode),
            communicationDevice = audioManager.communicationDevice?.toObservedDevice(),
            availableCommunicationDevices = audioManager.availableCommunicationDevices
                .map(AudioDeviceInfo::toObservedDevice),
            speakerphoneState = observedSpeakerphoneState(),
            activePlaybackConfigurations = activePlaybackConfigurations(),
        )
        val changes = describeChanges(snapshot, observed)
        snapshot = observed
        addEvent("$reason — $changes")
        if (assistantEarlyRoute.active) abortAssistantEarlyPreArmIfContextLost(reason)
        if (abortAssistantLingerIfContextLost(reason)) return
        observeExperimentOutcome(observed, reason)
        if (!routingActionInProgress) prepareSilentCommunicationTrack()
        if (!routingActionInProgress) evaluateExperimentTrigger()
        onEvidenceChanged(reason)
    }

    fun enableController() {
        if (controllerEnabled) return
        controllerEnabled = true
        cycleGeneration++
        experiment = EarpieceExperiment(state = ExperimentState.ARMED, armed = true)
        audioManager.registerAudioPlaybackCallback(playbackCallback, observationHandler)
        playbackCallbackRegistered = true
        addEvent("Controller ON — clean waiting; playback observation registered")
        snapshot("Controller waiting snapshot")
        prepareSilentCommunicationTrack()
        handlePlaybackConfigurations(audioManager.activePlaybackConfigurations)
    }

    fun disableController() {
        controllerEnabled = false
        invalidatePendingControllerWork()
        unregisterPlaybackCallback()
        addEvent("Controller OFF — pending detection invalidated")
        if (experiment.requestAttempted) clearExperiment("Power OFF", ExperimentState.CLEARED)
        else {
            cleanupAssistantEarlyPreArm("Power OFF")
            releasePreparedSilentTrack("Power OFF")
        }
    }

    fun updateAssistantEarlyRouteEnabled(enabled: Boolean) {
        if (assistantEarlyRoute.featureEnabled == enabled) return
        assistantEarlyRoute = assistantEarlyRoute.copy(featureEnabled = enabled)
        addEvent("Assistant early route preference ${if (enabled) "enabled" else "disabled"}")
        if (!controllerEnabled || experiment.requestAttempted) return
        if (enabled) handlePlaybackConfigurations(audioManager.activePlaybackConfigurations)
        else cleanupAssistantEarlyPreArm("Preference disabled")
        onEvidenceChanged("Assistant early route preference changed")
    }

    fun recordLifecycleEvent(message: String) {
        addEvent(message)
    }

    internal fun report(supportSummary: DiagnosticsSummary): String = buildDiagnosticReport(
        timestamp = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        experiment = experiment,
        lastCompletedExperiment = lastCompletedExperiment,
        snapshot = snapshot,
        events = events,
        packageName = context.packageName,
        baseline = baseline,
        startupAudioTrace = startupAudioTrace.toList(),
        assistantQualifyingPlaybackCount = currentAssistantQualifyingPlaybackCount,
        browserQualifyingPlaybackCount = currentBrowserQualifyingPlaybackCount,
        environment = DiagnosticEnvironment.from(context),
        eventEntriesDropped = eventEntriesDropped,
        startupTraceEntriesDropped = startupTraceEntriesDropped,
        redundantPlaybackCallbacksSuppressed = redundantPlaybackCallbacksSuppressed,
        assistantEarlyRoute = assistantEarlyRoute,
        recordingCallbackRegistered = recordingCallbackRegistered,
        currentRecordingConfigurations = currentRecordingConfigurations,
        recordingTrace = recordingTrace.toList(),
        recordingTraceEntriesDropped = recordingTraceEntriesDropped,
        redundantRecordingCallbacksSuppressed = redundantRecordingCallbacksSuppressed,
        recordingStartupObservation = recordingStartupObservation ?: lastRecordingStartupObservation,
        supportSummary = supportSummary,
    )

    private fun registerRecordingCallback() {
        if (recordingCallbackRegistered) return
        audioManager.registerAudioRecordingCallback(recordingCallback, observationHandler)
        recordingCallbackRegistered = true
    }

    private fun unregisterRecordingCallback() {
        if (!recordingCallbackRegistered) return
        audioManager.unregisterAudioRecordingCallback(recordingCallback)
        recordingCallbackRegistered = false
    }

    private fun readActiveRecordingConfigurations(): List<ObservedRecording> =
        runCatching { audioManager.activeRecordingConfigurations.map(AudioRecordingConfiguration::toObservedRecording) }
            .getOrDefault(emptyList())

    private fun handleRecordingConfigurations(current: List<ObservedRecording>) {
        val changes = recordingChanges(currentRecordingConfigurations, current)
        if (changes.descriptions.isEmpty()) {
            redundantRecordingCallbacksSuppressed++
            return
        }
        val previous = currentRecordingConfigurations
        currentRecordingConfigurations = current
        if (assistantEarlyRoute.active && !voiceRecognitionPresent()) {
            cleanupAssistantEarlyPreArm("VOICE_RECOGNITION disappeared")
        } else if (controllerEnabled && assistantEarlyRoute.featureEnabled && !routingActionInProgress) {
            handlePlaybackConfigurations(audioManager.activePlaybackConfigurations)
        }
        val startup = recordingStartupObservation?.takeIf { it.generation == cycleGeneration }
        if (startup != null) {
            startup.transitionObserved = true
            startup.disappearanceObserved = startup.disappearanceObserved || (previous.isNotEmpty() && current.isEmpty())
            startup.reappearanceObserved = startup.reappearanceObserved || (previous.isEmpty() && current.isNotEmpty() &&
                startup.atRoutingTrigger.isNotEmpty())
            startup.silencedTransitionObserved = startup.silencedTransitionObserved ||
                changes.descriptions.contains("client silenced changed")
            startup.inputDeviceTransitionObserved = startup.inputDeviceTransitionObserved ||
                changes.descriptions.contains("input device changed")
        }
        recordingTrace.addLast(
            RecordingTraceEntry(
                timestamp = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos(),
                description = changes.descriptions.joinToString("; "),
                recordings = current,
                context = recordingContext(),
            ),
        )
        while (recordingTrace.size > MAX_RECORDING_TRACE_EVENTS) {
            recordingTrace.removeFirst()
            recordingTraceEntriesDropped++
        }
        onEvidenceChanged("Public recording configuration changed")
    }

    private fun recordingContext() = RecordingContext(
        controllerEnabled = controllerEnabled,
        mode = audioModeName(audioManager.mode),
        communicationDevice = audioManager.communicationDevice?.toObservedDevice(),
        speakerphoneState = observedSpeakerphoneState(),
        protectedCycleRequested = experiment.requestAttempted,
        triggerOrigin = experiment.triggerOrigin,
        silentTrackPlayState = audioTrackPlayStateName(silentTrack?.playState ?: AudioTrack.PLAYSTATE_STOPPED),
        routingGeneration = experiment.startupTimingGeneration,
    )

    internal fun diagnosticsSummary(
        privateAudioEnabled: Boolean,
        privateAudioState: PrivateAudioState,
        proximitySupported: Boolean,
        overlayPermissionGranted: Boolean,
    ): DiagnosticsSummary = projectDiagnosticsSummary(
        snapshot = snapshot,
        lastCompletedCycle = lastCompletedExperiment,
        privateAudioEnabled = privateAudioEnabled,
        privateAudioState = privateAudioState,
        proximitySupported = proximitySupported,
        overlayPermissionGranted = overlayPermissionGranted,
    )

    private fun addEvent(message: String) {
        val timestamp = LocalTime.now().format(TIME_FORMAT)
        events.add(0, "$timestamp  $message")
        while (events.size > MAX_EVENTS) {
            events.removeAt(events.lastIndex)
            eventEntriesDropped++
        }
        addStartupTrace("$timestamp  $message")
    }

    private fun addStartupTrace(entry: String) {
        startupAudioTrace.addLast(entry)
        while (startupAudioTrace.size > MAX_STARTUP_TRACE_EVENTS) {
            startupAudioTrace.removeFirst()
            startupTraceEntriesDropped++
        }
    }

    private fun evaluateExperimentTrigger() {
        if (!controllerEnabled || !experiment.armed) return

        val mode = audioManager.mode
        if (mode.isTelephonyOrSystemPriorityMode()) {
            if (preparedSilentTrack) {
                releasePreparedSilentTrack("System/telephony-priority takeover")
            }
            if (assistantEarlyRoute.active) {
                cleanupAssistantEarlyPreArm("Blocked by system/telephony-priority mode ${audioModeName(mode)}")
                return
            }
            clearExperiment(
                reason = "Blocked by system/telephony-priority mode ${audioModeName(mode)}",
                finalState = ExperimentState.BLOCKED,
            )
            return
        }

        if (experiment.attempts.isNotEmpty()) return
        val configs = audioManager.activePlaybackConfigurations
        val normalTrigger = mode == AudioManager.MODE_IN_COMMUNICATION &&
            qualifyingPlaybackCount(configs) > (if (assistantEarlyRoute.active) 1 else 0) &&
            audioManager.communicationDevice?.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
        val assistantCount = assistantQualifyingPlaybackCount(configs)
        currentAssistantQualifyingPlaybackCount = assistantCount
        val assistantTrigger = assistantCount > 0
        val browserCount = browserQualifyingPlaybackCount(configs)
        currentBrowserQualifyingPlaybackCount = browserCount
        val browserTrigger = mode == AudioManager.MODE_IN_COMMUNICATION &&
            browserCount > 0 &&
            audioManager.communicationDevice?.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
        if (!normalTrigger && !assistantTrigger && !browserTrigger) return
        val earpiece = audioManager.availableCommunicationDevices.firstOrNull {
            it.type == AudioDeviceInfo.TYPE_BUILTIN_EARPIECE
        } ?: return

        if (assistantTrigger && !normalTrigger) {
            addEvent("Assistant speech detected — starting protected POC-5 route")
        }
        val triggerOrigin = when {
            assistantTrigger && !normalTrigger -> TriggerOrigin.ASSISTANT
            browserTrigger && !normalTrigger -> TriggerOrigin.BROWSER_COMMUNICATION
            else -> TriggerOrigin.COMMUNICATION
        }
        if (triggerOrigin == TriggerOrigin.BROWSER_COMMUNICATION) {
            addEvent("Browser communication detected — starting protected POC-5 route")
        }
        startProtectedPoc5Probe(earpiece, triggerOrigin, assistantCount, browserCount)
    }

    private fun startProtectedPoc5Probe(
        earpiece: AudioDeviceInfo,
        triggerOrigin: TriggerOrigin,
        assistantCount: Int,
        browserCount: Int,
    ) {
        val reuseEarlyTrack = triggerOrigin == TriggerOrigin.ASSISTANT && isAssistantEarlyPreArmHealthy()
        if (assistantEarlyRoute.active && !reuseEarlyTrack) {
            cleanupAssistantEarlyPreArm("incompatible or unhealthy protected trigger", reEvaluatePlayback = false)
        }
        val routingTriggerTimestamp = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val timingGeneration = cycleGeneration
        startupTiming = StartupTiming(timingGeneration, SystemClock.elapsedRealtimeNanos())
        recordingStartupObservation = RecordingStartupObservation(
            generation = timingGeneration,
            routingTriggerElapsedRealtimeNanos = startupTiming!!.triggerNanos,
            atRoutingTrigger = currentRecordingConfigurations,
        )
        val target = earpiece.toObservedDevice()
        val modeBeforeParticipation = audioManager.mode
        val preOwnership = collectSnapshot()
        addEvent("Qualifying communication detected — complete pre-change state: ${preOwnership.inlineDescription()}")
        // Set the guard before changing Android state: even a synchronous callback cannot retrigger.
        experiment = experiment.copy(
            state = ExperimentState.REQUEST_ATTEMPTED,
            requestAttempted = true,
            selectedTarget = target,
            modeBeforeParticipation = audioModeName(modeBeforeParticipation),
            preOwnership = preOwnership,
            assistantQualifyingPlaybackCount = assistantCount,
            browserQualifyingPlaybackCount = browserCount,
            triggerOrigin = triggerOrigin,
            modeBeforeAssistantParticipation =
                if (triggerOrigin == TriggerOrigin.ASSISTANT) audioModeName(modeBeforeParticipation) else null,
            routingTriggerTimestamp = routingTriggerTimestamp,
            startupTimingGeneration = timingGeneration,
        )
        addEvent(
            "Routing trigger received — timestamp=$routingTriggerTimestamp; startup generation=$timingGeneration; " +
                "prepared track available=${preparedSilentTrack && silentTrack?.state == AudioTrack.STATE_INITIALIZED}; " +
                "prefill completed=${experiment.preparedTrackPrefilled}",
        )
        routingActionInProgress = true
        if (reuseEarlyTrack) promoteAssistantEarlyPreArm()
        if (!reuseEarlyTrack && !startSilentCommunicationTrack()) {
            routingActionInProgress = false
            clearExperiment("Silent communication AudioTrack could not be started", ExperimentState.BLOCKED)
            return
        }
        val postTrackStart = collectSnapshot()
        recordingStartupObservation?.takeIf { it.generation == timingGeneration }?.atPostSilentTrackStart =
            currentRecordingConfigurations
        val visibleVoicePlayback = postTrackStart.activePlaybackConfigurations.any {
            it.usage == "USAGE_VOICE_COMMUNICATION" && it.contentType == "CONTENT_TYPE_SPEECH"
        }
        experiment = experiment.copy(
            postSilentTrackStart = postTrackStart,
            activeVoiceCommunicationPlaybackObserved = visibleVoicePlayback,
        )
        externalContributionEstablished = when (triggerOrigin) {
            TriggerOrigin.ASSISTANT -> assistantQualifyingPlaybackCount(audioManager.activePlaybackConfigurations) > 0
            TriggerOrigin.BROWSER_COMMUNICATION -> browserQualifyingPlaybackCount(audioManager.activePlaybackConfigurations) > 0
            TriggerOrigin.COMMUNICATION -> qualifyingPlaybackCount(audioManager.activePlaybackConfigurations) >= 2
        }
        addEvent(
            "Routing cycle $cycleGeneration started — qualifying playback contributions after local start=" +
                qualifyingPlaybackCount(audioManager.activePlaybackConfigurations),
        )
        addEvent(
            "Silent track is PLAYING before mode request — visible active VOICE_COMMUNICATION/SPEECH playback=$visibleVoicePlayback",
        )
        val trackActiveBeforeModeRequest = silentTrack?.playState == AudioTrack.PLAYSTATE_PLAYING
        val modeRequestTimestamp = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        matchingStartupTiming()?.modeRequestStartedNanos = SystemClock.elapsedRealtimeNanos()
        val modeBeforeRequest = audioManager.mode
        val requestThread = "${Thread.currentThread().name} (id=${Thread.currentThread().id})"
        modeParticipationActive = true
        experiment = experiment.copy(
            silentTrackActiveBeforeModeRequest = trackActiveBeforeModeRequest,
            modeRequestIssuedAfterPlaybackActive = trackActiveBeforeModeRequest,
            explicitModeRequestInvoked = true,
            modeRequestTimestamp = modeRequestTimestamp,
            modeRequestThread = requestThread,
            modeImmediatelyBeforeRequest = audioModeName(modeBeforeRequest),
        )
        addEvent(
            "Invoking explicit setMode(MODE_IN_COMMUNICATION) — timestamp=$modeRequestTimestamp; " +
                "thread=$requestThread; mode before=${audioModeName(modeBeforeRequest)}; " +
                "silent track play state=${experiment.silentTrackPlayState}",
        )
        val modeRequestFailure = runCatching {
            requestCommunicationMode()
        }.exceptionOrNull()
        val modeAfterParticipation = audioManager.mode
        matchingStartupTiming()?.modeObservedNanos = SystemClock.elapsedRealtimeNanos()
        experiment = experiment.copy(
            modeImmediatelyAfterRequest = audioModeName(modeAfterParticipation),
            modeRequestException = modeRequestFailure?.exactDescription(),
            modeInCommunicationObserved = modeAfterParticipation == AudioManager.MODE_IN_COMMUNICATION,
            modeObservedTimestamp = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
            playingToModeObservedElapsedMs = startupDurationMs(
                matchingStartupTiming()?.playingObservedNanos,
                matchingStartupTiming()?.modeObservedNanos,
            ),
        )
        addEvent(
            "Explicit setMode returned — requested=MODE_IN_COMMUNICATION; " +
                "Android-reported mode=${audioModeName(modeAfterParticipation)}; ${currentStateDescription()}",
        )
        experiment = experiment.copy(postModeOwnership = collectSnapshot())
        recordingStartupObservation?.takeIf { it.generation == timingGeneration }?.atPostModeRequest =
            currentRecordingConfigurations
        routingActionInProgress = false
        if (modeRequestFailure != null) {
            clearExperiment(
                "Explicit setMode failed with ${modeRequestFailure.exactDescription()}",
                ExperimentState.BLOCKED,
            )
            return
        }
        if (modeAfterParticipation != AudioManager.MODE_IN_COMMUNICATION) {
            clearExperiment("MODE_IN_COMMUNICATION was not re-established", ExperimentState.BLOCKED)
            return
        }
        performRoutingAttempt(earpiece, "silent communication playback active and mode requested")
    }

    private fun createSilentCommunicationTrack(): AudioTrack? {
        val sampleRate = AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_VOICE_CALL)
            .takeIf { it > 0 } ?: DEFAULT_SAMPLE_RATE
        val minBuffer = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        )
        if (minBuffer <= 0) {
            addEvent("Silent AudioTrack creation failed — unsupported buffer result=$minBuffer at ${sampleRate}Hz")
            return null
        }
        val bufferBytes = maxOf(minBuffer, MIN_SILENCE_BUFFER_BYTES)
        val track = runCatching {
            AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build(),
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build(),
                )
                .setBufferSizeInBytes(bufferBytes)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
        }.getOrElse {
            addEvent("Silent AudioTrack creation failed — ${it.javaClass.simpleName}: ${it.message}")
            return null
        }
        if (track.state != AudioTrack.STATE_INITIALIZED) {
            addEvent("Silent AudioTrack creation produced uninitialized state=${track.state}")
            track.release()
            return null
        }
        experiment = experiment.copy(
            silentTrackCreated = true,
            silentTrackSampleRate = sampleRate,
            silentTrackBufferBytes = bufferBytes,
        )
        return track
    }

    private fun prepareSilentCommunicationTrack() {
        if (!controllerEnabled || experiment.requestAttempted || routingActionInProgress ||
            assistantEarlyRoute.active || silentTrack != null ||
            audioManager.mode != AudioManager.MODE_NORMAL
        ) return
        val track = createSilentCommunicationTrack() ?: run {
            addEvent("Silent AudioTrack preparation failed open — normal trigger fallback remains available")
            return
        }
        silentTrack = track
        preparedSilentTrack = true
        val silence = ShortArray((experiment.silentTrackBufferBytes ?: MIN_SILENCE_BUFFER_BYTES) / Short.SIZE_BYTES)
        val prefillResult = runCatching {
            track.write(silence, 0, silence.size, AudioTrack.WRITE_NON_BLOCKING)
        }.onFailure {
            addEvent("Prepared silent AudioTrack prefill failed open — ${it.exactDescription()}")
        }.getOrDefault(AudioTrack.ERROR_INVALID_OPERATION)
        val prefilled = prefillResult > 0
        if (!prefilled) {
            addEvent("Prepared silent AudioTrack prefill failed open — non-blocking write result=$prefillResult")
        }
        val completedAt = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val activeWhileWaiting = track.playState == AudioTrack.PLAYSTATE_PLAYING ||
            activePlaybackConfigurations().any {
                it.usage == "USAGE_VOICE_COMMUNICATION" && it.contentType == "CONTENT_TYPE_SPEECH"
            }
        experiment = experiment.copy(
            preparedTrackCreated = true,
            preparedTrackPrefilled = prefilled,
            prefillBytesWritten = prefillResult.takeIf { it > 0 }?.times(Short.SIZE_BYTES),
            prefillCompletedTimestamp = completedAt.takeIf { prefilled },
            firstSuccessfulWriteTimestamp = completedAt.takeIf { prefilled },
            preparationCompletedTimestamp = completedAt,
            preparedTrackState = "STATE_INITIALIZED / ${audioTrackPlayStateName(track.playState)}",
            preparedTrackActivePlaybackWhileWaiting = activeWhileWaiting,
            silentTrackPlayState = audioTrackPlayStateName(track.playState),
        )
        addEvent(
            "Prepared silent AudioTrack created and bounded prefill completed=$prefilled — completed=$completedAt; " +
                "zero PCM bytes=${experiment.prefillBytesWritten ?: 0}; writes attempted=1; state=${track.state}; " +
                "playState=${audioTrackPlayStateName(track.playState)}; public active playback while WAITING=$activeWhileWaiting",
        )
    }

    private fun startSilentCommunicationTrack(): Boolean {
        val reused = preparedSilentTrack && silentTrack?.state == AudioTrack.STATE_INITIALIZED
        if (preparedSilentTrack && !reused) releasePreparedSilentTrack("Prepared track validation failed")
        val fallbackUsed = silentTrack == null
        val track = silentTrack ?: (createSilentCommunicationTrack() ?: return false).also {
            silentTrack = it
        }
        preparedSilentTrack = false
        val bufferBytes = experiment.silentTrackBufferBytes ?: MIN_SILENCE_BUFFER_BYTES
        val silence = ShortArray(bufferBytes / Short.SIZE_BYTES)
        val playInvocationTimestamp = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val playInvocationNanos = SystemClock.elapsedRealtimeNanos()
        matchingStartupTiming()?.playInvocationNanos = playInvocationNanos
        addEvent("Silent AudioTrack play() invocation timestamp=$playInvocationTimestamp; prepared track reused=$reused")
        val playSucceeded = runCatching { track.play() }.isSuccess
        val playReturnedNanos = SystemClock.elapsedRealtimeNanos()
        matchingStartupTiming()?.playReturnedNanos = playReturnedNanos
        val playReturnedTimestamp = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val started = playSucceeded && track.playState == AudioTrack.PLAYSTATE_PLAYING
        if (started) matchingStartupTiming()?.playingObservedNanos = SystemClock.elapsedRealtimeNanos()
        val playingTimestamp = if (started) OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) else null
        val timing = matchingStartupTiming()
        val elapsed = startupDurationMs(timing?.triggerNanos, timing?.playingObservedNanos)
        val writerStartRequestedTimestamp = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        experiment = experiment.copy(
            silentTrackStarted = started,
            silentTrackPlayState = audioTrackPlayStateName(track.playState),
            preparedTrackReused = reused,
            playInvocationTimestamp = playInvocationTimestamp,
            playReturnedTimestamp = playReturnedTimestamp,
            writerStartRequestedTimestamp = writerStartRequestedTimestamp.takeIf { started },
            playingTimestamp = playingTimestamp,
            triggerToPlayingElapsedMs = elapsed,
            triggerToPlayInvocationElapsedMs = startupDurationMs(timing?.triggerNanos, timing?.playInvocationNanos),
            playCallDurationMs = startupDurationMs(playInvocationNanos, playReturnedNanos),
            fallbackCreationUsed = fallbackUsed,
        )
        if (started) startSilenceWriter(track, silence)
        addEvent(
            "Silent AudioTrack creation result — initialized=true; sampleRate=${experiment.silentTrackSampleRate}Hz; " +
                "buffer=$bufferBytes bytes; attributes=USAGE_VOICE_COMMUNICATION/CONTENT_TYPE_SPEECH; " +
                "playState=${audioTrackPlayStateName(track.playState)}; audio focus requested=false; " +
                "PLAYSTATE_PLAYING timestamp=${playingTimestamp ?: "not observed"}; trigger to PLAYING=${elapsed ?: "unknown"} ms; " +
                "fallback creation used=$fallbackUsed",
        )
        return started
    }

    private fun startSilenceWriter(track: AudioTrack, silence: ShortArray) {
        silentWriterRunning.set(true)
        firstWriterWriteRecorded.set(experiment.firstSuccessfulWriteTimestamp != null)
        addEvent("Silence writer thread start requested after PLAYSTATE_PLAYING")
        silentWriterThread = Thread({
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
            while (silentWriterRunning.get()) {
                val written = track.write(silence, 0, silence.size, AudioTrack.WRITE_NON_BLOCKING)
                if (written > 0 && firstWriterWriteRecorded.compareAndSet(false, true)) {
                    val timestamp = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                    observationHandler.post {
                        if (experiment.firstSuccessfulWriteTimestamp == null) {
                            experiment = experiment.copy(firstSuccessfulWriteTimestamp = timestamp)
                        }
                    }
                }
                if (written <= 0) {
                    try {
                        Thread.sleep(SILENCE_WRITER_PAUSE_MS)
                    } catch (_: InterruptedException) {
                        break
                    }
                }
            }
        }, "poc5-silence-writer").apply {
            isDaemon = true
            start()
        }
    }

    private fun startAssistantEarlyPreArm() {
        if (!controllerEnabled || !assistantEarlyRoute.featureEnabled || assistantEarlyRoute.active ||
            experiment.requestAttempted || routingActionInProgress || audioManager.mode != AudioManager.MODE_NORMAL ||
            !voiceRecognitionPresent()
        ) return
        val generation = ++assistantEarlyRouteGeneration
        val startedAt = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val deadline = OffsetDateTime.now().plusNanos(ASSISTANT_EARLY_ROUTE_TIMEOUT_MS * 1_000_000)
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        assistantEarlyRoute = AssistantEarlyRouteStatus(
            featureEnabled = true,
            active = true,
            voiceRecognitionPresent = true,
            assistantSonificationObserved = true,
            startedAt = startedAt,
            generation = generation,
            timeoutDeadline = deadline,
            prefillCompleted = experiment.preparedTrackPrefilled,
            recordingBeforePreArm = currentRecordingConfigurations,
        )
        addEvent("Assistant early-route qualification detected — VOICE_RECOGNITION + ASSISTANT/SONIFICATION; generation=$generation")
        scheduleAssistantEarlyRouteTimeout(generation)
        routingActionInProgress = true
        val started = startSilentCommunicationTrack()
        routingActionInProgress = false
        if (!started) {
            cleanupAssistantEarlyPreArm("silent AudioTrack start failed", reEvaluatePlayback = false)
            return
        }
        assistantEarlyRoute = assistantEarlyRoute.copy(
            preparedTrackReused = experiment.preparedTrackReused,
            prefillCompleted = experiment.preparedTrackPrefilled,
            playInvocationAt = experiment.playInvocationTimestamp,
            playReturnedAt = experiment.playReturnedTimestamp,
            playingAt = experiment.playingTimestamp,
            playDurationMs = experiment.playCallDurationMs,
            modeDuringEarlyPlaying = audioModeName(audioManager.mode),
            communicationDeviceDuringEarlyPlaying = audioManager.communicationDevice?.toObservedDevice(),
            recordingAfterPlaying = currentRecordingConfigurations,
            playingElapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos(),
        )
        addEvent("Assistant early silent track play invoked — returned=${experiment.playReturnedTimestamp}")
        addEvent("Assistant early silent track PLAYING — generation=$generation")
        if (audioManager.mode == AudioManager.MODE_NORMAL) addEvent("Assistant early track active while MODE_NORMAL")
        snapshot("Assistant early silent track PLAYING")
    }

    private fun promoteAssistantEarlyPreArm() {
        val arrivalNanos = SystemClock.elapsedRealtimeNanos()
        val arrivalAt = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val headStart = startupDurationMs(assistantEarlyRoute.playingElapsedRealtimeNanos, arrivalNanos)
        invalidateAssistantEarlyRouteDelayedWork()
        assistantEarlyRoute = assistantEarlyRoute.copy(
            active = false,
            assistantSpeechArrivalAt = arrivalAt,
            playingToAssistantSpeechHeadStartMs = headStart,
            promoted = true,
            promotionAt = arrivalAt,
            recordingAtAssistantSpeech = currentRecordingConfigurations,
        )
        addEvent("ASSISTANT/SPEECH arrived with early track already PLAYING")
        addEvent("Early track head-start before ASSISTANT/SPEECH: ${headStart ?: "unknown"} ms")
        addEvent("Assistant early pre-arm promoted to protected POC-5")
    }

    private fun cleanupAssistantEarlyPreArm(reason: String, reEvaluatePlayback: Boolean = true) {
        invalidateAssistantEarlyRouteDelayedWork()
        if (!assistantEarlyRoute.active) return
        val cleaned = stopSilentCommunicationTrack()
        assistantEarlyRoute = assistantEarlyRoute.copy(
            active = false,
            cleanupCompleted = cleaned,
            cleanupReason = reason,
        )
        experiment = EarpieceExperiment(state = ExperimentState.ARMED, armed = controllerEnabled)
        addEvent("Assistant early pre-arm aborted — $reason")
        if (controllerEnabled && reEvaluatePlayback && !routingActionInProgress) {
            prepareSilentCommunicationTrack()
            handlePlaybackConfigurations(audioManager.activePlaybackConfigurations)
        }
    }

    private fun isAssistantEarlyPreArmHealthy(): Boolean =
        assistantEarlyRoute.active && voiceRecognitionPresent() &&
            silentTrack?.state == AudioTrack.STATE_INITIALIZED &&
            silentTrack?.playState == AudioTrack.PLAYSTATE_PLAYING &&
            !modeParticipationActive

    private fun scheduleAssistantEarlyRouteTimeout(generation: Long) {
        val runnable = object : Runnable {
            override fun run() {
                if (pendingAssistantEarlyRouteTimeout !== this || generation != assistantEarlyRouteGeneration ||
                    !assistantEarlyRoute.active || experiment.requestAttempted
                ) return
                pendingAssistantEarlyRouteTimeout = null
                addEvent("Assistant early pre-arm timed out — generation=$generation")
                cleanupAssistantEarlyPreArm("10 second absolute timeout")
            }
        }
        pendingAssistantEarlyRouteTimeout = runnable
        observationHandler.postDelayed(runnable, ASSISTANT_EARLY_ROUTE_TIMEOUT_MS)
    }

    private fun invalidateAssistantEarlyRouteDelayedWork() {
        assistantEarlyRouteGeneration++
        pendingAssistantEarlyRouteTimeout?.let(observationHandler::removeCallbacks)
        pendingAssistantEarlyRouteTimeout = null
    }

    private fun voiceRecognitionPresent(): Boolean = currentRecordingConfigurations.any {
        it.audioSource == "VOICE_RECOGNITION"
    }

    private fun abortAssistantEarlyPreArmIfContextLost(reason: String) {
        if (!assistantEarlyRoute.active) return
        val failure = when {
            !controllerEnabled -> "controller OFF"
            !assistantEarlyRoute.featureEnabled -> "feature disabled"
            !voiceRecognitionPresent() -> "VOICE_RECOGNITION disappeared"
            audioManager.mode.isTelephonyOrSystemPriorityMode() ->
                "system/telephony-priority mode ${audioModeName(audioManager.mode)}"
            silentTrack?.state != AudioTrack.STATE_INITIALIZED ||
                silentTrack?.playState != AudioTrack.PLAYSTATE_PLAYING -> "silent track unhealthy"
            else -> return
        }
        cleanupAssistantEarlyPreArm("$failure ($reason)")
    }

    private fun requestCommunicationMode() {
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
    }

    private fun requestCommunicationDevice(earpiece: AudioDeviceInfo): Boolean =
        audioManager.setCommunicationDevice(earpiece)

    private fun performRoutingAttempt(earpiece: AudioDeviceInfo, trigger: String) {
        if (!experiment.armed || experiment.attempts.isNotEmpty()) return
        if (audioManager.mode.isTelephonyOrSystemPriorityMode()) {
            clearExperiment("Routing attempt blocked by system/telephony-priority mode", ExperimentState.BLOCKED)
            return
        }
        if (audioManager.mode != AudioManager.MODE_IN_COMMUNICATION) {
            clearExperiment("Routing attempt cancelled: communication session ended", ExperimentState.CLEARED)
            return
        }

        val number = experiment.attempts.size + 1
        val before = audioManager.communicationDevice?.toObservedDevice()
        val mode = audioModeName(audioManager.mode)
        val timestamp = LocalTime.now().format(TIME_FORMAT)
        routingActionInProgress = true
        addEvent("Single routing request — trigger=$trigger; mode=$mode; device before=${before.reportDescription()}")
        matchingStartupTiming()?.deviceRequestStartedNanos = SystemClock.elapsedRealtimeNanos()
        val deviceRequestStartedTimestamp = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val accepted = requestCommunicationDevice(earpiece)
        matchingStartupTiming()?.deviceRequestReturnedNanos = SystemClock.elapsedRealtimeNanos()
        val deviceRequestReturnedTimestamp = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val after = audioManager.communicationDevice?.toObservedDevice()
        val speakerphone = observedSpeakerphoneState()
        val attempt = RoutingAttempt(number, timestamp, trigger, mode, before, accepted, after, speakerphone)
        experiment = experiment.copy(
            requestAccepted = accepted,
            attempts = experiment.attempts + attempt,
            earpieceRequestAfterExplicitModeRequest = experiment.explicitModeRequestInvoked,
            postRoutingRequest = collectSnapshot(),
            externalVoicePlaybackDeviceAfterRequest = inferExternalVoicePlaybackDevice(collectSnapshot()),
            deviceRequestStartedTimestamp = deviceRequestStartedTimestamp,
            deviceRequestReturnedTimestamp = deviceRequestReturnedTimestamp,
            modeObservedToDeviceRequestReturnElapsedMs = startupDurationMs(
                matchingStartupTiming()?.modeObservedNanos,
                matchingStartupTiming()?.deviceRequestReturnedNanos,
            ),
        )
        recordingStartupObservation?.takeIf { it.generation == cycleGeneration }?.atPostRoutingRequest =
            currentRecordingConfigurations
        addEvent("Routing attempt $number result — accepted=$accepted; device immediately after=${after.reportDescription()}; speakerphone=$speakerphone")
        routingActionInProgress = false
        snapshot("Immediate post-request observation")
        scheduleShortObservation()
    }

    private fun scheduleShortObservation() {
        cancelPendingObservation()
        val runnable = Runnable {
            pendingObservation = null
            if (!experiment.armed || experiment.attempts.size != 1) return@Runnable
            snapshot("Short post-request observation period elapsed")
            experiment = experiment.copy(shortObservation = snapshot)
        }
        pendingObservation = runnable
        observationHandler.postDelayed(runnable, OBSERVATION_DELAY_MS)
        addEvent("Short ${OBSERVATION_DELAY_MS} ms observation scheduled — no further routing request will be made")
    }

    private fun handlePlaybackConfigurations(configs: List<AudioPlaybackConfiguration>) {
        if (!controllerEnabled) return
        val count = qualifyingPlaybackCount(configs)
        val assistantCount = assistantQualifyingPlaybackCount(configs)
        val browserCount = browserQualifyingPlaybackCount(configs)
        recordPlaybackObservation(
            configs.map(AudioPlaybackConfiguration::toObservedPlayback),
            Triple(count, assistantCount, browserCount),
        )
        currentAssistantQualifyingPlaybackCount = assistantCount
        currentBrowserQualifyingPlaybackCount = browserCount
        val sonification = configs.firstOrNull {
            it.audioAttributes.usage == AudioAttributes.USAGE_ASSISTANT &&
                it.audioAttributes.contentType == AudioAttributes.CONTENT_TYPE_SONIFICATION
        }
        if (assistantEarlyRoute.active) {
            abortAssistantEarlyPreArmIfContextLost("playback callback")
            val incompatibleCommunication = qualifyingPlaybackCount(configs) >= 2 || browserCount > 0
            if (incompatibleCommunication) {
                cleanupAssistantEarlyPreArm("incompatible COMMUNICATION/BROWSER session began", reEvaluatePlayback = false)
            }
        }
        if (experiment.attempts.isEmpty() && !experiment.requestAttempted && !assistantEarlyRoute.active &&
            sonification != null && assistantEarlyRoute.featureEnabled && voiceRecognitionPresent() &&
            !routingActionInProgress
        ) {
            startAssistantEarlyPreArm()
        }
        val qualifierCounts = Triple(count, assistantCount, browserCount)
        if (qualifierCounts != previousQualifierCounts) {
            addEvent(
                "Playback callback — qualifying VOICE_COMMUNICATION/SPEECH count=$count; " +
                    "qualifying ASSISTANT/SPEECH count=$assistantCount; " +
                    "browser qualifying VOICE_COMMUNICATION/UNKNOWN count=$browserCount",
            )
            previousQualifierCounts = qualifierCounts
        }
        if (experiment.attempts.isEmpty()) {
            if (!routingActionInProgress && (count > 0 || assistantCount > 0 || browserCount > 0)) evaluateExperimentTrigger()
            return
        }
        if (silentTrack?.playState != AudioTrack.PLAYSTATE_PLAYING) return
        val externalPlaybackPresent = when (experiment.triggerOrigin) {
            TriggerOrigin.ASSISTANT -> assistantCount > 0
            TriggerOrigin.BROWSER_COMMUNICATION -> browserCount > 0
            else -> count >= 2
        }
        if (externalPlaybackPresent) {
            externalContributionEstablished = true
            cancelPendingEndConfirmation()
            if (pendingSessionLinger != null) resumeAssistantDuringLinger()
        } else if (externalContributionEstablished) {
            scheduleEndConfirmation()
        }
    }

    private fun recordPlaybackObservation(current: List<ObservedPlayback>, counts: Triple<Int, Int, Int>) {
        val timestamp = LocalTime.now().format(TIME_FORMAT)
        val changes = playbackChanges(previousPlaybackObservation, current)
        val controllerState = "mode=${audioModeName(audioManager.mode)}; " +
            "communication device=${audioManager.communicationDevice?.toObservedDevice().reportDescription()}; " +
            "speakerphone=${observedSpeakerphoneState()}; counts=$counts"
        if (changes.entries.isEmpty() && controllerState == previousPlaybackControllerState) {
            redundantPlaybackCallbacksSuppressed++
            return
        }
        addStartupTrace("$timestamp  playback callback — ${changes.summary}")
        changes.entries.forEach { change ->
            addStartupTrace("  $change")
        }
        addStartupTrace(
            "  controller: $controllerState",
        )
        previousPlaybackObservation = current
        previousPlaybackControllerState = controllerState
    }

    private fun qualifyingPlaybackCount(configs: List<AudioPlaybackConfiguration>) = configs.count {
        it.audioAttributes.usage == AudioAttributes.USAGE_VOICE_COMMUNICATION &&
            it.audioAttributes.contentType == AudioAttributes.CONTENT_TYPE_SPEECH
    }

    private fun assistantQualifyingPlaybackCount(configs: List<AudioPlaybackConfiguration>) = configs.count {
        it.audioAttributes.usage == AudioAttributes.USAGE_ASSISTANT &&
            it.audioAttributes.contentType == AudioAttributes.CONTENT_TYPE_SPEECH
    }

    private fun browserQualifyingPlaybackCount(configs: List<AudioPlaybackConfiguration>) = configs.count {
        it.audioAttributes.usage == AudioAttributes.USAGE_VOICE_COMMUNICATION &&
            it.audioAttributes.contentType == AudioAttributes.CONTENT_TYPE_UNKNOWN
    }

    private fun scheduleEndConfirmation() {
        if (pendingEndConfirmation != null) return
        val generation = cycleGeneration
        addEvent(
            "External communication end candidate — only known local contribution remains; " +
                "confirming for $END_CONFIRMATION_DELAY_MS ms",
        )
        val runnable = Runnable {
            pendingEndConfirmation = null
            if (!controllerEnabled || generation != cycleGeneration || experiment.attempts.size != 1 ||
                silentTrack?.playState != AudioTrack.PLAYSTATE_PLAYING
            ) return@Runnable
            val externalPlaybackPresent = when (experiment.triggerOrigin) {
                TriggerOrigin.ASSISTANT -> assistantQualifyingPlaybackCount(audioManager.activePlaybackConfigurations) > 0
                TriggerOrigin.BROWSER_COMMUNICATION -> browserQualifyingPlaybackCount(audioManager.activePlaybackConfigurations) > 0
                else -> qualifyingPlaybackCount(audioManager.activePlaybackConfigurations) > 1
            }
            if (externalPlaybackPresent) return@Runnable
            addEvent("External communication end confirmed for routing cycle $generation")
            if (experiment.triggerOrigin == TriggerOrigin.ASSISTANT) {
                addEvent("ASSISTANT playback-loss confirmation completed for routing cycle $generation")
                startAssistantSessionLinger(generation)
            } else {
                clearExperiment("External communication playback ended", ExperimentState.CLEARED)
                returnToWaiting()
            }
        }
        pendingEndConfirmation = runnable
        observationHandler.postDelayed(runnable, END_CONFIRMATION_DELAY_MS)
    }

    private fun startAssistantSessionLinger(generation: Long) {
        cancelPendingSessionLinger()
        // The confirmed contribution is absent. Only a genuinely resumed ASSISTANT/SPEECH
        // callback may establish it again and make a later disappearance eligible for stage 1.
        externalContributionEstablished = false
        val token = ++lingerGeneration
        val deadline = OffsetDateTime.now().plusNanos(ASSISTANT_SESSION_LINGER_MS * 1_000_000)
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        addEvent(
            "Protected session linger started — routing cycle=$generation; trigger=ASSISTANT; " +
                "deadline=$deadline; ${protectedContextDescription()}",
        )
        val runnable = object : Runnable {
            override fun run() {
                if (pendingSessionLinger !== this || token != lingerGeneration ||
                    generation != cycleGeneration || !controllerEnabled ||
                    experiment.triggerOrigin != TriggerOrigin.ASSISTANT || experiment.attempts.size != 1
                ) return
                pendingSessionLinger = null
                addEvent("Protected session linger expired — routing cycle=$generation; ${protectedContextDescription()}")
                addEvent("Cleanup started because linger expired — routing cycle=$generation; trigger=ASSISTANT")
                clearExperiment("ASSISTANT protected session linger expired", ExperimentState.CLEARED)
                returnToWaiting()
            }
        }
        pendingSessionLinger = runnable
        observationHandler.postDelayed(runnable, ASSISTANT_SESSION_LINGER_MS)
    }

    private fun resumeAssistantDuringLinger() {
        val generation = cycleGeneration
        addEvent("ASSISTANT/SPEECH resumed during linger — routing cycle=$generation; ${protectedContextDescription()}")
        cancelPendingSessionLinger()
        addEvent("Linger cancelled because external contribution resumed — routing cycle=$generation")
        addEvent("Protected context reused without new routing attempt — routing cycle=$generation; ${protectedContextDescription()}")
    }

    private fun abortAssistantLingerIfContextLost(reason: String): Boolean {
        if (pendingSessionLinger == null) return false
        val failure = when {
            audioManager.mode.isTelephonyOrSystemPriorityMode() ->
                "system/telephony-priority mode ${audioModeName(audioManager.mode)}"
            silentTrack?.playState != AudioTrack.PLAYSTATE_PLAYING -> "silent track failure"
            audioManager.availableCommunicationDevices.none { it.type == AudioDeviceInfo.TYPE_BUILTIN_EARPIECE } ->
                "required earpiece unavailable"
            audioManager.mode != AudioManager.MODE_IN_COMMUNICATION -> "communication mode ownership lost"
            audioManager.communicationDevice?.type != AudioDeviceInfo.TYPE_BUILTIN_EARPIECE ->
                "protected earpiece route lost"
            else -> return false
        }
        addEvent("Immediate cleanup bypassed linger because $failure — observation=$reason; routing cycle=$cycleGeneration")
        clearExperiment("Assistant linger aborted: $failure", ExperimentState.BLOCKED)
        return true
    }

    private fun protectedContextDescription(): String =
        "mode=${audioModeName(audioManager.mode)}; " +
            "communication device=${audioManager.communicationDevice?.toObservedDevice().reportDescription()}; " +
            "silent track=${audioTrackPlayStateName(silentTrack?.playState ?: AudioTrack.PLAYSTATE_STOPPED)}; " +
            "public state=ACTIVE; routing attempts=${experiment.attempts.size}"

    private fun returnToWaiting() {
        if (!controllerEnabled) return
        lastRecordingStartupObservation = recordingStartupObservation
        cycleGeneration++
        recordingStartupObservation = null
        externalContributionEstablished = false
        activeEvidenceRecorded = false
        experiment = EarpieceExperiment(state = ExperimentState.ARMED, armed = true)
        addEvent("Cleanup completed — controller remains ON and returned to clean waiting")
        prepareSilentCommunicationTrack()
        handlePlaybackConfigurations(audioManager.activePlaybackConfigurations)
    }

    private fun unregisterPlaybackCallback() {
        if (!playbackCallbackRegistered) return
        audioManager.unregisterAudioPlaybackCallback(playbackCallback)
        playbackCallbackRegistered = false
    }

    private fun invalidatePendingControllerWork() {
        cycleGeneration++
        invalidateAssistantEarlyRouteDelayedWork()
        cancelPendingEndConfirmation()
        cancelPendingSessionLinger()
    }

    private fun cancelPendingEndConfirmation() {
        pendingEndConfirmation?.let(observationHandler::removeCallbacks)
        pendingEndConfirmation = null
    }

    private fun cancelPendingSessionLinger() {
        lingerGeneration++
        pendingSessionLinger?.let(observationHandler::removeCallbacks)
        pendingSessionLinger = null
    }

    private fun observeExperimentOutcome(observed: DiagnosticSnapshot, reason: String) {
        if (experiment.attempts.isEmpty() || observed.mode != "MODE_IN_COMMUNICATION") return
        when (observed.communicationDevice?.type) {
            "Built-in earpiece" -> if (!experiment.earpieceReportedDuringSession) {
                val timing = matchingStartupTiming()
                timing?.earpieceObservedNanos = SystemClock.elapsedRealtimeNanos()
                experiment = experiment.copy(
                    earpieceReportedDuringSession = true,
                    earpieceFirstObservedTimestamp = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                    deviceRequestToEarpieceObservedElapsedMs = startupDurationMs(
                        timing?.deviceRequestStartedNanos,
                        timing?.earpieceObservedNanos,
                    ),
                    triggerToEarpieceObservedElapsedMs = startupDurationMs(
                        timing?.triggerNanos,
                        timing?.earpieceObservedNanos,
                    ),
                )
                recordingStartupObservation?.takeIf { it.generation == cycleGeneration }?.atFirstEarpiece =
                    currentRecordingConfigurations
                recordingStartupObservation?.takeIf { it.generation == cycleGeneration }?.firstEarpieceElapsedRealtimeNanos =
                    timing?.earpieceObservedNanos
                addEvent("Android reported built-in earpiece while external communication remained active — observation=$reason")
            }
            "Built-in speaker" -> if (experiment.earpieceReportedDuringSession && !experiment.revertedToSpeaker) {
                experiment = experiment.copy(revertedToSpeaker = true)
                addEvent("Android route reverted to built-in speaker during active session — observation=$reason")
            }
        }
        if (!activeEvidenceRecorded && observed.communicationDevice?.type == "Built-in earpiece" &&
            experiment.silentTrackStarted && !experiment.silentTrackCleanupCompleted
        ) {
            activeEvidenceRecorded = true
            addEvent("ACTIVE state evidence established — MODE_IN_COMMUNICATION, built-in earpiece, and local silent track active")
        }
    }

    private fun clearExperiment(reason: String, finalState: ExperimentState) {
        cancelPendingEndConfirmation()
        if (pendingSessionLinger != null && !reason.contains("linger expired")) {
            addEvent("Immediate cleanup bypassed linger because $reason — routing cycle=$cycleGeneration")
        }
        cancelPendingSessionLinger()
        cancelPendingObservation()
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
        val trackCleanupCompleted = stopSilentCommunicationTrack()
        routingActionInProgress = false
        experiment = experiment.copy(
            state = finalState,
            armed = false,
            silentTrackCleanupCompleted = experiment.silentTrackCreated && trackCleanupCompleted,
            silentTrackPlayState = if (experiment.silentTrackCreated && trackCleanupCompleted) "Released" else experiment.silentTrackPlayState,
        )
        if (assistantEarlyRoute.startedAt != null) {
            assistantEarlyRoute = assistantEarlyRoute.copy(
                active = false,
                cleanupCompleted = trackCleanupCompleted,
                cleanupReason = reason,
            )
        }
        snapshot("Post-cleanup observation")
        if (experiment.requestAttempted || experiment.triggerOrigin != null) {
            lastCompletedExperiment = CompletedRoutingCycle(
                experiment = experiment,
                finalCleanupObservation = snapshot,
                completionReason = reason,
                completedAt = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
            )
        }
    }

    private fun stopSilentCommunicationTrack(): Boolean {
        val track = silentTrack ?: return true
        silentWriterRunning.set(false)
        silentWriterThread?.interrupt()
        runCatching { silentWriterThread?.join(SILENCE_WRITER_JOIN_MS) }
        val writerStopped = silentWriterThread?.isAlive != true
        silentWriterThread = null
        runCatching { track.stop() }
        runCatching { track.flush() }
        runCatching { track.release() }
        silentTrack = null
        preparedSilentTrack = false
        addEvent("Silent AudioTrack cleanup — writer stopped=$writerStopped; track stopped, flushed, and released")
        return writerStopped
    }

    private fun releasePreparedSilentTrack(reason: String) {
        if (!preparedSilentTrack) return
        val track = silentTrack
        preparedSilentTrack = false
        silentTrack = null
        runCatching { track?.release() }
        addEvent("Unused prepared silent AudioTrack released — reason=$reason; no route or mode cleanup required")
    }

    private fun inferExternalVoicePlaybackDevice(observed: DiagnosticSnapshot): ObservedDevice? {
        val matching = observed.activePlaybackConfigurations.filter {
            it.usage == "USAGE_VOICE_COMMUNICATION" && it.contentType == "CONTENT_TYPE_SPEECH"
        }
        // Public playback diagnostics expose no client package/UID here. With both apps using the
        // same attributes, only a single common reported device can be recorded without claiming identity.
        return matching.mapNotNull { it.device }.distinct().singleOrNull()
    }

    private fun cancelPendingObservation() {
        pendingObservation?.let(observationHandler::removeCallbacks)
        pendingObservation = null
    }

    private fun currentStateDescription() =
        "communication device=${audioManager.communicationDevice?.toObservedDevice().reportDescription()}; " +
            "speakerphone=${observedSpeakerphoneState()}"

    private fun matchingStartupTiming(): StartupTiming? =
        startupTiming?.takeIf {
            it.generation == cycleGeneration && experiment.startupTimingGeneration == it.generation &&
                experiment.triggerOrigin != null
        }

    private fun startupDurationMs(startNanos: Long?, endNanos: Long?): Long? =
        if (startNanos != null && endNanos != null && endNanos >= startNanos) {
            (endNanos - startNanos) / 1_000_000L
        } else {
            null
        }

    private fun collectSnapshot() = DiagnosticSnapshot(
        timestamp = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        lifecycleState = processLifecycleState(),
        mode = audioModeName(audioManager.mode),
        communicationDevice = audioManager.communicationDevice?.toObservedDevice(),
        availableCommunicationDevices = audioManager.availableCommunicationDevices.map(AudioDeviceInfo::toObservedDevice),
        speakerphoneState = observedSpeakerphoneState(),
        activePlaybackConfigurations = activePlaybackConfigurations(),
    )

    private fun activePlaybackConfigurations() = audioManager.activePlaybackConfigurations
        .map(AudioPlaybackConfiguration::toObservedPlayback)

    private fun processLifecycleState(): String {
        val state = ActivityManager.RunningAppProcessInfo()
        ActivityManager.getMyMemoryState(state)
        return when (state.importance) {
            ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND -> "Foreground"
            ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE -> "Visible"
            else -> "Background (importance=${state.importance})"
        }
    }

    @Suppress("DEPRECATION")
    private fun observedSpeakerphoneState() =
        if (audioManager.isSpeakerphoneOn) "On (directly observed)" else "Off (directly observed)"
}

internal fun buildDiagnosticReport(
    timestamp: String,
    experiment: EarpieceExperiment,
    lastCompletedExperiment: CompletedRoutingCycle? = null,
    snapshot: DiagnosticSnapshot,
    events: List<String>,
    packageName: String = "app.privateaudio",
    baseline: DiagnosticSnapshot? = null,
    startupAudioTrace: List<String> = emptyList(),
    assistantQualifyingPlaybackCount: Int = experiment.assistantQualifyingPlaybackCount,
    browserQualifyingPlaybackCount: Int = experiment.browserQualifyingPlaybackCount,
    environment: DiagnosticEnvironment = DiagnosticEnvironment("Unknown", 0, "Unknown", 0, "Unknown", "Unknown", "Unknown"),
    eventEntriesDropped: Int = 0,
    startupTraceEntriesDropped: Int = 0,
    redundantPlaybackCallbacksSuppressed: Int = 0,
    assistantEarlyRoute: AssistantEarlyRouteStatus = AssistantEarlyRouteStatus(),
    recordingCallbackRegistered: Boolean = false,
    currentRecordingConfigurations: List<ObservedRecording> = emptyList(),
    recordingTrace: List<RecordingTraceEntry> = emptyList(),
    recordingTraceEntriesDropped: Int = 0,
    redundantRecordingCallbacksSuppressed: Int = 0,
    recordingStartupObservation: RecordingStartupObservation? = null,
    supportSummary: DiagnosticsSummary? = null,
) = buildString {
    appendLine("PRIVATE AUDIO — DIAGNOSTIC REPORT")
    appendLine("Diagnostic report format: $DIAGNOSTIC_REPORT_FORMAT")
    appendLine("Timestamp: $timestamp")
    appendLine("Package: $packageName")
    appendLine("Private Audio PID: ${Process.myPid()}")
    appendLine("Private Audio UID: ${Process.myUid()}")
    appendLine()
    supportSummary?.let {
        appendLine(it.supportSummary())
        appendLine()
    }
    appendLine("DIAGNOSTIC ENVIRONMENT")
    appendLine("Private Audio version: ${environment.versionName} (${environment.versionCode})")
    appendLine("Android: ${environment.androidRelease}")
    appendLine("API level: ${environment.apiLevel}")
    appendLine("Manufacturer: ${environment.manufacturer}")
    appendLine("Model: ${environment.model}")
    appendLine("Product: ${environment.product}")
    appendLine()
    appendLine("RECORDING / INPUT OBSERVATION")
    appendLine("Recording callback registered: $recordingCallbackRegistered")
    appendLine("Current active recording configuration count: ${currentRecordingConfigurations.size}")
    appendLine("Current public recording configurations:")
    if (currentRecordingConfigurations.isEmpty()) appendLine("None reported by Android")
    else currentRecordingConfigurations.forEachIndexed { index, recording ->
        appendLine("${index + 1}. ${recording.reportDescription()}")
    }
    appendLine("Recording trace retained/dropped/suppressed: ${recordingTrace.size}/$recordingTraceEntriesDropped/$redundantRecordingCallbacksSuppressed")
    appendLine("Recording state at routing trigger: ${recordingStartupObservation?.atRoutingTrigger.recordingDescription()}")
    appendLine("Recording state at POST-SILENT-TRACK-START: ${recordingStartupObservation?.atPostSilentTrackStart.recordingDescription()}")
    appendLine("Recording state at POST-MODE-REQUEST: ${recordingStartupObservation?.atPostModeRequest.recordingDescription()}")
    appendLine("Recording state at POST-ROUTING-REQUEST: ${recordingStartupObservation?.atPostRoutingRequest.recordingDescription()}")
    appendLine("Recording state at first earpiece observation: ${recordingStartupObservation?.atFirstEarpiece.recordingDescription()}")
    appendLine("Recording transition observed during startup: ${recordingStartupObservation?.transitionObserved.yesNoUnavailable()}")
    appendLine("Recording disappearance/reappearance during startup: ${recordingStartupObservation?.let { it.disappearanceObserved || it.reappearanceObserved }.yesNoUnavailable()}")
    val silencedExposed = currentRecordingConfigurations.any { it.clientSilenced != null } ||
        recordingTrace.any { entry -> entry.recordings.any { it.clientSilenced != null } }
    val deviceExposed = currentRecordingConfigurations.any { it.device != null } ||
        recordingTrace.any { entry -> entry.recordings.any { it.device != null } }
    appendLine("Client silenced transition observed: ${if (!silencedExposed) "Not exposed" else recordingStartupObservation?.silencedTransitionObserved.yesNoUnavailable()}")
    appendLine("Input-device transition observed: ${if (!deviceExposed) "Not exposed" else recordingStartupObservation?.inputDeviceTransitionObserved.yesNoUnavailable()}")
    appendLine("Recording appeared → routing trigger elapsed ms: ${recordingAppearanceToTriggerMs(recordingStartupObservation, recordingTrace) ?: "Not available"}")
    appendLine("Routing trigger → recording disappeared/changed elapsed ms: ${recordingTransitionAfterTriggerMs(recordingStartupObservation, recordingTrace, "RECORDING") ?: "Not available"}")
    appendLine("Routing trigger → client silenced change elapsed ms: ${recordingTransitionAfterTriggerMs(recordingStartupObservation, recordingTrace, "client silenced changed") ?: "Not available"}")
    appendLine("Routing trigger → input device change elapsed ms: ${recordingTransitionAfterTriggerMs(recordingStartupObservation, recordingTrace, "input device changed") ?: "Not available"}")
    appendLine("Routing trigger → first earpiece observation elapsed ms: ${recordingStartupObservation?.let { startupDurationForReport(it.routingTriggerElapsedRealtimeNanos, it.firstEarpieceElapsedRealtimeNanos) } ?: "Not available"}")
    appendLine("FACT: These diagnostics use only public Android recording metadata.")
    appendLine("No microphone audio is captured or recorded.")
    appendLine("UNKNOWN: Public Android APIs may redact information about recording sessions owned by other applications.")
    appendLine("UNKNOWN: A continuously visible recording configuration does not prove that the external application recognized or processed the user's speech.")
    appendLine("RECORDING / INPUT TRACE")
    if (recordingTrace.isEmpty()) appendLine("No meaningful recording transitions observed") else recordingTrace.forEach { entry ->
        appendLine("${entry.timestamp} elapsedRealtimeNanos=${entry.elapsedRealtimeNanos}; ${entry.description}; count=${entry.recordings.size}; " +
            "controllerEnabled=${entry.context.controllerEnabled}; mode=${entry.context.mode}; communicationDevice=${entry.context.communicationDevice.reportDescription()}; " +
            "speakerphone=${entry.context.speakerphoneState}; protectedCycleRequested=${entry.context.protectedCycleRequested}; " +
            "triggerOrigin=${entry.context.triggerOrigin ?: "None"}; silentTrack=${entry.context.silentTrackPlayState}; generation=${entry.context.routingGeneration ?: "Not available"}")
        entry.recordings.forEach { appendLine("  ${it.reportDescription()}") }
    }
    appendLine()
    appendLine("TRACE RETENTION")
    appendLine("Startup trace entries retained: ${startupAudioTrace.size}")
    appendLine("Startup trace capacity: $MAX_STARTUP_TRACE_EVENTS")
    appendLine("Startup trace entries dropped: $startupTraceEntriesDropped")
    appendLine("Event log entries retained: ${events.size}")
    appendLine("Event log capacity: $MAX_EVENTS")
    appendLine("Event log entries dropped: $eventEntriesDropped")
    appendLine("Redundant playback callbacks suppressed: $redundantPlaybackCallbacksSuppressed")
    appendLine()
    appendLine("ASSISTANT EARLY SILENT-TRACK EXPERIMENT")
    appendLine("Feature enabled: ${assistantEarlyRoute.featureEnabled}")
    appendLine("Early pre-arm active: ${assistantEarlyRoute.active}")
    appendLine("Generation: ${assistantEarlyRoute.generation}")
    appendLine("VOICE_RECOGNITION present: ${assistantEarlyRoute.voiceRecognitionPresent}")
    appendLine("ASSISTANT/SONIFICATION observed: ${assistantEarlyRoute.assistantSonificationObserved}")
    appendLine("Pre-arm start timestamp: ${assistantEarlyRoute.startedAt ?: "Not started"}")
    appendLine("Prepared track reused: ${assistantEarlyRoute.preparedTrackReused}")
    appendLine("Prefill completed: ${assistantEarlyRoute.prefillCompleted}")
    appendLine("play() invocation timestamp: ${assistantEarlyRoute.playInvocationAt ?: "Not attempted"}")
    appendLine("play() returned timestamp: ${assistantEarlyRoute.playReturnedAt ?: "Not attempted"}")
    appendLine("PLAYSTATE_PLAYING timestamp: ${assistantEarlyRoute.playingAt ?: "Not observed"}")
    appendLine("play() duration ms: ${assistantEarlyRoute.playDurationMs ?: "Not available"}")
    appendLine("Mode during early PLAYING phase: ${assistantEarlyRoute.modeDuringEarlyPlaying ?: "Not observed"}")
    appendLine("Communication device during early PLAYING phase: ${assistantEarlyRoute.communicationDeviceDuringEarlyPlaying.reportDescription()}")
    appendLine("Explicit early setMode attempted: ${assistantEarlyRoute.explicitEarlySetModeAttempted}")
    appendLine("Explicit early setCommunicationDevice attempted: ${assistantEarlyRoute.explicitEarlySetCommunicationDeviceAttempted}")
    appendLine("ASSISTANT/SPEECH arrival timestamp: ${assistantEarlyRoute.assistantSpeechArrivalAt ?: "Not observed"}")
    appendLine("Early track head-start before ASSISTANT/SPEECH: ${assistantEarlyRoute.playingToAssistantSpeechHeadStartMs ?: "Not available"} ms")
    appendLine("Promoted to protected ASSISTANT POC-5: ${assistantEarlyRoute.promoted}")
    appendLine("Promotion timestamp: ${assistantEarlyRoute.promotionAt ?: "Not promoted"}")
    appendLine("Recording configuration before pre-arm: ${assistantEarlyRoute.recordingBeforePreArm.recordingDescription()}")
    appendLine("Recording configuration after track PLAYING: ${assistantEarlyRoute.recordingAfterPlaying.recordingDescription()}")
    appendLine("Recording configuration at ASSISTANT/SPEECH arrival: ${assistantEarlyRoute.recordingAtAssistantSpeech.recordingDescription()}")
    appendLine("Cleanup completed: ${assistantEarlyRoute.cleanupCompleted}")
    appendLine("Cleanup reason: ${assistantEarlyRoute.cleanupReason ?: "None"}")
    appendLine("Timeout deadline: ${assistantEarlyRoute.timeoutDeadline ?: "Not scheduled"}")
    appendLine("Audible result: Requires physical confirmation")
    appendLine()
    appendLine("STARTUP AUDIO TRACE")
    appendLine("FACT: entries below contain only public Android playback metadata; no audio is captured or recorded.")
    appendLine("UNKNOWN: Android does not publicly expose player/session ownership or an exact player-state value here, so entries are not classified as startup chime, ChatGPT, or Private Audio.")
    if (startupAudioTrace.isEmpty()) appendLine("No playback callbacks recorded") else {
        startupAudioTrace.forEach(::appendLine)
    }
    appendLine()
    appendLine("PROCESS IDENTITY SNAPSHOTS")
    appendIdentity("BASELINE", baseline)
    appendIdentity("QUALIFYING TRIGGER", experiment.preOwnership)
    appendIdentity("ROUTING REQUEST", experiment.postRoutingRequest)
    appendIdentity("DELAYED OBSERVATION", experiment.shortObservation)
    appendLine()
    appendLine("EARPIECE EXPERIMENT")
    appendLine("Assistant qualifying playback count: $assistantQualifyingPlaybackCount")
    appendLine("Browser qualifying VOICE_COMMUNICATION/UNKNOWN count: $browserQualifyingPlaybackCount")
    appendLine("Trigger origin: ${experiment.triggerOrigin ?: "Not attempted"}")
    appendLine("Mode immediately before assistant participation: ${experiment.modeBeforeAssistantParticipation ?: "Not attempted"}")
    appendLine("Experiment state: ${experiment.state.label}")
    appendLine("Armed: ${experiment.armed}")
    appendLine("Routing request attempted: ${experiment.requestAttempted}")
    appendLine("Mode before participation: ${experiment.modeBeforeParticipation ?: "Not attempted"}")
    appendLine("Silent communication AudioTrack created: ${experiment.silentTrackCreated}")
    appendLine("Silent AudioTrack started: ${experiment.silentTrackStarted}")
    appendLine("Silent AudioTrack configuration: sampleRate=${experiment.silentTrackSampleRate ?: "Not created"}; mono PCM 16-bit; bufferBytes=${experiment.silentTrackBufferBytes ?: "Not created"}")
    appendLine("Silent AudioTrack play state: ${experiment.silentTrackPlayState}")
    appendLine("Prepared silent track created: ${experiment.preparedTrackCreated}")
    appendLine("Prepared track bounded zero-PCM prefill completed: ${experiment.preparedTrackPrefilled}")
    appendLine("Prepared track zero-PCM prefill bytes: ${experiment.prefillBytesWritten ?: "unknown / not applicable"}")
    appendLine("Prefill completed timestamp: ${experiment.prefillCompletedTimestamp ?: "unknown / not applicable"}")
    appendLine("Preparation completed timestamp: ${experiment.preparationCompletedTimestamp ?: "Not prepared"}")
    appendLine("Prepared track state: ${experiment.preparedTrackState}")
    appendLine("Prepared track active playback observed while WAITING: ${experiment.preparedTrackActivePlaybackWhileWaiting} (expected false)")
    appendLine("Routing trigger timestamp: ${experiment.routingTriggerTimestamp ?: "Not triggered"}")
    appendLine("Prepared track reused: ${experiment.preparedTrackReused}")
    appendLine("play() invocation timestamp: ${experiment.playInvocationTimestamp ?: "Not invoked"}")
    appendLine("play() returned timestamp: ${experiment.playReturnedTimestamp ?: "unknown / not applicable"}")
    appendLine("Writer thread start requested timestamp: ${experiment.writerStartRequestedTimestamp ?: "unknown / not applicable"}")
    appendLine("First successful AudioTrack.write() timestamp: ${experiment.firstSuccessfulWriteTimestamp ?: "unknown / not applicable"}")
    appendLine("PLAYSTATE_PLAYING timestamp: ${experiment.playingTimestamp ?: "Not observed"}")
    appendLine("Startup timing attempt/generation: ${experiment.startupTimingGeneration ?: "unknown / not applicable"}")
    appendLine("Trigger to play() invocation elapsed ms: ${experiment.triggerToPlayInvocationElapsedMs ?: "unknown / not applicable"}")
    appendLine("play() call duration ms: ${experiment.playCallDurationMs ?: "unknown / not applicable"}")
    appendLine("Trigger to PLAYSTATE_PLAYING elapsed ms: ${experiment.triggerToPlayingElapsedMs ?: "unknown / not applicable"}")
    appendLine("MODE_IN_COMMUNICATION observed timestamp: ${experiment.modeObservedTimestamp ?: "unknown / not applicable"}")
    appendLine("PLAYSTATE_PLAYING to mode observed elapsed ms: ${experiment.playingToModeObservedElapsedMs ?: "unknown / not applicable"}")
    appendLine("setCommunicationDevice() started timestamp: ${experiment.deviceRequestStartedTimestamp ?: "unknown / not applicable"}")
    appendLine("setCommunicationDevice() returned timestamp: ${experiment.deviceRequestReturnedTimestamp ?: "unknown / not applicable"}")
    appendLine("Mode observed to device request return elapsed ms: ${experiment.modeObservedToDeviceRequestReturnElapsedMs ?: "unknown / not applicable"}")
    appendLine("Earpiece first observed timestamp: ${experiment.earpieceFirstObservedTimestamp ?: "unknown / not applicable"}")
    appendLine("Device request start to earpiece first observed elapsed ms: ${experiment.deviceRequestToEarpieceObservedElapsedMs ?: "unknown / not applicable"}")
    appendLine("Trigger to earpiece first observed elapsed ms: ${experiment.triggerToEarpieceObservedElapsedMs ?: "unknown / not applicable"}")
    appendLine("Fallback AudioTrack creation used: ${experiment.fallbackCreationUsed}")
    appendLine("AudioAttributes: USAGE_VOICE_COMMUNICATION + CONTENT_TYPE_SPEECH")
    appendLine("Private Audio active VOICE_COMMUNICATION playback observed: ${experiment.activeVoiceCommunicationPlaybackObserved} (track PLAYING plus matching public active-playback configuration)")
    appendLine("Silent VOICE_COMMUNICATION AudioTrack active before mode request: ${experiment.silentTrackActiveBeforeModeRequest}")
    appendLine("Mode request issued after silent playback became active: ${experiment.modeRequestIssuedAfterPlaybackActive}")
    appendLine("Explicit Private Audio setMode(MODE_IN_COMMUNICATION) invoked: ${experiment.explicitModeRequestInvoked}")
    appendLine("Mode request timestamp: ${experiment.modeRequestTimestamp ?: "Not invoked"}")
    appendLine("Mode request thread: ${experiment.modeRequestThread ?: "Not invoked"}")
    appendLine("Mode immediately before request: ${experiment.modeImmediatelyBeforeRequest ?: "Not invoked"}")
    appendLine("Mode immediately after request: ${experiment.modeImmediatelyAfterRequest ?: "Not invoked"}")
    appendLine("Mode request exception: ${experiment.modeRequestException ?: "None"}")
    appendLine("MODE_IN_COMMUNICATION observed after request: ${experiment.modeInCommunicationObserved}")
    appendLine("Earpiece request occurred after explicit mode request: ${experiment.earpieceRequestAfterExplicitModeRequest}")
    appendLine("Selected target: ${experiment.selectedTarget.reportDescription()}")
    appendLine("Routing request accepted: ${experiment.requestAccepted ?: "Not attempted"}")
    appendLine("Total routing attempts: ${experiment.attempts.size}")
    appendLine("Android reported earpiece while external communication remained active: ${experiment.earpieceReportedDuringSession}")
    appendLine("External/system playback subsequently reclaimed speaker: ${experiment.revertedToSpeaker}")
    appendLine("External voice playback device after routing request where observable: ${experiment.externalVoicePlaybackDeviceAfterRequest.reportDescription()} (public diagnostics do not identify the client)")
    appendLine("Silent AudioTrack cleanup completed: ${experiment.silentTrackCleanupCompleted}")
    appendLine("Audible result requiring human confirmation: UNKNOWN")
    appendLine()
    appendSnapshot("PRE-POC5", experiment.preOwnership ?: baseline)
    appendSnapshot("POST-SILENT-TRACK-START", experiment.postSilentTrackStart)
    appendSnapshot("POST-MODE-REQUEST", experiment.postModeOwnership)
    appendSnapshot("POST-ROUTING-REQUEST", experiment.postRoutingRequest, experiment.requestAccepted)
    appendSnapshot("DELAYED OBSERVATION", experiment.shortObservation)
    appendLine("Earpiece reported while external communication remained active: ${experiment.earpieceReportedDuringSession}")
    appendLine("Speaker subsequently reclaimed: ${experiment.revertedToSpeaker}")
    appendLine()
    appendLine("ROUTING ATTEMPTS")
    if (experiment.attempts.isEmpty()) appendLine("None") else experiment.attempts.forEach { attempt ->
        appendLine("Attempt ${attempt.number}: timestamp=${attempt.timestamp}; trigger=${attempt.trigger}; mode=${attempt.mode}; device before=${attempt.deviceBefore.reportDescription()}; return=${attempt.accepted}; device immediately after=${attempt.deviceImmediatelyAfter.reportDescription()}; speakerphone immediately after=${attempt.speakerphoneImmediatelyAfter}")
    }
    appendLine()
    appendCompletedRoutingCycle(lastCompletedExperiment)
    appendLine()
    appendLine("CURRENT STATE")
    appendLine("AudioManager mode: ${snapshot.mode}")
    appendLine("Communication device: ${snapshot.communicationDevice.reportDescription()}")
    appendLine("Final reported communication device: ${snapshot.communicationDevice.reportDescription()}")
    appendLine("Speakerphone: ${snapshot.speakerphoneState}")
    appendLine("Cleanup result: silent track cleanup completed=${experiment.silentTrackCleanupCompleted}; experiment state=${experiment.state.label}")
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
    appendLine()
    appendLine()
    appendLine("ADB CORRELATION")
    appendLine("Private Audio PID: ${Process.myPid()}")
    appendLine("Private Audio UID: ${Process.myUid()}")
    appendLine("Report timestamp: $timestamp")
    appendLine("Capture before cleanup with: adb shell dumpsys audio > audio-poc5.txt")
    append("This report does not claim actual mode ownership; verify it externally.")
}

private fun StringBuilder.appendCompletedRoutingCycle(cycle: CompletedRoutingCycle?) {
    appendLine("LAST COMPLETED ROUTING CYCLE")
    if (cycle == null) {
        appendLine("None recorded")
        return
    }

    val completed = cycle.experiment
    appendLine("Completed at: ${cycle.completedAt}")
    appendLine("Completion reason: ${cycle.completionReason}")
    appendLine("Trigger origin: ${completed.triggerOrigin ?: "Not recorded"}")
    appendLine("Browser qualifying VOICE_COMMUNICATION/UNKNOWN count: ${completed.browserQualifyingPlaybackCount}")
    appendLine("Mode before participation: ${completed.modeBeforeParticipation ?: "Not recorded"}")
    appendLine("Routing request attempted: ${completed.requestAttempted}")
    appendLine("Routing request accepted: ${completed.requestAccepted ?: "Not attempted"}")
    appendLine("Total routing attempts: ${completed.attempts.size}")
    appendLine("Selected target: ${completed.selectedTarget.reportDescription()}")
    appendLine("Earpiece reported during session: ${completed.earpieceReportedDuringSession}")
    appendLine("Speaker subsequently reclaimed: ${completed.revertedToSpeaker}")
    appendLine("Silent AudioTrack cleanup completed: ${completed.silentTrackCleanupCompleted}")
    appendLine("Final mode after cleanup: ${cycle.finalCleanupObservation.mode}")
    appendSnapshot("COMPLETED PRE-POC5", completed.preOwnership)
    appendSnapshot("COMPLETED POST-SILENT-TRACK-START", completed.postSilentTrackStart)
    appendSnapshot("COMPLETED POST-MODE-REQUEST", completed.postModeOwnership)
    appendSnapshot("COMPLETED POST-ROUTING-REQUEST", completed.postRoutingRequest, completed.requestAccepted)
    appendSnapshot("COMPLETED DELAYED OBSERVATION", completed.shortObservation)
    appendSnapshot("COMPLETED FINAL CLEANUP OBSERVATION", cycle.finalCleanupObservation)
    appendLine("COMPLETED ROUTING ATTEMPTS")
    completed.attempts.forEach { attempt ->
        appendLine(
            "Attempt ${attempt.number}: timestamp=${attempt.timestamp}; trigger=${attempt.trigger}; " +
                "mode=${attempt.mode}; accepted=${attempt.accepted}; " +
                "device before=${attempt.deviceBefore.reportDescription()}; " +
                "device immediately after=${attempt.deviceImmediatelyAfter.reportDescription()}",
        )
    }
}

private fun StringBuilder.appendIdentity(label: String, snapshot: DiagnosticSnapshot?) {
    if (snapshot == null) {
        appendLine("$label: Not recorded")
    } else {
        appendLine("$label: timestamp=${snapshot.timestamp}; PID=${snapshot.processId}; UID=${snapshot.userId}; process=${snapshot.lifecycleState}")
    }
}

private fun StringBuilder.appendSnapshot(
    heading: String,
    snapshot: DiagnosticSnapshot?,
    requestAccepted: Boolean? = null,
) {
    appendLine(heading)
    if (snapshot == null) {
        appendLine("Not recorded")
        appendLine()
        return
    }
    appendLine("Timestamp: ${snapshot.timestamp}")
    appendLine("PID / UID: ${snapshot.processId} / ${snapshot.userId}")
    appendLine("Process state: ${snapshot.lifecycleState}")
    appendLine("AudioManager mode: ${snapshot.mode}")
    appendLine("Communication device: ${snapshot.communicationDevice.reportDescription()}")
    appendLine("Speakerphone: ${snapshot.speakerphoneState}")
    appendLine("Available communication devices: ${snapshot.availableCommunicationDevices.joinToString { it.reportDescription() }}")
    requestAccepted?.let { appendLine("setCommunicationDevice return value: $it") }
    appendLine("Active playback configurations:")
    if (snapshot.activePlaybackConfigurations.isEmpty()) appendLine("  None visible")
    snapshot.activePlaybackConfigurations.forEachIndexed { index, playback ->
        appendLine("  ${index + 1}. usage=${playback.usage}; content=${playback.contentType}; flags=${playback.flags}; capture policy=${playback.allowedCapturePolicy}; player state=${playback.playerState}; player/session identity=${playback.playerIdentity}; device=${playback.device.reportDescription()}")
    }
    appendLine()
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

private fun AudioPlaybackConfiguration.toObservedPlayback() = ObservedPlayback(
    usage = audioUsageName(audioAttributes.usage),
    contentType = audioContentTypeName(audioAttributes.contentType),
    flags = audioFlagsName(audioAttributes.flags),
    allowedCapturePolicy = capturePolicyName(audioAttributes.allowedCapturePolicy),
    device = audioDeviceInfo?.toObservedDevice(),
)

private fun AudioRecordingConfiguration.toObservedRecording() = ObservedRecording(
    audioSource = audioSourceName(clientAudioSource),
    clientSessionId = clientAudioSessionId.takeIf { it > 0 }?.toString() ?: "Unknown / redacted / not exposed",
    clientFormat = clientFormat.let {
        "encoding=${it.encoding}; sampleRate=${it.sampleRate}; channelMask=0x${it.channelMask.toUInt().toString(16)}"
    },
    device = audioDevice?.toObservedDevice(),
    clientSilenced = isClientSilenced,
)

internal fun audioSourceName(source: Int) = when (source) {
    android.media.MediaRecorder.AudioSource.DEFAULT -> "DEFAULT"
    android.media.MediaRecorder.AudioSource.MIC -> "MIC"
    android.media.MediaRecorder.AudioSource.VOICE_UPLINK -> "VOICE_UPLINK"
    android.media.MediaRecorder.AudioSource.VOICE_DOWNLINK -> "VOICE_DOWNLINK"
    android.media.MediaRecorder.AudioSource.VOICE_CALL -> "VOICE_CALL"
    android.media.MediaRecorder.AudioSource.CAMCORDER -> "CAMCORDER"
    android.media.MediaRecorder.AudioSource.VOICE_RECOGNITION -> "VOICE_RECOGNITION"
    android.media.MediaRecorder.AudioSource.VOICE_COMMUNICATION -> "VOICE_COMMUNICATION"
    android.media.MediaRecorder.AudioSource.UNPROCESSED -> "UNPROCESSED"
    else -> "Unknown / redacted / not exposed ($source)"
}

private fun List<ObservedRecording>?.recordingDescription(): String = when {
    this == null -> "Not available"
    isEmpty() -> "None reported by Android"
    else -> joinToString(" | ") { it.reportDescription() }
}

private fun Boolean?.yesNoUnavailable(): String = when (this) {
    true -> "yes"
    false -> "no"
    null -> "Not available"
}

private fun recordingAppearanceToTriggerMs(
    startup: RecordingStartupObservation?,
    trace: List<RecordingTraceEntry>,
): Long? {
    if (startup == null || startup.atRoutingTrigger.isEmpty()) return null
    val appeared = trace.lastOrNull {
        it.elapsedRealtimeNanos <= startup.routingTriggerElapsedRealtimeNanos &&
            it.description.contains("RECORDING appeared")
    } ?: return null
    val invalidated = trace.any {
        it.elapsedRealtimeNanos in (appeared.elapsedRealtimeNanos + 1)..startup.routingTriggerElapsedRealtimeNanos &&
            it.description.contains("RECORDING disappeared")
    }
    return if (invalidated) null else startupDurationForReport(appeared.elapsedRealtimeNanos, startup.routingTriggerElapsedRealtimeNanos)
}

private fun recordingTransitionAfterTriggerMs(
    startup: RecordingStartupObservation?,
    trace: List<RecordingTraceEntry>,
    marker: String,
): Long? {
    if (startup == null) return null
    val event = trace.firstOrNull {
        it.context.routingGeneration == startup.generation &&
            it.elapsedRealtimeNanos >= startup.routingTriggerElapsedRealtimeNanos &&
            it.description.contains(marker)
    } ?: return null
    return startupDurationForReport(startup.routingTriggerElapsedRealtimeNanos, event.elapsedRealtimeNanos)
}

private fun startupDurationForReport(startNanos: Long, endNanos: Long?): Long? =
    endNanos?.takeIf { it >= startNanos }?.let { (it - startNanos) / 1_000_000L }

internal data class PlaybackChanges(val summary: String, val entries: List<String>)

internal fun playbackChanges(
    previous: List<ObservedPlayback>,
    current: List<ObservedPlayback>,
): PlaybackChanges {
    val remainingPrevious = previous.toMutableList()
    val added = current.filter { observed -> !remainingPrevious.remove(observed) }
    val remainingCurrent = current.toMutableList()
    val removed = previous.filter { observed -> !remainingCurrent.remove(observed) }
    val unchangedCount = current.size - added.size
    val summary = "previous=${previous.size}; current=${current.size}; added=${added.size}; " +
        "removed=${removed.size}; unchanged=$unchangedCount"
    val entries = buildList {
        added.forEach { add("playback appeared/started — ${it.traceDescription()}") }
        removed.forEach { add("playback disappeared/stopped — ${it.traceDescription()}") }
    }
    return PlaybackChanges(summary, entries)
}

private fun ObservedPlayback.traceDescription() =
    "usage=$usage; contentType=$contentType; flags=$flags; capturePolicy=$allowedCapturePolicy; " +
        "playerState=$playerState; player/session=$playerIdentity; device=${device.reportDescription()}"

internal fun audioFlagsName(flags: Int): String =
    if (flags == 0) "FLAGS_NONE (0x0)" else "0x${flags.toUInt().toString(16)}"

internal fun audioUsageName(usage: Int) = when (usage) {
    AudioAttributes.USAGE_VOICE_COMMUNICATION -> "USAGE_VOICE_COMMUNICATION"
    AudioAttributes.USAGE_MEDIA -> "USAGE_MEDIA"
    AudioAttributes.USAGE_ASSISTANT -> "USAGE_ASSISTANT"
    AudioAttributes.USAGE_ASSISTANCE_SONIFICATION -> "USAGE_ASSISTANCE_SONIFICATION"
    AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY -> "USAGE_ASSISTANCE_ACCESSIBILITY"
    AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE -> "USAGE_ASSISTANCE_NAVIGATION_GUIDANCE"
    AudioAttributes.USAGE_NOTIFICATION -> "USAGE_NOTIFICATION"
    AudioAttributes.USAGE_NOTIFICATION_EVENT -> "USAGE_NOTIFICATION_EVENT"
    AudioAttributes.USAGE_ALARM -> "USAGE_ALARM"
    AudioAttributes.USAGE_GAME -> "USAGE_GAME"
    AudioAttributes.USAGE_UNKNOWN -> "USAGE_UNKNOWN"
    else -> "Usage $usage"
}

internal fun audioContentTypeName(contentType: Int) = when (contentType) {
    AudioAttributes.CONTENT_TYPE_SPEECH -> "CONTENT_TYPE_SPEECH"
    AudioAttributes.CONTENT_TYPE_MUSIC -> "CONTENT_TYPE_MUSIC"
    AudioAttributes.CONTENT_TYPE_MOVIE -> "CONTENT_TYPE_MOVIE"
    AudioAttributes.CONTENT_TYPE_SONIFICATION -> "CONTENT_TYPE_SONIFICATION"
    AudioAttributes.CONTENT_TYPE_UNKNOWN -> "CONTENT_TYPE_UNKNOWN"
    else -> "Content type $contentType"
}

private fun capturePolicyName(policy: Int) = when (policy) {
    AudioAttributes.ALLOW_CAPTURE_BY_ALL -> "ALLOW_CAPTURE_BY_ALL"
    AudioAttributes.ALLOW_CAPTURE_BY_SYSTEM -> "ALLOW_CAPTURE_BY_SYSTEM"
    AudioAttributes.ALLOW_CAPTURE_BY_NONE -> "ALLOW_CAPTURE_BY_NONE"
    else -> "Policy $policy"
}

private fun ObservedDevice?.shortName() = this?.let { "${it.type} (${it.productName})" } ?: "None reported"

private fun ObservedDevice?.reportDescription() = this?.let {
    "type=${it.type}; product=${it.productName}; Android device ID=${it.id}"
} ?: "None reported by Android"

private fun Throwable.exactDescription() =
    "${javaClass.name}: ${message ?: "No message"}"

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
internal const val DIAGNOSTIC_REPORT_FORMAT = 2
internal const val MAX_EVENTS = 100
internal const val MAX_STARTUP_TRACE_EVENTS = 240
internal const val MAX_RECORDING_TRACE_EVENTS = 80
private const val OBSERVATION_DELAY_MS = 1_000L
private const val END_CONFIRMATION_DELAY_MS = 1_500L
private const val ASSISTANT_SESSION_LINGER_MS = 7_000L
private const val ASSISTANT_EARLY_ROUTE_TIMEOUT_MS = 10_000L
private const val DEFAULT_SAMPLE_RATE = 48_000
private const val MIN_SILENCE_BUFFER_BYTES = 1_024
private const val SILENCE_WRITER_PAUSE_MS = 10L
private const val SILENCE_WRITER_JOIN_MS = 250L

private fun audioTrackPlayStateName(state: Int) = when (state) {
    AudioTrack.PLAYSTATE_STOPPED -> "PLAYSTATE_STOPPED"
    AudioTrack.PLAYSTATE_PAUSED -> "PLAYSTATE_PAUSED"
    AudioTrack.PLAYSTATE_PLAYING -> "PLAYSTATE_PLAYING"
    else -> "Unknown play state ($state)"
}
