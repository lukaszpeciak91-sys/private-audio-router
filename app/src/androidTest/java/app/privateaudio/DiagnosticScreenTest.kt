package app.privateaudio

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class DiagnosticScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun diagnosticSectionsAreDisplayed() {
        composeRule.onNodeWithText("POC-2 Mode Participation").assertIsDisplayed()
        composeRule.onNodeWithText("EARPIECE EXPERIMENT").assertIsDisplayed()
        composeRule.onNodeWithText("ARM EARPIECE TEST").assertIsDisplayed()
        composeRule.onNodeWithText("DISARM / CLEAR").assertIsDisplayed()
        composeRule.onNodeWithText("CURRENT STATE").assertIsDisplayed()
        composeRule.onNodeWithText("AVAILABLE COMMUNICATION DEVICES").assertIsDisplayed()
        composeRule.onNodeWithText("REFRESH / RECORD SNAPSHOT").assertIsDisplayed()
        composeRule.onNodeWithText("COPY REPORT").assertIsDisplayed()
    }

    @Test
    fun copyReportShowsConfirmation() {
        composeRule.onNodeWithText("COPY REPORT").performClick()

        composeRule.onNodeWithText("Report copied").assertIsDisplayed()
    }
}
