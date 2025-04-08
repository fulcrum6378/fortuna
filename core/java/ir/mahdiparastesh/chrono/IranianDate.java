package ir.mahdiparastesh.chrono;

import java.io.*;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoPeriod;
import java.time.chrono.Chronology;
import java.time.chrono.Era;
import java.time.temporal.*;
import java.util.Objects;

import static java.time.temporal.ChronoField.*;
import static java.time.temporal.ChronoUnit.MONTHS;

@SuppressWarnings("unused")
public class IranianDate
        implements Temporal, TemporalAdjuster, ChronoLocalDate, Serializable {

    private final int year;
    private final short month;
    private final short day;

    private IranianDate(int year, int month, int day) {
        this.year = year;
        this.month = (short) month;
        this.day = (short) day;

        if (month == 12 && day == 30 && !isLeapYear())
            throw new DateTimeException("Year " + year + " is not a leap year!");
    }


    //-------------------------BUILDERS--------------------------------------

    public static IranianDate of(int year, int month, int day) {
        YEAR.checkValidValue(year);
        MONTH_OF_YEAR.checkValidValue(month);
        IranianChronology.INSTANCE.range(DAY_OF_MONTH).checkValidValue(day, DAY_OF_MONTH);
        return new IranianDate(year, month, day);
    }

    public static IranianDate ofYearDay(int year, int dayOfYear) {
        YEAR.checkValidValue(year);
        DAY_OF_YEAR.checkValidValue(dayOfYear);

        boolean leap = IranianChronology.INSTANCE.isLeapYear(year);
        if (dayOfYear == 366 && !leap)
            throw new DateTimeException(
                    "Invalid date 'DayOfYear 366' as '" + year + "' is not a leap year");

        int month = 1;
        int maxDaysInMonth = 31;

        while (dayOfYear > maxDaysInMonth) {
            dayOfYear -= maxDaysInMonth;
            month++;
            maxDaysInMonth = (month < 7) ? 31 : 30;
        }

        return new IranianDate(year, month, dayOfYear);
    }

    public static IranianDate ofEpochDay(long epochDay) {
        int y, yearLength;

        if (epochDay >= 79L) {  // after 6348
            epochDay -= 78L;
            y = 6349;
            while (true) {
                yearLength = IranianChronology.INSTANCE.isLeapYear(y) ? 366 : 365;
                if (epochDay > yearLength)
                    epochDay -= yearLength;
                else
                    return ofYearDay(y, (int) epochDay);
                y++;
            }

        } else if (epochDay < -286L) {  // before 6348
            epochDay = -epochDay - 287L;
            y = 6347;
            while (true) {
                yearLength = IranianChronology.INSTANCE.isLeapYear(y) ? 366 : 365;
                if (epochDay > yearLength)
                    epochDay -= yearLength;
                else
                    return ofYearDay(y, (int) (yearLength - epochDay));
                y--;
            }

        } else {  // during 6348 (1969-1970)
            return ofYearDay(6348, (int) epochDay + 287);
        }
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public Object clone() {
        return new IranianDate(year, month, day);
    }


    //-------------------------GETTERS---------------------------------------

    @Override
    public Chronology getChronology() {
        return IranianChronology.INSTANCE;
    }

    @Override
    public int lengthOfMonth() {
        if (month <= 6)
            return 31;
        else if (month <= 11)
            return 30;
        else
            return isLeapYear() ? 30 : 29;
    }

    @Override
    public long getLong(TemporalField field) {
        if (field instanceof ChronoField) {
            if (field == EPOCH_DAY) {
                return toEpochDay();
            }
            if (field == PROLEPTIC_MONTH) {
                return getProlepticMonth();
            }
            return get0(field);
        }
        return field.getFrom(this);
    }

    @Override
    public int get(TemporalField field) {
        if (field instanceof ChronoField) {
            return get0(field);
        }
        return ChronoLocalDate.super.get(field);
    }

    private int get0(TemporalField field) {
        return switch ((ChronoField) field) {
            case DAY_OF_WEEK -> getDayOfWeek().getValue();
            case ALIGNED_DAY_OF_WEEK_IN_MONTH -> ((day - 1) % 7) + 1;
            case ALIGNED_DAY_OF_WEEK_IN_YEAR -> ((getDayOfYear() - 1) % 7) + 1;
            case DAY_OF_MONTH -> day;
            case DAY_OF_YEAR -> getDayOfYear();
            case EPOCH_DAY -> throw new UnsupportedTemporalTypeException(
                    "Invalid field 'EpochDay' for get() method, use getLong() instead");
            case ALIGNED_WEEK_OF_MONTH -> ((day - 1) / 7) + 1;
            case ALIGNED_WEEK_OF_YEAR -> ((getDayOfYear() - 1) / 7) + 1;
            case MONTH_OF_YEAR -> month;
            case PROLEPTIC_MONTH -> throw new UnsupportedTemporalTypeException(
                    "Invalid field 'ProlepticMonth' for get() method, use getLong() instead");
            case YEAR -> year;
            case ERA, YEAR_OF_ERA -> throw new IranianChronology.EraNotSupportedException();
            default -> throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        };
    }

    public DayOfWeek getDayOfWeek() {
        int dow0 = Math.floorMod(toEpochDay() + 3, 7);
        return DayOfWeek.of(dow0 + 1);
    }

    public int getDayOfYear() {
        int doy = 0;
        int m = 1;
        while (m < month) {
            if (m <= 6)
                doy += 31;
            else if (m <= 11)
                doy += 30;
            m++;
        }
        doy += day;
        return doy;
    }

    private long getProlepticMonth() {
        return (year * 12L + month - 1);
    }

    @Override
    public Era getEra() {
        throw new IranianChronology.EraNotSupportedException();
    }

    @Override
    public long toEpochDay() {
        if (year > 6348) {  // after 6348
            long days = 79L;
            int y = 6349;
            while (y != year) {
                days += IranianChronology.INSTANCE.isLeapYear(y) ? 366 : 365;
                y++;
            }
            days += getDayOfYear() - 1;
            return days;

        } else if (year < 6348) {  // before 6348
            long days = -286L;
            int y = 6347;
            while (y != year) {
                days -= IranianChronology.INSTANCE.isLeapYear(y) ? 366 : 365;
                y--;
            }
            days -= (IranianChronology.INSTANCE.isLeapYear(y) ? 366 : 365) - (getDayOfYear() - 1);
            return days;

        } else {  // during 6348 (1969-1970)
            return getDayOfYear() - 287L;
        }
    }

    @Override
    public boolean isSupported(TemporalField field) {
        if (field instanceof ChronoField) {
            if (field == ERA || field == YEAR_OF_ERA)
                return false;
            return field.isDateBased();
        }
        return field != null && field.isSupportedBy(this);
    }

    @Override
    public boolean isSupported(TemporalUnit unit) {
        if (unit instanceof ChronoUnit) {
            if (unit == ChronoUnit.ERAS)
                return false;
            return unit.isDateBased();
        }
        return unit != null && unit.isSupportedBy(this);
    }

    @Override
    public ValueRange range(TemporalField field) {
        if (field instanceof ChronoField) {
            if (isSupported(field)) {
                return IranianChronology.INSTANCE.range((ChronoField) field);
            }
            throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
        }
        Objects.requireNonNull(field, "field");
        return field.rangeRefinedBy(this);
    }


    //-------------------------COMPARISON------------------------------------

    @Override
    public int compareTo(ChronoLocalDate other) {
        if (other instanceof IranianDate) {
            return compareTo0((IranianDate) other);
        }
        return ChronoLocalDate.super.compareTo(other);
    }

    int compareTo0(IranianDate otherDate) {
        int cmp = (year - otherDate.year);
        if (cmp == 0) {
            cmp = (month - otherDate.month);
            if (cmp == 0) {
                cmp = (day - otherDate.day);
            }
        }
        return cmp;
    }

    @Override
    public boolean isAfter(ChronoLocalDate other) {
        if (other instanceof IranianDate) {
            return compareTo0((IranianDate) other) > 0;
        }
        return ChronoLocalDate.super.isAfter(other);
    }

    @Override
    public boolean isBefore(ChronoLocalDate other) {
        if (other instanceof IranianDate) {
            return compareTo0((IranianDate) other) < 0;
        }
        return ChronoLocalDate.super.isBefore(other);
    }

    @Override
    public boolean isEqual(ChronoLocalDate other) {
        if (other instanceof IranianDate) {
            return compareTo0((IranianDate) other) == 0;
        }
        return ChronoLocalDate.super.isEqual(other);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof IranianDate) {
            return compareTo0((IranianDate) obj) == 0;
        }
        return false;
    }


    //-------------------------PROXIMITY------------------------------------

    @Override
    public ChronoPeriod until(ChronoLocalDate other) {
        IranianDate end = IranianChronology.INSTANCE.date(other);
        int
                y = other.get(YEAR) - this.get(YEAR),
                m = other.get(MONTH_OF_YEAR) - this.get(MONTH_OF_YEAR),
                d = other.get(DAY_OF_MONTH) - this.get(DAY_OF_MONTH);

        if (isBefore(other)) {
            IranianDate prevMonth = (IranianDate) other.minus(1, MONTHS);
            if (d < 0) {
                d += prevMonth.lengthOfMonth();
                m--;
            }
            if (m < 0) {
                m += 12;
                y--;
            }
        } else {
            if (d > 0) {
                d = other.lengthOfMonth() - d;
                m++;
            } else
                d = Math.abs(d);
            if (m > 0) {
                m = 12 - m;
                y++;
            } else
                m = Math.abs(m);
            y = Math.abs(y);
        }
        return IranianChronology.INSTANCE.period(y, m, d);
    }

    @Override
    public long until(Temporal endExclusive, TemporalUnit unit) {
        IranianDate end = IranianChronology.INSTANCE.date(endExclusive);
        if (unit instanceof ChronoUnit chronoUnit) {
            return switch (chronoUnit) {
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

    long daysUntil(IranianDate end) {
        return end.toEpochDay() - toEpochDay();  // no overflow
    }

    private long monthsUntil(IranianDate end) {
        long packed1 = getProlepticMonth() * 32L + getLong(DAY_OF_MONTH);  // no overflow
        long packed2 = end.getProlepticMonth() * 32L + end.getLong(DAY_OF_MONTH);  // no overflow
        return (packed2 - packed1) / 32;
    }


    //-------------------------MUTATION-------------------------------------

    @Override
    public IranianDate with(TemporalAdjuster adjuster) {
        if (adjuster instanceof IranianDate) {
            return (IranianDate) adjuster;
        }
        return (IranianDate) adjuster.adjustInto(this);
    }

    @Override
    public IranianDate with(TemporalField field, long newValue) {
        if (field instanceof ChronoField chronoField) {
            chronoField.checkValidValue(newValue);
            return switch (chronoField) {
                case DAY_OF_WEEK -> plusDays(
                        newValue - getDayOfWeek().getValue());
                case ALIGNED_DAY_OF_WEEK_IN_MONTH -> plusDays(
                        newValue - getLong(ALIGNED_DAY_OF_WEEK_IN_MONTH));
                case ALIGNED_DAY_OF_WEEK_IN_YEAR -> plusDays(
                        newValue - getLong(ALIGNED_DAY_OF_WEEK_IN_YEAR));
                case DAY_OF_MONTH -> withDayOfMonth((int) newValue);
                case DAY_OF_YEAR -> withDayOfYear((int) newValue);
                case EPOCH_DAY -> IranianDate.ofEpochDay(newValue);
                case ALIGNED_WEEK_OF_MONTH -> plusWeeks(
                        newValue - getLong(ALIGNED_WEEK_OF_MONTH));
                case ALIGNED_WEEK_OF_YEAR -> plusWeeks(
                        newValue - getLong(ALIGNED_WEEK_OF_YEAR));
                case MONTH_OF_YEAR -> withMonth((int) newValue);
                case PROLEPTIC_MONTH -> plusMonths(
                        newValue - getProlepticMonth());
                case YEAR -> withYear((int) newValue);
                default -> throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
            };
        }
        return field.adjustInto(this, newValue);
    }

    public IranianDate withYear(int year) {
        if (this.year == year) {
            return this;
        }
        YEAR.checkValidValue(year);
        return resolvePreviousValid(year, month, day);
    }

    public IranianDate withMonth(int month) {
        if (this.month == month) {
            return this;
        }
        MONTH_OF_YEAR.checkValidValue(month);
        return resolvePreviousValid(year, month, day);
    }

    public IranianDate withDayOfMonth(int dayOfMonth) {
        if (this.day == dayOfMonth) {
            return this;
        }
        return of(year, month, dayOfMonth);
    }

    public IranianDate withDayOfYear(int dayOfYear) {
        if (this.getDayOfYear() == dayOfYear) {
            return this;
        }
        return ofYearDay(year, dayOfYear);
    }

    @Override
    public IranianDate plus(TemporalAmount amount) {
        IranianDate altered = (IranianDate) clone();
        for (TemporalUnit unit : amount.getUnits())
            altered = plus(amount.get(unit), unit);
        return altered;
    }

    @Override
    public IranianDate plus(long amountToAdd, TemporalUnit unit) {
        if (unit instanceof ChronoUnit chronoUnit) {
            return switch (chronoUnit) {
                case DAYS -> plusDays(amountToAdd);
                case WEEKS -> plusWeeks(amountToAdd);
                case MONTHS -> plusMonths(amountToAdd);
                case YEARS -> plusYears(amountToAdd);
                case DECADES -> plusYears(Math.multiplyExact(amountToAdd, 10));
                case CENTURIES -> plusYears(Math.multiplyExact(amountToAdd, 100));
                case MILLENNIA -> plusYears(Math.multiplyExact(amountToAdd, 1000));
                default -> throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
            };
        }
        return unit.addTo(this, amountToAdd);
    }

    public IranianDate plusYears(long yearsToAdd) {
        if (yearsToAdd == 0) {
            return this;
        }
        int newYear = YEAR.checkValidIntValue(year + yearsToAdd);  // safe overflow
        return resolvePreviousValid(newYear, month, day);
    }

    public IranianDate plusMonths(long monthsToAdd) {
        if (monthsToAdd == 0) {
            return this;
        }
        long monthCount = year * 12L + (month - 1);
        long calcMonths = monthCount + monthsToAdd;  // safe overflow
        int newYear = YEAR.checkValidIntValue(Math.floorDiv(calcMonths, 12));
        int newMonth = Math.floorMod(calcMonths, 12) + 1;
        return resolvePreviousValid(newYear, newMonth, day);
    }

    public IranianDate plusWeeks(long weeksToAdd) {
        return plusDays(Math.multiplyExact(weeksToAdd, 7));
    }

    public IranianDate plusDays(long daysToAdd) {
        return IranianDate.ofEpochDay(toEpochDay() + daysToAdd);
    }

    @Override
    public IranianDate minus(TemporalAmount amount) {
        IranianDate altered = (IranianDate) clone();
        for (TemporalUnit unit : amount.getUnits())
            altered = minus(amount.get(unit), unit);
        return altered;
    }

    @Override
    public IranianDate minus(long amountToSubtract, TemporalUnit unit) {
        return (amountToSubtract == Long.MIN_VALUE
                ? plus(Long.MAX_VALUE, unit).plus(1, unit)
                : plus(-amountToSubtract, unit));
    }

    public IranianDate minusYears(long yearsToSubtract) {
        return (yearsToSubtract == Long.MIN_VALUE
                ? plusYears(Long.MAX_VALUE).plusYears(1)
                : plusYears(-yearsToSubtract));
    }

    public IranianDate minusMonths(long monthsToSubtract) {
        return (monthsToSubtract == Long.MIN_VALUE
                ? plusMonths(Long.MAX_VALUE).plusMonths(1)
                : plusMonths(-monthsToSubtract));
    }

    public IranianDate minusWeeks(long weeksToSubtract) {
        return (weeksToSubtract == Long.MIN_VALUE
                ? plusWeeks(Long.MAX_VALUE).plusWeeks(1)
                : plusWeeks(-weeksToSubtract));
    }

    public IranianDate minusDays(long daysToSubtract) {
        return (daysToSubtract == Long.MIN_VALUE
                ? plusDays(Long.MAX_VALUE).plusDays(1)
                : plusDays(-daysToSubtract));
    }

    private static IranianDate resolvePreviousValid(
            int year, int month, int day) {
        switch (month) {
            case 12 -> day = Math.min(day, IranianChronology.INSTANCE.isLeapYear(year) ? 30 : 29);
            case 7, 8, 9, 10, 11 -> day = Math.min(day, 30);
        }
        return new IranianDate(year, month, day);
    }


    //-------------------------PRINTING--------------------------------------

    @Override
    public String toString() {
        var buf = new StringBuilder(10);
        formatTo(buf);
        return buf.toString();
    }

    void formatTo(StringBuilder buf) {
        int yearValue = year;
        int monthValue = month;
        int dayValue = day;
        int absYear = Math.abs(yearValue);
        if (absYear < 1000) {
            if (yearValue < 0) {
                buf.append('-');
            }
            buf.repeat('0', absYear < 10 ? 3 : absYear < 100 ? 2 : 1);
            buf.append(absYear);
        } else {
            if (yearValue > 9999) {
                buf.append('+');
            }
            buf.append(yearValue);
        }
        buf.append(monthValue < 10 ? "-0" : "-")
                .append(monthValue)
                .append(dayValue < 10 ? "-0" : "-")
                .append(dayValue);
    }


    //-------------------------COMPRESSION-----------------------------------

    @Override
    public int hashCode() {
        return (year & 0xFFFFF800) ^ ((year << 11) + (month << 6) + day);
    }

    @java.io.Serial
    private Object writeReplace() {
        return new Ser(this);
    }

    /**
     * Defend against malicious streams.
     */
    @java.io.Serial
    private void readObject(ObjectInputStream s) throws InvalidObjectException {
        throw new InvalidObjectException("Deserialization via serialization delegate");
    }

    void writeExternal(DataOutput out) throws IOException {
        out.writeInt(year);
        out.writeByte(month);
        out.writeByte(day);
    }

    static IranianDate readExternal(DataInput in) throws IOException {
        int year = in.readInt();
        int month = in.readByte();
        int dayOfMonth = in.readByte();
        return IranianDate.of(year, month, dayOfMonth);
    }

    static final class Ser implements Externalizable {

        private IranianDate obj;

        public Ser() {
        }

        Ser(IranianDate object) {
            this.obj = object;
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            obj.writeExternal(out);
        }

        @SuppressWarnings("RedundantThrows")
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            obj = IranianDate.readExternal(in);
        }
    }
}
