package ir.mahdiparastesh.fortuna.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val FortunaIcons.Backup: ImageVector by lazy {
    ImageVector.Builder(
        name = "Backup",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 32f,
        viewportHeight = 32f,
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            pathFillType = NonZero
        ) {
            moveTo(9.0f, 29.0f)
            lineToRelative(0.0f, -8.25f)
            curveToRelative(0.0f, -1.518f, 1.232f, -2.75f, 2.75f, -2.75f)
            lineToRelative(8.5f, 0.0f)
            curveToRelative(1.518f, 0.0f, 2.75f, 1.232f, 2.75f, 2.75f)
            lineToRelative(0.0f, 8.25f)
            lineToRelative(-14.0f, -0.0f)
            close()

            moveTo(7.0f, 28.899f)
            curveToRelative(-0.953f, -0.195f, -1.837f, -0.665f, -2.536f, -1.363f)
            curveToRelative(-0.937f, -0.938f, -1.464f, -2.21f, -1.464f, -3.536f)
            curveToRelative(-0.0f, -4.439f, -0.0f, -11.561f, 0.0f, -16.0f)
            curveToRelative(-0.0f, -1.326f, 0.527f, -2.598f, 1.464f, -3.536f)
            curveToRelative(0.938f, -0.937f, 2.21f, -1.464f, 3.536f, -1.464f)
            lineToRelative(2.0f, -0.0f)
            lineToRelative(0.0f, 5.083f)
            curveToRelative(0.0f, 2.201f, 1.613f, 3.917f, 3.5f, 3.917f)
            lineToRelative(5.0f, 0.0f)
            curveToRelative(1.887f, 0.0f, 3.5f, -1.716f, 3.5f, -3.917f)
            lineToRelative(0.0f, -5.083f)
            lineToRelative(0.221f, 0.0f)
            curveToRelative(0.24f, 0.0f, 0.472f, 0.087f, 0.654f, 0.244f)
            lineToRelative(5.779f, 5.0f)
            curveToRelative(0.22f, 0.19f, 0.346f, 0.466f, 0.346f, 0.756f)
            curveToRelative(0.0f, 0.0f, 0.0f, 9.426f, -0.0f, 15.0f)
            curveToRelative(0.0f, 1.326f, -0.527f, 2.598f, -1.464f, 3.536f)
            curveToRelative(-0.699f, 0.698f, -1.583f, 1.168f, -2.536f, 1.363f)
            lineToRelative(0.0f, -8.149f)
            curveToRelative(0.0f, -2.622f, -2.128f, -4.75f, -4.75f, -4.75f)
            curveToRelative(0.0f, 0.0f, -8.5f, 0.0f, -8.5f, 0.0f)
            curveToRelative(-2.622f, 0.0f, -4.75f, 2.128f, -4.75f, 4.75f)
            lineToRelative(0.0f, 8.149f)
            close()

            moveTo(20.0f, 3.0f)
            lineToRelative(0.0f, 5.083f)
            curveToRelative(0.0f, 1.02f, -0.626f, 1.917f, -1.5f, 1.917f)
            curveToRelative(0.0f, 0.0f, -5.0f, 0.0f, -5.0f, 0.0f)
            curveToRelative(-0.874f, 0.0f, -1.5f, -0.897f, -1.5f, -1.917f)
            lineToRelative(0.0f, -5.083f)
            lineToRelative(8.0f, 0.0f)
            close()
        }
    }.build()
}
