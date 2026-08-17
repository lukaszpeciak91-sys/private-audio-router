package app.privateaudio

import android.content.ComponentName
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.privateaudio.overlay.OverlayService
import app.privateaudio.ui.PrivateAudioScreen
import app.privateaudio.ui.theme.PrivateAudioTheme

class MainActivity : ComponentActivity() {
    private var service by mutableStateOf<PrivateAudioService?>(null)
    private var isBound = false
    private var overlayPermissionRequestPending = false
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            service = (binder as PrivateAudioService.LocalBinder).service
            isBound = true
            service?.recordSnapshot("App foregrounded")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )

        setContent {
            PrivateAudioTheme {
                val connectedService = service
                val state = connectedService?.privateAudioState ?: PrivateAudioState.READY
                PrivateAudioScreen(
                    state = state,
                    powerEnabled = connectedService != null,
                    onPowerClick = {
                        if (state == PrivateAudioState.READY) {
                            startForegroundService(
                                Intent(this, PrivateAudioService::class.java)
                                    .setAction(PrivateAudioService.ACTION_ARM),
                            )
                        } else {
                            connectedService?.disarmAndStopStartedLifetime()
                        }
                    },
                    onFloatingClick = { showOverlayOrRequestPermission() },
                    onCloseClick = {
                        hideOverlay()
                        connectedService?.disarmAndStopStartedLifetime()
                        finishAndRemoveTask()
                    },
                    onCopyDiagnosticReport = {
                        connectedService?.let { activeService ->
                            val report = activeService.diagnosticReport()
                            getSystemService(ClipboardManager::class.java).setPrimaryClip(
                                ClipData.newPlainText(
                                    getString(R.string.diagnostic_report_clip_label),
                                    report,
                                ),
                            )
                            Toast.makeText(
                                this,
                                getString(R.string.diagnostic_report_copied),
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    },
                    versionName = BuildConfig.VERSION_NAME,
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        isBound = bindService(
            Intent(this, PrivateAudioService::class.java),
            serviceConnection,
            Context.BIND_AUTO_CREATE,
        )
    }

    override fun onResume() {
        super.onResume()
        if (overlayPermissionRequestPending) {
            overlayPermissionRequestPending = false
            if (Settings.canDrawOverlays(this)) showOverlay()
        }
    }

    override fun onStop() {
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
            service = null
        }
        super.onStop()
    }

    private fun showOverlayOrRequestPermission() {
        if (Settings.canDrawOverlays(this)) {
            showOverlay()
            return
        }
        overlayPermissionRequestPending = true
        startActivity(
            Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName"),
            ),
        )
    }

    private fun showOverlay() {
        startService(OverlayService.showIntent(this))
    }

    private fun hideOverlay() {
        startService(OverlayService.hideIntent(this))
    }
}
