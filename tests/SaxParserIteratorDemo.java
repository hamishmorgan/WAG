
import edu.jhu.nlp.wikipedia.IteratorHandler;
import edu.jhu.nlp.wikipedia.PageCallbackHandler;
import edu.jhu.nlp.wikipedia.WikiPage;
import edu.jhu.nlp.wikipedia.WikiPageIterator;
import edu.jhu.nlp.wikipedia.WikiXMLParser;
import edu.jhu.nlp.wikipedia.WikiXMLParserFactory;

/**
 * 
 * @author Delip Rao
 *
 */
public class SaxParserIteratorDemo {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if(args.length != 1) {
			System.err.println("Usage: Parser <XML-FILE>");
			System.exit(-1);
		}
		
		WikiXMLParser wxsp = WikiXMLParserFactory.getSAXParser(args[0]);
		PageCallbackHandler handler = new IteratorHandler(wxsp);
		
		try {
			wxsp.setPageCallback(handler);
			wxsp.parse();
			WikiPageIterator it = wxsp.getIterator();
			while(it.hasMorePages()) {
				WikiPage page = it.nextPage();
				System.out.println(page.getID());
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
