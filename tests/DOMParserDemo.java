
import edu.jhu.nlp.wikipedia.WikiPageIterator;
import edu.jhu.nlp.wikipedia.WikiXMLDOMParser;

public class DOMParserDemo {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if(args.length != 1) {
			System.err.println("Usage: Parser <XML-FILE>");
			System.exit(-1);
		}
		
		WikiXMLDOMParser wxp = new WikiXMLDOMParser(args[0]);
		try {
			wxp.parse();
			WikiPageIterator it = wxp.getIterator();
			while(it.hasMorePages()) {
				System.err.println(it.nextPage().getTitle());
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
