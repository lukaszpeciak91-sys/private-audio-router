package app.privateaudio

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.espresso.Espresso.pressBack
import androidx.compose.ui.test.click
import app.privateaudio.ui.PrivateAudioScreen
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
import org.junit.Rule
import org.junit.Test

class PrivateAudioScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

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
