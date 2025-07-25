package ir.mahdiparastesh.fortuna.util

import androidx.core.net.toUri
import ir.mahdiparastesh.fortuna.Fortuna
import ir.mahdiparastesh.fortuna.util.AndroidUtils.iterate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.temporal.ChronoField

/**
 * Imports data from the Sexbook app in a background thread, if the app is installed.
 * The data includes orgasm times, crushes' birthdays and place names.
 *
 * @see <a href="https://github.com/fulcrum6378/sexbook">Sexbook repo in GitHub</a>
 * @see <a href="https://codeberg.org/fulcrum6378/sexbook">Sexbook repo in Codeberg</a>
 */
class Sexbook(private val c: Fortuna) {

    companion object {
        const val PACKAGE = "ir.mahdiparastesh.sexbook"
        const val MAIN_PAGE = "${Sexbook.PACKAGE}.page.Main"
    }

    @Throws(SecurityException::class)
    suspend fun load(listener: suspend (Data) -> Unit) {
        val places = hashMapOf<Long, String>()
        val reports = arrayListOf<Report>()
        val crushes = arrayListOf<Crush>()

        // get a list of all Places
        try {
            c.contentResolver.query(
                "content://$PACKAGE/place".toUri(),
                null, null, null, null
            ).iterate { places[getLong(0)] = getString(1) }
        } catch (_: SecurityException) {
            return
        }

        // now get a list of all sex Reports
        c.contentResolver.query(
            "content://$PACKAGE/report".toUri(),
            null, null, null, "time ASC"  // DESC
        ).iterate {
            val dt = c.dateTimeFromTimestamp(getLong(1))
            val date = dt.first
            val time = dt.second
            reports.add(
                Report(
                    getLong(0),
                    date[ChronoField.YEAR].toShort(),
                    date[ChronoField.MONTH_OF_YEAR].toShort(),
                    date[ChronoField.DAY_OF_MONTH].toShort(),
                    time[ChronoField.HOUR_OF_DAY].toByte(),
                    time[ChronoField.MINUTE_OF_HOUR].toByte(),
                    time[ChronoField.SECOND_OF_MINUTE].toByte(),
                    getString(2),
                    getShort(3).toByte(),
                    places[getLong(4)],
                    getString(5),
                    getInt(6) == 1,
                )
            )
        }

        // also load Crushes
        c.contentResolver.query(
            "content://$PACKAGE/crush".toUri(), arrayOf(
                "key", "first_name", "middle_name", "last_name", "status", "birthday", "first_met"
            ), "birthday IS NOT NULL OR first_met IS NOT NULL",
            null, null
        ).iterate {
            crushes.add(
                Crush(
                    getString(0),
                    getString(1),
                    getString(2),
                    getString(3),
                    getInt(4),
                    getString(5),
                    getString(6)
                )
            )
        }

        withContext(Dispatchers.Main) {
            listener(Data(reports, crushes))
        }
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
     * @param place the place where the sex happened (String?)
     * @param desc description (String)
     * @param accurate is this record accurate? (Boolean)
     *
     * @see Grid#appendSexReports
     */
    @Suppress("KDocUnresolvedReference")
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
        val place: String?,
        val desc: String?,
        val accurate: Boolean,
    )

    /**
     * Class containing the information about a crush from Sexbook.
     * @see Grid#appendCrushDates
     */
    @Suppress("KDocUnresolvedReference")
    inner class Crush(
        private val key: String,
        private val fName: String?, private val mName: String?, private val lName: String?,
        status: Int, birth: String?, firstMet: String?
    ) {

        val presence = status and 7
        val safePersonality = (status and 512) == 0
        val notifyBirthday = (status and 1024) != 0
        val active = (status and 32768) == 0

        var birthYear: Short? = null
        var birthMonth: Short? = null
        var birthDay: Short? = null
        var birthTime: String? = null
        var firstMetYear: Short? = null
        var firstMetMonth: Short? = null
        var firstMetDay: Short? = null
        var firstMetTime: String? = null

        init {
            if (birth != null) {
                val isImportant = notifyBirthday || active
                if ((presence != 5 || isImportant) &&  // has not disappeared or is important
                    (safePersonality || isImportant)
                ) parseDateTime(birth).also { dt ->
                    dt.first.also {
                        birthYear = it[0]
                        birthMonth = it[1]
                        birthDay = it[2]
                    }
                    birthTime = dt.second
                }
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
                    .joinToString(":") { NumberUtils.z(it.toIntOrNull() ?: 0) }
            }
            val spl = date.split("/")
            try {
                yb = spl[0].toInt()
                mb = spl[1].toInt()
                db = spl[2].toInt()
                if (c.chronology.calendarType != "iso8601") {
                    val cal = c.chronology.dateEpochDay(
                        LocalDate.of(yb, mb, db).toEpochDay()
                    )
                    yb = cal[ChronoField.YEAR]
                    mb = cal[ChronoField.MONTH_OF_YEAR]
                    db = cal[ChronoField.DAY_OF_MONTH]
                }
            } catch (_: Exception) {
            }
            return Pair(arrayOf(yb?.toShort(), mb?.toShort(), db?.toShort()), tb)
        }
    }

    class Data(val reports: List<Report>, crushes: List<Crush>) {

        val birthdayCrushes: List<Crush> = crushes.filter { cr ->
            cr.birthMonth != null && cr.birthDay != null
        }

        val firstMetCrushes: List<Crush> = crushes.filter { cr ->
            cr.firstMetYear != null && cr.firstMetMonth != null && cr.firstMetDay != null
        }
    }
}
