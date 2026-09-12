package app.privateaudio

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Test

class LauncherIconContractTest {
    @Test
    fun manifestRetainsStableLauncherResource() {
        val manifest = xml("app/src/main/AndroidManifest.xml")

        assertEquals("@drawable/ic_launcher", manifest.documentElement
            .getElementsByTagName("application").item(0).attributes
            .getNamedItem("android:icon").nodeValue)
    }

    @Test
    fun launcherWrapperReferencesOwnerSuppliedRaster() {
        val launcher = xml("app/src/main/res/drawable/ic_launcher.xml").documentElement

        assertEquals("bitmap", launcher.tagName)
        assertEquals("@drawable/puzru_launcher", launcher.getAttribute("android:src"))
        assertEquals(0, launcher.getElementsByTagName("*").length)
    }

    private fun xml(relativePath: String) = DocumentBuilderFactory.newInstance()
        .newDocumentBuilder()
        .parse(File(projectRoot, relativePath))

    private companion object {
        val projectRoot = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
            .first { File(it, "app/src/main").isDirectory }
    }
}
