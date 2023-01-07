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

    val calType = when (BuildConfig.FLAVOR) {
        "gregorian" -> android.icu.util.GregorianCalendar::class.java
        "iranian" -> HumanistIranianCalendar::class.java
        else -> throw Exception()
    }
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

    fun Calendar.resetHours(): Calendar {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        return this
    }

    fun Calendar.compareByDays(other: Calendar): Int =
        ((other.timeInMillis - timeInMillis) / A_DAY).toInt()

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

    fun Int.toValue() = toFloat() / 256f

    fun diesNum(i: Int, numType: String?): String = (numType?.let { BaseNumeral.find(it) }
        ?.constructors?.getOrNull(0)?.newInstance(i) as BaseNumeral?)
        ?.toString() ?: i.toString()


    abstract class DoubleClickListener(private val span: Long = 500) : View.OnClickListener {
        private var times: Long = 0

        override fun onClick(v: View) {
            if ((SystemClock.elapsedRealtime() - times) < span) onDoubleClick()
            times = SystemClock.elapsedRealtime()
        }

        abstract fun onDoubleClick()
    }

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
