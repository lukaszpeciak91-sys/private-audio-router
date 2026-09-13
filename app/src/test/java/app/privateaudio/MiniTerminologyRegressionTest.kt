package app.privateaudio

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class MiniTerminologyRegressionTest {
    @Test
    fun wolofAndMaoriKeepReviewedMiniProductLabel() {
        val wolof = projectFile("app/src/main/res/values-wo/strings.xml").readText()
        val maori = projectFile("app/src/main/res/values-mi/strings.xml").readText()

        assertTrue(wolof.contains("name=\"floating\">Mini</string>"))
        assertFalse(wolof.contains("name=\"floating\">Bu ndaw</string>"))
        assertTrue(wolof.contains("permission_overlay_title\">May Mini ci kaw yeneen aplikasioŋ</string>"))

        assertTrue(maori.contains("name=\"floating\">Mini</string>"))
        assertFalse(maori.contains("name=\"floating\">Iti</string>"))
        assertTrue(maori.contains("permission_overlay_title\">Whakaaetia a Mini ki runga i ētahi atu taupānga</string>"))
    }

    private fun projectFile(relativePath: String): File = File(projectRoot, relativePath)

    private val projectRoot: File = generateSequence(File(System.getProperty("user.dir"))) { it.parentFile }
        .first { File(it, "settings.gradle.kts").isFile }
}
