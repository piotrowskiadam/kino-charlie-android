package pl.charlie.repertuar.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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

/** Visible secondary action; the disabled outline changes without fading the monochrome caption. */
@Composable
fun ProgramOutlinedButton(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit) {
    val colors = MaterialTheme.colorScheme
    OutlinedButton(onClick, modifier.heightIn(min = 48.dp), enabled, shape = CircleShape,
        border = BorderStroke(1.dp, if (enabled) colors.onSurface else colors.outlineVariant),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = colors.onSurface, disabledContentColor = colors.onSurface), content = content)
}
