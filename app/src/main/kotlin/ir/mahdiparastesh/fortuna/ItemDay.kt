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
import ir.mahdiparastesh.fortuna.Main.Companion.color
import ir.mahdiparastesh.fortuna.Main.Companion.stylise
import ir.mahdiparastesh.fortuna.Vita.Companion.showScore
import ir.mahdiparastesh.fortuna.Vita.Companion.toPersianCalendar
import ir.mahdiparastesh.fortuna.Vita.Companion.z
import ir.mahdiparastesh.fortuna.databinding.ItemDayBinding
import ir.mahdiparastesh.fortuna.databinding.VariabilisBinding

class ItemDay(private val c: Main) : ListAdapter {
    private val roman: Array<String> by lazy { c.resources.getStringArray(R.array.romanNumbers) }
    private val cp: Int by lazy { c.color(com.google.android.material.R.attr.colorPrimary) }
    private val cs: Int by lazy { c.color(com.google.android.material.R.attr.colorSecondary) }
    private val tc: Int by lazy { c.color(android.R.attr.textColor) }
    private val cpo: Int by lazy { c.color(com.google.android.material.R.attr.colorOnPrimary) }
    private val cso: Int by lazy { c.color(com.google.android.material.R.attr.colorOnSecondary) }
    private val luna = c.m.vita?.find(c.m.luna) ?: Vita.emptyLuna()
    private val calendar = c.m.luna.toPersianCalendar()

    override fun registerDataSetObserver(observer: DataSetObserver) {}
    override fun unregisterDataSetObserver(observer: DataSetObserver) {}

    override fun getCount(): Int = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    override fun getItem(i: Int): Float = 0f

    override fun getItemId(i: Int): Long = i.toLong()

    override fun getView(i: Int, convertView: View?, parent: ViewGroup): View =
        ItemDayBinding.inflate(c.layoutInflater, parent, false).apply {
            dies.text = roman[i]
            variabilis.text = luna[i].showScore()

            root.setBackgroundColor(
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
                            -luna[i]!! / Vita.MAX_RANGE
                        ).toArgb()
                    }
                    else -> {
                        dies.setTextColor(tc)
                        variabilis.setTextColor(tc)
                        Color.TRANSPARENT
                    }
                }
            )

            highlight.setOnClickListener {
                val bv = VariabilisBinding.inflate(c.layoutInflater)
                bv.root.apply {
                    maxValue = 12 // this range is reverse
                    minValue = 0 // must be >= 0
                    value = luna[i]?.let { (-(it * 2f) + 6f).toInt() } ?: 6
                    wrapSelectorWheel = false
                    setFormatter { it.toScore().showScore() }
                    //textColor =
                    textSize = c.resources.displayMetrics.density * 19f
                    //forEach { (it as EditText).setText("TEST") }
                }
                AlertDialog.Builder(c).apply {
                    setTitle(c.getString(R.string.variabilis, "${c.m.luna}.${z(i + 1)}"))
                    setView(bv.root)
                    setNegativeButton(R.string.cancel, null)
                    setPositiveButton(R.string.save) { _, _ ->
                        if (c.m.vita != null) saveScore(i, bv.root.value.toScore())
                    }
                    setNeutralButton(R.string.clear) { _, _ ->
                        if (c.m.vita != null) saveScore(i, null)
                    }
                }.show().stylise(c)
            }
        }.root

    override fun hasStableIds(): Boolean = true
    override fun getItemViewType(i: Int): Int = 0
    override fun getViewTypeCount(): Int = 1
    override fun isEmpty(): Boolean = false
    override fun areAllItemsEnabled(): Boolean = true
    override fun isEnabled(i: Int): Boolean = true

    private fun saveScore(i: Int, score: Float?) {
        luna[i] = score
        c.m.vita!![c.m.luna] = luna
        c.m.vita!!.save(c.c)
        c.updateGrid()
    }

    companion object {
        @ColorInt
        fun Int.toValue() = toFloat() / 256f

        fun Int.toScore() = -(toFloat() - 6f) / 2f
    }
}
