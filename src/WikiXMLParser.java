/**
 * A simple API to handle Wikipedia XML dumps
 * @author delip
 *
 */

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Vector;
import java.util.zip.GZIPInputStream;


public class WikiXMLParser {
	
	private String wikiXMLFile = null;
	private boolean useGZip = false;
	private DOMParser domParser = new DOMParser();
	private static String FEATURE_URI = 
		"http://apache.org/xml/features/dom/defer-node-expansion";
	private Vector<WikiPage> pageList = new Vector<WikiPage>();
	private PageCallbackHandler pageHandler = null;
	
	public WikiXMLParser(String fileName){
		wikiXMLFile = fileName;
		if(wikiXMLFile.endsWith(".gz")) useGZip = true;
	}
	
	public void setPageCallback(PageCallbackHandler handler) {
		pageHandler = handler;
	}
	
	public void parse()  throws Exception  {
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
		Document doc = domParser.getDocument();
		NodeList pages = doc.getElementsByTagName("page");
		for(int i = 0; i < pages.getLength(); i++) {
			WikiPage wpage = new WikiPage();
			Node pageNode = pages.item(i);
			NodeList childNodes = pageNode.getChildNodes();
			for(int j = 0; j < childNodes.getLength(); j++) {
				Node child = childNodes.item(j);
				if(child.getNodeName().equals("title"))
					wpage.setTitle(child.getFirstChild().getNodeValue());
				else if(child.getNodeName().equals("id"))
					wpage.setID(child.getFirstChild().getNodeValue());
				else if(child.getNodeName().equals("revision")) {
					NodeList revchilds = child.getChildNodes();
					for(int k = 0; k < revchilds.getLength(); k++) {
						Node rchild = revchilds.item(k); 
						if(rchild.getNodeName().equals("text")) 
							wpage.setWikiText(rchild.getFirstChild().getNodeValue());
					}
				}
			}
			
			if(pageHandler != null) {
				pageHandler.process(wpage);
			} else pageList.add(wpage);
		}
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
		wxp.setPageCallback(new DemoHandler());
		try {
			wxp.parse();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
