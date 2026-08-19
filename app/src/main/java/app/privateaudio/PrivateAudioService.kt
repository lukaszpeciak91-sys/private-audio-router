package app.privateaudio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.Context
import android.content.res.Configuration
import android.content.pm.ServiceInfo
import android.media.AudioManager
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.privateaudio.diagnostic.AudioDiagnosticObserver
import app.privateaudio.diagnostic.ExperimentState

class PrivateAudioService : Service() {
    inner class LocalBinder : Binder() {
        val service: PrivateAudioService
            get() = this@PrivateAudioService
    }

    private val binder = LocalBinder()
    private var shuttingDown = false
    private var foregroundNotificationActive = false

    private val proximityController: ProximityScreenController by lazy(LazyThreadSafetyMode.NONE) {
        ProximityScreenController(getSystemService(PowerManager::class.java)) { event ->
            observer.recordLifecycleEvent(event)
        }
    }

    var isPrivateAudioEnabled by mutableStateOf(false)
        private set

    var isProximityFeatureEnabled by mutableStateOf(true)
        private set

    val privateAudioState: PrivateAudioState
        get() {
            val currentExperiment = observer.experiment
            val currentSnapshot = observer.snapshot
            return projectPrivateAudioState(
                PrivateAudioStateEvidence(
                    controllerEnabled = isPrivateAudioEnabled,
                    currentProtectedFailure = currentExperiment.state == ExperimentState.BLOCKED ||
                        currentExperiment.requestAccepted == false,
                    currentCycleParticipating =
                        currentExperiment.state == ExperimentState.REQUEST_ATTEMPTED &&
                            currentExperiment.silentTrackStarted &&
                            !currentExperiment.silentTrackCleanupCompleted,
                    modeInCommunication = currentSnapshot.mode == "MODE_IN_COMMUNICATION",
                    builtInEarpieceIsCurrent =
                        currentSnapshot.communicationDevice?.type == "Built-in earpiece",
                ),
            )
        }

    val observer: AudioDiagnosticObserver by lazy(LazyThreadSafetyMode.NONE) {
        AudioDiagnosticObserver(
            context = applicationContext,
            audioManager = getSystemService(AudioManager::class.java),
            callbackExecutor = mainExecutor,
            onEvidenceChanged = ::syncProximityBehavior,
        )
    }

    override fun onCreate() {
        super.onCreate()
        isProximityFeatureEnabled = getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .getBoolean(PROXIMITY_FEATURE_KEY, true)
        observer.start()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_ARM) {
            isPrivateAudioEnabled = true
            enterForeground()
            observer.enableController()
            syncProximityBehavior("Power ON")
        }
        return START_NOT_STICKY
    }

    fun disarmAndStopStartedLifetime() {
        isPrivateAudioEnabled = false
        proximityController.release("Power OFF", privateAudioState, currentRoute())
        observer.disableController()
        foregroundNotificationActive = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    fun recordSnapshot(reason: String) {
        observer.snapshot(reason)
    }

    fun recordDiagnosticSaveEvent(event: String) {
        observer.recordLifecycleEvent(event)
    }

    fun updateProximityFeatureEnabled(enabled: Boolean) {
        if (enabled == isProximityFeatureEnabled) return
        isProximityFeatureEnabled = enabled
        getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(PROXIMITY_FEATURE_KEY, enabled)
            .apply()
        syncProximityBehavior(if (enabled) "Preference enabled" else "Preference disabled")
    }

    fun diagnosticReport(): String {
        observer.snapshot("Report snapshot")
        val proximity = proximityController.status()
        return buildString {
            append(observer.report())
            appendLine()
            appendLine("PROXIMITY SCREEN")
            appendLine("Feature enabled: $isProximityFeatureEnabled")
            appendLine("Wake lock supported: ${proximity.supported}")
            appendLine("Wake lock currently held: ${proximity.held}")
            appendLine("Last acquire reason: ${proximity.lastAcquireReason ?: "None"}")
            appendLine("Last release reason: ${proximity.lastReleaseReason ?: "None"}")
            appendLine("State at last transition: ${proximity.stateAtLastTransition ?: "None"}")
            appendLine("Route at last transition: ${proximity.routeAtLastTransition ?: "None"}")
        }
    }

    override fun onDestroy() {
        shuttingDown = true
        isPrivateAudioEnabled = false
        foregroundNotificationActive = false
        proximityController.release("Service destroyed", privateAudioState, currentRoute())
        observer.stop("Service destroyed")
        super.onDestroy()
    }

    private fun syncProximityBehavior(reason: String) {
        val state = privateAudioState
        val route = currentRoute()
        val supported = proximityController.status().supported
        if (proximityEligible(isProximityFeatureEnabled, isPrivateAudioEnabled, state, observer.snapshot.mode, route, supported)) {
            proximityController.acquire(reason, state, route ?: "Unknown")
        } else {
            proximityController.release(reason, state, route)
        }
    }

    private fun currentRoute() = observer.snapshot.communicationDevice?.type

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (foregroundNotificationActive) {
            getSystemService(NotificationManager::class.java).notify(
                NOTIFICATION_ID,
                buildForegroundNotification(),
            )
        }
    }

    private fun enterForeground() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(
            NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                getString(R.string.routing_notification_channel),
                NotificationManager.IMPORTANCE_LOW,
            ),
        )
        val notification = buildForegroundNotification()

        if (android.os.Build.VERSION.SDK_INT >= 34) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        foregroundNotificationActive = true
    }

    private fun buildForegroundNotification(): Notification {
        val openMain = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(getString(R.string.routing_notification_title))
            .setContentText(getString(R.string.routing_notification_text))
            .setContentIntent(openMain)
            .setOngoing(true)
            .build()
    }

    companion object {
        const val ACTION_ARM = "app.privateaudio.action.ARM_EARPIECE_TEST"
        private const val NOTIFICATION_CHANNEL_ID = "private_audio_routing"
        private const val NOTIFICATION_ID = 1
        private const val PREFERENCES_NAME = "private_audio_preferences"
        private const val PROXIMITY_FEATURE_KEY = "proximity_screen_enabled"
    }
}
