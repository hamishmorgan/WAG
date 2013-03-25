package uk.ac.susx.tag.wag;

import com.beust.jcommander.internal.Lists;
import org.junit.Test;
import uk.ac.susx.tag.test.AbstractTest;

import java.net.URL;
import java.util.EnumSet;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * @author hiam20
 * @since 19/02/2013 16:50
 */
public class WikiAliasGeneratorTest extends AbstractTest {


//        Wikipedia-Jaccard_Distance.xml
    //Wikipedia-AccessibleComputing.xml


    @Test
    public void testRedirect() throws Exception {
        // This resource should contain exactly one alias of type redirect
        final URL pageUrl = getClass().getResource("Wikipedia-AccessibleComputing.xml");

        final List<Alias> aliases = Lists.newArrayList();
        final WikiAliasGenerator instance = new WikiAliasGenerator(
                new AliasHandler() {
                    @Override
                    public void handle(Alias alias) {
                        aliases.add(alias);
                    }
                }, EnumSet.allOf(AliasType.class));
        instance.process(pageUrl, 1);

        assertEquals("Unexpected number of aliases produced.", 1, aliases.size());
        assertEquals("Unexpected alias type.", AliasType.REDIRECT, aliases.get(0).getType());
    }

    @Test
    public void testBoldText() throws Exception {
        // This resource should contain exactly one alias of type redirect
        final URL pageUrl = getClass().getResource("Wikipedia-Jaccard_Index.xml");

        final List<Alias> aliases = Lists.newArrayList();
        final WikiAliasGenerator instance = new WikiAliasGenerator(
                new AliasHandler() {
                    @Override
                    public void handle(Alias alias) {
                        if (alias.getType() == AliasType.P1BOLD)
                            aliases.add(alias);
                    }
                }, EnumSet.allOf(AliasType.class));
        instance.process(pageUrl, 1);


        assertEquals("Unexpected number of aliases produced.", 1, aliases.size());
        assertEquals("Unexpected alias type.", AliasType.P1BOLD, aliases.get(0).getType());
        assertEquals("Unexpected alias target.", "Jaccard index", aliases.get(0).getTarget());
        assertEquals("Unexpected alias source.", "Jaccard similarity coefficient", aliases.get(0).getSource());

    }

    @Test
    public void testInternalLinks() throws Exception {
        // This resource should contain exactly one alias of type redirect
        final URL pageUrl = getClass().getResource("Wikipedia-Brighton.xml");

        final List<Alias> aliases = Lists.newArrayList();
        final WikiAliasGenerator instance = new WikiAliasGenerator(
                new AliasHandler() {
                    @Override
                    public void handle(Alias alias) {
                        aliases.add(alias);
                    }
                }, EnumSet.allOf(AliasType.class));
        instance.process(pageUrl, 1);

        // XXX: Add some assertions
//
//        for (Alias a : aliases)
//            System.out.println("\t" + a);
////
//        assertEquals("Unexpected number of aliases produced.", 1, aliases.size());
//        assertEquals("Unexpected alias type.", Alias.AliasType.LINK, aliases.get(0).getType());
//        assertEquals("Unexpected alias target.", "Jaccard index", aliases.get(0).getTarget());
//        assertEquals("Unexpected alias source.", "Jaccard similarity coefficient", aliases.get(0).getSource());

    }

    @Test
    public void testTitles() throws Exception {
        // This resource should contain exactly one alias of type TITLE
        final URL pageUrl = getClass().getResource("Wikipedia-iPod.xml");

        final List<Alias> aliases = Lists.newArrayList();
        final WikiAliasGenerator instance = new WikiAliasGenerator(
                new AliasHandler() {
                    @Override
                    public void handle(Alias alias) {
                        if (alias.getType() == AliasType.TITLE)
                            aliases.add(alias);
                    }
                }, EnumSet.allOf(AliasType.class));
        instance.setIdentityAliasesProduced(true);

        instance.process(pageUrl, 1);

        assertEquals("Unexpected number of aliases produced.", 1, aliases.size());
        assertEquals("Unexpected alias type.", AliasType.TITLE, aliases.get(0).getType());
        assertEquals("Unexpected alias source.", "IPod", aliases.get(0).getSource());
        assertEquals("Unexpected alias target.", "IPod", aliases.get(0).getTarget());
    }

    @Test
    public void testLowercaseTitles() throws Exception {
        // This resource should contain exactly one alias of type LOWERCASE_TITLE
        final URL pageUrl = getClass().getResource("Wikipedia-iPod.xml");

        final List<Alias> aliases = Lists.newArrayList();
        final WikiAliasGenerator instance = new WikiAliasGenerator(
                new AliasHandler() {
                    @Override
                    public void handle(Alias alias) {
                        if (alias.getType() == AliasType.LOWERCASE_TITLE)
                            aliases.add(alias);
                    }
                }, EnumSet.allOf(AliasType.class));
        instance.process(pageUrl, 1);

        assertEquals("Unexpected number of aliases produced.", 1, aliases.size());
        assertEquals("Unexpected alias type.", AliasType.LOWERCASE_TITLE, aliases.get(0).getType());
        assertEquals("Unexpected alias source.", "IPod", aliases.get(0).getSource());
        assertEquals("Unexpected alias target.", "iPod", aliases.get(0).getTarget());
    }

    @Test
    public void testHatNotes() throws Exception {
        // This resource should contain exactly one alias of type HatNote
        final URL pageUrl = getClass().getResource("Wikipedia-John_A._Williams.xml");

        final List<Alias> aliases = Lists.newArrayList();
        final WikiAliasGenerator instance = new WikiAliasGenerator(
                new AliasHandler() {
                    @Override
                    public void handle(Alias alias) {
                        if (alias.getType() == AliasType.HAT_NOTE)
                            aliases.add(alias);
                    }
                }, EnumSet.allOf(AliasType.class));

        instance.process(pageUrl, 1);

        System.out.println(aliases);
    }


    @Test
    public void testHatNotes2() throws Exception {
        // This resource should contain exactly one alias of type HatNote
        final URL pageUrl = getClass().getResource("Wikipedia-Brighton.xml");

        final List<Alias> aliases = Lists.newArrayList();
        final WikiAliasGenerator instance = new WikiAliasGenerator(
                new AliasHandler() {
                    @Override
                    public void handle(Alias alias) {
                        if (alias.getType() == AliasType.HAT_NOTE)
                            aliases.add(alias);
                    }
                }, EnumSet.allOf(AliasType.class));

        instance.process(pageUrl, 1);

        System.out.println(aliases);
    }


    @Test
    public void testHatNotes_EmptyRedirect() throws Exception {
        // This resource should contain exactly one alias of type HatNote
        final URL pageUrl = getClass().getResource("Wikipedia-Stephensen's_method.xml");

//        final List<Alias> aliases = Lists.newArrayList();
        final WikiAliasGenerator instance = new WikiAliasGenerator(
                new AliasHandler() {
                    @Override
                    public void handle(Alias alias) {
                        System.out.println(alias);
                    }
                }, EnumSet.allOf(AliasType.class));

        instance.process(pageUrl, 1);

//        System.out.println(aliases);
    }


    //
    @Test
    public void testHatNotes_EscapedPipes() throws Exception {
        // This resource should contain exactly one alias of type HatNote
        final URL pageUrl = getClass().getResource("Wikipedia-Crashpad.xml");

        final List<Alias> aliases = Lists.newArrayList();
        final WikiAliasGenerator instance = new WikiAliasGenerator(
                new AliasHandler() {
                    @Override
                    public void handle(Alias alias) {
                        aliases.add(alias);
                        System.out.println(alias);
                    }
                }, EnumSet.allOf(AliasType.class));

        instance.process(pageUrl, 1);


//        HAT_NOTE/other uses

        // Find the index of the hat-note
        int i = 0;
        while (i < aliases.size()) {
            Alias alias = aliases.get(i);
            if (alias.getType() == AliasType.HAT_NOTE && alias.getSubType().equals("other uses"))
                break;
            ++i;
        }
        assertTrue("Couldn't find the target hat-note", i < aliases.size());
        final Alias alias = aliases.get(i);

        assertEquals("Unexpected alias source.", "Crash pad", alias.getSource());
        assertEquals("Unexpected alias target.", "Crashpad", alias.getTarget());

    }


    @Test
    public void testDisambiguationPages() throws Exception {
        // This resource should contain exactly one alias of type HatNote
        final URL pageUrl = getClass().getResource("Wikipedia-Amp.xml");

        final List<Alias> aliases = Lists.newArrayList();
        final WikiAliasGenerator instance = new WikiAliasGenerator(
                new AliasHandler() {
                    @Override
                    public void handle(Alias alias) {
                        aliases.add(alias);
                        System.out.println(alias);
                    }
                }, EnumSet.allOf(AliasType.class));

        instance.process(pageUrl, 1);

        System.out.println(aliases);
    }

}
