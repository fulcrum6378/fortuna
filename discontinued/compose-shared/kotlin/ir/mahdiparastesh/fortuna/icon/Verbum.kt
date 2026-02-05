package ir.mahdiparastesh.fortuna.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val FortunaIcons.Verbum: ImageVector by lazy {
    ImageVector.Builder(
        name = "Verbum",
        defaultWidth = 16.dp,
        defaultHeight = 16.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            pathFillType = NonZero
        ) {
            moveTo(22.0f, 2.0f)
            lineTo(2.01f, 2.0f)
            lineTo(2.0f, 22.0f)
            lineToRelative(4.0f, -4.0f)
            horizontalLineToRelative(16.0f)
            lineTo(22.0f, 2.0f)
            close()

            moveTo(6.0f, 9.0f)
            horizontalLineToRelative(12.0f)
            verticalLineToRelative(2.0f)
            lineTo(6.0f, 11.0f)
            lineTo(6.0f, 9.0f)
            close()

            moveTo(14.0f, 14.0f)
            lineTo(6.0f, 14.0f)
            verticalLineToRelative(-2.0f)
            horizontalLineToRelative(8.0f)
            verticalLineToRelative(2.0f)
            close()

            moveTo(18.0f, 8.0f)
            lineTo(6.0f, 8.0f)
            lineTo(6.0f, 6.0f)
            horizontalLineToRelative(12.0f)
            verticalLineToRelative(2.0f)
            close()
        }
    }.build()
}
