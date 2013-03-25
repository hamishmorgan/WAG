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

import com.google.common.base.CharMatcher;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An <tt>AliasHandler</tt> which cleans and simplifies the given alias by replacing all white-space tokens and
 * collapsing consecutive sequences of white-space with a single space. In addition leading, and trailing space is
 * dropped. If, after this trimming processes, either the source or target string is empty, then no alias is
 * forwarded to the delegate.
 *
 * @author Hamish Morgan &lt;hamish.morgan@sussex.ac.uk&gt;
 */
public class SpaceTrimmingAliasHandlerAdapter extends ForwardingAliasHandler {

    public SpaceTrimmingAliasHandlerAdapter(AliasHandler delegate) {
        super(delegate);
    }

    @Override
    public void handle(Alias alias) throws NullPointerException {
        checkNotNull(alias, "alias");

        final String sourceStripped = strip(alias.getSource());
        if (sourceStripped.isEmpty())
            return;

        final String targetStripped = strip(alias.getSource());
        if (targetStripped.isEmpty())
            return;

        super.handle(new Alias(
                alias.getType(),
                alias.getSubType(),
                sourceStripped,
                targetStripped));
    }

    private static String strip(CharSequence str) {
        return CharMatcher.WHITESPACE.trimAndCollapseFrom(str, ' ').trim();
    }
}
