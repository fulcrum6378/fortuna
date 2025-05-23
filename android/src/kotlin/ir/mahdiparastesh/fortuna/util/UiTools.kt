package ir.mahdiparastesh.fortuna.util

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.database.Cursor
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
import ir.mahdiparastesh.fortuna.util.NumberUtils.toKey
import java.time.chrono.ChronoLocalDate
import java.time.temporal.ChronoField

/** Static UI-related functions used everywhere */
object UiTools {

    /** @return the colour value of this attribute resource from the theme */
    @ColorInt
    fun ContextThemeWrapper.color(@AttrRes attr: Int) = TypedValue().apply {
        theme.resolveAttribute(attr, this, true)
    }.data

    /** Opens the specified date in the device's default calendar app. */
    fun openInDate(c: Context, cal: ChronoLocalDate, req: Int): PendingIntent =
        PendingIntent.getActivity(
            c, req, Intent(c, Main::class.java)
                .putExtra(Main.EXTRA_LUNA, cal.toKey())
                .putExtra(Main.EXTRA_DIES, cal[ChronoField.DAY_OF_MONTH]),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                PendingIntent.FLAG_MUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
        )  // A unique request code protects the PendingIntent from being recycled!

    /** Clears focus from an [EditText]. */
    fun EditText.blur(c: Context) {
        (c.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)
            ?.hideSoftInputFromWindow(windowToken, 0)
        clearFocus()
    }

    fun Cursor?.iterate(action: Cursor.() -> Unit) {
        this?.use { cur -> while (cur.moveToNext()) cur.action() }
    }

    /** Explains bytes for humans. */
    fun showBytes(c: Context, length: Long): String =
        NumberUtils.showBytes(c.resources.getStringArray(R.array.bytes), length)
}

abstract class DoubleClickListener(private val span: Long = 500) : View.OnClickListener {
    private var times: Long = 0

    override fun onClick(v: View) {
        if ((SystemClock.elapsedRealtime() - times) < span) onDoubleClick()
        times = SystemClock.elapsedRealtime()
    }

    abstract fun onDoubleClick()
}

/** Subclass of [View.OnClickListener] which shows one [Toast] at a time */
class LimitedToastAlert(private val c: Context, @StringRes private val msg: Int) :
    View.OnClickListener {
    private var last = 0L

    override fun onClick(v: View?) {
        if (SystemClock.elapsedRealtime() - last < 2500L) return
        Toast.makeText(c, msg, Toast.LENGTH_SHORT).show()
        last = SystemClock.elapsedRealtime()
    }
}

/** Helper class for implementing [RecyclerView.ViewHolder] */
open class AnyViewHolder<B>(val b: B) : RecyclerView.ViewHolder(b.root) where B : ViewBinding

/** Base class for [DialogFragment] instances in this app */
abstract class BaseDialogue : DialogFragment() {
    protected val c: Main by lazy { activity as Main }
}
