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
        assertEquals("අක්‍රියයි", resourceValue("diagnostics_off"))
        assertFalse(resourceValue("state_active") == resourceValue("diagnostics_on"))

        assertEquals("Private Audio", resourceValue("app_name"))
        assertEquals("Private Audio", resourceValue("product_title"))
        assertEquals("සැකසීම්", resourceValue("settings"))
        assertEquals("පෙරනිමි", resourceValue("settings_system_default"))
        assertEquals("උසස්", resourceValue("settings_advanced"))
        assertEquals("රහස්‍යතා ප්‍රතිපත්තිය", resourceValue("settings_privacy_policy"))

        assertEquals("කන් රිසීවරය", resourceValue("diagnostics_route_earpiece"))
        assertEquals("ස්පීකරය", resourceValue("diagnostics_route_speaker"))
        assertFalse(resourceValue("diagnostics_route_earpiece") == resourceValue("diagnostics_route_speaker"))
        assertTrue(resourceValue("settings_about_body").contains("සහාය දක්වන කටහඬ ශ්‍රව්‍යය"))
        assertTrue(resourceValue("settings_about_body").contains("දුරකථනයේ ඇතුළත් කන් රිසීවරය"))
        assertFalse(resourceValue("settings_about_body").contains("ඇමතුම්"))

        assertTrue(strings.contains("එකතු කිරීම, පටිගත කිරීම හෝ සම්ප්‍රේෂණය කිරීම නොකරයි"))
        assertTrue(strings.contains("සංවාදවල අන්තර්ගතයට ප්‍රවේශ නොවේ"))
        assertTrue(strings.contains("යෙදුම අනපේක්ෂිත ලෙස නතර වූ විට ඒ බව වාර්තා කරන සේවා"))
        assertTrue(strings.contains("රෝග විනිශ්චය දත්ත දේශීයව ජනනය කර සකසනු ලබන"))
        assertTrue(strings.contains("සුරැකෙන්නේ ඔබ එය සුරැකීමට තෝරාගත් විට පමණි"))
        assertTrue(strings.contains("Android යෙදුම් දත්ත උපස්ථ කිරීම අබල කර ඇත"))
        assertFalse(projectFile("app/src/main/res/values-si/mini_state_strings.xml").exists())
    }

    private fun resourceValue(key: String): String =
        Regex("<string name=\"$key\">([^<]*)</string>").find(strings)?.groupValues?.get(1).orEmpty()

    private fun projectFile(relativePath: String) = File(projectRoot, relativePath)
}
