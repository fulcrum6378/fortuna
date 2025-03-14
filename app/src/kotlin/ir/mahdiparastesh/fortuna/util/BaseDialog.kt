package ir.mahdiparastesh.fortuna.util

import androidx.fragment.app.DialogFragment
import ir.mahdiparastesh.fortuna.Main

/** Base class for DialogFragment instances in this app. */
abstract class BaseDialogue : DialogFragment() {
    protected val c: Main by lazy { activity as Main }
}
