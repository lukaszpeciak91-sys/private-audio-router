package app.privateaudio.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PrivateAudioColorScheme = darkColorScheme(
    background = Color.Black,
    surface = Color.Black,
    surfaceContainer = Color.Black,
    onBackground = Color(0xFFF5F5F5),
    onSurface = Color(0xFFF5F5F5),
)

@Composable
fun PrivateAudioTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = PrivateAudioColorScheme,
        content = content,
    )
}
