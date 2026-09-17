package app.privateaudio

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
        assertTrue(overlay.contains("PrivateAudioState.READY -> Color.rgb(133, 133, 133)"))
        assertTrue(overlay.contains("statePresentation(value).miniLabel"))
        assertTrue(overlay.contains("fullStateLabel(value)"))
    }

    @Test fun notificationRefreshUsesCurrentServiceOwnedStateWithoutPolling() {
        assertTrue(service.contains("onEvidenceChanged = ::syncStateOwnedBehavior"))
        assertTrue(service.method("private fun syncStateOwnedBehavior").contains("updateForegroundNotification()"))
        assertTrue(service.contains("statePresentation(privateAudioState)"))
        assertTrue(service.contains("privateAudioState == PrivateAudioState.READY"))
        assertFalse(service.method("private fun updateForegroundNotification").contains("postDelayed"))
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
