package ir.mahdiparastesh.fortuna.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val FortunaIcons.Menu: ImageVector by lazy {
    ImageVector.Builder(
        name = "Menu",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            pathFillType = NonZero
        ) {
            moveTo(3.0f, 18.0f)
            horizontalLineToRelative(18.0f)
            verticalLineToRelative(-2.0f)
            lineTo(3.0f, 16.0f)
            verticalLineToRelative(2.0f)
            close()

            moveTo(3.0f, 13.0f)
            horizontalLineToRelative(18.0f)
            verticalLineToRelative(-2.0f)
            lineTo(3.0f, 11.0f)
            verticalLineToRelative(2.0f)
            close()

            moveTo(3.0f, 6.0f)
            verticalLineToRelative(2.0f)
            horizontalLineToRelative(18.0f)
            lineTo(21.0f, 6.0f)
            lineTo(3.0f, 6.0f)
            close()
        }
    }.build()
}
