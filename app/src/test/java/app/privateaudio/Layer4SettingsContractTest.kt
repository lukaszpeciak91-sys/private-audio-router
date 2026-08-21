package app.privateaudio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

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
        assertTrue(settingsSource.contains("tag = \"settings_diagnostics\""))
        assertFalse(settingsSource.contains("private fun SaveIcon()"))
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
    fun privacyPolicyUsesTheSingleSettingsModalAndAuthoritativeEnglishClaims() {
        assertTrue(settingsSource.contains("tag = \"settings_privacy_policy\""))
        assertTrue(settingsSource.contains("SettingsPage.PRIVACY_POLICY -> PrivacyPolicyPage("))
        val privacyPolicyPage = settingsSource.method("private fun PrivacyPolicyPage(onBack: () -> Unit)")
        assertTrue(privacyPolicyPage.contains("testTag(\"settings_child_back\")"))
        assertTrue(privacyPolicyPage.contains("BackChevron()"))
        assertTrue(settingsSource.contains("BackHandler(enabled = page != SettingsPage.ROOT) { page = SettingsPage.ROOT }"))
        assertTrue(settingsSource.contains("testTag(\"privacy_policy_body\")"))
        assertEquals(1, settingsSource.occurrences("Dialog("))

        val strings = projectFile("app/src/main/res/values/strings.xml").readText()
        listOf(
            "does not collect, record, or transmit your conversations or audio content",
            "does not require an account or sign-in",
            "does not request microphone access",
            "does not use analytics, advertising, or crash-reporting services",
            "does not request Android’s Internet permission",
            "does not send data to a server",
            "observes only the technical state and metadata",
            "does not access the content of your conversations",
            "saved only when you choose to save it",
            "does not contain conversation or audio content",
            "Android app-data backup is disabled",
        ).forEach { claim -> assertTrue(claim, strings.contains(claim)) }
    }

    @Test
    fun diagnosticSaveUsesCreateDocumentAndUtf8ContentResolver() {
        val launchMethod = mainSource.method("private fun launchDiagnosticDocumentPicker()")
        val saveMethod = mainSource.method("private fun saveDiagnosticReport(destination: Uri)")
        assertTrue(launchMethod.contains("connectedService.diagnosticReport()"))
        assertTrue(launchMethod.indexOf("connectedService.diagnosticReport()") < launchMethod.indexOf("diagnosticDocumentLauncher.launch("))
        assertFalse(saveMethod.contains("service?.diagnosticReport()"))
        assertTrue(saveMethod.contains("pendingDiagnosticReport"))
        assertTrue(mainSource.contains("Intent.ACTION_CREATE_DOCUMENT"))
        assertTrue(mainSource.contains("Intent.CATEGORY_OPENABLE"))
        assertTrue(mainSource.contains("setType(\"text/plain\")"))
        assertTrue(mainSource.contains("contentResolver.openOutputStream"))
        assertTrue(mainSource.contains("Charsets.UTF_8"))
        assertFalse(mainSource.contains("ClipboardManager"))
        assertTrue(serviceSource.method("fun diagnosticReport(): String").contains("observer.report(supportSummary)"))
        assertEquals(1, observerSource.occurrences("internal fun buildDiagnosticReport("))
        assertEquals(0, settingsSource.occurrences("buildDiagnosticReport("))
        assertFalse(settingsSource.contains("settings_save_diagnostic"))
    }

    @Test
    fun diagnosticWritePreservesUtf8WithoutAService() {
        val output = ByteArrayOutputStream()
        val report = "Private Audio — zażółć 日本語"

        assertEquals(DiagnosticWriteResult.Success, writeDiagnosticReport(report) { output })
        assertEquals(report, output.toString(Charsets.UTF_8.name()))
    }

    @Test
    fun diagnosticWriteReportsNullStreamAndExceptions() {
        assertEquals(
            DiagnosticWriteResult.Failure("openOutputStream returned null"),
            writeDiagnosticReport("report") { null },
        )
        assertEquals(
            DiagnosticWriteResult.Failure("IOException: provider rejected write"),
            writeDiagnosticReport("report") { throw IOException("provider rejected write") },
        )
    }

    @Test
    fun diagnosticPickerCancellationAndEveryResultClearPendingReport() {
        assertTrue(mainSource.contains("if (result.resultCode != RESULT_OK)"))
        assertTrue(mainSource.contains("pendingDiagnosticReport = null\n            return@registerForActivityResult"))
        assertTrue(mainSource.method("private fun saveDiagnosticReport(destination: Uri)").contains("pendingDiagnosticReport = null"))
        assertTrue(mainSource.contains("Document Uri unavailable"))
        assertTrue(mainSource.contains("No connected service when Save was tapped"))
    }

    @Test
    fun saveWorkflowAddsNoBroadStoragePermission() {
        val manifest = projectFile("app/src/main/AndroidManifest.xml").readText()
        assertFalse(manifest.contains("READ_EXTERNAL_STORAGE"))
        assertFalse(manifest.contains("WRITE_EXTERNAL_STORAGE"))
        assertFalse(manifest.contains("MANAGE_EXTERNAL_STORAGE"))
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

    @Test
    fun diagnosticFilenameIsTimestampedPlainText() {
        val filename = diagnosticFilename(java.time.LocalDateTime.of(2026, 8, 19, 7, 5, 9))
        assertEquals("private-audio-diagnostic-2026-08-19_07-05-09.txt", filename)
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
