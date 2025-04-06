package ir.mahdiparastesh.chrono;

import java.time.chrono.AbstractChronology;
import java.time.chrono.Era;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;
import java.util.List;

public class IranianChronology extends AbstractChronology {
    // TODO implements Serializable

    public static final IranianChronology INSTANCE = new IranianChronology();

    @Override
    public String getId() {
        return "Iranian";
    }

    @Override
    public String getCalendarType() {
        return "iranian";
    }

    @Override
    public IranianDate date(int prolepticYear, int month, int dayOfMonth) {
        return IranianDate.of(prolepticYear, month, dayOfMonth);
    }

    @Override
    public IranianDate dateYearDay(int prolepticYear, int dayOfYear) {
        return IranianDate.ofYearDay(prolepticYear, dayOfYear);
    }

    @Override
    public IranianDate dateEpochDay(long epochDay) {
        return IranianDate.ofEpochDay(epochDay);
    }

    @Override
    public IranianDate date(TemporalAccessor temporal) {
        return dateEpochDay(temporal.getLong(ChronoField.EPOCH_DAY));
    }

    @Override
    public boolean isLeapYear(long prolepticYear) {
        while (prolepticYear < 0) prolepticYear += 2820L;
        long periodicYear = (prolepticYear - 5474L) % 2820L;
        long test = ((periodicYear + 38L) * 682L) % 2816L;
        return test < 682;
    }

    @Override
    public int prolepticYear(Era era, int yearOfEra) {
        return yearOfEra;
    }

    @Override
    public Era eraOf(int eraValue) {
        return null;
    }

    @Override
    public List<Era> eras() {
        return null;
    }

    @Override
    public ValueRange range(ChronoField field) {
        return switch (field) {
            case ChronoField.DAY_OF_MONTH -> ValueRange.of(1, 29, 31);
            case ChronoField.ALIGNED_WEEK_OF_MONTH -> ValueRange.of(1, 5);
            default -> field.range();
        };
    }

    public static class EraNotSupportedException
            extends UnsupportedTemporalTypeException {
        private static final String msg = "Eras are not supported.";

        public EraNotSupportedException() {
            super(msg);
        }
    }
}
