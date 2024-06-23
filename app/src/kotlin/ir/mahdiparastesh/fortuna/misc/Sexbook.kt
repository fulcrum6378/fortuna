package ir.mahdiparastesh.fortuna.misc

import android.annotation.SuppressLint
import android.content.Context
import android.icu.util.Calendar
import android.icu.util.GregorianCalendar
import android.net.Uri
import ir.mahdiparastesh.fortuna.Grid
import ir.mahdiparastesh.fortuna.Kit
import ir.mahdiparastesh.fortuna.Kit.create
import ir.mahdiparastesh.fortuna.Kit.iterate
import ir.mahdiparastesh.fortuna.Main

/**
 * Imports data from the Sexbook app in a separate thread, if the app is installed.
 * The data includes orgasm times and crushes' birthdays.
 *
 * @see <a href="https://play.google.com/store/apps/details?id=ir.mahdiparastesh.sexbook">Sexbook in Google Play</a>
 * @see <a href="https://github.com/fulcrum6378/sexbook">Sexbook repository</a>
 */
class Sexbook(private val c: Context) : Thread() {
    @SuppressLint("Recycle")
    override fun run() {
        val places = hashMapOf<Long, String>()
        val reports = arrayListOf<Report>()
        val crushes = arrayListOf<Crush>()

        // Get list of Places
        try {
            c.contentResolver.query(
                Uri.parse("content://${Kit.SEXBOOK}/place"),
                null, null, null, null
            ).iterate { places[getLong(0)] = getString(1) }
        } catch (_: SecurityException) {
            interrupt()
            return
        }

        // Now get the sex reports
        c.contentResolver.query(
            Uri.parse("content://${Kit.SEXBOOK}/report"),
            null, null, null, "time ASC" // DESC
        ).iterate {
            val cal = Kit.calType.create()
            cal.timeInMillis = getLong(0)
            reports.add(
                Report(
                    getLong(6),
                    cal[Calendar.YEAR].toShort(), cal[Calendar.MONTH].toShort(),
                    cal[Calendar.DAY_OF_MONTH].toShort(), cal[Calendar.HOUR_OF_DAY].toByte(),
                    cal[Calendar.MINUTE].toByte(), cal[Calendar.SECOND].toByte(),
                    getString(1), getShort(2).toByte(),
                    getString(3), getInt(4) == 1,
                    places[getLong(5)]
                )
            )
        }

        // Also load the crushes
        c.contentResolver.query(
            Uri.parse("content://${Kit.SEXBOOK}/crush"), arrayOf(
                "key", "first_name", "middle_name", "last_name", "status", "birth", "first_met"
            ), "(birth IS NOT NULL OR first_met IS NOT NULL)", null, null
        ).iterate {
            val status = getInt(4)
            // Birthday
            var yb: Int? = null
            var mb: Int? = null
            var db: Int? = null
            var tb: String? = null
            getString(5)?.also { birth ->
                if (((status and 128) != 0) && ((status and 16) != 16)) return@also
                var date = birth
                if (' ' in birth) birth.split(" ").also {
                    date = it[0]
                    tb = it[1]
                }
                val spl = date.replace(".", "/").split("/")
                try {
                    yb = spl[0].toInt()
                    mb = spl[1].toInt() - 1
                    db = spl[2].toInt()
                } catch (_: Exception) {
                    return@iterate
                }
                if (Kit.calType != GregorianCalendar::class.java) {
                    val cal = Kit.calType.create()
                    cal.timeInMillis = GregorianCalendar(yb!!, mb!!, db!!).timeInMillis
                    yb = cal[Calendar.YEAR]
                    mb = cal[Calendar.MONTH]
                    db = cal[Calendar.DAY_OF_MONTH]
                }
            }

            // First Met Date
            var yf: Int? = null
            var mf: Int? = null
            var df: Int? = null
            var tf: String? = null
            getString(6)?.also { first ->
                var date = first
                if (' ' in first) first.split(" ").also {
                    date = it[0]
                    tf = it[1]
                }
                val spl = date.replace(".", "/").split("/")
                try {
                    yf = spl[0].toInt()
                    mf = spl[1].toInt() - 1
                    df = spl[2].toInt()
                } catch (_: Exception) {
                    return@iterate
                }
                if (Kit.calType != GregorianCalendar::class.java) {
                    val cal = Kit.calType.create()
                    cal.timeInMillis = GregorianCalendar(yf!!, mf!!, df!!).timeInMillis
                    yf = cal[Calendar.YEAR]
                    mf = cal[Calendar.MONTH]
                    df = cal[Calendar.DAY_OF_MONTH]
                }
            }

            crushes.add(
                Crush(
                    getString(0),
                    getString(1), getString(2), getString(3),
                    yb?.toShort(), mb?.toShort(), db?.toShort(), tb,
                    (status and 128) == 0,
                    yf?.toShort(), mf?.toShort(), df?.toShort(), tf
                )
            )
        }

        Main.handler?.obtainMessage(
            Main.HANDLE_SEXBOOK_LOADED, Data(reports.toList(), crushes.toList())
        )?.sendToTarget()
    }

    /**
     * Data class containing the information about a sex record from Sexbook.
     *
     * @param id the unique id (Long)
     * @param year in this calendar (Short)
     * @param month ... (0-based)
     * @param day ...
     * @param hour (Byte)
     * @param minute (Byte)
     * @param second (Byte)
     * @param key the raw input from the user indicating their crush's name (String)
     * @param type wet dream, masturbation, oral, anal or vaginal sex (Byte)
     * @param desc description (String)
     * @param accurate is this record accurate? (Boolean)
     * @param place the place where the sex happened (String?)
     *
     * @see Grid#appendSexReports
     */
    data class Report(
        val id: Long,
        val year: Short,
        val month: Short,
        val day: Short, // never compare bytes!
        val hour: Byte,
        val minute: Byte,
        val second: Byte,
        val key: String?,
        val type: Byte,
        val desc: String?,
        val accurate: Boolean,
        val place: String?
    )

    /**
     * Data class containing the information about a crush's birthday from Sexbook.
     *
     * @param fName first name
     * @param mName middle name
     * @param lName last name
     * @param birthYear in this calendar (Short?)
     * @param birthMonth ... (0-based)
     * @param birthDay ...
     * @param birthTime
     * @param active whether or not the crush is specified as active
     * @param firstMetYear first encounter with this person (Short?)
     * @param firstMetMonth ... (0-based)
     * @param firstMetYear ...
     * @param firstMetTime
     *
     * @see Grid#appendCrushDates
     */
    data class Crush(
        val key: String, val fName: String?, val mName: String?, val lName: String?,
        val birthYear: Short?, val birthMonth: Short?, val birthDay: Short?, val birthTime: String?,
        val active: Boolean, val firstMetYear: Short?, val firstMetMonth: Short?,
        val firstMetDay: Short?, val firstMetTime: String?
    ) {
        fun visName(): String =
            if (fName.isNullOrEmpty() || lName.isNullOrEmpty()) when {
                !fName.isNullOrEmpty() -> fName
                !lName.isNullOrEmpty() -> lName
                !mName.isNullOrEmpty() -> mName
                else -> key
            } else "$fName $lName"

        fun theirs() = run {
            val visN = visName()
            if (!visN.endsWith('s')) "${visN}'s" else "${visN}'"
        }
    }

    data class Data(val reports: List<Report>, val crushes: List<Crush>)
}
