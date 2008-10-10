/**
 * @author delip
 * Parser for wikipedia-formatted text
 */

import java.util.Vector;


public class WikiTextParser {
	
	private String wikiText = null;
	private Vector<String> pageCats = null;
	private Vector<String> pageLinks = null;

	public WikiTextParser(String wtext) {
		wikiText = wtext;
	}

	public String getText() {
		// TODO Auto-generated method stub
		return null;
	}

	public Vector<String> getCategories() {
		// TODO Auto-generated method stub
		if(pageCats == null) parseCategories();
		return pageCats;
	}

	private void parseCategories() {
		// TODO Auto-generated method stub
		
	}

	public String getPlainText() {
		// TODO Auto-generated method stub
		return null;
	}

}
