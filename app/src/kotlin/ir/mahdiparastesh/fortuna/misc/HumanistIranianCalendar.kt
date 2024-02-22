package ir.mahdiparastesh.fortuna.misc

import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.icu.util.ULocale
import java.util.Date
import java.util.Locale

/**
 * Humanist Iranian Calendar is an implementation of the Iranian calendar whose numbering system
 * starts with the foundation of the ancient city Susa of Iran, marking the spark of civilisation
 * in the area.
 *
 * The exact number is 4395 BC (a calibrated radio-carbon date), 5016 year before Hijrah. In order
 * to make it easy 16 is subtracted from it. So it only needs to add "5" to the first digit of the
 * previous calendar:
 * Humanist Iranian year = 5000 + Islamic Iranian year (e.g. 1401 -> 6401)
 *
 * @see <a href="https://en.wikipedia.org/wiki/Susa">Susa - Wikipedia</a>
 */
@Suppress("PrivatePropertyName", "LocalVariableName", "unused")
class HumanistIranianCalendar : Calendar {

    constructor() : this(TimeZone.getDefault(), ULocale.getDefault(ULocale.Category.FORMAT))
    constructor(zone: TimeZone) : this(zone, ULocale.getDefault(ULocale.Category.FORMAT))
    constructor(aLocale: Locale) : this(TimeZone.getDefault(), aLocale)
    constructor(locale: ULocale) : this(TimeZone.getDefault(), locale)

    constructor(zone: TimeZone, aLocale: Locale) : super(zone, aLocale) {
        timeInMillis = System.currentTimeMillis()
    }

    constructor(zone: TimeZone, locale: ULocale) : super(zone, locale) {
        timeInMillis = System.currentTimeMillis()
    }

    constructor(date: Date) :
            super(TimeZone.getDefault(), ULocale.getDefault(ULocale.Category.FORMAT)) {
        this.time = date
    }

    constructor(year: Int, month: Int, day: Int) :
            super(TimeZone.getDefault(), ULocale.getDefault(ULocale.Category.FORMAT)) {
        this[YEAR] = year
        this[MONTH] = month
        this[DATE] = day
    }

    constructor(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int) :
            super(TimeZone.getDefault(), ULocale.getDefault(ULocale.Category.FORMAT)) {
        this[YEAR] = year
        this[MONTH] = month
        this[DATE] = day
        this[HOUR_OF_DAY] = hour
        this[MINUTE] = minute
        this[SECOND] = second
    }

    private val MONTH_COUNT = arrayOf(
        //        len len2 st
        intArrayOf(31, 31, 0),   // Farvardin
        intArrayOf(31, 31, 31),  // Ordibehesht
        intArrayOf(31, 31, 62),  // Khordad
        intArrayOf(31, 31, 93),  // Tir
        intArrayOf(31, 31, 124), // Mordad
        intArrayOf(31, 31, 155), // Shahrivar
        intArrayOf(30, 30, 186), // Mehr
        intArrayOf(30, 30, 216), // Aban
        intArrayOf(30, 30, 246), // Azar
        intArrayOf(30, 30, 276), // Dey
        intArrayOf(30, 30, 306), // Bahman
        intArrayOf(29, 30, 336)  // Esfand
        // len  length of month
        // len2 length of month in a leap year
        // st   days in year before start of month
    )
    private val EPOCH = 122108 // Islamic: 1948320
    private val LIMITS = arrayOf(
        //          Minimum  Greatest    Least  Maximum
        //                    Minimum  Maximum
        intArrayOf(/*   */0,/*    */0,  /* */0,/*   */0), // ERA
        intArrayOf(-5000000, -5000000, 5000000, 5000000), // YEAR
        intArrayOf(/*   */0,/*    */0,/*  */11,/*  */11), // MONTH
        intArrayOf(/*   */1,/*    */1,/*  */52,/*  */53), // WEEK_OF_YEAR
        intArrayOf(/*                                */), // WEEK_OF_MONTH
        intArrayOf(/*   */1,/*    */1,/*  */29,/*  */31), // DAY_OF_MONTH
        intArrayOf(/*   */1,/*    */1,/* */365,/* */366), // DAY_OF_YEAR
        intArrayOf(/*                                */), // DAY_OF_WEEK
        intArrayOf(/*  */-1,/*   */-1,/*   */5,/*   */5), // DAY_OF_WEEK_IN_MONTH
        intArrayOf(/*                                */), // AM_PM
        intArrayOf(/*                                */), // HOUR
        intArrayOf(/*                                */), // HOUR_OF_DAY
        intArrayOf(/*                                */), // MINUTE
        intArrayOf(/*                                */), // SECOND
        intArrayOf(/*                                */), // MILLISECOND
        intArrayOf(/*                                */), // ZONE_OFFSET
        intArrayOf(/*                                */), // DST_OFFSET
        intArrayOf(-5000000, -5000000, 5000000, 5000000), // YEAR_WOY
        intArrayOf(/*                                */), // DOW_LOCAL
        intArrayOf(-5000000, -5000000, 5000000, 5000000), // EXTENDED_YEAR
        intArrayOf(/*                                */), // JULIAN_DAY
        intArrayOf(/*                                */), // MILLISECONDS_IN_DAY
    )


    override fun handleGetLimit(field: Int, limitType: Int): Int =
        LIMITS[field][limitType]

    /** Determine whether a year is a leap year in the calendar */
    private fun isLeapYear(year: Int): Boolean {
        val remainder = IntArray(1)
        floorDivide(25 * year + 11, 33, remainder)
        return remainder[0] < 8
    }

    /** Return the number of days in the given year */
    override fun handleGetYearLength(extendedYear: Int): Int =
        if (isLeapYear(extendedYear)) 366 else 365

    /** Return the length (in days) of the given month. */
    override fun handleGetMonthLength(extendedYear: Int, month: Int): Int {
        // If the month is out of range, adjust it into range, and
        // modify the extended year value accordingly.
        var extendedYear_ = extendedYear
        var month_ = month
        if (month < 0 || month > 11) {
            val rem = IntArray(1)
            extendedYear_ += floorDivide(month, 12, rem)
            month_ = rem[0]
        }
        return MONTH_COUNT[month_][if (isLeapYear(extendedYear_)) 1 else 0]
    }

    /** Return JD of start of given month/year */
    override fun handleComputeMonthStart(eYear: Int, month: Int, useMonth: Boolean): Int {
        // If the month is out of range, adjust it into range, and
        // modify the extended year value accordingly.
        var eYear_ = eYear
        var month_ = month
        if (month < 0 || month > 11) {
            val rem = IntArray(1)
            eYear_ += floorDivide(month, 12, rem)
            month_ = rem[0]
        }
        var julianDay = EPOCH - 1 + 365 * (eYear_ - 1) +
                floorDivide(8 * eYear_ + 21, 33)
        if (month_ != 0) julianDay += MONTH_COUNT[month_][2]
        return julianDay
    }

    override fun handleGetExtendedYear(): Int = internalGet(
        if (newerField(EXTENDED_YEAR, YEAR) == EXTENDED_YEAR) EXTENDED_YEAR else YEAR, 1
    ) // default to year 1;

    override fun handleComputeFields(julianDay: Int) {
        val year: Int
        val month: Int
        val dayOfMonth: Int
        var dayOfYear: Int
        val daysSinceEpoch = (julianDay - EPOCH).toLong()
        year = 1 + floorDivide(33 * daysSinceEpoch + 3, 12053).toInt()
        val farvardin1 = 365L * (year - 1L) + floorDivide(8L * year + 21, 33L)
        dayOfYear = (daysSinceEpoch - farvardin1).toInt() // 0-based
        month = if (dayOfYear < 216) // compute 0-based month
            dayOfYear / 31 else (dayOfYear - 6) / 30
        dayOfMonth = dayOfYear - MONTH_COUNT[month][2] + 1
        ++dayOfYear // Make it 1-based now

        internalSet(ERA, 0)
        internalSet(YEAR, year)
        internalSet(EXTENDED_YEAR, year)
        internalSet(MONTH, month)
        internalSet(DAY_OF_MONTH, dayOfMonth)
        internalSet(DAY_OF_YEAR, dayOfYear)
    }

    override fun getType(): String = "humanist_iranian"
}
