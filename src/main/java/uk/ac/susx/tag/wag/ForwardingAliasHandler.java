/*
 * Copyright (c) 2012-2013, University of Sussex
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  * Neither the name of the University of Sussex nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package uk.ac.susx.tag.wag;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An abstract <tt>AliasHandler</tt> which simply forwards all calls to a given delegate.
 * <p/>
 * This class should be extended, overriding the {@link #handle(Alias)} method to do something useful.
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public abstract class ForwardingAliasHandler implements AliasHandler, Flushable, Closeable {

    /**
     * Inner <tt>AliasHandler</tt> to which method invocations will be forwarded.
     */
    private final AliasHandler delegate;

    /**
     * Protected constructor to be used by subclasses only.
     *
     * @param delegate inner <tt>AliasHandler</tt> to which method invocations will be forwarded.
     * @throws NullPointerException if delegate is null
     */
    protected ForwardingAliasHandler(final AliasHandler delegate) throws NullPointerException {
        this.delegate = checkNotNull(delegate, "delegate");
    }

    /**
     * Get the inner <tt>AliasHandler</tt> to which method invocations will be forwarded.
     *
     * @return inner <tt>AliasHandler</tt> to which method invocations will be forwarded.
     */
    public final AliasHandler getDelegate() {
        return delegate;
    }

    @Override
    public void handle(Alias alias) {
        delegate.handle(checkNotNull(alias, "alias"));
    }

    @Override
    public void close() throws IOException {
        if(delegate instanceof Closeable)
            ((Closeable)delegate).close();
    }

    @Override
    public void flush() throws IOException {
        if(delegate instanceof Flushable)
            ((Flushable)delegate).flush();
    }
}
