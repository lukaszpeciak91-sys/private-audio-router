package app.privateaudio

import java.io.File
import java.nio.ByteBuffer
import javax.imageio.ImageIO
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LauncherIconContractTest {
    @Test
    fun manifestUsesAdaptiveLauncherEntryPointForStandardAndRoundIcons() {
        val application = xml("app/src/main/AndroidManifest.xml").documentElement
            .getElementsByTagName("application").item(0).attributes

        assertEquals("@mipmap/ic_launcher", application.getNamedItem("android:icon").nodeValue)
        assertEquals("@mipmap/ic_launcher", application.getNamedItem("android:roundIcon").nodeValue)
        assertFalse(source("app/src/main/AndroidManifest.xml").contains("@drawable/ic_launcher"))
    }

    @Test
    fun baseLauncherFallbackReferencesOwnerSuppliedLegacyMaster() {
        val launcher = xml("app/src/main/res/mipmap/ic_launcher.xml").documentElement

        assertEquals("bitmap", launcher.tagName)
        assertEquals("@drawable/puzru_legacy_master", launcher.getAttribute("android:src"))
        assertEquals(0, launcher.getElementsByTagName("*").length)
    }

    @Test
    fun api26LauncherDefinesAdaptiveForegroundBackgroundAndMonochromeLayers() {
        val launcher = xml("app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml").documentElement

        assertEquals("adaptive-icon", launcher.tagName)
        assertEquals("@color/launcher_background", launcher.singleDrawable("background"))
        assertEquals("@drawable/puzru_adaptive_foreground", launcher.singleDrawable("foreground"))
        assertEquals("@drawable/puzru_adaptive_monochrome", launcher.singleDrawable("monochrome"))
    }

    @Test
    fun launcherBackgroundIsExactApprovedColor() {
        val colors = xml("app/src/main/res/values/colors.xml")
        val launcherBackground = colors.getElementsByTagName("color")
            .let { nodes -> (0 until nodes.length).map(nodes::item) }
            .single { it.attributes.getNamedItem("name").nodeValue == "launcher_background" }

        assertEquals("#11162F", launcherBackground.textContent.trim())
    }

    @Test
    fun obsoleteLauncherResourcesAreNotReferencedOrRetained() {
        val inspectedFiles = listOf(
            "app/src/main/AndroidManifest.xml",
            "app/src/main/java/app/privateaudio/PrivateAudioService.kt",
            "app/src/main/res/mipmap/ic_launcher.xml",
            "app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml",
        )
        assertFalse(inspectedFiles.any { source(it).contains("puzru_launcher") })
        assertFalse(File(projectRoot, "app/src/main/res/drawable/ic_launcher.xml").exists())
        assertFalse(File(projectRoot, "app/src/main/res/drawable-nodpi/puzru_launcher.png").exists())
    }

    @Test
    fun foregroundServiceUsesTransparentMonochromeNotificationIcon() {
        val service = source("app/src/main/java/app/privateaudio/PrivateAudioService.kt")

        assertTrue(service.contains(".setSmallIcon(R.drawable.puzru_adaptive_monochrome)"))
        assertFalse(service.contains(".setSmallIcon(R.drawable.ic_launcher)"))
    }

    @Test
    fun ownerSuppliedLauncherPngsMeetPixelContract() {
        val foreground = png("app/src/main/res/drawable-nodpi/puzru_adaptive_foreground.png", requireRgba = true)
        val monochrome = png("app/src/main/res/drawable-nodpi/puzru_adaptive_monochrome.png", requireRgba = true)
        val legacy = png("app/src/main/res/drawable-nodpi/puzru_legacy_master.png", requireRgba = false)

        assertTrue("Adaptive foreground must contain transparent pixels", foreground.alpha.any { it < 255 })
        assertTrue("Adaptive monochrome must contain transparent pixels", monochrome.alpha.any { it < 255 })
        assertTrue("Legacy master must be fully opaque", legacy.alpha.all { it == 255 })
        assertArrayEquals(
            "Adaptive foreground and monochrome must have identical alpha masks",
            foreground.alpha,
            monochrome.alpha,
        )
    }

    private fun png(relativePath: String, requireRgba: Boolean): PngContract {
        val file = File(projectRoot, relativePath)
        assertTrue("Owner-supplied PNG is missing: $relativePath", file.isFile)
        val bytes = file.readBytes()
        assertTrue("Invalid PNG signature: $relativePath", bytes.size >= PNG_SIGNATURE.size)
        assertArrayEquals("Invalid PNG signature: $relativePath", PNG_SIGNATURE, bytes.copyOfRange(0, 8))
        assertEquals("IHDR must be the first PNG chunk: $relativePath", "IHDR", bytes.copyOfRange(12, 16).toString(Charsets.US_ASCII))
        assertEquals("PNG width must be 1024: $relativePath", 1024, ByteBuffer.wrap(bytes, 16, 4).int)
        assertEquals("PNG height must be 1024: $relativePath", 1024, ByteBuffer.wrap(bytes, 20, 4).int)
        if (requireRgba) {
            assertEquals("PNG must use RGBA color type: $relativePath", 6, bytes[25].toInt() and 0xff)
        }

        val image = ImageIO.read(file)
        assertNotNull("PNG could not be decoded: $relativePath", image)
        assertEquals(1024, image.width)
        assertEquals(1024, image.height)
        val alpha = IntArray(image.width * image.height)
        var index = 0
        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                alpha[index++] = image.getRGB(x, y) ushr 24
            }
        }
        return PngContract(alpha)
    }

    private fun org.w3c.dom.Element.singleDrawable(tagName: String): String {
        val nodes = getElementsByTagName(tagName)
        assertEquals("Expected exactly one <$tagName>", 1, nodes.length)
        return nodes.item(0).attributes.getNamedItem("android:drawable").nodeValue
    }

    private fun source(relativePath: String) = File(projectRoot, relativePath).readText()

    private fun xml(relativePath: String) = DocumentBuilderFactory.newInstance()
        .newDocumentBuilder()
        .parse(File(projectRoot, relativePath))

    private data class PngContract(val alpha: IntArray)

    private companion object {
        val projectRoot = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
            .first { File(it, "app/src/main").isDirectory }
        val PNG_SIGNATURE = byteArrayOf(
            0x89.toByte(), 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a,
        )
    }
}
