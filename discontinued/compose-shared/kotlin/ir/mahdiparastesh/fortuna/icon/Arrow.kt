package ir.mahdiparastesh.fortuna.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val FortunaIcons.Arrow: ImageVector by lazy {
    ImageVector.Builder(
        name = "Arrow",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            pathFillType = NonZero
        ) {
            moveTo(7.0f, 10.0f)
            lineToRelative(5.0f, 5.0f)
            lineToRelative(5.0f, -5.0f)
            close()
        }
    }.build()
}
