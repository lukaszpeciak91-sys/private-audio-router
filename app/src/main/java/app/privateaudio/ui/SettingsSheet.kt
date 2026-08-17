package app.privateaudio.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.privateaudio.R

private object SettingsLayout {
    const val widthFraction = 0.88f
    val verticalOffset = 104.dp
    val cornerRadius = 16.dp
    val horizontalPadding = 20.dp
    val verticalPadding = 18.dp
    val rowHeight = 54.dp
}

private val SettingsScrim = Color.Black.copy(alpha = 0.34f)
private val SettingsSurface = Color(0xFF101112)
private val SettingsBorder = Color(0xFF5B5C5E)
private val SettingsDivider = Color(0xFF292A2C)
private val SettingsPrimary = Color(0xFFF2F2F2)
private val SettingsSecondary = Color(0xFFA6A6A8)

private enum class SettingsPage { ROOT, LANGUAGE, ADVANCED, ABOUT }

@Composable
fun SettingsSheet(
    versionName: String,
    onCopyDiagnosticReport: () -> Unit,
    onDismiss: () -> Unit,
) {
    var page by rememberSaveable { mutableStateOf(SettingsPage.ROOT) }

    BackHandler(enabled = page != SettingsPage.ROOT) { page = SettingsPage.ROOT }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        val sheetInteraction = remember { MutableInteractionSource() }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SettingsScrim)
                .clickable(onClick = onDismiss)
                .testTag("settings_backdrop"),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(SettingsLayout.widthFraction)
                    .offset(y = SettingsLayout.verticalOffset)
                    .background(SettingsSurface, RoundedCornerShape(SettingsLayout.cornerRadius))
                    .border(1.dp, SettingsBorder, RoundedCornerShape(SettingsLayout.cornerRadius))
                    .clickable(
                        interactionSource = sheetInteraction,
                        indication = null,
                        onClick = {},
                    )
                    .padding(
                        horizontal = SettingsLayout.horizontalPadding,
                        vertical = SettingsLayout.verticalPadding,
                    )
                    .testTag("settings_sheet"),
            ) {
                when (page) {
                    SettingsPage.ROOT -> SettingsRoot(
                        versionName = versionName,
                        onLanguage = { page = SettingsPage.LANGUAGE },
                        onCopyDiagnosticReport = onCopyDiagnosticReport,
                        onAdvanced = { page = SettingsPage.ADVANCED },
                        onAbout = { page = SettingsPage.ABOUT },
                    )
                    SettingsPage.LANGUAGE -> ChildPage(
                        title = stringResource(R.string.settings_language),
                        body = stringResource(R.string.settings_language_body),
                        onBack = { page = SettingsPage.ROOT },
                    )
                    SettingsPage.ADVANCED -> ChildPage(
                        title = stringResource(R.string.settings_advanced),
                        body = stringResource(R.string.settings_advanced_body),
                        onBack = { page = SettingsPage.ROOT },
                    )
                    SettingsPage.ABOUT -> ChildPage(
                        title = stringResource(R.string.settings_about),
                        body = stringResource(R.string.settings_about_body),
                        onBack = { page = SettingsPage.ROOT },
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsRoot(
    versionName: String,
    onLanguage: () -> Unit,
    onCopyDiagnosticReport: () -> Unit,
    onAdvanced: () -> Unit,
    onAbout: () -> Unit,
) {
    SheetTitle(stringResource(R.string.settings))
    Spacer(Modifier.height(10.dp))
    SettingsRow(
        label = stringResource(R.string.settings_language),
        value = stringResource(R.string.settings_system_default),
        chevron = true,
        tag = "settings_language",
        onClick = onLanguage,
    )
    SettingsRow(
        label = stringResource(R.string.settings_copy_diagnostic),
        tag = "settings_copy_diagnostic",
        trailing = { CopyIcon() },
        onClick = onCopyDiagnosticReport,
    )
    SettingsDivider()
    SettingsRow(
        label = stringResource(R.string.settings_advanced),
        chevron = true,
        tag = "settings_advanced",
        onClick = onAdvanced,
    )
    SettingsDivider()
    SettingsRow(
        label = stringResource(R.string.settings_about),
        chevron = true,
        tag = "settings_about",
        onClick = onAbout,
    )
    Spacer(Modifier.height(14.dp))
    Text(
        text = stringResource(R.string.settings_version, versionName),
        modifier = Modifier.fillMaxWidth().testTag("settings_version"),
        color = SettingsSecondary,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun ChildPage(title: String, body: String, onBack: () -> Unit) {
    Box(Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clickable(role = Role.Button, onClick = onBack)
                .testTag("settings_child_back"),
            contentAlignment = Alignment.CenterStart,
        ) { BackChevron() }
        SheetTitle(title)
    }
    Spacer(Modifier.height(26.dp))
    Text(
        text = body,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        color = SettingsSecondary,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(30.dp))
}

@Composable
private fun SheetTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier.fillMaxWidth(),
        color = SettingsPrimary,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun SettingsRow(
    label: String,
    tag: String,
    value: String? = null,
    chevron: Boolean = false,
    trailing: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(SettingsLayout.rowHeight)
            .clickable(role = Role.Button, onClick = onClick)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = SettingsPrimary, fontSize = 15.sp, lineHeight = 21.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            value?.let {
                Text(it, color = SettingsSecondary, fontSize = 14.sp, lineHeight = 20.sp)
                Spacer(Modifier.size(8.dp))
            }
            if (chevron) ForwardChevron()
            trailing?.invoke()
        }
    }
}

@Composable
private fun SettingsDivider() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(SettingsDivider),
    )
}

@Composable
private fun CopyIcon() = Canvas(Modifier.size(22.dp)) {
    val stroke = 1.35.dp.toPx()
    drawRoundRect(
        color = SettingsPrimary,
        topLeft = Offset(size.width * 0.10f, size.height * 0.25f),
        size = androidx.compose.ui.geometry.Size(size.width * 0.64f, size.height * 0.64f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx()),
        style = androidx.compose.ui.graphics.drawscope.Stroke(stroke),
    )
    drawLine(SettingsPrimary, Offset(size.width * 0.31f, size.height * 0.10f), Offset(size.width * 0.80f, size.height * 0.10f), stroke, StrokeCap.Round)
    drawLine(SettingsPrimary, Offset(size.width * 0.80f, size.height * 0.10f), Offset(size.width * 0.90f, size.height * 0.20f), stroke, StrokeCap.Round)
    drawLine(SettingsPrimary, Offset(size.width * 0.90f, size.height * 0.20f), Offset(size.width * 0.90f, size.height * 0.69f), stroke, StrokeCap.Round)
}

@Composable
private fun ForwardChevron() = Canvas(Modifier.size(16.dp)) {
    val stroke = 1.4.dp.toPx()
    drawLine(SettingsSecondary, Offset(size.width * .35f, size.height * .2f), Offset(size.width * .65f, size.height * .5f), strokeWidth = stroke, cap = StrokeCap.Round)
    drawLine(SettingsSecondary, Offset(size.width * .65f, size.height * .5f), Offset(size.width * .35f, size.height * .8f), strokeWidth = stroke, cap = StrokeCap.Round)
}

@Composable
private fun BackChevron() = Canvas(Modifier.size(18.dp)) {
    drawLine(SettingsPrimary, Offset(size.width * .65f, size.height * .18f), Offset(size.width * .32f, size.height * .5f), 1.7.dp.toPx(), StrokeCap.Round)
    drawLine(SettingsPrimary, Offset(size.width * .32f, size.height * .5f), Offset(size.width * .65f, size.height * .82f), 1.7.dp.toPx(), StrokeCap.Round)
}
