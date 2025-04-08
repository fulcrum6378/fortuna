package ir.mahdiparastesh.fortuna

import ir.mahdiparastesh.fortuna.util.NumberUtils.toKey
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.time.chrono.ChronoLocalDate
import java.time.chrono.Chronology
import java.time.temporal.ChronoField

interface FortunaContext {

    /** Main [Vita] object used across a Fortuna application */
    var vita: Vita

    /** Default Vita file */
    val stored: File

    /** A copy of [stored] as a backup */
    val backup: File


    /** Default calendar type used across a Fortuna application */
    val chronology: Chronology

    /** A calendar used for navigating at main pages */
    var date: ChronoLocalDate

    /** A [Luna] key for navigating at main pages */
    var luna: String

    /** A calendar that indicates today */
    var todayDate: ChronoLocalDate

    /** A [Luna] key that indicates this month */
    var todayLuna: String

    /** Other calendar types used for elaboration */
    val otherChronologies: List<Chronology>


    /** Prepares calendars and the [Vita]. */
    fun onCreate() {
        date = chronology.dateNow()
        luna = date.toKey()
        vita = Vita(this)
        updateToday()
    }

    /** Updates [todayDate] and [todayLuna]. */
    fun updateToday() {
        todayDate = chronology.dateNow()
        todayLuna = todayDate.toKey()
    }

    /** Creates a ChronoLocalDate out of a [Luna] key. */
    fun lunaToDate(luna: String): ChronoLocalDate {
        val spl = luna.split(".")
        return chronology.date(spl[0].toInt(), spl[1].toInt(), 1)
    }

    /**
     * Calculates the maximum date for getting a mean value for statistics, ignores the future.
     * @return null if the given month is the future
     */
    fun maximaForStats(date: ChronoLocalDate, key: String): Int? =
        if (key == todayLuna)  // this month
            todayDate[ChronoField.DAY_OF_MONTH]
        else if (date.isBefore(todayDate))  // past months
            date.lengthOfMonth()
        else  // future months
            null


    /** Copies data from [stored] into [backup]. */
    fun backupVita() {
        FileOutputStream(backup).use { fos ->
            fos.write(FileInputStream(stored).use { it.readBytes() })
        }
    }
}
