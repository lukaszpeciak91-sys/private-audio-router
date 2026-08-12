package app.privateaudio

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class BootstrapScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun bootstrapCopyIsDisplayed() {
        composeRule.onNodeWithText("Private Audio").assertIsDisplayed()
        composeRule.onNodeWithText("Android foundation ready").assertIsDisplayed()
    }
}
