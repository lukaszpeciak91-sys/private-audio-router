package app.privateaudio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class Layer41LocalizationContractTest {
    @Test
    fun standardAndroidLocaleConfigurationUsesEnglishDefaultResources() {
        assertTrue(appBuildSource.contains("generateLocaleConfig = true"))
        assertEquals("unqualifiedResLocale=en-US", resourcesProperties.trim())
        assertTrue(defaultStrings.contains("name=\"settings_system_default\">Default</string>"))
        assertFalse(defaultStrings.contains("translatable=\"false\""))
        assertEquals(listOf("values-pl"), localeDirectories.map { it.name })
        assertEquals(stringKeys(defaultStrings), stringKeys(polishStrings))
        assertEquals(placeholders(defaultStrings), placeholders(polishStrings))
    }

    @Test
    fun languageSelectionUsesPlatformConfigurationWithoutAParallelLocaleRegistry() {
        assertTrue(languagePreferencesSource.contains("LocaleConfig(context).supportedLocales"))
        assertTrue(languagePreferencesSource.contains("getSystemService(LocaleManager::class.java)"))
        assertTrue(languagePreferencesSource.contains("LocaleList.getEmptyLocaleList()"))
        assertTrue(languagePreferencesSource.contains("Build.VERSION_CODES.TIRAMISU"))
        assertFalse(languagePreferencesSource.contains("listOf(\"en-US\", \"pl\")"))
        assertFalse(languagePreferencesSource.contains("SharedPreferences"))
        assertTrue(settingsSource.contains("supportedLanguages.forEach"))
        assertTrue(settingsSource.contains("selectedLanguageTag == null"))
    }

    @Test
    fun existingOverlayRefreshesOnlyLocalizedPresentationOnConfigurationChange() {
        assertTrue(overlaySource.contains("override fun onConfigurationChanged"))
        assertTrue(overlaySource.contains("overlayView?.refreshLocalizedPresentation()"))
        assertTrue(overlaySource.contains("contentDescription = stateDescription(state)"))
        assertFalse(overlaySource.method("override fun onConfigurationChanged").contains("hideOverlay()"))
        assertFalse(overlaySource.method("override fun onConfigurationChanged").contains("bindControllerService()"))
    }

    @Test
    fun productSurfacesUseResourcesWhileDiagnosticsStayOutsideLocalization() {
        assertFalse(mainSource.contains("ClipData.newPlainText(\""))
        assertTrue(mainSource.contains("getString(R.string.diagnostic_report_clip_label)"))
        assertFalse(productScreenSource.contains("Text(\""))
        assertFalse(settingsSource.contains("Text(\""))
        assertTrue(diagnosticScreenSource.contains("Text(\"PRIVATE AUDIO\""))
    }

    @Test
    fun protectedRoutingAndReportFormatterRemainSingleAndUnchangedInOwnership() {
        assertEquals(1, productionSources.sumOf { it.readText().occurrences("setCommunicationDevice(") })
        assertEquals(1, observerSource.occurrences("internal fun buildDiagnosticReport("))
        assertFalse(defaultStrings.contains("PRIVATE AUDIO DIAGNOSTIC REPORT"))
    }

    private fun String.occurrences(needle: String): Int = windowed(needle.length).count { it == needle }

    private fun String.method(signature: String): String =
        substring(indexOf(signature)).substringBefore("\n    }")

    private fun stringKeys(resources: String) =
        Regex("<string name=\"([^\"]+)\"").findAll(resources).map { it.groupValues[1] }.toList()

    private fun placeholders(resources: String) =
        Regex("<string name=\"([^\"]+)\">([^<]*)</string>").findAll(resources).associate {
            it.groupValues[1] to Regex("%\\d+\\$[a-z]").findAll(it.groupValues[2]).map { match -> match.value }.toList()
        }

    private companion object {
        val projectRoot = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
            .first { File(it, "app/src/main").isDirectory }
        val appBuildSource = projectFile("app/build.gradle.kts").readText()
        val resourcesProperties = projectFile("app/src/main/res/resources.properties").readText()
        val defaultStrings = projectFile("app/src/main/res/values/strings.xml").readText()
        val polishStrings = projectFile("app/src/main/res/values-pl/strings.xml").readText()
        val mainSource = projectFile("app/src/main/java/app/privateaudio/MainActivity.kt").readText()
        val productScreenSource = projectFile("app/src/main/java/app/privateaudio/ui/PrivateAudioScreen.kt").readText()
        val settingsSource = projectFile("app/src/main/java/app/privateaudio/ui/SettingsSheet.kt").readText()
        val diagnosticScreenSource = projectFile("app/src/main/java/app/privateaudio/ui/DiagnosticScreen.kt").readText()
        val languagePreferencesSource = projectFile("app/src/main/java/app/privateaudio/localization/AppLanguagePreferences.kt").readText()
        val overlaySource = projectFile("app/src/main/java/app/privateaudio/overlay/OverlayService.kt").readText()
        val observerSource = projectFile("app/src/main/java/app/privateaudio/diagnostic/AudioDiagnosticObserver.kt").readText()
        val productionSources = File(projectRoot, "app/src/main/java")
            .walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()
        val localeDirectories = File(projectRoot, "app/src/main/res")
            .listFiles().orEmpty()
            .filter { it.isDirectory && it.name.startsWith("values-") && it.name != "values-night" }

        fun projectFile(relativePath: String) = File(projectRoot, relativePath)
    }
}
