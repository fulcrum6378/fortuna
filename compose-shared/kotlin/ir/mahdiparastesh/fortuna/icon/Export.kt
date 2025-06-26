package ir.mahdiparastesh.fortuna.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.EvenOdd
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val FortunaIcons.Export: ImageVector by lazy {
    ImageVector.Builder(
        name = "Export",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 16.75f,
        viewportHeight = 16.75f,
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            pathFillType = EvenOdd
        ) {
            moveToRelative(4.845f, 4.28f)
            curveToRelative(0.293f, 0.293f, 0.768f, 0.293f, 1.061f, 0.0f)
            lineTo(7.625f, 2.561f)
            verticalLineToRelative(8.189f)
            curveToRelative(0.0f, 0.414f, 0.336f, 0.75f, 0.75f, 0.75f)
            curveToRelative(0.414f, 0.0f, 0.75f, -0.336f, 0.75f, -0.75f)
            verticalLineTo(2.561f)
            lineToRelative(1.72f, 1.72f)
            curveToRelative(0.293f, 0.293f, 0.768f, 0.293f, 1.061f, 0.0f)
            curveToRelative(0.293f, -0.293f, 0.293f, -0.768f, 0.0f, -1.061f)
            lineToRelative(-3.0f, -3.0f)
            curveToRelative(-0.293f, -0.293f, -0.768f, -0.293f, -1.061f, 0.0f)
            lineToRelative(-3.0f, 3.0f)
            curveToRelative(-0.293f, 0.293f, -0.293f, 0.768f, 0.0f, 1.061f)
            close()
        }
        path(
            fill = SolidColor(Color.Black),
            pathFillType = NonZero
        ) {
            moveToRelative(8.375f, 16.75f)
            curveToRelative(4.418f, 0.0f, 8.0f, -3.582f, 8.0f, -8.0f)
            horizontalLineToRelative(-3.75f)
            curveToRelative(-0.943f, 0.0f, -1.414f, 0.0f, -1.707f, 0.293f)
            curveToRelative(-0.293f, 0.293f, -0.293f, 0.764f, -0.293f, 1.707f)
            curveToRelative(0.0f, 1.243f, -1.007f, 2.25f, -2.25f, 2.25f)
            curveToRelative(-1.243f, 0.0f, -2.25f, -1.007f, -2.25f, -2.25f)
            curveToRelative(0.0f, -0.943f, 0.0f, -1.414f, -0.293f, -1.707f)
            curveToRelative(-0.293f, -0.293f, -0.764f, -0.293f, -1.707f, -0.293f)
            horizontalLineTo(0.375f)
            curveToRelative(0.0f, 4.418f, 3.582f, 8.0f, 8.0f, 8.0f)
            close()
        }
    }.build()
}
