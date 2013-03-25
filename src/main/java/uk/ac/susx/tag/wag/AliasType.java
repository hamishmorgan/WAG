package uk.ac.susx.tag.wag;

import java.util.Collection;
import java.util.EnumSet;

/**
 * Enum containing the various types of alias that can be extracted from wikipedia.
 *
 * @author Hamish I A Morgan
 */
public enum AliasType {

    /*
     * ==================================
     *  Original Types
     * ==================================
     */

    /**
     * Exact match to the page title. Relation source and target will be the same.
     * <p/>
     * Note this will always be an identity alias so that option must be enabled or it will never be produced.
     */
    TITLE,

    /**
     * When a page contains the {@code {{lowercase title}} } template (e.g iPod, gzip) then the upper case title should
     * be denoted as an alias of the lower-case variant (since lowercase is less ambiguous.) I.e source is upper-case,
     * target is lower-case.
     */
    LOWERCASE_TITLE,

    /**
     * From wiki internal links (to other pages) we extract the link surface text as an {@link #LINK} alias for the
     * title of the target page.
     */
    LINK,

    /**
     * When a page tet contains {@code #REDIRECT [[target]] } directive it indicates that it should permanently
     * redirect to the target page. We exact the redirecting page title as a type {@link #REDIRECT} alias for the
     * target.
     */
    REDIRECT,

    /**
     * Common and canonical names for a topic are conventionally listed in bold in the articles first paragraph. Note
     * however that this is purely syntactic markup, and so is liable to be used for other meanings.
     * <p/>
     * When P1BOLD alias types are enabled, the bold surface text is produced as an alias of the page title.
     */
    P1BOLD,

    /**
     * Disambiguation (DAB) pages contains internal links to articles that each represent a single sense of the page
     * title. When this type is enabled, the DAB title is extracted as an alias for each of the linked articles.
     */
    DAB_TITLE,

    /**
     * There may be multiple terms that refer to the same disambiguation page (e.g AMP redirects to Amp). This is
     * indicated with bold text at the start of the disambiguation page, or hat-note redirects. These are extracted
     * but are not distinguished from normal pages, so the type is not used.
     */
    @Deprecated
    DAB_REDIRECT,

    /**
     * Articles frequently contain special templates to indicate related content. For example the template
     * {@code {{About|USE1||PAGE2}} } transcludes to <em>This page is about USE1. For other uses, see PAGE2.</em> There
     * are a large number of variations on this theme, each of which is handled slightly differently, but in general
     * we extract aliases where a more ambiguous terms references a less ambiguous term.
     */
    HAT_NOTE,

    /**
     * Page titles frequently contains disambiguation suffixes as bracketed terms or after a comma. When this alias
     * type is enabled we permute page titles be stripping all combinations of disambiguation suffix, and include each
     * variation as an alias of the full title.
     * <p/>
     * Note there is one exception: the suffix <tt>"(disambiguation)"</tt>, which is used to denote a disambiguation
     * page. If this suffix is encountered it is <em>always</em> stripped.
     */
    TRUNCATED,

    /*
     * ==================================
     *  New Types
     * ==================================
     */

    /**
     * Articles about people usually contain a special infobox denoted by the <tt>{{Persondata}}</tt> template. This
     * includes, among other things, a list of "<tt>ALTERNATIVE NAMES</tt>" for the individual. When this alias type
     * is enabled we extra each alternative name as an alias of the page title.
     * <p/>
     * When processing this alias we handle commas differently from other articles. In addition to including truncated
     * variants we also re-order the name, broken by commas. E.g, for the name "Augustine, Saint, Bishop of Hippo", we
     * also produce "Saint Augustine", "Bishop of Hippo", and "Augustine, Bishop of Hippo".
     */
    PERSON_ALT_NAME,

    /**
     * Same as {@code P1BOLD} but for second paragraph, moderately improve recall at the expense of precision.
     *
     * @see #P1BOLD
     */
    P2BOLD,

    /**
     * Extract all bold text in the first section (preceding the first sub-title) as aliases of the page title.
     * Alternative to first and second paragraph bold text extraction ({@link #P1BOLD} and {@link #P2BOLD}).
     */
    S1BOLD;

    /*
     * ==================================
     *  Types Sets
     * ==================================
     */

    /**
     * Set of all types that where proposed in <em>Hackey et al. (2012) "Evaluating Entity Linking with
     * Wikipedia".</em>
     */
    public static final Collection<AliasType> HACKEY = EnumSet.of(
            TITLE, LOWERCASE_TITLE, LINK, REDIRECT, P1BOLD,
            DAB_TITLE, HAT_NOTE, TRUNCATED);

    /**
     * Default types to use. (First section bold instead of paragraphs. Disable DAB_REDIRECT which doesn't work
     * anyway.)
     */
    public static final Collection<AliasType> STANDARD = EnumSet.of(
            TITLE, LOWERCASE_TITLE, LINK, REDIRECT, P1BOLD, S1BOLD,
            DAB_TITLE, HAT_NOTE, TRUNCATED, PERSON_ALT_NAME);


}
