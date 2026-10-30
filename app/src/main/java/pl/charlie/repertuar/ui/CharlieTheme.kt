package pl.charlie.repertuar.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import pl.charlie.repertuar.R

val ProgramSerif = FontFamily(Font(R.font.literata))
private val Light = lightColorScheme(
    primary = Color.Black, onPrimary = Color.White,
    primaryContainer = Color(0xFF742D3A), onPrimaryContainer = Color.White,
    secondary = Color.Black, onSecondary = Color.White, secondaryContainer = Color(0xFFF1E8DB),
    onSecondaryContainer = Color.Black, surface = Color(0xFFF8F4EC),
    onSurface = Color.Black, onSurfaceVariant = Color.Black,
    background = Color(0xFFF8F4EC), onBackground = Color.Black,
    onErrorContainer = Color.Black,
    surfaceContainer = Color(0xFFF1EAE0), surfaceContainerLow = Color(0xFFF5EFE6),
    surfaceContainerHighest = Color(0xFFEDE5DB), outline = Color(0xFF84726B),
    outlineVariant = Color(0xFFDCD0C7), error = Color(0xFFAD2530),
)
private val Dark = darkColorScheme(
    primary = Color.White, onPrimary = Color.Black,
    primaryContainer = Color(0xFFA83E57), onPrimaryContainer = Color.White,
    secondary = Color.White, onSecondary = Color.Black,
    secondaryContainer = Color.Black, onSecondaryContainer = Color.White,
    tertiary = Color.White, onTertiary = Color.Black,
    tertiaryContainer = Color.Black, onTertiaryContainer = Color.White,
    background = Color.Black, onBackground = Color.White,
    surface = Color.Black, onSurface = Color.White, onSurfaceVariant = Color.White,
    surfaceVariant = Color.Black, surfaceTint = Color.Black,
    surfaceDim = Color.Black, surfaceBright = Color.Black,
    surfaceContainerLowest = Color.Black, surfaceContainerLow = Color.Black,
    surfaceContainer = Color.Black, surfaceContainerHigh = Color.Black,
    surfaceContainerHighest = Color.Black,
    inverseSurface = Color.Black, inverseOnSurface = Color.White, inversePrimary = Color.White,
    error = Color.White, onError = Color.Black, errorContainer = Color.Black, onErrorContainer = Color.White,
    outline = Color(0xFF999999), outlineVariant = Color(0xFF444444), scrim = Color.Black,
)

@Composable
fun CharlieTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val base = Typography()
    MaterialTheme(colorScheme = if (dark) Dark else Light, typography = base.copy(
        headlineLarge = base.headlineLarge.copy(fontFamily = ProgramSerif, fontSize = 30.sp, lineHeight = 38.sp, fontWeight = FontWeight.SemiBold),
        headlineSmall = base.headlineSmall.copy(fontFamily = ProgramSerif, fontSize = 24.sp, lineHeight = 32.sp),
        titleLarge = base.titleLarge.copy(fontFamily = ProgramSerif, fontSize = 22.sp, lineHeight = 29.sp, fontWeight = FontWeight.SemiBold),
        titleMedium = base.titleMedium.copy(fontFamily = ProgramSerif, fontSize = 18.sp, lineHeight = 25.sp, fontWeight = FontWeight.SemiBold),
    ), content = content)
}
