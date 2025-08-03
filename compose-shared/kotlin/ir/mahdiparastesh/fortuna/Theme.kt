package ir.mahdiparastesh.fortuna

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object Theme {

    lateinit var palette: Palette
    lateinit var geometry: Geometry


    fun init(
        isSystemInDarkTheme: Boolean,
    ) {
        pickPalette(isSystemInDarkTheme)
        pickGeometry()
    }

    private fun pickPalette(isSystemInDarkTheme: Boolean) {
        palette = if (!isSystemInDarkTheme)
            Palette(
                Color(0xFF4CAF50),
                Color(0xFFF44336),
                Color.White,
                Color(0xFFFEF7FF),  // applied directly in themes.xml
                Color(0xFF777777),
                Color(0xFFF9F9F7),
                Color(0xFF000000),
                Color(0xFFF0F5EF),
                Color(0xFFECF1EB),
            )
        else
            Palette(
                Color(0xFF034C06),
                Color(0xFF670D06),
                Color.White,
                Color(0xFF141118),
                Color.White,
                Color(0xFF313133),
                Color(0xFFFFFFFF),
                Color(0xFF1A1E1D),
                Color(0xFF191F1B),
            )
    }

    fun pickGeometry() {
        geometry =
            Geometry(
                toolbarHeight = 60.dp,
                appTitle = 28.sp,
                dialogTitle = 25.sp,

                thisMonthName = 21.sp,
                thisYearNumber = 19.sp,
                thisMonthScore = 18.sp,
                panelBottomTexts = 14.sp,
            )
        // TODO adjust laptops and tablets
    }

    data class Palette(
        val themePleasure: Color,
        val themePain: Color,
        val onTheme: Color,
        val window: Color,
        val onWindow: Color,
        val variabilisField: Color,
        val onVariabilisField: Color,
        val popup: Color,
        val dialog: Color,
    )

    data class Geometry(
        val toolbarHeight: Dp,
        val appTitle: TextUnit,
        val dialogTitle: TextUnit,

        val thisMonthName: TextUnit,
        val thisYearNumber: TextUnit,
        val thisMonthScore: TextUnit,
        val panelBottomTexts: TextUnit,
    )
}
