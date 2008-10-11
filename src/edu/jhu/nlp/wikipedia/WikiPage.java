package edu.jhu.nlp.wikipedia;

import java.util.Vector;

/**
 * Data structures for a wikipedia page.
 * 
 * @author Delip Rao
 *
 */
public class WikiPage {
	
	private String title = null;
	private WikiTextParser wikiTextParser = null;
	private String id = null;
	
	/**
	 * Set the page title. This is not intended for direct use.
	 * @param title
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	
	/**
	 * Set the wiki text associated with this page. 
	 * This setter also introduces side effects. This is not intended for direct use. 
	 * @param wtext wiki-formatted text
	 */
	public void setWikiText(String wtext) {
		wikiTextParser = new WikiTextParser(wtext);
	}
	
	/**
	 * 
	 * @return a string containing the page title.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * 
	 * @return true if this a disambiguation page.
	 */
	public boolean isDisambiguationPage() {
		return title.contains("(disambiguation)");
	}
	
	/**
	 * Use this method to get the wiki text associated with this page.
	 * Useful for custom processing the wiki text.
	 * @return a string containing the wiki text.
	 */
	public String getWikiText() {
		return wikiTextParser.getText();
	}
	
	/**
	 * 
	 * @return true if this is a redirection page
	 */
	public boolean isRedirect() {
		return wikiTextParser.isRedirect();
	}
	
	/**
	 * 
	 * @return the title of the page being redirected to. 
	 */
	public String getRedirectPage() {
		return wikiTextParser.getRedirectText();
	}
	
	/**
	 * 
	 * @return plain text stripped of all wiki formatting.
	 */
	public String getText() {
		return wikiTextParser.getPlainText();
	}
	
	/**
	 * 
	 * @return a list of categories the page belongs to, null if this a redirection/disambiguation page 
	 */
	public Vector<String> getCategories() {
		return wikiTextParser.getCategories();
	}

	public void setID(String id) {
		this.id = id;
	}

	
	public String getID() {
		return id;
	}
}
