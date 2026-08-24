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
        assertTrue(privacy.contains("唔會收集、錄製或傳送"))
        assertTrue(privacy.contains("唔會要求咪高風存取權限"))
        assertTrue(privacy.contains("App 意外結束回報服務"))
        assertTrue(privacy.contains("Android 嘅互聯網權限"))
        assertTrue(privacy.contains("Android 音訊系統技術狀態同元數據"))
        assertTrue(privacy.contains("Private Audio 唔會存取你嘅對話內容"))
        assertFalse(privacy.split("\\n\\n")[2].contains("本機"))
        assertTrue(privacy.contains("只有你選擇儲存診斷報告時"))
        assertTrue(privacy.contains("Android App 資料備份已停用"))
        assertTrue(resourceValue("settings_about_body").contains("內置聽筒"))
        assertEquals("聽筒音訊導向要求未獲接受。", resourceValue("diagnostics_error_request_rejected"))
        assertFalse(resourceValue("diagnostics_error_request_rejected").contains("系統"))

        assertFalse(strings.contains("settings_assistant_early_route"))
        assertFalse(projectFile("app/src/main/res/values-b+yue+Hant+HK/mini_state_strings.xml").exists())
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
    fun simplifiedCantonesePrivacyClaimsRetainSourceStructureAndScope() {
        val privacy = simplifiedValue("settings_privacy_policy_body")
        val paragraphs = privacy.split("\\n\\n")
        assertEquals(5, paragraphs.size)
        listOf("唔会收集、录制或传输", "唔会请求麦克风访问权限", "崩溃报告服务", "互联网权限", "向服务器发送数据")
            .forEach { assertTrue("Missing privacy action: $it", privacy.contains(it)) }
        assertTrue(paragraphs[2].contains("音频系统技术状态同元数据"))
        assertTrue(paragraphs[2].contains("唔会访问你嘅对话内容"))
        assertTrue(paragraphs[3].contains("喺本地产生同处理"))
        assertTrue(paragraphs[3].contains("只有你选择保存诊断报告时先会保存报告"))
        assertTrue(paragraphs[3].contains("唔会包含对话或音频内容"))
        assertEquals("Android 应用数据备份已停用。", paragraphs[4])
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
