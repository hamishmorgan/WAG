import edu.jhu.nlp.util.FileUtil;
import edu.jhu.nlp.wikipedia.WikiTextParser;


public class WikiTextParserTest extends TestCase {
  
  public static boolean testDisambiguationPage() {
    String lexingtonWikiText = FileUtil.readFile("data/Lexington.wiki");
    WikiTextParser wtp = new WikiTextParser(lexingtonWikiText);
    return wtp.isDisambiguationPage();
  }

  public static void demoGetText(String wikiFile) {
    String wikiText = FileUtil.readFile(wikiFile);
    WikiTextParser wtp = new WikiTextParser(wikiText);
    System.err.println(wtp.getPlainText());
  }
  
	/**
	 * @param args
	 */
	public static void main(String[] args) {
	  WikiTextParserTest test = new WikiTextParserTest();
	  test.check("Disambiguation Page", testDisambiguationPage());
	}

}
