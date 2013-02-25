package uk.ac.susx.tag.wikiparser;

import java.util.EnumSet;

/**
 * Created with IntelliJ IDEA.
 * User: hiam20
 * Date: 19/02/2013
 * Time: 17:51
 * To change this template use File | Settings | File Templates.
 */
public enum AliasType {

    TITLE,
    LOWERCASE_TITLE,
    LINK,
    REDIRECT,
    /**
     * Common and canonical names for a topic are conventionally listed in bld in the articles first paragraph.
     */
    P1BOLD,
    /**
     * Same as {@code P1BOLD} but for second paragraph.
     *
     * @see #P1BOLD
     */
    P2BOLD,
    S1BOLD,
    DAB_TITLE,
    DAB_REDIRECT,
    HAT_NOTE,
    TRUNCATED,

    // New
    PERSON_ALT_NAME;

    public static final EnumSet<AliasType> STANDARD = EnumSet.of(
            TITLE, LOWERCASE_TITLE, LINK, REDIRECT, P1BOLD,
            DAB_TITLE, DAB_REDIRECT, HAT_NOTE, TRUNCATED);
}
