package ir.mahdiparastesh.fortuna

import android.database.DataSetObserver
import android.icu.util.Calendar
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import ir.mahdiparastesh.fortuna.databinding.ItemDayBinding

class ItemDay(private val c: Main) : ListAdapter {
    override fun registerDataSetObserver(observer: DataSetObserver) {}
    override fun unregisterDataSetObserver(observer: DataSetObserver) {}

    override fun getCount(): Int = c.calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    override fun getItem(i: Int): Float = 0f

    override fun getItemId(i: Int): Long = i.toLong()

    override fun getView(i: Int, convertView: View?, parent: ViewGroup): View =
        ItemDayBinding.inflate(c.layoutInflater, parent, false).apply {
            dayOfMonth.text = (i + 1).toString()
        }.root

    override fun hasStableIds(): Boolean = true
    override fun getItemViewType(i: Int): Int = 0
    override fun getViewTypeCount(): Int = 1
    override fun isEmpty(): Boolean = false
    override fun areAllItemsEnabled(): Boolean = true
    override fun isEnabled(i: Int): Boolean = true
}
