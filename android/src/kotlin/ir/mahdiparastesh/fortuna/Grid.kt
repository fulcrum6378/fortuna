package ir.mahdiparastesh.fortuna

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.database.DataSetObserver
import android.graphics.Color
import android.os.Build
import android.provider.CalendarContract
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ListAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.net.toUri
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import ir.mahdiparastesh.chrono.IranianChronology
import ir.mahdiparastesh.fortuna.databinding.DateComparisonBinding
import ir.mahdiparastesh.fortuna.databinding.ItemGridBinding
import ir.mahdiparastesh.fortuna.databinding.VariabilisBinding
import ir.mahdiparastesh.fortuna.util.LimitedToastAlert
import ir.mahdiparastesh.fortuna.util.NumberUtils.displayScore
import ir.mahdiparastesh.fortuna.util.NumberUtils.groupDigits
import ir.mahdiparastesh.fortuna.util.NumberUtils.toKey
import ir.mahdiparastesh.fortuna.util.NumberUtils.toScore
import ir.mahdiparastesh.fortuna.util.NumberUtils.toVariabilis
import ir.mahdiparastesh.fortuna.util.NumberUtils.write
import ir.mahdiparastesh.fortuna.util.NumberUtils.z
import ir.mahdiparastesh.fortuna.util.Numeral
import ir.mahdiparastesh.fortuna.util.Numerals
import ir.mahdiparastesh.fortuna.util.Sexbook
import ir.mahdiparastesh.fortuna.util.UiTools
import ir.mahdiparastesh.fortuna.util.UiTools.color
import java.time.DateTimeException
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.chrono.ChronoLocalDate
import java.time.chrono.HijrahChronology
import java.time.chrono.IsoChronology
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.abs
import kotlin.math.absoluteValue

/** Main table of our calendar grid which lists days of a month */
@SuppressLint("SetTextI18n")
class Grid(private val c: Main) : ListAdapter {
    lateinit var luna: Luna
    var sexbook: Sexbook.Data? = null
    private var numType: String? = null
    private var numeral: Numeral? = null
    var maximumStats: Int? = null

    init {
        onRefresh()
    }

    /** Reference to the [TextView] inside the dialogue of [changeVar] */
    var cvTvSexbook: TextView? = null

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

    /** Improved drawable for the fields in [changeVar] */
    private val varFieldBg: MaterialShapeDrawable by lazy {
        MaterialShapeDrawable(
            ShapeAppearanceModel.Builder()
                .setAllCorners(CornerFamily.CUT, c.resources.getDimension(R.dimen.smallCornerSize))
                .build()
        ).apply { fillColor = c.resources.getColorStateList(R.color.varField, null) }
    }

    @SuppressLint("ViewHolder", "UseCompatLoadingForDrawables")
    override fun getView(i: Int, convertView: View?, parent: ViewGroup): View {
        val b = convertView?.let { ItemGridBinding.bind(it) }
            ?: ItemGridBinding.inflate(c.layoutInflater, parent, false)

        // calculation
        val score: Float? =
            if (i < (maximumStats ?: 0)) luna[i] ?: luna.default else null
        val isEstimated = i < (maximumStats ?: 0) && luna[i] == null && luna.default != null

        // numbers
        b.dies.text = numeral.write(i + 1)
        val enlarge = Numerals.all.find { it.jClass?.simpleName == numType }?.enlarge == true
        if (enlarge) b.dies.textSize =
            (b.dies.textSize / c.resources.displayMetrics.density) * 1.75f
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
            duration = 100L
            start()
        }
        cellColours[i] = targetColour

        // clicks
        b.root.setOnClickListener { changeVar(i, dailyCalendar(i)) }
        b.root.setOnLongClickListener { detailDate(i, dailyCalendar(i)); true }

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
        sexbook = cacheSexbook()
        numType = c.c.sp.getString(Fortuna.SP_NUMERAL_TYPE, Fortuna.SP_NUMERAL_TYPE_DEF)
            .let { if (it == Fortuna.SP_NUMERAL_TYPE_DEF) null else it }
        numeral = Numerals.build(numType)
        maximumStats = c.c.maximaForStats(c.c.date, c.c.luna)
    }

    /** Returns a filtered version of the Sexbook data [Main.Model.sexbook] to be stored as cache. */
    fun cacheSexbook() = c.m.sexbook?.let {
        val spl = c.c.luna.split(".")
        val yr = spl[0].toShort()
        val mo = spl[1].toInt().toShort()
        Sexbook.Data(
            it.reports.filter { x -> x.year == yr && x.month == mo },
            it.crushes.filter { x ->
                (x.birthYear != null && x.birthYear!! <= yr && x.birthMonth == mo) ||
                        (x.firstMetYear == yr && x.firstMetMonth == mo)
            }
        )
    }

    /** Creates a calendar indicating this day. */
    fun dailyCalendar(i: Int): ChronoLocalDate =
        c.c.date.with(ChronoField.DAY_OF_MONTH, i + 1L)

    /**
     * Opens an [AlertDialog] in order to let the user change the score of this day.
     *
     * @param i day (starting from 0)
     * @param date the calendar indicating that day
     */
    @SuppressLint("ClickableViewAccessibility")
    @Suppress("KotlinConstantConditions")
    fun changeVar(i: Int, date: ChronoLocalDate = c.c.date) {
        if (c.m.changingVar != null) return
        c.m.changingVar = i
        val bv = VariabilisBinding.inflate(c.layoutInflater)
        var dialogue: AlertDialog? = null
        var isCancelable = true
        arrayOf(bv.highlight, bv.verbum).forEach { it.background = varFieldBg }

        // score picker
        bv.picker.apply {
            maxValue = 12
            minValue = 0
            value = c.m.changingVarScore
                ?: (if (i != -1) luna[i]?.toVariabilis() else null)
                        ?: luna.default?.toVariabilis() ?: 6
            wrapSelectorWheel = false
            setFormatter { it.toScore().displayScore(false) }
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

        // emojis
        bv.emoji.apply {
            setText(
                c.m.changingVarEmoji ?: (if (i != -1) luna.emojis[i] else luna.emoji)?.toString()
            )
            if (text.isEmpty()) luna.emoji?.also { hint = it }
            filters = arrayOf(EmojiFilter(this@apply))
            if (c.m.changingVarEmoji != null) isCancelable = false
            addTextChangedListener {
                c.m.changingVarEmoji = it.toString()
                dialogue?.setCancelable(false)
            }
        }

        // descriptions
        bv.verbum.apply {
            setText(c.m.changingVarVerbum ?: (if (i != -1) luna.verba[i] else luna.verbum))
            if (c.m.changingVarVerbum != null) isCancelable = false
            addTextChangedListener {
                c.m.changingVarVerbum = it.toString()
                dialogue?.setCancelable(false)
            }
            setOnTouchListener { v, event ->  // scroll inside ScrollView
                var ret = false
                v.parent.requestDisallowInterceptTouchEvent(true)
                if ((event.action and MotionEvent.ACTION_MASK) == MotionEvent.ACTION_SCROLL) {
                    v.parent.requestDisallowInterceptTouchEvent(false)
                    ret = true
                }; ret
            }
            // https://stackoverflow.com/a/54118763/10728785   : equal to "clipToPadding"!
            setShadowLayer(extendedPaddingBottom.toFloat(), 0f, 0f, Color.TRANSPARENT)
        }

        // Sexbook records for this day
        cvTvSexbook = bv.sexbook
        if (i != -1) {
            bv.sexbook.appendCrushDates(i.toShort(), date[ChronoField.YEAR].toShort())
            bv.sexbook.appendSexReports(i)
        }

        dialogue = MaterialAlertDialogBuilder(c).apply {
            setTitle(
                if (i != -1) "${c.c.luna}.${z(i + 1)}"
                else c.getString(R.string.defValue)
            )
            setView(bv.root)
            setNegativeButton(R.string.cancel, null)
            setPositiveButton(R.string.save) { _, _ ->
                c.saveDies(
                    luna, i, bv.picker.value.toScore(),
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
            val isPast = date.isBefore(c.c.todayDate) &&
                    c.c.todayDate.until(date, ChronoUnit.DAYS) < -7
            val isFuture = date.isAfter(c.c.todayDate) &&
                    c.c.todayDate.until(date, ChronoUnit.DAYS) >= 1
            if ((isPast && luna.diebus[i] != null) || isFuture) {
                bv.picker.isEnabled = false
                bv.picker.alpha = 0.4f
                bv.lock.isVisible = true
                if (isFuture)
                    bv.lock.setOnClickListener(LimitedToastAlert(c, R.string.scoreFuture))
                else {  // is the past
                    bv.lock.setOnClickListener(LimitedToastAlert(c, R.string.holdLonger))
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
            setOnClickListener(LimitedToastAlert(c, R.string.holdLonger))
            setOnLongClickListener {
                c.saveDies(luna, i, null, null, null)
                c.shake()
                dialogue?.dismiss(); true
            }
        }
    }

    /**
     * Elaborates birthdays and other special dates related to crushes imported from the Sexbook app
     * and puts them inside the specified TextView, and makes the TextView visible.
     * Unfortunately estimated dates cannot be imported because of the difference in the calendars!
     */
    fun TextView.appendCrushDates(day: Short, year: Short) {
        if (day == (-1).toShort()) return
        val birth = sexbook?.crushes
            ?.filter { it.birthDay == day }
            ?.sortedBy { it.birthTime }
            ?.sortedBy { it.birthDay }
        val firstMet = sexbook?.crushes
            ?.filter { it.firstMetDay == day }
            ?.sortedBy { it.firstMetTime }
            ?.sortedBy { it.firstMetDay }
        if (birth.isNullOrEmpty() && firstMet.isNullOrEmpty()) return

        val sb = StringBuilder()
        if (!birth.isNullOrEmpty()) for (b in birth) {
            val age = year - b.birthYear!!
            if (age > 0) {
                if (b.active) sb.append("Happy ")
                sb.append("${b.theirs()} ")
                val sAge = age.toString()
                sb.append(sAge).append(
                    if (sAge.first() != '1' || sAge.length != 2) when (sAge.last()) {
                        '1' -> "st"
                        '2' -> "nd"
                        '3' -> "rd"
                        else -> "th"
                    } else "th"
                ).append(" birthday!\n")
            } else sb.append(b.visName().uppercase(Locale.getDefault()))
                .append(" was born${if (b.birthTime != null) " at ${b.birthTime}" else ""}!\n")
        }
        if (!firstMet.isNullOrEmpty()) for (fm in firstMet)
            sb.append(
                "Met ${fm.visName()} for the first time" +
                        "${if (fm.firstMetTime != null) " at ${fm.firstMetTime}" else ""}!\n"
            )

        sb.deleteCharAt(sb.length - 1)
        text = text.toString() + sb.toString()
        isVisible = true
    }

    /**
     * Elaborates sex records imported from the Sexbook app and puts them inside the specified
     * TextView, and makes the TextView visible and clickable.
     *
     * @param i day
     */
    fun TextView.appendSexReports(i: Int) {
        if (i == -1) return
        val da = (i + 1).toShort()
        val sex = sexbook?.reports?.filter { it.day == da }
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
            if (!x.key.isNullOrBlank()) sb.append(" with ${x.key}")
            if (x.accurate) sb.append(" at ${z(x.hour)}:${z(x.minute)}:${z(x.second)}")
            else sb.append(" at ~${z(x.hour)}:${z(x.minute)}")
            if (x.place?.isNotBlank() == true) sb.append(" in ${x.place}")
            if (x.desc.isNullOrBlank()) sb.append(".\n")
            else sb.append(": ${x.desc}\n")
        }
        sb.deleteCharAt(sb.length - 1)
        text = text.toString() + sb.toString()
        isVisible = true
        setOnLongClickListener {
            try {
                c.startActivity(
                    Intent("${Sexbook.PACKAGE}.ACTION_VIEW")
                        .setComponent(ComponentName(Sexbook.PACKAGE, "${Sexbook.PACKAGE}.Main"))
                        .setData(sex.first().id.toString().toUri())
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                ); true
            } catch (_: ActivityNotFoundException) {
                false
            }
        }
    }

    /**
     * When the user holds on a date, it must explain that date in other calendars and its
     * difference from today inside an AlertDialog.
     *
     * @param i day
     * @param date the calendar indicating that day
     */
    fun detailDate(i: Int, date: ChronoLocalDate) {
        if (c.m.showingDate != null) return
        c.m.showingDate = i
        MaterialAlertDialogBuilder(c).apply {
            setTitle(
                "${c.c.luna}.${z(i + 1)} - " +
                        c.resources.getStringArray(R.array.weekDays)[
                            date[ChronoField.DAY_OF_WEEK] - 1]
            )
            setMessage(StringBuilder().apply {
                val epochDay = date.toEpochDay()
                for (oc in c.c.otherChronologies()) {
                    val d = try {
                        oc.dateEpochDay(epochDay)
                    } catch (_: DateTimeException) {
                        continue  // some calendar do not support ancient dates
                    }
                    val visualName = c.getString(
                        when (oc) {
                            is IranianChronology -> R.string.calIranian
                            is IsoChronology -> R.string.calGregorian
                            is HijrahChronology -> R.string.calIslamic
                            else -> throw IllegalStateException(
                                "Please add a string resource for this new Chronology."
                            )
                        }
                    )
                    append("$visualName: ${d.toKey()}.${z(d[ChronoField.DAY_OF_MONTH])}\n")
                }
            }.toString())
            setView(DateComparisonBinding.inflate(c.layoutInflater).apply {
                dat.background = varFieldBg
                val watcher = object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
                    override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
                    override fun afterTextChanged(s: Editable?) {
                        result.text = try {
                            result.isVisible = true
                            val dat = c.c.chronology.date(
                                y.text.toString().toInt(),
                                (m.text.toString().toInt()),
                                d.text.toString().toInt()
                            )
                            c.m.compareDatesWith = dat
                            dateComparison(date, dat)
                        } catch (_: DateTimeException) {
                            result.isVisible = false
                            ""
                        }
                    }
                }
                val dit = c.m.compareDatesWith ?: c.c.todayDate
                y.setText(z(dit[ChronoField.YEAR]))
                m.setText(z(dit[ChronoField.MONTH_OF_YEAR]))
                y.addTextChangedListener(watcher)
                m.addTextChangedListener(watcher)
                d.addTextChangedListener(watcher)
                d.setText(z(dit[ChronoField.DAY_OF_MONTH]))

                result.setOnLongClickListener {
                    UiTools.copyToClipboard(c.c, result.text, null); true
                }
            }.root)
            setPositiveButton(R.string.ok, null)
            setNeutralButton(R.string.viewInCalendar) { _, _ ->
                c.startActivity(
                    Intent(Intent.ACTION_VIEW).setData(
                        CalendarContract.CONTENT_URI.buildUpon()
                            .appendPath("time")
                            .appendEncodedPath(
                                (date.atTime(LocalTime.of(0, 0, 0))
                                    .toEpochSecond(OffsetDateTime.now().offset) * 1000L)
                                    .toString()
                            )
                            .build()
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
            setOnDismissListener { c.m.showingDate = null }
        }.show()
    }

    private fun dateComparison(dit: ChronoLocalDate, dat: ChronoLocalDate): String {
        val dif = dat.until(dit, ChronoUnit.DAYS).toInt()
        val basedOnToday = dat == c.c.todayDate
        val sb = StringBuilder()
        sb.append(" => ").append(
            when {
                basedOnToday && dif == -1 -> c.getString(R.string.yesterday)
                basedOnToday && dif == 1 -> c.getString(R.string.tomorrow)
                dif < 0 -> c.getString(
                    if (basedOnToday) R.string.difAgo else R.string.difBefore,
                    c.resources.getQuantityString(
                        R.plurals.day, dif.absoluteValue, dif.absoluteValue.groupDigits()
                    )
                )

                dif > 0 -> c.getString(
                    if (basedOnToday) R.string.difLater else R.string.difAfter,
                    c.resources.getQuantityString(R.plurals.day, dif, dif.groupDigits())
                )

                else -> c.getString(if (basedOnToday) R.string.today else R.string.sameDay)
            }
        )
        if (abs(dif) > dat.lengthOfMonth()) {
            val expDif = dat.until(dit)
            val years = abs(expDif[ChronoUnit.YEARS].toInt())
            val months = abs(expDif[ChronoUnit.MONTHS].toInt())
            val days = abs(expDif[ChronoUnit.DAYS].toInt())
            if (years != 0 || months != 0) {
                sb.append(" (")
                val sep = c.getString(R.string.difSep)
                if (years != 0)
                    sb.append(c.resources.getQuantityString(R.plurals.year, years, years))
                        .append(sep)
                if (months != 0)
                    sb.append(c.resources.getQuantityString(R.plurals.month, months, months))
                        .append(sep)
                if (days != 0)
                    sb.append(c.resources.getQuantityString(R.plurals.day, days, days))
                        .append(sep)
                sb.deleteRange(sb.length - sep.length, sb.length)
                sb.append(")")
            }
        }
        return sb.toString()
    }


    inner class EmojiFilter(private val field: EditText) : InputFilter {
        override fun filter(
            source: CharSequence?, start: Int, end: Int,
            dest: Spanned?, dstart: Int, dend: Int
        ): CharSequence? {
            if (field.text.isNotEmpty()) return ""
            if (source == null || c.m.emojis.any { it == source }) return null

            val ba = source.toString().toByteArray(Charsets.UTF_8)
            val bz = ba.size
            if (bz > 3 && // Android's excess char
                ba[bz - 1] == (-113).toByte() &&
                ba[bz - 2] == (-72).toByte() &&
                ba[bz - 3] == (-17).toByte()
            ) return null
            if (bz > 4 && // skin colours
                ba[bz - 1].toInt() in -69..-65 &&
                ba[bz - 2] == (-113).toByte() &&
                ba[bz - 3] == (-97).toByte() &&
                ba[bz - 4] == (-16).toByte()
            ) return null
            //Toast.makeText(c, ba.joinToString(","), Toast.LENGTH_LONG).show()
            return "" // do NOT invoke "setText()" in a filter!
        }
    }
}
