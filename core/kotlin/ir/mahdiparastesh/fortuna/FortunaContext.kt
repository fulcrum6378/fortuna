package ir.mahdiparastesh.fortuna

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

interface FortunaContext<Calendar> {

    /** Main [Vita] used across the application */
    var vita: Vita?

    /** A [Luna] key for navigating at main pages */
    var luna: String?

    /** A calendar used for navigating at main pages */
    var calendar: Calendar

    /** A calendar that indicates today. */
    var todayCalendar: Calendar

    /** A [Luna] key that indicates today */
    var todayLuna: String

    /** Default Vita file */
    val stored: File

    /** A copy of [stored] as a backup */
    val backup: File


    /** @return number of days in a local month of any calendar system */
    fun getMonthLength(year: Int, month: Int): Int

    /** Copies data from [stored] into [backup]. */
    fun backupVita() {
        FileOutputStream(backup).use { fos ->
            fos.write(FileInputStream(stored).use { it.readBytes() })
        }
    }
}
