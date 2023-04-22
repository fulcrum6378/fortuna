package ir.mahdiparastesh.fortuna

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.database.Cursor
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.icu.util.Calendar
import android.os.Build
import android.os.SystemClock
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import ir.mahdiparastesh.fortuna.Vita.Companion.toKey
import java.util.*

object Kit {
    const val A_DAY = 86400000L
    const val SP_NUMERAL_TYPE = "numeral_type"
    const val arNumType = "0"
    const val SEXBOOK = "ir.mahdiparastesh.sexbook"

    /**
     * Default Calendar Type
     * This is a very important constant containing the class type of our calendar, which must be
     * a subclass of android.icu.util.Calendar.
     *
     * @see android.icu.util.Calendar
     */
    @Suppress("KotlinConstantConditions")
    val calType = when (BuildConfig.FLAVOR) {
        "iranian" -> HumanistIranianCalendar::class.java
        "gregorian" -> android.icu.util.GregorianCalendar::class.java
        else -> throw Exception("Unknown calendar type!")
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

    /** List of the required permissions */
    val reqPermissions =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            arrayOf(Manifest.permission.POST_NOTIFICATIONS)
        else arrayOf()


    /** @return the main shared preferences instance; <code>settings.xml</code>. */
    fun Context.sp(): SharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE)

    /** @return the colour value of this attribute resource from the theme. */
    @ColorInt
    fun ContextThemeWrapper.color(@AttrRes attr: Int) = TypedValue().apply {
        theme.resolveAttribute(attr, this, true)
    }.data

    /** @return the colour filter instance of this colour with the given PorterDuff.Mode. */
    fun pdcf(@ColorInt color: Int, mode: PorterDuff.Mode = PorterDuff.Mode.SRC_IN) =
        PorterDuffColorFilter(color, mode)

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

    /** Opens the specified date in the device's default calendar app. */
    fun openInDate(c: Context, cal: Calendar, req: Int): PendingIntent =
        PendingIntent.getActivity(
            c, req, Intent(c, Main::class.java)
                .putExtra(Main.EXTRA_LUNA, cal.toKey())
                .putExtra(Main.EXTRA_DIES, cal[Calendar.DAY_OF_MONTH]),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                PendingIntent.FLAG_MUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
        ) // A unique request code protects the PendingIntent from being recycled!

    /** Clears focus from an EditText. */
    fun EditText.blur(c: Context) {
        (c.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)
            ?.hideSoftInputFromWindow(windowToken, 0)
        clearFocus()
    }

    fun Context.isLandscape() =
        resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    fun Cursor?.iterate(action: Cursor.() -> Unit) {
        this?.use { cur -> while (cur.moveToNext()) cur.action() }
    }


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
