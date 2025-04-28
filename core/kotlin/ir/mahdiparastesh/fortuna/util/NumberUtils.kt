package ir.mahdiparastesh.fortuna.util

import java.time.chrono.ChronoLocalDate
import java.time.temporal.ChronoField

object NumberUtils {
    const val A_DAY = 86400000L

    /**
     * Fills a String with a number and zeroes before it. (e.g. 2 -> "02")
     *
     * @param n number
     * @param ideal the desired length of the returned string
     */
    fun z(n: Any?, ideal: Int = 2): String {
        var s = n.toString()
        var neg = false
        if (s.startsWith("-")) {
            s = s.substring(1)
            neg = true
        }
        while (s.length < ideal) s = "0$s"
        return if (!neg) s else "-$s"
    }

    fun ChronoLocalDate.toKey(): String =
        "${z(get(ChronoField.YEAR), 4)}.${z(get(ChronoField.MONTH_OF_YEAR))}"

    fun Numeral?.write(i: Int) = this?.output(i) ?: i.toString()

    /**
     * Groups the digits of a number triply (both integral and fractional ones).
     * e.g. 6401 -> 6,401 or 1234.5678 -> 1,234.567,8
     *
     * @param fractionLimit cut the fraction numbers since this position
     */
    fun Number.groupDigits(fractionLimit: Int = 0): String {
        val i: String
        var f: String? = null
        toString().split(".").also {
            if (it.size == 2) f = it[1]
            i = it[0]
        }
        val ret = StringBuilder()

        // group the integral digits
        var left = 0
        for (ii in i.length - 1 downTo 0) {
            ret.insert(0, i[ii])
            left++
            if (left % 3 == 0 && ii != 0) ret.insert(0, ",")
        }

        // group the fractional digits (if available)
        if (f != null) {
            ret.append(".")
            var right = 0
            for (ff in 0 until f.length) {
                ret.append(f[ff])
                right++
                if (fractionLimit in 1..right) break
                if (right % 3 == 0 && ff != 0 && ff < f.length - 1) ret.append(",")
            }
        }
        return "$ret"
    }

    /* Converts a hexadecimal colour integer into a Float of range 0..1. */
    //fun Int.hexToValue() = toFloat() / 256f

    /** Explains bytes for humans. */
    fun showBytes(units: Array<String>, length: Long): String {
        var unit = 0
        var nominalSize = length.toDouble()
        while ((nominalSize / 1024.0) > 1.0) {
            nominalSize /= 1024.0
            unit++
            if (unit == units.size - 1) break
        }
        return units[unit].format(nominalSize.toInt())
    }
}
