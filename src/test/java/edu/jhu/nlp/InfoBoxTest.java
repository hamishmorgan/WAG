package edu.jhu.nlp;

import edu.jhu.nlp.util.FileUtil;
import edu.jhu.nlp.wikipedia.InfoBox;
import edu.jhu.nlp.wikipedia.WikiTextParser;
import org.junit.Test;

/**
 * @author Delip Rao
 */
public class InfoBoxTest extends TestCase {
  public boolean doTest() {
    String wikiText = FileUtil.readFile("src/test/resources/edu/jhu/nlp/newton.xml");
    WikiTextParser parser = new WikiTextParser(wikiText);
    InfoBox infoBox = parser.getInfoBox();
    if(infoBox == null) System.err.println("null");
    else System.err.println(infoBox.dumpRaw());
    return false;
  }

  public static void main(String [] args) {
    InfoBoxTest test = new InfoBoxTest();
    test.doTest();
  }


    @Test
    public void testMain() {
        main(new String[]{});
    }
}
