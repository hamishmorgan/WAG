package edu.jhu.nlp;

import edu.jhu.nlp.wikipedia.WikiXMLDOMParser;
import org.junit.Test;

public class CallbackDemo {
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
			wxp.setPageCallback(new DemoHandler());
			wxp.parse();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}


    @Test
    public void testMainNewton() {
        main(new String[]{"src/test/resources/edu/jhu/nlp/newton.xml"});
    }
}
