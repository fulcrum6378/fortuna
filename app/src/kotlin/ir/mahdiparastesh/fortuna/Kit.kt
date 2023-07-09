package ir.mahdiparastesh.fortuna

import android.Manifest
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
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
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import ir.mahdiparastesh.fortuna.Vita.Companion.toKey
import ir.mahdiparastesh.fortuna.misc.HumanistIranianCalendar
import java.util.*

/** Static functions and utilities used everywhere. */
object Kit {
    const val A_DAY = 86400000L
    const val SP_NUMERAL_TYPE = "numeral_type"
    const val SP_SEARCH_INCLUSIVE = "search_inclusive"
    const val defNumType = "0" // Arabic
    const val SEXBOOK = "ir.mahdiparastesh.sexbook"

    /**
     * Default Calendar Type
     * This is a very important constant containing the class type of our default calendar,
     * which must be a subclass of android.icu.util.Calendar.
     *
     * Do NOT use Lunisolar calendars here!
     *
     * @see android.icu.util.Calendar
     * @see <a href="https://en.wikipedia.org/wiki/Lunisolar_calendar">Lunisolar calendar - Wikipedia</a>
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
        android.icu.util.IndianCalendar::class.java,
        android.icu.util.ChineseCalendar::class.java,
        android.icu.util.IslamicCalendar::class.java,
        android.icu.util.HebrewCalendar::class.java,
        android.icu.util.CopticCalendar::class.java,
    ).filter { it != calType }
    val locale: Locale = Locale.UK // never ever use SimpleDateFormat

    /**
     * List of all the required permissions.
     * Change Main::reqPermLauncher to RequestMultiplePermissions() if you wanna add more.
     */
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
        if ((forward && this[Calendar.MONTH] == getActualMinimum(Calendar.MONTH)) ||
            (!forward && this[Calendar.MONTH] == getActualMaximum(Calendar.MONTH))
        ) roll(Calendar.YEAR, forward)
    }

    /** Compares two Calendar instances and returns their difference in days. */
    fun Calendar.compareByDays(other: Calendar): Int =
        ((other.timeInMillis - timeInMillis) / A_DAY).toInt()

    /**
     * Groups the digits of a number triply (both integral and fractional ones).
     * e.g. 6401 -> 6,401 or 1234.5678 -> 1,234.567,8
     */
    fun Number.groupDigits(): String {
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
            for (ff in 0 until f!!.length) {
                ret.append(f!![ff])
                right++
                if (right % 3 == 0 && ff != 0) ret.append(",")
            }
        }
        return "$ret"
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

    /** Helper function for copying a text to clipboard. */
    fun copyToClipboard(c: Context, text: CharSequence, label: CharSequence?) {
        (c.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)
            ?.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(c, R.string.copied, Toast.LENGTH_SHORT).show()
    }

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

    /**
     * Finds a Java class name in a safer way.
     * @param jc simple Java class name (e.g. RomanNumeral)
     */
    fun findClass(jc: String): Class<*>? = try {
        Class.forName(jc)
    } catch (e: ClassNotFoundException) {
        null
    }

    fun <T> Class<T>.create(): T = getDeclaredConstructor().newInstance()

    /** Explains bytes for humans. */
    fun showBytes(c: Context, length: Long): String {
        val units = c.resources.getStringArray(R.array.bytes)
        var unit = 0
        var nominalSize = length
        while ((nominalSize / 1024L) > 1) {
            nominalSize /= 1024L
            unit++
            if (unit == units.size - 1) break
        }
        return units[unit].format(nominalSize)
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

    /** Helper class for implementing RecyclerView.ViewHolder. */
    open class AnyViewHolder<B>(val b: B) : RecyclerView.ViewHolder(b.root) where B : ViewBinding
}
