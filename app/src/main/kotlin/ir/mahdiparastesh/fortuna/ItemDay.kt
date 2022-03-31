package ir.mahdiparastesh.fortuna

import android.database.DataSetObserver
import android.graphics.Color
import android.icu.util.Calendar
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import ir.mahdiparastesh.fortuna.databinding.ItemDayBinding

class ItemDay(private val c: Main) : ListAdapter {
    private val cp: Int by lazy { c.color(com.google.android.material.R.attr.colorPrimary) }
    private val cs: Int by lazy { c.color(com.google.android.material.R.attr.colorSecondary) }
    private val luna = c.m.vita?.findByCalendar(c.calendar) ?: Luna()

    override fun registerDataSetObserver(observer: DataSetObserver) {}
    override fun unregisterDataSetObserver(observer: DataSetObserver) {}

    override fun getCount(): Int = c.calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    override fun getItem(i: Int): Float = 0f

    override fun getItemId(i: Int): Long = i.toLong()

    override fun getView(i: Int, convertView: View?, parent: ViewGroup): View =
        ItemDayBinding.inflate(c.layoutInflater, parent, false).apply {
            dayOfMonth.text = (/*i + 1*/luna[i]).toString()
            val bg: Int
            when {
                luna[i] != null && luna[i]!! > 0f -> {
                    bg = Color.valueOf(
                        cp.red.toFloat(), cp.green.toFloat(), cp.blue.toFloat(),
                        luna[i]!! / Luna.MAX_RANGE
                    ).toArgb()
                }
                luna[i] != null && luna[i]!! < 0f -> {
                    bg = Color.valueOf(
                        cs.red.toFloat(), cs.green.toFloat(), cs.blue.toFloat(),
                        luna[i]!! / Luna.MAX_RANGE
                    ).toArgb()
                }
                else -> {
                    bg = Color.valueOf(0f, 0f, 0f, 0f).toArgb()
                }
            }
            root.setBackgroundColor(bg)
        }.root

    override fun hasStableIds(): Boolean = true
    override fun getItemViewType(i: Int): Int = 0
    override fun getViewTypeCount(): Int = 1
    override fun isEmpty(): Boolean = false
    override fun areAllItemsEnabled(): Boolean = true
    override fun isEnabled(i: Int): Boolean = true
}
