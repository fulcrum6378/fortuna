package ir.mahdiparastesh.fortuna.util

import kotlin.math.pow

/**
 * Base class for implementing ancient numeral systems
 *
 * @see <a href="https://en.wikipedia.org/wiki/List_of_numeral_systems">More on Wikipedia</a>
 */
abstract class Numeral {

    abstract val chars: Array<String>
    open val forceRtl: Boolean = false
    open val defaultStr: String = "NaN"
    open val zero: String? = null
    open val minus: String? = null

    private val sb = StringBuilder()

    fun output(num: Int): String =
        if ((num > 0 || zero != null) && (num >= 0 || minus != null)) try {
            sb.clear()
            convert(num)
            sb.toString().let { if (!forceRtl) it else it.reversed() }
        } catch (_: Exception) {
            defaultStr
        } else defaultStr

    /** Converts a number into an ancient numeral sequence and writes them using [write]. */
    protected abstract fun convert(num: Int)

    protected fun write(s: String) {
        sb.append(s)
    }
}

/**
 * Base class for systems which resemble the Attic system, like Roman and Etruscan
 */
abstract class AtticBasedNumeral(private val subtract4th: Boolean) : Numeral() {

    override fun convert(num: Int) {
        val n: String = num.toString()
        val ln = n.length
        for (ii in n.indices) {
            val i = n[ii].digitToInt()
            val base = chars[((ln - ii) - 1) * 2]
            val half = chars[(((ln - ii) - 1) * 2) + 1]
            when {
                i in 0..3 || (subtract4th && i == 4) ->
                    write(base.repeat(i))

                !subtract4th && i == 4 ->
                    write(base + half)

                i in 5..8 || (subtract4th && i == 9) ->
                    write(half + (base.repeat(i - 5)))

                !subtract4th && i == 9 ->
                    write(base + chars[(((ln - ii) - 1) * 2) + 2])
            }
        }
    }
}

/** @see <a href="https://en.wikipedia.org/wiki/Attic_numerals">Wikipedia</a> */
class AtticNumeral : AtticBasedNumeral(true) {
    override val chars = arrayOf(
        "I", "\ud800\udd43", // 1, 5
        "Δ", "\ud800\udd44", // 10, 50
        "Η", "\ud800\udd45", // 100, 500
        "Χ", "\ud800\udd46", // 1,000, 5,000
        "M", "\ud800\udd47"  // 10,000, 50,000
    )
    // https://charbase.com/10144-unicode-greek-acrophonic-attic-fifty
    // https://unicode-table.com/en/blocks/ancient-greek-numbers/
}

/** @see <a href="https://en.wikipedia.org/wiki/Etruscan_numerals">Wikipedia</a> */
class EtruscanNumeral : AtticBasedNumeral(true) {
    override val forceRtl = true
    override val chars = arrayOf(
        "\uD800\udf20", "\uD800\uDF21", "\uD800\uDF22", "\uD800\uDF23", "\uD800\uDF1F"
    )
}

/** @see <a href="https://en.wikipedia.org/wiki/Roman_numerals">Wikipedia</a> */
class RomanNumeral : AtticBasedNumeral(false) {
    override val chars = arrayOf(
        "I", "V", "X", "L", "C", "D", "M",
        "I\u0305", "V\u0305", "X\u0305", "L\u0305", "C\u0305", "D\u0305", "M\u0305"
        // An over line on a Roman numeral means you are multiplying that Roman numeral by 1,000.
    )
}


/**
 * Base class for systems which resemble Gematria
 *
 * @see <a href="https://en.wikipedia.org/wiki/Gematria">Gematria, Wikipedia</a>
 */
abstract class GematriaLikeNumeral() : Numeral() {
    override fun convert(num: Int) {
        val n: String = num.toString()
        val ln = n.length
        for (ii in n.indices) {
            val i = n[ii].digitToInt()
            if (i == 0) continue
            write(chars[((((ln - ii) - 1) * 9) - 1) + i])
        }
    }
}

/** @see <a href="https://en.wikipedia.org/wiki/Egyptian_numerals">Wikipedia</a> */
@Suppress("unused")
class HieroglyphNumeral : GematriaLikeNumeral() {
    override val chars = arrayOf(
        // 1..9
        "\uD80C\uDFFA", "\uD80C\uDFFB", "\uD80C\uDFFC", "\uD80C\uDFFD", "\uD80C\uDFFE",
        "\uD80C\uDFFF", "\uD80D\uDC00", "\uD80D\uDC01", "\uD80D\uDC02",
        // 10..90
        "\uD80C\uDF86", "\uD80C\uDF8F", "\uD80C\uDF88", "\uD80C\uDF89", "\uD80C\uDF8A",
        "\uD80C\uDF8B", "\uD80C\uDF8C", "\uD80C\uDF8D", "\uD80C\uDF8E",
        // 100..900
        "\uD80C\uDF62", "\uD80C\uDF63", "\uD80C\uDF64", "\uD80C\uDF65", "\uD80C\uDF66",
        "\uD80C\uDF67", "\uD80C\uDF68", "\uD80C\uDF69", "\uD80C\uDF6A",
        // 1,000..9,000
        "\uD80C\uDDBC", "\uD80C\uDDBD", "\uD80C\uDDBE", "\uD80C\uDDBF", "\uD80C\uDDC0",
        "\uD80C\uDDC1", "\uD80C\uDDC2", "\uD80C\uDDC3", "\uD80C\uDDC4",
        // 10,000..100,000
        "\uD80C\uDCAD", "\uD80C\uDCAE", "\uD80C\uDCAF", "\uD80C\uDCB0", "\uD80C\uDCB1",
        "\uD80C\uDCB2", "\uD80C\uDCB3", "\uD80C\uDCB4", "\uD80C\uDCB5", "\uD80C\uDD90"
    )
    override val defaultStr = "\uD80C\uDC4F"
    // https://unicode-table.com/en/blocks/egyptian-hieroglyphs/
}

/** @see <a href="https://en.wikipedia.org/wiki/Brahmi_numerals">Wikipedia</a> */
class BrahmiNumeral : GematriaLikeNumeral() {
    override val chars = arrayOf(
        // 1..9
        "\uD804\uDC52", "\uD804\uDC53", "\uD804\uDC54", "\uD804\uDC55", "\uD804\uDC56",
        "\uD804\uDC57", "\uD804\uDC58", "\uD804\uDC59", "\uD804\uDC5A",
        // 10..90
        "\uD804\uDC5B", "\uD804\uDC5C", "\uD804\uDC5D", "\uD804\uDC5E", "\uD804\uDC5F",
        "\uD804\uDC60", "\uD804\uDC61", "\uD804\uDC62", "\uD804\uDC63",
        // 100 (the rest are not available in unicode!)
        "\uD804\uDC64"
        // except 1,000: "\uD804\uDC65" which is useless without the previous chars!
    )
    // https://unicode-table.com/en/blocks/brahmi/
    // Magadhi Prakrit was Mahavira and Buddha's language.
    // Presumably they used Brahmi numerals or maybe Kharosthi.
}

/**
 * The desired cuneiform characters specified in the main Wikipedia page are scattered across
 * multiple unicode regions!
 *
 * IMPORTANT NOTE: I replaced the actual `20` character with double `10` characters;
 * because the actual character is not supported by many devices (neither my laptop nor my phone)!
 * REPLACE IT with the actual character in the FUTURE.....
 *
 * I also replaced the `2` character with double `1` characters; because there is no `2` character
 * in the DISH group of the unicode. Nonetheless there is another group called "GESH2" which contain
 * the both characters `1` and `2` in a sequential order, but they look nothing like the picture
 * in Wikipedia. The `2` character in GESH2 was `\uD809\uDC16`.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Babylonian_cuneiform_numerals">
 *     Babylonian cuneiform numerals - Wikipedia</a>
 * @see <a href="https://en.wikipedia.org/wiki/Cuneiform_Numbers_and_Punctuation">
 *     Cuneiform Numbers and Punctuation - Wikipedia</a>
 * @see <a href="https://en.wikipedia.org/wiki/Cuneiform_(Unicode_block)">
 *     Cuneiform (Unicode block) - Wikipedia</a>
 *
 * @see <a href="https://unicode.org/charts/PDF/U12400.pdf">
 *     Cuneiform Numbers and Punctuation - Range: 12400–1247F - The Unicode Standard</a>
 * @see <a href="https://unicode.org/charts/PDF/U12000.pdf">
 *     Cuneiform - Range: 12000–123FF - The Unicode Standard</a>
 */
class BabylonianNumeral : GematriaLikeNumeral() {
    override val chars = arrayOf(
        // 1..9
        "\uD808\uDC79", "\uD808\uDC79\uD808\uDC79", "\uD809\uDC08",
        "\uD809\uDC3C", "\uD809\uDC0A", "\uD809\uDC0B",
        "\uD809\uDC42", "\uD809\uDC44", "\uD809\uDC46",
        // 10..90
        "\uD808\uDF0B", "\uD808\uDF0B\uD808\uDF0B", /* actual 20: \uD808\uDF99 */ "\uD808\uDF0D",
        "\uD809\uDC69", "\uD809\uDC6A", "\uD809\uDC6B",
        "\uD809\uDC6C", "\uD809\uDC6D", "\uD809\uDC6E",
    )
}


/**
 * Old Persian cuneiform numbers
 *
 * Apparently in some dialects, 6 and 7 (but not 8) are written like six-pack abs: \uD809\uDC1A
 * But we skipped it for the ease of both the writer and the reader!
 *
 * @see <a href="https://www.heritageinstitute.com/zoroastrianism/languages/oldPersian.htm">
 *     Heritage Institute - Old Persian</a>
 * @see <a href="https://www.omniglot.com/writing/opcuneiform.htm">
 *     Omniglot - Old Persian Cuneiform</a>
 * @see <a href="https://en.wikipedia.org/wiki/Old_Persian_(Unicode_block)">
 *     Old Persian (Unicode block) - Wikipedia</a>
 */
class OldPersianNumeral : Numeral() {
    override val chars = arrayOf(
        "\uD800\uDFD1", "\uD800\uDFD2",  // 1, 2
        "\uD800\uDFD3", "\uD800\uDFD4",  // 10, 20
        "\uD800\uDFD5"  // 100
    )

    override fun convert(num: Int) {
        var n = num
        while (n > 0) {
            var subVal = 0
            var subChar = 0
            for (ch in chars.indices) {
                val charVal = charToInt(ch)
                if (charVal > n) break
                subChar = ch
                subVal = charVal
            }
            n -= subVal
            write(chars[subChar])
        }
    }

    private fun charToInt(i: Int): Int {
        var ii = i.toDouble()
        if (i % 2 == 1) ii -= 1.0
        ii /= 2
        return 10.0.pow(ii).toInt() * (if (i % 2 == 0) 1 else 2)
    }
}


/**
 * Kharosthi is an RTL alphabet and Android will make it RTL automatically.
 *
 * - Minimum supported number: 1
 * - Maximum supported number: -unlimited-
 *
 * @see <a href="https://en.wikipedia.org/wiki/Kharosthi">Kharosthi - Wikipedia</a>
 */
class KharosthiNumeral : Numeral() {
    override val chars = arrayOf(
        // 1..4
        "\uD802\uDE40", "\uD802\uDE41", "\uD802\uDE42", "\uD802\uDE43",
        // 10, 20
        "\uD802\uDE44", "\uD802\uDE45",
        // 100
        "\uD802\uDE46",
        // 1000
        "\uD802\uDE47"
    )

    override fun convert(num: Int) {
        var n = num
        if (n >= 1000) describeSuperKiloNumber(n / 1000)
        if (n > 0) describeSubKiloNumber(n % 1000)
    }

    /** @param n must be greater than or equal to 1000 */
    private fun describeSuperKiloNumber(n: Int) {
        if (n >= 1000) {
            describeSuperKiloNumber(n / 1000)
            write(chars[7])
        } else
            describeSubKiloNumber(n)
    }

    /** @param n must be less than 1000 */
    private fun describeSubKiloNumber(n: Int) {
        if (n >= 1000) throw IllegalArgumentException("Sub-kilo numbers must be less than 1000.")
        var nn = n

        // solve hundreds (with preceding ones)
        if (nn >= 100) {
            if (nn >= 200) describeOnes(nn / 100)
            write(chars[6])
            nn %= 100
        }

        // solve tens (with repeating themselves)
        if (nn >= 10) {
            describeTens(nn / 10)
            nn %= 10
        }

        describeOnes(nn)
    }

    /** @param n must be less than 100 */
    private fun describeTens(n: Int) {
        if (n >= 100) throw IllegalArgumentException("Tens must be less than 100.")
        var nn = n
        while (nn > 0) {
            val subtract = when {
                nn >= 2 -> 2
                else /*nn == 1*/ -> 1
            }
            write(chars[subtract + 3])
            nn -= subtract
        }
    }

    /** @param n must be less than 10 */
    private fun describeOnes(n: Int) {
        if (n >= 10) throw IllegalArgumentException("Ones must be less than 10.")
        var nn = n
        while (nn > 0) {
            val subtract = when {
                nn >= 4 -> 4
                nn == 3 -> 3
                nn == 2 -> 2
                else /*nn == 1*/ -> 1
            }
            write(chars[subtract - 1])
            nn -= subtract
        }
    }
}


/*
 * Numerals to add in the future when the newer unicode versions are applied:
 * - Chinese Rod Numerals (so interesting!): https://en.wikipedia.org/wiki/Counting_rods
 * - Maya Numerals: https://en.wikipedia.org/wiki/Maya_numerals
 */
