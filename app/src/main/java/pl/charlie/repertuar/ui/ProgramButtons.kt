package pl.charlie.repertuar.ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Accent fills keep inverse white captions; neutral and disabled text follows the monochrome theme. */
@Composable
fun ProgramButton(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit) {
    val colors = MaterialTheme.colorScheme
    Button(onClick, modifier, enabled, colors = ButtonDefaults.buttonColors(
        containerColor = colors.primaryContainer, contentColor = colors.onPrimaryContainer,
        disabledContainerColor = colors.surface, disabledContentColor = colors.onSurface), content = content)
}

@Composable
fun ProgramTextButton(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit) {
    val color = MaterialTheme.colorScheme.onSurface
    TextButton(onClick, modifier, enabled, colors = ButtonDefaults.textButtonColors(
        contentColor = color, disabledContentColor = color), content = content)
}
