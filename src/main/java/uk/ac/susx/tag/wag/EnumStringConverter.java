package uk.ac.susx.tag.wag;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.converters.BaseConverter;
import uk.ac.susx.tag.util.EnumUtils;

import java.util.concurrent.TimeUnit;

/**
 * @author hiam20
 * @since 26/02/2013 11:44
 */
public abstract class EnumStringConverter<T extends Enum<T>>
        extends BaseConverter<T>
        implements IStringConverter<T> {


    /**
     * Whether or not the p
     */
    private final boolean caseInsenstive;

    private final Class<T> enumType;

    protected EnumStringConverter(String name, Class<T> enumType) {
        super(name);
        this.enumType = enumType;
        caseInsenstive = EnumUtils.isCaseRequired(enumType);
    }


    protected EnumStringConverter(Class<T> enumType) {
        super("[unnamed]");
        this.enumType = enumType;
        caseInsenstive = EnumUtils.isCaseRequired(enumType);
    }


    @Override
    public final T convert(String value) {
        return caseInsenstive
                ? EnumUtils.valueOfIgnoreCase(enumType, value)
                : Enum.valueOf(enumType, value);
    }


    public static  final class TimeUnitConverter extends EnumStringConverter {
        public TimeUnitConverter() {
            super(TimeUnit.class);
        }

        public TimeUnitConverter(String name) {
            super(name, TimeUnit.class);
        }
    }
}
