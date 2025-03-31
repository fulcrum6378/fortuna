package ir.mahdiparastesh.fortuna.time;

import static java.time.temporal.ChronoField.ERA;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.PROLEPTIC_MONTH;
import static java.time.temporal.ChronoField.YEAR;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Period;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoPeriod;
import java.time.chrono.Chronology;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.JulianFields;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.Objects;

/**
 * This is an implementation of Solar Hijri calendar (also known as Jalali calendar,
 * Persian calendar).
 * <p>
 * {@code PersianDate} is an immutable date-time object that represents a date,
 * often viewed as year-month-day.
 * <p>
 * In order to simplify usage of this class, it is tried to make API of this class
 * the same as JDK8 {@link LocalDate} class. Since some methods of {@link LocalDate} were
 * useful for Persian calendar system, they have been exactly copied. Some other methods of
 * {@link java.time.chrono.HijrahDate} and {@link java.time.chrono.JapaneseDate} have been
 * modified and used in this class.
 * <p>
 * This class is immutable and can be used in multi-threaded programs.
 *
 * @author Mahmoud Fathi
 */
public final class PersianDate implements ChronoLocalDate {

    /**
     * The minimum supported persian date {@code 0001-01-01}.
     */
    public static final PersianDate MIN =
            PersianDate.of((int) PersianChronology.INSTANCE.range(YEAR).getMinimum(), 1, 1);

    /**
     * The maximum supported persian date {@code 1999-12-29}.
     */
    public static final PersianDate MAX =
            PersianDate.of((int) PersianChronology.INSTANCE.range(YEAR).getMaximum(), 12, 29);

    /**
     * 1970-01-01 to julian day.
     */
    private static final long JULIAN_DAY_TO_1970 = 2440588L;

    /**
     * Constant for cycle of of days.
     */
    private static final long CYCLE_DAYS = 1029983;

    /**
     * Constant for cycle of years.
     */
    private static final int CYCLE_YEARS = 2820;

    /**
     * Constant for length of year.
     */
    private static final double YEAR_LENGTH = 365.24219858156028368;

    /**
     * Constant for Persian epoch date.
     */
    private static final long PERSIAN_DATE_EPOCH = 2121446;

    /**
     * Constant for leap year threshold.
     */
    private static final double LEAP_THRESHOLD = 0.24219858156028368;

    /**
     * The year.
     */
    private final int year;

    /**
     * The month-of-year.
     */
    private final int month;

    /**
     * The day-of-month.
     */
    private final int day;

    /**
     * @return the year
     */
    public int getYear() {
        return year + 5000;
    }

    /**
     * @return the month-of-year field using the {@code Month} enum.
     * @see #getMonthValue()
     */
    public PersianMonth getMonth() {
        return PersianMonth.of(month);
    }

    /**
     * @return the month-of-year, from 1 to 12
     * @see #getMonth()
     */
    public int getMonthValue() {
        return month;
    }

    /**
     * @return day-of-month, from 1 to 31
     */
    public int getDayOfMonth() {
        return day;
    }

    /**
     * @return day-of-year, from 1 to 365 or 366 in a leap year
     */
    public int getDayOfYear() {
        return PersianMonth.of(month).daysToFirstOfMonth() + day;
    }

    /**
     * Returns day-of-week as an enum {@link DayOfWeek}. This avoids confusion as to what
     * {@code int} means. If you need access to the primitive {@code int} value then the
     * enum provides the {@link DayOfWeek#getValue() int value}.
     *
     * @return day-of-week, which is an enum {@link DayOfWeek}
     */
    public DayOfWeek getDayOfWeek() {
        int dow0 = Math.floorMod((int) toEpochDay() + 3, 7);
        return DayOfWeek.of(dow0 + 1);
    }

    /**
     * Obtains current Persian date from the system clock in the default time zone.
     *
     * @return current Persian date from the system clock in the default time zone
     */
    public static PersianDate now() {
        return ofJulianDays(JulianFields.JULIAN_DAY.getFrom(LocalDate.now()));
    }

    /**
     * Obtains an instance of {@code PersianDate} with year, month and day of month.
     * The value of month must be between {@code 1} and {@code 12}. Value {@code 1} would
     * be {@link PersianMonth#FARVARDIN} and value {@code 12} represents
     * {@link PersianMonth#ESFAND}.
     *
     * @param year       the year to represent, from 1 to MAX_YEAR
     * @param month      the value of month, from 1 to 12
     * @param dayOfMonth the dayOfMonth to represent, from 1 to 31
     * @return an instance of {@code PersianDate}
     * @throws DateTimeException if the passed parameters do not form a valid date or time.
     */
    public static PersianDate of(int year, int month, int dayOfMonth) {
        return new PersianDate(year, month, dayOfMonth);
    }

    /**
     * Obtains an instance of {@code PersianDate} with year, month and day of month.
     *
     * @param year       the year to represent, from 1 to MAX_YEAR
     * @param month      the month-of-year to represent, an instance of {@link PersianMonth}
     * @param dayOfMonth the dayOfMonth to represent, from 1 to 31
     * @return an instance of {@code PersianDate}
     * @throws DateTimeException if the passed parameters do not form a valid date or time.
     */
    public static PersianDate of(int year, PersianMonth month, int dayOfMonth) {
        Objects.requireNonNull(month, "month");
        return new PersianDate(year, month.getValue(), dayOfMonth);
    }

    /**
     * Returns an instance of {@code PersianDate} that is correspondent to the gregorian
     * date of parameter {@code localDate}.
     *
     * @param localDate Gregorian date and time, not null
     * @return an equivalent Persian date and time as an instance of {@link PersianDate}
     */
    public static PersianDate fromGregorian(LocalDate localDate) {
        Objects.requireNonNull(localDate, "localDate");
        return ofJulianDays(JulianFields.JULIAN_DAY.getFrom(localDate));
    }

    /**
     * Obtains an instance of {@code PersianDate} from a text, assuming its format is {@code yyyy-MM-dd}.
     * For example the given text could be {@code 1399-10-12}, otherwise an exception will be thrown.
     *
     * @param text the text to parse, not {@code null}
     * @return an instance of {@code PersianDate} from the given text
     */
    public static PersianDate parse(final CharSequence text) {
        return parse(text, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /**
     * Obtains an instance of {@code PersianDate} from a text string using a specific formatter.
     *
     * @param text      the text to parse, not {@code null}
     * @param formatter the formatter to use, not {@code null}
     * @return an instance of {@code PersianDate} from the given text
     */
    public static PersianDate parse(final CharSequence text, final DateTimeFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter");
        return formatter.withChronology(PersianChronology.INSTANCE).parse(text, PersianDate::from);
    }

    /**
     * Obtains an instance of {@code PersianDate} from a temporal object.
     *
     * @param temporal the temporal object to convert, not {@code null}
     * @return the local date, not {@code null}
     * @throws DateTimeException if unable to convert to a {@code PersianDate}
     */
    public static PersianDate from(final TemporalAccessor temporal) {
        Objects.requireNonNull(temporal, "temporal");
        return PersianChronology.INSTANCE.date(temporal);
    }

    /**
     * Returns an instance of {@link PersianDate}, based on number of epoch days,
     * which is from 1970-01-01. For example passing {@code 17468} as the parameter
     * results a persian date of 1396-08-07.
     *
     * @param epochDays epoch days
     * @return an instance of {@link PersianDate}
     */
    public static PersianDate ofEpochDay(long epochDays) {
        return ofJulianDays(epochDays + JULIAN_DAY_TO_1970);
    }

    /**
     * Returns an instance of {@link PersianDate}, based on number of julian days.
     * For example passing {@code 2458055} as the parameter will cause to get a
     * Persian date of "1396-8-6".
     *
     * @param julianDays julian days
     * @return an instance of {@link PersianDate}
     * @see <a href="https://github.com/soroush/libcalendars">libcalendars</a>
     */
    public static PersianDate ofJulianDays(long julianDays) {
        final long offset = julianDays - PERSIAN_DATE_EPOCH;
        long cycle_no = offset / CYCLE_DAYS;
        if (offset < 0) {
            --cycle_no;
        }
        final long cycleStart = PERSIAN_DATE_EPOCH + cycle_no * CYCLE_DAYS;
        final int yc = (int) (Math.floor((julianDays - cycleStart) / YEAR_LENGTH));
        long year = yc + 475 + cycle_no * 2820;
        final long lll = PERSIAN_DATE_EPOCH + cycle_no * CYCLE_DAYS + (long) Math.floor((yc * YEAR_LENGTH));
        long day = julianDays - lll + 1;
        if (day > (isLeapYear((int) year) ? 366 : 365)) {
            year++;
            day = 1;
        }
        if (year <= 0) {
            year--;
        }
        int month;
        for (month = 1; month < 12; ++month) {
            if (day > PersianMonth.of(month).length(isLeapYear((int) year))) {
                day -= PersianMonth.of(month).length(isLeapYear((int) year));
            } else {
                break;
            }
        }
        return PersianDate.of((int) year, month, (int) day);
    }

    /**
     * Constructor.
     *
     * @param year       the year to represent, from 1 to MAX_YEAR
     * @param month      the month-of-year to represent, not null, from {@link PersianMonth} enum
     * @param dayOfMonth the dayOfMonth-of-month to represent, from 1 to 31
     * @throws DateTimeException if the passed parameters do not form a valid date or time.
     */
    private PersianDate(int year, int month, int dayOfMonth) {
        PersianChronology.INSTANCE.checkValidValue(year, YEAR);
        PersianChronology.INSTANCE.checkValidValue(month, MONTH_OF_YEAR);
        boolean leapYear = PersianChronology.INSTANCE.isLeapYear(year);
        int maxDaysOfMonth = PersianMonth.of(month).length(leapYear);
        if (dayOfMonth > maxDaysOfMonth) {
            if (month == 12 && dayOfMonth == 30 && !leapYear) {
                throw new DateTimeException("Invalid date ESFAND 30, as " + year + " is not a leap year");
            }
            throw new DateTimeException("Invalid date " + PersianMonth.of(month).name() + " " + dayOfMonth);
        }
        this.year = year;
        this.month = month;
        this.day = dayOfMonth;
    }

    //-----------------------------------------------------------------------

    /**
     * Gets the chronology of this date, which is the Persian calendar system.
     * <p>
     * The {@code Chronology} represents the calendar system in use.
     * The era and other fields in {@link ChronoField} are defined by the chronology.
     *
     * @return the Persian chronology, not null
     */
    @Override
    public Chronology getChronology() {
        return PersianChronology.INSTANCE;
    }

    /**
     * Returns the length of the month represented by this date.
     * <p>
     * This returns the length of the month in days.
     *
     * @return the length of the month in days
     */
    @Override
    public int lengthOfMonth() {
        PersianMonth pm = PersianMonth.of(month);
        return PersianChronology.INSTANCE.isLeapYear(year) ? pm.maxLength() : pm.minLength();
    }

    /**
     * Calculates the amount of time until another date in terms of the specified unit.
     * <p>
     * This calculates the amount of time between two {@code PersianDate}
     * objects in terms of a single {@code TemporalUnit}.
     * The start and end points are {@code this} and the specified date.
     * The result will be negative if the end is before the start.
     * The {@code Temporal} passed to this method is converted to a
     * {@code PersianDate} using {@link #from(TemporalAccessor)}.
     * For example, the amount in days between two dates can be calculated
     * using {@code startDate.until(endDate, DAYS)}.
     * <p>
     * The calculation returns a whole number, representing the number of
     * complete units between the two dates.
     * For example, the amount in months between 1396-06-15 and 1396-08-14
     * will only be one month as it is one day short of two months.
     * <p>
     * There are two equivalent ways of using this method.
     * The first is to invoke this method.
     * The second is to use {@link TemporalUnit#between(Temporal, Temporal)}:
     * <pre>
     *   // these two lines are equivalent
     *   amount = start.until(end, MONTHS);
     *   amount = MONTHS.between(start, end);
     * </pre>
     * The choice should be made based on which makes the code more readable.
     * <p>
     * The calculation is implemented in this method for {@link ChronoUnit}.
     * The units {@code DAYS}, {@code WEEKS}, {@code MONTHS}, {@code YEARS},
     * {@code DECADES}, {@code CENTURIES}, {@code MILLENNIA} and {@code ERAS}
     * are supported. Other {@code ChronoUnit} values will throw an exception.
     * <p>
     * If the unit is not a {@code ChronoUnit}, then the result of this method
     * is obtained by invoking {@code TemporalUnit.between(Temporal, Temporal)}
     * passing {@code this} as the first argument and the converted input temporal
     * as the second argument.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param endExclusive the end date, exclusive, which is converted to a {@code PersianDate}, not null
     * @param unit         the unit to measure the amount in, not null
     * @return the amount of time between this date and the end date
     * @throws DateTimeException                if the amount cannot be calculated, or the end
     *                                          temporal cannot be converted to a {@code PersianDate}
     * @throws UnsupportedTemporalTypeException if the unit is not supported
     * @throws ArithmeticException              if numeric overflow occurs
     */
    @Override
    public long until(Temporal endExclusive, TemporalUnit unit) {
        Objects.requireNonNull(endExclusive, "endExclusive");
        Objects.requireNonNull(unit, "unit");
        PersianDate end = (PersianDate) getChronology().date(endExclusive);
        if (unit instanceof ChronoUnit) {
            return switch ((ChronoUnit) unit) {
                case DAYS -> daysUntil(end);
                case WEEKS -> daysUntil(end) / 7;
                case MONTHS -> monthsUntil(end);
                case YEARS -> monthsUntil(end) / 12;
                case DECADES -> monthsUntil(end) / 120;
                case CENTURIES -> monthsUntil(end) / 1200;
                case MILLENNIA -> monthsUntil(end) / 12000;
                case ERAS -> end.getLong(ERA) - getLong(ERA);
                default -> throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
            };
        }
        return unit.between(this, end);
    }

    private long daysUntil(PersianDate end) {
        return end.toEpochDay() - toEpochDay();  // no overflow
    }

    private long monthsUntil(PersianDate end) {
        long packed1 = getLong(PROLEPTIC_MONTH) * 32L + getDayOfMonth();  // no overflow
        long packed2 = end.getLong(PROLEPTIC_MONTH) * 32L + end.getDayOfMonth();  // no overflow
        return (packed2 - packed1) / 32;
    }

    /**
     * Calculates the period between this date and another date as a {@code Period}.
     * <p>
     * This calculates the period between two dates in terms of years, months and days.
     * The start and end points are {@code this} and the specified date.
     * The result will be negative if the end is before the start.
     * The negative sign will be the same in each of year, month and day.
     * <p>
     * The calculation is performed using the ISO calendar system.
     * If necessary, the input date will be converted to ISO.
     * <p>
     * The start date is included, but the end date is not.
     * The period is calculated by removing complete months, then calculating
     * the remaining number of days, adjusting to ensure that both have the same sign.
     * The number of months is then normalized into years and months based on a 12 month year.
     * A month is considered to be complete if the end day-of-month is greater
     * than or equal to the start day-of-month.
     * For example, from {@code 2010-01-15} to {@code 2011-03-18} is "1 year, 2 months and 3 days".
     * <p>
     * There are two equivalent ways of using this method.
     * The first is to invoke this method.
     * The second is to use {@link Period#between(LocalDate, LocalDate)}:
     * <pre>
     *   // these two lines are equivalent
     *   period = start.until(end);
     *   period = Period.between(start, end);
     * </pre>
     * The choice should be made based on which makes the code more readable.
     *
     * @param endDateExclusive the end date, exclusive, which may be in any chronology, not null
     * @return the period between this date and the end date, not null
     */
    @Override
    public ChronoPeriod until(ChronoLocalDate endDateExclusive) {
        Objects.requireNonNull(endDateExclusive, "endDateExclusive");
        PersianDate end = PersianChronology.INSTANCE.date(endDateExclusive);
        long totalMonths = end.getLong(PROLEPTIC_MONTH) - this.getLong(PROLEPTIC_MONTH);  // safe
        int days = end.day - this.day;
        if (totalMonths > 0 && days < 0) {
            totalMonths--;
            PersianDate calcDate = this.plusMonths(totalMonths);
            days = (int) (end.toEpochDay() - calcDate.toEpochDay());  // safe
        } else if (totalMonths < 0 && days > 0) {
            totalMonths++;
            days -= end.lengthOfMonth();
        }
        long years = totalMonths / 12;  // safe
        int months = (int) (totalMonths % 12);  // safe
        return Period.of(Math.toIntExact(years), months, days);
    }

    /**
     * Gets the value of the specified field from this date as a {@code long}.
     * <p>
     * This queries this date for the value for the specified field.
     * If it is not possible to return the value, because the field is not supported
     * or for some other reason, an exception is thrown.
     * <p>
     * If the field is a {@link ChronoField} then the query is implemented here.
     * The {@link #isSupported(TemporalField) supported fields} will return valid
     * values based on this date.
     * All other {@code ChronoField} instances will throw an {@code UnsupportedTemporalTypeException}.
     * <p>
     * If the field is not a {@code ChronoField}, then the result of this method
     * is obtained by invoking {@code TemporalField.getFrom(TemporalAccessor)}
     * passing {@code this} as the argument. Whether the value can be obtained,
     * and what the value represents, is determined by the field.
     *
     * @param field the field to get, not null
     * @return the value for the field
     * @throws DateTimeException                if a value for the field cannot be obtained
     * @throws UnsupportedTemporalTypeException if the field is not supported
     * @throws ArithmeticException              if numeric overflow occurs
     */
    @Override
    public long getLong(TemporalField field) {
        if (field instanceof ChronoField) {
            switch ((ChronoField) field) {
                case DAY_OF_WEEK:
                    return getDayOfWeek().getValue();
                case ALIGNED_DAY_OF_WEEK_IN_MONTH:
                    return ((day - 1) % 7) + 1;
                case ALIGNED_DAY_OF_WEEK_IN_YEAR:
                    return ((getDayOfYear() - 1) % 7) + 1;
                case DAY_OF_MONTH:
                    return this.day;
                case DAY_OF_YEAR:
                    return this.getDayOfYear();
                case EPOCH_DAY:
                    return this.toEpochDay();
                case ALIGNED_WEEK_OF_MONTH:
                    return ((day - 1) / 7) + 1;
                case ALIGNED_WEEK_OF_YEAR:
                    return ((getDayOfYear() - 1) / 7) + 1;
                case MONTH_OF_YEAR:
                    return month;
                case PROLEPTIC_MONTH:
                    return (year * 12L + month - 1);
                case YEAR_OF_ERA:
                    return (year >= 1 ? year : 1 - year);
                case YEAR:
                    return year;
                case ERA:
                    return (year >= 1 ? 1 : 0);
            }
        }
        throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
    }

    /**
     * Returns a copy of this {@code PersianDate} with the specified period in years added.
     * <p>
     * This method adds the specified amount to the years field in three steps:
     * <ol>
     * <li>Add the input years to the year field</li>
     * <li>Check if the resulting date would be invalid</li>
     * <li>Adjust the day-of-month to the last valid day if necessary</li>
     * </ol>
     * <p>
     * For example, 1387-12-30 (leap year) plus one year would result in the
     * invalid date 1388-12-30 (standard year). Instead of returning an invalid
     * result, the last valid day of the month, 1388-12-29, is selected instead.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param yearsToAdd the years to add, may be negative
     * @return a {@code PersianDate} based on this date with the years added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    public PersianDate plusYears(long yearsToAdd) {
        return plusMonths(yearsToAdd * 12);
    }

    /**
     * Returns a copy of this {@code PersianDate} with the specified period in months added.
     * <p>
     * This method adds the specified amount to the months field in three steps:
     * <ol>
     * <li>Add the input months to the month-of-year field</li>
     * <li>Check if the resulting date would be invalid</li>
     * <li>Adjust the day-of-month to the last valid day if necessary</li>
     * </ol>
     * <p>
     * For example, 1388-11-30 plus one month would result in the invalid date
     * 1388-12-30. Instead of returning an invalid result, the last valid day
     * of the month, 1388-12-29, is selected instead.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param monthsToAdd the months to add, may be negative
     * @return a {@code PersianDate} based on this date with the months added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    public PersianDate plusMonths(long monthsToAdd) {
        if (monthsToAdd == 0) {
            return this;
        }
        long monthCount = year * 12L + (month - 1);
        long calcMonths = monthCount + monthsToAdd;
        int newYear = (int) Math.floorDiv(calcMonths, 12L);
        int newMonth = (int) Math.floorMod(calcMonths, 12L) + 1;
        return resolvePreviousValid(newYear, newMonth, day);
    }

    /**
     * Returns a copy of this {@code PersianDate} with the specified number of days added.
     * <p>
     * This method adds the specified amount to the days field incrementing the
     * month and year fields as necessary to ensure the result remains valid.
     * The result is only invalid if the maximum/minimum year is exceeded.
     * <p>
     * For example, 1396-12-29 plus one day would result in 1397-01-01.
     * <p>
     * This instance is immutable and unaffected by this method call.
     *
     * @param daysToAdd the days to add, may be negative
     * @return a {@code PersianDate} based on this date with the days added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    public PersianDate plusDays(long daysToAdd) {
        if (daysToAdd == 0) {
            return this;
        }
        return ofJulianDays(toJulianDay() + daysToAdd);
    }

    /**
     * Returns true if {@code year} is a leap year in Persian calendar.
     *
     * @return true if {@code year} is a leap year in Persian calendar
     */
    @Override
    public boolean isLeapYear() {
        return isLeapYear(year);
    }

    /**
     * Returns {@code true} if the given year is a leap year in Persian calendar.
     *
     * @param year the year to be checked
     * @return {@code true} if the given year is a leap year in Persian calendar.
     */
    public static boolean isLeapYear(final int year) {
        JalaliUtils.intRequirePositive(year, "year");
        return ((year + 2346) * LEAP_THRESHOLD % 1) < LEAP_THRESHOLD;
    }

    /**
     * Resolves the date, resolving days past the end of month.
     *
     * @param year  the year to represent
     * @param month the month-of-year to represent, validated from 1 to 12
     * @param day   the day-of-month to represent, validated from 1 to 31
     * @return the resolved date, not null
     */
    private PersianDate resolvePreviousValid(int year, int month, int day) {
        boolean leapYear = PersianChronology.INSTANCE.isLeapYear(year);
        int maxDaysOfMonth = PersianMonth.of(month).length(leapYear);
        if (day > maxDaysOfMonth) {
            day = maxDaysOfMonth;
        }
        return PersianDate.of(year, month, day);
    }

    /**
     * Returns an equivalent Gregorian date and time as an instance of {@link LocalDate}.
     * Calling this method has no effect on the object that calls this.
     *
     * @return the equivalent Gregorian date as an instance of {@link LocalDate}
     */
    public LocalDate toGregorian() {
        return LocalDate.from(this);
    }

    @Override
    public long toEpochDay() {
        return toJulianDay() - JULIAN_DAY_TO_1970;
    }

    /**
     * Returns number of corresponding julian days. For number of julian days of
     * PersianDate.of(1396, 8, 6) is 2458054.
     * <p>
     * Calling this method has no effect on this instance.
     *
     * @return number of corresponding julian days
     * @see <a href="http://www.fourmilab.ch/documents/calendar/">calendar convertor</a>
     */
    public long toJulianDay() {
        return toJulianDay(year, month, day);
    }

    /**
     * Returns number of corresponding julian days. For number of julian days of
     * PersianDate.of(1396, 8, 6) is 2458054. This method is provided in order to
     * prevent creating unnecessary instances of {@code PersianDate} only to calculate
     * julian day.
     *
     * @return number of corresponding julian days
     * @see <a href="http://www.fourmilab.ch/documents/calendar/">calendar convertor</a>
     */
    static long toJulianDay(int year, int month, int dayOfMonth) {

        long era = (year - 475) / CYCLE_YEARS;
        if ((year - 475) < 0) {
            era--;
        }
        final long y_c = (year - 475) - era * CYCLE_YEARS;
        final long f_d = PERSIAN_DATE_EPOCH + era * CYCLE_DAYS + (long) Math.floor((y_c * YEAR_LENGTH));
        return f_d + PersianMonth.of(month).daysToFirstOfMonth() + dayOfMonth - 1;
    }
    //-----------------------------------------------------------------------

    /**
     * Checks if this date is equal to another date.
     * <p>
     * Compares this {@code PersianDate} with another ensuring that the date is the same.
     *
     * @param obj the object to check, null returns false
     * @return true if this is equal to the other date
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof PersianDate) {
            return compareTo((PersianDate) obj) == 0;
        }
        return false;
    }

    /**
     * A hash code for this persian date.
     *
     * @return a suitable hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(year, month, day);
    }

    //-----------------------------------------------------------------------

    /**
     * Returns the string representation of this persian date. The string contains of ten
     * characters whose format is "XXXX-YY-ZZ", where XXXX is the year, YY is the
     * month-of-year and ZZ is day-of-month. (Each of the capital characters represents a
     * single decimal digit.)
     * <p>
     * If any of the three parts of this persian date is too small to fill up its field,
     * the field is padded with leading zeros.
     *
     * @return a suitable representation of this persian date
     */
    public String toString() {
        return String.format("%04d-%02d-%02d", year, month, day);
    }
}
