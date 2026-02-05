package ir.mahdiparastesh.fortuna.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.EvenOdd
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val FortunaIcons.Import: ImageVector by lazy {
    ImageVector.Builder(
        name = "Import",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 16.75f,
        viewportHeight = 16.75f,
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            pathFillType = EvenOdd
        ) {
            moveToRelative(11.905f, 7.22f)
            curveToRelative(-0.293f, -0.293f, -0.768f, -0.293f, -1.061f, 0.0f)
            lineTo(9.125f, 8.939f)
            verticalLineTo(0.75f)
            curveTo(9.125f, 0.336f, 8.789f, 0.0f, 8.375f, 0.0f)
            curveTo(7.961f, 0.0f, 7.625f, 0.336f, 7.625f, 0.75f)
            verticalLineTo(8.939f)
            lineTo(5.905f, 7.22f)
            curveToRelative(-0.293f, -0.293f, -0.768f, -0.293f, -1.061f, 0.0f)
            curveToRelative(-0.293f, 0.293f, -0.293f, 0.768f, 0.0f, 1.061f)
            lineToRelative(3.0f, 3.0f)
            curveToRelative(0.293f, 0.293f, 0.768f, 0.293f, 1.061f, 0.0f)
            lineToRelative(3.0f, -3.0f)
            curveToRelative(0.293f, -0.293f, 0.293f, -0.768f, 0.0f, -1.061f)
            close()
        }
        path(
            fill = SolidColor(Color.Black),
            pathFillType = NonZero
        ) {
            moveToRelative(14.123f, 8.75f)
            curveToRelative(-0.448f, 0.0f, -0.84f, 0.274f, -1.157f, 0.591f)
            lineToRelative(-3.0f, 3.0f)
            curveToRelative(-0.879f, 0.879f, -2.303f, 0.879f, -3.182f, 0.0f)
            lineToRelative(-3.0f, -3.0f)
            curveTo(3.467f, 9.024f, 3.075f, 8.75f, 2.627f, 8.75f)
            horizontalLineTo(0.375f)
            curveToRelative(0.0f, 4.418f, 3.582f, 8.0f, 8.0f, 8.0f)
            curveToRelative(4.418f, 0.0f, 8.0f, -3.582f, 8.0f, -8.0f)
            close()
        }
    }.build()
}
