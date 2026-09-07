package app.privateaudio

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files
import java.time.LocalDateTime

class DiagnosticEmailContractTest {
    private val root = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
        .first { File(it, "app/src/main").isDirectory }
    private val main = File(root, "app/src/main/java/app/privateaudio/MainActivity.kt").readText()
    private val manifest = File(root, "app/src/main/AndroidManifest.xml").readText()
    private val paths = File(root, "app/src/main/res/xml/diagnostic_share_paths.xml").readText()

    @Test
    fun attachmentFilenameIsStableTimestampedPlainText() {
        assertEquals(
            "puzru-diagnostic-2026-09-07_08-09-10.txt",
            diagnosticFilename(LocalDateTime.of(2026, 9, 7, 8, 9, 10)),
        )
    }

    @Test
    fun shareWriterPreservesFrozenUtf8AndBoundsRetention() {
        val cache = Files.createTempDirectory("puzru-cache").toFile()
        val shareDirectory = File(cache, "diagnostic-share").apply { mkdirs() }
        File(shareDirectory, "stale.txt").writeText("old")
        val report = "format=3\nPuzru — zażółć 日本語\n"

        val result = writeDiagnosticShareFile(cache, "current.txt", report)

        assertTrue(result is DiagnosticShareFileResult.Success)
        val file = (result as DiagnosticShareFileResult.Success).file
        assertEquals(shareDirectory.canonicalFile, file.parentFile.canonicalFile)
        assertEquals(listOf("current.txt"), shareDirectory.listFiles().orEmpty().map { it.name })
        assertArrayEquals(report.toByteArray(Charsets.UTF_8), file.readBytes())
        cache.deleteRecursively()
    }

    @Test
    fun sendCapturesOnceAndBuildsOneReadOnlyContentAttachment() {
        val method = main.method("private fun sendDiagnosticReport()")
        val capture = "val frozenReport = connectedService.diagnosticReport()"
        assertEquals(1, method.occurrences("connectedService.diagnosticReport()"))
        assertTrue(method.contains(capture))
        assertTrue(method.contains("writeDiagnosticShareFile(cacheDir, diagnosticFilename(), frozenReport)"))
        assertFalse(method.substringAfter(capture).contains("diagnosticReport()"))
        assertTrue(method.contains("Intent(Intent.ACTION_SEND)"))
        assertFalse(method.contains("ACTION_SENDTO"))
        assertFalse(method.contains("ACTION_SEND_MULTIPLE"))
        assertEquals(1, method.occurrences("Intent.EXTRA_STREAM"))
        assertTrue(method.contains("R.string.privacy_support_email"))
        assertTrue(method.contains("Intent.EXTRA_SUBJECT"))
        assertTrue(method.contains("Intent.EXTRA_TEXT"))
        assertFalse(method.contains("putExtra(Intent.EXTRA_TEXT, frozenReport)"))
        assertTrue(method.contains("clipData = ClipData.newUri"))
        assertTrue(method.contains("FileProvider.getUriForFile"))
        assertTrue(method.contains("Intent.FLAG_GRANT_READ_URI_PERMISSION"))
        assertFalse(method.contains("FLAG_GRANT_WRITE_URI_PERMISSION"))
        assertFalse(method.contains("FLAG_GRANT_PERSISTABLE_URI_PERMISSION"))
        assertFalse(method.contains("ACTION_CREATE_DOCUMENT"))
        assertFalse(method.contains("resolveActivity("))
        assertTrue(method.contains("ActivityNotFoundException"))
    }

    @Test
    fun providerExposesOnlyDedicatedCachePathWithoutNewPermissions() {
        assertTrue(manifest.contains("android:name=\"androidx.core.content.FileProvider\""))
        assertTrue(manifest.contains("android:authorities=\"\${applicationId}.fileprovider\""))
        assertTrue(manifest.contains("android:exported=\"false\""))
        assertTrue(manifest.contains("android:grantUriPermissions=\"true\""))
        assertTrue(paths.contains("<cache-path"))
        assertTrue(paths.contains("path=\"diagnostic-share/\""))
        assertFalse(paths.contains("path=\".\""))
        listOf("INTERNET", "READ_EXTERNAL_STORAGE", "WRITE_EXTERNAL_STORAGE", "RECORD_AUDIO", "QUERY_ALL_PACKAGES")
            .forEach { assertFalse(it, manifest.contains(it)) }
    }

    private fun String.method(signature: String): String {
        val start = indexOf(signature)
        require(start >= 0) { "Missing $signature" }
        val bodyStart = indexOf('{', start)
        var depth = 0
        for (index in bodyStart until length) {
            if (this[index] == '{') depth++
            if (this[index] == '}') depth--
            if (depth == 0) return substring(start, index + 1)
        }
        error("Unclosed $signature")
    }

    private fun String.occurrences(value: String): Int = windowed(value.length).count { it == value }
}
