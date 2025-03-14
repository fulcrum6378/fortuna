package ir.mahdiparastesh.fortuna.util

import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import ir.mahdiparastesh.fortuna.Main
import ir.mahdiparastesh.fortuna.R
import ir.mahdiparastesh.fortuna.Vita.Companion.toKey

/** Static fields and methods used everywhere. */
object Kit {
    const val A_DAY = 86400000L
    const val SEXBOOK = "ir.mahdiparastesh.sexbook"

    /* Keys of the Shared Preferences */
    const val SP_NUMERAL_TYPE = "numeral_type"
    const val SP_NUMERAL_TYPE_DEF = "0" // defaults to Arabic
    const val SP_SEARCH_INCLUSIVE = "search_inclusive"
    const val SP_DROPBOX_CREDENTIAL = "dropbox_credential"

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

    fun Cursor?.iterate(action: Cursor.() -> Unit) {
        this?.use { cur -> while (cur.moveToNext()) cur.action() }
    }

    /**
     * Finds a Java class name in a safer way.
     * @param jc simple Java class name (e.g. RomanNumeral)
     */
    fun findClass(jc: String): Class<*>? = try {
        Class.forName(jc)
    } catch (_: ClassNotFoundException) {
        null
    }

    fun <T> Class<T>.create(): T = getDeclaredConstructor().newInstance()

    /** Explains bytes for humans. */
    fun showBytes(c: Context, length: Long): String {
        val units = c.resources.getStringArray(R.array.bytes)
        var unit = 0
        var nominalSize = length.toDouble()
        while ((nominalSize / 1024.0) > 1.0) {
            nominalSize /= 1024.0
            unit++
            if (unit == units.size - 1) break
        }
        return units[unit].format(nominalSize.toInt())
    }


    abstract class DoubleClickListener(private val span: Long = 500) : View.OnClickListener {
        private var times: Long = 0

        override fun onClick(v: View) {
            if ((SystemClock.elapsedRealtime() - times) < span) onDoubleClick()
            times = SystemClock.elapsedRealtime()
        }

        abstract fun onDoubleClick()
    }

    /** Subclass of [View.OnClickListener] which shows one Toast at a time. */
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

    /** Base class for DialogFragment instances in this app. */
    abstract class BaseDialogue : DialogFragment() {
        protected val c: Main by lazy { activity as Main }
    }
}
