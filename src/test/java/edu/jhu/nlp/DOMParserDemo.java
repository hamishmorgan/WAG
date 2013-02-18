package edu.jhu.nlp;

import edu.jhu.nlp.wikipedia.WikiPageIterator;
import edu.jhu.nlp.wikipedia.WikiXMLParser;
import edu.jhu.nlp.wikipedia.WikiXMLParserFactory;
import org.junit.Test;

/**
 * 
 * @author Delip Rao
 *
 */
public class DOMParserDemo {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if(args.length != 1) {
			System.err.println("Usage: Parser <XML-FILE>");
			System.exit(-1);
		}
		
		WikiXMLParser wxp = WikiXMLParserFactory.getDOMParser(args[0]);
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


    @Test
    public void testMainNewton() {
        main(new String[]{"src/test/resources/edu/jhu/nlp/newton.xml"});
    }
}
