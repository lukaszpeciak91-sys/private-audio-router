package app.privateaudio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class MiniCompactStateContractTest {
    @Test fun reviewedTamilAndGujaratiParadigmsRemainFrozenBesideUnchangedFullStates() {
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
        assertEquals(
            mapOf("state_ready_mini" to "તૈયાર", "state_waiting_mini" to "પ્રતીક્ષા", "state_active_mini" to "સક્રિય", "state_error_mini" to "ભૂલ"),
            strings(gujaratiMini),
        )
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
        assertTrue(overlay.contains("textSize = 16f"))
        assertTrue(overlay.contains("android.graphics.Typeface.create(\"sans-serif\", android.graphics.Typeface.NORMAL)"))
        listOf("Locale", "language ==", "locale ==", "\"ta\"", "\"gu\"").forEach {
            assertFalse("locale branch found: $it", overlay.contains(it))
        }
        localeResourceFiles.filter { it.name != "strings.xml" }.forEach { file ->
            assertFalse("locale-specific geometry resource: $file", file.readText().contains("dimen"))
        }
    }

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

    private fun String.method(signature: String) = substring(indexOf(signature)).substringBefore("\n        }")

    private companion object {
        val root = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
            .first { File(it, "app/src/main").isDirectory }
        val res = File(root, "app/src/main/res")
        val overlay = File(root, "app/src/main/java/app/privateaudio/overlay/OverlayService.kt").readText()
        val aliasFile = File(res, "values/mini_state_aliases.xml")
        val tamilFull = File(res, "values-ta/strings.xml")
        val gujaratiFull = File(res, "values-gu/strings.xml")
        val tamilMini = File(res, "values-ta/mini_state_strings.xml")
        val gujaratiMini = File(res, "values-gu/mini_state_strings.xml")
        val localeStringFiles = res.listFiles()!!.filter { it.name.startsWith("values-") }
            .map { File(it, "strings.xml") }.filter(File::isFile)
        val localeResourceFiles = res.listFiles()!!.filter { it.name.startsWith("values-") }
            .flatMap { it.listFiles()?.toList().orEmpty() }
    }
}
