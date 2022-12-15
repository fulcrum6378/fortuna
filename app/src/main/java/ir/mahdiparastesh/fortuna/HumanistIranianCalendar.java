package ir.mahdiparastesh.fortuna;

import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.icu.util.ULocale;
import android.icu.util.ULocale.Category;

import java.util.Date;
import java.util.Locale;

/**
 * Humanist Iranian Calendar is an implementation of the Iranian calendar whose numbering system
 * starts with the foundation of the ancient city Susa of Iran, marking the spark of civilisation
 * in this land.
 *
 * The exact number is 4395 BC (a calibrated radio-carbon date), 5016 year before Hijrah. In order
 * to make it easy 16 is subtracted from it. So it only needs to add "5" to the first digit of the
 * previous calendar:
 * Humanist Iranian year = 5000 + Islamic Iranian year (e.g. 1401 -> 6401)
 *
 * For more information:
 * https://en.wikipedia.org/wiki/Susa
 */
@SuppressWarnings("unused")
public class HumanistIranianCalendar extends Calendar {

    private static final int[][] MONTH_COUNT = {
            //len len2 st
            {31, 31, 0},   // Farvardin
            {31, 31, 31},  // Ordibehesht
            {31, 31, 62},  // Khordad
            {31, 31, 93},  // Tir
            {31, 31, 124}, // Mordad
            {31, 31, 155}, // Shahrivar
            {30, 30, 186}, // Mehr
            {30, 30, 216}, // Aban
            {30, 30, 246}, // Azar
            {30, 30, 276}, // Dey
            {30, 30, 306}, // Bahman
            {29, 30, 336}  // Esfand
            // len  length of month
            // len2 length of month in a leap year
            // st   days in year before start of month
    };
    private static final int EPOCH = 122108;

    public HumanistIranianCalendar() {
        this(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
    }

    public HumanistIranianCalendar(TimeZone zone) {
        this(zone, ULocale.getDefault(Category.FORMAT));
    }

    public HumanistIranianCalendar(Locale aLocale) {
        this(TimeZone.getDefault(), aLocale);
    }

    public HumanistIranianCalendar(ULocale locale) {
        this(TimeZone.getDefault(), locale);
    }

    public HumanistIranianCalendar(TimeZone zone, Locale aLocale) {
        super(zone, aLocale);
        setTimeInMillis(System.currentTimeMillis());
    }

    public HumanistIranianCalendar(TimeZone zone, ULocale locale) {
        super(zone, locale);
        setTimeInMillis(System.currentTimeMillis());
    }

    public HumanistIranianCalendar(Date date) {
        super(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
        this.setTime(date);
    }

    public HumanistIranianCalendar(int year, int month, int date) {
        super(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
        this.set(Calendar.YEAR, year);
        this.set(Calendar.MONTH, month);
        this.set(Calendar.DATE, date);
    }

    public HumanistIranianCalendar(int year, int month, int date, int hour, int minute, int second) {
        super(TimeZone.getDefault(), ULocale.getDefault(Category.FORMAT));
        this.set(Calendar.YEAR, year);
        this.set(Calendar.MONTH, month);
        this.set(Calendar.DATE, date);
        this.set(Calendar.HOUR_OF_DAY, hour);
        this.set(Calendar.MINUTE, minute);
        this.set(Calendar.SECOND, second);
    }

    private static final int[][] LIMITS = {
            // Minimum Greatest    Least   Maximum
            //          Minimum  Maximum
            {/*   */0,/*   */0,/*   */0,/*    */0}, // ERA
            {-5000000, -5000000, 5000000, 5000000}, // YEAR
            {/*   */0,/*   */0,/*  */11,/*   */11}, // MONTH
            {/*   */1,/*   */1,/*  */52,/*   */53}, // WEEK_OF_YEAR
            {/*                                */}, // WEEK_OF_MONTH
            {/*   */1,/*   */1,/*  */29,/*   */31}, // DAY_OF_MONTH
            {/*   */1,/*   */1,/*  */365,/* */366}, // DAY_OF_YEAR
            {/*                                */}, // DAY_OF_WEEK
            {/*  */-1,/*  */-1,/*  */5,/*     */5}, // DAY_OF_WEEK_IN_MONTH
            {/*                                */}, // AM_PM
            {/*                                */}, // HOUR
            {/*                                */}, // HOUR_OF_DAY
            {/*                                */}, // MINUTE
            {/*                                */}, // SECOND
            {/*                                */}, // MILLISECOND
            {/*                                */}, // ZONE_OFFSET
            {/*                                */}, // DST_OFFSET
            {-5000000, -5000000, 5000000, 5000000}, // YEAR_WOY
            {/*                                */}, // DOW_LOCAL
            {-5000000, -5000000, 5000000, 5000000}, // EXTENDED_YEAR
            {/*                                */}, // JULIAN_DAY
            {/*                                */}, // MILLISECONDS_IN_DAY
    };

    @Override
    protected int handleGetLimit(int field, int limitType) {
        return LIMITS[field][limitType];
    }

    // Determine whether a year is a leap year in the calendar
    private static boolean isLeapYear(int year) {
        int[] remainder = new int[1];
        floorDivide(25 * year + 11, 33, remainder);
        return remainder[0] < 8;
    }

    // Return the number of days in the given year
    protected int handleGetYearLength(int extendedYear) {
        return isLeapYear(extendedYear) ? 366 : 365;
    }

    // Return the length (in days) of the given month.
    @Override
    protected int handleGetMonthLength(int extendedYear, int month) {
        // If the month is out of range, adjust it into range, and
        // modify the extended year value accordingly.
        if (month < 0 || month > 11) {
            int[] rem = new int[1];
            extendedYear += floorDivide(month, 12, rem);
            month = rem[0];
        }
        return MONTH_COUNT[month][isLeapYear(extendedYear) ? 1 : 0];
    }

    // Return JD of start of given month/year
    @Override
    protected int handleComputeMonthStart(int eYear, int month, boolean useMonth) {
        // If the month is out of range, adjust it into range, and
        // modify the extended year value accordingly.
        if (month < 0 || month > 11) {
            int[] rem = new int[1];
            eYear += floorDivide(month, 12, rem);
            month = rem[0];
        }
        int julianDay = EPOCH - 1 + 365 * (eYear - 1) + floorDivide(8 * eYear + 21, 33);
        if (month != 0) julianDay += MONTH_COUNT[month][2];
        return julianDay;
    }

    @Override
    protected int handleGetExtendedYear() {
        return internalGet((newerField(EXTENDED_YEAR, YEAR) == EXTENDED_YEAR)
                ? EXTENDED_YEAR : YEAR, 1);// default to year 1;
    }

    @Override
    protected void handleComputeFields(int julianDay) {
        int year, month, dayOfMonth, dayOfYear;
        long daysSinceEpoch = julianDay - EPOCH;
        year = 1 + (int) floorDivide(33 * daysSinceEpoch + 3, 12053);
        long farvardin1 = 365L * (year - 1) + floorDivide(8 * year + 21, 33);
        dayOfYear = (int) (daysSinceEpoch - farvardin1); // 0-based
        if (dayOfYear < 216) // compute 0-based month
            month = dayOfYear / 31;
        else month = (dayOfYear - 6) / 30;
        dayOfMonth = dayOfYear - MONTH_COUNT[month][2] + 1;
        ++dayOfYear; // Make it 1-based now

        internalSet(ERA, 0);
        internalSet(YEAR, year);
        internalSet(EXTENDED_YEAR, year);
        internalSet(MONTH, month);
        internalSet(DAY_OF_MONTH, dayOfMonth);
        internalSet(DAY_OF_YEAR, dayOfYear);
    }

    @Override
    public String getType() {
        return "humanist_iranian";
    }
}
