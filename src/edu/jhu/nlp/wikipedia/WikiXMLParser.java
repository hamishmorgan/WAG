package edu.jhu.nlp.wikipedia;

/**
 * 
 * 
 * @author Delip Rao
 *
 */
public interface WikiXMLParser {
	
	/**
	 * Set a callback handler. The callback is executed every time a
	 * page instance is detected in the stream. Custom handlers are
	 * implementations of {@link PageCallbackHandler}
	 * @param handler
	 * @throws Exception
	 */
	public void setPageCallback(PageCallbackHandler handler) throws Exception;
	
	/**
	 * The main parse method.
	 * @throws Exception
	 */
	public void parse()  throws Exception;
}
