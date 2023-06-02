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
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.emoji2.text.EmojiCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ir.mahdiparastesh.fortuna.Kit.SEXBOOK
import ir.mahdiparastesh.fortuna.Kit.calType
import ir.mahdiparastesh.fortuna.Kit.color
import ir.mahdiparastesh.fortuna.Kit.compareByDays
import ir.mahdiparastesh.fortuna.Kit.decSep
import ir.mahdiparastesh.fortuna.Kit.moveCalendarInMonths
import ir.mahdiparastesh.fortuna.Kit.resetHours
import ir.mahdiparastesh.fortuna.Kit.toValue
import ir.mahdiparastesh.fortuna.Kit.z
import ir.mahdiparastesh.fortuna.Vita.Companion.lunaMaxima
import ir.mahdiparastesh.fortuna.Vita.Companion.saveDies
import ir.mahdiparastesh.fortuna.Vita.Companion.showScore
import ir.mahdiparastesh.fortuna.Vita.Companion.toKey
import ir.mahdiparastesh.fortuna.databinding.ItemGridBinding
import ir.mahdiparastesh.fortuna.databinding.VariabilisBinding
import ir.mahdiparastesh.fortuna.misc.Numerals
import ir.mahdiparastesh.fortuna.misc.Sexbook
import java.util.*
import kotlin.math.abs
import kotlin.math.absoluteValue

/**
 * Main table of our calendar
 * Subclass of ListAdapter, customised to be used in a GridView, and list the days.
 */
@SuppressLint("SetTextI18n")
class Grid(private val c: Main) : ListAdapter {
    var luna = c.m.thisLuna()
    var sexbook: Sexbook.Data? = cacheSexbook()

    /** TextView inside the changeVar()'s dialogue. */
    var cvTvSexbook: TextView? = null

    private val cp: Int by lazy { c.color(com.google.android.material.R.attr.colorPrimary) }
    private val cs: Int by lazy { c.color(com.google.android.material.R.attr.colorSecondary) }
    private val tc: Int by lazy { c.color(android.R.attr.textColor) }
    private val cpo: Int by lazy { c.color(com.google.android.material.R.attr.colorOnPrimary) }
    private val cso: Int by lazy { c.color(com.google.android.material.R.attr.colorOnSecondary) }
    private val telEmojis = arrayOf(
        Pair("1", "1️⃣"), Pair("2", "2️⃣"), Pair("3", "3️⃣"),
        Pair("4", "4️⃣"), Pair("5", "5️⃣"), Pair("6", "6️⃣"),
        Pair("7", "7️⃣"), Pair("8", "8️⃣"), Pair("9", "9️⃣"),
        Pair("*", "*️⃣"), Pair("0", "0️⃣"), Pair("#", "#️⃣"),
    )

    override fun getCount(): Int = c.m.calendar.lunaMaxima()
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
    override fun getView(i: Int, convertView: View?, parent: ViewGroup): View =
        ItemGridBinding.inflate(c.layoutInflater, parent, false).apply {
            val score: Float? = luna[i] ?: luna.default
            val isEstimated = luna[i] == null && luna.default != null
            val numType = c.sp.getString(Kit.SP_NUMERAL_TYPE, Kit.arNumType)
                .let { if (it == Kit.arNumType) null else it }

            dies.text = Numerals.make(i + 1, numType)
            val enlarge =
                Numerals.all.find { it.jClass?.simpleName == numType }?.enlarge ?: false
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
            root.setOnClickListener { changeVar(i, cal) }
            root.setOnLongClickListener { detailDate(i, cal); true }
            if (c.m.luna == c.todayLuna && c.todayCalendar[Calendar.DAY_OF_MONTH] == i + 1)
                root.foreground = c.getDrawable(R.drawable.dies_today)
        }.root

    /** Invoked via {@link Main#updateGrid()} */
    fun onRefresh() {
        luna = c.m.thisLuna()
        sexbook = cacheSexbook()
    }

    /**
     * Return a copy of the main Sexbook data (<code>m.sexbook</code>)
     * in order to cache it in this class.
     */
    fun cacheSexbook() = c.m.sexbook?.let {
        val spl = c.m.luna!!.split(".")
        val yr = spl[0].toShort()
        val mo = spl[1].toShort()
        Sexbook.Data(
            it.reports.filter { x -> x.year == yr && x.month == mo },
            it.crushes.filter { x -> x.birthYear <= yr && x.birthMonth == mo }
        )
    }

    /**
     * Open an AlertDialog in order to let the user change the score of this day.
     *
     * @param i day
     * @param cal the calendar indicating that day
     */
    @SuppressLint("ClickableViewAccessibility")
    @Suppress("KotlinConstantConditions")
    fun changeVar(i: Int, cal: Calendar = c.m.calendar) {
        if (c.m.changingVar != null && !c.firstResume) return
        c.m.changingVar = i
        val bv = VariabilisBinding.inflate(c.layoutInflater)
        var dialogue: AlertDialog? = null
        var isCancelable = true
        arrayOf(bv.highlight, bv.verbum).forEach { it.background = c.varFieldBg }
        bv.picker.apply {
            maxValue = 12
            minValue = 0
            value = c.m.changingVarScore
                ?: (if (i != -1) luna[i]?.toVariabilis() else null)
                    ?: luna.default?.toVariabilis() ?: 6
            wrapSelectorWheel = false
            setFormatter { it.toScore().showScore() }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                textColor = c.color(android.R.attr.textColor)
                textSize = c.resources.displayMetrics.density * 25f
            }
            (this@apply[0] as EditText).also { it.filters = arrayOf() }
            if (c.m.changingVarScore != null) isCancelable = false
            setOnValueChangedListener { _, _, newVal ->
                c.m.changingVarScore = newVal
                dialogue?.setCancelable(false)
            }
        }
        bv.emoji.apply {
            setText(
                c.m.changingVarEmoji ?: (if (i != -1) luna.emojis[i] else luna.emoji)?.toString()
            )
            if (text.isEmpty()) luna.emoji?.also { hint = it }
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
            if (c.m.changingVarEmoji != null) isCancelable = false
            addTextChangedListener {
                c.m.changingVarEmoji = it.toString()
                dialogue?.setCancelable(false)
            }
        }
        bv.verbum.apply {
            setText(c.m.changingVarVerbum ?: (if (i != -1) luna.verba[i] else luna.verbum))
            if (c.m.changingVarVerbum != null) isCancelable = false
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
        cvTvSexbook = bv.sexbook
        bv.sexbook.appendCrushBirthdays(i)
        bv.sexbook.appendSexReports(i)
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
                luna.saveDies(
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
                cvTvSexbook = null
            }
            setCancelable(isCancelable)
        }.show()
        if (i > -1) {
            val isPast = c.todayCalendar.timeInMillis - (Kit.A_DAY * 6L) > cal.timeInMillis
            val isFuture = c.todayCalendar.timeInMillis + (Kit.A_DAY * 1L) < cal.timeInMillis
            if ((isPast && luna.diebus[i] != null) || isFuture) {
                bv.picker.isEnabled = false
                bv.picker.alpha = 0.4f
                bv.lock.isVisible = true
                if (isFuture)
                    bv.lock.setOnClickListener(Kit.LimitedToastAlert(c, R.string.scoreFuture))
                else { // obviously is past
                    bv.lock.setOnClickListener(Kit.LimitedToastAlert(c, R.string.holdLonger))
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
            setOnClickListener(Kit.LimitedToastAlert(c, R.string.holdLonger))
            setOnLongClickListener {
                if (c.m.vita == null) return@setOnLongClickListener true
                luna.saveDies(c, i, null, null, null)
                c.shake()
                dialogue?.dismiss(); true
            }
        }
    }

    /** Converts a NumberPicker integer into a Fortuna score. */
    private fun Int.toScore() = -(toFloat() - 6f) / 2f

    /** Converts a Fortuna score into a NumberPicker integer. */
    private fun Float.toVariabilis() = (-(this * 2f) + 6f).toInt()

    /** Checks if "<code>source</code>" has no emoji indicating a telephone character. */
    private fun hasNonEmojiNumber(source: CharSequence): Boolean {
        for (te in telEmojis)
            if (te.first in source && te.second !in source)
                return true
        return false
    }

    /**
     * Explains the birthdays of the crushes imported from the Sexbook app and puts them
     * inside the TextView, and makes the TextView visible.
     *
     * @param i day
     */
    fun TextView.appendCrushBirthdays(i: Int) {
        if (i == -1) return
        val birth = sexbook?.crushes?.filter { it.birthDay == (i + 1).toShort() }
        if (birth.isNullOrEmpty()) return

        val sb = StringBuilder()
        for (b in birth)
            sb.append("Happy ${b.theirs()} birthday!\n")
        sb.deleteCharAt(sb.length - 1)
        text = text.toString() + sb.toString()
        isVisible = true
    }

    /**
     * Explains the sex records imported from the Sexbook app and puts them inside the TextView,
     * and makes the TextView visible and clickable.
     *
     * @param i day
     */
    fun TextView.appendSexReports(i: Int) {
        if (i == -1) return
        val sex = sexbook?.reports?.filter { it.day == (i + 1).toShort() }
        if (sex.isNullOrEmpty()) return

        val sb = StringBuilder()
        if (text.isNotEmpty()) sb.append("\n")
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
        text = text.toString() + sb.toString()
        isVisible = true
        setOnLongClickListener {
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

    /**
     * When the user holds on a date, it must explain that date in other calendars and its
     * difference from today inside an AlertDialog.
     *
     * @param i day
     * @param cal the calendar indicating that day
     */
    fun detailDate(i: Int, cal: Calendar) {
        if (c.m.showingDate != null && !c.firstResume) return
        c.m.showingDate = i
        MaterialAlertDialogBuilder(c).apply {
            setTitle(
                "${c.m.luna!!}.${z(i + 1)} - " + DateFormatSymbols.getInstance(Kit.locale)
                    .weekdays[cal[Calendar.DAY_OF_WEEK]]
            )
            val sb = StringBuilder()
            for (oc in Kit.otherCalendars) {
                val d = oc.newInstance()
                d.timeInMillis = cal.timeInMillis
                sb.append("${oc.simpleName.substringBefore("Calendar")}: ")
                sb.append("${d.toKey()}.${z(d[Calendar.DAY_OF_MONTH])}\n")
            }
            val dif = c.todayCalendar.compareByDays(cal)
            sb.append(" => ").append(
                when {
                    dif == -1 -> c.getString(R.string.yesterday)
                    dif == 1 -> c.getString(R.string.tomorrow)
                    dif < 0 -> enumerate(R.string.difAgo, dif.absoluteValue)
                    dif > 0 -> enumerate(R.string.difLater, dif)
                    else -> c.getString(R.string.today)
                }
            )
            if (abs(dif) > c.todayCalendar.getLeastMaximum(Calendar.DAY_OF_MONTH)) {
                val expDif = explainDatesDif(c.todayCalendar, cal, dif > 0)
                if (expDif[0] != 0 || expDif[1] != 0) {
                    sb.append(" (")
                    val sep = c.getString(R.string.difSep)
                    if (expDif[0] != 0) sb.append(enumerate(R.string.difYears, expDif[0]) + sep)
                    if (expDif[1] != 0) sb.append(enumerate(R.string.difMonths, expDif[1]) + sep)
                    if (expDif[2] != 0) sb.append(enumerate(R.string.difDays, expDif[2]) + sep)
                    sb.deleteRange(sb.length - sep.length, sb.length)
                    sb.append(")")
                }
            }
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

    /**
     * Helper function for string resources which contain a number and a word with a probable
     * plurality; namely "(s)".
     * Only one "(" and one ")" are allowed and required in "res".
     *
     * @param res string resource
     * @param num number
     *
     * @return a clean string
     */
    private fun enumerate(@StringRes res: Int, num: Int): String {
        var ret = c.getString(res, num.decSep())
        ret = if (num == 1) ret.removeRange(ret.indexOf("("), ret.indexOf(")") + 1)
        else ret.substringBefore("(") + ret.substringAfter("(")
            .substringBefore(")") + ret.substringAfter(")")
        return ret
    }

    /** Explains the difference of the Calendar instances in years, months and days */
    private fun explainDatesDif(main: Calendar, other: Calendar, isFuture: Boolean): IntArray {
        val arr = IntArray(3)
        arr[0] = other[Calendar.YEAR] - main[Calendar.YEAR]
        arr[1] = (other[Calendar.MONTH] + 1) - (main[Calendar.MONTH] + 1)
        arr[2] = other[Calendar.DAY_OF_MONTH] - main[Calendar.DAY_OF_MONTH]
        if (isFuture) {
            /* the first part of the month always starts with 1,
             * but the second part of the month ends with changeable numbers;
             * therefore we need to use the previous month's maximum when explaining the future! */
            val prev = calType.newInstance().apply {
                timeInMillis = other.timeInMillis
                moveCalendarInMonths(false)
            }
            if (arr[2] < 0) {
                arr[2] += prev.getActualMaximum(Calendar.DAY_OF_MONTH)
                arr[1]--
            }
            if (arr[1] < 0) { // Can this part make mistakes in the Hebrew calendar?
                arr[1] += prev.getActualMaximum(Calendar.MONTH) + 1
                arr[0]--
            }
        } else {
            if (arr[2] > 0) {
                arr[2] = other.getActualMaximum(Calendar.DAY_OF_MONTH) - arr[2]
                arr[1]++
            } else arr[2] = abs(arr[2])
            if (arr[1] > 0) {
                arr[1] = (other.getActualMaximum(Calendar.MONTH) + 1) - arr[1]
                arr[0]++
            } else arr[1] = abs(arr[1])
            arr[0] = abs(arr[0])
        }
        return arr
    }
}
