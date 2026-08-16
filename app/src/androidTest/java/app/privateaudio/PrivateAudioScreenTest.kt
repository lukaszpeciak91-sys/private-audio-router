package app.privateaudio

import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import app.privateaudio.ui.PrivateAudioScreen
import app.privateaudio.ui.theme.PrivateAudioTheme
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
    fun connectingPowerIsUnavailableAndFutureControlsHaveNoActions() {
        var powerClicks = 0
        composeRule.setContent {
            PrivateAudioTheme {
                PrivateAudioScreen(
                    state = PrivateAudioState.READY,
                    powerEnabled = false,
                    onPowerClick = { powerClicks++ },
                    onCloseClick = {},
                )
            }
        }

        composeRule.onNodeWithTag("private_audio_power").performClick()
        composeRule.onNodeWithTag("private_audio_floating").assertHasNoClickAction()
        composeRule.onNodeWithTag("private_audio_settings").assertHasNoClickAction()

        composeRule.runOnIdle { assertEquals(0, powerClicks) }
    }
}
