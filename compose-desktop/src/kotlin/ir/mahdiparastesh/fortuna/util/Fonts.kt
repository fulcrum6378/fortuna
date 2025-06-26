package ir.mahdiparastesh.fortuna.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import ir.mahdiparastesh.fortuna.R
import ir.mahdiparastesh.fortuna.morris_roman_black
import ir.mahdiparastesh.fortuna.quattrocento_bold
import ir.mahdiparastesh.fortuna.quattrocento_regular
import org.jetbrains.compose.resources.Font

@get:Composable
val FontFamilyMorrisRoman
    get() = FontFamily(
        Font(R.font.morris_roman_black, weight = FontWeight.Bold),
    )

@get:Composable
val FontFamilyQuattrocento
    get() = FontFamily(
        Font(R.font.quattrocento_bold, weight = FontWeight.Bold),
        Font(R.font.quattrocento_regular, weight = FontWeight.Normal),
    )
