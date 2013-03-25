package uk.ac.susx.tag.util;

import java.text.MessageFormat;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author hiam20
 * @since 26/02/2013 14:07
 */
public class EnumUtils {

    private EnumUtils() {
        throw new AssertionError();
    }

    /**
     * Determine if the enum class, given by {@code type}, can be treated as case-insensitive or not. An enum is
     * case case-insensitive when all values are distinct when comparing their names ignoring case.
     *
     * @param type enum class to check
     * @param <T>  enum type
     * @return true if all names are distinct when ignoring case, false otherwise
     * @throws IllegalArgumentException if the specified class object does not represent an enum type
     * @throws NullPointerException     if enumType is null
     */
    public static <T extends Enum<T>> boolean isCaseRequired(Class<T> type) {
        checkNotNull(type, "type");
        checkArgument(type.isEnum(), "specified class object {0} does not represent an enum type", type);

        final T[] aliases = type.getEnumConstants();
        for (int i = 1; i < aliases.length; i++)
            for (int j = 0; j < i; j++)
                if (aliases[i].name().equalsIgnoreCase(aliases[j].name()))
                    return false;
        return true;
    }

    /**
     * Returns the enum constant of the specified enum type with the specified name.  The name must match, ignoring
     * case, an identifier used to declare an enum constant in this type.  (Extraneous whitespace characters are not
     * permitted.)
     * <p/>
     * If the enum contains multiple values with names that differentiate only on case, then an arbitrary one of those
     * values will be returned. It is highly recommended that you first check the enum can be treated
     * case-insensitively  by calling {@link #isCaseRequired(Class)}.
     *
     * @param enumType the <tt>Class</tt> object of the enum type from which
     *                 to return a constant
     * @param name     the name of the constant to return
     * @param <T>
     * @return the enum constant of the specified enum type with the  specified name
     * @throws IllegalArgumentException if the specified enum type has no constant with the specified name, or the
     *                                  specified class object does not represent an enum type
     * @throws NullPointerException     if enumType or name is null
     */
    public static <T extends Enum<T>> T valueOfIgnoreCase(final Class<T> enumType, final String name)
            throws IllegalArgumentException {
        checkNotNull(enumType, "type");
        checkNotNull(name, "name");
        checkArgument(enumType.isEnum(), "specified class object {0} does not represent an enum type", enumType);

        try {
            return Enum.valueOf(enumType, name);
        } catch (IllegalArgumentException ex) {
            for (final T enumConst : enumType.getEnumConstants())
                if (enumConst.name().equalsIgnoreCase(name))
                    return enumConst;
        }
        throw new IllegalArgumentException(MessageFormat.format(
                "the specified enum type {0} has no constant with the specified name \"{1}\"", enumType, name));
    }

}
