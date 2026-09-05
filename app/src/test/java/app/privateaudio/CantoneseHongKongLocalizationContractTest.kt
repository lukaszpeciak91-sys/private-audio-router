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
        listOf(
            "唔需要帳戶或者登入", "唔會要求麥克風權限", "唔會擷取或者錄製麥克風音訊",
            "唔會記錄或者儲存你嘅對話或者其音訊內容", "錄音工作階段中繼資料係技術性音訊系統資料",
            "唔會擷取與該等工作階段相關聯嘅麥克風音訊", "診斷資料會喺你嘅裝置上產生同處理", "報告唔會自動儲存或者傳送",
            "只有當你明確選擇「儲存診斷報告」", "目前版本唔會要求 Android 嘅 Internet 權限", "冇 Private Audio 後端或者網絡傳輸路徑",
            "唔包含分析、廣告或者當機回報服務或 SDK", "唔會將診斷報告傳送畀開發者或者 Private Audio 伺服器",
            "Android 雲端備份同裝置對裝置傳輸之外",
        ).forEach { guard -> assertTrue("Missing privacy guard: $guard", privacy.contains(guard)) }
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
        listOf("唔使", "佢", "嘅", "喺", "使用紧", "先会", "唔系 Private Audio")
            .forEach { assertTrue("Missing Simplified Written Cantonese marker: $it", privacy.contains(it)) }
        listOf(
            "唔会要求麦克风权限", "唔会采集或者录制麦克风音频", "唔会录制或者存储你嘅对话或者当中嘅音频内容",
            "录音会话元数据系音频系统嘅技术信息", "唔会采集同呢啲会话相关嘅麦克风音频", "设备上生成同处理",
            "报告唔会自动保存或者发送", "明确选择“保存诊断报告”", "唔会要求 Android 嘅互联网权限",
            "冇 Private Audio 后端或者网络传输路径", "唔包含分析、广告或者崩溃报告服务或 SDK",
            "唔会将诊断报告发送畀开发者或者 Private Audio 服务器", "唔会包含喺 Android 云备份同设备到设备传输入面",
        )
            .forEach { assertTrue("Missing privacy concept: $it", privacy.contains(it)) }
        assertFalse(privacy.contains("您的"))
        assertNotEquals(resourceValue("settings_privacy_policy_body"), privacy)
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
