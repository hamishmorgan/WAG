package uk.ac.susx.tag.wag;

import com.google.common.base.CharMatcher;

/**
 * An <tt>AliasHandler</tt> which cleans and simplifies the given alias by replacing all white-space tokens and
 * collapsing consecutive sequences of white-space with a single space. In addition leading, and trailing space is
 * dropped.
 */
public class SpaceTrimmingAliasHandlerAdapter extends ForwardingAliasHandler {

    public SpaceTrimmingAliasHandlerAdapter(AliasHandler delegate) {
        super(delegate);
    }

    @Override
    public void handle(Alias alias) {
        super.handle(new Alias(
                alias.getType(),
                alias.getSubType(),
                strip(alias.getSource()),
                strip(alias.getTarget())));
    }

    private static String strip(String str) {
        return CharMatcher.WHITESPACE.trimAndCollapseFrom(str, ' ').trim();
    }
}
