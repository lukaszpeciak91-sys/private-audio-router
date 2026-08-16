package app.privateaudio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioManager
import android.os.Binder
import android.os.IBinder
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

    var isPrivateAudioEnabled by mutableStateOf(false)
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
        )
    }

    override fun onCreate() {
        super.onCreate()
        observer.start()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_ARM) {
            isPrivateAudioEnabled = true
            enterForeground()
            observer.enableController()
        }
        return START_NOT_STICKY
    }

    fun disarmAndStopStartedLifetime() {
        isPrivateAudioEnabled = false
        observer.disableController()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    fun recordSnapshot(reason: String) {
        observer.snapshot(reason)
    }

    fun diagnosticReport(): String {
        observer.snapshot("Report snapshot")
        return observer.report()
    }

    override fun onDestroy() {
        shuttingDown = true
        isPrivateAudioEnabled = false
        observer.stop("Service destroyed")
        super.onDestroy()
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
        val openMain = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(getString(R.string.routing_notification_title))
            .setContentText(getString(R.string.routing_notification_text))
            .setContentIntent(openMain)
            .setOngoing(true)
            .build()

        if (android.os.Build.VERSION.SDK_INT >= 34) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    companion object {
        const val ACTION_ARM = "app.privateaudio.action.ARM_EARPIECE_TEST"
        private const val NOTIFICATION_CHANNEL_ID = "private_audio_routing"
        private const val NOTIFICATION_ID = 1
    }
}
