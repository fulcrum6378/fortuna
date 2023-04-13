package ir.mahdiparastesh.fortuna.misc

import android.content.Context
import android.icu.util.Calendar
import android.net.Uri
import ir.mahdiparastesh.fortuna.Grid
import ir.mahdiparastesh.fortuna.Kit
import ir.mahdiparastesh.fortuna.Main

/**
 * Imports data from the Sexbook application in a separate thread.
 *
 * @see <a href="https://github.com/fulcrum1378/sexbook">Sexbook repository</a>
 * @see <a href="https://mahdiparastesh.ir/?fk=1">Mahdi Parastesh's personal website</a>
 */
class Sexbook(private val c: Context) : Thread() {
    override fun run() {

        // Get list of Places
        val places = hashMapOf<Long, String>()
        c.contentResolver.query(
            Uri.parse("content://${Kit.SEXBOOK}/place"),
            null, null, null, null
        )?.use { cur ->
            while (cur.moveToNext())
                places[cur.getLong(0)] = cur.getString(1)
        }

        // Now get the sex reports
        val cur = c.contentResolver.query(
            Uri.parse("content://${Kit.SEXBOOK}/report"),
            null, null, null, "time ASC" // DESC
        ) ?: return
        val sexbook = arrayListOf<Report>()
        while (cur.moveToNext()) {
            val cal = Kit.calType.newInstance()
            cal.timeInMillis = cur.getLong(1)
            sexbook.add(
                Report(
                    cur.getLong(0),
                    cal[Calendar.YEAR].toShort(), (cal[Calendar.MONTH] + 1).toShort(),
                    cal[Calendar.DAY_OF_MONTH].toShort(), cal[Calendar.HOUR_OF_DAY].toByte(),
                    cal[Calendar.MINUTE].toByte(), cal[Calendar.SECOND].toByte(),
                    cur.getString(2), cur.getShort(3).toByte(),
                    cur.getString(4), cur.getInt(5) == 1,
                    places[cur.getLong(6)]
                )
            )
        }
        cur.close()

        // Also load the crushes TODO

        Main.handler?.obtainMessage(Main.HANDLE_SEXBOOK_LOADED, sexbook.toList())?.sendToTarget()
    }

    /**
     * Data class containing the information about a sex record from Sexbook.
     *
     * @param id the unique id (Long)
     * @param year in this calendar (Short)
     * @param month in this calendar (Short)
     * @param day in this calendar (Short)
     * @param hour (Byte)
     * @param minute (Byte)
     * @param second (Byte)
     * @param key the raw input from the user indicating their crush's name (String)
     * @param type wet dream, masturbation, oral, anal or vaginal sex (Byte)
     * @param desc description (String)
     * @param accurate is this record accurate? (Boolean)
     * @param place the place where the sex happened (String?)
     *
     * @see Grid#showSexbook
     */
    data class Report(
        val id: Long, val year: Short, val month: Short, val day: Short, // never compare bytes!
        val hour: Byte, val minute: Byte, val second: Byte,
        val key: String, val type: Byte, val desc: String, val accurate: Boolean, val place: String?
    )
}
