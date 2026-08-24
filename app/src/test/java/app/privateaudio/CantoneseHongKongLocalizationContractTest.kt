package app.privateaudio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.Locale

class CantoneseHongKongLocalizationContractTest {
    private val projectRoot = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
        .first { File(it, "app/src/main").isDirectory }
    private val resourcePath = "app/src/main/res/values-b+yue+Hant+HK/strings.xml"
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

    private fun resourceValue(key: String): String =
        Regex("<string name=\"$key\">([^<]*)</string>").find(strings)?.groupValues?.get(1).orEmpty()

    private fun projectFile(relativePath: String) = File(projectRoot, relativePath)
}
