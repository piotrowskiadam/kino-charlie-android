package pl.charlie.repertuar.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import pl.charlie.repertuar.R
import kotlin.math.roundToInt

private const val LogoSample = 4
private val LogoSourceOffset = IntOffset(340, 400)
private val LogoSourceSize = IntSize(1690, 2500)

/** Official source asset; display its mark without the surrounding website captions. */
@Composable
fun CinemaLogo(height: Dp = 56.dp) {
    val resources = LocalContext.current.resources
    val image = remember(resources) {
        BitmapFactory.decodeResource(resources, R.drawable.charlie_logo,
            BitmapFactory.Options().apply { inSampleSize = LogoSample }).asImageBitmap()
    }
    val filter = remember { ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
        0f, 0f, 0f, 0f, 0f,
        0f, 0f, 0f, 0f, 0f,
        0f, 0f, 0f, 0f, 0f,
        -1f / 3, -1f / 3, -1f / 3, 0f, 255f,
    ))) }
    val shape = RoundedCornerShape(6.dp)
    val plaque = if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) Modifier
        .background(Color.White, shape)
        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
        .padding(4.dp) else Modifier
    Box(Modifier.height(height).then(plaque).semantics { contentDescription = "Kino-Galeria Charlie" }) {
        Canvas(Modifier.fillMaxHeight().aspectRatio(LogoSourceSize.width.toFloat() / LogoSourceSize.height)) {
            drawImage(image, srcOffset = IntOffset(LogoSourceOffset.x / LogoSample, LogoSourceOffset.y / LogoSample),
                srcSize = IntSize(LogoSourceSize.width / LogoSample, LogoSourceSize.height / LogoSample),
                dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()), colorFilter = filter)
        }
    }
}
