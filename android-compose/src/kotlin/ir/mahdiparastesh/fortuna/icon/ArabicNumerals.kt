package ir.mahdiparastesh.fortuna.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val FortunaIcons.ArabicNumerals: ImageVector by lazy {
    ImageVector.Builder(
        name = "ArabicNumerals",
        defaultWidth = 36.dp,
        defaultHeight = 36.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            pathFillType = NonZero
        ) {
            moveTo(7.0f, 15.0f)
            horizontalLineTo(5.5f)
            verticalLineToRelative(-4.5f)
            horizontalLineTo(4.0f)
            verticalLineTo(9.0f)
            horizontalLineToRelative(3.0f)
            verticalLineTo(15.0f)
            close()

            moveTo(13.5f, 13.5f)
            horizontalLineToRelative(-3.0f)
            verticalLineToRelative(-1.0f)
            horizontalLineToRelative(2.0f)
            curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
            verticalLineTo(10.0f)
            curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
            horizontalLineTo(9.0f)
            verticalLineToRelative(1.5f)
            horizontalLineToRelative(3.0f)
            verticalLineToRelative(1.0f)
            horizontalLineToRelative(-2.0f)
            curveToRelative(-0.55f, 0.0f, -1.0f, 0.45f, -1.0f, 1.0f)
            verticalLineTo(15.0f)
            horizontalLineToRelative(4.5f)
            verticalLineTo(13.5f)
            close()

            moveTo(19.5f, 14.0f)
            verticalLineToRelative(-4.0f)
            curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
            horizontalLineTo(15.0f)
            verticalLineToRelative(1.5f)
            horizontalLineToRelative(3.0f)
            verticalLineToRelative(1.0f)
            horizontalLineToRelative(-2.0f)
            verticalLineToRelative(1.0f)
            horizontalLineToRelative(2.0f)
            verticalLineToRelative(1.0f)
            horizontalLineToRelative(-3.0f)
            verticalLineTo(15.0f)
            horizontalLineToRelative(3.5f)
            curveTo(19.05f, 15.0f, 19.5f, 14.55f, 19.5f, 14.0f)
            close()
        }
    }.build()
}
