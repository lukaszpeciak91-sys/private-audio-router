package app.privateaudio

import app.privateaudio.overlay.MINI_ERROR_STATUS_COLOR
import app.privateaudio.overlay.MINI_ACTIVE_STATUS_COLOR
import app.privateaudio.overlay.MINI_BORDER_COLOR
import app.privateaudio.overlay.MINI_READY_STATUS_COLOR
import app.privateaudio.overlay.MINI_SURFACE_COLOR
import app.privateaudio.overlay.MINI_WAITING_STATUS_COLOR
import app.privateaudio.overlay.miniStatusColor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class StatusClarityContractTest {
    @Test fun approvedEnglishCopyAndLocalizedKeyCoverageRemainComplete() {
        val expected = mapOf(
            "status_main_ready" to "Off",
            "status_main_waiting" to "Waiting for voice",
            "status_main_active" to "Through earpiece",
            "status_main_error" to "Routing problem",
            "status_mini_ready" to "Off",
            "status_mini_waiting" to "Waiting",
            "status_mini_active" to "Earpiece",
            "status_mini_error" to "Problem",
            "power_turn_on" to "Turn on Puzru",
            "power_turn_off" to "Turn off Puzru",
            "routing_notification_waiting_title" to "Puzru is on",
            "routing_notification_waiting_text" to "Waiting for supported voice audio.",
            "routing_notification_active_title" to "Earpiece routing active",
            "routing_notification_active_text" to "Supported voice audio is being routed to the earpiece.",
            "routing_notification_error_title" to "Routing problem",
            "routing_notification_error_text" to "Open Puzru to turn it off and try again.",
        )
        assertTrue(strings(defaultStrings).entries.containsAll(expected.entries))
        localeStrings.forEach { file ->
            val localized = strings(file)
            expected.keys.forEach { key -> assertTrue("${file.parentFile.name}: $key", !localized[key].isNullOrBlank()) }
        }
    }

    @Test fun mainAndMiniUseSymbolsAndNeutralReadyWithoutChangingStateProjection() {
        assertTrue(screen.contains("StatusSymbol.OFF -> drawCircle"))
        assertTrue(screen.contains("ProductNeutral"))
        assertFalse(screen.substringAfter("PrivateAudioState.READY -> StateVisuals").substringBefore('\n').contains("ProductGreen"))
        StatusSymbol.entries.forEach { assertTrue(overlay.contains("StatusSymbol.${it.name}")) }
        assertTrue(overlay.contains("statePresentation(value).miniLabel"))
        assertTrue(overlay.contains("fullStateLabel(value)"))
    }

    @Test fun notificationRefreshUsesCurrentServiceOwnedStateWithoutPolling() {
        assertTrue(service.contains("onEvidenceChanged = ::syncStateOwnedBehavior"))
        assertTrue(service.method("private fun syncStateOwnedBehavior").contains("updateForegroundNotification()"))
        assertTrue(service.contains("foregroundNotificationPresentation(privateAudioState)"))
        assertTrue(service.contains("privateAudioState == PrivateAudioState.READY"))
        assertFalse(service.method("private fun updateForegroundNotification").contains("postDelayed"))
        assertTrue(service.method("private fun updateForegroundNotification").contains("privateAudioState == PrivateAudioState.READY) return"))
        assertTrue(service.method("private fun buildForegroundNotification").contains("foregroundNotificationPresentation(privateAudioState) ?: return null"))
        assertTrue(service.method("fun disarmAndStopStartedLifetime").contains("stopForeground(STOP_FOREGROUND_REMOVE)"))
        assertTrue(service.method("fun disarmAndStopStartedLifetime").contains("stopSelf()"))
    }

    @Test fun miniTokensAndStateMappingsAreExact() {
        assertEquals(0x0F0F10, MINI_SURFACE_COLOR)
        assertEquals(0x5B5B5E, MINI_BORDER_COLOR)
        assertEquals(0xB3B3B3, MINI_READY_STATUS_COLOR)
        assertEquals(0xEEAC36, MINI_WAITING_STATUS_COLOR)
        assertEquals(0x22DA70, MINI_ACTIVE_STATUS_COLOR)
        assertEquals(0xFF8C8C, MINI_ERROR_STATUS_COLOR)
        assertEquals(MINI_READY_STATUS_COLOR, miniStatusColor(PrivateAudioState.READY))
        assertEquals(MINI_WAITING_STATUS_COLOR, miniStatusColor(PrivateAudioState.WAITING))
        assertEquals(MINI_ACTIVE_STATUS_COLOR, miniStatusColor(PrivateAudioState.ACTIVE))
        assertEquals(MINI_ERROR_STATUS_COLOR, miniStatusColor(PrivateAudioState.ERROR))
        assertTrue(overlay.contains("paint.color = miniColor(MINI_SURFACE_COLOR)"))
        assertTrue(overlay.contains("paint.color = miniColor(MINI_BORDER_COLOR)"))
    }

    @Test fun miniReadyAndErrorMeetNonTextContrastAgainstActualSurface() {
        assertTrue(contrast(MINI_READY_STATUS_COLOR, MINI_SURFACE_COLOR) >= 3.0)
        assertTrue(contrast(MINI_ERROR_STATUS_COLOR, MINI_SURFACE_COLOR) >= 3.0)
    }

    private fun contrast(first: Int, second: Int): Double {
        val lighter = maxOf(relativeLuminance(first), relativeLuminance(second))
        val darker = minOf(relativeLuminance(first), relativeLuminance(second))
        return (lighter + 0.05) / (darker + 0.05)
    }

    private fun relativeLuminance(rgb: Int): Double {
        fun channel(shift: Int): Double {
            val value = (rgb shr shift and 0xFF) / 255.0
            return if (value <= 0.04045) value / 12.92 else Math.pow((value + 0.055) / 1.055, 2.4)
        }
        return 0.2126 * channel(16) + 0.7152 * channel(8) + 0.0722 * channel(0)
    }

    private fun strings(file: File): Map<String, String> {
        val nodes = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file).getElementsByTagName("string")
        return (0 until nodes.length).associate { i -> nodes.item(i).attributes.getNamedItem("name").nodeValue to nodes.item(i).textContent }
    }
    private fun String.method(signature: String) = kotlinDeclaration(signature)

    private companion object {
        val root = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
            .first { File(it, "app/src/main").isDirectory }
        val res = File(root, "app/src/main/res")
        val defaultStrings = File(res, "values/strings.xml")
        val localeStrings = res.listFiles()!!.filter(File::isCompleteTargetLocalizationDirectory)
            .map { File(it, "strings.xml") }.filter(File::isFile)
        val screen = File(root, "app/src/main/java/app/privateaudio/ui/PrivateAudioScreen.kt").readText()
        val overlay = File(root, "app/src/main/java/app/privateaudio/overlay/OverlayService.kt").readText()
        val service = File(root, "app/src/main/java/app/privateaudio/PrivateAudioService.kt").readText()
    }
}
