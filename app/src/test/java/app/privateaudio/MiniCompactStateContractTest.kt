package app.privateaudio

import app.privateaudio.overlay.MINI_STATUS_NON_ELLIPSIS_WIDTH
import app.privateaudio.overlay.selectMiniStatusTextSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class MiniCompactStateContractTest {
    @Test fun everyLocaleOwnsCompactLabelsAndFullSpokenLabels() {
        localeStringFiles.forEach { file ->
            val values = strings(file)
            stateKeys.forEach { assertTrue("${file.parentFile.name}: $it", !values[it].isNullOrBlank()) }
        }
        assertTrue(overlay.contains("drawStatusLabel(canvas, miniStateLabel(state))"))
        assertTrue(overlay.method("private fun stateDescription").contains("fullStateLabel(value)"))
        assertTrue(overlay.contains("STATUS_TEXT_WIDTH = 100"))
        assertTrue(overlay.contains("DESIGN_WIDTH = 300f"))
    }

    @Test fun miniMeasurementTypographyAssumptionsRemainCoupledToProduction() {
        assertTrue(overlay.contains("textSize = 16f"))
        assertTrue(overlay.contains("PrivateAudioState.entries.map(::miniStateLabel)"))
        assertTrue(overlay.contains("statusTextPaint.measureText(label)"))
        assertTrue(overlay.method("fun refreshLocalizedPresentation()").contains("refreshMiniStatusTextSize()"))
        assertFalse(overlay.method("private val refreshState").contains("refreshMiniStatusTextSize()"))
    }

    @Test fun measuredSelectionUsesLargestSharedCandidateAndHardMinimum() {
        assertEquals(16f, selected(mapOf("a" to 80f, "b" to 96f, "c" to 70f, "d" to 50f)))
        assertEquals(15f, selected(mapOf("a" to 80f, "b" to 97f, "c" to 70f, "d" to 50f)))
        assertEquals(14f, selected(mapOf("a" to 80f, "b" to 120f, "c" to 70f, "d" to 50f)))
    }

    private fun selected(widths: Map<String, Float>) = selectMiniStatusTextSize(widths.keys.toList()) { label, size ->
        widths.getValue(label) * size / 16f
    }.also { assertEquals(96f, MINI_STATUS_NON_ELLIPSIS_WIDTH) }

    private fun strings(file: File): Map<String, String> {
        val nodes = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file).getElementsByTagName("string")
        return (0 until nodes.length).associate { i -> nodes.item(i).attributes.getNamedItem("name").nodeValue to nodes.item(i).textContent }
    }
    private fun String.method(signature: String) = kotlinDeclaration(signature)

    private companion object {
        val root = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }.first { File(it, "app/src/main").isDirectory }
        val res = File(root, "app/src/main/res")
        val localeStringFiles = res.listFiles()!!.filter(File::isCompleteTargetLocalizationDirectory).map { File(it, "strings.xml") }.filter(File::isFile)
        val overlay = File(root, "app/src/main/java/app/privateaudio/overlay/OverlayService.kt").readText()
        val stateKeys = listOf(
            "status_mini_ready",
            "status_mini_waiting",
            "status_mini_active",
            "status_mini_error",
        )
    }
}
