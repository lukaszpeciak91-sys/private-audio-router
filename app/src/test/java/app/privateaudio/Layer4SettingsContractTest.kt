package app.privateaudio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import javax.xml.parsers.DocumentBuilderFactory

class Layer4SettingsContractTest {
    @Test
    fun appDataBackupAndDeviceTransferExcludeEveryAppOwnedDomain() {
        val manifest = projectFile("app/src/main/AndroidManifest.xml").readText()
        assertTrue(manifest.contains("android:allowBackup=\"false\""))
        assertTrue(manifest.contains("android:dataExtractionRules=\"@xml/data_extraction_rules\""))

        val rules = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(projectFile("app/src/main/res/xml/data_extraction_rules.xml"))
        val expectedDomains = setOf(
            "root", "file", "database", "sharedpref", "external",
            "device_root", "device_file", "device_database", "device_sharedpref",
        )
        listOf("cloud-backup", "device-transfer").forEach { destination ->
            val exclusions = rules.getElementsByTagName(destination).item(0).childNodes
            val domains = (0 until exclusions.length).mapNotNull { index ->
                exclusions.item(index).attributes?.getNamedItem("domain")?.nodeValue
            }.toSet()
            assertEquals(destination, expectedDomains, domains)
        }
    }

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
        val settingsSheet = settingsSource.kotlinDeclaration("fun SettingsSheet(")
        assertTrue(settingsSheet.contains("SettingsPage.LANGUAGE -> LanguagePage("))
        assertTrue(settingsSheet.contains("val portraitInsetPage = page == SettingsPage.LANGUAGE"))
        assertTrue(settingsSheet.contains("WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical)"))
        val portraitHeightBranch = settingsSheet.indexOf("else if (portraitInsetPage)")
        val insetHeight = settingsSheet.indexOf("maxHeight - SettingsLayout.verticalOffset * 2", portraitHeightBranch)
        assertTrue("Portrait inset height branch must be present", portraitHeightBranch >= 0)
        assertTrue("Portrait inset height must subtract both offsets", insetHeight > portraitHeightBranch)
        assertTrue(settingsSource.kotlinDeclaration("private fun LanguagePage(").contains("LazyColumn("))
        val select = settingsSheet.indexOf("AppLanguagePreferences.select(context, it)")
        val returnToRoot = settingsSheet.indexOf("page = SettingsPage.ROOT", select)
        assertTrue("Language selection must be present", select >= 0)
        assertTrue("Language selection must return to Settings root", returnToRoot > select)
    }

    @Test
    fun languageOptionsObserveComposeConfigurationChanges() {
        val settingsSheet = settingsSource.kotlinDeclaration("fun SettingsSheet(")
        assertTrue(settingsSheet.contains("val configuration = LocalConfiguration.current"))
        assertTrue(settingsSheet.contains("remember(configuration)"))
        assertFalse(settingsSheet.contains("remember(context.resources.configuration)"))
    }

    @Test
    fun notificationRefreshPermissionIsOptionalAndDoesNotGuardForegroundEntry() {
        val manifest = projectFile("app/src/main/AndroidManifest.xml").readText()
        val configurationRefresh = serviceSource.kotlinDeclaration("override fun onConfigurationChanged(")
        val foregroundEntry = serviceSource.kotlinDeclaration("fun enterForeground()")

        assertTrue(manifest.contains("android.permission.POST_NOTIFICATIONS"))
        assertTrue(configurationRefresh.contains("Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU"))
        assertTrue(configurationRefresh.contains("Manifest.permission.POST_NOTIFICATIONS"))
        assertTrue(configurationRefresh.contains("PackageManager.PERMISSION_GRANTED"))
        assertTrue(foregroundEntry.contains("startForeground("))
        assertFalse(foregroundEntry.contains("POST_NOTIFICATIONS"))
        assertFalse(mainSource.contains("ActivityResultContracts.RequestPermission"))
    }

    @Test
    fun privacySummaryUsesTheSingleSettingsModalAndKeepsCanonicalPolicyAvailableOnline() {
        assertTrue(settingsSource.contains("tag = \"settings_privacy_policy\""))
        assertTrue(settingsSource.contains("SettingsPage.PRIVACY_POLICY -> PrivacyPolicyPage("))
        val privacyPolicyPage = settingsSource.method(
            "private fun PrivacyPolicyPage(",
        )
        assertTrue(privacyPolicyPage.contains("testTag(\"settings_child_back\")"))
        assertTrue(privacyPolicyPage.contains("BackChevron()"))
        assertTrue(settingsSource.contains("BackHandler(enabled = page != SettingsPage.ROOT) { page = SettingsPage.ROOT }"))
        assertTrue(settingsSource.contains("testTag(\"privacy_policy_body\")"))
        assertTrue(privacyPolicyPage.contains("R.string.settings_privacy_summary_body"))
        assertFalse(privacyPolicyPage.contains("R.string.settings_privacy_policy_body"))
        assertTrue(privacyPolicyPage.contains("R.string.settings_privacy_policy_online"))
        assertTrue(privacyPolicyPage.contains("R.string.settings_privacy_policy_language"))
        assertTrue(privacyPolicyPage.contains("tag = \"privacy_policy_online\""))
        assertTrue(privacyPolicyPage.contains("onClick = onPrivacyPolicyOnlineClick"))
        assertEquals(1, settingsSource.occurrences("Dialog("))

        assertTrue(settingsSource.contains("ContactBlock(onContactClick)"))

        val summary = resourceValue(projectFile("app/src/main/res/values/strings.xml"), "settings_privacy_summary_body")
        assertEquals(3, summary.split("\\n\\n").size)
        listOf(
            "does not require an account or microphone permission",
            "does not capture or record microphone audio or conversation content",
            "technical audio-system information for routing and diagnostics",
            "processed locally on the device",
            "not automatically saved or transmitted",
            "requires an explicit user action",
        ).forEach { claim -> assertTrue(claim, summary.contains(claim)) }
        val strings = projectFile("app/src/main/res/values/strings.xml").readText()
        listOf(
            "does not require an account or sign-in",
            "does not request microphone permission",
            "does not capture or record microphone audio",
            "record or store your conversations or their audio content",
            "playback and recording-session metadata",
            "Recording-session metadata is technical audio-system information",
            "does not capture the microphone audio associated with those sessions",
            "security patch, and build identifiers",
            "bounded histories of routing attempts, results, timing",
            "generated and processed on your device",
            "Reports are not automatically saved or transmitted",
            "select a destination through Android’s document picker",
            "temporary text attachment in Puzru’s app-private cache",
            "through Android’s chooser, to the external app you select",
            "does not request Android’s Internet permission",
            "has no Puzru backend or network transmission path",
            "does not include analytics, advertising, or crash-reporting services or SDKs",
            "Puzru itself does not automatically send diagnostic reports",
            "Napahu Studios privacy and support address may be prefilled",
            "actual transmission is performed by the selected external app or service",
            "governed by its privacy practices and controls, not Puzru’s",
            "removes prior regular report files in its diagnostic-share cache where deletion succeeds",
            "no fixed deletion time for the current attachment is guaranteed",
            "excluded from Android cloud backup and device-to-device transfer",
        ).forEach { claim -> assertTrue(claim, strings.contains(claim)) }
        listOf(
            "cannot access the microphone",
            "never transmits",
            "completely anonymous",
            "contain no identifying information",
        ).forEach { overclaim -> assertFalse(overclaim, strings.contains(overclaim, ignoreCase = true)) }
    }

    @Test
    fun publicPrivacyPolicyMatchesCanonicalEnglishBodyAndIdentity() {
        val canonical = resourceValue(
            projectFile("app/src/main/res/values/strings.xml"),
            "settings_privacy_policy_body",
        ).split("\\n\\n")
        val publicPage = projectFile("public/index.html").readText()
        val publicSection = Regex(
            "<section aria-label=\"Privacy Policy\">(.*?)</section>",
            RegexOption.DOT_MATCHES_ALL,
        ).find(publicPage)?.groupValues?.get(1).orEmpty()
        val publicParagraphs = Regex("<p>(.*?)</p>", RegexOption.DOT_MATCHES_ALL)
            .findAll(publicSection)
            .map { it.groupValues[1].trim() }
            .toList()

        assertEquals(canonical, publicParagraphs)
        assertTrue(publicPage.contains("<title>Puzru Privacy Policy</title>"))
        assertTrue(publicPage.contains("<h1>Puzru Privacy Policy</h1>"))
        assertTrue(publicPage.contains("Developer and publisher: Napahu Studios"))
        assertTrue(publicPage.contains("Last updated: September 8, 2026"))
        assertTrue(publicPage.contains("mailto:napahustudios@gmail.com"))
    }

    @Test
    fun privacyPolicyOnlineActionUsesSafeExternalBrowserFlowAndIsNotInAbout() {
        val privacyPolicyPage = settingsSource.kotlinDeclaration("private fun PrivacyPolicyPage(")
        val aboutPage = settingsSource.kotlinDeclaration("private fun AboutPage(")
        val browserMethod = mainSource.kotlinDeclaration("private fun openPrivacyPolicyOnline()")
        val manifest = projectFile("app/src/main/AndroidManifest.xml").readText()

        assertTrue(privacyPolicyPage.contains("settings_privacy_policy_online"))
        assertFalse(aboutPage.contains("settings_privacy_policy_online"))
        assertTrue(mainSource.contains("onPrivacyPolicyOnlineClick = { openPrivacyPolicyOnline() }"))
        assertTrue(browserMethod.contains("Intent.ACTION_VIEW"))
        assertTrue(browserMethod.contains("Uri.parse(PRIVACY_POLICY_URL)"))
        assertTrue(browserMethod.contains("catch (_: android.content.ActivityNotFoundException)"))
        assertTrue(mainSource.contains(
            "https://lukaszpeciak91-sys.github.io/private-audio-router/",
        ))
        assertFalse(manifest.contains("android.permission.INTERNET"))
    }

    @Test
    fun privacyPolicyOnlineActionIsLocalizedAcrossTheProductLocaleInventory() {
        val resourceRoot = projectFile("app/src/main/res")
        val defaultValue = resourceValue(
            File(resourceRoot, "values/strings.xml"),
            "settings_privacy_policy_online",
        )
        val localizedFiles = resourceRoot.listFiles().orEmpty()
            .filter { it.isDirectory && it.name.startsWith("values-") }
            .map { File(it, "strings.xml") }
            .filter(File::isFile)

        assertEquals("View Privacy Policy online", defaultValue)
        localizedFiles.forEach { stringsFile ->
            val localizedValue = resourceValue(stringsFile, "settings_privacy_policy_online")
            assertTrue("${stringsFile.parentFile.name}: action is missing", localizedValue.isNotBlank())
            assertFalse("${stringsFile.parentFile.name}: English fallback", localizedValue == defaultValue)
        }
    }

    @Test
    fun publisherAndPrivacyContactAreSeparateNonTranslatableResources() {
        val strings = projectFile("app/src/main/res/values/strings.xml").readText()
        assertTrue(strings.contains("name=\"publisher_name\" translatable=\"false\">Napahu Studios</string>"))
        assertTrue(strings.contains("name=\"privacy_support_email\" translatable=\"false\">napahustudios@gmail.com</string>"))
        assertTrue(settingsSource.contains("testTag(\"settings_support_contact\")"))
        assertEquals(2, settingsSource.occurrences("ContactBlock(onContactClick)"))
        assertTrue(mainSource.contains("Intent.ACTION_SENDTO"))
        assertTrue(mainSource.contains("Uri.parse(\"mailto:${'$'}{getString(R.string.privacy_support_email)}\")"))
        assertTrue(mainSource.contains("catch (_: android.content.ActivityNotFoundException)"))
    }

    @Test
    fun aboutUsesTheAuthoritativeStructuredEnglishSourceAndReadablePresentation() {
        val stringsFile = projectFile("app/src/main/res/values/strings.xml")
        val strings = stringsFile.readText()
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stringsFile)
        val stringNodes = document.getElementsByTagName("string")
        val aboutValues = (0 until stringNodes.length).mapNotNull { index ->
            stringNodes.item(index).attributes?.getNamedItem("name")
                ?.takeIf { it.nodeValue == "settings_about_body" }
                ?.let { stringNodes.item(index).textContent }
        }

        assertEquals(1, aboutValues.size)
        val about = aboutValues.single()
        listOf(
            "Puzru by Napahu Studios",
            "What the name means",
            "Akkadian word associated with concealment or secrecy",
            "What Puzru does",
            "phone’s built-in earpiece",
            "like a normal phone call",
            "standard Android audio-routing features",
            "Compatibility and limitations",
            "does not unlock, extend, or bypass",
            "What Puzru does not do",
            "does not make phone calls",
            "does not provide privacy from the AI service itself",
        ).forEach { claim -> assertTrue(claim, about.contains(claim)) }
        assertEquals(6, about.split("\\n\\n").size)
        assertEquals(7, about.split("\\n").count { it.startsWith("• ") })
        assertTrue(strings.contains("Puzru by Napahu Studios\\n\\nWhat the name means\\n"))

        val settingsSheet = settingsSource.kotlinDeclaration("fun SettingsSheet(")
        assertTrue(settingsSheet.contains("SettingsPage.ABOUT -> AboutPage("))
        assertTrue(settingsSheet.contains("page == SettingsPage.ABOUT"))
        val aboutPage = settingsSource.kotlinDeclaration("private fun AboutPage(")
        assertTrue(aboutPage.contains(".verticalScroll(rememberScrollState())"))
        assertTrue(aboutPage.contains(".testTag(\"settings_about_body\")"))
        assertTrue(aboutPage.contains("textAlign = TextAlign.Start"))
        assertFalse(aboutPage.contains("textAlign = TextAlign.Center"))
    }

    @Test
    fun finalizedAboutLocalesPreserveTheSourceStructureWithoutEnglishFallback() {
        val defaultAbout = resourceValue(projectFile("app/src/main/res/values/strings.xml"), "settings_about_body")

        listOf(
            "values-pl", "values-de", "values-es", "values-ar", "values-ja",
            "values-af", "values-am", "values-as", "values-az", "values-b+az+Arab+IR",
            "values-b+bho", "values-b+ceb", "values-b+ku+Latn", "values-b+mai",
            "values-b+pa+Arab+PK", "values-b+pa+Guru+IN", "values-b+sr+Latn",
            "values-b+sr+Latn+ME", "values-b+uz+Arab+AF", "values-b+uz+Cyrl+UZ",
            "values-b+yue+Hans+CN", "values-b+yue+Hant+HK", "values-b+zh+Hans",
            "values-b+zh+Hant", "values-be",
        ).forEach { resourceDirectory ->
            val stringsFile = projectFile("app/src/main/res/$resourceDirectory/strings.xml")
            val about = resourceValue(stringsFile, "settings_about_body")

            assertTrue("$resourceDirectory: translated About is missing", about.isNotBlank())
            assertFalse("$resourceDirectory: English About fallback", about == defaultAbout)
            assertEquals("$resourceDirectory: semantic sections", 6, about.split("\\n\\n").size)
            assertEquals("$resourceDirectory: bullet structure", 7, about.split("\\n").count { it.startsWith("• ") })
            assertFalse("$resourceDirectory: raw XML newline", about.contains('\n'))
            assertTrue("$resourceDirectory: Puzru brand", about.contains("Puzru"))
            assertTrue("$resourceDirectory: publisher brand", about.contains("Napahu Studios"))
        }
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
        assertTrue(serviceSource.method("fun diagnosticReport(): String").contains("observer.report(supportSummary, currentObservation)"))
        assertEquals(1, observerSource.occurrences("internal fun buildDiagnosticReport("))
        assertEquals(0, settingsSource.occurrences("buildDiagnosticReport("))
        assertFalse(settingsSource.contains("settings_save_diagnostic"))
    }

    @Test
    fun diagnosticWritePreservesUtf8WithoutAService() {
        val output = ByteArrayOutputStream()
        val report = "Puzru — zażółć 日本語"

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
        assertEquals(
            listOf(observerSourceFile),
            productionSources.kotlinMemberCallSites("setCommunicationDevice").map(KotlinCallSite::file),
        )
        assertFalse(settingsSource.contains("AudioManager"))
        assertFalse(settingsSource.contains("PrivateAudioState"))
        assertFalse(settingsSource.contains("AudioDiagnosticObserver"))
        assertFalse(settingsSource.contains("MODE_IN_COMMUNICATION"))
    }

    @Test
    fun diagnosticFilenameIsTimestampedPlainText() {
        val filename = diagnosticFilename(java.time.LocalDateTime.of(2026, 8, 19, 7, 5, 9))
        assertEquals("puzru-diagnostic-2026-08-19_07-05-09.txt", filename)
    }

    private fun String.method(signature: String): String =
        substring(indexOf(signature)).substringBefore("\n    }")

    private fun String.occurrences(needle: String): Int = windowed(needle.length).count { it == needle }

    private fun resourceValue(stringsFile: File, key: String): String {
        val nodes = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(stringsFile).getElementsByTagName("string")
        return (0 until nodes.length).firstNotNullOf { index ->
            nodes.item(index).attributes?.getNamedItem("name")
                ?.takeIf { it.nodeValue == key }
                ?.let { nodes.item(index).textContent }
        }
    }

    private companion object {
        val projectRoot = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
            .first { File(it, "app/src/main").isDirectory }
        fun projectFile(relativePath: String) = File(projectRoot, relativePath)
        val mainSource = projectFile("app/src/main/java/app/privateaudio/MainActivity.kt").readText()
        val screenSource = projectFile("app/src/main/java/app/privateaudio/ui/PrivateAudioScreen.kt").readText()
        val settingsSource = projectFile("app/src/main/java/app/privateaudio/ui/SettingsSheet.kt").readText()
        val serviceSource = projectFile("app/src/main/java/app/privateaudio/PrivateAudioService.kt").readText()
        val observerSource = projectFile("app/src/main/java/app/privateaudio/diagnostic/AudioDiagnosticObserver.kt").readText()
        val observerSourceFile = projectFile("app/src/main/java/app/privateaudio/diagnostic/AudioDiagnosticObserver.kt")
        val productionSources = File(projectRoot, "app/src/main/java")
            .walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()
    }
}
