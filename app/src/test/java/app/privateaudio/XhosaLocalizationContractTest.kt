package app.privateaudio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.Locale

class XhosaLocalizationContractTest {
    private val projectRoot = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
        .first { File(it, "app/src/main").isDirectory }
    private val strings = projectFile("app/src/main/res/values-xh/strings.xml").readText()

    @Test
    fun xhosaLocalePreservesIdentityAndProductSemantics() {
        val locale = Locale.forLanguageTag("xh")
        assertEquals("xh", locale.toLanguageTag())
        assertEquals("xh", locale.language)
        assertEquals("IsiXhosa", locale.getDisplayLanguage(locale))

        assertTrue(projectFile("app/src/main/res/values-xh/strings.xml").isFile)
        listOf("values-xh-rZA", "values-b+xh+Latn", "values-b+xh+Latn+ZA").forEach {
            assertFalse(projectFile("app/src/main/res/$it").exists())
        }

        assertEquals("Ilungile", resourceValue("state_ready"))
        assertEquals("Ilindile", resourceValue("state_waiting"))
        assertEquals("Iyasebenza", resourceValue("state_active"))
        assertEquals("Impazamo", resourceValue("state_error"))
        assertFalse(resourceValue("state_active") == resourceValue("diagnostics_on"))

        assertEquals("Private Audio", resourceValue("app_name"))
        assertEquals("Private Audio", resourceValue("product_title"))
        assertEquals("Iisetingi", resourceValue("settings"))
        assertEquals("Okumiselweyo", resourceValue("settings_system_default"))
        assertEquals("Ezihambele phambili", resourceValue("settings_advanced"))
        assertEquals("Umgaqo-nkqubo wabucala", resourceValue("settings_privacy_policy"))
        assertEquals("Ivuliwe", resourceValue("diagnostics_on"))
        assertEquals("Ivaliwe", resourceValue("diagnostics_off"))

        assertEquals("Isamkeli sendlebe", resourceValue("diagnostics_route_earpiece"))
        assertEquals("Isipikha", resourceValue("diagnostics_route_speaker"))
        assertFalse(resourceValue("diagnostics_route_earpiece") == resourceValue("diagnostics_route_speaker"))
        assertTrue(resourceValue("settings_about_body").contains("iaudio yelizwi exhaswayo"))
        assertTrue(resourceValue("settings_about_body").contains("isamkeli sendlebe esakhelwe ngaphakathi"))
        assertFalse(resourceValue("settings_about_body").contains("umnxeba", ignoreCase = true))

        assertTrue(strings.contains("ayiqokeleli, ayirekhodi, okanye ayithumeli"))
        assertTrue(strings.contains("Ayifikeleli kumxholo weencoko zakho"))
        assertTrue(strings.contains("ukunika ingxelo xa iapp ima ngokungalindelekanga"))
        assertTrue(strings.contains("igcinwa kuphela xa ukhetha ukuyigcina"))
        assertFalse(projectFile("app/src/main/res/values-xh/mini_state_strings.xml").exists())
    }

    private fun resourceValue(key: String): String =
        Regex("<string name=\"$key\">([^<]*)</string>").find(strings)?.groupValues?.get(1).orEmpty()

    private fun projectFile(relativePath: String) = File(projectRoot, relativePath)
}
