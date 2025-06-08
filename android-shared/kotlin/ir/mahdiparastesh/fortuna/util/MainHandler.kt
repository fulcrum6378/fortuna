package ir.mahdiparastesh.fortuna.util

import android.os.Handler

object MainHandler {
    const val EXTRA_LUNA = "luna"
    const val EXTRA_DIES = "dies"
    const val HANDLE_NEW_DAY = 0
    var handler: Handler? = null
}
