package ir.mahdiparastesh.fortuna.sect

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.provider.CalendarContract
import android.text.Editable
import android.text.TextWatcher
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ir.mahdiparastesh.chrono.IranianChronology
import ir.mahdiparastesh.fortuna.R
import ir.mahdiparastesh.fortuna.databinding.DateComparisonBinding
import ir.mahdiparastesh.fortuna.util.BaseDialogue
import ir.mahdiparastesh.fortuna.util.NumberUtils.groupDigits
import ir.mahdiparastesh.fortuna.util.NumberUtils.toKey
import ir.mahdiparastesh.fortuna.util.NumberUtils.z
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

/**
 * Elaborates about a specific date in all calendars and measures its distance from any other
 * date. It can also open Android's default calendar bringing the same date.
 */
class ChronometerDialog : BaseDialogue() {

    private var i: Int = 0
    private val date: ChronoLocalDate by lazy {
        c.c.date.with(ChronoField.DAY_OF_MONTH, i + 1L)
    }

    companion object {
        const val TAG = "chronometer"
        const val ARG_DAY = "day"

        /** @param day starting from 0 */
        fun newInstance(day: Int): ChronometerDialog {
            return ChronometerDialog().apply {
                arguments = Bundle().apply {
                    putInt(ARG_DAY, day)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireArguments().also { args ->
            i = args.getInt(ARG_DAY)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(c).apply {
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
                        } catch (_: RuntimeException) {  // DateTimeException or NumberFormatException
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
                                (date.atTime(
                                    LocalTime.of(0, 0, 0)
                                ).toEpochSecond(OffsetDateTime.now().offset) * 1000L)
                                    .toString()
                            )
                            .build()
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
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
