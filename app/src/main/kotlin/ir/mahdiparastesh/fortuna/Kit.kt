package ir.mahdiparastesh.fortuna

import android.content.Context
import android.icu.util.Calendar
import android.os.SystemClock
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import java.util.*

object Kit {
    const val A_DAY = 86400000L
    const val SEXBOOK = "ir.mahdiparastesh.sexbook"

    /**
     * Default Calendar Type
     * This is a very important constant containing the class type of our calendar, which must be
     * a subclass of android.icu.util.Calendar.
     *
     * @see android.icu.util.Calendar
     */
    val calType = when (BuildConfig.FLAVOR) {
        "gregorian" -> android.icu.util.GregorianCalendar::class.java
        "iranian" -> HumanistIranianCalendar::class.java
        else -> throw Exception()
    }

    /** Other supported Calendar types */
    val otherCalendars = arrayOf(
        HumanistIranianCalendar::class.java,
        // GregorianCalendar does not show a negative number in BCE, which is correct!
        android.icu.util.GregorianCalendar::class.java,
        android.icu.util.IslamicCalendar::class.java,
        android.icu.util.ChineseCalendar::class.java,
        android.icu.util.IndianCalendar::class.java,
        android.icu.util.CopticCalendar::class.java,
        android.icu.util.HebrewCalendar::class.java,
    ).filter { it != calType }
    val locale: Locale = Locale.UK // never ever use SimpleDateFormat

    /**
     * Fills a String with a number and zeroes before it.
     * E.g. 2 -> "02"
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

    /** Sets the time of the calendar on 00:00:00:000 */
    fun Calendar.resetHours(): Calendar {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        return this
    }

    /** Moves the calendar into a different month. */
    fun Calendar.moveCalendarInMonths(forward: Boolean) {
        roll(Calendar.MONTH, forward)
        if ((forward && this[Calendar.MONTH] == 0) ||
            (!forward && this[Calendar.MONTH] == getActualMaximum(Calendar.MONTH))
        ) roll(Calendar.YEAR, forward)
    }

    /** Compares two Calendar instances and returns their difference in days. */
    fun Calendar.compareByDays(other: Calendar): Int =
        ((other.timeInMillis - timeInMillis) / A_DAY).toInt()

    /** Separates the digits of a number triply; e.g. 6401 -> 6,401. */
    fun Number.decSep(): String {
        val s: String
        var fraction = ""
        val ss = StringBuilder()
        toString().split(".").also {
            if (it.size == 2) fraction = "." + it[1]
            s = it[0]
        }
        var sep = 0
        for (i in s.length - 1 downTo 0) {
            ss.insert(0, s[i])
            sep++
            if (sep % 3 == 0 && i != 0) ss.insert(0, ",")
        }
        return ss.toString() + fraction
    }

    /** Converts a hexadecimal colour integer into a Float of range 0..1. */
    fun Int.toValue() = toFloat() / 256f


    abstract class DoubleClickListener(private val span: Long = 500) : View.OnClickListener {
        private var times: Long = 0

        override fun onClick(v: View) {
            if ((SystemClock.elapsedRealtime() - times) < span) onDoubleClick()
            times = SystemClock.elapsedRealtime()
        }

        abstract fun onDoubleClick()
    }

    /** Subclass of {@link View#OnClickListener} which shows one Toast at a time. */
    class LimitedToastAlert(private val c: Context, @StringRes private val msg: Int) :
        View.OnClickListener {
        private var last = 0L

        override fun onClick(v: View?) {
            if (SystemClock.elapsedRealtime() - last < 2500L) return
            Toast.makeText(c, msg, Toast.LENGTH_SHORT).show()
            last = SystemClock.elapsedRealtime()
        }
    }
}
