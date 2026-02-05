package ir.mahdiparastesh.fortuna.util

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Build
import ir.mahdiparastesh.fortuna.Main
import ir.mahdiparastesh.fortuna.R
import ir.mahdiparastesh.fortuna.util.NumberUtils.toKey
import java.time.chrono.ChronoLocalDate
import java.time.temporal.ChronoField

/** Static shared Android utilities */
object AndroidUtils {

    /** Opens Fortuna navigating to the specified date. */
    fun openInDate(c: Context, cal: ChronoLocalDate, req: Int): PendingIntent =
        PendingIntent.getActivity(
            c, req, Intent(c, Main::class.java)
                .putExtra(MainHandler.EXTRA_LUNA, cal.toKey())
                .putExtra(MainHandler.EXTRA_DIES, cal[ChronoField.DAY_OF_MONTH]),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                PendingIntent.FLAG_MUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
        )  // A unique request code protects the PendingIntent from being recycled!

    /** Explains bytes for humans. */
    fun showBytes(c: Context, length: Long): String =
        NumberUtils.showBytes(c.resources.getStringArray(R.array.bytes), length)

    fun Cursor?.iterate(action: Cursor.() -> Unit) {
        this?.use { cur -> while (cur.moveToNext()) cur.action() }
    }
}
