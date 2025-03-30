package ir.mahdiparastesh.fortuna.util

import android.icu.util.Calendar
import android.icu.util.GregorianCalendar
import androidx.core.net.toUri
import ir.mahdiparastesh.fortuna.Fortuna
import ir.mahdiparastesh.fortuna.Grid
import ir.mahdiparastesh.fortuna.Main
import ir.mahdiparastesh.fortuna.util.Kit.create
import ir.mahdiparastesh.fortuna.util.Kit.iterate
import ir.mahdiparastesh.fortuna.util.NumberUtils.z

/**
 * Imports data from the Sexbook app in a separate thread, if the app is installed.
 * The data includes orgasm times and crushes' birthdays.
 *
 * @see <a href="https://github.com/fulcrum6378/sexbook">The Sexbook repository</a>
 */
class Sexbook(private val c: Fortuna) : Thread() {

    override fun run() {
        val places = hashMapOf<Long, String>()
        val reports = arrayListOf<Report>()
        val crushes = arrayListOf<Crush>()

        // get a list of all Places
        try {
            c.contentResolver.query(
                "content://${Kit.SEXBOOK}/place".toUri(),
                null, null, null, null
            ).iterate { places[getLong(0)] = getString(1) }
        } catch (_: SecurityException) {
            interrupt()
            return
        }

        // now get a list of all sex Reports
        c.contentResolver.query(
            "content://${Kit.SEXBOOK}/report".toUri(),
            null, null, null, "time ASC" // DESC
        ).iterate {
            val cal = c.calType.create()
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

        // also load Crushes
        c.contentResolver.query(
            "content://${Kit.SEXBOOK}/crush".toUri(), arrayOf(
                "key", "first_name", "middle_name", "last_name", "status", "birth", "first_met"
            ), "birth IS NOT NULL OR first_met IS NOT NULL", null, null
        ).iterate {
            crushes.add(
                Crush(
                    getString(0),
                    getString(1), getString(2), getString(3),
                    getInt(4), getString(5), getString(6)
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
     * Class containing the information about a crush from Sexbook.
     * @see Grid#appendCrushDates
     */
    inner class Crush(
        private val key: String,
        private val fName: String?, private val mName: String?, private val lName: String?,
        status: Int, birth: String?, firstMet: String?
    ) {
        val active = (status and 128) == 0
        var birthYear: Short? = null
        var birthMonth: Short? = null
        var birthDay: Short? = null
        var birthTime: String? = null
        var firstMetYear: Short? = null
        var firstMetMonth: Short? = null
        var firstMetDay: Short? = null
        var firstMetTime: String? = null

        init {
            if (birth != null && (((status and 128) == 0) || ((status and 16) == 16)) // (status and 32) == 0
            ) parseDateTime(birth).also { dt ->
                dt.first.also {
                    birthYear = it[0]
                    birthMonth = it[1]
                    birthDay = it[2]
                }
                birthTime = dt.second
            }
            if (firstMet != null) parseDateTime(firstMet).also { dt ->
                dt.first.also {
                    firstMetYear = it[0]
                    firstMetMonth = it[1]
                    firstMetDay = it[2]
                }
                firstMetTime = dt.second
            }
        }

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

        private fun parseDateTime(dt: String): Pair<Array<Short?>, String?> {
            var yb: Int? = null
            var mb: Int? = null
            var db: Int? = null
            var tb: String? = null
            var date = dt
            if (' ' in dt) dt.split(" ").also { dta ->
                date = dta[0]
                tb = dta[1].split(":")
                    .joinToString(":") { z(it.toIntOrNull() ?: 0) }
            }
            val spl = date.split("/")
            try {
                yb = spl[0].toInt()
                mb = spl[1].toInt() - 1
                db = spl[2].toInt()
                if (c.calType != GregorianCalendar::class.java) {
                    val cal = c.calType.create()
                    cal.timeInMillis = GregorianCalendar(yb, mb, db).timeInMillis
                    yb = cal[Calendar.YEAR]
                    mb = cal[Calendar.MONTH]
                    db = cal[Calendar.DAY_OF_MONTH]
                }
            } catch (_: Exception) {
            }
            return Pair(arrayOf(yb?.toShort(), mb?.toShort(), db?.toShort()), tb)
        }
    }

    data class Data(val reports: List<Report>, val crushes: List<Crush>)
}
