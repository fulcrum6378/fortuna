package ir.mahdiparastesh.fortuna.sect

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import android.util.SparseArray
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.core.util.containsKey
import androidx.core.util.forEach
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ir.mahdiparastesh.fortuna.R
import ir.mahdiparastesh.fortuna.Vita
import ir.mahdiparastesh.fortuna.databinding.WholeBinding
import ir.mahdiparastesh.fortuna.util.Kit
import ir.mahdiparastesh.fortuna.util.Kit.create
import ir.mahdiparastesh.fortuna.util.Kit.resetHours
import ir.mahdiparastesh.fortuna.util.NumberUtils.groupDigits
import ir.mahdiparastesh.fortuna.util.NumberUtils.hexToValue

/**
 * A dialogue doing some statistics.
 *
 * Making statistics in a way that it'll show every year since the minimum scored days till the
 * maximum scored days could cause a super huge table in irregular scoring accident, e. g. if
 * someone accidentally or deliberately score a day in year 25 or 8000.
 */
class StatisticsDialog : Kit.BaseDialogue() {
    private var dialogue: AlertDialog? = null
    private lateinit var text: String

    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val scores = arrayListOf<Float>()
        val keyMeanMap = hashMapOf<String, Float>()
        for ((key, luna) in c.c.vita) {
            val lunaScores = arrayListOf<Float>()
            val cal = c.c.lunaToCalendar(key).resetHours()
            val maxima = c.c.maximaForStats(cal, key) ?: continue
            for (v in 0 until maxima)
                (luna[v] ?: luna.default)?.also { lunaScores.add(it) }
            scores.addAll(lunaScores)
            keyMeanMap[key] = lunaScores.sum() / lunaScores.size.toFloat()
        } // don't use Luna.mean() for efficiency.
        val sum = scores.sum()
        text = getString(
            R.string.statText,
            (if (scores.isEmpty()) 0f else sum / scores.size.toFloat()).groupDigits(6),
            sum.groupDigits(),
            resources.getQuantityString(R.plurals.day, scores.size, scores.size.groupDigits())
        )

        dialogue = MaterialAlertDialogBuilder(c).apply {
            val maxMonths = c.c.calendar.getMaximum(Calendar.MONTH) + 1
            val meanMap = SparseArray<Array<Float?>>()
            keyMeanMap.forEach { (key, mean) ->
                val spl = key.split(".")
                val y = spl[0].toInt()
                val m = spl[1].toInt() - 1
                if (!meanMap.containsKey(y)) meanMap[y] = Array(maxMonths) { null }
                meanMap[y][m] = mean
            }
            val bw = WholeBinding.inflate(layoutInflater)

            val cp = c.getColor(R.color.CP)
            val cs = c.getColor(R.color.CS)
            val cellH = resources.getDimension(R.dimen.statCellHeight).toInt()
            val nullCellColour = ContextCompat.getColor(c, R.color.statCell)
            val monthNames = resources.getStringArray(R.array.luna)
            meanMap.forEach { year, array ->
                bw.years.addView(TextView(c).apply {
                    text = year.toString()
                    textSize = cellH.toFloat() * 0.25f
                    gravity = Gravity.CENTER_VERTICAL
                }, LinearLayout.LayoutParams(-2, cellH))

                val tr = LinearLayout(c)
                tr.orientation = LinearLayout.HORIZONTAL
                tr.weightSum = maxMonths.toFloat()
                array.forEachIndexed { month, score ->
                    tr.addView(
                        View(c).apply {
                            setBackgroundColor(
                                when {
                                    score != null && score > 0f -> Color.valueOf(
                                        cp.red.hexToValue(),
                                        cp.green.hexToValue(),
                                        cp.blue.hexToValue(),
                                        score / Vita.MAX_RANGE
                                    ).toArgb()
                                    score != null && score < 0f -> Color.valueOf(
                                        cs.red.hexToValue(),
                                        cs.green.hexToValue(),
                                        cs.blue.hexToValue(),
                                        -score / Vita.MAX_RANGE
                                    ).toArgb()
                                    score != null -> Color.TRANSPARENT
                                    else -> nullCellColour
                                }
                            )
                            tooltipText =
                                "${monthNames[month]} $year${
                                    score?.groupDigits()?.let { "\n$it" } ?: ""
                                }"
                            setOnClickListener(object : Kit.DoubleClickListener() {
                                override fun onDoubleClick() {
                                    c.c.calendar = c.c.calType.create().apply {
                                        set(Calendar.YEAR, year)
                                        set(Calendar.MONTH, month)
                                        set(Calendar.DAY_OF_MONTH, 1)
                                        resetHours()
                                    }
                                    c.onCalendarChanged()
                                    dialogue?.cancel()
                                    c.closeDrawer()
                                }
                            })
                        }, LinearLayout.LayoutParams(0, cellH, 1f)
                            .apply { setMargins(1, 1, 1, 1) })
                }
                bw.table.addView(tr, LinearLayout.LayoutParams(-1, cellH))
            }
            bw.sv.post {
                bw.sv.smoothScrollTo(
                    0, (bw.body.bottom + bw.sv.paddingBottom) - (bw.sv.scrollY + bw.sv.height)
                )
            }

            setTitle(R.string.navStat)
            setMessage(text)
            setView(bw.root)
            setPositiveButton(R.string.ok, null)
            setNeutralButton(R.string.copy, null)
        }.create()
        return dialogue!!
    }

    override fun onResume() {
        super.onResume()
        dialogue?.getButton(AlertDialog.BUTTON_NEUTRAL)
            ?.setOnClickListener { Kit.copyToClipboard(c, text, getString(R.string.fortunaStat)) }
    }
}
