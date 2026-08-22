package app.privateaudio.ui

import androidx.annotation.StringRes
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.LayoutDirection
import app.privateaudio.PrivateAudioState
import app.privateaudio.R
import app.privateaudio.diagnostic.DiagnosticsSummary
import app.privateaudio.localization.AppLanguagePreferences
import app.privateaudio.ui.theme.PrivateAudioTheme
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private object ProductLayout {
    val horizontalPadding = 24.dp
    val titleTopSpacing = 48.dp
    val titleSubtitleSpacing = 8.dp
    val headerStatusSpacing = 52.dp
    val statusPowerSpacing = 48.dp
    val bottomHeight = 88.dp
    val bottomSpacing = 44.dp
    val maximumContentWidth = 440.dp
    const val powerWidthFraction = 0.88f
    val maximumPowerDiameter = 304.dp
}

private val ProductWhite = Color(0xFFF5F5F5)
private val ProductSecondary = Color(0xFFD2D2D2)
private val ProductGreen = Color(0xFF00F02A)
private val ProductAmber = Color(0xFFFFDE00)
private val ProductRed = Color(0xFFFF1D2D)
private val ReadyPower = Color(0xFF858585)
private val PowerBorder = Color(0xFFB3B3B3)
private const val PowerGlyphScale = 1.04f
private const val WaitingHalfCycleMillis = 900
private const val ActiveHalfCycleMillis = 700

private data class StateVisuals(
    @StringRes val label: Int,
    val dotColor: Color,
    val powerColor: Color,
    val glow: Boolean,
    val pulse: Boolean = false,
)

@Composable
internal fun PrivateAudioScreen(
    state: PrivateAudioState,
    proximityFeatureEnabled: Boolean = true,
    onProximityFeatureChange: (Boolean) -> Unit = {},
    assistantEarlyRouteEnabled: Boolean = false,
    onAssistantEarlyRouteChange: (Boolean) -> Unit = {},
    powerEnabled: Boolean = true,
    onPowerClick: () -> Unit,
    onFloatingClick: () -> Unit = {},
    onCloseClick: () -> Unit,
    diagnosticsSummary: DiagnosticsSummary? = null,
    onSaveDiagnosticReport: () -> Unit = {},
    versionName: String = "",
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val presentationDirection = if (
        AppLanguagePreferences.presentationLayoutDirection(context) == android.view.View.LAYOUT_DIRECTION_RTL
    ) LayoutDirection.Rtl else LayoutDirection.Ltr
    CompositionLocalProvider(LocalLayoutDirection provides presentationDirection) {
        PrivateAudioScreenContent(
            state = state,
            proximityFeatureEnabled = proximityFeatureEnabled,
            onProximityFeatureChange = onProximityFeatureChange,
            assistantEarlyRouteEnabled = assistantEarlyRouteEnabled,
            onAssistantEarlyRouteChange = onAssistantEarlyRouteChange,
            powerEnabled = powerEnabled,
            onPowerClick = onPowerClick,
            onFloatingClick = onFloatingClick,
            onCloseClick = onCloseClick,
            diagnosticsSummary = diagnosticsSummary,
            onSaveDiagnosticReport = onSaveDiagnosticReport,
            versionName = versionName,
            modifier = modifier,
        )
    }
}

@Composable
private fun PrivateAudioScreenContent(
    state: PrivateAudioState,
    proximityFeatureEnabled: Boolean,
    onProximityFeatureChange: (Boolean) -> Unit,
    assistantEarlyRouteEnabled: Boolean,
    onAssistantEarlyRouteChange: (Boolean) -> Unit,
    powerEnabled: Boolean,
    onPowerClick: () -> Unit,
    onFloatingClick: () -> Unit,
    onCloseClick: () -> Unit,
    diagnosticsSummary: DiagnosticsSummary?,
    onSaveDiagnosticReport: () -> Unit,
    versionName: String,
    modifier: Modifier,
) {
    val visuals = stateVisuals(state)
    val motionPhase = stateMotionPhase(state)
    var settingsVisible by rememberSaveable { mutableStateOf(false) }
    var diagnosticsVisible by rememberSaveable { mutableStateOf(false) }

    if (diagnosticsVisible) {
        UserDiagnosticsScreen(
            summary = diagnosticsSummary,
            onBack = { diagnosticsVisible = false },
            onSaveDiagnosticReport = onSaveDiagnosticReport,
            modifier = modifier,
        )
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = ProductLayout.maximumContentWidth)
                .padding(horizontal = ProductLayout.horizontalPadding),
        ) {
            val powerDiameter = min(
                maxWidth.value * ProductLayout.powerWidthFraction,
                ProductLayout.maximumPowerDiameter.value,
            ).dp

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(ProductLayout.titleTopSpacing))
                Text(
                    text = stringResource(R.string.product_title),
                    color = ProductWhite,
                    fontSize = 30.sp,
                    lineHeight = 36.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(ProductLayout.titleSubtitleSpacing))
                Text(
                    text = stringResource(R.string.product_subtitle),
                    color = ProductSecondary,
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(ProductLayout.headerStatusSpacing))
                StatusIndicator(
                    visuals = visuals,
                    dotAlpha = when (state) {
                        PrivateAudioState.WAITING, PrivateAudioState.ACTIVE ->
                            0.65f + 0.35f * motionPhase
                        PrivateAudioState.READY, PrivateAudioState.ERROR -> 1f
                    },
                )
                Spacer(Modifier.height(ProductLayout.statusPowerSpacing))
                PowerControl(
                    color = visuals.powerColor,
                    glow = visuals.glow,
                    glowAlpha = if (visuals.pulse) 0.55f + 0.35f * motionPhase else 0.78f,
                    enabled = powerEnabled,
                    diameter = powerDiameter,
                    onClick = onPowerClick,
                )
                Spacer(Modifier.weight(1f))
                BottomControls(
                    onFloatingClick = onFloatingClick,
                    onSettingsClick = { settingsVisible = true },
                    onCloseClick = onCloseClick,
                )
                Spacer(Modifier.height(ProductLayout.bottomSpacing))
            }
        }

        if (settingsVisible) {
            SettingsSheet(
                versionName = versionName,
                proximityFeatureEnabled = proximityFeatureEnabled,
                onProximityFeatureChange = onProximityFeatureChange,
                assistantEarlyRouteEnabled = assistantEarlyRouteEnabled,
                onAssistantEarlyRouteChange = onAssistantEarlyRouteChange,
                onDiagnostics = {
                    settingsVisible = false
                    diagnosticsVisible = true
                },
                onDismiss = { settingsVisible = false },
            )
        }
    }
}

@Composable
private fun StatusIndicator(visuals: StateVisuals, dotAlpha: Float) {
    Row(
        modifier = Modifier
            .height(32.dp)
            .wrapContentWidth()
            .testTag("private_audio_status"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(10.dp)
                .alpha(dotAlpha)
                .background(visuals.dotColor, CircleShape),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = stringResource(visuals.label),
            color = ProductWhite,
            fontSize = 20.sp,
            lineHeight = 26.sp,
            fontWeight = FontWeight.Normal,
        )
    }
}

@Composable
private fun PowerControl(
    color: Color,
    glow: Boolean,
    glowAlpha: Float,
    enabled: Boolean,
    diameter: Dp,
    onClick: () -> Unit,
) {
    val powerDescription = stringResource(R.string.power_control)

    Canvas(
        modifier = Modifier
            .size(diameter)
            .alpha(if (enabled) 1f else 0.72f)
            .semantics { contentDescription = powerDescription }
            .testTag("private_audio_power")
            .clickable(enabled = enabled, onClick = onClick),
    ) {
        val strokeScale = size.minDimension / 300f
        val center = this.center
        drawCircle(
            color = PowerBorder,
            radius = size.minDimension / 2f - 1.5f * strokeScale,
            style = Stroke(width = 1.5f * strokeScale),
        )
        scale(PowerGlyphScale, pivot = center) {
            if (glow) {
                drawArc(
                    color = color.copy(alpha = 0.06f * glowAlpha),
                    startAngle = -42f,
                    sweepAngle = 264f,
                    useCenter = false,
                    topLeft = Offset(size.width * 0.255f, size.height * 0.255f),
                    size = Size(size.width * 0.49f, size.height * 0.49f),
                    style = Stroke(width = 34f * strokeScale, cap = StrokeCap.Round),
                )
                drawArc(
                    color = color.copy(alpha = 0.13f * glowAlpha),
                    startAngle = -42f,
                    sweepAngle = 264f,
                    useCenter = false,
                    topLeft = Offset(size.width * 0.255f, size.height * 0.255f),
                    size = Size(size.width * 0.49f, size.height * 0.49f),
                    style = Stroke(width = 22f * strokeScale, cap = StrokeCap.Round),
                )
            }
            drawArc(
                color = color,
                startAngle = -42f,
                sweepAngle = 264f,
                useCenter = false,
                topLeft = Offset(size.width * 0.255f, size.height * 0.255f),
                size = Size(size.width * 0.49f, size.height * 0.49f),
                style = Stroke(width = 16f * strokeScale, cap = StrokeCap.Round),
            )
            drawLine(
                color = color,
                start = Offset(center.x, size.height * 0.27f),
                end = Offset(center.x, size.height * 0.48f),
                strokeWidth = 16f * strokeScale,
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
private fun stateMotionPhase(state: PrivateAudioState): Float {
    val halfCycleMillis = when (state) {
        PrivateAudioState.WAITING -> WaitingHalfCycleMillis
        PrivateAudioState.ACTIVE -> ActiveHalfCycleMillis
        PrivateAudioState.READY, PrivateAudioState.ERROR -> return 1f
    }
    return key(state) {
        val transition = rememberInfiniteTransition(label = "${state.name.lowercase()} status motion")
        val phase by transition.animateFloat(
            initialValue = 1f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = halfCycleMillis,
                    easing = FastOutSlowInEasing,
                ),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "${state.name.lowercase()} motion phase",
        )
        phase
    }
}

@Composable
private fun BottomControls(
    onFloatingClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onCloseClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(ProductLayout.bottomHeight),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        BottomControl(
            label = stringResource(R.string.floating),
            description = stringResource(R.string.floating),
            tag = "private_audio_floating",
            onClick = onFloatingClick,
            icon = { FloatingIcon() },
        )
        BottomControl(
            label = stringResource(R.string.settings),
            description = stringResource(R.string.settings),
            tag = "private_audio_settings",
            onClick = onSettingsClick,
            icon = { SettingsIcon() },
        )
        BottomControl(
            label = stringResource(R.string.close),
            description = stringResource(R.string.close),
            tag = "private_audio_close",
            onClick = onCloseClick,
            icon = { CloseIcon() },
        )
    }
}

@Composable
private fun BottomControl(
    label: String,
    description: String,
    tag: String,
    onClick: (() -> Unit)? = null,
    icon: @Composable () -> Unit,
) {
    val interactionModifier = if (onClick == null) Modifier else Modifier.clickable(onClick = onClick)
    var labelFontSize by remember(label) { mutableStateOf(bottomLabelInitialFontSize(label)) }
    val isMultiWordLabel = label.any(Char::isWhitespace)
    Column(
        modifier = Modifier
            .width(88.dp)
            .height(88.dp)
            .then(interactionModifier)
            .semantics { contentDescription = description }
            .testTag(tag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) { icon() }
        Spacer(Modifier.height(7.dp))
        Text(
            text = label,
            color = ProductWhite,
            fontSize = labelFontSize,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            softWrap = isMultiWordLabel,
            maxLines = if (isMultiWordLabel) 2 else 1,
            overflow = TextOverflow.Clip,
            onTextLayout = { result ->
                if (result.hasVisualOverflow && labelFontSize > 11.sp) {
                    labelFontSize = (labelFontSize.value - 0.5f).coerceAtLeast(11f).sp
                }
            },
            modifier = Modifier.requiredWidth(112.dp),
        )
    }
}

internal fun bottomLabelInitialFontSize(label: String): TextUnit =
    if (label.usesArabicScript()) 15.sp else 13.sp

internal fun String.usesArabicScript(): Boolean {
    var index = 0
    while (index < length) {
        val codePoint = Character.codePointAt(this, index)
        if (Character.UnicodeScript.of(codePoint) == Character.UnicodeScript.ARABIC) return true
        index += Character.charCount(codePoint)
    }
    return false
}

@Composable
private fun FloatingIcon() {
    Canvas(Modifier.size(30.dp)) {
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawRoundRect(
            color = ProductWhite,
            topLeft = Offset(size.width * 0.08f, size.height * 0.12f),
            size = Size(size.width * 0.76f, size.height * 0.66f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.5.dp.toPx()),
            style = stroke,
        )
        drawRoundRect(
            color = ProductWhite,
            topLeft = Offset(size.width * 0.48f, size.height * 0.57f),
            size = Size(size.width * 0.43f, size.height * 0.31f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.5.dp.toPx()),
            style = stroke,
        )
        drawLine(
            color = ProductWhite,
            start = Offset(size.width * 0.65f, size.height * 0.73f),
            end = Offset(size.width * 0.76f, size.height * 0.73f),
            strokeWidth = 1.8.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun SettingsIcon() {
    Canvas(Modifier.size(30.dp)) {
        val outer = size.minDimension * 0.46f
        val inner = size.minDimension * 0.18f
        val path = Path()
        for (index in 0 until 24) {
            val angle = Math.toRadians(index * 15.0 - 90.0)
            val radius = when (index % 3) {
                0 -> outer
                1 -> outer
                else -> outer * 0.78f
            }
            val point = Offset(
                center.x + cos(angle).toFloat() * radius,
                center.y + sin(angle).toFloat() * radius,
            )
            if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
        }
        path.close()
        drawPath(
            path = path,
            color = ProductWhite,
            style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
        drawCircle(
            color = ProductWhite,
            radius = inner,
            style = Stroke(width = 1.8.dp.toPx()),
        )
    }
}

@Composable
private fun CloseIcon() {
    Canvas(Modifier.size(28.dp)) {
        val inset = 3.dp.toPx()
        val width = 1.8.dp.toPx()
        drawLine(ProductWhite, Offset(inset, inset), Offset(size.width - inset, size.height - inset), width, StrokeCap.Round)
        drawLine(ProductWhite, Offset(size.width - inset, inset), Offset(inset, size.height - inset), width, StrokeCap.Round)
    }
}

private fun stateVisuals(state: PrivateAudioState): StateVisuals = when (state) {
    PrivateAudioState.READY -> StateVisuals(R.string.state_ready, ProductGreen, ReadyPower, glow = false)
    PrivateAudioState.WAITING -> StateVisuals(R.string.state_waiting, ProductAmber, ProductAmber, glow = true, pulse = true)
    PrivateAudioState.ACTIVE -> StateVisuals(R.string.state_active, ProductGreen, ProductGreen, glow = true)
    PrivateAudioState.ERROR -> StateVisuals(R.string.state_error, ProductRed, ProductRed, glow = true)
}

@Preview(name = "Ready", showBackground = true, backgroundColor = 0xFF000000, widthDp = 393, heightDp = 852)
@Composable
private fun ReadyPreview() = ProductPreview(PrivateAudioState.READY)

@Preview(name = "Waiting", showBackground = true, backgroundColor = 0xFF000000, widthDp = 393, heightDp = 852)
@Composable
private fun WaitingPreview() = ProductPreview(PrivateAudioState.WAITING)

@Preview(name = "Active", showBackground = true, backgroundColor = 0xFF000000, widthDp = 393, heightDp = 852)
@Composable
private fun ActivePreview() = ProductPreview(PrivateAudioState.ACTIVE)

@Preview(name = "Error", showBackground = true, backgroundColor = 0xFF000000, widthDp = 393, heightDp = 852)
@Composable
private fun ErrorPreview() = ProductPreview(PrivateAudioState.ERROR)

@Composable
private fun ProductPreview(state: PrivateAudioState) {
    PrivateAudioTheme {
        PrivateAudioScreen(state = state, onPowerClick = {}, onCloseClick = {})
    }
}
