package ir.mahdiparastesh.fortuna.sect

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ir.mahdiparastesh.fortuna.R
import ir.mahdiparastesh.fortuna.databinding.ConvertDialogBinding
import ir.mahdiparastesh.fortuna.databinding.ConvertDialogItemBinding
import ir.mahdiparastesh.fortuna.util.BaseDialogue
import ir.mahdiparastesh.fortuna.util.NumberUtils.z
import java.time.chrono.Chronology
import java.time.temporal.ChronoField

/** A dialog box for converting dates across calendars */
class ConvertDialog : BaseDialogue() {

    companion object {
        const val TAG = "convert"
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val otherChronologies = c.c.otherChronologies()
        val chronologies = ArrayList<Chronology>(otherChronologies.size + 1)
        chronologies.add(c.c.chronology)
        chronologies.addAll(otherChronologies)

        val b = ConvertDialogBinding.inflate(c.layoutInflater)
        val today = c.c.todayDate.toEpochDay()
        for (ch in chronologies) {

            val bi = ConvertDialogItemBinding.inflate(c.layoutInflater)
            b.root.addView(bi.root)
            bi.chronologyName.text = c.c.chronologyName(ch) + ":"
            bi.dateEntry.apply {
                root.background = c.varFieldBg

                val date = ch.dateEpochDay(today)
                y.setText(z(date[ChronoField.YEAR]))
                m.setText(z(date[ChronoField.MONTH_OF_YEAR]))
                d.setText(z(date[ChronoField.DAY_OF_MONTH]))
            }
        }

        val dialogue = MaterialAlertDialogBuilder(c).apply {
            setIcon(R.drawable.today)
            setTitle(R.string.navConvert)
            setView(b.root)
            setNeutralButton(R.string.navConvert, null)
            setPositiveButton(R.string.go) { _, _ ->
            }
        }.show()

        dialogue.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
        }

        return dialogue
    }
}
