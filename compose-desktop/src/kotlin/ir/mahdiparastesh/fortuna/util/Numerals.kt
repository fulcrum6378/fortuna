package ir.mahdiparastesh.fortuna.util

import ir.mahdiparastesh.fortuna.R
import ir.mahdiparastesh.fortuna.numArabic
import ir.mahdiparastesh.fortuna.numAttic
import ir.mahdiparastesh.fortuna.numBabylonian
import ir.mahdiparastesh.fortuna.numBrahmi
import ir.mahdiparastesh.fortuna.numEtruscan
import ir.mahdiparastesh.fortuna.numKharosthi
import ir.mahdiparastesh.fortuna.numOldPersian
import ir.mahdiparastesh.fortuna.numRoman
import org.jetbrains.compose.resources.StringResource
import kotlin.reflect.KClass

/** Ancient Numeral Systems */
object Numerals {

    /** List of all the supported numeral systems */
    val all = arrayOf(
        NumeralType(null, R.string.numArabic),
        NumeralType(
            RomanNumeral::class, R.string.numRoman
        ),
        NumeralType(
            BrahmiNumeral::class, R.string.numBrahmi
        ),
        NumeralType(
            KharosthiNumeral::class, R.string.numKharosthi
        ),
        NumeralType(
            OldPersianNumeral::class, R.string.numOldPersian
        ),
        NumeralType(
            EtruscanNumeral::class, R.string.numEtruscan
        ),
        NumeralType(
            AtticNumeral::class, R.string.numAttic
        ),
        NumeralType(
            BabylonianNumeral::class, R.string.numBabylonian
        ),
        /*NumeralType(
            HieroglyphNumeral::class, R.string.numHieroglyph, true
        ),*/
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
 */
data class NumeralType(
    val kClass: KClass<*>?,
    val name: StringResource,
) {
    fun name(): String? = kClass?.java?.simpleName
}
