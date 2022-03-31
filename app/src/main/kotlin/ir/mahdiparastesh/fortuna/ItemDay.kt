package ir.mahdiparastesh.fortuna

import android.database.DataSetObserver
import android.graphics.Color
import android.icu.util.Calendar
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import ir.mahdiparastesh.fortuna.Vita.Companion.toPersianCalendar
import ir.mahdiparastesh.fortuna.databinding.ItemDayBinding

class ItemDay(private val c: Main) : ListAdapter {
    private val roman: Array<String> by lazy { c.resources.getStringArray(R.array.romanNumbers) }
    private val cp: Int by lazy { c.color(com.google.android.material.R.attr.colorPrimary) }
    private val cs: Int by lazy { c.color(com.google.android.material.R.attr.colorSecondary) }
    private val tc: Int by lazy { c.color(android.R.attr.textColor) }
    private val cpo: Int by lazy { c.color(com.google.android.material.R.attr.colorOnPrimary) }
    private val cso: Int by lazy { c.color(com.google.android.material.R.attr.colorOnSecondary) }
    private val luna = c.m.vita?.findByKey(c.m.luna) ?: Vita.emptyLuna()
    private val calendar = c.m.luna.toPersianCalendar()

    override fun registerDataSetObserver(observer: DataSetObserver) {}
    override fun unregisterDataSetObserver(observer: DataSetObserver) {}

    override fun getCount(): Int = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    override fun getItem(i: Int): Float = 0f

    override fun getItemId(i: Int): Long = i.toLong()

    override fun getView(i: Int, convertView: View?, parent: ViewGroup): View =
        ItemDayBinding.inflate(c.layoutInflater, parent, false).apply {
            dies.text = roman[i]
            variabilis.text = luna[i]?.toString() ?: "_"

            highlight.setBackgroundColor(
                when {
                    luna[i] != null && luna[i]!! > 0f -> {
                        dies.setTextColor(cpo)
                        variabilis.setTextColor(cpo)
                        Color.valueOf(
                            cp.red.toValue(), cp.green.toValue(), cp.blue.toValue(),
                            luna[i]!! / Vita.MAX_RANGE
                        ).toArgb()
                    }
                    luna[i] != null && luna[i]!! < 0f -> {
                        dies.setTextColor(cso)
                        variabilis.setTextColor(cso)
                        Color.valueOf(
                            cs.red.toValue(), cs.green.toValue(), cs.blue.toValue(),
                            luna[i]!! / Vita.MAX_RANGE
                        ).toArgb()
                    }
                    else -> {
                        dies.setTextColor(tc)
                        variabilis.setTextColor(tc)
                        Color.valueOf(0f, 0f, 0f, 0f).toArgb()
                    }
                }
            )

            root.setOnClickListener {
                //AlertDialog.Builder(c).apply {}
            }
        }.root

    override fun hasStableIds(): Boolean = true
    override fun getItemViewType(i: Int): Int = 0
    override fun getViewTypeCount(): Int = 1
    override fun isEmpty(): Boolean = false
    override fun areAllItemsEnabled(): Boolean = true
    override fun isEnabled(i: Int): Boolean = true

    companion object {
        @ColorInt
        fun Int.toValue() = toFloat() / 256f
    }
}
