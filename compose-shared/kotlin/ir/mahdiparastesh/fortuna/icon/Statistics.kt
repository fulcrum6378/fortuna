package ir.mahdiparastesh.fortuna.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val FortunaIcons.Statistics: ImageVector by lazy {
    ImageVector.Builder(
        name = "Statistics",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 32f,
        viewportHeight = 32f,
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            pathFillType = NonZero
        ) {
            moveTo(29.0f, 23.0f)
            lineToRelative(-8.0f, 0.0f)
            lineToRelative(0.0f, 6.0f)
            lineToRelative(3.0f, 0.0f)
            curveToRelative(1.326f, 0.0f, 2.598f, -0.527f, 3.536f, -1.464f)
            curveToRelative(0.937f, -0.938f, 1.464f, -2.21f, 1.464f, -3.536f)
            lineToRelative(-0.0f, -1.0f)
            close()

            moveTo(19.0f, 29.0f)
            lineToRelative(-6.0f, -0.0f)
            lineToRelative(0.0f, -6.0f)
            lineToRelative(6.0f, 0.0f)
            lineToRelative(-0.0f, 6.0f)
            close()

            moveTo(3.0f, 23.0f)
            lineToRelative(0.0f, 1.0f)
            curveToRelative(-0.0f, 1.326f, 0.527f, 2.598f, 1.464f, 3.536f)
            curveToRelative(0.938f, 0.937f, 2.21f, 1.464f, 3.536f, 1.464f)
            lineToRelative(3.0f, -0.0f)
            lineToRelative(0.0f, -6.0f)
            lineToRelative(-8.0f, 0.0f)
            close()

            moveTo(11.0f, 17.0f)
            lineToRelative(0.0f, 4.0f)
            lineToRelative(-8.0f, -0.0f)
            lineToRelative(0.0f, -4.0f)
            lineToRelative(8.0f, 0.0f)
            close()

            moveTo(19.0f, 17.0f)
            lineToRelative(-0.0f, 4.0f)
            lineToRelative(-6.0f, -0.0f)
            lineToRelative(0.0f, -4.0f)
            lineToRelative(6.0f, 0.0f)
            close()

            moveTo(29.0f, 17.0f)
            lineToRelative(-0.0f, 4.0f)
            lineToRelative(-8.0f, -0.0f)
            lineToRelative(0.0f, -4.0f)
            lineToRelative(8.0f, 0.0f)
            close()

            moveTo(11.0f, 15.0f)
            lineToRelative(-8.0f, -0.0f)
            lineToRelative(0.0f, -4.0f)
            lineToRelative(8.0f, 0.0f)
            lineToRelative(0.0f, 4.0f)
            close()

            moveTo(19.0f, 15.0f)
            lineToRelative(-6.0f, -0.0f)
            lineToRelative(0.0f, -4.0f)
            lineToRelative(6.0f, 0.0f)
            lineToRelative(-0.0f, 4.0f)
            close()

            moveTo(29.0f, 15.0f)
            lineToRelative(-8.0f, -0.0f)
            lineToRelative(0.0f, -4.0f)
            lineToRelative(8.0f, 0.0f)
            lineToRelative(-0.0f, 4.0f)
            close()

            moveTo(3.0f, 9.0f)
            lineToRelative(26.0f, 0.0f)
            lineToRelative(-0.0f, -1.0f)
            curveToRelative(0.0f, -1.326f, -0.527f, -2.598f, -1.464f, -3.536f)
            curveToRelative(-0.938f, -0.937f, -2.21f, -1.464f, -3.536f, -1.464f)
            curveToRelative(-4.439f, -0.0f, -11.561f, -0.0f, -16.0f, -0.0f)
            curveToRelative(-1.326f, -0.0f, -2.598f, 0.527f, -3.536f, 1.464f)
            curveToRelative(-0.937f, 0.938f, -1.464f, 2.21f, -1.464f, 3.536f)
            lineToRelative(0.0f, 1.0f)
            close()
        }
    }.build()
}
