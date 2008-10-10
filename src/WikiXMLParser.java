/**
 * A simple API to handle Wikipedia XML dumps
 * @author delip
 *
 */

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;


public class WikiXMLParser {
	
	private String wikiXMLFile = null;
	private boolean useGZip = false;
	private DOMParser domParser = new DOMParser();
	private static String FEATURE_URI = 
		"http://apache.org/xml/features/dom/defer-node-expansion";
	
	public WikiXMLParser(String fileName) throws Exception {
		wikiXMLFile = fileName;
		if(wikiXMLFile.endsWith(".gz")) useGZip = true;
		domParser.setFeature(FEATURE_URI, true);
		BufferedReader br = null;
		if(useGZip) {
			br = new BufferedReader(new InputStreamReader(
					new GZIPInputStream(new FileInputStream(wikiXMLFile))));
		} else {
			br = new BufferedReader(new InputStreamReader( 
					new FileInputStream(wikiXMLFile)));
		}
		domParser.parse(new InputSource(br));
	}
	
	void process() throws Exception {
		Document doc = domParser.getDocument();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if(args.length != 1) {
			System.err.println("Usage: Parser <XML-FILE>");
			System.exit(-1);
		}
		
		try {
			WikiXMLParser wxp = new WikiXMLParser(args[0]);
			wxp.process();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
