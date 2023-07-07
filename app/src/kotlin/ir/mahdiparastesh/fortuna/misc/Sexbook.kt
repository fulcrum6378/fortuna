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
        c.contentResolver.query(
            Uri.parse("content://${Kit.SEXBOOK}/place"),
            null, null, null, null
        ).iterate { places[getLong(0)] = getString(1) }

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
                    cal[Calendar.YEAR].toShort(), (cal[Calendar.MONTH] + 1).toShort(),
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
            Uri.parse("content://${Kit.SEXBOOK}/crush"),
            arrayOf("key", "first_name", "middle_name", "last_name", "birth"),
            "birth IS NOT NULL", null, null
        ).iterate {
            val birth = getString(4).split(".")
            var y = birth[0].toInt()
            var m = birth[1].toInt()
            var d = birth[2].toInt()
            if (Kit.calType != GregorianCalendar::class.java) {
                val cal = Kit.calType.create()
                cal.timeInMillis = GregorianCalendar(y, m, d).timeInMillis
                y = cal[Calendar.YEAR]
                m = cal[Calendar.MONTH]
                d = cal[Calendar.DAY_OF_MONTH]
            }
            crushes.add(
                Crush(
                    getString(0), getString(1), getString(2), getString(3),
                    y.toShort(), m.toShort(), d.toShort()
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
     * @see Grid#appendSexReports
     */
    data class Report(
        val id: Long, val year: Short, val month: Short, val day: Short, // never compare bytes!
        val hour: Byte, val minute: Byte, val second: Byte,
        val key: String, val type: Byte, val desc: String, val accurate: Boolean, val place: String?
    )

    /**
     * Data class containing the information about a crush's birthday from Sexbook.
     *
     * @param fName First Name
     * @param mName Middle Name
     * @param lName Last Name
     * @param birthYear in this calendar (Short)
     * @param birthMonth in this calendar (Short)
     * @param birthDay in this calendar (Short)
     *
     * @see Grid#appendCrushBirthdays
     */
    data class Crush(
        val key: String, val fName: String?, val mName: String?, val lName: String?,
        val birthYear: Short, val birthMonth: Short, val birthDay: Short
    ) {
        private fun visName(): String =
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
