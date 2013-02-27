package uk.ac.susx.tag.wag;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import de.fau.cs.osr.ptk.common.AstPrinter;
import de.fau.cs.osr.ptk.common.ast.AstNode;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * AstUtils is a static utility class containing methods for manipulating abstract syntax tree (AST) related data.
 * <p/>
 * Actually it is largely concerned with Wikipedia related stuff, so I should probably split some of this class into
 * a WikiUtils class at some point.
 *
 * @author Hamish Morgan
 */
public class AstUtils {

    /**
     * Suffix sometimes (not always) used in Wikipedia to denote a disambiguation page.
     */
    public static final String DISAMBIGUATION_SUFFIX = " (disambiguation)";

    /**
     * Character used to denote namespace prefixes in Wikipedia article titles.
     */
    public static final char NAMESPACE_DELIMITER = ':';

    /**
     * Private constructor (Static utility class should not be instantiated.)
     *
     * @throws UnsupportedOperationException always
     */
    private AstUtils() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Static utility class should not be instantiated.");
    }

    /**
     * Get all the text with the given {@code node} tree, returning it as a string.
     *
     * @param node Abstract syntax tree to iterate and decent over
     * @return Concatenated contents of all text within the {@code node}.
     */
    public static ImmutableList<AstNode> getLinks(final AstNode node) {
        try {
            return (ImmutableList<AstNode>) (new GetLinksAstVisitor().go(node));
        } catch (ClassCastException e) {
            throw new AssertionError("Casting to ImmutableList<AstNode> should never fail: " + e);
        }
    }


    /**
     * Get all the text with the given {@code node} tree, returning it as a string.
     *
     * @param node Abstract syntax tree to iterate and decent over
     * @return Concatenated contents of all text within the {@code node}.
     */
    public static String getText(final AstNode node) {
        try {
            return (String) (new GetTextAstVisitor().go(node));
        } catch (ClassCastException e) {
            throw new AssertionError("Casting to String should never fail: " + e);
        }
    }


    /**
     * Print the entire abstract syntax tree rooted at the given <tt>node</tt>, on <tt>stdout</tt>.
     *
     * @param node tree to print
     * @throws NullPointerException if node is null
     */
    public static void print(AstNode node) throws NullPointerException {
        checkNotNull(node, "node");
        new AstPrinter(new PrintWriter(System.out) {
            @Override
            public void close() {
                super.flush();
            }
        }).go(node);
    }

    /**
     * Get the entire abstract syntax tree rooted at <tt>node</tt> as a human readable string.
     *
     * @param node tree to represent
     * @return human readable string representation
     * @throws NullPointerException if node is null
     */
    public static String toString(AstNode node) throws NullPointerException {
        checkNotNull(node, "node");
        final StringWriter writer = new StringWriter();
        new AstPrinter(writer).go(node);
        return writer.toString();
    }


    /**
     * Get the surface form from the text of the given node, which is assumed to be a link (although not necessarily
     * parsed as such.)
     *
     * @param node
     * @return
     */
    public static String getLinkSurface(AstNode node) {
        final String text = getText(node);
        final int i = text.lastIndexOf('|');
        return i == -1 ? text : text.substring(i + 1);
    }


    /**
     * Strips the given suffix from the string if it is present, otherwise the string is returned unaltered.
     * <p/>
     * Note that string comparison is case insensitive.
     *
     * @param string string to search
     * @param suffix ending to remove
     * @return string with suffix removed (if present)
     */
    public static final String stripSuffixIfPresent(String string, String suffix) {
        checkNotNull(string, "string");
        checkNotNull(suffix, "suffix");

        if (string.isEmpty() || suffix.isEmpty() || string.length() < suffix.length())
            return string;
        if (string.substring(string.length() - suffix.length()).equalsIgnoreCase(suffix)) {
            return string.substring(0, string.length() - suffix.length());
        } else {
            return string;
        }
    }

    /**
     * Get whether or not the given string contains a WikiText title disambiguation suffix. Suffixes can either be
     * denoted by being contained in round brackets, or by a comma.
     *
     * @param title WikiText page title to check
     * @return true if the title contains a disambiguation suffix, false otherwise
     */
    public static final boolean containsWikiTitleSuffix(String title) {
        return title.contains(",") || (title.contains("(") && title.contains(")"));
    }

    /**
     * Strips the last disambiguation suffix (if present) from the given page title. If no suffix is found then the
     * title is returned unaltered.
     *
     * @param title Wiki page title to strip a suffix from
     * @return the title with the last disambiguation suffix stripped, if present.
     */
    public static final String stripWikiTitleSuffix(String title) {
        int i;
        if (-1 != (i = title.lastIndexOf('(')))
            return title.substring(0, i).trim();
        if (-1 != (i = title.lastIndexOf(',')))
            return title.substring(0, i).trim();
        return title;
    }

    /**
     * Get a set of title variants; computed by iteratively removing the last disambiguation suffix from the title
     * until
     * no further suffixes remain.
     * <p/>
     * The title itself is <em>not</em> included in the result set. If the title does not contain any disambiguation
     * suffixes then the empty set it returned.
     *
     * @param title Wiki page title
     * @return set of all title variants
     */
    public static final Set<String> wikiTitleVarients(String title) {
        final Set<String> perms = Sets.newHashSet();
        while (containsWikiTitleSuffix(title)) {
            title = stripWikiTitleSuffix(title).trim();
            perms.add(title);
        }
        return perms;
    }


    /**
     * Get whether or not the given Wikipedia article title contains a namespace.
     *
     * @param pageTitle wikipedia article title check for a namespace
     * @return true if <tt>pageTitle</tt> contains a namespace, false otherwise
     * @throws NullPointerException if <tt>pageTitle</tt> is null
     */
    public static final boolean containsNamespace(final String pageTitle)
            throws NullPointerException {
        checkNotNull(pageTitle, "pageTitle");
        return -1 != pageTitle.indexOf(NAMESPACE_DELIMITER);
    }

    /**
     * Strip the first namespace prefixes from the given page title, returning the result. Namespaces are denoted by
     * ':' (colon) character; e.g <tt>wikt:Solidarity</tt> starts with the Wiktionary namespace. If the title does not
     * contain any namespaces then it will be returned unaltered.
     *
     * @param pageTitle wikipedia article title to strip a namespace from
     * @return title with the first namespace removed.
     * @throws NullPointerException if <tt>pageTitle</tt> is null
     */
    public static final String stripNamespace(final String pageTitle)
            throws NullPointerException {
        checkNotNull(pageTitle, "pageTitle");
        int i = pageTitle.indexOf(NAMESPACE_DELIMITER);
        return (i != -1)
                ? pageTitle.substring(i + 1)
                : pageTitle;
    }

    /**
     * Strip <em>all</em> namespace prefixes from the given page title, returning the result. Namespaces are denoted
     * by ':' (colon) character; e.g <tt>wikt:Solidarity</tt> starts with the Wiktionary namespace. If the title does
     * not contain any namespaces then it will be returned unaltered.
     *
     * @param pageTitle wikipedia article title to strip namespaces from
     * @return title with <em>all</em> namespace removed.
     * @throws NullPointerException if <tt>pageTitle</tt> is null
     */
    public static final String stripNamespaces(final String pageTitle)
            throws NullPointerException {
        checkNotNull(pageTitle, "pageTitle");
        int start = 0;
        {
            int i;
            while (-1 != (i = pageTitle.indexOf(NAMESPACE_DELIMITER, start)))
                start = i + 1;
        }
        return (start > 0)
                ? pageTitle.substring(start)
                : pageTitle;
    }


}
