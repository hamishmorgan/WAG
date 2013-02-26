package uk.ac.susx.tag.wag;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;

/**
* Created with IntelliJ IDEA.
* User: hiam20
* Date: 26/02/2013
* Time: 11:44
* To change this template use File | Settings | File Templates.
*/
public class AliasTypeStringConverter implements IStringConverter<AliasType> {


    /**
     * Whether or not the p
     */
    private static final boolean caseInsenstive;

    static {
        boolean _caseInsenstive = true;

        final AliasType[] aliases = AliasType.values();

        search:
        for (int i = 1; i < aliases.length; i++)
            for (int j = 0; j < i; j++)
                if (aliases[i].name().equalsIgnoreCase(aliases[j].name())) {
                    _caseInsenstive = false;
                    break search;
                }
        caseInsenstive = _caseInsenstive;
    }

    @Override
    public AliasType convert(String value) {
        try {
            return AliasType.valueOf(value);
        } catch (IllegalArgumentException ex) {
            if (caseInsenstive) {
                for (AliasType type : AliasType.values())
                    if (type.name().equalsIgnoreCase(value))
                        return type;
            }
            throw new ParameterException(ex);
        }
    }
}
