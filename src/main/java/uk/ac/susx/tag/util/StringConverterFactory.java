package uk.ac.susx.tag.util;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.IStringConverterFactory;
import com.beust.jcommander.converters.BaseConverter;
import com.beust.jcommander.internal.Maps;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * @author Hamish I A Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class StringConverterFactory implements IStringConverterFactory {

    private final Map<Class<?>, Class<? extends IStringConverter<?>>> converts = Maps.newHashMap();

    private StringConverterFactory() {
    }

    public static IStringConverterFactory newInstance(final boolean loadDefaults) {
        StringConverterFactory factory = new StringConverterFactory();
        if (loadDefaults) {
            factory.register(Charset.class, CharsetStringConverter.class);
        }
        return factory;
    }

    @Override
    public <T> Class<? extends IStringConverter<T>> getConverter(Class<T> forType) {
        if (converts.containsKey(forType))
            return (Class<IStringConverter<T>>) converts.get(forType);
        return null;
    }

    public <T> void register(final Class<T> type,
                             final Class<? extends IStringConverter<? extends T>> converter) {
        converts.put(type, converter);
    }


    public static final class CharsetStringConverter extends BaseConverter<Charset> {


        public CharsetStringConverter(String optionName) {
            super(optionName);
        }

        @Override
        public Charset convert(String string) {
            return Charset.forName(string);
        }
    }
//
//
//    public <T extends Enum<T>> void registerEnumSet(final Class<T> type) {
//        final class ConcreteEnumSetConverted extends EnumSetConverter<T> {
//
//            public ConcreteEnumSetConverted() {
//                super(type);
//            }
//
//            public ConcreteEnumSetConverted(String optionName) {
//                super(type, optionName);
//            }
//        }
//
//        converts.put(type, ConcreteEnumSetConverted.class);
//    }
//
//


    //
//    public static class TimeUnitSetConverter extends EnumSetConverter<TimeUnit> {
//
//        public TimeUnitSetConverter() {
//            super(TimeUnit.class);
//        }
//
//        public TimeUnitSetConverter(final String optionName) {
//            super(TimeUnit.class, optionName);
//        }
//    }

}
