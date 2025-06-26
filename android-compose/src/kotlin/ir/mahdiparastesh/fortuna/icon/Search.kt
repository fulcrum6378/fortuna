package ir.mahdiparastesh.fortuna.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val FortunaIcons.Search: ImageVector by lazy {
    ImageVector.Builder(
        name = "Search",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 32f,
        viewportHeight = 32f,
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            pathFillType = NonZero
        ) {
            moveTo(13.0f, 9.0f)
            curveToRelative(4.967f, 0.0f, 9.0f, 4.033f, 9.0f, 9.0f)
            curveToRelative(0.0f, 4.642f, -3.523f, 8.469f, -8.038f, 8.949f)
            curveToRelative(0.91f, 1.244f, 2.379f, 2.051f, 4.038f, 2.051f)
            lineToRelative(5.967f, 0.0f)
            curveToRelative(2.758f, 0.0f, 4.995f, -2.233f, 4.999f, -4.992f)
            lineToRelative(0.027f, -16.0f)
            curveToRelative(0.003f, -1.327f, -0.523f, -2.601f, -1.461f, -3.541f)
            curveToRelative(-0.938f, -0.939f, -2.211f, -1.467f, -3.539f, -1.467f)
            curveToRelative(-1.879f, 0.0f, -4.114f, 0.0f, -5.993f, 0.0f)
            curveToRelative(-2.761f, 0.0f, -5.0f, 2.239f, -5.0f, 5.0f)
            lineToRelative(0.0f, 1.0f)
            close()
        }
        path(
            fill = SolidColor(Color.Black),
            pathFillType = NonZero
        ) {
            moveTo(7.522f, 22.356f)
            lineToRelative(-4.583f, 4.583f)
            curveToRelative(-0.585f, 0.586f, -0.585f, 1.536f, 0.0f, 2.122f)
            curveToRelative(0.586f, 0.585f, 1.536f, 0.585f, 2.122f, -0.0f)
            lineToRelative(4.802f, -4.803f)
            curveToRelative(0.943f, 0.475f, 2.009f, 0.742f, 3.137f, 0.742f)
            curveToRelative(3.863f, 0.0f, 7.0f, -3.137f, 7.0f, -7.0f)
            curveToRelative(0.0f, -3.863f, -3.137f, -7.0f, -7.0f, -7.0f)
            curveToRelative(-3.863f, 0.0f, -7.0f, 3.137f, -7.0f, 7.0f)
            curveToRelative(0.0f, 1.646f, 0.569f, 3.16f, 1.522f, 4.356f)
            close()
        }
    }.build()
}
