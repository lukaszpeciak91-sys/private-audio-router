package app.privateaudio

import app.privateaudio.ui.StatusSymbol
import app.privateaudio.ui.statusVisualStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class StatusVisualStyleTest {
    @Test fun authoritativeStatesUseDistinctSemanticSymbolsAndReadyIsNotActiveGreen() {
        val styles = PrivateAudioState.entries.associateWith(::statusVisualStyle)

        assertEquals(StatusSymbol.OFF, styles.getValue(PrivateAudioState.READY).symbol)
        assertEquals(StatusSymbol.WAITING_CLOCK, styles.getValue(PrivateAudioState.WAITING).symbol)
        assertEquals(StatusSymbol.EARPIECE, styles.getValue(PrivateAudioState.ACTIVE).symbol)
        assertEquals(StatusSymbol.WARNING, styles.getValue(PrivateAudioState.ERROR).symbol)
        assertEquals(4, styles.values.map { it.symbol }.distinct().size)
        assertNotEquals(
            styles.getValue(PrivateAudioState.ACTIVE).colorArgb,
            styles.getValue(PrivateAudioState.READY).colorArgb,
        )
        assertEquals(0xFFB8B8BCL, styles.getValue(PrivateAudioState.READY).colorArgb)
        assertEquals(0xFFEEAC36L, styles.getValue(PrivateAudioState.WAITING).colorArgb)
        assertEquals(0xFF22DA70L, styles.getValue(PrivateAudioState.ACTIVE).colorArgb)
        assertEquals(0xFFEE4B4BL, styles.getValue(PrivateAudioState.ERROR).colorArgb)
    }

    @Test fun surfacesRetainExistingLocalizedStateContractsAndNotificationCopy() {
        listOf("state_ready", "state_waiting", "state_active", "state_error").forEach {
            assertTrue(mainScreen.contains("R.string.$it"))
        }
        listOf("state_ready_mini", "state_waiting_mini", "state_active_mini", "state_error_mini").forEach {
            assertTrue(overlay.contains("R.string.$it"))
        }
        assertTrue(aliasFile.isFile)
        assertTrue(aliasFile.readText().contains("@string/state_ready"))
        assertTrue(File(res, "values-ta/mini_state_strings.xml").isFile)
        assertTrue(File(res, "values-ml/mini_state_strings.xml").isFile)
        assertTrue(service.contains("R.string.routing_notification_title"))
        assertTrue(service.contains("R.string.routing_notification_text"))

        val defaultNames = resourceNames(File(res, "values/strings.xml"))
        assertTrue(defaultNames.none { it.startsWith("status_main_") || it.startsWith("status_mini_") })
        assertTrue(defaultNames.none { it == "power_turn_on" || it == "power_turn_off" })
        assertTrue(defaultNames.none { it.startsWith("routing_notification_waiting") || it.startsWith("routing_notification_active") || it.startsWith("routing_notification_error") })
    }

    private fun resourceNames(file: File): Set<String> {
        val nodes = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file).getElementsByTagName("string")
        return (0 until nodes.length).mapTo(mutableSetOf()) { nodes.item(it).attributes.getNamedItem("name").nodeValue }
    }

    private companion object {
        val root = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
            .first { File(it, "app/src/main").isDirectory }
        val res = File(root, "app/src/main/res")
        val mainScreen = File(root, "app/src/main/java/app/privateaudio/ui/PrivateAudioScreen.kt").readText()
        val overlay = File(root, "app/src/main/java/app/privateaudio/overlay/OverlayService.kt").readText()
        val service = File(root, "app/src/main/java/app/privateaudio/PrivateAudioService.kt").readText()
        val aliasFile = File(res, "values/mini_state_aliases.xml")
    }
}
