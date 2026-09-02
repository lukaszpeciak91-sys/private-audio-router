package app.privateaudio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.text.Normalizer
import java.util.Locale

class CebuanoLocalizationContractTest {
    private val projectRoot = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
        .first { File(it, "app/src/main").isDirectory }
    private val resourceDirectory = projectFile("app/src/main/res/values-b+ceb")
    private val stringsFile = File(resourceDirectory, "strings.xml")
    private val strings = stringsFile.readText()

    @Test
    fun cebuanoUsesOneCanonicalGenericResourceForCebPh() {
        val generic = Locale.forLanguageTag("ceb")
        val philippines = Locale.forLanguageTag("ceb-PH")
        val latinPhilippines = Locale.forLanguageTag("ceb-Latn-PH")

        assertEquals("ceb", generic.toLanguageTag())
        assertEquals("ceb", philippines.language)
        assertEquals("PH", philippines.country)
        assertEquals("ceb-PH", philippines.toLanguageTag())
        assertEquals("ceb", latinPhilippines.language)
        assertEquals("Latn", latinPhilippines.script)
        assertEquals("PH", latinPhilippines.country)
        assertEquals("ceb-Latn-PH", latinPhilippines.toLanguageTag())
        assertEquals("values-b+ceb", resourceDirectory.name)
        assertTrue(stringsFile.isFile)

        val competingDirectories = projectFile("app/src/main/res").listFiles().orEmpty()
            .filter { it.isDirectory && it.name != "values-b+ceb" }
            .filter {
                it.name == "values-ceb" || it.name.startsWith("values-ceb-") ||
                    it.name.startsWith("values-b+ceb+")
            }
        assertTrue(competingDirectories.isEmpty())

        val appBuild = projectFile("app/build.gradle.kts").readText()
        assertTrue(appBuild.contains("minSdk = 31"))
        assertTrue(appBuild.contains("targetSdk = 36"))
        assertTrue(appBuild.contains("generateLocaleConfig = true"))
        assertFalse(projectFile("app/src/main/res/values-b+ceb/mini_state_strings.xml").exists())
        assertEquals(strings, Normalizer.normalize(strings, Normalizer.Form.NFC))
        assertEquals(
            Character.DIRECTIONALITY_LEFT_TO_RIGHT,
            Character.getDirectionality(generic.getDisplayLanguage(generic).first()),
        )
    }

    @Test
    fun productStatesAndAudioConceptsRemainDistinct() {
        assertEquals("Private Audio", resourceValue("app_name"))
        assertEquals("Private Audio", resourceValue("product_title"))
        assertEquals("Mini", resourceValue("floating"))

        val states = listOf("state_ready", "state_waiting", "state_active", "state_error")
            .map(::resourceValue)
        assertEquals(states.size, states.toSet().size)
        assertEquals("Naka-on", resourceValue("diagnostics_on"))
        assertEquals("Aktibo", resourceValue("state_active"))
        assertFalse(resourceValue("diagnostics_on") == resourceValue("state_active"))

        val earpiece = resourceValue("diagnostics_route_earpiece")
        val loudspeaker = resourceValue("diagnostics_route_speaker")
        assertTrue(earpiece.contains("earpiece", ignoreCase = true))
        assertTrue(earpiece.contains("built-in", ignoreCase = true))
        assertTrue(loudspeaker.contains("loudspeaker", ignoreCase = true))
        assertFalse(earpiece == loudspeaker)
        assertTrue(resourceValue("settings_about_body").contains("built-in nga earpiece"))

        val routingValues = listOf(
            "routing_notification_text", "diagnostics_routing", "diagnostics_last_routing",
            "diagnostics_error_blocked_by_system", "diagnostics_error_session_ended",
            "diagnostics_error_audio_start", "diagnostics_error_request_rejected",
            "diagnostics_error_not_completed",
        ).map(::resourceValue)
        assertTrue(routingValues.all { it.contains("route", ignoreCase = true) })
        assertFalse(resourceValue("diagnostics_error_audio_preparation").contains("panag-istorya"))
    }

    @Test
    fun privacyStructureAndRoutingActorsRemainProtected() {
        val privacy = resourceValue("settings_privacy_policy_body")
        assertEquals(4, Regex(Regex.escape("\\n\\n")).findAll(privacy).count())
        assertEquals(5, privacy.split("\\n\\n").size)
        assertTrue(privacy.split("\\n\\n").all(String::isNotBlank))

        val rejected = resourceValue("diagnostics_error_request_rejected")
        assertTrue(rejected.startsWith("Wala dawata ang hangyo"))
        listOf("Android", "system", "telepono", "Private Audio").forEach { actor ->
            assertFalse("Rejected request invented actor: $actor", rejected.contains(actor, ignoreCase = true))
        }
        assertTrue(resourceValue("diagnostics_error_blocked_by_system").contains("system"))

        assertFalse(strings.contains("name=\"settings_assistant_early_route\""))
        assertFalse(strings.contains("name=\"settings_assistant_early_route_description\""))
    }

    private fun resourceValue(key: String): String =
        Regex("<string name=\"$key\">([^<]*)</string>").find(strings)?.groupValues?.get(1).orEmpty()

    private fun projectFile(relativePath: String) = File(projectRoot, relativePath)
}
