package hu.app.wearablehydration.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val LightColorPalette = lightColorScheme(
        background = Color.White,
        onBackground = Color.Black,
        // Itt adhatod meg a többi színt is, például:
        primary = Color.Blue, // Gombok színe
        onPrimary = Color.White // Gombokon lévő szöveg színe
)

@Composable
fun HydrationAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
            colorScheme = LightColorPalette,
            content = content
    )
}
