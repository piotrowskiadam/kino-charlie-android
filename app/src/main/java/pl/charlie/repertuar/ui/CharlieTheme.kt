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
    primary = Color(0xFF742D3A), onPrimary = Color.White,
    primaryContainer = Color(0xFFF2DDE0), onPrimaryContainer = Color(0xFF52212C),
    secondary = Color(0xFF705447), secondaryContainer = Color(0xFFF1E8DB),
    onSecondaryContainer = Color(0xFF42362E), surface = Color(0xFFF8F4EC),
    onSurface = Color(0xFF28201F), onSurfaceVariant = Color(0xFF665952),
    surfaceContainer = Color(0xFFF1EAE0), surfaceContainerLow = Color(0xFFF5EFE6),
    surfaceContainerHighest = Color(0xFFEDE5DB), outline = Color(0xFF84726B),
    outlineVariant = Color(0xFFDCD0C7), error = Color(0xFFAD2530),
)
private val Dark = darkColorScheme(
    primary = Color(0xFFF0B8C2), onPrimary = Color(0xFF4A1723),
    primaryContainer = Color(0xFF5D2633), onPrimaryContainer = Color(0xFFFFD9DF),
    secondary = Color(0xFFD6BAA6), secondaryContainer = Color(0xFF4D4036),
    onSecondaryContainer = Color(0xFFF2E4D4), surface = Color(0xFF1D1918),
    onSurface = Color(0xFFF3EAE2), onSurfaceVariant = Color(0xFFCEBEB4),
    surfaceContainer = Color(0xFF292321), surfaceContainerLow = Color(0xFF241F1D),
    surfaceContainerHighest = Color(0xFF38302C), outline = Color(0xFFA79890),
    outlineVariant = Color(0xFF4D413C),
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
