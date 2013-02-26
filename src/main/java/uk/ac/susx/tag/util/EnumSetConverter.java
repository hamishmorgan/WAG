package uk.ac.susx.tag.util;

import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.BaseConverter;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * EnumConverter is a JCommander class for parsing command line argument strings into an EnumSet of
 * some element type.
 * <p/>
 * Sadly JCommander is only parameterizable with a class that implements IStringConverter, rather
 * than an instance object, which means you have to subclass this convert for every single possible
 * element type (i.e every enum you want to use.)
 * <p/>
 * Matching is case-insensitive.
 *
 * @param <E> element type
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
@Nonnull
@Immutable
public class EnumSetConverter<E extends Enum<E>>
        extends BaseConverter<EnumSet<E>> {

    private static final String DEFAULT_OPTION_NAME = "";
    private static final Pattern DELIMITER = Pattern.compile(",");
    private final Class<E> elementType;

    protected EnumSetConverter(final Class<E> elementType, final String optionName) {
        super(optionName);
        this.elementType = checkNotNull(elementType);
    }

    protected EnumSetConverter(final Class<E> elementType) {
        super(DEFAULT_OPTION_NAME);
        this.elementType = checkNotNull(elementType);
    }

    public final Class<E> getElementType() {
        return elementType;
    }

    @Override
    public EnumSet<E> convert(final String stringValue) {
        checkNotNull(stringValue);
        final EnumSet<E> resultSet = EnumSet.noneOf(elementType);
        for (String name : DELIMITER.split(stringValue)) {
            try {
                name = name.trim().toLowerCase();
                final E value = Enum.valueOf(elementType, name);
                resultSet.add(value);
            } catch (IllegalArgumentException ex) {
                throw new ParameterException(MessageFormat.format(
                        "Failed to convert option {0} from \"{1}\" to EnumSet<{2}>: "
                                + "Found unknown name \"{3}\", but expecting one of {4}",
                        new Object[]{getOptionName(), stringValue, elementType.getSimpleName(),
                                name, EnumSet.allOf(elementType)}));
            }
        }
        return resultSet;
    }

    @Override
    public String toString() {
        return "EnumSetConverter{elementType=" + elementType + '}';
    }
}
