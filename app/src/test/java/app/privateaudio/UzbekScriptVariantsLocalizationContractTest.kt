package app.privateaudio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.text.Normalizer
import java.util.Locale
import javax.xml.parsers.DocumentBuilderFactory

class UzbekScriptVariantsLocalizationContractTest {
    private val projectRoot = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
        .first { File(it, "app/src/main/res").isDirectory }
    private val latinFile = projectFile("app/src/main/res/values-uz/strings.xml")
    private val cyrillicFile = projectFile("app/src/main/res/values-b+uz+Cyrl+UZ/strings.xml")
    private val arabicFile = projectFile("app/src/main/res/values-b+uz+Arab+AF/strings.xml")

    @Test
    fun threeUzbekScriptsHaveDistinctExactResourceIdentitiesAndDirections() {
        assertTrue(latinFile.isFile)
        assertTrue(cyrillicFile.isFile)
        assertTrue(arabicFile.isFile)
        assertEquals("Latn", Locale.forLanguageTag("uz-Latn-UZ").script)
        assertEquals("Cyrl", Locale.forLanguageTag("uz-Cyrl-UZ").script)
        assertEquals("Arab", Locale.forLanguageTag("uz-Arab-AF").script)
        assertEquals(Character.DIRECTIONALITY_LEFT_TO_RIGHT, Character.getDirectionality(value(cyrillicFile, "settings").first()))
        assertEquals(Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC, Character.getDirectionality(value(arabicFile, "settings").first()))

        listOf("values-b+uz+Latn+UZ", "values-b+uz+Cyrl", "values-b+uz+Arab").forEach {
            assertFalse(projectFile("app/src/main/res/$it").exists())
        }
    }

    @Test
    fun variantsMatchTheExistingUzbekKeyAndPlaceholderContract() {
        val expectedKeys = values(latinFile).keys
        listOf(cyrillicFile, arabicFile).forEach { file ->
            val candidate = values(file)
            assertEquals("${file.parentFile.name} keys", expectedKeys, candidate.keys)
            expectedKeys.forEach { key ->
                assertEquals("${file.parentFile.name}/$key placeholders", placeholders(values(latinFile).getValue(key)), placeholders(candidate.getValue(key)))
            }
            assertTrue("${file.parentFile.name} must be NFC", Normalizer.isNormalized(file.readText(), Normalizer.Form.NFC))
        }
    }

    @Test
    fun scriptIdentityAndProtectedProductConceptsRemainDistinct() {
        val cyrillic = cyrillicFile.readText()
        val arabic = arabicFile.readText()
        assertTrue(cyrillic.any { it in '\u0400'..'\u04FF' })
        assertFalse(cyrillic.any { it in '\u0600'..'\u06FF' })
        assertTrue(arabic.any { it in '\u0600'..'\u06FF' })
        assertFalse(arabic.any { it in '\u0400'..'\u04FF' })

        listOf(cyrillicFile, arabicFile).forEach { file ->
            assertEquals("Puzru", value(file, "app_name"))
            assertNotEquals(value(file, "state_active"), value(file, "diagnostics_on"))
            assertNotEquals(value(file, "diagnostics_route_earpiece"), value(file, "diagnostics_route_speaker"))
            assertTrue(value(file, "diagnostics_error_request_rejected").contains(value(file, "diagnostics_route_earpiece")))
            assertFalse(value(file, "diagnostics_error_request_rejected").contains(value(file, "diagnostics_route_speaker")))
            assertFalse(file.readText().contains("name=\"settings_assistant_early_route\""))
        }
        assertTrue(value(cyrillicFile, "diagnostics_error_audio_preparation").contains("алоқа аудиосини"))
        assertTrue(value(arabicFile, "diagnostics_error_audio_preparation").contains("آلاقه آدیوسینی"))
        assertTrue(value(arabicFile, "product_subtitle").contains("صنعي عقل"))
        assertFalse(value(arabicFile, "product_subtitle").contains("مصنوعی اقل"))
        assertEquals("مخفی‌لیک سیاستی", value(arabicFile, "settings_privacy_policy"))
        assertFalse(value(arabicFile, "settings_privacy_policy").contains("محرم"))
    }

    @Test
    fun privacyClaimSetsRetainFiveParagraphsAndBoundedActions() {
        listOf(cyrillicFile, arabicFile).forEach { file ->
            val privacy = value(file, "settings_privacy_policy_body")
            val paragraphs = privacy.split("\\n\\n")
            assertEquals("${file.parentFile.name} privacy paragraphs", 5, paragraphs.size)
            assertTrue(paragraphs.all(String::isNotBlank))
            assertFalse("Local metadata observation must not become conversation processing", paragraphs[2].contains(paragraphs[0]))
        }

        val cyrillic = value(cyrillicFile, "settings_privacy_policy_body")
        listOf(
            "ҳисоб ёки тизимга киришни талаб қилмайди", "микрофон рухсатини сўрамайди", "микрофон овозини ушламайди ёки ёзиб олмайди",
            "суҳбатларингизни ёки уларнинг аудио мазмунини ёзиб олмайди ёки сақламайди", "Ёзиб олиш сеанси метамаълумотлари техник аудио тизими маълумотидир",
            "сеанслар билан боғлиқ микрофон овозини ушламайди", "қурилмангизда яратилади ва қайта ишланади", "Ҳисоботлар автоматик тарзда сақланмайди ёки юборилмайди",
            "фақат сиз “Diagnostika ҳисоботини сақлаш”ни очиқ танлаганингизда", "Жорий версия Android’нинг Интернет рухсатини сўрамайди",
            "ҳеч қандай бекендга ёки тармоқ орқали узатиш йўлига эга эмас", "аналитика, реклама ёки носозликлар ҳақида ҳисобот бериш хизматлари ёки SDK’ларни ўз ичига олмайди",
            "диагностик ҳисоботларни дастурчига ёки Puzru серверига юбормайди", "Android булутли захира нусхасидан ва қурилмадан қурилмага кўчиришдан чиқариб ташланган",
        ).forEach { assertTrue("Missing Cyrillic Privacy guard: $it", cyrillic.contains(it)) }

        val arabic = value(arabicFile, "settings_privacy_policy_body")
        assertTrue(arabic.any { it in '\u0600'..'\u06ff' })
        listOf("قىلمايدۇ", "ئىشلىتىدۇ", "ئۇچۇر", "ئۈسكۈنە", "دوكلات", "ئەۋەتىلمەيدۇ")
            .forEach { assertFalse("Uyghur drift in Uzbek-AF Privacy text: $it", arabic.contains(it)) }
        listOf(
            "مایکروفون اجازتینی سورمه‌یدی", "ضبط جلسه‌سینینگ میته‌معلوماتی آدیو سیستمینه عاید تخنیکی معلومات‌دیر", "عمومی Android API‌لری",
            "ایلووه‌گه مخصوص مخفی سقلش جاییده محلی سقلنه‌دی", "خودکار سقلنمه‌یدی یا یوبریلمه‌یدی", "حاضرگی نسخه Android‌نینگ انترنت اجازتینی سورمه‌یدی",
            "شبکه آرقلی یوبریش یۉلی یۉق", "سرورده تشخیص معلوماتینی سقلمه‌یدی",
            "بولوتی‌ده‌گی زحیره نسخه و قوریلمه‌دن قوریلمه‌گه اۉتکزیشدن چیقریلب تشلنگن",
        )
            .forEach { assertTrue("Missing Uzbek-AF semantic guard: $it", arabic.contains(it)) }

    }

    private fun values(file: File): Map<String, String> {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
        val strings = document.getElementsByTagName("string")
        return (0 until strings.length).associate { index ->
            val element = strings.item(index)
            element.attributes.getNamedItem("name").nodeValue to element.textContent
        }
    }

    private fun value(file: File, key: String): String = values(file).getValue(key)

    private fun placeholders(text: String): List<String> = Regex("%\\d+\\$[a-zA-Z]").findAll(text).map { it.value }.toList()

    private fun projectFile(relativePath: String) = File(projectRoot, relativePath)
}
