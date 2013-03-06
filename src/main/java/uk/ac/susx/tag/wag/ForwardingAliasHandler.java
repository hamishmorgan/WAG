package uk.ac.susx.tag.wag;

import com.google.common.base.Preconditions;

/**
 * An abstract <tt>AliasHandler</tt> which simply forwards all calls to a given delegate.
 * <p/>
 * This class should be extended, overriding the {@link #handle(Alias)} method to do something useful.
 */
public abstract class ForwardingAliasHandler implements AliasHandler {

    private final AliasHandler delegate;

    protected ForwardingAliasHandler(final AliasHandler delegate) {
        this.delegate = Preconditions.checkNotNull(delegate, "delegate");
    }

    public final AliasHandler getDelegate() {
        return delegate;
    }

    @Override
    public void handle(Alias alias) {
        delegate.handle(alias);
    }
}
