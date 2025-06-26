package ir.mahdiparastesh.fortuna.base

import ir.mahdiparastesh.fortuna.Luna
import ir.mahdiparastesh.fortuna.util.NumberUtils.toKey
import java.time.temporal.ChronoUnit

/**
 * The main page of Fortuna must have:
 * 1. a Panel for navigating through calendars
 * 2. a Grid for the current month and the scores given to its days
 */
interface MainPage {
    val c: FortunaContext

    /** Creates a light version of the primary theme colour. */
    fun createCPL(): FloatArray = floatArrayOf(0.296875f, 0.68359375f, 0.3125f)  // #4CAF50

    /** Creates a dark version of the primary theme colour. */
    fun createCPD(): FloatArray = floatArrayOf(0.01171875f, 0.296875f, 0.0234375f)  // #034C06

    /** Creates a light version of the secondary theme colour. */
    fun createCSL(): FloatArray = floatArrayOf(0.953125f, 0.26171875f, 0.2109375f)  // #F44336

    /** Creates a dark version of the secondary theme colour. */
    fun createCSD(): FloatArray = floatArrayOf(0.40234375f, 0.05078125f, 0.0234375f)  // #670D06

    val cp: FloatArray
    val cpl: FloatArray
    val cs: FloatArray
    val csl: FloatArray


    /** Updates only the year and month inputs of Panel. */
    fun updatePanel()

    /** Refreshes Grid and its header contents which reside in Panel. ALso adjusts Grid's size. */
    fun updateGrid()

    /** Adds N months to or subtracts them from the [FortunaContext.date]. */
    fun moveInMonths(forward: Boolean, nTimes: Long = 1L) {
        c.date =
            if (forward) c.date.plus(nTimes, ChronoUnit.MONTHS)
            else c.date.minus(nTimes, ChronoUnit.MONTHS)
        onDateChanged()
    }

    /** Adds N years to or subtracts them from the [FortunaContext.date]. */
    fun moveInYears(to: Int)

    /** Updates everything whenever the calendar changes. */
    fun onDateChanged() {
        c.luna = c.date.toKey()
        updatePanel()
        updateGrid()
    }

    /** Saves data of a day cell in the [ir.mahdiparastesh.fortuna.Vita]. */
    fun saveDies(luna: Luna, i: Int, score: Float?, emoji: String?, verbum: String?) {
        luna.set(i, score, emoji, verbum)
        c.vita.save()
        updateGrid()
    }
}
