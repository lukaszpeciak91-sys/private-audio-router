package app.privateaudio

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.privateaudio.ui.DiagnosticScreen
import app.privateaudio.ui.theme.PrivateAudioTheme

class MainActivity : ComponentActivity() {
    private var service by mutableStateOf<PrivateAudioService?>(null)
    private var isBound = false
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
        enableEdgeToEdge()

        setContent {
            PrivateAudioTheme {
                val connectedService = service
                if (connectedService == null) {
                    Text("Connecting to audio diagnostics…")
                } else {
                    DiagnosticScreen(
                        snapshot = connectedService.observer.snapshot,
                        experiment = connectedService.observer.experiment,
                        events = connectedService.observer.events,
                        onArm = {
                            startForegroundService(
                                Intent(this, PrivateAudioService::class.java)
                                    .setAction(PrivateAudioService.ACTION_ARM),
                            )
                        },
                        onDisarm = connectedService::disarmAndStopStartedLifetime,
                        onSnapshot = { connectedService.recordSnapshot("Manual snapshot") },
                        onCopyReport = {
                            val report = connectedService.diagnosticReport()
                            getSystemService(ClipboardManager::class.java).setPrimaryClip(
                                ClipData.newPlainText("Private Audio diagnostic report", report),
                            )
                        },
                    )
                }
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

    override fun onStop() {
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
            service = null
        }
        super.onStop()
    }
}
