package ir.mahdiparastesh.fortuna

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.database.DataSetObserver
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import ir.mahdiparastesh.fortuna.databinding.ItemGridBinding
import ir.mahdiparastesh.fortuna.sect.ChronometerDialog
import ir.mahdiparastesh.fortuna.util.NumberUtils.displayScore
import ir.mahdiparastesh.fortuna.util.NumberUtils.write
import ir.mahdiparastesh.fortuna.util.Numeral
import ir.mahdiparastesh.fortuna.util.Numerals
import ir.mahdiparastesh.fortuna.util.UiTools.color
import java.time.temporal.ChronoField

/** Main table of our calendar grid which lists days of a month */
class Grid(private val c: Main) : ListAdapter {
    lateinit var luna: Luna
    private var numType: String? = null
    private var numeral: Numeral? = null
    var maximumStats: Int? = null

    init {
        onRefresh()
    }

    private val tc: Int by lazy { c.color(android.R.attr.textColor) }
    private val cpo: Int by lazy { c.color(com.google.android.material.R.attr.colorOnPrimary) }
    private val cso: Int by lazy { c.color(com.google.android.material.R.attr.colorOnSecondary) }

    /** Helper array for animating cell colours */
    private val cellColours: Array<Int> = Array<Int>(
        c.c.chronology.range(ChronoField.DAY_OF_MONTH).maximum.toInt()
    ) { Color.TRANSPARENT }

    override fun getCount(): Int = c.c.date.lengthOfMonth()
    override fun isEmpty(): Boolean = false
    override fun getItem(i: Int): Any = 0f
    override fun getItemId(i: Int): Long = i.toLong()
    override fun hasStableIds(): Boolean = true
    override fun getItemViewType(i: Int): Int = 0
    override fun getViewTypeCount(): Int = 1
    override fun isEnabled(i: Int): Boolean = true
    override fun areAllItemsEnabled(): Boolean = true
    override fun registerDataSetObserver(observer: DataSetObserver) {}
    override fun unregisterDataSetObserver(observer: DataSetObserver) {}

    @SuppressLint("SetTextI18n", "ViewHolder", "UseCompatLoadingForDrawables")
    override fun getView(i: Int, convertView: View?, parent: ViewGroup): View {
        val b = convertView?.let { ItemGridBinding.bind(it) }
            ?: ItemGridBinding.inflate(c.layoutInflater, parent, false)

        // calculation
        val score: Float? =
            if (i < (maximumStats ?: 0)) luna[i] ?: luna.default else null
        val isEstimated = i < (maximumStats ?: 0) && luna[i] == null && luna.default != null

        // numbers
        b.dies.text = numeral.write(i + 1)
        b.variabilis.text = (if (isEstimated) "c. " else "") + score.displayScore(false)
        b.variabilis.alpha = if (score != null) 1f else .6f

        // icons
        (luna.verba[i]?.isNotBlank() == true).also { show ->
            b.verbumIcon.isVisible = show
            if (show) b.verbumIcon.setImageResource(R.drawable.verbum)
            else b.verbumIcon.setImageDrawable(null)
        }
        val emj = luna.emojis.getOrNull(i)
        b.emoji.text = emj
        b.emoji.isVisible = emj != null

        // background colour
        val targetColour = when {
            score != null && score > 0f -> {
                b.dies.setTextColor(cpo)
                b.variabilis.setTextColor(cpo)
                b.verbumIcon.setColorFilter(cpo)
                Color.valueOf(c.cp[0], c.cp[1], c.cp[2], score / Vita.MAX_RANGE).toArgb()
            }

            score != null && score < 0f -> {
                b.dies.setTextColor(cso)
                b.variabilis.setTextColor(cso)
                b.verbumIcon.setColorFilter(cso)
                Color.valueOf(c.cs[0], c.cs[1], c.cs[2], -score / Vita.MAX_RANGE).toArgb()
            }

            else -> {
                b.dies.setTextColor(tc)
                b.variabilis.setTextColor(tc)
                b.verbumIcon.setColorFilter(tc)
                Color.TRANSPARENT
            }
        }
        ValueAnimator.ofArgb(cellColours[i], targetColour).apply {
            addUpdateListener { b.root.setBackgroundColor(it.animatedValue as Int) }
            // startDelay = 10L * i
            duration = 100L
            start()
        }
        cellColours[i] = targetColour

        // clicks
        b.root.setOnClickListener { c.variabilis(i) }
        b.root.setOnLongClickListener {
            ChronometerDialog.newInstance(i).show(c.supportFragmentManager, ChronometerDialog.TAG)
            true
        }

        // highlight the cell if it indicates today
        if (c.c.luna == c.c.todayLuna && c.c.todayDate[ChronoField.DAY_OF_MONTH] == i + 1)
            b.root.foreground = AppCompatResources.getDrawable(c, R.drawable.dies_today)
        else
            b.root.foreground = c.resources.getDrawable(R.drawable.dies, c.theme)

        return b.root
    }

    /** Invoked via [Main.updateGrid]. */
    fun onRefresh() {
        luna = c.c.vita[c.c.luna]
        numType = c.c.sp.getString(Fortuna.SP_NUMERAL_TYPE, Fortuna.SP_NUMERAL_TYPE_DEF)
            .let { if (it == Fortuna.SP_NUMERAL_TYPE_DEF) null else it }
        numeral = Numerals.build(numType)
        maximumStats = c.c.maximaForStats(c.c.date, c.c.luna)
    }
}
