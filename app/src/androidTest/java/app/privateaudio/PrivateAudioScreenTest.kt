package app.privateaudio

import android.app.LocaleManager
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.test.espresso.Espresso.pressBack
import androidx.test.platform.app.InstrumentationRegistry
import androidx.compose.ui.test.click
import app.privateaudio.localization.AppLanguagePreferences
import app.privateaudio.ui.PrivateAudioScreen
import app.privateaudio.ui.SettingsSheet
import app.privateaudio.ui.UserDiagnosticsScreen
import app.privateaudio.ui.theme.PrivateAudioTheme
import app.privateaudio.diagnostic.DiagnosticsAvailability
import app.privateaudio.diagnostic.DiagnosticsError
import app.privateaudio.diagnostic.DiagnosticsPermission
import app.privateaudio.diagnostic.DiagnosticsRoute
import app.privateaudio.diagnostic.DiagnosticsRouting
import app.privateaudio.diagnostic.DiagnosticsRoutingResult
import app.privateaudio.diagnostic.DiagnosticsSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.Locale

class PrivateAudioScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun portraitMainKeepsEveryPrimaryAndSecondaryControlVisible() {
        composeRule.setContent {
            PrivateAudioTheme {
                PrivateAudioScreen(
                    state = PrivateAudioState.READY,
                    onPowerClick = {},
                    onCloseClick = {},
                    modifier = Modifier.width(393.dp).height(852.dp),
                )
            }
        }

        mainControlTags.forEach { composeRule.onNodeWithTag(it).assertIsDisplayed() }
    }

    @Test
    fun narrowPortraitPreservesCanonicalPowerSizing() {
        composeRule.setContent {
            PrivateAudioTheme {
                PrivateAudioScreen(
                    state = PrivateAudioState.READY,
                    onPowerClick = {},
                    onCloseClick = {},
                    modifier = Modifier.width(360.dp).height(800.dp),
                )
            }
        }

        val rootBounds = composeRule.onRoot().fetchSemanticsNode().boundsInRoot
        val powerBounds = composeRule.onNodeWithTag("private_audio_power").fetchSemanticsNode().boundsInRoot
        assertTrue(
            "narrow portrait Power keeps the pre-landscape padded-width sizing contract",
            powerBounds.width < rootBounds.width * 0.80f,
        )
    }

    @Test
    fun compactLandscapeKeepsThreeAreaContentInsideUsableBoundsAndActionsConnected() {
        var powerClicks = 0
        var floatingClicks = 0
        var closeClicks = 0
        composeRule.setContent {
            PrivateAudioTheme {
                PrivateAudioScreen(
                    state = PrivateAudioState.WAITING,
                    onPowerClick = { powerClicks++ },
                    onFloatingClick = { floatingClicks++ },
                    onCloseClick = { closeClicks++ },
                    modifier = Modifier.width(720.dp).height(360.dp),
                )
            }
        }

        val rootBounds = composeRule.onRoot().fetchSemanticsNode().boundsInRoot
        mainControlTags.forEach { tag ->
            composeRule.onNodeWithTag(tag).assertIsDisplayed()
            val bounds = composeRule.onNodeWithTag(tag).fetchSemanticsNode().boundsInRoot
            assertTrue("$tag starts inside the window", bounds.left >= 0f && bounds.top >= 0f)
            assertTrue("$tag ends inside the window", bounds.right <= rootBounds.right && bounds.bottom <= rootBounds.bottom)
        }
        val floatingBounds = composeRule.onNodeWithTag("private_audio_floating").fetchSemanticsNode().boundsInRoot
        val settingsBounds = composeRule.onNodeWithTag("private_audio_settings").fetchSemanticsNode().boundsInRoot
        val closeBounds = composeRule.onNodeWithTag("private_audio_close").fetchSemanticsNode().boundsInRoot
        assertTrue(floatingBounds.center.y < settingsBounds.center.y)
        assertTrue(settingsBounds.center.y < closeBounds.center.y)
        assertTrue(floatingBounds.width > floatingBounds.height)
        composeRule.onNodeWithTag("private_audio_power").performClick()
        composeRule.onNodeWithTag("private_audio_floating").performClick()
        composeRule.onNodeWithTag("private_audio_close").performClick()
        composeRule.onNodeWithTag("private_audio_settings").performClick()
        composeRule.onNodeWithTag("settings_sheet").assertIsDisplayed()
        composeRule.runOnIdle {
            assertEquals(1, powerClicks)
            assertEquals(1, floatingClicks)
            assertEquals(1, closeClicks)
        }
    }

    @Test
    fun yiddishBottomControlsAreRtl() {
        assertBottomControlOrder(
            "yi",
            listOf("private_audio_close", "private_audio_settings", "private_audio_floating"),
        )
    }

    @Test
    fun polishBottomControlsAreLtr() {
        assertBottomControlOrder(
            "pl",
            listOf("private_audio_floating", "private_audio_settings", "private_audio_close"),
        )
    }

    @Test
    fun compactLandscapeMirrorsItsThreeLogicalAreasForRtl() {
        assertLandscapeAreaDirection("pl", rtl = false)
        assertLandscapeAreaDirection("yi", rtl = true)
    }

    @Test
    fun selectingModernYiddishKeepsYiddishResourcesAndRtl() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val localeManager = context.getSystemService(LocaleManager::class.java)
        val original = localeManager.applicationLocales
        try {
            AppLanguagePreferences.select(context, "yi")

            assertEquals("yi", AppLanguagePreferences.currentLanguageTag(context))
            assertEquals(
                android.view.View.LAYOUT_DIRECTION_RTL,
                AppLanguagePreferences.presentationLayoutDirection(context),
            )
            val selectedTag = AppLanguagePreferences.currentLanguageTag(context)!!
            val localizedContext = context.createConfigurationContext(
                Configuration(context.resources.configuration).apply {
                    setLocale(Locale.forLanguageTag(selectedTag))
                },
            )
            assertEquals("סעטינגס", localizedContext.getString(R.string.settings))
        } finally {
            localeManager.applicationLocales = original
        }
    }

    @Test
    fun allProductStatesRenderTheirAuthoritativeLabel() {
        listOf(
            PrivateAudioState.READY to "Ready",
            PrivateAudioState.WAITING to "Waiting",
            PrivateAudioState.ACTIVE to "Active",
            PrivateAudioState.ERROR to "Error",
        ).forEach { (state, label) ->
            composeRule.setContent {
                PrivateAudioTheme {
                    PrivateAudioScreen(state = state, onPowerClick = {}, onCloseClick = {})
                }
            }

            composeRule.onNodeWithText(label).assertIsDisplayed()
            composeRule.onNodeWithTag("private_audio_power").assertIsDisplayed()
        }
    }

    @Test
    fun powerAndCloseInvokeOnlyTheirSuppliedProductActions() {
        var powerClicks = 0
        var closeClicks = 0
        composeRule.setContent {
            PrivateAudioTheme {
                PrivateAudioScreen(
                    state = PrivateAudioState.WAITING,
                    onPowerClick = { powerClicks++ },
                    onCloseClick = { closeClicks++ },
                )
            }
        }

        composeRule.onNodeWithTag("private_audio_power").performClick()
        composeRule.onNodeWithTag("private_audio_close").performClick()

        composeRule.runOnIdle {
            assertEquals(1, powerClicks)
            assertEquals(1, closeClicks)
        }
    }

    @Test
    fun connectingPowerIsUnavailableAndFloatingInvokesItsSuppliedAction() {
        var powerClicks = 0
        var floatingClicks = 0
        composeRule.setContent {
            PrivateAudioTheme {
                PrivateAudioScreen(
                    state = PrivateAudioState.READY,
                    powerEnabled = false,
                    onPowerClick = { powerClicks++ },
                    onFloatingClick = { floatingClicks++ },
                    onCloseClick = {},
                )
            }
        }

        composeRule.onNodeWithTag("private_audio_power").performClick()
        composeRule.onNodeWithTag("private_audio_floating").performClick()

        composeRule.runOnIdle {
            assertEquals(0, powerClicks)
            assertEquals(1, floatingClicks)
        }
    }

    @Test
    fun settingsOpensAndOutsideTapClosesIt() {
        composeRule.setContent {
            PrivateAudioTheme {
                PrivateAudioScreen(
                    state = PrivateAudioState.READY,
                    onPowerClick = {},
                    onCloseClick = {},
                    versionName = "9.8.7",
                )
            }
        }

        composeRule.onNodeWithTag("private_audio_settings").performClick()
        composeRule.onNodeWithTag("settings_sheet").assertIsDisplayed()
        composeRule.onNodeWithText("Version 9.8.7").assertIsDisplayed()
        composeRule.onRoot().performTouchInput { click(Offset(1f, 1f)) }
        composeRule.onNodeWithTag("settings_sheet").assertDoesNotExist()
    }

    @Test
    fun compactHeightSettingsStaysBoundedAndScrollsToEveryDestination() {
        var diagnosticsClicks = 0
        showCompactSettings(onDiagnostics = { diagnosticsClicks++ })

        val backdrop = composeRule.onNodeWithTag("settings_backdrop").fetchSemanticsNode().boundsInRoot
        val sheet = composeRule.onNodeWithTag("settings_sheet").fetchSemanticsNode().boundsInRoot
        assertTrue("sheet starts inside compact safe content", sheet.top >= backdrop.top)
        assertTrue("sheet ends inside compact safe content", sheet.bottom <= backdrop.bottom)

        composeRule.onNodeWithTag("settings_version").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("settings_about").performScrollTo().performClick()
        composeRule.onNodeWithTag("settings_child_back").assertIsDisplayed().performClick()
        composeRule.onNodeWithTag("settings_diagnostics").performScrollTo().performClick()
        composeRule.runOnIdle { assertEquals(1, diagnosticsClicks) }
    }

    @Test
    fun compactHeightAdvancedKeepsBothSwitchesReachableAndConnected() {
        var proximityEnabled = true
        var assistantEnabled = false
        showCompactSettings(
            onProximityFeatureChange = { proximityEnabled = it },
            onAssistantEarlyRouteChange = { assistantEnabled = it },
        )

        composeRule.onNodeWithTag("settings_advanced").performScrollTo().performClick()
        composeRule.onNodeWithTag("settings_proximity_screen").performClick()
        composeRule.onNodeWithTag("settings_assistant_early_route")
            .performScrollTo().assertIsDisplayed().performClick()
        composeRule.runOnIdle {
            assertEquals(false, proximityEnabled)
            assertEquals(true, assistantEnabled)
        }
    }

    @Test
    fun compactHeightAboutBodyAndBackRemainReachable() {
        showCompactSettings()

        composeRule.onNodeWithTag("settings_about").performScrollTo().performClick()
        composeRule.onNodeWithText(
            InstrumentationRegistry.getInstrumentation().targetContext
                .getString(R.string.settings_about_body),
        ).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("settings_child_back").performScrollTo().performClick()
        composeRule.onNodeWithTag("settings_root_page").assertExists()
    }

    @Test
    fun compactHeightDiagnosticsScrollsToSaveReport() {
        var saveClicks = 0
        composeRule.setContent {
            PrivateAudioTheme {
                UserDiagnosticsScreen(
                    diagnosticsSummary(),
                    onBack = {},
                    onSaveDiagnosticReport = { saveClicks++ },
                    modifier = Modifier.width(720.dp).height(300.dp),
                )
            }
        }

        composeRule.onNodeWithTag("diagnostics_save_report")
            .performScrollTo().assertIsDisplayed().performClick()
        composeRule.runOnIdle { assertEquals(1, saveClicks) }
    }

    @Test
    fun privacyPolicyOpensWithoutStackingAndOutsideTapClosesIt() {
        composeRule.setContent {
            PrivateAudioTheme {
                PrivateAudioScreen(
                    state = PrivateAudioState.READY,
                    onPowerClick = {},
                    onCloseClick = {},
                )
            }
        }

        composeRule.onNodeWithTag("private_audio_settings").performClick()
        composeRule.onNodeWithTag("settings_privacy_policy").assertIsDisplayed().performClick()
        composeRule.onNodeWithTag("privacy_policy_panel").assertIsDisplayed()
        composeRule.onNodeWithText("Privacy Policy").assertIsDisplayed()
        composeRule.onNodeWithTag("settings_privacy_policy").assertDoesNotExist()
        composeRule.onRoot().performTouchInput { click(Offset(1f, 1f)) }
        composeRule.onNodeWithTag("privacy_policy_panel").assertDoesNotExist()
        composeRule.onNodeWithTag("private_audio_power").assertIsDisplayed()
    }

    @Test
    fun privacyPolicyBackReturnsToSettingsRoot() {
        composeRule.setContent {
            PrivateAudioTheme {
                PrivateAudioScreen(
                    state = PrivateAudioState.READY,
                    onPowerClick = {},
                    onCloseClick = {},
                )
            }
        }

        composeRule.onNodeWithTag("private_audio_settings").performClick()
        composeRule.onNodeWithTag("settings_privacy_policy").performClick()
        pressBack()
        composeRule.onNodeWithTag("privacy_policy_panel").assertDoesNotExist()
        composeRule.onNodeWithTag("settings_privacy_policy").assertIsDisplayed()
        composeRule.onNodeWithTag("settings_sheet").assertIsDisplayed()
    }

    @Test
    fun privacyPolicyVisibleBackReturnsToSettingsRoot() {
        composeRule.setContent {
            PrivateAudioTheme {
                PrivateAudioScreen(
                    state = PrivateAudioState.READY,
                    onPowerClick = {},
                    onCloseClick = {},
                )
            }
        }

        composeRule.onNodeWithTag("private_audio_settings").performClick()
        composeRule.onNodeWithTag("settings_privacy_policy").performClick()
        composeRule.onNodeWithTag("settings_child_back").assertIsDisplayed().performClick()
        composeRule.onNodeWithTag("privacy_policy_panel").assertDoesNotExist()
        composeRule.onNodeWithTag("settings_privacy_policy").assertIsDisplayed()
        composeRule.onNodeWithTag("settings_sheet").assertIsDisplayed()
    }

    @Test
    fun diagnosticsMovesReportActionOutOfSettingsAndBackReturnsToMainScreen() {
        var saveClicks = 0
        composeRule.setContent {
            PrivateAudioTheme {
                PrivateAudioScreen(
                    state = PrivateAudioState.READY,
                    onPowerClick = {},
                    onCloseClick = {},
                    onSaveDiagnosticReport = { saveClicks++ },
                )
            }
        }

        composeRule.onNodeWithTag("private_audio_settings").performClick()
        composeRule.onNodeWithTag("settings_diagnostics").performClick()
        composeRule.onNodeWithTag("settings_sheet").assertDoesNotExist()
        composeRule.onNodeWithTag("diagnostics_save_report").performClick()
        composeRule.runOnIdle { assertEquals(1, saveClicks) }
        composeRule.onNodeWithTag("diagnostics_back").performClick()
        composeRule.onNodeWithTag("private_audio_power").assertIsDisplayed()
    }

    @Test
    fun diagnosticsBaseScreenContainsOnlyProductHealthSectionsAndRows() {
        showDiagnostics(diagnosticsSummary())

        listOf(
            "SYSTEM CHECK", "PRIVATE AUDIO", "Earpiece", "Proximity sensor", "Floating control",
            "Routing", "Status", "Audio route", "Save diagnostic report",
        ).forEach { composeRule.onNodeWithText(it).assertIsDisplayed() }
        listOf("DEVICE", "Device", "Android", "Private Audio version", "Detected audio", "LAST ROUTING")
            .forEach { composeRule.onNodeWithText(it).assertDoesNotExist() }
    }

    @Test
    fun diagnosticsSuccessShowsResultWithoutError() {
        showDiagnostics(diagnosticsSummary(result = DiagnosticsRoutingResult.SUCCESS))

        composeRule.onNodeWithText("LAST ROUTING").assertIsDisplayed()
        composeRule.onNodeWithText("Result").assertIsDisplayed()
        composeRule.onNodeWithText("Success").assertIsDisplayed()
        composeRule.onNodeWithText("Error").assertDoesNotExist()
    }

    @Test
    fun diagnosticsFailureShowsMappedErrorAndFloatingPermissionLanguage() {
        showDiagnostics(
            diagnosticsSummary(
                permission = DiagnosticsPermission.NOT_GRANTED,
                result = DiagnosticsRoutingResult.FAILED,
                error = DiagnosticsError.BLOCKED_BY_SYSTEM,
            ),
        )

        composeRule.onNodeWithText("Permission required").assertIsDisplayed()
        composeRule.onNodeWithText("Failed").assertIsDisplayed()
        composeRule.onNodeWithText("Error").assertIsDisplayed()
        composeRule.onNodeWithText("Routing was blocked by the system.").assertIsDisplayed()
        composeRule.onNodeWithText("Overlay permission").assertDoesNotExist()
    }

    private fun showDiagnostics(summary: DiagnosticsSummary) {
        composeRule.setContent {
            PrivateAudioTheme {
                UserDiagnosticsScreen(summary, onBack = {}, onSaveDiagnosticReport = {})
            }
        }
    }

    private fun showCompactSettings(
        onProximityFeatureChange: (Boolean) -> Unit = {},
        onAssistantEarlyRouteChange: (Boolean) -> Unit = {},
        onDiagnostics: () -> Unit = {},
    ) {
        composeRule.setContent {
            PrivateAudioTheme {
                SettingsSheet(
                    versionName = "9.8.7",
                    proximityFeatureEnabled = true,
                    onProximityFeatureChange = onProximityFeatureChange,
                    assistantEarlyRouteEnabled = false,
                    onAssistantEarlyRouteChange = onAssistantEarlyRouteChange,
                    onDiagnostics = onDiagnostics,
                    onDismiss = {},
                    modifier = Modifier.width(720.dp).height(300.dp),
                )
            }
        }
    }

    private fun assertBottomControlOrder(languageTag: String, expectedLeftToRight: List<String>) {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val localeManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            targetContext.getSystemService(LocaleManager::class.java)
        } else {
            null
        }
        val original = localeManager?.applicationLocales
        try {
            localeManager?.applicationLocales = LocaleList.forLanguageTags(languageTag)
            val localizedContext = targetContext.createConfigurationContext(
                Configuration(targetContext.resources.configuration).apply {
                    setLocale(Locale.forLanguageTag(languageTag))
                },
            )
            composeRule.setContent {
                CompositionLocalProvider(LocalContext provides localizedContext) {
                    PrivateAudioTheme {
                        PrivateAudioScreen(
                            state = PrivateAudioState.READY,
                            onPowerClick = {},
                            onCloseClick = {},
                        )
                    }
                }
            }

            val actual = expectedLeftToRight.sortedBy { tag ->
                composeRule.onNodeWithTag(tag).fetchSemanticsNode().boundsInRoot.left
            }
            assertEquals(languageTag, expectedLeftToRight, actual)
        } finally {
            if (localeManager != null && original != null) {
                localeManager.applicationLocales = original
            }
        }
    }

    private fun assertLandscapeAreaDirection(languageTag: String, rtl: Boolean) {
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val localeManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            targetContext.getSystemService(LocaleManager::class.java)
        } else null
        val original = localeManager?.applicationLocales
        try {
            localeManager?.applicationLocales = LocaleList.forLanguageTags(languageTag)
            val localizedContext = targetContext.createConfigurationContext(
                Configuration(targetContext.resources.configuration).apply {
                    setLocale(Locale.forLanguageTag(languageTag))
                },
            )
            composeRule.setContent {
                CompositionLocalProvider(LocalContext provides localizedContext) {
                    PrivateAudioTheme {
                        PrivateAudioScreen(
                            state = PrivateAudioState.READY,
                            onPowerClick = {},
                            onCloseClick = {},
                            modifier = Modifier.width(720.dp).height(360.dp),
                        )
                    }
                }
            }
            val statusCenter = composeRule.onNodeWithTag("private_audio_status")
                .fetchSemanticsNode().boundsInRoot.center.x
            val powerCenter = composeRule.onNodeWithTag("private_audio_power")
                .fetchSemanticsNode().boundsInRoot.center.x
            val actionsCenter = composeRule.onNodeWithTag("private_audio_floating")
                .fetchSemanticsNode().boundsInRoot.center.x
            if (rtl) {
                assertTrue(statusCenter > powerCenter && powerCenter > actionsCenter)
            } else {
                assertTrue(statusCenter < powerCenter && powerCenter < actionsCenter)
            }
        } finally {
            if (localeManager != null && original != null) localeManager.applicationLocales = original
        }
    }

    private companion object {
        val mainControlTags = listOf(
            "private_audio_power",
            "private_audio_status",
            "private_audio_floating",
            "private_audio_settings",
            "private_audio_close",
        )
    }

    private fun diagnosticsSummary(
        permission: DiagnosticsPermission = DiagnosticsPermission.GRANTED,
        result: DiagnosticsRoutingResult = DiagnosticsRoutingResult.NONE,
        error: DiagnosticsError = DiagnosticsError.NONE,
    ) = DiagnosticsSummary(
        earpiece = DiagnosticsAvailability.AVAILABLE,
        proximitySensor = DiagnosticsAvailability.AVAILABLE,
        floatingControlPermission = permission,
        routing = DiagnosticsRouting.ON,
        status = PrivateAudioState.WAITING,
        audioRoute = DiagnosticsRoute.SPEAKER,
        lastRoutingResult = result,
        lastError = error,
    )
}
