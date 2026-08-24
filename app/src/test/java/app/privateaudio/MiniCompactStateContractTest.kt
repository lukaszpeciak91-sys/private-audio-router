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
    @Test fun reviewedCompactParadigmsRemainNarrowAndGujaratiUsesUnchangedFullStates() {
        assertEquals(
            mapOf("state_ready" to "தயார்", "state_waiting" to "காத்திருக்கிறது", "state_active" to "செயலில் உள்ளது", "state_error" to "பிழை"),
            fullStates(tamilFull),
        )
        assertEquals(
            mapOf("state_ready" to "તૈયાર", "state_waiting" to "રાહ જોઈ રહ્યું છે", "state_active" to "સક્રિય", "state_error" to "ભૂલ"),
            fullStates(gujaratiFull),
        )
        assertEquals(
            mapOf("state_ready_mini" to "தயார்", "state_waiting_mini" to "காத்திருப்பு", "state_active_mini" to "செயலில்", "state_error_mini" to "பிழை"),
            strings(tamilMini),
        )
        assertTrue(tamilMini.isFile)
        assertFalse(gujaratiMini.exists())
        assertEquals(
            mapOf("state_ready" to "തയ്യാറാണ്", "state_waiting" to "കാത്തിരിക്കുന്നു", "state_active" to "സജീവം", "state_error" to "പിശക്"),
            fullStates(malayalamFull),
        )
        assertEquals(mapOf("state_waiting_mini" to "കാത്തിരിപ്പ്"), strings(malayalamMini))
        assertFalse(strings(malayalamMini)["state_waiting_mini"] == fullStates(malayalamFull)["state_waiting"])
    }

    @Test fun defaultMiniResourcesAliasLocalizedFullResources() {
        val aliases = resources(aliasFile, "item")
        assertEquals("@string/state_ready", aliases["state_ready_mini"])
        assertEquals("@string/state_waiting", aliases["state_waiting_mini"])
        assertEquals("@string/state_active", aliases["state_active_mini"])
        assertEquals("@string/state_error", aliases["state_error_mini"])
        localeStringFiles.forEach { locale ->
            val localized = strings(locale)
            aliases.values.forEach { target ->
                assertTrue("${locale.parentFile.name} lacks localized $target", localized.containsKey(target.removePrefix("@string/")))
            }
        }
    }

    @Test fun miniUsesCompactVisualCopyButFullSpokenCopyAndOneSharedGeometry() {
        assertTrue(overlay.contains("drawStatusLabel(canvas, miniStateLabel(state))"))
        assertTrue(overlay.method("private fun stateDescription").contains("fullStateLabel(value)"))
        assertTrue(overlay.contains("STATUS_TEXT_WIDTH = 100"))
        assertTrue(overlay.contains("DESIGN_WIDTH = 300f"))
        listOf("language ==", "locale ==", "\"ta\"", "\"gu\"").forEach {
            assertFalse("locale branch found: $it", productionKotlin.contains(it))
        }
        localeResourceFiles.filter { it.name != "strings.xml" }.forEach { file ->
            assertFalse("locale-specific geometry resource: $file", file.readText().contains("dimen"))
        }
    }

    @Test fun miniMeasurementTypographyAssumptionsRemainCoupledToProduction() {
        assertTrue(overlay.contains("textSize = 16f"))
        assertTrue(overlay.contains("android.graphics.Typeface.create(\"sans-serif\", android.graphics.Typeface.NORMAL)"))
        assertTrue(overlay.contains("PrivateAudioState.entries.map(::miniStateLabel)"))
        assertTrue(overlay.contains("statusTextPaint.measureText(label)"))
        assertTrue(overlay.method("fun refreshLocalizedPresentation()").contains("refreshMiniStatusTextSize()"))
        assertFalse(overlay.method("private val refreshState").contains("refreshMiniStatusTextSize()"))
    }

    @Test fun measuredSelectionUsesLargestSharedCandidateAndHardMinimum() {
        assertEquals(16f, selected(mapOf("ready" to 80f, "waiting" to 96f, "active" to 70f, "error" to 50f)))
        assertEquals(15f, selected(mapOf("ready" to 80f, "waiting" to 97f, "active" to 70f, "error" to 50f)))
        assertEquals(14f, selected(mapOf("ready" to 80f, "waiting" to 103f, "active" to 70f, "error" to 50f)))
        assertEquals(14f, selected(mapOf("ready" to 80f, "waiting" to 120f, "active" to 70f, "error" to 50f)))
    }

    @Test fun measuredSelectionConsidersEveryStateRatherThanStringLength() {
        val equalLengthLabels = listOf("aaaa", "bbbb", "cccc", "dddd")
        val measuredWidthsAt16 = mapOf("aaaa" to 40f, "bbbb" to 40f, "cccc" to 97f, "dddd" to 40f)
        val chosen = selectMiniStatusTextSize(equalLengthLabels) { label, size ->
            measuredWidthsAt16.getValue(label) * size / 16f
        }
        assertEquals(15f, chosen)
        assertEquals(4, equalLengthLabels.map(String::length).distinct().single())
    }

    private fun selected(widthsAt16: Map<String, Float>) =
        selectMiniStatusTextSize(listOf("ready", "waiting", "active", "error")) { label, size ->
            widthsAt16.getValue(label) * size / 16f
        }.also { assertEquals(96f, MINI_STATUS_NON_ELLIPSIS_WIDTH) }

    private fun strings(file: File) = resources(file, "string")
    private fun fullStates(file: File) = strings(file).filterKeys {
        it in setOf("state_ready", "state_waiting", "state_active", "state_error")
    }

    private fun resources(file: File, tag: String): Map<String, String> {
        val nodes = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file).getElementsByTagName(tag)
        return (0 until nodes.length).associate { index ->
            val node = nodes.item(index)
            node.attributes.getNamedItem("name").nodeValue to node.textContent
        }
    }

    private fun String.method(signature: String) = kotlinDeclaration(signature)

    private companion object {
        val root = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
            .first { File(it, "app/src/main").isDirectory }
        val res = File(root, "app/src/main/res")
        val productionKotlinFiles = File(root, "app/src/main/java").walkTopDown().filter { it.extension == "kt" }.toList()
        val productionKotlin = productionKotlinFiles.joinToString("\n") { it.readText() }
        val overlay = File(root, "app/src/main/java/app/privateaudio/overlay/OverlayService.kt").readText()
        val aliasFile = File(res, "values/mini_state_aliases.xml")
        val tamilFull = File(res, "values-ta/strings.xml")
        val gujaratiFull = File(res, "values-gu/strings.xml")
        val tamilMini = File(res, "values-ta/mini_state_strings.xml")
        val gujaratiMini = File(res, "values-gu/mini_state_strings.xml")
        val malayalamFull = File(res, "values-ml/strings.xml")
        val malayalamMini = File(res, "values-ml/mini_state_strings.xml")
        val localeStringFiles = res.listFiles()!!.filter { it.name.startsWith("values-") }
            .map { File(it, "strings.xml") }.filter(File::isFile)
        val localeResourceFiles = res.listFiles()!!.filter { it.name.startsWith("values-") }
            .flatMap { it.listFiles()?.toList().orEmpty() }
    }
}
