package uk.ac.susx.tag.wag;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import de.fau.cs.osr.ptk.common.AstVisitor;
import de.fau.cs.osr.ptk.common.ast.AstNode;
import de.fau.cs.osr.ptk.common.ast.NodeList;
import de.fau.cs.osr.ptk.common.ast.Text;
import org.sweble.wikitext.engine.Page;
import org.sweble.wikitext.engine.config.WikiConfigurationInterface;
import org.sweble.wikitext.engine.utils.EntityReferences;
import org.sweble.wikitext.lazy.parser.*;
import org.sweble.wikitext.lazy.parser.Enumeration;
import org.sweble.wikitext.lazy.preprocessor.Redirect;
import org.sweble.wikitext.lazy.preprocessor.Template;
import org.sweble.wikitext.lazy.preprocessor.TemplateArgument;
import org.sweble.wikitext.lazy.utils.XmlCharRef;
import org.sweble.wikitext.lazy.utils.XmlEntityRef;
import uk.ac.susx.tag.util.StringUtils;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static uk.ac.susx.tag.wag.AstUtils.*;


import static com.google.common.base.Preconditions.checkNotNull;

/**
 * AliasAstVisitor is a Sweble {@link AstVisitor} which produces a list of all internal synonyms in the given
 * wikipedia AstNode.
 * <p/>
 * <ul>
 * <li>All the officially defined hat-note templates are parsed, and aliases are extracted from them where
 * appropriately. This includes links to disambiguation pages, or alternative source of information.</li>
 * <li>The page title suffix "(disambiguation)" is stripped where-ever it is found.</li>
 * <li>Page title that  include disambiguating phrases (either in brackets or after a comma) are
 * <em>additionally</em> aliased in truncated form.</li>
 * <li>Where pages are referenced with a label (e.g {@code [[link|label]]}) the label is taken over link text. This
 * also
 * applies to hat-note template arguments of the form " {@code link{{!}}label}".
 * <li></li>
 * </li>
 * </ul>
 * TODO: Disambiguation page redirect (i.e redirects too DAB pages) should be typed distinctly from other pages to be
 * consistent with Hackey work. (http://en.wikipedia.org/wiki/Amp)
 * <p/>
 * TODO: Template arguments with unparsed links. Currently template arguments are only half-parsed by sweble
 * <p/>
 * TODO: Name-spaces (e.g  Apocrypha => wiktionary:apocryphal )
 * TODO: Sub-page references (Arsenal F.C. => Arsenal F.C. records#Player records)
 * TODO: Probably we could extract something from categories
 * TODO: common name for template
 * TODO: Template discussed in http://en.wikipedia.org/wiki/Template:Consider_disambiguation
 * TODO: Check everything in http://en.wikipedia.org/wiki/Category:Hatnote_templates
 *
 * @author Hamish Morgan
 */
public class AliasAstVisitor extends AstVisitor {
    private static final boolean VERBOSE_WARNINGS = false;

    private static final Logger LOG = Logger.getLogger(AliasAstVisitor.class.getName());
    private final String pageTitle;
    private final WikiConfigurationInterface config;

    private ImmutableList.Builder<Alias> synonyms;

    private Set<StringBuilder> surfaceTextBuilders = Sets.newIdentityHashSet();

    /**
     * Store all-link surfaces (the part that is displayed) for all links in the page. If a {{disambiguation}} template
     * is discovered this collection is used to create aliases from the current pageTitle to all the surfaces.
     */
    private Set<String> linkSurfaces;

    /**
     *
     */
    private int nonEmptyParagraphCounter = 0;

    /**
     *
     */
    private int sectionCounter = 0;

    /**
     *
     */
    private final EnumSet<AliasType> produceTypes;
    /**
     * True when the current page contains the "lowercase title" template; e.g iPod, and gzip. Reset to false on each
     * new page, then set to true when the template is found. When true the title arc is produced with the target
     * label in lower-case.
     */
    private boolean lowerCaseTitle;

    public AliasAstVisitor(String pageTitle, WikiConfigurationInterface config, EnumSet<AliasType> produceTypes) {
        this.pageTitle = checkNotNull(pageTitle, "pageTitle").trim();
        this.config = checkNotNull(config, "config");
        this.produceTypes = checkNotNull(produceTypes, "produceTypes");
    }

    @Override
    protected boolean before(AstNode node) {
        synonyms = ImmutableList.builder();
        lowerCaseTitle = false;
        linkSurfaces = Sets.newHashSet();
        return super.before(node);
    }


    @Override
    protected Object after(AstNode node, Object result) {

        // Add the article title alias (which is an identity alias to itself)
        addPageTitleAlias(AliasType.TITLE, Alias.NO_SUBTYPE, pageTitle, pageTitle);


        // Produce an arc from the regular title to the lower-case variant if the lower-case title template is found
        if (lowerCaseTitle) {
            final String lcTitle = StringUtils.firstCharToLowerCase(pageTitle.trim());
            addPageTitleAlias(AliasType.LOWERCASE_TITLE, Alias.NO_SUBTYPE, pageTitle.trim(), lcTitle);
            // Also produce the identity relation
            addPageTitleAlias(AliasType.LOWERCASE_TITLE, Alias.NO_SUBTYPE, lcTitle, lcTitle);
        }

//        print(node);

        return synonyms.build();
    }


    public void visit(Template template) {

        final String templateName = getText(template.getName());


        if ("lowercase title".equalsIgnoreCase(templateName)) {
            lowerCaseTitle = true;
        }


        parseHatNote(template);

    }

    public void visit(AstNode n) {

        // Fallback for all nodes that are not explicitly handled below
//        System.out.println("<" + n.getNodeName() + " />");
    }


    public void visit(NodeList n) {
        iterate(n);
    }


    public void visit(Itemization e) {
        iterate(e.getContent());
    }

    public void visit(ItemizationItem i) {
        iterate(i.getContent());
    }

    public void visit(Enumeration e) {
        iterate(e.getContent());
    }

    public void visit(EnumerationItem item) {
        iterate(item.getContent());
    }

    public void visit(Page p) {
        iterate(p.getContent());
    }

    public void visit(Redirect node) {
        addPageTitleAlias(AliasType.REDIRECT, Alias.NO_SUBTYPE, pageTitle, node.getTarget());
    }

    public void visit(Section s) {
        sectionCounter++;
        iterate(s.getBody());
    }


    public void visit(Paragraph paragraph) {
        final StringBuilder surface = newSurface();

        iterate(paragraph.getContent());

        if (!surface.toString().trim().isEmpty()) {
            ++nonEmptyParagraphCounter;
        }

        removeSurface(surface);
    }


    public void visit(InternalLink link) {
        final StringBuilder surface = newSurface();

        iterate(link.getTitle());

        final String surfaceText = surface.toString().trim();
        removeSurface(surface);

        if (surfaceText.length() > 0) {
            linkSurfaces.add(surfaceText);
            addPageTitleAlias(AliasType.LINK, Alias.NO_SUBTYPE, surfaceText, link.getTarget());
        } else {
            linkSurfaces.add(link.getTarget());
            addPageTitleAlias(AliasType.LINK, Alias.NO_SUBTYPE, link.getTarget(), link.getTarget());
            appendSurfaceText(link.getTarget());
        }


    }


    public void visit(Bold e) {
        final StringBuilder surface = newSurface();

        iterate(e.getContent());


        String text = surface.toString().trim();

        if (!text.isEmpty()) {
            if (nonEmptyParagraphCounter == 0) {
                addPageTitleAlias(AliasType.P1BOLD, Alias.NO_SUBTYPE, surface.toString(), pageTitle);
            } else if (nonEmptyParagraphCounter == 1) {
                addPageTitleAlias(AliasType.P2BOLD, Alias.NO_SUBTYPE, surface.toString(), pageTitle);
            }

            if (sectionCounter == 0)
                addPageTitleAlias(AliasType.S1BOLD, Alias.NO_SUBTYPE, surface.toString(), pageTitle);
        }

        removeSurface(surface);
    }


    public void visit(Italics i) {
        iterate(i.getContent());
    }

    public void visit(XmlCharRef cr) {
        appendSurfaceText(String.valueOf(Character.toChars(cr.getCodePoint())));
    }

    public void visit(XmlEntityRef er) {
        String ch = EntityReferences.resolve(er.getName());

        if (ch == null) {
            appendSurfaceText("&");
            appendSurfaceText(er.getName());
            appendSurfaceText(";");
        } else {
            appendSurfaceText(ch);
        }
    }

    public void visit(Url url) {
        appendSurfaceText(url.getProtocol());
        appendSurfaceText(":");
        appendSurfaceText(url.getPath());
    }

    public void visit(Text text) {
        appendSurfaceText(text.getContent());
    }

    public void visit(Whitespace whitespace) {
        appendSurfaceText(" ");
    }

    /*

     */
    private void appendSurfaceText(String text) {
        for (StringBuilder builder : surfaceTextBuilders) {
            builder.append(text);
        }
    }


    private StringBuilder newSurface() {
        final StringBuilder surface = new StringBuilder();
        if (!surfaceTextBuilders.add(surface))
            throw new AssertionError();
        return surface;
    }

    private void removeSurface(StringBuilder surface) {
        if (!surfaceTextBuilders.remove(surface))
            throw new AssertionError();
    }


    private void addPageTitleAlias(AliasType type, String subType, String source, String target) {
        source = source.trim();
        target = target.trim();

        // Subsection links are a huge pain in the arse, because the reference semantics can vary wildly depending on
        // the context. In addition the relation to source (place it's linked from) is hard to determine. Note that we
        // always want to produce aliases from an ambiguous source to a concrete target (or at least relatively.) Is a
        // section of another page less ambiguous? Well sometimes but not always. However since we are indenturing to
        // do entity linking, a sub-page target will generally be undesirable so lets jsut ignore all of them.
        if (target.contains("#")) {
            return;
        }

        source = stripSuffixIfPresent(source, DISAMBIGUATION_SUFFIX);
        target = stripSuffixIfPresent(target, DISAMBIGUATION_SUFFIX);

        source = stripNamespaces(source);
        target = stripNamespaces(target);

        if (source.isEmpty() || target.isEmpty())
            return;

        addAlias(type, subType, source, target);

        Set<String> sourcePerms = wikiTitleVarients(source);
//        Set<String> targetPerms = wikiTitleVarients(target);

//        for (String s : sourcePerms) {
//            for (String t : targetPerms) {
//                addAlias(AliasType.TRUNCATED, type + "/" + subType, s, t);
//            }
//        }
        for (String sourceVariant : sourcePerms) {
            addAlias(AliasType.TRUNCATED,
                    type + (subType.isEmpty() ? "" : "/" + subType),
                    sourceVariant, target);
        }
//        for (String t : targetPerms) {
//            addAlias(AliasType.TRUNCATED, type + "/" + subType, source, t);
//        }
    }

    private void addAlias(AliasType type, String subType, String source, String target) {
        if (!produceTypes.contains(type))
            return;

        synonyms.add(new Alias(type, subType, source.trim(), target.trim()));
    }


    private void parseAllLinks(AliasType type, String subType, AstNode node) {
        final List<AstNode> links = getLinks(node);
        for (AstNode link : links) {
            if (link.getClass().equals(InternalLink.class)) {
                // Get the surface text if there is any, otherwise use the title
                final String surfaceText = getText(((InternalLink) link).getTitle());
                if (surfaceText.length() > 0) {
                    addPageTitleAlias(type, subType, pageTitle, surfaceText);
                } else {
                    addPageTitleAlias(type, subType, pageTitle, ((InternalLink) link).getTarget());
                }
            }
        }
    }


    private final Set<String> foundTemplateNames = Sets.newHashSet();

    private void parseHatNote(Template template) {
        final String templateName = getText(template.getName());
        final String subType = templateName.toLowerCase();

        final NodeList args = template.getArgs();
        if ("Hatnote".equalsIgnoreCase(templateName) || "Rellink".equalsIgnoreCase(templateName)) {
            //{{Hatnote|CUSTOM TEXT}}
            //{{Hatnote|For other senses of this term, see [[etc…]]}}
            //{{Rellink|CUSTOM TEXT}}

            checkTemplateArgs(1, 1, template);
            // Extract all link surfaces as aliases
            // TODO: Template contents do not appear to be parsed correctly by Sweble so links aren't retrieved.
            parseAllLinks(AliasType.HAT_NOTE, subType, template.getArgs());

        } else if ("About".equalsIgnoreCase(templateName)
                || "Two other uses".equalsIgnoreCase(templateName)
                || "Three other uses".equalsIgnoreCase(templateName)) {

            // {{About|USE1||PAGE2}}
            // {{About|USE1|USE2|PAGE2}}
            // {{About|USE1|USE2|PAGE2#SUBSECTION{{!}}PAGE2TITLE}}
            // {{About|||PAGE1|and|PAGE2}}
            // {{About|USE1|USE2|PAGE2|and|PAGE3}}
            // {{About|USE1|USE2|PAGE2|USE3|PAGE3}}
            // {{About||USE2|PAGE2|USE3|PAGE3|other uses}}
            // {{About|USE1|USE2|PAGE2|USE3|PAGE3|other uses}}
            // {{About|USE1|USE2|PAGE2|USE3|PAGE3|other uses|PAGE4}}
            // {{About|USE1|USE2|PAGE2|USE3|PAGE3|other uses|PAGE4|and}}
            // {{About|USE1|USE2|PAGE2|USE3|PAGE3|USE4|PAGE4|USE5|PAGE5}}
            checkTemplateArgs(0, 9, template);

            // The first argument (if it is set) always refers to the usage on the current page.
            // Subsequent arguments occur in pairs for an alternative use, followed by the page link. If only the
            // use is present, without the link, then it defaults to refer to the disambiguation page for the
            // current pages title.
            for (int i = 2; i < args.size(); i += 2) {
                addPageTitleAlias(AliasType.HAT_NOTE, subType, pageTitle, getLinkSurface(args.get(i)));
            }

            // Force second argument with "Two other uses" template.
//            if ("Two other uses".equalsIgnoreCase(templateName) && args.size() == 3) {
//                //{{Two other uses|USE1|USE2|PAGE2}} →
//                //{{Two other uses|USE1|USE2|PAGE2|USE3|PAGE3}} →
//                addPageTitleAlias(AliasType.HAT_NOTE, subType, pageTitle, pageTitle + DISAMBIGUATION_SUFFIX);
//            }

            // Force third argument with "Three other uses" template.
//            if ("Three other uses".equalsIgnoreCase(templateName) && args.size() == 5) {
//                //{{Three other uses|USE1|USE2|PAGE2|USE3|PAGE3}}
//                //{{Three other uses||USE2|PAGE2|USE3|PAGE3}} ?
//                //{{Three other uses|USE1|USE2|PAGE2|USE3|PAGE3|USE4|PAGE4}} ?
//                addPageTitleAlias(AliasType.HAT_NOTE, subType, pageTitle, pageTitle + DISAMBIGUATION_SUFFIX);
//            }


            // TODO: PAGE2#SUBSECTION{{!}}PAGE2TITLE isnt supported correctly (need to find an example)


        } else if ("For".equalsIgnoreCase(templateName)) {
            //{{For|OTHER TOPIC}}
            //{{For|OTHER TOPIC|PAGE1}}
            //{{For||PAGE1|PAGE2}}
            //{{For|OTHER TOPIC|PAGE1|PAGE2}}
            //{{For|OTHER TOPIC|PAGE1|PAGE2|PAGE3}}

            checkTemplateArgs(1, 4, template);

            // The first argument (if it is set) refers to an alternative usage. All subsequent argument links to pages
            // that discuss this alternative usage. If there is exactly zero or one argument then the disambiguation
            // page is referenced.

            for (int i = 1; i < args.size(); i++) {
                addPageTitleAlias(AliasType.HAT_NOTE, subType, pageTitle, getLinkSurface(args.get(i)));
            }
//            }
        } else if ("For2".equalsIgnoreCase(templateName)) {
            //{{For2|OTHER TOPIC|CUSTOM TEXT}}
            checkTemplateArgs(2, 2, template);
            // TODO: Template contents do not appear to be parsed correctly by Sweble so links aren't retrieved.
            if (args.size() >= 2) {
                parseAllLinks(AliasType.HAT_NOTE, subType, args.get(1));
            }

        } else if ("Other uses".equalsIgnoreCase(templateName)) {
            //{{Other uses}} (disambiguous) →
            //{{Other uses|PAGE1}} →
            //{{Other uses|PAGE1|PAGE2}} →
            checkTemplateArgs(0, 2, template);

            // Note: Added support for arbitrary number of arguments (because people get it wrong.)

            for (AstNode node : args) {
                final String page = getLinkSurface(node);
                addPageTitleAlias(AliasType.HAT_NOTE, subType, page, pageTitle);
            }

        } else if ("Other uses2".equalsIgnoreCase(templateName)) {
            //{{Other uses2|PAGE1}} (disambiguous) →
            checkTemplateArgs(1, 1, template);

            if (!args.isEmpty()) {
                final String otherPage = getLinkSurface(args.get(0));
                addPageTitleAlias(AliasType.HAT_NOTE, subType, otherPage, pageTitle);
            }

        } else if ("Other uses of".equalsIgnoreCase(templateName)) {
            //{{Other uses of}} (disambiguous) →
            //{{Other uses of|TOPIC}} (disambiguous) →
            //{{Other uses of|TOPIC|PAGE1}} →
            checkTemplateArgs(0, 2, template);


            if (args.size() >= 2) {
                final String otherPage = getLinkSurface(args.get(1));
                addPageTitleAlias(AliasType.HAT_NOTE, subType, otherPage, pageTitle);
            }

        } else if ("Redirect".equalsIgnoreCase(templateName)
                || "Redirect6".equalsIgnoreCase(templateName)) {
            //"… redirects here. For other uses, see …"
            //{{Redirect|REDIRECT}} (disambiguous) →
            //"REDIRECT" redirects here. For other uses, see REDIRECT (disambiguation).
            //{{Redirect|REDIRECT||PAGE1}} →
            //"REDIRECT" redirects here. For other uses, see PAGE1.
            //{{Redirect|REDIRECT|USE1|PAGE1}} →
            //"REDIRECT" redirects here. For USE1, see PAGE1.
            //{{Redirect|REDIRECT|USE1|PAGE1|USE2|PAGE2}} →
            //"REDIRECT" redirects here. For USE1, see PAGE1. For USE2, see PAGE2.
            //{{Redirect|REDIRECT|USE1|PAGE1|USE2|PAGE2|USE3|PAGE3}} →
            //"REDIRECT" redirects here. For USE1, see PAGE1. For USE2, see PAGE2. For USE3, see PAGE3.
            //{{Redirect|REDIRECT|USE1|PAGE1|and|PAGE2}} →
            //"REDIRECT" redirects here. For USE1, see PAGE1 and PAGE2.
            //{{Redirect|REDIRECT|USE1|PAGE1|USE2|PAGE2|and|PAGE3}} →
            //"REDIRECT" redirects here. For USE1, see PAGE1. For USE2, see PAGE2 and PAGE3.

            // TODO: Redirect6 isn't precisely implemented
            //{{Redirect6|REDIRECT|USE1|PAGE1}} (disambiguous) →
            //"REDIRECT" redirects here. For USE1, see PAGE1. For other uses, see REDIRECT (disambiguation).
            //{{Redirect6|REDIRECT|USE1|PAGE1|‌|PAGE2}} →
            //"REDIRECT" redirects here. For USE1, see PAGE1. For other uses, see PAGE2.

            // First argument is a redirect page. Subsequent argument pairs are the description and link for an
            // alternative usage. If the final link is missing then it refers to the disambiguation page for the
            // current article title.
            checkTemplateArgs(1, 7, template);

            if (!args.isEmpty()) {
                final String redirect = getLinkSurface(args.get(0));
                addPageTitleAlias(AliasType.HAT_NOTE, subType, redirect, pageTitle);
                for (int i = 2; i < args.size(); i += 2) {
                    final String page = getLinkSurface(args.get(i));
                    addPageTitleAlias(AliasType.HAT_NOTE, subType, pageTitle, page);
                }
            }

        } else if ("Redirect2".equalsIgnoreCase(templateName)) {
            //For two sources:
            //{{Redirect2|REDIRECT1|REDIRECT2}} (disambiguous) →
            //"REDIRECT1" and "REDIRECT2" redirect here. For other uses, see REDIRECT1 (disambiguation).
            //{{Redirect2|REDIRECT1|REDIRECT2|USE|PAGE1}} →
            //"REDIRECT1" and "REDIRECT2" redirect here. For USE, see PAGE1.
            //{{Redirect2|REDIRECT1|REDIRECT2|USE1|PAGE1|USE2|PAGE2}} →
            //"REDIRECT1" and "REDIRECT2" redirect here. For USE1, see PAGE1. For USE2, see PAGE2.
            //{{Redirect2|REDIRECT1|REDIRECT2|USE1|PAGE1|USE2|PAGE2|USE3|PAGE3}} →
            //"REDIRECT1" and "REDIRECT2" redirect here. For USE1, see PAGE1. For USE2, see PAGE2. For USE3, see PAGE3.
            //{{Redirect2|REDIRECT1|REDIRECT2|USE1|PAGE1|USE2|PAGE2|USE3|PAGE3|USE4|PAGE4}} →
            //"REDIRECT1" and "REDIRECT2" redirect here. For USE1, see PAGE1. For USE2, see PAGE2. For USE3, see PAGE3. For USE4, see PAGE4.
            checkTemplateArgs(2, 10, template);

            if (!args.isEmpty()) {
                final String redirect1 = getLinkSurface(args.get(0));
                addPageTitleAlias(AliasType.HAT_NOTE, subType, redirect1, pageTitle);

                if (args.size() >= 2) {
                    final String redirect2 = getLinkSurface(args.get(1));
                    addPageTitleAlias(AliasType.HAT_NOTE, subType, redirect2, pageTitle);

                    for (int i = 2; i < args.size(); i += 2) {
                        final String page = getLinkSurface(args.get(i));
                        addPageTitleAlias(AliasType.HAT_NOTE, subType, pageTitle, page);
                    }
                }
            }

        } else if ("Redirect3".equalsIgnoreCase(templateName)) {
            //{{Redirect3|REDIRECT|TEXT}} →
            //"REDIRECT" redirects here. TEXT.
            checkTemplateArgs(2, 2, template);

            if (!args.isEmpty()) {
                final String redirect = getLinkSurface(args.get(0));
                addPageTitleAlias(AliasType.HAT_NOTE, subType, redirect, pageTitle);
            }

        } else if ("Redirect4".equalsIgnoreCase(templateName)) {
            //{{Redirect4|REDIRECT1|REDIRECT2}} (disambiguous) →
            //"REDIRECT1" and "REDIRECT2" redirect here. For other uses, see REDIRECT1 (disambiguation) and REDIRECT2 (disambiguation).
            //For three sources:
            checkTemplateArgs(2, 2, template);

            for (int i = 0; i < args.size(); i++) {
                final String redirect = getLinkSurface(args.get(0));
                addPageTitleAlias(AliasType.HAT_NOTE, subType, redirect, pageTitle);
            }

        } else if ("Redirect7".equalsIgnoreCase(templateName)) {
            //{{Redirect7|"REDIRECT1", "REDIRECT2", and "REDIRECT3"|USE1|PAGE1|USE2|PAGE2}} →
            //"REDIRECT1", "REDIRECT2", and "REDIRECT3" redirect here. For USE1, see PAGE1. For USE2, see PAGE2.
            checkTemplateArgs(5, 5, template);

            if (!args.isEmpty()) {
                final String[] parts = getText(args.get(0)).split("\"");
                for (int i = 0; i < parts.length; i += 2)
                    addPageTitleAlias(AliasType.HAT_NOTE, subType, parts[i], pageTitle);
            }

        } else if ("Redirect10".equalsIgnoreCase(templateName)) {
            //{{Redirect10|REDIRECT1|REDIRECT2|REDIRECT3}} (disambiguous) →
            //"REDIRECT1", "REDIRECT2", and "REDIRECT3" redirect here. For other uses, see REDIRECT1 (disambiguation), REDIRECT2 (disambiguation), and REDIRECT3 (disambiguation).
            checkTemplateArgs(3, 3, template);

            for (int i = 0; i < args.size(); i++) {
                final String redirect = getLinkSurface(args.get(0));
                addPageTitleAlias(AliasType.HAT_NOTE, subType, redirect, pageTitle);
            }

        } else if ("Redirect-synonym".equalsIgnoreCase(templateName)) {
            //{{Redirect-synonym|TERM|OTHER TOPIC}} →
            //"TERM" redirects here. TERM may also refer to OTHER TOPIC.
            checkTemplateArgs(2, 2, template);

            if (!args.isEmpty()) {
                final String term = getLinkSurface(args.get(0));
                addPageTitleAlias(AliasType.HAT_NOTE, subType, term, pageTitle);

                if (args.size() >= 2) {
                    // TODO: Topic is free-form so should be parsed for links
                    final String otherTopic = getText(args.get(1));
                    addPageTitleAlias(AliasType.HAT_NOTE, subType, term, otherTopic);
                }
            }

        } else if ("Redirect text".equalsIgnoreCase(templateName)) {
            //{{Redirect text|REDIRECT|TEXT}} →
            //"REDIRECT" redirects here. TEXT.
            checkTemplateArgs(1, 1, template);

            if (!args.isEmpty()) {
                final String redirect = getLinkSurface(args.get(0));
                addPageTitleAlias(AliasType.HAT_NOTE, subType, redirect, pageTitle);
                //TODO: Parse links form the text?
            }

        } else if ("Redirect-distinguish".equalsIgnoreCase(templateName)) {
            //... Not to be confused with ...
            //{{Redirect-distinguish|REDIRECT|PAGE1}} →
            //"REDIRECT" redirects here. It is not to be confused with PAGE1.
            //{{Redirect-distinguish|REDIRECT|PAGE1|PAGE2|PAGE3|PAGE4}} →
            //"REDIRECT" redirects here. It is not to be confused with PAGE1, PAGE2, PAGE3, or PAGE4.
            checkTemplateArgs(2, 5, template);

            // Note: The distinguish pages are explicitly not the same as the current page, but they could be confused
            // with the redirect

            if (!args.isEmpty()) {
                final String redirect = getLinkSurface(args.get(0));
                addPageTitleAlias(AliasType.HAT_NOTE, subType, redirect, pageTitle);
                for (int i = 1; i < args.size(); i++) {
                    final String otherPage = getLinkSurface(args.get(i));
                    addPageTitleAlias(AliasType.HAT_NOTE, subType, redirect, otherPage);
                }
            }

        } else if ("Redirect-distinguish2".equalsIgnoreCase(templateName)) {
            //{{Redirect-distinguish2|REDIRECT|TEXT}} →
            //"REDIRECT" redirects here. It is not to be confused with TEXT.
            checkTemplateArgs(2, 2, template);

            if (!args.isEmpty()) {
                final String redirect = getLinkSurface(args.get(0));
                addPageTitleAlias(AliasType.HAT_NOTE, templateName.toLowerCase(), redirect, pageTitle);
            }

        } else if ("Consider disambiguation".equalsIgnoreCase(templateName)) {
            //"… If you are seeking another topic, … consider adding it to …."
            //{{Consider disambiguation|USE1|USE2|PAGE2}} (disambiguous) →
            //This article is about USE1. For USE2, see PAGE2. If you are seeking another topic, additional searches are listed at Hatnote (disambiguation).
            //{{Consider disambiguation|USE1|USE2|PAGE2|PAGE3}} →
            //This article is about USE1. For USE2, see PAGE2. If you are seeking another topic, additional searches are listed at PAGE3.
            //        Note:USE1 can be omitted, resulting in language like {{For}} above.
            checkTemplateArgs(3, 4, template);

            if (args.size() >= 3) {
                final String page2 = getLinkSurface(args.get(2));
                addPageTitleAlias(AliasType.HAT_NOTE, subType, pageTitle, page2);
                addPageTitleAlias(AliasType.HAT_NOTE, subType, page2, pageTitle);
                if (args.size() >= 4) {
                    final String page3 = getLinkSurface(args.get(3));
                    addPageTitleAlias(AliasType.HAT_NOTE, subType, page3, pageTitle);
                }
            }

        } else if ("Other people".equalsIgnoreCase(templateName)) {
            //{{Other people}} (disambiguous) ?
            //For other people named Hatnote, see Hatnote (disambiguation).
            //{{Other people|NAME}} (disambiguous) ?
            //For other people named NAME, see NAME (disambiguation).
            //{{Other people|NAME|PAGE}} ?
            //For other people named NAME, see PAGE.
            //{{Other people|NAME|PAGE|named=titled}} ?
            //For other people titled NAME, see PAGE.
            checkTemplateArgs(0, 3, template);

            if (!args.isEmpty()) {
                final String name = getLinkSurface(args.get(0));
                addPageTitleAlias(AliasType.HAT_NOTE, subType, name, pageTitle);
                if (args.size() >= 2) {
                    final String page = getLinkSurface(args.get(0));
                    addPageTitleAlias(AliasType.HAT_NOTE, subType, page, pageTitle);
                }
            }

        } else if ("Other people2".equalsIgnoreCase(templateName)) {
            //{{Other people2|PAGE}} →
            //For other people of the same name, see PAGE.
            checkTemplateArgs(1, 1, template);

            if (!args.isEmpty()) {
                final String otherPage = getLinkSurface(args.get(0));
                addPageTitleAlias(AliasType.HAT_NOTE, subType, otherPage, pageTitle);
            }

        } else if ("Other people3".equalsIgnoreCase(templateName)) {
            //{{Other people3}} (disambiguous) →
            //For other people named Hatnote, see Hatnote (disambiguation).
            //        Note: same as {{About}} except uses "other people" instead of "other uses" if only 1 parameter is used
            //{{Other people3|PERSON1}} (disambiguous) →
            //This article is about PERSON1. For other people named Hatnote, see Hatnote (disambiguation).
            //{{Other people3|PERSON1|PERSON2}} (disambiguous) →
            //This article is about PERSON1. For PERSON2, see Hatnote (disambiguation).
            //{{Other people3|PERSON1|PERSON2|PAGE2}} →
            //This article is about PERSON1. For PERSON2, see PAGE2.
            //{{Other people3|PERSON1||PAGE2}} →
            //This article is about PERSON1. For other people named Hatnote, see PAGE2.
            checkTemplateArgs(0, 3, template);

            if (args.size() >= 3) {
                final String person = getLinkSurface(args.get(0));
                addPageTitleAlias(AliasType.HAT_NOTE, subType, pageTitle, person);
                addPageTitleAlias(AliasType.HAT_NOTE, subType, person, pageTitle);
            }

        } else if ("Other people5".equalsIgnoreCase(templateName)) {
            //{{Other people5|NAME1|NAME2|NAME3|NAME4}} →
            //For other people with similar names, see NAME1, NAME2, NAME3, or NAME4.
            //        Note: defaults to "named" as in {{Other people}}, exists for options like "nicknamed", "known as", etc.
            checkTemplateArgs(1, 4, template);

            // Note: Re-interpreted as allowing arbitrary number of arguments.
            for (AstNode node : args) {
                final String name = getLinkSurface(node);
                addPageTitleAlias(AliasType.HAT_NOTE, subType, pageTitle, name);
                addPageTitleAlias(AliasType.HAT_NOTE, subType, name, pageTitle);
            }

        } else if ("Other places".equalsIgnoreCase(templateName)) {
            //{{Other places}}, analogous to {{Other uses}} (disambiguous) →
            //For other places with the same name, see Hatnote (disambiguation).
            //{{Other places|PAGE}}, analogous to {{Other uses2}}(disambiguous) →
            //For other places with the same name, see PAGE (disambiguation).
            checkTemplateArgs(0, 1, template);

            if (!args.isEmpty()) {
                final String otherPage = getLinkSurface(args.get(0));
                addPageTitleAlias(AliasType.HAT_NOTE, subType, otherPage, pageTitle);
            }

        } else if ("Other places3".equalsIgnoreCase(templateName)) {
            //{{Other places3|PAGE}}, analogous to {{Other uses}} →
            //For other places with the same name, see PAGE.
            checkTemplateArgs(1, 1, template);

            if (!args.isEmpty()) {
                final String otherPage = getLinkSurface(args.get(0));
                addPageTitleAlias(AliasType.HAT_NOTE, subType, otherPage, pageTitle);
            }

        } else if ("Other hurricanes".equalsIgnoreCase(templateName)) {
            //{{Other hurricanes}} (disambiguous) →
            //For other storms of the same name, see Hatnote (disambiguation).
            //{{Other hurricanes|PAGE1}} →
            //For other storms of the same name, see PAGE1.
            //{{Other hurricanes|PAGE1|THIS}} →
            //This page is about THIS. For other storms of the same name, see PAGE1.
            //{{Other hurricanes||THIS}} →
            //This page is about THIS. For other storms of the same name, see Hatnote (disambiguation).
            checkTemplateArgs(1, 2, template);

            if (!args.isEmpty()) {
                final String otherPage = getLinkSurface(args.get(0));
                addPageTitleAlias(AliasType.HAT_NOTE, subType, otherPage, pageTitle);
            }

        } else if ("Other ships".equalsIgnoreCase(templateName)) {
            //{{Other ships|SHIP1}} →
            //For other ships of the same name, see SHIP1.
            checkTemplateArgs(1, 1, template);

            if (args.size() > 0) {
                final String otherShip = getLinkSurface(args.get(0));
                addPageTitleAlias(AliasType.HAT_NOTE, subType, otherShip, pageTitle);
            }

        } else if ("Distinguish".equalsIgnoreCase(templateName)) {

            //{{Distinguish|PAGE1}} →
            //Not to be confused with PAGE1.
            //{{Distinguish|PAGE1|PAGE2|PAGE3|PAGE4}} →
            //Not to be confused with PAGE1, PAGE2, PAGE3, or PAGE4.
            checkTemplateArgs(1, 4, template);

            // Note: Not to be confused with implies other pages are substantively different and so
            // should not be considered good aliases.

        } else if ("Distinguish2".equalsIgnoreCase(templateName)) {
            //{{Distinguish2|TEXT}} →
            //Not to be confused with TEXT.
            checkTemplateArgs(1, 1, template);

            // Note: Not to be confused with implies other pages are substantively different and so
            // should not be considered good aliases.

        } else if ("Redirect-distinguish".equalsIgnoreCase(templateName)) {
            //{{Redirect-distinguish|REDIRECT|PAGE1}} →
            //"REDIRECT" redirects here. It is not to be confused with PAGE1.
            //{{Redirect-distinguish|REDIRECT|PAGE1|PAGE2|PAGE3|PAGE4}} →
            //"REDIRECT" redirects here. It is not to be confused with PAGE1, PAGE2, PAGE3, or PAGE4.
            checkTemplateArgs(2, 5, template);

            if (args.size() > 0) {
                final String redirect = getLinkSurface(args.get(0));
                addPageTitleAlias(AliasType.HAT_NOTE, subType, redirect, pageTitle);
                for (int i = 1; i < args.size(); i++) {
                    final String otherPage = getLinkSurface(args.get(i));
                    addPageTitleAlias(AliasType.HAT_NOTE, subType, redirect, otherPage);
                }
            }

        } else if ("Redirect-distinguish2".equalsIgnoreCase(templateName)) {
            //{{Redirect-distinguish2|REDIRECT|TEXT}} →
            //"REDIRECT" redirects here. It is not to be confused with TEXT.
            checkTemplateArgs(2, 2, template);

            if (args.size() > 0) {
                final String redirect = getLinkSurface(args.get(0));
                addPageTitleAlias(AliasType.HAT_NOTE, subType, redirect, pageTitle);
                if (args.size() > 1) {
                    // TODO: Last parameter is free form so should be parsed for links
                    final String text = getText(args.get(1));
                    addPageTitleAlias(AliasType.HAT_NOTE, subType, redirect, text);
                }
            }
        } else if ("Common name for".equalsIgnoreCase(templateName)) {
            // {{common name for|Otariid|eared seals, including the [[sea lions]]}}
            // Note: Currently there is only one use of this template
            checkTemplateArgs(1, 2, template);
            if (args.size() > 0) {
                final String name = getLinkSurface(args.get(0));
                addPageTitleAlias(AliasType.HAT_NOTE, subType, pageTitle, name);
                // TODO: Parse free-text argumentment for links
            }
        } else if ("Persondata".equalsIgnoreCase(templateName)) {


            // TODO
            final Map<String, String> data = getNamedData(template);

            if (data.containsKey("NAME") && data.containsKey("ALTERNATIVE NAMES")) {

                String name = data.get("NAME");
                String altNames = data.get("ALTERNATIVE NAMES");

                for (Map.Entry<String, String> e : getNamedData(template).entrySet())
                    if (e.getKey().equalsIgnoreCase("NAME")) {
                        name = e.getValue();
                    } else if (e.getKey().equalsIgnoreCase("ALTERNATIVE NAMES")) {
                        altNames = e.getValue();
                    }


                if (name != null && !name.isEmpty() && altNames != null && !altNames.isEmpty()) {

                    final Set<String> names = Sets.newHashSet();
                    names.add(name.trim());
                    int i, j;
                    if (-1 != (i = name.lastIndexOf(','))) {
                        names.add(name.substring(i + 1).trim() + " " + name.substring(0, i).trim());
                    }

                    for (String alt : altNames.split(";")) {
                        if (-1 != (i = alt.lastIndexOf('(')))
                            alt = alt.substring(0, i);
                        alt = alt.trim();

                        if (alt.isEmpty())
                            continue;

                        names.add(alt);

                        if (-1 != (i = alt.indexOf(','))) {
                            // If there's a second comma then everything after it is titles
                            //  E.g: Augustine, Saint, Bishop of Hippo
                            if (-1 != (j = alt.indexOf(',', i + 1))) {
                                // Augustine, Saint
                                names.add(alt.substring(0, j).trim());
                                // Bishop of Hippo
                                names.add(alt.substring(j + 1).trim());
                                // Saint Augustine
                                names.add(alt.substring(i + 1, j).trim() + " " + alt.substring(0, i).trim());
                                // Saint Augustine, Bishop of Hippo
                                names.add(alt.substring(i + 1, j).trim()
                                        + " " + alt.substring(0, i).trim()
                                        + ", " + alt.substring(j + 1).trim());
                            } else {
                                names.add(alt.substring(i + 1).trim() + " " + alt.substring(0, i).trim());
                            }
                        }
                    }

                    for (String x : names) {
                        addAlias(AliasType.PERSON_ALT_NAME, "", x, pageTitle);
                    }
                }

            }

        } else if ("Details".equalsIgnoreCase(templateName)
                || "Details3".equalsIgnoreCase(templateName)
                || "Further".equalsIgnoreCase(templateName)
                || "Further2".equalsIgnoreCase(templateName)
                || "See also".equalsIgnoreCase(templateName)
                || "See also2".equalsIgnoreCase(templateName)
                || "See for".equalsIgnoreCase(templateName)
                || "Solename".equalsIgnoreCase(templateName)) {

            // TODO:
//            System.out.println("Title: " + pageTitle);
//            System.out.println("Template: " + templateName);
//            print(template);


        } else if ("Disambiguation".equalsIgnoreCase(templateName)
                || "disambig".equalsIgnoreCase(templateName)
                || "geodis".equalsIgnoreCase(templateName)
                || "Dab".equalsIgnoreCase(templateName)
                || "DAB".equalsIgnoreCase(templateName)
                || "Disamb".equalsIgnoreCase(templateName)
                || "hndis".equalsIgnoreCase(templateName)
                || templateName.toLowerCase().endsWith(" disambiguation")) {

            //Letter-NumberCombDisambig
//            mathdab
//            {{numberdis}}
//            {{schooldis}}
//            {{Species Latin name disambiguation}}
//            shipindex
//            {{mil-unit-dis}}


            checkTemplateArgs(0, 2, template);


            for (String s : linkSurfaces)
                addPageTitleAlias(AliasType.DAB_TITLE, "", pageTitle, s);

        } else {
            if (!foundTemplateNames.contains(templateName.trim().toLowerCase())) {
                foundTemplateNames.add(templateName.trim().toLowerCase());
//            System.err.println("Unhandled template: " + templateName);

            }
        }


        //{{Selfref|ANYTEXT}} →
        //ANYTEXT
        //Further information: Wikipedia:Manual of Style/Self-references to avoid
        //[edit]Categories
        //Category-specific templates:
        //{{Category see also|THIS|THAT|THE OTHER}} →
        //See also categories: THIS, THAT, and THE OTHER
        //This is a template for linking categories horizontally. Horizontal linkage is often the right solution when vertical linkage (i.e., as sub-category and parent category) is not appropriate. In most cases, this template should be used on both categories to create reciprocal linkage between the two categories.
        //{{Cat main|MAIN ARTICLE}} →
        //The main article for this category is MAIN ARTICLE.
        //{{Category explanation|colourless green ideas}} →
        //This category is for colourless green ideas.
        //{{Category pair|TOPIC1|TOPIC2}} →
        //See also the preceding Category:TOPIC1 and the succeeding Category:TOPIC2.
        //{{CatPreceding|OTHER TOPIC}} →
        //See also the preceding Category:OTHER TOPIC.
        //{{CatSucceeding|OTHER TOPIC}} →
        //See also the succeeding Category:OTHER TOPIC.
        //{{Contrast|OTHERCAT|OTHERCAT2}} →
        //Hatnote is often contrasted with OTHERCAT or OTHERCAT2.
        //{{Contrast|OTHERCAT|OTHERCAT2|plural=yes}} →
        //Hatnote are often contrasted with OTHERCAT or OTHERCAT2.
    }


    private Map<String, String> getNamedData(Template template) {
        final ImmutableMap.Builder<String, String> mapBuilder = ImmutableMap.builder();
        for (AstNode node : template.getArgs()) {
            if (node instanceof TemplateArgument && node.size() == 2) {
                final String key = getText(node.get(0)).trim();
                final String value = getText(node.get(1)).trim();
                mapBuilder.put(key, value);
            }
        }
        try {
            return mapBuilder.build();
        } catch (IllegalArgumentException e) {
            LOG.log(Level.WARNING, "Problem loading template named data on page " + pageTitle, e.getLocalizedMessage());
            return Collections.emptyMap();
        }
    }

    private void checkTemplateArgs(int min, int max, Template template) {
        checkTemplateArgs(min, max, template, "");
    }

    private void checkTemplateArgs(int min, int max, Template template, String message) {

        final int nArgs = template.getArgs().size();
        final String msg;
        if (min == max) {
            if (min == 0) {
                msg = "Expecting no argument, but found " + nArgs;
            } else if (min == 1) {
                msg = "Expecting exactly " + min + " argument, but found " + nArgs;
            } else {
                msg = "Expecting exactly " + min + " arguments, but found " + nArgs;
            }
        } else {
            msg = "Expecting between " + min + " and " + max + " arguments, but found " + nArgs;
        }
        checkTemplate(nArgs >= min && nArgs <= max, template, msg + (message.isEmpty() ? "" : " " + message));
    }

    private void checkTemplate(boolean condition, Template template, String message) {
        if (!condition) {
            LOG.log(Level.WARNING, "Ill-formed template \"{0}\" in page \"{1}\": {2}{3}",
                    new String[]{getText(template.getName()), pageTitle, message,
                            (VERBOSE_WARNINGS ? ("\n" + AstUtils.toString(template)) : "")});
        }
    }


}