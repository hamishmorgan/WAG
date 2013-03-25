package uk.ac.susx.tag.util;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.*;

/**
 * DateTimeUtils is a static utility class contains methods for manipulating dates and times.
 *
 * @author Hamish Morgan
 */
public class DateTimeUtils {

    private DateTimeUtils() {
    }

    /**
     * Convert the given time to a human readable string representation.
     *
     * @param time time convert
     * @param unit
     * @return
     */
    public static String humanReadableTime(long time, TimeUnit unit) {
        final TimeUnit unit1 = largestNonzeroUnit(time, unit);

        final long value1 = time / unit.convert(1, unit1);
        final long timeRemainder = time % unit.convert(1, unit1);

        if (unit1 == NANOSECONDS || timeRemainder == 0) {
            return String.format("%d %s", value1, unit1.name().toLowerCase());
        } else {
            final TimeUnit unit2 = largestNonzeroUnit(timeRemainder, unit);
            final long value2 = timeRemainder / unit.convert(1, unit2);

            return String.format("%d %s %d %s",
                    value1, unit1.name().toLowerCase(),
                    value2, unit2.name().toLowerCase());
        }
    }

    private static final TimeUnit[] TIME_UNITS = new TimeUnit[]{DAYS, HOURS, MINUTES, SECONDS, MILLISECONDS, MICROSECONDS};

    private static TimeUnit largestNonzeroUnit(final long time, final TimeUnit unit) {
        for (final TimeUnit newUnit : TIME_UNITS)
            if (newUnit.convert(time, unit) > 0)
                return newUnit;
        return NANOSECONDS;
    }

}
