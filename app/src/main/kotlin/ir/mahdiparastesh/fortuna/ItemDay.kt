package ir.mahdiparastesh.fortuna

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.database.DataSetObserver
import android.graphics.Color
import android.icu.text.DateFormatSymbols
import android.icu.util.Calendar
import android.os.Build
import android.provider.CalendarContract
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ListAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.core.view.get
import androidx.core.widget.addTextChangedListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ir.mahdiparastesh.fortuna.Main.Companion.SEXBOOK
import ir.mahdiparastesh.fortuna.Main.Companion.calType
import ir.mahdiparastesh.fortuna.Main.Companion.color
import ir.mahdiparastesh.fortuna.Main.Companion.vis
import ir.mahdiparastesh.fortuna.Vita.Companion.lunaMaxima
import ir.mahdiparastesh.fortuna.Vita.Companion.saveDies
import ir.mahdiparastesh.fortuna.Vita.Companion.showScore
import ir.mahdiparastesh.fortuna.Vita.Companion.toKey
import ir.mahdiparastesh.fortuna.Vita.Companion.z
import ir.mahdiparastesh.fortuna.databinding.ItemDayBinding
import ir.mahdiparastesh.fortuna.databinding.VariabilisBinding
import java.util.*

class ItemDay(private val c: Main) : ListAdapter {
    private val cp: Int by lazy { c.color(com.google.android.material.R.attr.colorPrimary) }
    private val cs: Int by lazy { c.color(com.google.android.material.R.attr.colorSecondary) }
    private val tc: Int by lazy { c.color(android.R.attr.textColor) }
    private val cpo: Int by lazy { c.color(com.google.android.material.R.attr.colorOnPrimary) }
    private val cso: Int by lazy { c.color(com.google.android.material.R.attr.colorOnSecondary) }
    val luna = c.m.thisLuna()
    private val todayCalendar = calType.newInstance()
    private val todayLuna = todayCalendar.toKey()
    private val sexbook: List<Main.Sex>? by lazy {
        c.m.sexbook?.let {
            val spl = c.m.luna!!.split(".")
            val yr = spl[0].toShort()
            val mo = spl[1].toShort()
            it.filter { x -> x.year == yr && x.month == mo }
        }
    }

    override fun registerDataSetObserver(observer: DataSetObserver) {}
    override fun unregisterDataSetObserver(observer: DataSetObserver) {}
    override fun getCount(): Int = c.m.calendar.lunaMaxima()
    override fun getItem(i: Int): Float = 0f
    override fun getItemId(i: Int): Long = i.toLong()

    @SuppressLint("SetTextI18n", "ViewHolder", "UseCompatLoadingForDrawables")
    override fun getView(i: Int, convertView: View?, parent: ViewGroup): View =
        ItemDayBinding.inflate(c.layoutInflater, parent, false).apply {
            val score: Float? = luna[i] ?: luna.default
            val isEstimated = luna[i] == null && luna.default != null
            val numType = c.sp.getString(Main.SP_NUMERAL_TYPE, Main.arNumType)
                .let { if (it == Main.arNumType) null else it }

            dies.text = (numType?.let { BaseNumeral.find(it) }
                ?.constructors?.getOrNull(0)?.newInstance(i + 1) as BaseNumeral?)
                ?.toString() ?: "${i + 1}"
            val enlarge =
                BaseNumeral.all.find { it.jClass?.canonicalName == numType }?.enlarge ?: false
            if (enlarge) dies.textSize =
                (dies.textSize / c.resources.displayMetrics.density) * 1.75f
            variabilis.text = (if (isEstimated) "c. " else "") + score.showScore()
            (luna.verba[i]?.isNotBlank() == true).also { show ->
                verbum.vis(show)
                if (show) verbum.setImageResource(R.drawable.verbum)
                else verbum.setImageDrawable(null)
            }

            root.setBackgroundColor(
                when {
                    score != null && score > 0f -> {
                        dies.setTextColor(cpo)
                        variabilis.setTextColor(cpo)
                        verbum.setColorFilter(cpo)
                        Color.valueOf(
                            cp.red.toValue(), cp.green.toValue(), cp.blue.toValue(),
                            score / Vita.MAX_RANGE
                        ).toArgb()
                    }
                    score != null && score < 0f -> {
                        dies.setTextColor(cso)
                        variabilis.setTextColor(cso)
                        verbum.setColorFilter(cso)
                        Color.valueOf(
                            cs.red.toValue(), cs.green.toValue(), cs.blue.toValue(),
                            -score / Vita.MAX_RANGE
                        ).toArgb()
                    }
                    else -> {
                        dies.setTextColor(tc)
                        variabilis.setTextColor(tc)
                        verbum.setColorFilter(tc)
                        Color.TRANSPARENT
                    }
                }
            )
            root.setOnClickListener {
                luna.changeVar(c, i, sexbook?.filter { it.day == (i + 1).toShort() })
            }
            root.setOnLongClickListener { showDate(c, i); true }
            if (c.m.luna == todayLuna && todayCalendar[Calendar.DAY_OF_MONTH] == i + 1)
                root.foreground = c.getDrawable(R.drawable.dies_today)
        }.root

    override fun hasStableIds(): Boolean = true
    override fun getItemViewType(i: Int): Int = 0
    override fun getViewTypeCount(): Int = 1
    override fun isEmpty(): Boolean = false
    override fun areAllItemsEnabled(): Boolean = true
    override fun isEnabled(i: Int): Boolean = true

    companion object {
        fun Int.toValue() = toFloat() / 256f

        private fun Int.toScore() = -(toFloat() - 6f) / 2f

        private fun Float.toVariabilis() = (-(this * 2f) + 6f).toInt()

        fun Luna.changeVar(c: Main, i: Int, sex: List<Main.Sex>? = null) {
            if (c.m.changingVar != null) return
            c.m.changingVar = i
            val bv = VariabilisBinding.inflate(c.layoutInflater)
            var dialogue: AlertDialog? = null
            arrayOf(bv.highlight, bv.verbum).forEach { it.background = c.varFieldBg }
            bv.picker.apply {
                maxValue = 12
                minValue = 0
                value = c.m.changingVarScore
                    ?: (if (i != -1) this@changeVar[i]?.toVariabilis() else null)
                            ?: default?.toVariabilis() ?: 6
                wrapSelectorWheel = false
                setFormatter { it.toScore().showScore() }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    textColor = c.color(android.R.attr.textColor)
                    textSize = c.resources.displayMetrics.density * 25f
                }
                (this@apply[0] as EditText).also { it.filters = arrayOf() }
                setOnValueChangedListener { _, _, newVal ->
                    c.m.changingVarScore = newVal
                    dialogue?.setCancelable(false)
                }
            }
            bv.verbum.apply {
                setText(
                    c.m.changingVarVerbum ?: (if (i != -1) this@changeVar.verba[i] else verbum)
                )
                addTextChangedListener {
                    c.m.changingVarVerbum = it.toString()
                    dialogue?.setCancelable(false)
                }
            }
            if (i != -1 && sex?.isNotEmpty() == true) {
                val sb = StringBuilder()
                for (x in sex) {
                    sb.append(
                        when (x.type) {
                            0.toByte() -> "Had a wet dream"
                            1.toByte() -> "Masturbated"
                            2.toByte() -> "Had oral sex"
                            3.toByte() -> "Had anal sex"
                            4.toByte() -> "Had vaginal sex"
                            else -> continue
                        }
                    )
                    if (x.key.isNotBlank()) sb.append(" with ${x.key}")
                    if (x.accurate) sb.append(" at ${z(x.hour)}:${z(x.minute)}:${z(x.second)}")
                    else sb.append(" at ~${z(x.hour)}:${z(x.minute)}")
                    if (x.place?.isNotBlank() == true) sb.append(" in ${x.place}")
                    if (x.desc.isBlank()) sb.append(".\n")
                    else sb.append(": ${x.desc}\n")
                }
                sb.deleteCharAt(sb.length - 1)
                bv.sexbook.text = sb.toString()
                bv.sexbook.vis()
                bv.sexbook.setOnLongClickListener {
                    try {
                        c.startActivity(
                            Intent("$SEXBOOK.ACTION_VIEW")
                                .setComponent(ComponentName(SEXBOOK, "$SEXBOOK.Main"))
                                .setData(android.net.Uri.parse(sex.first().id.toString()))
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                        true
                    } catch (e: ActivityNotFoundException) {
                        false
                    }
                }
            }
            dialogue = MaterialAlertDialogBuilder(c).apply {
                setTitle(
                    c.getString(
                        R.string.variabilis,
                        if (i != -1) "${c.m.luna!!}.${z(i + 1)}"
                        else c.getString(R.string.defValue)
                    )
                )
                setView(bv.root)
                setNegativeButton(R.string.cancel, null)
                setPositiveButton(R.string.save) { _, _ ->
                    if (c.m.vita == null) return@setPositiveButton
                    saveDies(c, i, bv.picker.value.toScore(), bv.verbum.text.toString())
                    c.shake()
                }
                setNeutralButton(R.string.clear) { _, _ ->
                    if (c.m.vita == null) return@setNeutralButton
                    saveDies(c, i, null, null)
                    c.shake()
                }
                setOnDismissListener {
                    c.m.changingVar = null
                    c.m.changingVarScore = null
                    c.m.changingVarVerbum = null
                }
                setCancelable(true)
            }.show()
        }

        fun showDate(c: Main, i: Int) {
            if (c.m.showingDate != null) return
            c.m.showingDate = i
            val cal = calType.newInstance().apply {
                timeInMillis = c.m.calendar.timeInMillis
                this[Calendar.DAY_OF_MONTH] = i + 1
            }
            MaterialAlertDialogBuilder(c).apply {
                setTitle(
                    "${c.m.luna!!}.${z(i + 1)} - " +
                            DateFormatSymbols.getInstance(Locale.UK).weekdays[cal[Calendar.DAY_OF_WEEK]]
                )
                val sb = StringBuilder()
                for (oc in Main.otherCalendars) {
                    val d = oc.newInstance()
                    d.timeInMillis = cal.timeInMillis
                    sb.append("${oc.simpleName.substringBefore("Calendar")}: ")
                    sb.append("${d.toKey()}.${z(d[Calendar.DAY_OF_MONTH])}\n")
                }
                sb.deleteCharAt(sb.length - 1)
                setMessage(sb.toString())
                setPositiveButton(R.string.ok, null)
                setNeutralButton(R.string.viewInCalendar) { _, _ ->
                    cal[Calendar.DAY_OF_MONTH] = i + 1
                    c.startActivity(
                        Intent(Intent.ACTION_VIEW).setData(
                            CalendarContract.CONTENT_URI.buildUpon()
                                .appendPath("time")
                                .appendEncodedPath(cal.timeInMillis.toString()).build()
                        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
                setOnDismissListener { c.m.showingDate = null }
            }.show()
        }
    }
}
