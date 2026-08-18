package app.privateaudio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class Layer4SettingsContractTest {
    @Test
    fun settingsNavigationUsesOneDismissibleModalWithChildBack() {
        assertTrue(screenSource.contains("onSettingsClick = { settingsVisible = true }"))
        assertTrue(settingsSource.contains("Dialog("))
        assertTrue(settingsSource.contains("onDismissRequest = onDismiss"))
        assertTrue(settingsSource.contains("BackHandler(enabled = page != SettingsPage.ROOT)"))
        assertTrue(settingsSource.contains("page = SettingsPage.ROOT"))
        assertFalse(settingsSource.contains("Activity"))
        assertFalse(settingsSource.contains("setContent"))
    }

    @Test
    fun settingsKeepsReferenceSpecificVisualStructureIsolated() {
        assertTrue(settingsSource.contains("const val widthFraction = 0.88f"))
        assertTrue(settingsSource.contains("val verticalOffset = 104.dp"))
        assertTrue(settingsSource.contains("private fun SettingsDivider()"))
        assertTrue(settingsSource.contains("private fun CopyIcon()"))
        assertTrue(settingsSource.contains("SettingsScrim"))
        assertTrue(settingsSource.contains("RoundedCornerShape(SettingsLayout.cornerRadius)"))
    }

    @Test
    fun languageOptionsScrollWithinTheSheetAndSelectionReturnsToSettings() {
        assertTrue(settingsSource.contains("LazyColumn("))
        assertTrue(settingsSource.contains("WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical)"))
        assertTrue(settingsSource.contains("max = maxHeight - SettingsLayout.verticalOffset * 2"))
        assertTrue(
            settingsSource.contains(
                "AppLanguagePreferences.select(context, it)\n                            page = SettingsPage.ROOT",
            ),
        )
    }

    @Test
    fun diagnosticCopyUsesExistingServiceReportAndAndroidClipboard() {
        assertTrue(mainSource.contains("activeService.diagnosticReport()"))
        assertTrue(mainSource.contains("getSystemService(ClipboardManager::class.java)"))
        assertTrue(mainSource.contains("ClipData.newPlainText"))
        assertTrue(serviceSource.method("fun diagnosticReport(): String").contains("return observer.report()"))
        assertEquals(1, observerSource.occurrences("internal fun buildDiagnosticReport("))
        assertEquals(0, settingsSource.occurrences("buildDiagnosticReport("))
    }

    @Test
    fun versionIsBuildMetadataAndSettingsAddNoProtectedBehavior() {
        assertTrue(mainSource.contains("versionName = BuildConfig.VERSION_NAME"))
        assertEquals(1, productionSources.sumOf { it.readText().occurrences("setCommunicationDevice(") })
        assertFalse(settingsSource.contains("AudioManager"))
        assertFalse(settingsSource.contains("PrivateAudioState"))
        assertFalse(settingsSource.contains("AudioDiagnosticObserver"))
        assertFalse(settingsSource.contains("MODE_IN_COMMUNICATION"))
    }

    private fun String.method(signature: String): String =
        substring(indexOf(signature)).substringBefore("\n    }")

    private fun String.occurrences(needle: String): Int = windowed(needle.length).count { it == needle }

    private companion object {
        val projectRoot = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
            .first { File(it, "app/src/main").isDirectory }
        fun projectFile(relativePath: String) = File(projectRoot, relativePath)
        val mainSource = projectFile("app/src/main/java/app/privateaudio/MainActivity.kt").readText()
        val screenSource = projectFile("app/src/main/java/app/privateaudio/ui/PrivateAudioScreen.kt").readText()
        val settingsSource = projectFile("app/src/main/java/app/privateaudio/ui/SettingsSheet.kt").readText()
        val serviceSource = projectFile("app/src/main/java/app/privateaudio/PrivateAudioService.kt").readText()
        val observerSource = projectFile("app/src/main/java/app/privateaudio/diagnostic/AudioDiagnosticObserver.kt").readText()
        val productionSources = File(projectRoot, "app/src/main/java")
            .walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()
    }
}
