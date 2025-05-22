package ir.mahdiparastesh.fortuna.sect

import android.app.Dialog
import android.os.Bundle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ir.mahdiparastesh.fortuna.R
import ir.mahdiparastesh.fortuna.util.BaseDialogue

/** A dialog box containing instructions for this application */
class HelpDialog : BaseDialogue() {

    companion object {
        const val TAG = "help"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(c).apply {
            setIcon(R.drawable.verbum)
            setTitle(R.string.navHelp)
            setMessage(R.string.help)
        }.create()
    }
}
