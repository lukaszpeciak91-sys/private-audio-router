package app.privateaudio

import android.content.ClipData
import android.content.ClipboardManager
import android.media.AudioManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.privateaudio.diagnostic.AudioDiagnosticObserver
import app.privateaudio.ui.DiagnosticScreen
import app.privateaudio.ui.theme.PrivateAudioTheme

class MainActivity : ComponentActivity() {
    private lateinit var observer: AudioDiagnosticObserver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        observer = AudioDiagnosticObserver(
            audioManager = getSystemService(AudioManager::class.java),
            callbackExecutor = mainExecutor,
        )
        observer.start()

        setContent {
            PrivateAudioTheme {
                DiagnosticScreen(
                    snapshot = observer.snapshot,
                    experiment = observer.experiment,
                    events = observer.events,
                    onArm = observer::armEarpieceTest,
                    onDisarm = observer::disarmAndClear,
                    onSnapshot = { observer.snapshot("Manual snapshot") },
                    onCopyReport = {
                        observer.snapshot("Report snapshot")
                        getSystemService(ClipboardManager::class.java).setPrimaryClip(
                            ClipData.newPlainText("Private Audio diagnostic report", observer.report()),
                        )
                    },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::observer.isInitialized) observer.snapshot("App foregrounded")
    }

    override fun onDestroy() {
        if (::observer.isInitialized) observer.stop()
        super.onDestroy()
    }
}
