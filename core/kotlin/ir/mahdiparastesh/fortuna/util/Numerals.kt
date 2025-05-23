package ir.mahdiparastesh.fortuna.util

import kotlin.math.pow

/**
 * Base class for implementing ancient numeral systems
 *
 * @see <a href="https://en.wikipedia.org/wiki/List_of_numeral_systems">More on Wikipedia</a>
 */
abstract class Numeral(
    private val zero: String? = null, private val minus: String? = null
) {
    abstract val chars: Array<String>
    open val defaultStr: String = "NaN"

    fun output(num: Int): String =
        if ((num > 0 || zero != null) && (num >= 0 || minus != null)) try {
            convert(num)
        } catch (_: Exception) {
            defaultStr
        } else defaultStr

    protected abstract fun convert(num: Int): String
}

/**
 * Base class for systems which resemble the Attic system, like Roman and Etruscan
 */
abstract class AtticBasedNumeral : Numeral() {
    abstract val subtract4th: Boolean
    open val rtl: Boolean = false

    override fun convert(num: Int): String {
        val n: String = num.toString()
        val ln = n.length
        val s = StringBuilder()
        for (ii in n.indices) {
            val i = n[ii].digitToInt()
            val base = chars[((ln - ii) - 1) * 2]
            val half = chars[(((ln - ii) - 1) * 2) + 1]
            when {
                i in 0..3 || (subtract4th && i == 4) ->
                    s.append(base.repeat(i))

                !subtract4th && i == 4 ->
                    s.append(base + half)

                i in 5..8 || (subtract4th && i == 9) ->
                    s.append(half + (base.repeat(i - 5)))

                !subtract4th && i == 9 ->
                    s.append(base + chars[(((ln - ii) - 1) * 2) + 2])
            }
        }
        var ret = s.toString()
        if (rtl) ret = ret.reversed()
        return ret
    }
}

/** @see <a href="https://en.wikipedia.org/wiki/Attic_numerals">Wikipedia</a> */
class AtticNumeral : AtticBasedNumeral() {
    override val subtract4th = true
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
class EtruscanNumeral : AtticBasedNumeral() {
    override val subtract4th = true
    override val rtl = true
    override val chars = arrayOf(
        "\uD800\udf20", "\uD800\uDF21", "\uD800\uDF22", "\uD800\uDF23", "\uD800\uDF1F"
    )
}

/** @see <a href="https://en.wikipedia.org/wiki/Roman_numerals">Wikipedia</a> */
class RomanNumeral : AtticBasedNumeral() {
    override val subtract4th = false
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
abstract class GematriaLikeNumeral : Numeral() {
    override fun convert(num: Int): String {
        val n: String = num.toString()
        val ln = n.length
        val s = StringBuilder()
        for (ii in n.indices) {
            val i = n[ii].digitToInt()
            if (i == 0) continue
            s.append(chars[((((ln - ii) - 1) * 9) - 1) + i])
        }
        return s.toString()
    }
}

/** @see <a href="https://en.wikipedia.org/wiki/Egyptian_numerals">Wikipedia</a> */
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
        "\uD809\uDC3C", "\uD809\uDC0A", "\uD809\uDC0A",
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
 * Apparently in some dialects, 6 and 7 (but not 8) are written like six-pack abs: 𒐚 (\uD809\uDC1A)
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

    override fun convert(num: Int): String {
        val s = StringBuilder()
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
            s.append(chars[subChar])
        }
        return s.toString()
    }

    private fun charToInt(i: Int): Int {
        var ii = i.toDouble()
        if (i % 2 == 1) ii -= 1.0
        ii /= 2
        return 10.0.pow(ii).toInt() * (if (i % 2 == 0) 1 else 2)
    }
}
