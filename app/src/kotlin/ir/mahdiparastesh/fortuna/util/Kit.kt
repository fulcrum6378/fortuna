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
import ir.mahdiparastesh.fortuna.util.NumberUtils.z

/** Static fields and methods used everywhere */
object Kit {
    const val A_DAY = 86400000L
    const val SEXBOOK = "ir.mahdiparastesh.sexbook"
    const val VITA_MIME_TYPE = "application/octet-stream"

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

    fun Calendar.toKey(): String =
        "${z(this[Calendar.YEAR], 4)}.${z(this[Calendar.MONTH] + 1)}"

    fun Calendar.lunaMaxima() = getActualMaximum(Calendar.DAY_OF_MONTH)

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

    fun <T> Class<T>.create(): T = getDeclaredConstructor().newInstance()

    /** Explains bytes for humans. */
    fun showBytes(c: Context, length: Long): String =
        NumberUtils.showBytes(c.resources.getStringArray(R.array.bytes), length)


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
