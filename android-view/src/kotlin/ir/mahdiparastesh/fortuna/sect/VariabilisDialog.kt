package ir.mahdiparastesh.fortuna.sect

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.view.MotionEvent
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ir.mahdiparastesh.fortuna.Luna
import ir.mahdiparastesh.fortuna.R
import ir.mahdiparastesh.fortuna.databinding.VariabilisBinding
import ir.mahdiparastesh.fortuna.util.BaseDialogue
import ir.mahdiparastesh.fortuna.util.LimitedToastAlert
import ir.mahdiparastesh.fortuna.util.NumberUtils.displayScore
import ir.mahdiparastesh.fortuna.util.NumberUtils.toScore
import ir.mahdiparastesh.fortuna.util.NumberUtils.toVariabilis
import ir.mahdiparastesh.fortuna.util.NumberUtils.z
import ir.mahdiparastesh.fortuna.util.Sexbook
import ir.mahdiparastesh.fortuna.util.UiTools.color
import java.time.chrono.ChronoLocalDate
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.util.Locale

/** A dialog box which lets the user change the scores of days, emojis and verbum descriptions */
class VariabilisDialog : BaseDialogue() {

    private lateinit var dialogue: AlertDialog
    private var i: Int = 0
    private val date: ChronoLocalDate by lazy {
        c.c.date.with(ChronoField.DAY_OF_MONTH, if (i != -1) i + 1L else 1)
    }
    private val luna: Luna by lazy { c.c.vita[c.c.luna] }
    private val b: VariabilisBinding by lazy { VariabilisBinding.inflate(c.layoutInflater) }

    companion object {
        const val TAG = "variabilis"
        const val ARG_DAY = "day"

        /** @param day starting from 0 */
        fun newInstance(day: Int): VariabilisDialog =
            VariabilisDialog().apply {
                arguments = Bundle().apply {
                    putInt(ARG_DAY, day)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireArguments().also { args ->
            i = args.getInt(ARG_DAY)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        arrayOf(b.highlight, b.verbum).forEach { it.background = c.varFieldBg }

        // score picker
        b.picker.apply {
            maxValue = 12
            minValue = 0
            value = c.m.variabilisScore
                ?: (if (i != -1) luna[i]?.toVariabilis() else null)
                        ?: luna.default?.toVariabilis() ?: 6
            wrapSelectorWheel = false
            setFormatter { it.toScore().displayScore(false) }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                textColor = c.color(android.R.attr.textColor)
                textSize = c.resources.displayMetrics.density * 25f
            }
            (this@apply[0] as EditText).also { it.filters = arrayOf() }
            if (c.m.variabilisScore != null) isCancelable = false
            setOnValueChangedListener { _, _, newVal ->
                c.m.variabilisScore = newVal
                dialogue.setCancelable(false)
            }
        }

        // emojis
        b.emoji.apply {
            setText(c.m.variabilisEmoji ?: (if (i != -1) luna.emojis[i] else luna.emoji))
            if (text.isEmpty()) luna.emoji?.also { hint = it }
            filters = arrayOf(EmojiFilter(this@apply))
            if (c.m.variabilisEmoji != null) isCancelable = false
            addTextChangedListener {
                c.m.variabilisEmoji = it.toString()
                dialogue.setCancelable(false)
            }
        }

        // descriptions
        b.verbum.apply {
            setText(c.m.variabilisVerbum ?: (if (i != -1) luna.verba[i] else luna.verbum))
            if (c.m.variabilisVerbum != null) isCancelable = false
            addTextChangedListener {
                c.m.variabilisVerbum = it.toString()
                dialogue.setCancelable(false)
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
        c.m.sexbook.observe(this) {
            b.sexbook.appendCrushDates(i.toShort(), date[ChronoField.YEAR].toShort())
            if (i != -1) b.sexbook.appendSexReports(i)
        }

        dialogue = MaterialAlertDialogBuilder(c).apply {
            setTitle(
                if (i != -1) "${c.c.luna}.${z(i + 1)}"
                else c.getString(R.string.defValue)
            )
            setView(b.root)
            setNegativeButton(R.string.cancel, null)
            setPositiveButton(R.string.save) { _, _ ->
                c.saveDies(
                    luna, i, b.picker.value.toScore(),
                    b.emoji.text.toString(), b.verbum.text.toString()
                )
                c.shake()
            }
            setNeutralButton(R.string.clear) { _, _ -> }
        }.show()

        if (i > -1) {
            val isPast = date.isBefore(c.c.todayDate) &&
                    c.c.todayDate.until(date, ChronoUnit.DAYS) < -7
            val isFuture = date.isAfter(c.c.todayDate) &&
                    c.c.todayDate.until(date, ChronoUnit.DAYS) >= 1
            if ((isPast && luna.diebus[i] != null) || isFuture) {
                b.picker.isEnabled = false
                b.picker.alpha = 0.4f
                b.lock.isVisible = true
                if (isFuture)
                    b.lock.setOnClickListener(LimitedToastAlert(c, R.string.scoreFuture))
                else {  // is the past
                    b.lock.setOnClickListener(LimitedToastAlert(c, R.string.holdLonger))
                    b.lock.setOnLongClickListener {
                        b.lock.isVisible = false
                        b.lock.setOnClickListener(null)
                        b.lock.setOnLongClickListener(null)
                        b.picker.alpha = 1f
                        b.picker.isEnabled = true
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
                dialogue.dismiss(); true
            }
        }

        return dialogue
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        b.verbum.clearFocus()
        c.m.variabilisScore = null
        c.m.variabilisEmoji = null
        c.m.variabilisVerbum = null
    }

    /**
     * Elaborates about birthdays and other special dates related to crushes imported from the
     * Sexbook app and puts them inside the specified TextView, and makes the TextView visible.
     * Unfortunately estimated dates cannot be imported because of the difference in the calendars!
     */
    @SuppressLint("SetTextI18n")
    private fun TextView.appendCrushDates(day: Short, year: Short) {
        val lunar = day == (-1).toShort()
        val yr = c.c.date[ChronoField.YEAR].toShort()
        val mo = c.c.date[ChronoField.MONTH_OF_YEAR].toShort()
        val da = (day + 1).toShort()

        val birthdays = c.m.sexbook.value?.birthdayCrushes
            ?.filter { x ->
                (x.birthYear == null || x.birthYear!! <= yr) && x.birthMonth == mo &&
                        (if (!lunar) x.birthDay == da else true)
            }
            ?.sortedBy { it.birthTime }
            ?.sortedBy { it.birthDay }
        val firstDates = c.m.sexbook.value?.firstMetCrushes
            ?.filter { x ->
                x.firstMetYear == yr && x.firstMetMonth == mo &&
                        (if (!lunar) x.firstMetDay == da else true)
            }
            ?.sortedBy { it.firstMetTime }
            ?.sortedBy { it.firstMetDay }
        if (birthdays.isNullOrEmpty() && firstDates.isNullOrEmpty()) return

        val sb = StringBuilder()
        if (!birthdays.isNullOrEmpty()) for (bd in birthdays) {
            val age = bd.birthYear?.let { year - it }
            if (age == null || age > 0) {
                if (bd.active && !lunar) sb.append("Happy ")
                sb.append(bd.theirs())
                if (age != null) {
                    val sAge = age.toString()
                    if (!lunar) sb.append(" ${ordinalSuffixes(sAge)}")
                }
                sb.append(" birthday")
                if (lunar && bd.birthDay != null)
                    sb.append(" on ${ordinalSuffixes(bd.birthDay.toString())}")
                sb.append(if (!lunar) "!" else ".")
                sb.append("\n")
            } else {
                sb.append(bd.visName().uppercase(Locale.getDefault()))
                sb.append(" was born")
                if (!lunar && bd.birthTime != null) sb.append(" at ${bd.birthTime}")
                if (lunar && bd.birthDay != null)
                    sb.append(" on ${ordinalSuffixes(bd.birthDay.toString())}")
                sb.append(if (!lunar) "!" else ".")
                sb.append("\n")
            }
        }
        if (!firstDates.isNullOrEmpty()) for (fm in firstDates) {
            sb.append("Met ${fm.visName()} for the first time")
            if (!lunar && fm.firstMetTime != null) sb.append(" at ${fm.firstMetTime}")
            if (lunar && fm.firstMetDay != null)
                sb.append(" on ${ordinalSuffixes(fm.firstMetDay.toString())}")
            sb.append(if (!lunar) "!" else ".")
            sb.append("\n")
        }

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
    @SuppressLint("SetTextI18n")
    private fun TextView.appendSexReports(i: Int) {
        if (i == -1) return

        val yr = c.c.date[ChronoField.YEAR].toShort()
        val mo = c.c.date[ChronoField.MONTH_OF_YEAR].toShort()
        val da = (i + 1).toShort()
        val sex = c.m.sexbook.value?.reports?.filter { x ->
            x.year == yr && x.month == mo && x.day == da
        }
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
                        .setComponent(ComponentName(Sexbook.PACKAGE, Sexbook.MAIN_PAGE))
                        .setData(sex.first().id.toString().toUri())
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                ); true
            } catch (_: ActivityNotFoundException) {
                false
            }
        }
    }

    private fun ordinalSuffixes(number: String) =
        number + (if (number.first() != '1' || number.length != 2)
            when (number.last()) {
                '1' -> "st"
                '2' -> "nd"
                '3' -> "rd"
                else -> "th"
            }
        else "th")


    /** Filters non-emoji characters allowing only emojis for an [EditText]. */
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
            return ""  // do NOT invoke "setText()" in a filter!
        }
    }
}
