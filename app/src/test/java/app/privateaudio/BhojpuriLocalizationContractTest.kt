package app.privateaudio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.Locale

class BhojpuriLocalizationContractTest {
    private val projectRoot = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
        .first { File(it, "app/src/main").isDirectory }
    private val resourceDirectory = projectFile("app/src/main/res/values-b+bho")
    private val stringsFile = File(resourceDirectory, "strings.xml")
    private val strings = stringsFile.readText()

    @Test
    fun bhojpuriLocaleUsesOneGenericBcp47ResourceAndLtrDirection() {
        val generic = Locale.forLanguageTag("bho")
        val india = Locale.forLanguageTag("bho-IN")
        assertEquals("bho", generic.toLanguageTag())
        assertEquals("bho", generic.language)
        assertEquals("bho-IN", india.toLanguageTag())
        assertEquals(generic.language, india.language)
        assertTrue(stringsFile.isFile)

        val competingDirectories = projectFile("app/src/main/res").listFiles().orEmpty()
            .filter { it.isDirectory && it != resourceDirectory }
            .filter {
                it.name == "values-bho" || it.name.startsWith("values-bho-") ||
                    it.name.startsWith("values-b+bho+")
            }
        assertTrue(competingDirectories.isEmpty())

        val appBuild = projectFile("app/build.gradle.kts").readText()
        assertTrue(appBuild.contains("minSdk = 31"))
        assertTrue(appBuild.contains("generateLocaleConfig = true"))
        assertFalse(projectFile("app/src/main/res/values-b+bho/mini_state_strings.xml").exists())

        assertEquals(
            Character.DIRECTIONALITY_LEFT_TO_RIGHT,
            Character.getDirectionality(generic.getDisplayLanguage(generic).first()),
        )
    }

    @Test
    fun productStatesBrandAndAudioOutputsRemainDistinct() {
        assertEquals("Private Audio", resourceValue("app_name"))
        assertEquals("Private Audio", resourceValue("product_title"))

        val runtimeStates = listOf("state_ready", "state_waiting", "state_active", "state_error")
            .map(::resourceValue)
        assertTrue(runtimeStates.all { it.isNotBlank() })
        assertEquals(runtimeStates.size, runtimeStates.toSet().size)
        assertEquals("सक्रिय बा", resourceValue("state_active"))
        assertFalse(resourceValue("diagnostics_on") == resourceValue("state_active"))

        val earpiece = resourceValue("diagnostics_route_earpiece")
        val loudspeaker = resourceValue("diagnostics_route_speaker")
        assertTrue(earpiece.contains("कान"))
        assertTrue(loudspeaker.contains("लाउडस्पीकर"))
        assertFalse(earpiece == loudspeaker)
        val about = resourceValue("settings_about_body")
        assertTrue(about.contains("फोन में लागल ऊपरी"))
        assertTrue(about.contains("कान वाला स्पीकर"))
        assertFalse(about.contains("लाउडस्पीकर"))

        val routingCopy = listOf(
            "routing_notification_text",
            "diagnostics_routing",
            "diagnostics_last_routing",
            "diagnostics_error_blocked_by_system",
            "diagnostics_error_session_ended",
            "diagnostics_error_audio_start",
            "diagnostics_error_request_rejected",
            "diagnostics_error_not_completed",
        ).map(::resourceValue)
        assertTrue(routingCopy.all { it.contains("रूट") })
        assertTrue(routingCopy.none { it.contains("भेज") })

        val communicationAudioError = resourceValue("diagnostics_error_audio_preparation")
        assertTrue(communicationAudioError.contains("कम्युनिकेशन ऑडियो"))
        assertFalse(communicationAudioError.contains("बातचीत के ऑडियो"))
    }

    @Test
    fun privacyStructureAndRoutingActorsRemainProtected() {
        val privacy = resourceValue("settings_privacy_policy_body")
        assertEquals(4, Regex(Regex.escape("\\n\\n")).findAll(privacy).count())
        assertEquals(5, privacy.split("\\n\\n").size)
        assertTrue(privacy.split("\\n\\n").all(String::isNotBlank))

        val rejected = resourceValue("diagnostics_error_request_rejected")
        assertTrue(rejected.contains("अनुरोध स्वीकार ना भइल"))
        listOf("Android", "सिस्टम", "फोन", "Private Audio").forEach { actor ->
            assertFalse("Rejected-request error invented actor: $actor", rejected.contains(actor, ignoreCase = true))
        }
        assertTrue(resourceValue("diagnostics_error_blocked_by_system").contains("सिस्टम"))

        assertFalse(strings.contains("name=\"settings_assistant_early_route\""))
        assertFalse(strings.contains("name=\"settings_assistant_early_route_description\""))
    }

    private fun resourceValue(key: String): String =
        Regex("<string name=\"$key\">([^<]*)</string>").find(strings)?.groupValues?.get(1).orEmpty()

    private fun projectFile(relativePath: String) = File(projectRoot, relativePath)
}
