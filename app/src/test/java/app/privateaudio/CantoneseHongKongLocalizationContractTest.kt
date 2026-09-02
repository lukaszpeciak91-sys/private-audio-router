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

class CantoneseHongKongLocalizationContractTest {
    private val projectRoot = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
        .first { File(it, "app/src/main").isDirectory }
    private val resourcePath = "app/src/main/res/values-b+yue+Hant+HK/strings.xml"
    private val simplifiedResourcePath = "app/src/main/res/values-b+yue+Hans+CN/strings.xml"
    private val strings = projectFile(resourcePath).readText()

    @Test
    fun cantoneseHongKongLocalePreservesIdentityAndProductSemantics() {
        val locale = Locale.forLanguageTag("yue-Hant-HK")
        assertEquals("yue-Hant-HK", locale.toLanguageTag())
        assertEquals("yue", locale.language)
        assertEquals("Hant", locale.script)
        assertEquals("HK", locale.country)

        assertTrue(projectFile(resourcePath).isFile)
        listOf("values-yue", "values-yue-rHK", "values-b+yue+Hant", "values-b+zh+Hant+HK").forEach {
            assertFalse(projectFile("app/src/main/res/$it").exists())
        }

        assertEquals("準備好", resourceValue("state_ready"))
        assertEquals("等緊", resourceValue("state_waiting"))
        assertEquals("使用中", resourceValue("state_active"))
        assertEquals("錯誤", resourceValue("state_error"))
        assertEquals(4, setOf("state_ready", "state_waiting", "state_active", "state_error").map(::resourceValue).toSet().size)
        assertEquals("開啟", resourceValue("diagnostics_on"))
        assertEquals("關閉", resourceValue("diagnostics_off"))
        assertNotEquals(resourceValue("state_active"), resourceValue("diagnostics_on"))

        assertEquals("Private Audio", resourceValue("app_name"))
        assertEquals("Private Audio", resourceValue("product_title"))
        assertEquals("迷你", resourceValue("floating"))
        assertEquals("私隱政策", resourceValue("settings_privacy_policy"))
        assertEquals("聽筒", resourceValue("diagnostics_route_earpiece"))
        assertEquals("喇叭", resourceValue("diagnostics_route_speaker"))
        assertNotEquals(resourceValue("diagnostics_route_earpiece"), resourceValue("diagnostics_route_speaker"))
        assertEquals(Character.DIRECTIONALITY_LEFT_TO_RIGHT, Character.getDirectionality(resourceValue("settings").first()))
        assertTrue(strings.any { it in '\u4E00'..'\u9FFF' })

        val privacy = resourceValue("settings_privacy_policy_body")
        assertEquals(4, privacy.windowed(4).count { it == "\\n\\n" })
        assertEquals(5, privacy.split("\\n\\n").size)

    }

    @Test
    fun simplifiedCantoneseIsAnIndependentMainlandProductLocale() {
        val locale = Locale.forLanguageTag("yue-Hans-CN")
        assertEquals("yue-Hans-CN", locale.toLanguageTag())
        assertEquals("yue", locale.language)
        assertEquals("Hans", locale.script)
        assertEquals("CN", locale.country)

        val traditionalFile = projectFile(resourcePath)
        val simplifiedFile = projectFile(simplifiedResourcePath)
        assertTrue(simplifiedFile.isFile)
        assertEquals(values(traditionalFile).keys, values(simplifiedFile).keys)
        values(traditionalFile).forEach { (key, traditionalValue) ->
            assertEquals("$key placeholders", placeholders(traditionalValue), placeholders(values(simplifiedFile).getValue(key)))
        }
        assertTrue(Normalizer.isNormalized(simplifiedFile.readText(), Normalizer.Form.NFC))
        assertFalse(projectFile("app/src/main/res/values-b+yue+Hans").exists())
        assertFalse(projectFile("app/src/main/res/values-yue-rCN").exists())

        assertEquals("设置", simplifiedValue("settings"))
        assertEquals("听筒", simplifiedValue("diagnostics_route_earpiece"))
        assertEquals("扬声器", simplifiedValue("diagnostics_route_speaker"))
        assertNotEquals(simplifiedValue("diagnostics_route_earpiece"), simplifiedValue("diagnostics_route_speaker"))
        assertEquals("迷你", simplifiedValue("floating"))
        assertEquals("开启", simplifiedValue("diagnostics_on"))
        assertEquals("使用紧", simplifiedValue("state_active"))
        assertNotEquals(simplifiedValue("diagnostics_on"), simplifiedValue("state_active"))
        assertTrue(simplifiedValue("diagnostics_error_audio_preparation").contains("通信音频"))
        assertTrue(simplifiedValue("settings_about_body").contains("内置听筒"))
        assertTrue(simplifiedValue("diagnostics_error_request_rejected").contains("听筒"))
        assertFalse(simplifiedValue("diagnostics_error_request_rejected").contains("扬声器"))
        assertEquals(Character.DIRECTIONALITY_LEFT_TO_RIGHT, Character.getDirectionality(simplifiedValue("settings").first()))
        assertNotEquals(resourceValue("settings"), simplifiedValue("settings"))
        assertNotEquals(resourceValue("diagnostics_route_speaker"), simplifiedValue("diagnostics_route_speaker"))
    }

    @Test
    fun simplifiedCantonesePrivacyRetainsSourceStructure() {
        val privacy = simplifiedValue("settings_privacy_policy_body")
        assertEquals(5, privacy.split("\\n\\n").size)
        assertTrue(privacy.split("\\n\\n").all(String::isNotBlank))
    }

    private fun resourceValue(key: String): String =
        Regex("<string name=\"$key\">([^<]*)</string>").find(strings)?.groupValues?.get(1).orEmpty()

    private fun simplifiedValue(key: String): String = values(projectFile(simplifiedResourcePath)).getValue(key)

    private fun values(file: File): Map<String, String> {
        val strings = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file).getElementsByTagName("string")
        return (0 until strings.length).associate { index ->
            val element = strings.item(index)
            element.attributes.getNamedItem("name").nodeValue to element.textContent
        }
    }

    private fun placeholders(text: String): List<String> = Regex("%\\d+\\$[a-zA-Z]").findAll(text).map { it.value }.toList()

    private fun projectFile(relativePath: String) = File(projectRoot, relativePath)
}
