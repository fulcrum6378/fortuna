package ir.mahdiparastesh.fortuna.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.EvenOdd
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val FortunaIcons.Help: ImageVector by lazy {
    ImageVector.Builder(
        name = "Help",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(
            fill = SolidColor(Color.Black),
            pathFillType = EvenOdd
        ) {
            moveTo(6.271f, 2.112f)
            curveTo(5.461f, 2.218f, 5.033f, 2.413f, 4.727f, 2.712f)
            curveTo(4.422f, 3.012f, 4.223f, 3.432f, 4.114f, 4.225f)
            curveTo(4.002f, 5.042f, 4.0f, 6.124f, 4.0f, 7.676f)
            verticalLineTo(16.244f)
            curveTo(4.389f, 15.978f, 4.827f, 15.776f, 5.299f, 15.652f)
            curveTo(5.827f, 15.513f, 6.443f, 15.513f, 7.346f, 15.514f)
            lineTo(20.0f, 15.514f)
            verticalLineTo(7.676f)
            curveTo(20.0f, 6.124f, 19.998f, 5.042f, 19.886f, 4.225f)
            curveTo(19.777f, 3.432f, 19.578f, 3.012f, 19.273f, 2.712f)
            curveTo(18.967f, 2.413f, 18.539f, 2.218f, 17.729f, 2.112f)
            curveTo(16.896f, 2.002f, 15.791f, 2.0f, 14.207f, 2.0f)
            horizontalLineTo(9.793f)
            curveTo(8.209f, 2.0f, 7.105f, 2.002f, 6.271f, 2.112f)
            close()

            moveTo(6.759f, 6.595f)
            curveTo(6.759f, 6.147f, 7.129f, 5.784f, 7.586f, 5.784f)
            horizontalLineTo(16.414f)
            curveTo(16.871f, 5.784f, 17.241f, 6.147f, 17.241f, 6.595f)
            curveTo(17.241f, 7.042f, 16.871f, 7.405f, 16.414f, 7.405f)
            horizontalLineTo(7.586f)
            curveTo(7.129f, 7.405f, 6.759f, 7.042f, 6.759f, 6.595f)
            close()

            moveTo(7.586f, 9.568f)
            curveTo(7.129f, 9.568f, 6.759f, 9.931f, 6.759f, 10.378f)
            curveTo(6.759f, 10.826f, 7.129f, 11.189f, 7.586f, 11.189f)
            horizontalLineTo(13.103f)
            curveTo(13.561f, 11.189f, 13.931f, 10.826f, 13.931f, 10.378f)
            curveTo(13.931f, 9.931f, 13.561f, 9.568f, 13.103f, 9.568f)
            horizontalLineTo(7.586f)
            close()
        }
        path(
            fill = SolidColor(Color.Black),
            pathFillType = NonZero
        ) {
            moveTo(7.473f, 17.135f)
            horizontalLineTo(8.69f)
            horizontalLineTo(13.103f)
            horizontalLineTo(19.999f)
            curveTo(19.996f, 18.266f, 19.978f, 19.109f, 19.886f, 19.775f)
            curveTo(19.777f, 20.568f, 19.578f, 20.988f, 19.273f, 21.288f)
            curveTo(18.967f, 21.587f, 18.539f, 21.782f, 17.729f, 21.889f)
            curveTo(16.896f, 21.998f, 15.791f, 22.0f, 14.207f, 22.0f)
            horizontalLineTo(9.793f)
            curveTo(8.209f, 22.0f, 7.105f, 21.998f, 6.271f, 21.889f)
            curveTo(5.461f, 21.782f, 5.033f, 21.587f, 4.727f, 21.288f)
            curveTo(4.422f, 20.988f, 4.223f, 20.568f, 4.114f, 19.775f)
            curveTo(4.073f, 19.475f, 4.046f, 19.138f, 4.03f, 18.756f)
            curveTo(4.301f, 18.004f, 4.934f, 17.426f, 5.727f, 17.218f)
            curveTo(6.017f, 17.142f, 6.394f, 17.135f, 7.473f, 17.135f)
            close()
        }
    }.build()
}
