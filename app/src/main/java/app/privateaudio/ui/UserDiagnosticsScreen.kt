package app.privateaudio.ui

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.privateaudio.PrivateAudioState
import app.privateaudio.R
import app.privateaudio.diagnostic.DiagnosticsAvailability
import app.privateaudio.diagnostic.DiagnosticsError
import app.privateaudio.diagnostic.DiagnosticsPermission
import app.privateaudio.diagnostic.DiagnosticsRoute
import app.privateaudio.diagnostic.DiagnosticsRouting
import app.privateaudio.diagnostic.DiagnosticsRoutingResult
import app.privateaudio.diagnostic.DiagnosticsSummary

private val DiagnosticsPrimary = Color(0xFFF2F2F2)
private val DiagnosticsSecondary = Color(0xFFA6A6A8)
private val DiagnosticsDivider = Color(0xFF292A2C)
private val DiagnosticsAccent = Color(0xFF22DA70)

@Composable
internal fun UserDiagnosticsScreen(
    summary: DiagnosticsSummary?,
    onBack: () -> Unit,
    onSaveDiagnosticReport: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBack)
    Column(
        modifier = modifier.fillMaxSize().background(Color.Black).statusBarsPadding()
            .navigationBarsPadding().verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp).widthIn(max = 560.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(44.dp).clickable(role = Role.Button, onClick = onBack)
                    .testTag("diagnostics_back"),
                contentAlignment = Alignment.CenterStart,
            ) { DiagnosticsBackIcon() }
            Text(
                text = stringResource(R.string.diagnostics_title),
                modifier = Modifier.weight(1f).padding(end = 44.dp),
                color = DiagnosticsPrimary,
                fontSize = 21.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            )
        }

        DiagnosticsSection(R.string.diagnostics_system_check) {
            DiagnosticsRow(R.string.diagnostics_earpiece, earpieceAvailability(summary?.earpiece))
            DiagnosticsRow(R.string.diagnostics_proximity_sensor, proximityAvailability(summary?.proximitySensor))
            DiagnosticsRow(R.string.floating, permission(summary?.floatingControlPermission))
        }
        DiagnosticsSection(R.string.diagnostics_private_audio) {
            DiagnosticsRow(R.string.diagnostics_routing, routing(summary?.routing))
            DiagnosticsRow(R.string.diagnostics_status, status(summary?.status))
            DiagnosticsRow(R.string.diagnostics_audio_route, route(summary?.audioRoute))
        }
        if (summary != null && summary.lastRoutingResult != DiagnosticsRoutingResult.NONE) {
            DiagnosticsSection(R.string.diagnostics_last_routing) {
                DiagnosticsRow(R.string.diagnostics_result, result(summary.lastRoutingResult))
                if (summary.lastRoutingResult == DiagnosticsRoutingResult.FAILED) {
                    DiagnosticsRow(R.string.diagnostics_error, error(summary.lastError))
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(52.dp)
                .background(DiagnosticsAccent, androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                .clickable(role = Role.Button, onClick = onSaveDiagnosticReport)
                .testTag("diagnostics_save_report"),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                stringResource(R.string.settings_save_diagnostic),
                color = Color.Black,
                fontSize = 15.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium,
            )
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun DiagnosticsSection(@StringRes title: Int, content: @Composable () -> Unit) {
    Spacer(Modifier.height(22.dp))
    Text(
        stringResource(title),
        color = DiagnosticsSecondary,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
    )
    Spacer(Modifier.height(8.dp))
    Box(Modifier.fillMaxWidth().height(1.dp).background(DiagnosticsDivider))
    content()
}

@Composable
private fun DiagnosticsRow(@StringRes label: Int, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(stringResource(label), Modifier.weight(1f), color = DiagnosticsPrimary, fontSize = 15.sp, lineHeight = 20.sp)
        Text(value, Modifier.weight(1f), color = DiagnosticsSecondary, fontSize = 15.sp, lineHeight = 20.sp, textAlign = TextAlign.End)
    }
}

@Composable private fun earpieceAvailability(value: DiagnosticsAvailability?) = stringResource(when (value) {
    DiagnosticsAvailability.AVAILABLE -> R.string.diagnostics_earpiece_available
    DiagnosticsAvailability.NOT_AVAILABLE -> R.string.diagnostics_earpiece_not_available
    null -> R.string.diagnostics_checking
})
@Composable private fun proximityAvailability(value: DiagnosticsAvailability?) = stringResource(when (value) {
    DiagnosticsAvailability.AVAILABLE -> R.string.diagnostics_proximity_available
    DiagnosticsAvailability.NOT_AVAILABLE -> R.string.diagnostics_proximity_not_available
    null -> R.string.diagnostics_checking
})
@Composable private fun permission(value: DiagnosticsPermission?) = stringResource(when (value) {
    DiagnosticsPermission.GRANTED -> R.string.diagnostics_available
    DiagnosticsPermission.NOT_GRANTED -> R.string.diagnostics_permission_required
    null -> R.string.diagnostics_checking
})
@Composable private fun routing(value: DiagnosticsRouting?) = stringResource(when (value) {
    DiagnosticsRouting.ON -> R.string.diagnostics_on
    DiagnosticsRouting.OFF -> R.string.diagnostics_off
    null -> R.string.diagnostics_checking
})
@Composable private fun status(value: PrivateAudioState?) = stringResource(when (value) {
    PrivateAudioState.READY -> R.string.state_ready
    PrivateAudioState.WAITING -> R.string.state_waiting
    PrivateAudioState.ACTIVE -> R.string.state_active
    PrivateAudioState.ERROR -> R.string.state_error
    null -> R.string.diagnostics_checking
})
@Composable private fun route(value: DiagnosticsRoute?) = stringResource(when (value) {
    DiagnosticsRoute.EARPIECE -> R.string.diagnostics_route_earpiece
    DiagnosticsRoute.SPEAKER -> R.string.diagnostics_route_speaker
    DiagnosticsRoute.BLUETOOTH -> R.string.diagnostics_route_bluetooth
    DiagnosticsRoute.OTHER -> R.string.diagnostics_route_other
    DiagnosticsRoute.UNKNOWN -> R.string.diagnostics_audio_output_unknown
    null -> R.string.diagnostics_checking
})
@Composable private fun result(value: DiagnosticsRoutingResult?) = stringResource(when (value) {
    DiagnosticsRoutingResult.SUCCESS -> R.string.diagnostics_success
    DiagnosticsRoutingResult.FAILED -> R.string.diagnostics_failed
    DiagnosticsRoutingResult.NONE, null -> R.string.diagnostics_checking
})
@Composable private fun error(value: DiagnosticsError?) = stringResource(when (value) {
    DiagnosticsError.BLOCKED_BY_SYSTEM -> R.string.diagnostics_error_blocked_by_system
    DiagnosticsError.SESSION_ENDED -> R.string.diagnostics_error_session_ended
    DiagnosticsError.AUDIO_ROUTING_START_FAILED -> R.string.diagnostics_error_audio_start
    DiagnosticsError.COMMUNICATION_AUDIO_PREPARATION_FAILED -> R.string.diagnostics_error_audio_preparation
    DiagnosticsError.EARPIECE_REQUEST_REJECTED -> R.string.diagnostics_error_request_rejected
    DiagnosticsError.ROUTING_NOT_COMPLETED -> R.string.diagnostics_error_not_completed
    DiagnosticsError.NONE, null -> R.string.diagnostics_checking
})

@Composable
private fun DiagnosticsBackIcon() = Canvas(Modifier.size(18.dp)) {
    drawLine(DiagnosticsPrimary, Offset(size.width * .68f, size.height * .18f), Offset(size.width * .32f, size.height * .5f), 1.7.dp.toPx(), StrokeCap.Round)
    drawLine(DiagnosticsPrimary, Offset(size.width * .32f, size.height * .5f), Offset(size.width * .68f, size.height * .82f), 1.7.dp.toPx(), StrokeCap.Round)
}
