package ir.mahdiparastesh.fortuna.util

import androidx.annotation.IdRes
import androidx.annotation.StringRes
import ir.mahdiparastesh.fortuna.R
import kotlin.reflect.KClass

/**
 * Ancient Numeral Systems
 *
 * @see <a href="https://en.wikipedia.org/wiki/List_of_numeral_systems">More on Wikipedia</a>
 */
object Numerals {

    /** List of all the supported numeral systems */
    val all = arrayOf(
        NumeralType(null, R.string.numArabic, R.id.numArabic),
        NumeralType(
            RomanNumeral::class, R.string.numRoman, R.id.numRoman
        ),  // -~0..+1400 preceded Arabic (sources claiming -900 mistake/mix it with Etruscan)
        NumeralType(
            BrahmiNumeral::class, R.string.numBrahmi, R.id.numBrahmi
        ),  // -300..+500 preceded Gupta script.
        NumeralType(
            KharosthiNumeral::class, R.string.numKharosthi, R.id.numKharosthi
        ),  // -400..+300 preceded Brahmi script.
        NumeralType(
            OldPersianNumeral::class, R.string.numOldPersian, R.id.numOldPersian
        ),  // -525..-330 annihilated! (presumably Avestan and Middle Persian had no numerals.)
        NumeralType(
            EtruscanNumeral::class, R.string.numEtruscan, R.id.numEtruscan
        ),  // -700..+50 inspired and was replaced by Roman numerals.
        NumeralType(
            AtticNumeral::class, R.string.numAttic, R.id.numAttic
        ),  // -700..-300 preceded Greek numerals.
        NumeralType(
            BabylonianNumeral::class, R.string.numBabylonian, R.id.numBabylonian,
        ),  // -2000 first appeared.
        /*NumeralType(
            HieroglyphNumeral::class, R.string.numHieroglyph, R.id.numHieroglyph, true
        ),*/  // -3200..+400 preceded Coptic script.
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
}

/**
 * Data class containing information about a [Numeral] type
 *
 * @param kClass Kotlin class
 * @param name string resource for its visible name
 * @param id its unique ID resource used in various places
 */
data class NumeralType(
    val kClass: KClass<*>?,
    @StringRes val name: Int,
    @IdRes val id: Int,
) {
    fun name(): String? = kClass?.java?.simpleName
}
