package ir.mahdiparastesh.fortuna

import android.app.Dialog
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.RippleDrawable
import android.icu.util.Calendar
import android.os.Bundle
import android.util.SparseArray
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.core.util.containsKey
import androidx.core.util.forEach
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import ir.mahdiparastesh.fortuna.Kit.color
import ir.mahdiparastesh.fortuna.Kit.create
import ir.mahdiparastesh.fortuna.Kit.groupDigits
import ir.mahdiparastesh.fortuna.Kit.resetHours
import ir.mahdiparastesh.fortuna.Kit.toValue
import ir.mahdiparastesh.fortuna.Vita.Companion.toCalendar
import ir.mahdiparastesh.fortuna.databinding.BackupBinding
import ir.mahdiparastesh.fortuna.databinding.SearchBinding
import ir.mahdiparastesh.fortuna.databinding.WholeBinding
import ir.mahdiparastesh.fortuna.misc.SearchAdapter
import java.io.FileInputStream

/** Base class for DialogFragment instances in this app. */
abstract class BaseDialogue : DialogFragment() {
    protected val c: Main by lazy { activity as Main }
}

/** A dialogue for searching in VITA. */
class SearchDialog : BaseDialogue() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(c).apply {
            setTitle(R.string.navSearch)
            setView(SearchBinding.inflate(layoutInflater).apply {
                field.addTextChangedListener { isCancelable = it.isNullOrEmpty() }
                field.setOnEditorActionListener { v, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_GO)
                        (list.adapter as SearchAdapter).search(v.text)
                    return@setOnEditorActionListener true
                }
                inclusivity.isChecked = c.sp.getBoolean(Kit.SP_SEARCH_INCLUSIVE, false)
                inclusivity.setOnCheckedChangeListener { _, bb ->
                    c.sp.edit { putBoolean(Kit.SP_SEARCH_INCLUSIVE, bb) }
                    (list.adapter as SearchAdapter).search(field.text, true)
                }
                list.adapter = SearchAdapter(c)
            }.root)
            setNegativeButton(R.string.cancel, null)
        }.create()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        c.m.searchResults.clear()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        c.m.searchResults.clear()
    }
}

/**
 * A dialogue doing some statistics.
 *
 * Making statistics in a way that it'll show every year since the minimum scored days till the
 * maximum scored days could cause a super huge table in irregular scoring accident, e. g. if
 * someone accidentally or deliberately score a day in year 25 or 8000.
 */
class StatisticsDialog : BaseDialogue() {
    private var dialogue: AlertDialog? = null
    private lateinit var text: String

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val scores = arrayListOf<Float>()
        val keyMeanMap = hashMapOf<String, Float>()
        if (c.m.vita != null) for ((key, luna) in c.m.vita!!) {
            val lunaScores = arrayListOf<Float>()
            val cal = key.toCalendar(Kit.calType).resetHours()
            val maxima = c.maximaForStats(cal, key) ?: continue
            for (v in 0 until maxima)
                (luna[v] ?: luna.default)?.also { lunaScores.add(it) }
            scores.addAll(lunaScores)
            keyMeanMap[key] = lunaScores.sum() / lunaScores.size.toFloat()
        } // don't use Luna.mean() for efficiency.
        val sum = scores.sum()
        text = getString(
            R.string.statText,
            (if (scores.isEmpty()) 0f else sum / scores.size.toFloat()).groupDigits(6),
            sum.groupDigits(), scores.size.groupDigits()
        )

        dialogue = MaterialAlertDialogBuilder(c).apply {
            val maxMonths = c.m.calendar.getMaximum(Calendar.MONTH) + 1
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
                                        cp.red.toValue(), cp.green.toValue(), cp.blue.toValue(),
                                        score / Vita.MAX_RANGE
                                    ).toArgb()
                                    score != null && score < 0f -> Color.valueOf(
                                        cs.red.toValue(), cs.green.toValue(), cs.blue.toValue(),
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
                                    c.m.calendar = Kit.calType.create().apply {
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

/**
 * Shows the status of the automatically backed-up file with 3 action buttons:<br />
 * - Backup: manually backs up the data.<br />
 * - Restore: overwrites the backup file on the main file.<br />
 * - Export: exports the backup file.
 */
class BackupDialog : BaseDialogue() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(c).apply {
            setTitle(R.string.backup)
            setMessage(R.string.backupDesc)
            setView(
                BackupBinding.inflate(layoutInflater).apply {
                    val f = Vita.Backup(c)
                    updateStatus()
                    for (butt in arrayOf(backup, export)) butt.background = RippleDrawable(
                        ColorStateList.valueOf(
                            c.color(com.google.android.material.R.attr.colorPrimaryVariant)
                        ), null, MaterialShapeDrawable(
                            ShapeAppearanceModel.Builder().apply {
                                val dim = resources.getDimension(R.dimen.mediumCornerSize)
                                var premise = butt == backup
                                if (resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL)
                                    premise = !premise
                                if (premise) setBottomLeftCorner(CornerFamily.CUT, dim)
                                else setBottomRightCorner(CornerFamily.CUT, dim)
                            }.build()
                        )
                    )
                    backup.setOnClickListener { Vita.backup(c); updateStatus() }
                    restore.setOnClickListener {
                        MaterialAlertDialogBuilder(c).apply {
                            setTitle(c.resources.getString(R.string.restore))
                            setMessage(
                                c.resources.getString(R.string.backupRestoreSure, lastBackup(f))
                            )
                            setPositiveButton(R.string.yes) { _, _ ->
                                c.m.vita = Vita.loads(
                                    FileInputStream(f).use { String(it.readBytes()) }
                                ).also { vita -> vita.save(c) }
                                c.updateGrid()
                                Toast.makeText(c, R.string.done, Toast.LENGTH_LONG).show()
                            }
                            setNegativeButton(R.string.no, null)
                        }.show()
                    }
                    export.setOnClickListener {
                        if (!f.exists()) return@setOnClickListener
                        c.sendFile(
                            FileInputStream(f).use { it.readBytes() },
                            R.string.backup_file
                        )
                    }
                }.root
            )
        }.create()
    }

    override fun isCancelable(): Boolean = true

    /** @return the human-readable modification date of the backup. */
    private fun lastBackup(f: Vita.Backup): String {
        if (!f.exists()) return getString(R.string.never)
        val d = Kit.calType.create().apply { timeInMillis = f.lastModified() }
        return "${Kit.z(d[Calendar.YEAR], 4)}.${Kit.z(d[Calendar.MONTH] + 1)}." +
                "${Kit.z(d[Calendar.DAY_OF_MONTH])} - ${Kit.z(d[Calendar.HOUR_OF_DAY])}:" +
                "${Kit.z(d[Calendar.MINUTE])}:${Kit.z(d[Calendar.SECOND])}"
    }

    /** Updates the modification date of the backup file. */
    private fun BackupBinding.updateStatus() {
        val f = Vita.Backup(c)
        status.text = getString(
            R.string.backupStatus, lastBackup(f), Kit.showBytes(c, f.length())
        )
    }
}

/** A dialogue containing the guide for this app. */
class HelpDialog : BaseDialogue() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(c).apply {
            setTitle(R.string.navHelp)
            setMessage(R.string.help)
            setPositiveButton(R.string.ok, null)
        }.create()
    }
}
