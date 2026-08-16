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
import app.privateaudio.diagnostic.AudioDiagnosticObserver

class PrivateAudioService : Service() {
    inner class LocalBinder : Binder() {
        val service: PrivateAudioService
            get() = this@PrivateAudioService
    }

    private val binder = LocalBinder()
    private var shuttingDown = false

    var isPrivateAudioEnabled = false
        private set

    val observer: AudioDiagnosticObserver by lazy(LazyThreadSafetyMode.NONE) {
        AudioDiagnosticObserver(
            context = applicationContext,
            audioManager = getSystemService(AudioManager::class.java),
            callbackExecutor = mainExecutor,
            onCompletedExperimentCleared = ::onCompletedExperimentCleared,
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
            armFreshExperimentIfSafe("Private Audio enabled — persistent intent is waiting for a voice session")
        }
        return START_NOT_STICKY
    }

    fun disarmAndStopStartedLifetime() {
        isPrivateAudioEnabled = false
        observer.disarmAndClear()
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

    private fun onCompletedExperimentCleared() {
        armFreshExperimentIfSafe(
            "Voice session cleanup completed — persistent Private Audio intent remains enabled",
        )
    }

    private fun armFreshExperimentIfSafe(reason: String) {
        if (!isPrivateAudioEnabled || shuttingDown) return
        if (observer.experiment.armed || observer.isRoutingActionInProgress) return
        observer.recordLifecycleEvent(reason)
        observer.armEarpieceTest()
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
