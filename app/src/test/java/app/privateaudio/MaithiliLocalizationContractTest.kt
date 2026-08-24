package app.privateaudio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.Locale

class MaithiliLocalizationContractTest {
    private val projectRoot = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
        .first { File(it, "app/src/main").isDirectory }
    private val resourceDirectory = projectFile("app/src/main/res/values-b+mai")
    private val stringsFile = File(resourceDirectory, "strings.xml")
    private val strings = stringsFile.readText()

    @Test
    fun maithiliLocaleUsesOneGenericBcp47ResourceAndLtrDirection() {
        val generic = Locale.forLanguageTag("mai")
        val india = Locale.forLanguageTag("mai-IN")
        assertEquals("mai", generic.toLanguageTag())
        assertEquals("mai", generic.language)
        assertEquals("mai-IN", india.toLanguageTag())
        assertEquals(generic.language, india.language)
        assertTrue(stringsFile.isFile)

        val competingDirectories = projectFile("app/src/main/res").listFiles().orEmpty()
            .filter { it.isDirectory && it != resourceDirectory }
            .filter {
                it.name == "values-mai" || it.name.startsWith("values-mai-") ||
                    it.name.startsWith("values-b+mai+")
            }
        assertTrue(competingDirectories.isEmpty())

        val appBuild = projectFile("app/build.gradle.kts").readText()
        assertTrue(appBuild.contains("minSdk = 31"))
        assertTrue(appBuild.contains("generateLocaleConfig = true"))
        assertFalse(projectFile("app/src/main/res/values-b+mai/mini_state_strings.xml").exists())
        assertEquals(
            Character.DIRECTIONALITY_LEFT_TO_RIGHT,
            Character.getDirectionality(generic.getDisplayLanguage(generic).first()),
        )
    }

    @Test
    fun productStatesBrandAndAudioConceptsRemainDistinct() {
        assertEquals("Private Audio", resourceValue("app_name"))
        assertEquals("Private Audio", resourceValue("product_title"))
        assertEquals("मिनी", resourceValue("floating"))

        val runtimeStates = listOf("state_ready", "state_waiting", "state_active", "state_error")
            .map(::resourceValue)
        assertTrue(runtimeStates.all { it.isNotBlank() })
        assertEquals(runtimeStates.size, runtimeStates.toSet().size)
        assertTrue(resourceValue("state_active").contains("रूटिंग"))
        assertFalse(resourceValue("diagnostics_on") == resourceValue("state_active"))

        val earpiece = resourceValue("diagnostics_route_earpiece")
        val loudspeaker = resourceValue("diagnostics_route_speaker")
        assertTrue(earpiece.contains("कान"))
        assertTrue(earpiece.contains("कॉल"))
        assertTrue(loudspeaker.contains("लाउडस्पीकर"))
        assertFalse(earpiece == loudspeaker)
        val about = resourceValue("settings_about_body")
        assertTrue(about.contains("फोनमे बनल"))
        assertTrue(about.contains("ऊपरी कॉल स्पीकर"))
        assertFalse(about.contains("लाउडस्पीकर"))

        val routingCopy = listOf(
            "routing_notification_text", "diagnostics_routing", "diagnostics_last_routing",
            "diagnostics_error_blocked_by_system", "diagnostics_error_session_ended",
            "diagnostics_error_audio_start", "diagnostics_error_request_rejected",
            "diagnostics_error_not_completed",
        ).map(::resourceValue)
        assertTrue(routingCopy.all { it.contains("रूट") })
        assertTrue(routingCopy.none { it.contains("पठब") || it.contains("प्रेष") })

        val communicationAudio = resourceValue("diagnostics_error_audio_preparation")
        assertTrue(communicationAudio.contains("संचार ऑडियो"))
        assertFalse(communicationAudio.contains("बातचीतक सामग्री"))
        assertTrue(resourceValue("settings_version").contains("संस्करण"))
        assertTrue(resourceValue("diagnostics_error_session_ended").contains("सत्र"))
    }

    @Test
    fun privacyClaimSetAndRoutingActorsRemainProtected() {
        val privacy = resourceValue("settings_privacy_policy_body")
        assertEquals(4, Regex(Regex.escape("\\n\\n")).findAll(privacy).count())
        val paragraphs = privacy.split("\\n\\n")
        assertEquals(5, paragraphs.size)

        listOf(
            "संग्रह", "रिकॉर्डिंग", "प्रसारण", "माइक्रोफोनक पहुँच नहि माँगैत",
            "एनालिटिक्स", "विज्ञापन", "क्रैश-रिपोर्टिंग", "इंटरनेट अनुमति नहि माँगैत",
            "सर्वर पर डेटा नहि पठबैत", "ऑडियो सिस्टमक तकनीकी स्थिति", "मेटाडेटा",
            "बातचीतक सामग्री धरि पहुँच नहि", "स्थानीय रूपेँ उत्पन्न आ प्रोसेस",
            "अहाँ ओकरा सहेजब चुनैत छी", "बातचीत वा ऑडियो सामग्री नहि",
            "ऐप-डेटाक बैकअप अक्षम",
        ).forEach { guard -> assertTrue("Missing privacy guard: $guard", privacy.contains(guard)) }
        assertTrue(paragraphs[2].contains("ऑडियो रूटिंग"))
        assertTrue(paragraphs[3].contains("ऑडियो रूटिंग"))
        assertFalse(paragraphs[2].contains("डेटा नहि पठबैत"))
        assertTrue(paragraphs[3].contains("Android संस्करण"))

        val rejected = resourceValue("diagnostics_error_request_rejected")
        assertTrue(rejected.contains("अनुरोध स्वीकार नहि भेल"))
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
