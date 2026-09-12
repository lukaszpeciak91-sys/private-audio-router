package app.privateaudio

import app.privateaudio.localization.AppLanguagePreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.Locale

class CentralKurdishLocalizationContractTest {
    private val stringsFile = projectFile("app/src/main/res/values-b+ckb/strings.xml")
    private val strings = stringsFile.readText()

    @Test
    fun centralKurdishKeepsOneBcp47ResourceIdentity() {
        val locale = Locale.forLanguageTag("ckb")

        assertEquals("ckb", locale.toLanguageTag())
        assertEquals("ckb", locale.language)
        assertTrue(AppLanguagePreferences.nativeName("ckb").isNotBlank())
        assertTrue(stringsFile.isFile)
        listOf("values-ckb", "values-b+ckb+Arab", "values-b+ckb+Arab+IQ").forEach {
            assertFalse(projectFile("app/src/main/res/$it").exists())
        }
    }

    @Test
    fun centralKurdishPreservesCoreStateAndRoutingDistinctions() {
        assertTrue(strings.contains("name=\"routing_notification_title\">Puzru چالاکە</string>"))
        assertTrue(strings.contains("name=\"state_active\">کارا</string>"))
        assertNotEquals(value("routing_notification_title"), value("state_active"))
        assertEquals("گوێگرە ناوخۆییەکەی پەیوەندی", value("diagnostics_route_earpiece"))
        assertEquals("بڵندگۆ", value("diagnostics_route_speaker"))
        assertNotEquals(value("diagnostics_route_earpiece"), value("diagnostics_route_speaker"))
        assertEquals("مینی", value("floating"))
    }

    private fun value(key: String): String =
        Regex("<string name=\"$key\">(.*?)</string>").find(strings)?.groupValues?.get(1)
            ?: error("Missing $key")

    private companion object {
        val root = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
            .first { File(it, "app/src/main/res").isDirectory }

        fun projectFile(relativePath: String) = File(root, relativePath)
    }
}
