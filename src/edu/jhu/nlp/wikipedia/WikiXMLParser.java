package edu.jhu.nlp.wikipedia;
/**
 * A simple API to handle Wikipedia XML dumps
 * @author delip
 *
 */

import org.apache.tools.bzip2.CBZip2InputStream;
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
	private DOMParser domParser = new DOMParser();
	private static String FEATURE_URI = 
		"http://apache.org/xml/features/dom/defer-node-expansion";
	private Vector<WikiPage> pageList = null;
	private PageCallbackHandler pageHandler = null;
		
	public WikiXMLParser(String fileName){
		wikiXMLFile = fileName;
	}
	
	public void setPageCallback(PageCallbackHandler handler) {
		pageHandler = handler;
	}
	
	public WikiPageIterator getIterator() throws Exception {
		if(pageHandler != null) throw new Exception("page callback found. Cannot iterate.");
		return new WikiPageIterator(pageList);
	}
	
	public void parse()  throws Exception  {
		
		if(pageHandler == null)
			pageList = new Vector<WikiPage>();
		
		domParser.setFeature(FEATURE_URI, true);
		BufferedReader br = null;
		
		if(wikiXMLFile.endsWith(".gz")) {
			br = new BufferedReader(new InputStreamReader(
					new GZIPInputStream(new FileInputStream(wikiXMLFile))));
		} else if(wikiXMLFile.endsWith(".bz2")) {
			FileInputStream fis = new FileInputStream(wikiXMLFile);
			byte [] ignoreBytes = new byte[2];
			fis.read(ignoreBytes); //"B", "Z" bytes from commandline tools
			br = new BufferedReader(new InputStreamReader(
					new CBZip2InputStream(fis)));
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
}
