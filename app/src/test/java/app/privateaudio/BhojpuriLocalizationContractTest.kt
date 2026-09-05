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
        assertEquals("Puzru", resourceValue("app_name"))
        assertEquals("Puzru", resourceValue("product_title"))

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
    fun privacyClaimSetAndRoutingActorsRemainProtected() {
        val privacy = resourceValue("settings_privacy_policy_body")
        assertEquals(4, Regex(Regex.escape("\\n\\n")).findAll(privacy).count())
        val privacyParagraphs = privacy.split("\\n\\n")
        assertEquals(5, privacyParagraphs.size)

        listOf(
            "खाता बनावे या साइन-इन करे के जरूरत नइखे", "माइक्रोफोन के अनुमति नइखे माँगत",
            "माइक्रोफोन के ऑडियो के कैप्चर या रिकॉर्ड नइखे करत", "ना ही रउरा बातचीत या ओकर ऑडियो सामग्री के रिकॉर्ड या स्टोर करत",
            "रिकॉर्डिंग-सेशन मेटाडेटा तकनीकी ऑडियो-सिस्टम जानकारी ह", "रउरा डिवाइस पर जनरेट आ प्रोसेस होला",
            "रिपोर्ट अपने-आप सेव या भेजल नइखे जात", "साफ तौर पर डायग्नोस्टिक रिपोर्ट सहेजें चुनीं",
            "मौजूदा संस्करण Android के Internet अनुमति नइखे माँगत", "बैकएंड या नेटवर्क ट्रांसमिशन पथ नइखे",
            "एनालिटिक्स, विज्ञापन, या क्रैश-रिपोर्टिंग सेवाएँ या SDKs शामिल नइखन", "डेवलपर या Puzru सर्वर के डायग्नोस्टिक रिपोर्ट नइखे भेजत",
            "Android क्लाउड बैकअप आ डिवाइस-से-डिवाइस ट्रांसफर से बाहर",
        ).forEach { guard -> assertTrue("Missing privacy guard: $guard", privacy.contains(guard)) }
        assertTrue(privacyParagraphs[1].contains("रूटिंग"))
        assertTrue(privacyParagraphs[2].contains("एक्सपोर्ट"))

        val rejected = resourceValue("diagnostics_error_request_rejected")
        assertTrue(rejected.contains("अनुरोध स्वीकार ना भइल"))
        listOf("Android", "सिस्टम", "फोन", "Puzru").forEach { actor ->
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
