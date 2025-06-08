package ir.mahdiparastesh.fortuna.util

import android.content.Context
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

/** Static UI-related functions used everywhere */
object UiTools {

    /** @return the colour value of this attribute resource from the theme */
    @ColorInt
    fun ContextThemeWrapper.color(@AttrRes attr: Int) = TypedValue().apply {
        theme.resolveAttribute(attr, this, true)
    }.data

    /** Clears focus from an [EditText]. */
    fun EditText.blur(c: Context) {
        (c.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)
            ?.hideSoftInputFromWindow(windowToken, 0)
        clearFocus()
    }
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
