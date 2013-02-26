package uk.ac.susx.tag.wag;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 *
 */
@Immutable
@Nonnull
public final class Alias {

    public static final String NO_SUBTYPE = "";
    private final AliasType type;
    private final String subType;
    private final String target;
    private final String source;

    public Alias(AliasType type, String subType, String source, String target) {
        this.type = checkNotNull(type, "type");
        this.subType = checkNotNull(subType, "subType");
        this.target = checkNotNull(target, "target");
        this.source = checkNotNull(source, "source");
    }


    public String getTarget() {
        return target;
    }

    public String getSource() {
        return source;
    }

    public AliasType getType() {
        return type;
    }

    public String getSubType() {
        return subType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Alias synonym = (Alias) o;

        if (!source.equals(synonym.source)) return false;
        if (!target.equals(synonym.target)) return false;
        if (type != synonym.type) return false;
        if (!subType.equals(synonym.subType)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + target.hashCode();
        result = 31 * result + source.hashCode();
        result = 31 * result + subType.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return getType() + (subType.isEmpty() ? "" : "/" + subType) +
                "[" + getSource() + " => " + getTarget() + "]";
    }
}
