package ir.mahdiparastesh.fortuna

import ir.mahdiparastesh.fortuna.util.NumberUtils.toKey
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.time.chrono.ChronoLocalDate
import java.time.chrono.Chronology
import java.time.temporal.ChronoField

interface FortunaContext {

    /** Main [Vita] used across the application */
    var vita: Vita

    /** Default Vita file */
    val stored: File

    /** A copy of [stored] as a backup */
    val backup: File


    /**
     * Default calendar Type
     */
    val chronology: Chronology

    /** A calendar used for navigating at main pages */
    var date: ChronoLocalDate

    /** A [Luna] key for navigating at main pages */
    var luna: String

    /** A calendar that indicates today. */
    var todayDate: ChronoLocalDate

    /** A [Luna] key that indicates today */
    var todayLuna: String

    /** Other supported calendar types */
    val otherChronologies: List<Chronology>


    fun lunaToDate(luna: String): ChronoLocalDate {
        val spl = luna.split(".")
        return chronology.date(spl[0].toInt(), spl[1].toInt(), 1)
    }

    /** Updates [todayDate] and [todayLuna] */
    fun updateToday() {
        todayDate = chronology.dateNow()
        todayLuna = todayDate.toKey()
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
