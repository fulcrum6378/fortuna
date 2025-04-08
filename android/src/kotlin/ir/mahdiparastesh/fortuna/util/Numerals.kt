package ir.mahdiparastesh.fortuna.util

import androidx.annotation.IdRes
import androidx.annotation.StringRes
import ir.mahdiparastesh.fortuna.R

/**
 * Ancient Numeral Systems
 *
 * @see <a href="https://en.wikipedia.org/wiki/List_of_numeral_systems">More on Wikipedia</a>
 */
object Numerals {

    /** List of all the supported ancient numeral systems */
    val all = arrayOf(
        NumeralType(null, R.string.numArabic, R.id.numArabic),
        NumeralType(
            RomanNumeral::class.java, R.string.numRoman, R.id.numRoman
        ), // -~0..+1400 preceded Arabic (sources claiming -900 mistake/mix it with Etruscan)
        NumeralType(
            BrahmiNumeral::class.java, R.string.numBrahmi, R.id.numBrahmi
        ), // -300..+500 preceded Gupta script.
        NumeralType(
            OldPersianNumeral::class.java, R.string.numOldPersian, R.id.numOldPersian
        ), // -525..-330 annihilated! (presumably Avestan and Middle Persian had no numerals.)
        NumeralType(
            EtruscanNumeral::class.java, R.string.numEtruscan, R.id.numEtruscan
        ), // -700..+50 inspired and was replaced by Roman numerals.
        NumeralType(
            AtticNumeral::class.java, R.string.numAttic, R.id.numAttic
        ), // -700..-300 preceded Greek numerals.
        NumeralType(
            HieroglyphNumeral::class.java, R.string.numHieroglyph, R.id.numHieroglyph, true
        ), // -3200..+400 preceded Coptic script.
    )

    /** Convert a modern number into ancient numerals! */
    fun build(numType: String?): Numeral? = numType
        ?.let {
            try {
                Class.forName(this::class.java.`package`!!.name + "." + it)
            } catch (_: ClassNotFoundException) {
                null
            }
        }
        ?.getDeclaredConstructor()?.newInstance() as Numeral?

    fun Numeral?.write(i: Int) = this?.output(i) ?: i.toString()
}

/**
 * Data class containing information about a Numeral type
 *
 * @param jClass Java class
 * @param name string resource for its visible name
 * @param id its unique ID resource used in various places
 * @param enlarge Are its characters complicated and require enlarging, like the hieroglyphs?
 */
data class NumeralType(
    val jClass: Class<*>?, @StringRes val name: Int, @IdRes val id: Int,
    val enlarge: Boolean = false
)
