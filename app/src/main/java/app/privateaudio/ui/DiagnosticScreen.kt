package app.privateaudio.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.privateaudio.diagnostic.DiagnosticSnapshot
import app.privateaudio.diagnostic.EarpieceExperiment
import app.privateaudio.diagnostic.ObservedDevice
import app.privateaudio.ui.theme.PrivateAudioTheme
import kotlinx.coroutines.launch

@Composable
fun DiagnosticScreen(
    snapshot: DiagnosticSnapshot,
    experiment: EarpieceExperiment,
    events: List<String>,
    onArm: () -> Unit,
    onDisarm: () -> Unit,
    onSnapshot: () -> Unit,
    onCopyReport: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("PRIVATE AUDIO", style = MaterialTheme.typography.labelLarge)
            Text("POC-5 Active Requester", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Experimental: one explicit arm starts a silent communication AudioTrack, requests communication mode, then makes exactly one earpiece request. No microphone, capture, proxy, or audio-focus request is used. Audible success requires your confirmation.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Section("EARPIECE EXPERIMENT") {
                StateRow("Experiment state", experiment.state.label)
                StateRow("Armed", experiment.armed.toString())
                StateRow("Request attempted", experiment.requestAttempted.toString())
                StateRow("Mode before", experiment.modeBeforeParticipation ?: "Not attempted")
                StateRow("Silent track created", experiment.silentTrackCreated.toString())
                StateRow("Silent track started", experiment.silentTrackStarted.toString())
                StateRow("Silent track state", experiment.silentTrackPlayState)
                StateRow("Active voice playback visible", experiment.activeVoiceCommunicationPlaybackObserved.toString())
                StateRow("Mode requested after playback", experiment.modeRequestIssuedAfterPlaybackActive.toString())
                StateRow("Communication mode observed", experiment.modeInCommunicationObserved.toString())
                StateRow("Request accepted", experiment.requestAccepted?.toString() ?: "Not attempted")
                StateRow("Total attempts", experiment.attempts.size.toString())
                StateRow("Earpiece reported in session", experiment.earpieceReportedDuringSession.toString())
                StateRow("Speaker reclaimed", experiment.revertedToSpeaker.toString())
                StateRow("Silent track cleanup", experiment.silentTrackCleanupCompleted.toString())
                StateRow(
                    "Device after observation",
                    experiment.shortObservation?.communicationDevice?.description() ?: "Not observed",
                )
                StateRow(
                    "Target",
                    experiment.selectedTarget?.description() ?: "None selected",
                )
            }

            Button(onClick = onArm, modifier = Modifier.fillMaxWidth()) {
                Text("ARM EARPIECE TEST")
            }

            Button(onClick = onDisarm, modifier = Modifier.fillMaxWidth()) {
                Text("DISARM / CLEAR")
            }

            Section("CURRENT STATE") {
                StateRow("AudioManager mode", snapshot.mode)
                StateRow(
                    "Communication device",
                    snapshot.communicationDevice?.description() ?: "None reported by Android",
                )
                StateRow("Speakerphone", snapshot.speakerphoneState)
            }

            Section("AVAILABLE COMMUNICATION DEVICES") {
                if (snapshot.availableCommunicationDevices.isEmpty()) {
                    Text("None reported by Android")
                } else {
                    snapshot.availableCommunicationDevices.forEachIndexed { index, device ->
                        if (index > 0) HorizontalDivider()
                        Text(device.description(), modifier = Modifier.padding(vertical = 6.dp))
                    }
                }
            }

            Button(onClick = onSnapshot, modifier = Modifier.fillMaxWidth()) {
                Text("REFRESH / RECORD SNAPSHOT")
            }

            Button(
                onClick = {
                    onCopyReport()
                    coroutineScope.launch { snackbarHostState.showSnackbar("Report copied") }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("COPY REPORT")
            }

            Section("OBSERVATION / EVENT LOG") {
                Text(
                    "Callbacks are owned by the local service. An armed experiment continues when this screen is closed; Disarm / Clear relinquishes routing and removes its foreground notification.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (events.isEmpty()) Text("No observations yet")
                events.forEach { Text(it, style = MaterialTheme.typography.bodySmall) }
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                content()
            }
        }
    }
}

@Composable
private fun StateRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, modifier = Modifier.weight(0.42f), style = MaterialTheme.typography.bodyMedium)
        Text(value, modifier = Modifier.weight(0.58f), style = MaterialTheme.typography.bodyMedium)
    }
}

private fun ObservedDevice.description() = "$type\n$productName · Android device ID $id"

@Preview(showBackground = true)
@Composable
private fun DiagnosticScreenPreview() {
    PrivateAudioTheme {
        DiagnosticScreen(
            snapshot = DiagnosticSnapshot(
                mode = "MODE_NORMAL",
                communicationDevice = ObservedDevice(2, "Built-in speaker", "Phone speaker"),
                availableCommunicationDevices = listOf(
                    ObservedDevice(1, "Built-in earpiece", "Phone earpiece"),
                    ObservedDevice(2, "Built-in speaker", "Phone speaker"),
                ),
                speakerphoneState = "Off (directly observed)",
            ),
            experiment = EarpieceExperiment(),
            events = listOf("12:00:00.000  Baseline — state recorded"),
            onArm = {},
            onDisarm = {},
            onSnapshot = {},
            onCopyReport = {},
        )
    }
}
