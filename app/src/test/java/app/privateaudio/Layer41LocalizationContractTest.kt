package app.privateaudio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.Locale

class Layer41LocalizationContractTest {
    @Test
    fun standardAndroidLocaleConfigurationUsesEnglishDefaultResources() {
        assertTrue(appBuildSource.contains("generateLocaleConfig = true"))
        assertEquals("unqualifiedResLocale=en-US", resourcesProperties.trim())
        assertTrue(defaultStrings.contains("name=\"settings_system_default\">Default</string>"))
        assertFalse(defaultStrings.contains("translatable=\"false\""))
        assertEquals(listOf("values-be", "values-cs", "values-de", "values-fr", "values-lt", "values-pl", "values-ru", "values-sk", "values-uk"), localeDirectories.map { it.name }.sorted())
        assertEquals(stringKeys(defaultStrings), stringKeys(polishStrings))
        assertEquals(placeholders(defaultStrings), placeholders(polishStrings))
        assertEquals(stringKeys(defaultStrings), stringKeys(germanStrings))
        assertEquals(placeholders(defaultStrings), placeholders(germanStrings))
        assertEquals(stringKeys(defaultStrings), stringKeys(czechStrings))
        assertEquals(placeholders(defaultStrings), placeholders(czechStrings))
        assertEquals(stringKeys(defaultStrings), stringKeys(slovakStrings))
        assertEquals(placeholders(defaultStrings), placeholders(slovakStrings))
        assertEquals(stringKeys(defaultStrings), stringKeys(ukrainianStrings))
        assertEquals(placeholders(defaultStrings), placeholders(ukrainianStrings))
        assertEquals(stringKeys(defaultStrings), stringKeys(belarusianStrings))
        assertEquals(placeholders(defaultStrings), placeholders(belarusianStrings))
        assertEquals(stringKeys(defaultStrings), stringKeys(lithuanianStrings))
        assertEquals(placeholders(defaultStrings), placeholders(lithuanianStrings))
        assertEquals(stringKeys(defaultStrings), stringKeys(russianStrings))
        assertEquals(placeholders(defaultStrings), placeholders(russianStrings))
        assertEquals(stringKeys(defaultStrings), stringKeys(frenchStrings))
        assertEquals(placeholders(defaultStrings), placeholders(frenchStrings))
        val frenchLocale = Locale.forLanguageTag("fr")
        val frenchNativeName = frenchLocale.getDisplayName(frenchLocale).replaceFirstChar { first ->
            if (first.isLowerCase()) first.titlecase(frenchLocale) else first.toString()
        }
        assertEquals("Français", frenchNativeName)
        assertTrue(frenchStrings.contains("name=\"state_ready\">Prêt</string>"))
        assertTrue(frenchStrings.contains("name=\"state_waiting\">En attente</string>"))
        assertTrue(frenchStrings.contains("name=\"state_active\">Actif</string>"))
        assertTrue(frenchStrings.contains("name=\"state_error\">Erreur</string>"))
        val russianLocale = Locale.forLanguageTag("ru")
        val russianNativeName = russianLocale.getDisplayName(russianLocale).replaceFirstChar { first ->
            if (first.isLowerCase()) first.titlecase(russianLocale) else first.toString()
        }
        assertEquals("Русский", russianNativeName)
        assertTrue(russianStrings.contains("name=\"state_ready\">Готово</string>"))
        assertTrue(russianStrings.contains("name=\"state_waiting\">Ожидание</string>"))
        assertTrue(russianStrings.contains("name=\"state_active\">Активно</string>"))
        assertTrue(russianStrings.contains("name=\"state_error\">Ошибка</string>"))
        val lithuanianLocale = Locale.forLanguageTag("lt")
        val lithuanianNativeName = lithuanianLocale.getDisplayName(lithuanianLocale).replaceFirstChar { first ->
            if (first.isLowerCase()) first.titlecase(lithuanianLocale) else first.toString()
        }
        assertEquals("Lietuvių", lithuanianNativeName)
        assertTrue(lithuanianStrings.contains("name=\"state_ready\">Paruošta</string>"))
        assertTrue(lithuanianStrings.contains("name=\"state_waiting\">Laukia</string>"))
        assertTrue(lithuanianStrings.contains("name=\"state_active\">Aktyvu</string>"))
        assertTrue(lithuanianStrings.contains("name=\"state_error\">Klaida</string>"))
        val belarusianLocale = Locale.forLanguageTag("be")
        val belarusianNativeName = belarusianLocale.getDisplayName(belarusianLocale).replaceFirstChar { first ->
            if (first.isLowerCase()) first.titlecase(belarusianLocale) else first.toString()
        }
        assertEquals("Беларуская", belarusianNativeName)
        assertTrue(belarusianStrings.contains("name=\"state_ready\">Гатова</string>"))
        assertTrue(belarusianStrings.contains("name=\"state_waiting\">Чакае</string>"))
        assertTrue(belarusianStrings.contains("name=\"state_active\">Актыўна</string>"))
        assertTrue(belarusianStrings.contains("name=\"state_error\">Памылка</string>"))
        val ukrainianLocale = Locale.forLanguageTag("uk")
        val ukrainianNativeName = ukrainianLocale.getDisplayName(ukrainianLocale).replaceFirstChar { first ->
            if (first.isLowerCase()) first.titlecase(ukrainianLocale) else first.toString()
        }
        assertEquals("Українська", ukrainianNativeName)
        assertTrue(ukrainianStrings.contains("name=\"state_ready\">Готово</string>"))
        assertTrue(ukrainianStrings.contains("name=\"state_waiting\">Очікує</string>"))
        assertTrue(ukrainianStrings.contains("name=\"state_active\">Активно</string>"))
        assertTrue(ukrainianStrings.contains("name=\"state_error\">Помилка</string>"))
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
        val germanStrings = projectFile("app/src/main/res/values-de/strings.xml").readText()
        val czechStrings = projectFile("app/src/main/res/values-cs/strings.xml").readText()
        val slovakStrings = projectFile("app/src/main/res/values-sk/strings.xml").readText()
        val ukrainianStrings = projectFile("app/src/main/res/values-uk/strings.xml").readText()
        val belarusianStrings = projectFile("app/src/main/res/values-be/strings.xml").readText()
        val lithuanianStrings = projectFile("app/src/main/res/values-lt/strings.xml").readText()
        val russianStrings = projectFile("app/src/main/res/values-ru/strings.xml").readText()
        val frenchStrings = projectFile("app/src/main/res/values-fr/strings.xml").readText()
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
