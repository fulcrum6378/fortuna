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
