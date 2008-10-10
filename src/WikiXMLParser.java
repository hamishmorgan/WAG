/**
 * A simple API to handle Wikipedia XML dumps
 * @author delip
 *
 */

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import java.io.IOException;




public class WikiXMLParser {
	
	String wikiXMLFile = null;
	boolean useGZip = false;
	
	public WikiXMLParser(String fileName) {
		wikiXMLFile = fileName;
		if(wikiXMLFile.endsWith(".gz")) useGZip = true;
	}
	
	void process() throws Exception {
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if(args.length != 1) {
			System.err.println("Usage: Parser <XML-FILE>");
			System.exit(-1);
		}
		
		WikiXMLParser wxp = new WikiXMLParser(args[0]);
		
		try {
			wxp.process();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
