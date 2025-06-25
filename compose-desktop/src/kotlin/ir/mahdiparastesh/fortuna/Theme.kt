package ir.mahdiparastesh.fortuna

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4CAF50),
    onPrimary = Color.White,
    secondary = Color(0xFFF44336),
    onSecondary = Color.White,
    surface = Color(0xFFFEF7FF),  // applied directly in themes.xml
    onSurface = Color(0xFF777777),
    onSurfaceVariant = Color(0xFF000000),  // texts in variabilis input fields
    surfaceBright = Color(0xFFF9F9F7),  // variabilis input fields
    surfaceContainer = Color(0xFFF0F5EF),  // popup menus
    surfaceContainerHigh = Color(0xFFECF1EB),  // dialog boxes
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF034C06),
    onPrimary = Color.White,
    secondary = Color(0xFF670D06),
    onSecondary = Color.White,
    surface = Color(0xFF141118),
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFFFFFFF),
    surfaceBright = Color(0xFF313133),
    surfaceContainer = Color(0xFF1A1E1D),
    surfaceContainerHigh = Color(0xFF191F1B),
)

/*private val FontFamilyMorrisRoman = FontFamily(
    Font(R.font.morris_roman_black, weight = FontWeight.Bold),
)

private val FontFamilyQuattrocento = FontFamily(
    Font(R.font.quattrocento_bold, weight = FontWeight.Bold),
    Font(R.font.quattrocento_regular, weight = FontWeight.Normal),
)

private val Typography = Typography(
    displayLarge = TextStyle(
        // Fortuna title in the TopAppBar
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamilyMorrisRoman,
    ),
    displaySmall = TextStyle(
        // dialog title
        fontSize = 25.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = FontFamilyQuattrocento,
    ),
    titleLarge = TextStyle(
        // luna selector
        fontSize = 21.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamilyQuattrocento,
    ),
    titleSmall = TextStyle(
        // annus field
        fontSize = 19.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamilyQuattrocento,
        textAlign = TextAlign.Center,
    ),
    bodyLarge = TextStyle(
        // dies numeral
        fontSize = 18.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = FontFamilyQuattrocento,
    ),
    bodyMedium = TextStyle(
        // dies numeral, verbum
        fontSize = 17.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = FontFamilyQuattrocento,
        lineHeight = 26.sp,
    ),
    labelSmall = TextStyle(
        // dies score
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = FontFamilyQuattrocento,
    ),
    /*TextStyle(
        TODO letterSpacing = 0.5.sp
    )*/
)*/

private val Geometry = Shapes(
    large = CutCornerShape(14.dp),
    medium = CutCornerShape(10.dp),
)

@Composable
fun FortunaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = Geometry,
        //typography = Typography,
        content = content
    )
}

@get:Composable
val CheckboxColorScheme: CheckboxColors
    get() = CheckboxDefaults.colors(
        checkedColor = MaterialTheme.colorScheme.onSurface,
        uncheckedColor = MaterialTheme.colorScheme.onSurface,
    )
