package app.privateaudio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class Layer3UiContractTest {
    @Test
    fun mainConsumesOnlyServiceOwnedProductStateAndExistingPowerOperations() {
        assertTrue(mainSource.contains("connectedService?.privateAudioState"))
        assertTrue(mainSource.contains("state == PrivateAudioState.READY"))
        assertTrue(mainSource.contains("setAction(PrivateAudioService.ACTION_ARM)"))
        assertTrue(mainSource.contains("connectedService?.disarmAndStopStartedLifetime()"))
        assertFalse(mainSource.contains("connectedService.observer"))
        assertFalse(mainSource.contains("MODE_IN_COMMUNICATION"))
        assertFalse(mainSource.contains("communicationDevice"))
        assertFalse(screenSource.contains("AudioDiagnosticObserver"))
        assertFalse(screenSource.contains("ExperimentState"))
    }

    @Test
    fun closeCleansUpBeforeRemovingTaskAndFutureControlsAddNoBehavior() {
        assertInOrder(
            mainSource,
            "onCloseClick = {",
            "connectedService?.disarmAndStopStartedLifetime()",
            "finishAndRemoveTask()",
        )
        assertFalse(screenSource.contains("SYSTEM_ALERT_WINDOW"))
        assertFalse(screenSource.contains("ModalBottomSheet("))
    }

    @Test
    fun protectedRoutingAndDiagnosticsRemainSingleAndPresent() {
        assertEquals(1, productionSources.sumOf { it.readText().occurrences("setCommunicationDevice(") })
        assertTrue(diagnosticScreen.isFile)
        assertTrue(observerSource.contains("internal fun buildDiagnosticReport("))
        assertTrue(serviceSource.contains("fun diagnosticReport(): String"))
        assertTrue(serviceSource.contains("observer.report(supportSummary)"))
    }

    @Test
    fun bottomLabelsUseSharedMeasuredFallbackWithoutChangingTranslations() {
        assertTrue(screenSource.contains("mutableStateOf(bottomLabelInitialFontSize(label))"))
        assertTrue(screenSource.contains("if (label.usesArabicScript()) 15.sp else 13.sp"))
        assertTrue(screenSource.contains("Character.UnicodeScript.ARABIC"))
        assertFalse(screenSource.contains("Locale("))
        assertFalse(screenSource.contains("language == \"ar\""))
        assertFalse(screenSource.contains("language == \"fa\""))
        assertFalse(screenSource.contains("language == \"ur\""))
        assertTrue(screenSource.contains("label.any(Char::isWhitespace)"))
        assertTrue(screenSource.contains("maxLines = if (isMultiWordLabel) 2 else 1"))
        assertTrue(screenSource.contains("result.hasVisualOverflow && labelFontSize > 11.sp"))
        assertTrue(screenSource.contains("labelFontSize.value - 0.5f"))
        assertTrue(screenSource.contains(".height(88.dp)"))
        assertTrue(screenSource.contains("lineHeight = 16.sp"))
        assertTrue(screenSource.contains("modifier = Modifier.requiredWidth(112.dp)"))
        assertFalse(screenSource.contains("TextOverflow.Ellipsis"))
        assertTrue(projectFile("app/src/main/res/values-pt-rBR/strings.xml").readText()
            .contains("name=\"settings\">Configurações</string>"))
        assertTrue(projectFile("app/src/main/res/values-uk/strings.xml").readText()
            .contains("name=\"settings\">Налаштування</string>"))
    }

    @Test
    fun mainPowerGlyphScalesAroundItsCenterWithoutScalingTheOuterControl() {
        assertTrue(screenSource.contains("private const val PowerGlyphScale = 1.04f"))
        val powerControl = screenSource.substringAfter("private fun PowerControl(")
            .substringBefore("private fun BottomControls(")
        val outerBorder = powerControl.indexOf("drawCircle(")
        val glyphScale = powerControl.indexOf("scale(PowerGlyphScale, pivot = center)")

        assertTrue(outerBorder >= 0)
        assertTrue(glyphScale > outerBorder)
        assertTrue(powerControl.substring(glyphScale).contains("if (glow)"))
        assertTrue(powerControl.substring(glyphScale).contains("drawArc("))
        assertTrue(powerControl.substring(glyphScale).contains("drawLine("))
        assertTrue(powerControl.contains(".size(diameter)"))
    }

    @Test
    fun stateMotionKeepsReadyAndErrorStaticAndSeparatesWaitingFromActive() {
        val motion = screenSource.substringAfter("private fun stateMotionPhase(")
            .substringBefore("private fun BottomControls(")

        assertTrue(motion.contains("PrivateAudioState.WAITING -> WaitingHalfCycleMillis"))
        assertTrue(motion.contains("PrivateAudioState.ACTIVE -> ActiveHalfCycleMillis"))
        assertTrue(motion.contains("PrivateAudioState.READY, PrivateAudioState.ERROR -> return 1f"))
        assertTrue(screenSource.contains("private const val WaitingHalfCycleMillis = 900"))
        assertTrue(screenSource.contains("private const val ActiveHalfCycleMillis = 700"))
        assertTrue(screenSource.contains("0.65f + 0.35f * motionPhase"))
        assertTrue(screenSource.contains("0.55f + 0.35f * motionPhase"))
        assertTrue(screenSource.contains("glowAlpha = if (visuals.pulse)"))
        assertFalse(screenSource.substringAfter("private fun PowerControl(")
            .substringBefore("private fun stateMotionPhase(")
            .contains("rememberInfiniteTransition"))
    }

    private fun assertInOrder(source: String, vararg fragments: String) {
        var previous = -1
        fragments.forEach { fragment ->
            val current = source.indexOf(fragment, previous + 1)
            assertTrue("Missing or out-of-order fragment: $fragment", current > previous)
            previous = current
        }
    }

    private fun String.occurrences(needle: String): Int = windowed(needle.length).count { it == needle }

    private companion object {
        val projectRoot = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
            .first { File(it, "app/src/main").isDirectory }
        val mainSource = projectFile("app/src/main/java/app/privateaudio/MainActivity.kt").readText()
        val screenSource = projectFile("app/src/main/java/app/privateaudio/ui/PrivateAudioScreen.kt").readText()
        val serviceSource = projectFile("app/src/main/java/app/privateaudio/PrivateAudioService.kt").readText()
        val observerSource = projectFile(
            "app/src/main/java/app/privateaudio/diagnostic/AudioDiagnosticObserver.kt",
        ).readText()
        val diagnosticScreen = projectFile("app/src/main/java/app/privateaudio/ui/DiagnosticScreen.kt")
        val productionSources = File(projectRoot, "app/src/main/java")
            .walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .toList()

        fun projectFile(relativePath: String) = File(projectRoot, relativePath)
    }
}
