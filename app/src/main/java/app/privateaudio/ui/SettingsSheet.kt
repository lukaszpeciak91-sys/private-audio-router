package app.privateaudio.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.privateaudio.R
import app.privateaudio.localization.AppLanguageOption
import app.privateaudio.localization.AppLanguagePreferences

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

private enum class SettingsPage { ROOT, LANGUAGE, ADVANCED, PRIVACY_POLICY, ABOUT }

@Composable
fun SettingsSheet(
    versionName: String,
    proximityFeatureEnabled: Boolean,
    onProximityFeatureChange: (Boolean) -> Unit,
    fakePhonePreArmEnabled: Boolean,
    onFakePhonePreArmChange: (Boolean) -> Unit,
    onDiagnostics: () -> Unit,
    onDismiss: () -> Unit,
) {
    var page by rememberSaveable { mutableStateOf(SettingsPage.ROOT) }
    val context = LocalContext.current
    val selectedLanguageTag = AppLanguagePreferences.currentLanguageTag(context)
    val supportedLanguages = remember(context.resources.configuration) {
        AppLanguagePreferences.supportedLanguages(context)
    }

    BackHandler(enabled = page != SettingsPage.ROOT) { page = SettingsPage.ROOT }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        val sheetInteraction = remember { MutableInteractionSource() }
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (page == SettingsPage.LANGUAGE || page == SettingsPage.PRIVACY_POLICY) {
                        Modifier.windowInsetsPadding(
                            WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical),
                        )
                    } else {
                        Modifier
                    },
                )
                .background(SettingsScrim)
                .clickable(onClick = onDismiss)
                .testTag("settings_backdrop"),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(SettingsLayout.widthFraction)
                    .then(
                        if (page == SettingsPage.LANGUAGE || page == SettingsPage.PRIVACY_POLICY) {
                            Modifier.heightIn(
                                max = maxHeight - SettingsLayout.verticalOffset * 2,
                            )
                        } else {
                            Modifier
                        },
                    )
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
                        selectedLanguageTag = selectedLanguageTag,
                        supportedLanguages = supportedLanguages,
                        onLanguage = { page = SettingsPage.LANGUAGE },
                        onDiagnostics = onDiagnostics,
                        onAdvanced = { page = SettingsPage.ADVANCED },
                        onPrivacyPolicy = { page = SettingsPage.PRIVACY_POLICY },
                        onAbout = { page = SettingsPage.ABOUT },
                    )
                    SettingsPage.LANGUAGE -> LanguagePage(
                        selectedLanguageTag = selectedLanguageTag,
                        supportedLanguages = supportedLanguages,
                        onSelect = {
                            AppLanguagePreferences.select(context, it)
                            page = SettingsPage.ROOT
                        },
                        onBack = { page = SettingsPage.ROOT },
                    )
                    SettingsPage.ADVANCED -> AdvancedPage(
                        proximityFeatureEnabled = proximityFeatureEnabled,
                        onProximityFeatureChange = onProximityFeatureChange,
                        fakePhonePreArmEnabled = fakePhonePreArmEnabled,
                        onFakePhonePreArmChange = onFakePhonePreArmChange,
                        onBack = { page = SettingsPage.ROOT },
                    )
                    SettingsPage.PRIVACY_POLICY -> PrivacyPolicyPage()
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
private fun AdvancedPage(
    proximityFeatureEnabled: Boolean,
    onProximityFeatureChange: (Boolean) -> Unit,
    fakePhonePreArmEnabled: Boolean,
    onFakePhonePreArmChange: (Boolean) -> Unit,
    onBack: () -> Unit,
) {
    Box(Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier.size(44.dp).clickable(role = Role.Button, onClick = onBack)
                .testTag("settings_child_back"),
            contentAlignment = Alignment.CenterStart,
        ) { BackChevron() }
        SheetTitle(stringResource(R.string.settings_advanced))
    }
    Spacer(Modifier.height(14.dp))
    Row(
        modifier = Modifier.fillMaxWidth().height(SettingsLayout.rowHeight)
            .toggleable(
                value = proximityFeatureEnabled,
                role = Role.Switch,
                onValueChange = onProximityFeatureChange,
            ).testTag("settings_proximity_screen"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            stringResource(R.string.settings_proximity_screen),
            modifier = Modifier.weight(1f).padding(end = 12.dp),
            color = SettingsPrimary,
            fontSize = 15.sp,
            lineHeight = 21.sp,
        )
        Switch(
            checked = proximityFeatureEnabled,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = Color(0xFF22DA70),
                uncheckedThumbColor = SettingsSecondary,
                uncheckedTrackColor = SettingsDivider,
                uncheckedBorderColor = SettingsBorder,
            ),
        )
    }
    Spacer(Modifier.height(30.dp))
    Row(
        modifier = Modifier.fillMaxWidth().height(SettingsLayout.rowHeight)
            .toggleable(
                value = fakePhonePreArmEnabled,
                role = Role.Switch,
                onValueChange = onFakePhonePreArmChange,
            ).testTag("settings_fake_phone_pre_arm"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            stringResource(R.string.settings_fake_phone_pre_arm),
            modifier = Modifier.weight(1f).padding(end = 12.dp),
            color = SettingsPrimary,
            fontSize = 15.sp,
            lineHeight = 21.sp,
        )
        Switch(
            checked = fakePhonePreArmEnabled,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = Color(0xFF22DA70),
                uncheckedThumbColor = SettingsSecondary,
                uncheckedTrackColor = SettingsDivider,
                uncheckedBorderColor = SettingsBorder,
            ),
        )
    }
    Spacer(Modifier.height(30.dp))
}

@Composable
private fun SettingsRoot(
    versionName: String,
    selectedLanguageTag: String?,
    supportedLanguages: List<AppLanguageOption>,
    onLanguage: () -> Unit,
    onDiagnostics: () -> Unit,
    onAdvanced: () -> Unit,
    onPrivacyPolicy: () -> Unit,
    onAbout: () -> Unit,
) {
    val selectedLanguageName = supportedLanguages
        .firstOrNull { it.languageTag == selectedLanguageTag }
        ?.nativeName
        ?: selectedLanguageTag?.let(AppLanguagePreferences::nativeName)
        ?: stringResource(R.string.settings_system_default)
    SheetTitle(stringResource(R.string.settings))
    Spacer(Modifier.height(10.dp))
    SettingsRow(
        label = stringResource(R.string.settings_language),
        value = selectedLanguageName,
        chevron = true,
        tag = "settings_language",
        onClick = onLanguage,
    )
    SettingsRow(
        label = stringResource(R.string.settings_diagnostics),
        chevron = true,
        tag = "settings_diagnostics",
        onClick = onDiagnostics,
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
        label = stringResource(R.string.settings_privacy_policy),
        chevron = true,
        tag = "settings_privacy_policy",
        onClick = onPrivacyPolicy,
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
private fun LanguagePage(
    selectedLanguageTag: String?,
    supportedLanguages: List<AppLanguageOption>,
    onSelect: (String?) -> Unit,
    onBack: () -> Unit,
) {
    Column(Modifier.fillMaxHeight()) {
        Box(Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clickable(role = Role.Button, onClick = onBack)
                    .testTag("settings_child_back"),
                contentAlignment = Alignment.CenterStart,
            ) { BackChevron() }
            SheetTitle(stringResource(R.string.settings_language))
        }
        Spacer(Modifier.height(14.dp))
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .testTag("language_list"),
        ) {
            item {
                LanguageChoice(
                    label = stringResource(R.string.settings_system_default),
                    selected = selectedLanguageTag == null,
                    tag = "language_default",
                    onClick = { onSelect(null) },
                )
            }
            items(supportedLanguages, key = { it.languageTag }) { language ->
                SettingsDivider()
                LanguageChoice(
                    label = language.nativeName,
                    selected = language.languageTag == selectedLanguageTag,
                    tag = "language_${language.languageTag}",
                    onClick = { onSelect(language.languageTag) },
                )
            }
            if (!AppLanguagePreferences.isSupported) {
                item {
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = stringResource(R.string.settings_language_android_13_required),
                        color = SettingsSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun LanguageChoice(label: String, selected: Boolean, tag: String, onClick: () -> Unit) {
    SettingsRow(
        label = label,
        value = if (selected) "✓" else null,
        tag = tag,
        onClick = onClick,
    )
}

@Composable
private fun PrivacyPolicyPage() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("privacy_policy_panel"),
    ) {
        SheetTitle(stringResource(R.string.settings_privacy_policy))
        Spacer(Modifier.height(22.dp))
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("privacy_policy_body"),
        ) {
            item {
                Text(
                    text = stringResource(R.string.settings_privacy_policy_body),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    color = SettingsSecondary,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    }
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
private fun ForwardChevron() = Canvas(Modifier.size(16.dp)) {
    val stroke = 1.4.dp.toPx()
    val tailX = directionalX(.35f)
    val pointX = directionalX(.65f)
    drawLine(SettingsSecondary, Offset(tailX, size.height * .2f), Offset(pointX, size.height * .5f), strokeWidth = stroke, cap = StrokeCap.Round)
    drawLine(SettingsSecondary, Offset(pointX, size.height * .5f), Offset(tailX, size.height * .8f), strokeWidth = stroke, cap = StrokeCap.Round)
}

@Composable
private fun BackChevron() = Canvas(Modifier.size(18.dp)) {
    val tailX = directionalX(.65f)
    val pointX = directionalX(.32f)
    drawLine(SettingsPrimary, Offset(tailX, size.height * .18f), Offset(pointX, size.height * .5f), 1.7.dp.toPx(), StrokeCap.Round)
    drawLine(SettingsPrimary, Offset(pointX, size.height * .5f), Offset(tailX, size.height * .82f), 1.7.dp.toPx(), StrokeCap.Round)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.directionalX(fraction: Float): Float =
    size.width * if (layoutDirection == LayoutDirection.Ltr) fraction else 1f - fraction
