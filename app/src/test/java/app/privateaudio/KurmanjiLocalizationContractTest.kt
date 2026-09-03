package app.privateaudio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.text.Normalizer
import java.util.Locale

class KurmanjiLocalizationContractTest {
    private val projectRoot = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
        .first { File(it, "app/src/main").isDirectory }
    private val resourceDirectory = projectFile("app/src/main/res/values-b+ku+Latn")
    private val stringsFile = File(resourceDirectory, "strings.xml")
    private val strings = stringsFile.readText()

    @Test
    fun kurmanjiUsesCanonicalLatinResourceForKuTrCompatibility() {
        val generic = Locale.forLanguageTag("ku")
        val turkey = Locale.forLanguageTag("ku-TR")
        val latin = Locale.forLanguageTag("ku-Latn")
        val latinTurkey = Locale.forLanguageTag("ku-Latn-TR")

        assertEquals("ku", generic.language)
        assertEquals("ku", turkey.language)
        assertEquals("TR", turkey.country)
        assertEquals("Latn", latin.script)
        assertEquals("Latn", latinTurkey.script)
        assertEquals("TR", latinTurkey.country)
        assertEquals("ku-Latn-TR", latinTurkey.toLanguageTag())
        // Android/ICU likely-subtag data maximizes ku-TR as ku-Latn-TR. This JVM
        // contract protects the inferred target identity and qualifier; it is not
        // a claim that an Android Resources runtime was exercised here.
        val expectedMaximizedKuTr = Locale.Builder().setLocale(turkey).setScript("Latn").build()
        assertEquals(latinTurkey, expectedMaximizedKuTr)
        assertEquals("values-b+ku+Latn", resourceDirectory.name)
        assertTrue(stringsFile.isFile)

        val forbidden = setOf(
            "values-ku", "values-ku-rTR", "values-b+ku+TR", "values-b+ku+Latn+TR",
            "values-ckb", "values-b+ckb", "values-b+ku+Arab",
        )
        val resourceNames = projectFile("app/src/main/res").listFiles().orEmpty().map { it.name }.toSet()
        assertTrue(resourceNames.intersect(forbidden).isEmpty())
        assertTrue(resourceNames.none { it.startsWith("values-b+ckb+") || it.startsWith("values-b+ku+Arab+") })

        val appBuild = projectFile("app/build.gradle.kts").readText()
        assertTrue(appBuild.contains("generateLocaleConfig = true"))
        assertFalse(projectFile("app/src/main/res/values-b+ku+Latn/mini_state_strings.xml").exists())
        assertEquals(
            Character.DIRECTIONALITY_LEFT_TO_RIGHT,
            Character.getDirectionality(generic.getDisplayLanguage(generic).first()),
        )
    }

    @Test
    fun kurmanjiResourceUsesHawarAndContainsNoArabicScript() {
        assertEquals(strings, Normalizer.normalize(strings, Normalizer.Form.NFC))
        assertTrue(listOf('ç', 'ê', 'î', 'ş', 'û', 'q', 'w', 'x').all { strings.contains(it) })
        assertFalse(Regex("[\\u0600-\\u06ff\\u0750-\\u077f\\u08a0-\\u08ff]").containsMatchIn(strings))
    }

    @Test
    fun productStatesBrandAndAudioReferentsRemainDistinct() {
        assertEquals("Private Audio", resourceValue("app_name"))
        assertEquals("Private Audio", resourceValue("product_title"))
        assertEquals("Wekî heyî", resourceValue("settings_system_default"))

        val overlay = resourceValue("overlay_controller_description")
        assertTrue(overlay.contains("li ser sepanên din"))
        assertFalse(overlay.contains("biçûk", ignoreCase = true))

        val states = listOf("state_ready", "state_waiting", "state_active", "state_error").map(::resourceValue)
        assertEquals(4, states.toSet().size)
        assertTrue(resourceValue("state_active").contains("Niha di rêkirinê de"))
        assertFalse(resourceValue("diagnostics_on") == resourceValue("state_active"))

        val earpiece = resourceValue("diagnostics_route_earpiece")
        val speaker = resourceValue("diagnostics_route_speaker")
        assertTrue(earpiece.contains("wergira bangê", ignoreCase = true))
        assertTrue(speaker.contains("bilindbêj", ignoreCase = true))
        assertFalse(earpiece == speaker)
        val about = resourceValue("settings_about_body")
        listOf("jorîn", "hundirê telefona", "ber guhê").forEach { assertTrue(about.contains(it)) }

        val routing = listOf(
            "routing_notification_text", "diagnostics_routing", "diagnostics_last_routing",
            "diagnostics_error_blocked_by_system", "diagnostics_error_session_ended",
            "diagnostics_error_audio_start", "diagnostics_error_request_rejected",
            "diagnostics_error_not_completed",
        ).map(::resourceValue)
        assertTrue(routing.all { it.contains("rêkir", ignoreCase = true) })
        assertTrue(routing.none { it.contains("şand", ignoreCase = true) || it.contains("veneguhez", ignoreCase = true) })
        val communicationAudio = resourceValue("diagnostics_error_audio_preparation")
        assertTrue(communicationAudio.contains("dengê ragihandinê"))
        assertFalse(communicationAudio.contains("naveroka axaftin"))
        assertTrue(resourceValue("settings_version").contains("Guhertoya"))
        assertTrue(resourceValue("diagnostics_error_session_ended").contains("Danişîna dengê"))
    }

    @Test
    fun privacyClaimSetAndRoutingActorsRemainProtected() {
        val privacy = resourceValue("settings_privacy_policy_body")
        assertEquals(4, Regex(Regex.escape("\\n\\n")).findAll(privacy).count())
        val paragraphs = privacy.split("\\n\\n")
        assertEquals(5, paragraphs.size)
        listOf(
            "ne hesabek an têketinê hewce dike", "destûra mîkrofônê dixwaze", "dengê mîkrofônê nagire an tomar nake",
            "ne jî axaftinên we an naveroka dengî ya wan tomar dike an diparêze", "Metadata-yên danişînên tomarê agahiyên teknîkî yên pergala dengê ne",
            "dengê mîkrofônê ku bi wan danişînan ve girêdayî ye nagire", "li ser cîhaza we têne afirandin û pêvajokirin", "Rapor bixweber nayên tomarkirin an şandin",
            "tenê dema ku hûn bi eşkereyî Tomara rapora teşhîsê hilbijêrin", "Guhertoya niha destûra Internetê ya Android naxwaze",
            "ne backendekî Private Audio heye ne jî rêyek şandina torê heye", "SDK-yên analîtîk, reklam, an raporkirina çewtiyan",
            "raporên teşhîsê ji pêşdebirê an jî ji serveurê Private Audio re naşîne", "cloud backup û veguheztina ji cîhazekê bo cîhazekî din a Android têne derxistin",
        ).forEach { guard -> assertTrue("Missing privacy guard: $guard", privacy.contains(guard)) }

        val rejected = resourceValue("diagnostics_error_request_rejected")
        assertTrue(rejected.contains("nehat pejirandin"))
        listOf("Android", "pergal", "telefon", "Private Audio").forEach { actor ->
            assertFalse("Rejected request invented actor: $actor", rejected.contains(actor, ignoreCase = true))
        }
        assertTrue(resourceValue("diagnostics_error_blocked_by_system").contains("pergalê"))
        assertFalse(strings.contains("name=\"settings_assistant_early_route\""))
        assertFalse(strings.contains("name=\"settings_assistant_early_route_description\""))
    }

    private fun resourceValue(key: String): String =
        Regex("<string name=\"$key\">([^<]*)</string>").find(strings)?.groupValues?.get(1).orEmpty()

    private fun projectFile(relativePath: String) = File(projectRoot, relativePath)
}
