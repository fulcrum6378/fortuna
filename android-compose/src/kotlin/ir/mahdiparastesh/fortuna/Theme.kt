package ir.mahdiparastesh.fortuna

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4CAF50),
    onPrimary = Color.White,
    secondary = Color(0xFFF44336),
    onSecondary = Color.White,
    surface = Color.White,
    onSurface = Color(0xFF777777),
    surfaceContainer = Color(0xFFF0F5EF),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF034C06),
    onPrimary = Color.White,
    secondary = Color(0xFF670D06),
    onSecondary = Color.White,
    surface = Color.Black,
    onSurface = Color.White,
    surfaceContainer = Color(0xFF1A1E1D),
)

private val FontFamilyMorrisRoman = FontFamily(
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
        // dies numeral
        fontSize = 17.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = FontFamilyQuattrocento,
    ),
    labelSmall = TextStyle(
        // dies score
        fontSize = 13.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = FontFamilyQuattrocento,
    ),
    /*TextStyle(
        TODO lineHeight = 16.sp,
        TODO letterSpacing = 0.5.sp
    )*/
)

private val Geometry = Shapes(
    large = CutCornerShape(14.dp),
    medium = CutCornerShape(10.dp),
)

@Composable
fun FortunaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = Geometry,
        typography = Typography,
        content = content
    )
}

@get:Composable
val CheckboxColorScheme: CheckboxColors
    get() = CheckboxDefaults.colors(
        checkedColor = MaterialTheme.colorScheme.onSurface,
        uncheckedColor = MaterialTheme.colorScheme.onSurface,
    )
