package edu.jhu.nlp.wikipedia;

import java.util.Vector;

/**
 * 
 * A class to iterate the pages after the wikipedia XML file has been parsed with {@link WikiXMLDOMParser}.
 * @author Delip Rao
 * @see WikiXMLDOMParser
 *
 */
public class WikiPageIterator {
	
	private WikiXMLSAXParser parser = null;
	
	public WikiPageIterator(WikiXMLSAXParser parser) {
		this.parser = parser;
	}

	/**
	 * 
	 * @return true if there are more pages to be read 
	 * @throws Exception 
	 */
	public boolean hasMorePages() throws Exception {
		return parser.hasMorePages();
	}
	
	/**
	 * Advances the iterator by one position.
	 * @return a {@link WikiPage} 
	 */
	public WikiPage nextPage() {
		return parser.getCurrentPage();
	}
}
