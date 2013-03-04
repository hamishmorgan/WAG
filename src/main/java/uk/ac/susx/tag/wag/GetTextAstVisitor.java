package uk.ac.susx.tag.wag;

import com.google.common.base.Optional;
import de.fau.cs.osr.ptk.common.AstVisitor;
import de.fau.cs.osr.ptk.common.ast.AstNode;
import de.fau.cs.osr.ptk.common.ast.Text;
import org.sweble.wikitext.engine.utils.EntityReferences;
import org.sweble.wikitext.lazy.encval.IllegalCodePoint;
import org.sweble.wikitext.lazy.parser.ImageLink;
import org.sweble.wikitext.lazy.parser.MagicWord;
import org.sweble.wikitext.lazy.parser.Url;
import org.sweble.wikitext.lazy.parser.Whitespace;
import org.sweble.wikitext.lazy.preprocessor.TagExtension;
import org.sweble.wikitext.lazy.preprocessor.Template;
import org.sweble.wikitext.lazy.preprocessor.XmlComment;
import org.sweble.wikitext.lazy.utils.XmlCharRef;
import org.sweble.wikitext.lazy.utils.XmlEntityRef;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;
import static uk.ac.susx.tag.wag.AstUtils.getText;

/**
 * GetTextAstVisitor is visitor over an abstract syntax tree, that find and concatenates all text within it.
 *
 * @author Hamish Morgan
 */
@Nonnull
@NotThreadSafe
public final class GetTextAstVisitor extends AstVisitor {

    private static final Logger LOG = Logger.getLogger(GetTextAstVisitor.class.getName());
    public static final String WHITESPACE_REPLACEMENT = " ";
    private Optional<StringBuilder> builder;

    public GetTextAstVisitor() {
        builder = Optional.absent();
    }

    @Override
    protected boolean before(final AstNode node) {
        builder = Optional.of(new StringBuilder());
        return super.before(node);
    }

    @Override
    protected Object after(final AstNode node, final Object result) {
        final String text = builder.get().toString().trim();
        builder = Optional.absent();
        return text;
    }

    public void visit(final AstNode node) {
        iterate(node);
    }

    public void visit(final Template template) {
        final String templateName = AstUtils.getText(template.getName());
        // Handle the special template escapes for pipe and equals signs
        if (templateName.equals("!")) {
            // See http://en.wikipedia.org/wiki/Template:!
            builder.get().append('|');
        } else if (templateName.equals("=")) {
            // See http://en.wikipedia.org/wiki/Template:=
            builder.get().append('=');
        } else if (templateName.equals(":")) {
            // See http://en.wikipedia.org/wiki/Template:Colon
            builder.get().append(':');
        } else if (templateName.equals(";")) {
            // See http://en.wikipedia.org/wiki/Template:;
            builder.get().append(';');
        } else if (templateName.equalsIgnoreCase("Null")) {
            // See http://en.wikipedia.org/wiki/Template:Null
            // Don't appent anything
        } else if (templateName.equalsIgnoreCase("Spaces")) {
            // See http://en.wikipedia.org/wiki/Template:Spaces

            int nSpaces = 1;
            if (!template.isEmpty()) {
                try {

                    final String argTet = AstUtils.getText(template.getArgs().get(0));
                    nSpaces = Integer.valueOf(argTet);

                } catch (Throwable throwable) {
                    LOG.log(Level.WARNING, "Ill-formed template \"{0}\": {1}",
                            new Object[]{getText(template.getName()), throwable});
                }
            }
            for (int i = 0; i < nSpaces; i++)
                builder.get().append(' ');

        } else {
            iterate(template);
        }
    }

    public void visit(final Text text) {
        builder.get().append(checkNotNull(text.getContent(), "Text node contents"));
    }

    public void visit(final Whitespace whitespace) {
        builder.get().append(WHITESPACE_REPLACEMENT);
    }


    public void visit(final XmlCharRef cr) {
        builder.get().appendCodePoint(cr.getCodePoint());
    }

    public void visit(final XmlEntityRef er) {
        @Nullable final String ch = EntityReferences.resolve(er.getName());
        if (ch == null) {
            LOG.log(Level.WARNING, "Unknown XML Entity: {0}", er.getName());
            builder.get().append("&");
            builder.get().append(er.getName());
            builder.get().append(";");
        } else {
            builder.get().append(ch);
        }
    }

    public void visit(final Url url) {
        builder.get().append(url.getProtocol());
        builder.get().append(":");
        builder.get().append(url.getPath());
    }


    // Stuff to hide

    public void visit(ImageLink n) {
    }

    public void visit(IllegalCodePoint n) {
    }

    public void visit(XmlComment n) {
    }


    public void visit(TagExtension n) {
    }

    public void visit(MagicWord n) {
    }


}
