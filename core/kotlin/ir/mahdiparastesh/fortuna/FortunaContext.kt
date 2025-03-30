package ir.mahdiparastesh.fortuna

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.reflect.Modifier
import java.time.ZoneOffset
import java.time.chrono.ChronoLocalDate
import java.time.chrono.ChronoLocalDateTime

interface FortunaContext {

    /** Main [Vita] used across the application */
    var vita: Vita?

    /** A [Luna] key for navigating at main pages */
    var luna: String?

    /** Default Vita file */
    val stored: File

    /** A copy of [stored] as a backup */
    val backup: File


    /**
     * Default Calendar Type
     *
     * @see java.time.chrono.ChronoLocalDate
     * @see java.time.chrono.ChronoLocalDateTime
     */
    val calType: Pair<Class<out ChronoLocalDate>, Class<out ChronoLocalDateTime<out ChronoLocalDate>>>

    /** A calendar used for navigating at main pages */
    var date: ChronoLocalDate

    /** A calendar that indicates today. */
    var todayDate: ChronoLocalDate

    /** A [Luna] key that indicates today */
    var todayLuna: String

    /** Other supported calendar types */
    val otherCalendars: List<Class<out ChronoLocalDate>>


    fun createDate(): ChronoLocalDate = calType.first.methods
        .find { it.name == "now" && it.parameterCount == 0 && Modifier.isStatic(it.modifiers) }!!
        .invoke(null) as ChronoLocalDate

    fun createDateTime(epochSeconds: Long): ChronoLocalDateTime<*> = calType.second.methods
        .find {
            it.name == "ofEpochSecond" && it.parameterCount == 3 &&
                    it.parameterTypes[0] == Long::class.javaPrimitiveType &&
                    it.parameterTypes[1] == Int::class.javaPrimitiveType &&
                    it.parameterTypes[2] == ZoneOffset::class.java &&
                    Modifier.isStatic(it.modifiers)
        }!!
        .invoke(null) as ChronoLocalDateTime<*>

    fun createDate(year: Int, month: Int, day: Int): ChronoLocalDate =
        calType.first.methods
            .find {
                it.name == "of" && it.parameterCount == 3 &&
                        it.parameterTypes.all { it == Int::class.javaPrimitiveType } &&
                        Modifier.isStatic(it.modifiers)
            }!!
            .invoke(null, year, month, day) as ChronoLocalDate

    fun lunaToDate(luna: String): ChronoLocalDate {
        val spl = luna.split(".")
        return createDate(spl[0].toInt(), spl[1].toInt(), 1)
    }

    /** Copies data from [stored] into [backup]. */
    fun backupVita() {
        FileOutputStream(backup).use { fos ->
            fos.write(FileInputStream(stored).use { it.readBytes() })
        }
    }
}
