package ir.mahdiparastesh.fortuna.sect

import android.app.Dialog
import android.os.Bundle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ir.mahdiparastesh.fortuna.R
import ir.mahdiparastesh.fortuna.util.BaseDialogue

/** A dialogue containing a guide text for this application */
class HelpDialog : BaseDialogue() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(c).apply {
            setIcon(R.drawable.verbum)
            setTitle(R.string.navHelp)
            setMessage(R.string.help)
        }.create()
    }
}
