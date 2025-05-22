package ir.mahdiparastesh.fortuna

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.database.DataSetObserver
import android.graphics.Color
import android.provider.CalendarContract
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ir.mahdiparastesh.chrono.IranianChronology
import ir.mahdiparastesh.fortuna.databinding.DateComparisonBinding
import ir.mahdiparastesh.fortuna.databinding.ItemGridBinding
import ir.mahdiparastesh.fortuna.util.NumberUtils.displayScore
import ir.mahdiparastesh.fortuna.util.NumberUtils.groupDigits
import ir.mahdiparastesh.fortuna.util.NumberUtils.toKey
import ir.mahdiparastesh.fortuna.util.NumberUtils.write
import ir.mahdiparastesh.fortuna.util.NumberUtils.z
import ir.mahdiparastesh.fortuna.util.Numeral
import ir.mahdiparastesh.fortuna.util.Numerals
import ir.mahdiparastesh.fortuna.util.UiTools.color
import java.time.DateTimeException
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.chrono.ChronoLocalDate
import java.time.chrono.HijrahChronology
import java.time.chrono.IsoChronology
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import kotlin.math.absoluteValue

/** Main table of our calendar grid which lists days of a month */
@SuppressLint("SetTextI18n")
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
            // startDelay = 10L * i
            duration = 100L
            start()
        }
        cellColours[i] = targetColour

        // clicks
        b.root.setOnClickListener { c.changeVar(i) }
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
        numType = c.c.sp.getString(Fortuna.SP_NUMERAL_TYPE, Fortuna.SP_NUMERAL_TYPE_DEF)
            .let { if (it == Fortuna.SP_NUMERAL_TYPE_DEF) null else it }
        numeral = Numerals.build(numType)
        maximumStats = c.c.maximaForStats(c.c.date, c.c.luna)
    }

    /** Creates a calendar indicating this day. */
    fun dailyCalendar(i: Int): ChronoLocalDate =
        c.c.date.with(ChronoField.DAY_OF_MONTH, i + 1L)

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
                dat.background = c.varFieldBg
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
}
