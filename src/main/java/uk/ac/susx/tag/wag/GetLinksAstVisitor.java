package uk.ac.susx.tag.wag;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import de.fau.cs.osr.ptk.common.AstVisitor;
import de.fau.cs.osr.ptk.common.ast.AstNode;
import org.sweble.wikitext.lazy.parser.ExternalLink;
import org.sweble.wikitext.lazy.parser.InternalLink;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.logging.Logger;

/**
* Created with IntelliJ IDEA.
* User: hiam20
* Date: 20/02/2013
* Time: 13:17
* To change this template use File | Settings | File Templates.
*/
@Nonnull
@NotThreadSafe
public final class GetLinksAstVisitor extends AstVisitor {

    private static final Logger LOG = Logger.getLogger(GetTextAstVisitor.class.getName());
    public static final String WHITESPACE_REPLACEMENT = " ";
    private Optional<ImmutableList.Builder<AstNode>> builder;

    public GetLinksAstVisitor() {
        builder = Optional.absent();
    }


    @Override
    protected boolean before(final AstNode node) {
        builder = Optional.of(ImmutableList.<AstNode>builder());
        return super.before(node);
    }

    @Override
    protected Object after(final AstNode node, final Object result) {
        final ImmutableList<AstNode> links = builder.get().build();
        builder = Optional.absent();
        return links;
    }

    public void visit(final AstNode node) {
        iterate(node);
    }


    public void visit(final ExternalLink link) {
        builder.get().add(link);
    }


    public void visit(final InternalLink link) {
        builder.get().add(link);
    }
}
