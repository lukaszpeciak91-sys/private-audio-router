package app.privateaudio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.Locale
import javax.xml.parsers.DocumentBuilderFactory

class LocaleVariantReadinessContractTest {
    private val root = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
        .first { File(it, "app/src/main/res").isDirectory }
    private fun file(configuration: String) = File(root, "app/src/main/res/values-$configuration/strings.xml")

    @Test
    fun implementedVariantsHaveExactCanonicalIdentitiesAndUsableNativeNames() {
        mapOf("b+az+Cyrl" to "az-Cyrl", "b+bs+Cyrl" to "bs-Cyrl", "en-rGB" to "en-GB").forEach { (configuration, tag) ->
            assertTrue(configuration, file(configuration).isFile)
            assertEquals(tag, Locale.forLanguageTag(tag).toLanguageTag())
            val locale = Locale.forLanguageTag(tag)
            assertTrue("$tag native name", locale.getDisplayName(locale).isNotBlank())
        }
        assertEquals("Cyrl", Locale.forLanguageTag("az-Cyrl").script)
        assertEquals("Cyrl", Locale.forLanguageTag("bs-Cyrl").script)
        assertEquals("GB", Locale.forLanguageTag("en-GB").country)
        mapOf("ht" to "ht", "ky" to "ky", "tg" to "tg", "tk" to "tk", "ga" to "ga", "gd" to "gd").forEach { (configuration, tag) ->
            assertTrue(configuration, file(configuration).isFile)
            val locale = Locale.forLanguageTag(tag)
            assertEquals(tag, locale.toLanguageTag())
            assertTrue("$tag native name", locale.getDisplayName(locale).isNotBlank())
        }
        assertFalse(file("b+hi+Latn").exists())
    }

    @Test
    fun cyrillicVariantsAreCompleteDistinctScriptResources() {
        val defaultKeys = keys(File(root, "app/src/main/res/values/strings.xml")) - NON_TRANSLATABLE
        listOf("b+az+Cyrl", "b+bs+Cyrl").forEach { configuration ->
            val target = file(configuration)
            assertEquals(configuration, defaultKeys, keys(target))
            assertTrue(configuration, target.readText().any { it in '\u0400'..'\u04ff' })
        }
        assertNotEquals(value(file("b+az+Cyrl"), "settings"), value(file("az"), "settings"))
        assertNotEquals(value(file("b+bs+Cyrl"), "settings"), value(file("bs"), "settings"))
    }

    @Test
    fun ordinaryReadyLocalesAreCompleteAndPreserveDurableProductBoundaries() {
        val defaultKeys = keys(File(root, "app/src/main/res/values/strings.xml")) - NON_TRANSLATABLE
        val miniLabels = mapOf(
            "ht" to "Mini", "ky" to "Мини", "tg" to "Мини", "tk" to "Mini", "ga" to "Mini", "gd" to "Mini",
            "b+az+Cyrl" to "Мини", "b+bs+Cyrl" to "Мини",
        )
        miniLabels.forEach { (configuration, mini) ->
            val target = file(configuration)
            assertEquals(configuration, defaultKeys, keys(target))
            assertEquals("Puzru", value(target, "app_name"))
            assertEquals("PUZRU", value(target, "diagnostics_private_audio"))
            assertEquals(mini, value(target, "floating"))
            assertNotEquals(value(target, "routing_notification_waiting_title"), value(target, "state_active"))
            assertNotEquals(value(target, "state_ready"), value(target, "state_waiting"))
            assertNotEquals(value(target, "diagnostics_route_earpiece"), value(target, "diagnostics_route_speaker"))
            assertEquals(3, value(target, "settings_privacy_summary_body").split("\\n\\n").size)
            assertEquals(6, value(target, "settings_about_body").split("\\n\\n").size)
            assertTrue(value(target, "settings_about_body").contains("Napahu Studios"))
            assertFalse(target.readText().contains("ПУЗРУ"))
        }
        mapOf(
            "ky" to ("Puzru — Күйүк" to "Puzru — Өчүк"),
            "tg" to ("Puzru — Фаъол" to "Puzru — Хомӯш"),
            "tk" to ("Puzru — Açyk" to "Puzru — Öçük"),
        ).forEach { (configuration, actions) ->
            assertEquals(actions.first, value(file(configuration), "power_turn_on"))
            assertEquals(actions.second, value(file(configuration), "power_turn_off"))
        }
    }

    @Test
    fun britishEnglishIsAnIntentionalFallbackVariantNotASecondSource() {
        assertEquals(setOf("app_name"), keys(file("en-rGB")))
        assertEquals("Puzru", value(file("en-rGB"), "app_name"))
        assertEquals("unqualifiedResLocale=en-US", File(root, "app/src/main/res/resources.properties").readText().trim())
    }

    private fun keys(file: File): Set<String> = nodes(file).map { it.attributes.getNamedItem("name").nodeValue }.toSet()
    private fun value(file: File, key: String) = nodes(file).single { it.attributes.getNamedItem("name").nodeValue == key }.textContent
    private fun nodes(file: File) = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
        .getElementsByTagName("string").let { nodes -> (0 until nodes.length).map(nodes::item) }

    companion object {
        private val NON_TRANSLATABLE = setOf(
            "settings_privacy_policy_language", "settings_privacy_policy_body", "settings_assistant_early_route",
            "settings_assistant_early_route_description", "settings_assistant_session_continuity",
            "settings_assistant_session_continuity_description", "publisher_name", "privacy_support_email",
            "diagnostic_email_subject", "diagnostic_email_body",
        )
    }
}
