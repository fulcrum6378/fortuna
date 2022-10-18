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
import android.text.InputFilter
import android.text.Spanned
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ListAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.emoji2.text.EmojiCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ir.mahdiparastesh.fortuna.Main.Companion.SEXBOOK
import ir.mahdiparastesh.fortuna.Main.Companion.calType
import ir.mahdiparastesh.fortuna.Main.Companion.color
import ir.mahdiparastesh.fortuna.Main.Companion.resetHours
import ir.mahdiparastesh.fortuna.Vita.Companion.lunaMaxima
import ir.mahdiparastesh.fortuna.Vita.Companion.saveDies
import ir.mahdiparastesh.fortuna.Vita.Companion.showScore
import ir.mahdiparastesh.fortuna.Vita.Companion.toKey
import ir.mahdiparastesh.fortuna.Vita.Companion.z
import ir.mahdiparastesh.fortuna.databinding.ItemDayBinding
import ir.mahdiparastesh.fortuna.databinding.VariabilisBinding
import java.util.*
import kotlin.math.absoluteValue

class ItemDay(private val c: Main) : ListAdapter {
    private val cp: Int by lazy { c.color(com.google.android.material.R.attr.colorPrimary) }
    private val cs: Int by lazy { c.color(com.google.android.material.R.attr.colorSecondary) }
    private val tc: Int by lazy { c.color(android.R.attr.textColor) }
    private val cpo: Int by lazy { c.color(com.google.android.material.R.attr.colorOnPrimary) }
    private val cso: Int by lazy { c.color(com.google.android.material.R.attr.colorOnSecondary) }
    val luna = c.m.thisLuna()
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

            dies.text = diesNum(i + 1, numType)
            val enlarge =
                BaseNumeral.all.find { it.jClass?.canonicalName == numType }?.enlarge ?: false
            if (enlarge) dies.textSize =
                (dies.textSize / c.resources.displayMetrics.density) * 1.75f
            variabilis.text = (if (isEstimated) "c. " else "") + score.showScore()

            (luna.verba[i]?.isNotBlank() == true).also { show ->
                verbumIcon.isVisible = show
                if (show) verbumIcon.setImageResource(R.drawable.verbum)
                else verbumIcon.setImageDrawable(null)
            }
            val emj = luna.emojis.getOrNull(i)
            emoji.text = emj
            emoji.isVisible = emj != null

            root.setBackgroundColor(
                when {
                    score != null && score > 0f -> {
                        dies.setTextColor(cpo)
                        variabilis.setTextColor(cpo)
                        verbumIcon.setColorFilter(cpo)
                        Color.valueOf(
                            cp.red.toValue(), cp.green.toValue(), cp.blue.toValue(),
                            score / Vita.MAX_RANGE
                        ).toArgb()
                    }
                    score != null && score < 0f -> {
                        dies.setTextColor(cso)
                        variabilis.setTextColor(cso)
                        verbumIcon.setColorFilter(cso)
                        Color.valueOf(
                            cs.red.toValue(), cs.green.toValue(), cs.blue.toValue(),
                            -score / Vita.MAX_RANGE
                        ).toArgb()
                    }
                    else -> {
                        dies.setTextColor(tc)
                        variabilis.setTextColor(tc)
                        verbumIcon.setColorFilter(tc)
                        Color.TRANSPARENT
                    }
                }
            )
            val cal = calType.newInstance().apply {
                timeInMillis = c.m.calendar.timeInMillis
                set(Calendar.DAY_OF_MONTH, i + 1)
                resetHours()
            }
            root.setOnClickListener {
                luna.changeVar(c, i, cal, sexbook?.filter { it.day == (i + 1).toShort() })
            }
            root.setOnLongClickListener { detailDate(c, i, cal); true }
            if (c.m.luna == c.todayLuna && c.todayCalendar[Calendar.DAY_OF_MONTH] == i + 1)
                root.foreground = c.getDrawable(R.drawable.dies_today)
        }.root

    override fun hasStableIds(): Boolean = true
    override fun getItemViewType(i: Int): Int = 0
    override fun getViewTypeCount(): Int = 1
    override fun isEmpty(): Boolean = false
    override fun areAllItemsEnabled(): Boolean = true
    override fun isEnabled(i: Int): Boolean = true

    companion object {
        private val telEmojis = arrayOf(
            Pair("1", "1️⃣"), Pair("2", "2️⃣"), Pair("3", "3️⃣"),
            Pair("4", "4️⃣"), Pair("5", "5️⃣"), Pair("6", "6️⃣"),
            Pair("7", "7️⃣"), Pair("8", "8️⃣"), Pair("9", "9️⃣"),
            Pair("*", "*️⃣"), Pair("0", "0️⃣"), Pair("#", "#️⃣"),
        )

        fun hasNonEmojiNumber(source: CharSequence): Boolean {
            for (te in telEmojis)
                if (te.first in source && te.second !in source)
                    return true
            return false
        }

        fun Int.toValue() = toFloat() / 256f

        private fun Int.toScore() = -(toFloat() - 6f) / 2f

        private fun Float.toVariabilis() = (-(this * 2f) + 6f).toInt()

        @SuppressLint("ClickableViewAccessibility")
        @Suppress("KotlinConstantConditions")
        fun Luna.changeVar(
            c: Main, i: Int,
            cal: Calendar = c.m.calendar, sex: List<Main.Sex>? = null
        ) {
            if (c.m.changingVar != null && !c.firstResume) return
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
            bv.emoji.apply {
                setText(
                    c.m.changingVarEmoji
                        ?: (if (i != -1) this@changeVar.emojis[i] else emoji)?.toString()
                )
                if (text.isEmpty()) emoji?.also { hint = it }
                filters = arrayOf(object : InputFilter {
                    override fun filter(
                        source: CharSequence?, start: Int, end: Int,
                        dest: Spanned?, dstart: Int, dend: Int
                    ): CharSequence? = when {
                        this@apply.text.isNotEmpty() -> ""
                        source == null -> null
                        EmojiCompat.get().getEmojiMatch(source, 16) in 1..2
                                && !hasNonEmojiNumber(source) -> null
                        else -> ""
                    } // do NOT invoke "setText()" in a filter!
                })
                addTextChangedListener {
                    c.m.changingVarEmoji = it.toString()
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
                setOnTouchListener { v, event -> // scroll inside ScrollView
                    var ret = false
                    v.parent.requestDisallowInterceptTouchEvent(true)
                    if ((event.action and MotionEvent.ACTION_MASK) == MotionEvent.ACTION_SCROLL) {
                        v.parent.requestDisallowInterceptTouchEvent(false)
                        ret = true
                    }; ret
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
                bv.sexbook.isVisible = true
                bv.sexbook.setOnLongClickListener {
                    try {
                        c.startActivity(
                            Intent("$SEXBOOK.ACTION_VIEW")
                                .setComponent(ComponentName(SEXBOOK, "$SEXBOOK.Main"))
                                .setData(android.net.Uri.parse(sex.first().id.toString()))
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        ); true
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
                    saveDies(
                        c, i, bv.picker.value.toScore(),
                        bv.emoji.text.toString(), bv.verbum.text.toString()
                    )
                    c.shake()
                }
                setNeutralButton(R.string.clear) { _, _ -> }
                setOnDismissListener {
                    bv.verbum.clearFocus()
                    c.m.changingVar = null
                    c.m.changingVarScore = null
                    c.m.changingVarEmoji = null
                    c.m.changingVarVerbum = null
                }
                setCancelable(true)
            }.show()
            if (i > -1) {
                val isPast = c.todayCalendar.timeInMillis - (Main.A_DAY * 6L) > cal.timeInMillis
                val isFuture = c.todayCalendar.timeInMillis + (Main.A_DAY * 1L) < cal.timeInMillis
                if ((isPast && diebus[i] != null) || isFuture) {
                    bv.picker.isEnabled = false
                    bv.picker.alpha = 0.4f
                    bv.lock.isVisible = true
                    if (isFuture)
                        bv.lock.setOnClickListener(Main.LimitedToastAlert(c, R.string.scoreFuture))
                    else { // obviously is past
                        bv.lock.setOnClickListener(Main.LimitedToastAlert(c, R.string.holdLonger))
                        bv.lock.setOnLongClickListener {
                            bv.lock.isVisible = false
                            bv.lock.setOnClickListener(null)
                            bv.lock.setOnLongClickListener(null)
                            bv.picker.alpha = 1f
                            bv.picker.isEnabled = true
                            true
                        }
                    }
                }
            }
            dialogue.getButton(AlertDialog.BUTTON_NEUTRAL).apply {
                setOnClickListener(Main.LimitedToastAlert(c, R.string.holdLonger))
                setOnLongClickListener {
                    if (c.m.vita == null) return@setOnLongClickListener true
                    saveDies(c, i, null, null, null)
                    c.shake()
                    dialogue?.dismiss(); true
                }
            }
        }

        fun detailDate(c: Main, i: Int, cal: Calendar) {
            if (c.m.showingDate != null && !c.firstResume) return
            c.m.showingDate = i
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
                val dif = ((cal.timeInMillis - c.todayCalendar.timeInMillis) / Main.A_DAY).toInt()
                sb.append(" => ").append(
                    when {
                        dif == -1 -> c.getString(R.string.yesterday)
                        dif == 1 -> c.getString(R.string.tomorrow)
                        dif < 0 -> c.getString(R.string.difAgo, dif.absoluteValue)
                        dif > 0 -> c.getString(R.string.difLater, dif)
                        else -> c.getString(R.string.today)
                    }
                )
                setMessage(sb.toString())
                setPositiveButton(R.string.ok, null)
                setNeutralButton(R.string.viewInCalendar) { _, _ ->
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

        fun diesNum(i: Int, numType: String?): String = (numType?.let { BaseNumeral.find(it) }
            ?.constructors?.getOrNull(0)?.newInstance(i) as BaseNumeral?)
            ?.toString() ?: i.toString()
    }
}
