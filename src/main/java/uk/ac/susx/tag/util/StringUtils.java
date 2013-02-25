package uk.ac.susx.tag.util;

import javax.annotation.Nonnull;

import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.concurrent.TimeUnit.*;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * Static utility class contain method for string manipulation.
 *
 * @author Hamish Morgan
 */
public class StringUtils {

    private StringUtils() {
        throw new AssertionError("Static utility class should not be instantiated.");
    }


    /**
     * Convert the given {@code string} such that the first character is lower-case. If the string is empty, or the
     * first character is already lower-case then the string is returned unaltered.
     *
     * @param string input string to convert
     * @return string converted so the first character is lower case.
     * @throws NullPointerException if {@code string} is null.
     */
    @Nonnull
    public static String firstCharToLowerCase(final String string) {
        checkNotNull(string, "string");

        if (string.isEmpty() || Character.isLowerCase(string.charAt(0)))
            return string;
        else if (string.length() == 1)
            return string.toLowerCase();
        else
            return "" + Character.toLowerCase(string.charAt(0))
                    + string.subSequence(1, string.length());
    }


    /**
     * Convert the given {@code string} such that the first character is upper-case. If the string is empty, or the
     * first character is already upper-case then the string is returned unaltered.
     *
     * @param string input string to convert
     * @return string converted so the first character is upper case.
     * @throws NullPointerException if {@code string} is null.
     */
    @Nonnull
    public static String firstCharToUpperCase(final String string) {
        checkNotNull(string, "string");

        if (string.isEmpty() || Character.isUpperCase(string.charAt(0)))
            return string;
        else if (string.length() == 1)
            return string.toUpperCase();
        else
            return "" + Character.toUpperCase(string.charAt(0))
                    + string.subSequence(1, string.length());
    }




}
