package app.privateaudio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.Locale

class SinhalaLocalizationContractTest {
    private val projectRoot = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
        .first { File(it, "app/src/main").isDirectory }
    private val strings = projectFile("app/src/main/res/values-si/strings.xml").readText()

    @Test
    fun sinhalaLocalePreservesIdentityAndProductSemantics() {
        val locale = Locale.forLanguageTag("si")
        assertEquals("si", locale.toLanguageTag())
        assertEquals("si", locale.language)
        assertEquals("සිංහල", locale.getDisplayLanguage(locale))

        assertTrue(projectFile("app/src/main/res/values-si/strings.xml").isFile)
        listOf("values-si-rLK", "values-b+si+Sinh", "values-b+si+Sinh+LK").forEach {
            assertFalse(projectFile("app/src/main/res/$it").exists())
        }

        assertEquals("සූදානම්", resourceValue("state_ready"))
        assertEquals("රැඳී සිටී", resourceValue("state_waiting"))
        assertEquals("සක්‍රිය", resourceValue("state_active"))
        assertEquals("දෝෂය", resourceValue("state_error"))
        assertEquals("ක්‍රියාත්මකයි", resourceValue("diagnostics_on"))
        assertEquals("ක්‍රියාවිරහිතයි", resourceValue("diagnostics_off"))
        assertFalse(resourceValue("state_active") == resourceValue("diagnostics_on"))

        assertEquals("Puzru", resourceValue("app_name"))
        assertEquals("Puzru", resourceValue("product_title"))
        assertEquals("සැකසීම්", resourceValue("settings"))
        assertEquals("පෙරනිමි", resourceValue("settings_system_default"))
        assertEquals("උසස්", resourceValue("settings_advanced"))
        assertEquals("රහස්‍යතා ප්‍රතිපත්තිය", resourceValue("settings_privacy_policy"))

        assertEquals("කන් රිසීවරය", resourceValue("diagnostics_route_earpiece"))
        assertEquals("ස්පීකරය", resourceValue("diagnostics_route_speaker"))
        assertFalse(resourceValue("diagnostics_route_earpiece") == resourceValue("diagnostics_route_speaker"))
        assertEquals("ලබා ගත හැකිය", resourceValue("diagnostics_earpiece_available"))
        assertEquals("ලබා ගත නොහැකිය", resourceValue("diagnostics_earpiece_not_available"))
        assertEquals("ලබා ගත හැකිය", resourceValue("diagnostics_proximity_available"))
        assertEquals("ලබා ගත නොහැකිය", resourceValue("diagnostics_proximity_not_available"))
        assertTrue(resourceValue("settings_about_body").contains("සහාය දක්වන AI කටහඬ ධාවනය"))
        assertTrue(resourceValue("settings_about_body").contains("දුරකථනයේ ඇතුළත් ඇමතුම් කන් රිසීවරයට"))
        assertTrue(resourceValue("settings_about_body").contains("ප්‍රධාන ශබ්ද විකාශකයෙන්"))

        val privacy = resourceValue("settings_privacy_summary_body")
        assertEquals(3, privacy.split("\\n\\n").size)
        assertTrue(privacy.contains("Puzru"))
        assertFalse(projectFile("app/src/main/res/values-si/mini_state_strings.xml").exists())
    }

    private fun resourceValue(key: String): String =
        Regex("<string name=\"$key\">([^<]*)</string>").find(strings)?.groupValues?.get(1).orEmpty()

    private fun projectFile(relativePath: String) = File(projectRoot, relativePath)
}
