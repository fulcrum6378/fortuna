package ir.mahdiparastesh.fortuna.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val FortunaIcons.Today: ImageVector by lazy {
    ImageVector.Builder(
        name = "Today",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            pathFillType = NonZero
        ) {
            moveTo(19.0f, 3.0f)
            horizontalLineToRelative(-1.0f)
            lineTo(18.0f, 1.0f)
            horizontalLineToRelative(-2.0f)
            verticalLineToRelative(2.0f)
            lineTo(8.0f, 3.0f)
            lineTo(8.0f, 1.0f)
            lineTo(6.0f, 1.0f)
            verticalLineToRelative(2.0f)
            lineTo(5.0f, 3.0f)
            curveToRelative(-1.11f, 0.0f, -1.99f, 0.9f, -1.99f, 2.0f)
            lineTo(3.0f, 19.0f)
            curveToRelative(0.0f, 1.1f, 0.89f, 2.0f, 2.0f, 2.0f)
            horizontalLineToRelative(14.0f)
            curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
            lineTo(21.0f, 5.0f)
            curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
            close()

            moveTo(19.0f, 19.0f)
            lineTo(5.0f, 19.0f)
            lineTo(5.0f, 8.0f)
            horizontalLineToRelative(14.0f)
            verticalLineToRelative(11.0f)
            close()

            moveTo(7.0f, 10.0f)
            horizontalLineToRelative(5.0f)
            verticalLineToRelative(5.0f)
            lineTo(7.0f, 15.0f)
            close()
        }
    }.build()
}
