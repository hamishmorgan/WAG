package edu.jhu.nlp.wikipedia;

import org.apache.tools.bzip2.CBZip2InputStream;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.SAXParser;

/**
 * 
 * A SAX Parser for Wikipedia XML dumps.  This parser is event driven, so it
 * can't provide a page iterator.
 * 
 * @author Jason Smith
 *
 */
public class WikiXMLSAXParser implements WikiXMLParser {
	
	private String wikiXMLFile = null;
	private XMLReader xmlReader;
	private PageCallbackHandler pageHandler = null;
		
	public WikiXMLSAXParser(String fileName){
		wikiXMLFile = fileName;
		try {
			xmlReader = XMLReaderFactory.createXMLReader();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Set a callback handler. The callback is executed every time a
	 * page instance is detected in the stream. Custom handlers are
	 * implementations of {@link PageCallbackHandler}
	 * @param handler
	 * @throws Exception
	 */
	public void setPageCallback(PageCallbackHandler handler) throws Exception {
		pageHandler = handler;
	}
	
	/**
	 * The main parse method.
	 * @throws Exception
	 */
	public void parse()  throws Exception  {
		
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
		
		xmlReader.setContentHandler(new SAXPageCallbackHandler(pageHandler));
		xmlReader.parse(new InputSource(br));
	}
}
