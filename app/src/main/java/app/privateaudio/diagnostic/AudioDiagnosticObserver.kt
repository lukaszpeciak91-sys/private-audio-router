package app.privateaudio.diagnostic

import android.app.ActivityManager
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioPlaybackConfiguration
import android.media.AudioTrack
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Process
import app.privateaudio.BuildConfig
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

data class FakePhonePreArmStatus(
    val featureEnabled: Boolean = false,
    val active: Boolean = false,
    val startedAt: String? = null,
    val modeBeforeParticipation: String? = null,
    val silentTrackCreated: Boolean = false,
    val silentTrackStarted: Boolean = false,
    val modeRequestAttempted: Boolean = false,
    val modeAfterRequest: String? = null,
    val targetEarpiece: ObservedDevice? = null,
    val routeRequestAttempted: Boolean = false,
    val routeRequestAccepted: Boolean? = null,
    val reportedDeviceAfterRequest: ObservedDevice? = null,
    val assistantSonificationObservedAt: String? = null,
    val assistantSonificationDevice: ObservedDevice? = null,
    val cleanupCompleted: Boolean = false,
    val lastCleanupReason: String? = null,
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

    var fakePhonePreArm by mutableStateOf(FakePhonePreArmStatus())
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
    private var cycleGeneration = 0L
    private var externalContributionEstablished = false
    private var activeEvidenceRecorded = false
    private var routingActionInProgress = false
    private var modeParticipationActive = false
    private var silentTrack: AudioTrack? = null
    private var silentWriterThread: Thread? = null
    private val silentWriterRunning = AtomicBoolean(false)
    private val observationHandler = Handler(Looper.getMainLooper())
    private var pendingObservation: Runnable? = null
    private var pendingEndConfirmation: Runnable? = null
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

    fun start() {
        if (started) return
        started = true
        audioManager.addOnCommunicationDeviceChangedListener(
            callbackExecutor,
            communicationDeviceListener,
        )
        audioManager.registerAudioDeviceCallback(audioDeviceCallback, Handler(Looper.getMainLooper()))
        snapshot("Baseline")
        baseline = snapshot
    }

    fun stop(reason: String) {
        if (!started) return
        controllerEnabled = false
        invalidatePendingControllerWork()
        unregisterPlaybackCallback()
        if (experiment.requestAttempted) clearExperiment(reason, ExperimentState.CLEARED)
        else cleanupFakePhonePreArm(reason)
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
        if (fakePhonePreArm.active && (
                audioManager.mode.isTelephonyOrSystemPriorityMode() ||
                    audioManager.availableCommunicationDevices.none { it.type == AudioDeviceInfo.TYPE_BUILTIN_EARPIECE }
                )
        ) cleanupFakePhonePreArm("Pre-arm eligibility lost: $reason")
        observeExperimentOutcome(observed, reason)
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
        handlePlaybackConfigurations(audioManager.activePlaybackConfigurations)
        maybeStartFakePhonePreArm()
    }

    fun disableController() {
        controllerEnabled = false
        invalidatePendingControllerWork()
        unregisterPlaybackCallback()
        addEvent("Controller OFF — pending detection invalidated")
        if (experiment.requestAttempted) clearExperiment("Power OFF", ExperimentState.CLEARED)
        else cleanupFakePhonePreArm("Power OFF")
    }

    fun updateFakePhonePreArmEnabled(enabled: Boolean) {
        if (fakePhonePreArm.featureEnabled == enabled) return
        fakePhonePreArm = fakePhonePreArm.copy(featureEnabled = enabled)
        addEvent("Fake Phone pre-arm preference ${if (enabled) "enabled" else "disabled"}")
        if (!controllerEnabled || experiment.requestAttempted) return
        if (enabled) maybeStartFakePhonePreArm() else cleanupFakePhonePreArm("Preference disabled")
        onEvidenceChanged("Fake Phone pre-arm preference changed")
    }

    fun recordLifecycleEvent(message: String) {
        addEvent(message)
    }

    fun report(): String = buildDiagnosticReport(
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
        fakePhonePreArm = fakePhonePreArm,
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
            if (fakePhonePreArm.active) {
                cleanupFakePhonePreArm("Blocked by system/telephony-priority mode ${audioModeName(mode)}")
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
            qualifyingPlaybackCount(configs) > (if (fakePhonePreArm.active) 1 else 0) &&
            (fakePhonePreArm.active || audioManager.communicationDevice?.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER)
        val assistantCount = assistantQualifyingPlaybackCount(configs)
        currentAssistantQualifyingPlaybackCount = assistantCount
        val assistantTrigger = assistantCount > 0
        val browserCount = browserQualifyingPlaybackCount(configs)
        currentBrowserQualifyingPlaybackCount = browserCount
        val browserTrigger = mode == AudioManager.MODE_IN_COMMUNICATION &&
            browserCount > 0 &&
            (fakePhonePreArm.active || audioManager.communicationDevice?.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER)
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
        )
        routingActionInProgress = true
        if (fakePhonePreArm.active) {
            promoteFakePhonePreArm(triggerOrigin, assistantCount, browserCount)
            routingActionInProgress = false
            snapshot("Pre-arm promoted to protected routing cycle")
            scheduleShortObservation()
            return
        }
        if (!startSilentCommunicationTrack()) {
            routingActionInProgress = false
            clearExperiment("Silent communication AudioTrack could not be started", ExperimentState.BLOCKED)
            return
        }
        val postTrackStart = collectSnapshot()
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
        experiment = experiment.copy(
            modeImmediatelyAfterRequest = audioModeName(modeAfterParticipation),
            modeRequestException = modeRequestFailure?.exactDescription(),
            modeInCommunicationObserved = modeAfterParticipation == AudioManager.MODE_IN_COMMUNICATION,
        )
        addEvent(
            "Explicit setMode returned — requested=MODE_IN_COMMUNICATION; " +
                "Android-reported mode=${audioModeName(modeAfterParticipation)}; ${currentStateDescription()}",
        )
        experiment = experiment.copy(postModeOwnership = collectSnapshot())
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

    private fun startSilentCommunicationTrack(): Boolean {
        val sampleRate = AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_VOICE_CALL)
            .takeIf { it > 0 } ?: DEFAULT_SAMPLE_RATE
        val minBuffer = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        )
        if (minBuffer <= 0) {
            addEvent("Silent AudioTrack creation failed — unsupported buffer result=$minBuffer at ${sampleRate}Hz")
            return false
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
            return false
        }
        experiment = experiment.copy(
            silentTrackCreated = true,
            silentTrackSampleRate = sampleRate,
            silentTrackBufferBytes = bufferBytes,
        )
        if (track.state != AudioTrack.STATE_INITIALIZED) {
            addEvent("Silent AudioTrack creation produced uninitialized state=${track.state}")
            track.release()
            return false
        }
        silentTrack = track
        val silence = ShortArray(bufferBytes / Short.SIZE_BYTES)
        silentWriterRunning.set(true)
        silentWriterThread = Thread({
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
            while (silentWriterRunning.get()) {
                val written = track.write(silence, 0, silence.size, AudioTrack.WRITE_NON_BLOCKING)
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
        val started = runCatching { track.play() }.isSuccess && track.playState == AudioTrack.PLAYSTATE_PLAYING
        experiment = experiment.copy(
            silentTrackStarted = started,
            silentTrackPlayState = audioTrackPlayStateName(track.playState),
        )
        addEvent(
            "Silent AudioTrack creation result — initialized=true; sampleRate=${sampleRate}Hz; " +
                "buffer=$bufferBytes bytes; attributes=USAGE_VOICE_COMMUNICATION/CONTENT_TYPE_SPEECH; " +
                "playState=${audioTrackPlayStateName(track.playState)}; audio focus requested=false",
        )
        return started
    }

    private fun maybeStartFakePhonePreArm() {
        if (!controllerEnabled || !fakePhonePreArm.featureEnabled || fakePhonePreArm.active ||
            experiment.requestAttempted || routingActionInProgress
        ) return
        val mode = audioManager.mode
        if (mode.isTelephonyOrSystemPriorityMode()) {
            cleanupFakePhonePreArm("Blocked by system/telephony-priority mode ${audioModeName(mode)}")
            return
        }
        val earpiece = audioManager.availableCommunicationDevices.firstOrNull {
            it.type == AudioDeviceInfo.TYPE_BUILTIN_EARPIECE
        } ?: run {
            cleanupFakePhonePreArm("Built-in earpiece unavailable")
            return
        }
        routingActionInProgress = true
        val startedAt = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        val modeBefore = audioModeName(mode)
        fakePhonePreArm = FakePhonePreArmStatus(
            featureEnabled = true,
            startedAt = startedAt,
            modeBeforeParticipation = modeBefore,
            targetEarpiece = earpiece.toObservedDevice(),
        )
        addEvent("Fake Phone pre-arm starting while controller remains WAITING")
        if (!startSilentCommunicationTrack()) {
            routingActionInProgress = false
            cleanupFakePhonePreArm("Silent communication AudioTrack could not be started")
            return
        }
        fakePhonePreArm = fakePhonePreArm.copy(
            silentTrackCreated = experiment.silentTrackCreated,
            silentTrackStarted = experiment.silentTrackStarted,
            modeRequestAttempted = true,
        )
        modeParticipationActive = true
        val modeFailure = runCatching { requestCommunicationMode() }.exceptionOrNull()
        val modeAfter = audioManager.mode
        fakePhonePreArm = fakePhonePreArm.copy(modeAfterRequest = audioModeName(modeAfter))
        if (modeFailure != null || modeAfter != AudioManager.MODE_IN_COMMUNICATION) {
            routingActionInProgress = false
            cleanupFakePhonePreArm("MODE_IN_COMMUNICATION pre-arm request failed")
            return
        }
        val accepted = requestCommunicationDevice(earpiece)
        fakePhonePreArm = fakePhonePreArm.copy(
            active = accepted,
            routeRequestAttempted = true,
            routeRequestAccepted = accepted,
            reportedDeviceAfterRequest = audioManager.communicationDevice?.toObservedDevice(),
        )
        routingActionInProgress = false
        if (!fakePhonePreArm.active) {
            cleanupFakePhonePreArm("Built-in earpiece was not established")
            return
        }
        addEvent("Fake Phone pre-arm active — protected context prepared; public state remains WAITING")
        snapshot("Fake Phone pre-arm established")
    }

    private fun promoteFakePhonePreArm(origin: TriggerOrigin, assistantCount: Int, browserCount: Int) {
        val now = LocalTime.now().format(TIME_FORMAT)
        val target = fakePhonePreArm.targetEarpiece
        val attempt = RoutingAttempt(
            number = 1,
            timestamp = now,
            trigger = "healthy Fake Phone pre-arm promoted; no duplicate route request",
            mode = audioModeName(audioManager.mode),
            deviceBefore = audioManager.communicationDevice?.toObservedDevice(),
            accepted = fakePhonePreArm.routeRequestAccepted == true,
            deviceImmediatelyAfter = audioManager.communicationDevice?.toObservedDevice(),
            speakerphoneImmediatelyAfter = observedSpeakerphoneState(),
        )
        experiment = experiment.copy(
            state = ExperimentState.REQUEST_ATTEMPTED,
            requestAttempted = true,
            selectedTarget = target,
            modeBeforeParticipation = fakePhonePreArm.modeBeforeParticipation,
            silentTrackCreated = true,
            silentTrackStarted = true,
            silentTrackPlayState = audioTrackPlayStateName(silentTrack?.playState ?: AudioTrack.PLAYSTATE_STOPPED),
            explicitModeRequestInvoked = true,
            modeInCommunicationObserved = true,
            requestAccepted = fakePhonePreArm.routeRequestAccepted,
            attempts = listOf(attempt),
            postRoutingRequest = collectSnapshot(),
            assistantQualifyingPlaybackCount = assistantCount,
            browserQualifyingPlaybackCount = browserCount,
            triggerOrigin = origin,
        )
        externalContributionEstablished = true
        fakePhonePreArm = fakePhonePreArm.copy(active = false)
        addEvent("Healthy Fake Phone pre-arm promoted into real external communication cycle — silent track and earpiece request reused")
    }

    private fun cleanupFakePhonePreArm(reason: String) {
        if (!fakePhonePreArm.active && silentTrack == null && !modeParticipationActive) {
            fakePhonePreArm = fakePhonePreArm.copy(cleanupCompleted = true, lastCleanupReason = reason)
            return
        }
        cancelPendingObservation()
        cancelPendingEndConfirmation()
        audioManager.clearCommunicationDevice()
        if (modeParticipationActive) {
            modeParticipationActive = false
            audioManager.mode = AudioManager.MODE_NORMAL
        }
        val cleaned = stopSilentCommunicationTrack()
        fakePhonePreArm = fakePhonePreArm.copy(
            active = false,
            cleanupCompleted = cleaned,
            lastCleanupReason = reason,
        )
        experiment = EarpieceExperiment(state = ExperimentState.ARMED, armed = controllerEnabled)
        addEvent("Fake Phone pre-arm cleanup completed=$cleaned — $reason")
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
        val accepted = requestCommunicationDevice(earpiece)
        val after = audioManager.communicationDevice?.toObservedDevice()
        val speakerphone = observedSpeakerphoneState()
        val attempt = RoutingAttempt(number, timestamp, trigger, mode, before, accepted, after, speakerphone)
        experiment = experiment.copy(
            requestAccepted = accepted,
            attempts = experiment.attempts + attempt,
            earpieceRequestAfterExplicitModeRequest = experiment.explicitModeRequestInvoked,
            postRoutingRequest = collectSnapshot(),
            externalVoicePlaybackDeviceAfterRequest = inferExternalVoicePlaybackDevice(collectSnapshot()),
        )
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
        configs.firstOrNull {
            it.audioAttributes.usage == AudioAttributes.USAGE_ASSISTANT &&
                it.audioAttributes.contentType == AudioAttributes.CONTENT_TYPE_SONIFICATION
        }?.takeIf { fakePhonePreArm.active }?.let { config ->
            val observedAt = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            fakePhonePreArm = fakePhonePreArm.copy(
                assistantSonificationObservedAt = observedAt,
                assistantSonificationDevice = config.audioDeviceInfo?.toObservedDevice(),
            )
            addEvent(
                "ASSISTANT/SONIFICATION observed during pre-arm — timestamp=$observedAt; " +
                    "device=${config.audioDeviceInfo?.toObservedDevice().reportDescription()}; " +
                    "mode=${audioModeName(audioManager.mode)}; communication device=" +
                    audioManager.communicationDevice?.toObservedDevice().reportDescription(),
            )
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
            clearExperiment("External communication playback ended", ExperimentState.CLEARED)
            returnToWaiting()
        }
        pendingEndConfirmation = runnable
        observationHandler.postDelayed(runnable, END_CONFIRMATION_DELAY_MS)
    }

    private fun returnToWaiting() {
        if (!controllerEnabled) return
        cycleGeneration++
        externalContributionEstablished = false
        activeEvidenceRecorded = false
        experiment = EarpieceExperiment(state = ExperimentState.ARMED, armed = true)
        addEvent("Cleanup completed — controller remains ON and returned to clean waiting")
        handlePlaybackConfigurations(audioManager.activePlaybackConfigurations)
        maybeStartFakePhonePreArm()
    }

    private fun unregisterPlaybackCallback() {
        if (!playbackCallbackRegistered) return
        audioManager.unregisterAudioPlaybackCallback(playbackCallback)
        playbackCallbackRegistered = false
    }

    private fun invalidatePendingControllerWork() {
        cycleGeneration++
        cancelPendingEndConfirmation()
    }

    private fun cancelPendingEndConfirmation() {
        pendingEndConfirmation?.let(observationHandler::removeCallbacks)
        pendingEndConfirmation = null
    }

    private fun observeExperimentOutcome(observed: DiagnosticSnapshot, reason: String) {
        if (experiment.attempts.isEmpty() || observed.mode != "MODE_IN_COMMUNICATION") return
        when (observed.communicationDevice?.type) {
            "Built-in earpiece" -> if (!experiment.earpieceReportedDuringSession) {
                experiment = experiment.copy(
                    earpieceReportedDuringSession = true,
                )
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
        if (fakePhonePreArm.startedAt != null) {
            fakePhonePreArm = fakePhonePreArm.copy(
                active = false,
                cleanupCompleted = trackCleanupCompleted,
                lastCleanupReason = reason,
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
        addEvent("Silent AudioTrack cleanup — writer stopped=$writerStopped; track stopped, flushed, and released")
        return writerStopped
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
    fakePhonePreArm: FakePhonePreArmStatus = FakePhonePreArmStatus(),
) = buildString {
    appendLine("PRIVATE AUDIO — DIAGNOSTIC REPORT")
    appendLine("Diagnostic report format: $DIAGNOSTIC_REPORT_FORMAT")
    appendLine("Timestamp: $timestamp")
    appendLine("Package: $packageName")
    appendLine("Private Audio PID: ${Process.myPid()}")
    appendLine("Private Audio UID: ${Process.myUid()}")
    appendLine()
    appendLine("DIAGNOSTIC ENVIRONMENT")
    appendLine("Private Audio version: ${environment.versionName} (${environment.versionCode})")
    appendLine("Android: ${environment.androidRelease}")
    appendLine("API level: ${environment.apiLevel}")
    appendLine("Manufacturer: ${environment.manufacturer}")
    appendLine("Model: ${environment.model}")
    appendLine("Product: ${environment.product}")
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
    appendLine("FAKE PHONE PRE-ARM EXPERIMENT")
    appendLine("Feature enabled: ${fakePhonePreArm.featureEnabled}")
    appendLine("Pre-arm currently active: ${fakePhonePreArm.active}")
    appendLine("Pre-arm start timestamp: ${fakePhonePreArm.startedAt ?: "Not started"}")
    appendLine("Mode before participation: ${fakePhonePreArm.modeBeforeParticipation ?: "Not attempted"}")
    appendLine("Silent communication track created: ${fakePhonePreArm.silentTrackCreated}")
    appendLine("Silent track started: ${fakePhonePreArm.silentTrackStarted}")
    appendLine("Mode request attempted: ${fakePhonePreArm.modeRequestAttempted}")
    appendLine("Mode after request: ${fakePhonePreArm.modeAfterRequest ?: "Not attempted"}")
    appendLine("Target earpiece: ${fakePhonePreArm.targetEarpiece.reportDescription()}")
    appendLine("setCommunicationDevice attempted: ${fakePhonePreArm.routeRequestAttempted}")
    appendLine("setCommunicationDevice return value: ${fakePhonePreArm.routeRequestAccepted ?: "Not attempted"}")
    appendLine("Android-reported communication device after request: ${fakePhonePreArm.reportedDeviceAfterRequest.reportDescription()}")
    appendLine("ASSISTANT/SONIFICATION observed at: ${fakePhonePreArm.assistantSonificationObservedAt ?: "Not observed"}")
    appendLine("ASSISTANT/SONIFICATION device observed during pre-arm: ${fakePhonePreArm.assistantSonificationDevice.reportDescription()}")
    appendLine("Cleanup completed: ${fakePhonePreArm.cleanupCompleted}")
    appendLine("Last cleanup reason: ${fakePhonePreArm.lastCleanupReason ?: "None"}")
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
private const val OBSERVATION_DELAY_MS = 1_000L
private const val END_CONFIRMATION_DELAY_MS = 1_500L
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
