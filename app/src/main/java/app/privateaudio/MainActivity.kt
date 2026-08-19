package app.privateaudio

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.os.Handler
import android.os.Looper
import android.os.ResultReceiver
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
    private var pendingDiagnosticReport: String? = null
    private var lastDiagnosticSaveFailureReason: String? = null
    private val diagnosticDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode != RESULT_OK) {
            pendingDiagnosticReport = null
            return@registerForActivityResult
        }
        val destination = result.data?.data
        if (destination == null) {
            recordDiagnosticSaveFailure("Document Uri unavailable")
            pendingDiagnosticReport = null
            showDiagnosticSaveResult(saved = false)
            return@registerForActivityResult
        }
        saveDiagnosticReport(destination)
    }
    private val overlayShowReceiver = object : ResultReceiver(Handler(Looper.getMainLooper())) {
        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
            if (resultCode == OverlayService.SHOW_SUCCEEDED) moveTaskToBack(true)
        }
    }
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
                    proximityFeatureEnabled = connectedService?.isProximityFeatureEnabled ?: true,
                    onProximityFeatureChange = { connectedService?.updateProximityFeatureEnabled(it) },
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
                    onSaveDiagnosticReport = { launchDiagnosticDocumentPicker() },
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
        startService(OverlayService.showIntent(this, overlayShowReceiver))
    }

    private fun hideOverlay() {
        startService(OverlayService.hideIntent(this))
    }

    private fun launchDiagnosticDocumentPicker() {
        val connectedService = service
        if (connectedService == null) {
            recordDiagnosticSaveFailure("No connected service when Save was tapped")
            showDiagnosticSaveResult(saved = false)
            return
        }
        pendingDiagnosticReport = connectedService.diagnosticReport()
        diagnosticDocumentLauncher.launch(
            Intent(Intent.ACTION_CREATE_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TITLE, diagnosticFilename()),
        )
    }

    private fun saveDiagnosticReport(destination: Uri) {
        val report = pendingDiagnosticReport
        val result = if (report == null) {
            DiagnosticWriteResult.Failure("Captured diagnostic report unavailable")
        } else {
            writeDiagnosticReport(report) { contentResolver.openOutputStream(destination, "w") }
        }
        pendingDiagnosticReport = null
        if (result is DiagnosticWriteResult.Failure) recordDiagnosticSaveFailure(result.reason)
        showDiagnosticSaveResult(saved = result == DiagnosticWriteResult.Success)
    }

    private fun recordDiagnosticSaveFailure(reason: String) {
        lastDiagnosticSaveFailureReason = reason
        Log.e(TAG, "Diagnostic report save failed — $reason")
        service?.recordDiagnosticSaveEvent("Diagnostic report save failed — $reason")
    }

    private fun showDiagnosticSaveResult(saved: Boolean) {
        Toast.makeText(
            this,
            getString(if (saved) R.string.diagnostic_report_saved else R.string.diagnostic_report_save_failed),
            Toast.LENGTH_SHORT,
        ).show()
    }

    private companion object {
        const val TAG = "PrivateAudio"
    }
}

internal sealed interface DiagnosticWriteResult {
    data object Success : DiagnosticWriteResult
    data class Failure(val reason: String) : DiagnosticWriteResult
}

internal fun writeDiagnosticReport(
    report: String,
    openOutputStream: () -> java.io.OutputStream?,
): DiagnosticWriteResult = try {
    val output = openOutputStream()
        ?: return DiagnosticWriteResult.Failure("openOutputStream returned null")
    output.use {
        it.writer(Charsets.UTF_8).use { writer -> writer.write(report) }
    }
    DiagnosticWriteResult.Success
} catch (exception: Exception) {
    val detail = exception.message?.takeIf { it.isNotBlank() }?.let { ": $it" }.orEmpty()
    DiagnosticWriteResult.Failure("${exception.javaClass.simpleName}$detail")
}

internal fun diagnosticFilename(now: java.time.LocalDateTime = java.time.LocalDateTime.now()): String =
    "private-audio-diagnostic-${now.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))}.txt"
