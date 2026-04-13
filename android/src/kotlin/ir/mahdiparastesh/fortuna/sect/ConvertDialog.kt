package ir.mahdiparastesh.fortuna.sect

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import ir.mahdiparastesh.fortuna.R
import ir.mahdiparastesh.fortuna.databinding.ConvertDialogBinding
import ir.mahdiparastesh.fortuna.databinding.ConvertDialogItemBinding
import ir.mahdiparastesh.fortuna.databinding.DateEntryBinding
import ir.mahdiparastesh.fortuna.util.BaseDialogue
import ir.mahdiparastesh.fortuna.util.NumberUtils.z
import ir.mahdiparastesh.fortuna.util.UiTools.color
import java.time.chrono.ChronoLocalDate
import java.time.chrono.Chronology
import java.time.temporal.ChronoField

/** A dialog box for converting dates across calendars */
class ConvertDialog : BaseDialogue() {
    lateinit var b: ConvertDialogBinding
    lateinit var chronologies: ArrayList<Chronology>
    lateinit var selectedFieldBg: MaterialShapeDrawable

    companion object {
        const val TAG = "convert"
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        c.c.otherChronologies().also { otherChronologies ->
            chronologies = ArrayList(otherChronologies.size + 1)
            chronologies.add(c.c.chronology)
            chronologies.addAll(otherChronologies)
        }

        selectedFieldBg = MaterialShapeDrawable(
            ShapeAppearanceModel.Builder().setAllCorners(
                CornerFamily.CUT, c.resources.getDimension(R.dimen.smallCornerSize)
            ).build()
        ).apply {
            fillColor = c.resources.getColorStateList(R.color.varField, null)
            strokeWidth = 1f * c.c.resources.displayMetrics.density
            strokeColor = c.resources.getColorStateList(R.color.selectedField, null)
        }

        b = ConvertDialogBinding.inflate(c.layoutInflater)
        val today = c.c.todayDate.toEpochDay()
        for (ch in chronologies.indices) {
            val chronology = chronologies[ch]

            val bi = ConvertDialogItemBinding.inflate(c.layoutInflater)
            b.root.addView(bi.root)
            bi.chronologyName.text = c.c.chronologyName(chronology) + ":"
            bi.applyColours(ch)
            bi.dateEntry.apply {
                applyDate(chronology, today)

                val focusListener = View.OnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) selectChronology(ch)
                }
                y.onFocusChangeListener = focusListener
                m.onFocusChangeListener = focusListener
                d.onFocusChangeListener = focusListener
            }
            bi.root.setOnClickListener {
                selectChronology(ch)
            }
        }

        val dialogue = MaterialAlertDialogBuilder(c).apply {
            setIcon(R.drawable.today)
            setTitle(R.string.navConvert)
            setView(b.root)
            setNeutralButton(R.string.navConvert, null)
            setPositiveButton(R.string.go) { _, _ ->
                var goTo: ChronoLocalDate = convertDate(true) ?: return@setPositiveButton
                if (goTo.chronology.javaClass != c.c.chronology.javaClass)
                    goTo = c.c.chronology.dateEpochDay((goTo).toEpochDay())
                c.c.date = goTo
                c.onDateChanged()
                c.closeDrawer()
                c.variabilis(goTo[ChronoField.DAY_OF_MONTH] - 1)
            }
        }.show()

        dialogue.getButton(AlertDialog.BUTTON_NEUTRAL)
            .setOnClickListener { convertDate(false) }

        return dialogue
    }

    fun ConvertDialogItemBinding.applyColours(iChronology: Int) {
        chronologyName.setTextColor(
            if (iChronology == c.m.selectedChronologyForConversion)
                c.resources.getColor(R.color.selectedField, null)
            else c.color(android.R.attr.textColor)
        )
        dateEntry.root.background =
            if (iChronology == c.m.selectedChronologyForConversion) selectedFieldBg
            else c.varFieldBg
    }

    fun DateEntryBinding.applyDate(chronology: Chronology, epochDay: Long) {
        val date = chronology.dateEpochDay(epochDay)
        y.setText(z(date[ChronoField.YEAR]))
        m.setText(z(date[ChronoField.MONTH_OF_YEAR]))
        d.setText(z(date[ChronoField.DAY_OF_MONTH]))
    }

    fun selectChronology(iChronology: Int) {
        c.m.selectedChronologyForConversion = iChronology
        for (ch in chronologies.indices)
            ConvertDialogItemBinding.bind(b.root.getChildAt(ch))
                .applyColours(ch)
    }

    fun convertDate(goTo: Boolean): ChronoLocalDate? {
        val bi = ConvertDialogItemBinding.bind(
            b.root.getChildAt(c.m.selectedChronologyForConversion)
        )
        val chronology = chronologies[c.m.selectedChronologyForConversion]
        val date: ChronoLocalDate
        try {
            date = chronology.date(
                bi.dateEntry.y.text.toString().toInt(),
                bi.dateEntry.m.text.toString().toInt(),
                bi.dateEntry.d.text.toString().toInt()
            )
        } catch (_: NumberFormatException) {
            return null
        }
        if (goTo) return date

        val epochDay = date.toEpochDay()
        for (ch in chronologies.indices) {
            val bi = ConvertDialogItemBinding.bind(b.root.getChildAt(ch))
            val date = chronologies[ch].dateEpochDay(epochDay)

            bi.dateEntry.y.setText(date[ChronoField.YEAR].toString())
            bi.dateEntry.m.setText(date[ChronoField.MONTH_OF_YEAR].toString())
            bi.dateEntry.d.setText(date[ChronoField.DAY_OF_MONTH].toString())
        }
        return null
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        c.m.selectedChronologyForConversion = 0
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        c.m.selectedChronologyForConversion = 0
    }
}
