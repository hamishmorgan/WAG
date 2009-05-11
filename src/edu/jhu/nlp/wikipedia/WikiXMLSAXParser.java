package edu.jhu.nlp.wikipedia;

import java.io.IOException;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * 
 * A SAX Parser for Wikipedia XML dumps.  
 * 
 * @author Jason Smith
 *
 */
public class WikiXMLSAXParser extends WikiXMLParser {

	private XMLReader xmlReader;
	private PageCallbackHandler pageHandler = null;

	public WikiXMLSAXParser(String fileName){
		super(fileName);
		try {
			xmlReader = XMLReaderFactory.createXMLReader();
			pageHandler = new IteratorHandler(this);
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

	
	private boolean parserInitialized = false;
	/**
	 * The main parse method.
	 */
	public void parse() {
		// Start the thread that will actually parse. 
		// This will call the run() method internally.
		start(); 
		while(parserInitialized == false);
	}
	
	public void run()  {
		xmlReader.setContentHandler(new SAXPageCallbackHandler(pageHandler));
		try {
			xmlReader.parse(getInputSource());
		} catch (Exception e) {
			e.printStackTrace();
		}
		parserInitialized = true;
	}

	@Override
	public WikiPageIterator getIterator() throws Exception {
		if(!(pageHandler instanceof IteratorHandler)) {
			throw new Exception("Custom page callback found. Will not iterate.");
		}
		return new WikiPageIterator(this);
	}

	public boolean hasMorePages() throws Exception {
		IteratorHandler hdlr = (IteratorHandler)pageHandler;
		return hdlr.hasMorePages();
	}

	public WikiPage getCurrentPage() {
		WikiPage tmp = currentPage;
		releaseLock();
		return tmp;
	}
}
