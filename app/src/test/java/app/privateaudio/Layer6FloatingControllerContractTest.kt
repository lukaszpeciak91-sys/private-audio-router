package app.privateaudio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class Layer6FloatingControllerContractTest {
    @Test fun finalSurfaceHasApprovedDimensionsOrderAndNoHideControl() {
        assertTrue(overlay.contains("(300 * density).toInt()")); assertTrue(overlay.contains("(62 * density).toInt()"))
        assertTrue(overlay.indexOf("canvas.drawCircle(directionalX(STATUS_DOT_X)") < overlay.indexOf("drawPower(canvas"))
        assertTrue(overlay.indexOf("drawExpand(canvas)") < overlay.indexOf("drawClose(canvas)"))
        assertTrue(overlay.contains("RectF(134f, 15f, 166f, 47f)"))
        assertFalse(overlay.contains("canvas.drawRect(170f"))
        assertTrue(overlay.contains("POWER_START_FRACTION = 0.40f")); assertTrue(overlay.contains("POWER_END_FRACTION = 0.60f"))
        assertTrue(overlay.contains("EXPAND_START_FRACTION = 0.60f")); assertTrue(overlay.contains("CLOSE_START_FRACTION = 0.80f"))
        assertTrue(overlay.contains("directionalRect(196f, 26f, 213f, 43f)"))
        assertTrue(overlay.contains("directionalX(263f)")); assertTrue(overlay.contains("directionalX(283f)"))
        assertFalse(overlay.contains("HIDE_START_FRACTION"))
    }

    @Test fun allFourAuthoritativeStatesHaveApprovedTreatment() {
        PrivateAudioState.entries.forEach { assertTrue(it.name, overlay.contains("PrivateAudioState.${it.name}")) }
        assertTrue(overlay.contains("privateAudioService?.privateAudioState"))
        assertTrue(overlay.contains("READY, PrivateAudioState.ACTIVE -> Color.rgb(34, 218, 112)"))
        assertTrue(overlay.contains("WAITING -> Color.rgb(238, 172, 54)")); assertTrue(overlay.contains("ERROR -> Color.rgb(238, 75, 75)"))
        assertTrue(overlay.contains("READY -> Color.rgb(184, 184, 188)")); assertFalse(overlay.contains("projectPrivateAudioState("))
    }

    @Test fun controlsDelegateWithRequiredBoundaries() {
        val power = overlay.substringAfter("private fun togglePower() {").substringBefore("private fun expandMain()")
        assertTrue(power.contains("controller.privateAudioState == PrivateAudioState.READY")); assertTrue(power.contains("PrivateAudioService.ACTION_ARM"))
        assertTrue(power.contains("controller.disarmAndStopStartedLifetime()"))
        val expand = overlay.substringAfter("private fun expandMain() {").substringBefore("private fun unbindControllerService()")
        assertTrue(expand.contains("Intent.FLAG_ACTIVITY_NEW_TASK")); assertTrue(expand.contains("Intent.FLAG_ACTIVITY_CLEAR_TOP")); assertTrue(expand.contains("Intent.FLAG_ACTIVITY_SINGLE_TOP")); assertTrue(expand.contains("closeOverlay()")); assertFalse(expand.contains("disarm"))
        val close = overlay.substringAfter("private fun closeOverlay() {").substringBefore("private fun togglePower()")
        assertTrue(close.contains("hideOverlay()")); assertTrue(close.contains("stopSelf()")); assertFalse(close.contains("disarm")); assertFalse(close.contains("finish"))
    }

    @Test fun statusDotAloneAnimatesForWaitingAndActiveAndStopsWithObservation() {
        val animation = overlay.substringAfter("private fun updateStatusDotAnimation()")
            .substringBefore("private fun stopStatusDotAnimation()")
        assertTrue(animation.contains("PrivateAudioState.WAITING -> WAITING_HALF_CYCLE_MILLIS"))
        assertTrue(animation.contains("PrivateAudioState.ACTIVE -> ACTIVE_HALF_CYCLE_MILLIS"))
        assertTrue(animation.contains("PrivateAudioState.READY, PrivateAudioState.ERROR -> return"))
        assertTrue(animation.contains("ValueAnimator.ofFloat(1f, 0.65f)"))
        assertTrue(overlay.contains("WAITING_HALF_CYCLE_MILLIS = 900L"))
        assertTrue(overlay.contains("ACTIVE_HALF_CYCLE_MILLIS = 700L"))
        assertTrue(overlay.contains("paint.alpha = (statusDotAlpha * 255).toInt()"))
        assertTrue(overlay.contains("paint.alpha = 255"))
        assertTrue(overlay.contains("state = latest\n                    contentDescription = stateDescription(latest)\n                    updateStatusDotAnimation()"))
        val stopObservation = overlay.substringAfter("fun stopStateObservation()")
            .substringBefore("fun refreshLocalizedPresentation()")
        assertTrue(stopObservation.contains("removeCallbacks(refreshState)"))
        assertTrue(stopObservation.contains("stopStatusDotAnimation()"))
        assertFalse(animation.contains("drawPower"))
        assertFalse(animation.contains("drawExpand"))
        assertFalse(animation.contains("drawClose"))
    }

    @Test fun isolationSingleInstanceShutdownAndResourcesRemainLocked() {
        assertTrue(overlay.contains("if (overlayView != null)"))
        assertTrue(overlay.contains("if (!Settings.canDrawOverlays(this)) return"))
        assertTrue(main.contains("hideOverlay()")); assertTrue(main.contains("disarmAndStopStartedLifetime()")); assertTrue(main.contains("finishAndRemoveTask()"))
        listOf("AudioManager", "setCommunicationDevice(", "MODE_IN_COMMUNICATION", "projectPrivateAudioState(").forEach { assertFalse(it, overlay.contains(it)) }
        assertEquals(1, productionSources.sumOf { it.readText().occurrences("setCommunicationDevice(") })
        listOf("state_ready", "state_waiting", "state_active", "state_error", "overlay_controller_description").forEach { assertTrue(it, strings.contains("name=\"$it\"")) }
        listOf("\"Ready\"", "\"Waiting\"", "\"Active\"", "\"Error\"").forEach { assertFalse(overlay.contains(it)) }
    }

    private fun String.occurrences(needle: String) = windowed(needle.length).count { it == needle }
    private companion object {
        val root = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }.first { File(it, "app/src/main").isDirectory }
        val overlay = File(root, "app/src/main/java/app/privateaudio/overlay/OverlayService.kt").readText()
        val main = File(root, "app/src/main/java/app/privateaudio/MainActivity.kt").readText()
        val strings = File(root, "app/src/main/res/values/strings.xml").readText()
        val productionSources = File(root, "app/src/main/java").walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()
    }
}
