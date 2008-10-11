package edu.jhu.nlp.wikipedia;
/**
 * @author delip
 * Parser for wikipedia-formatted text
 */

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class WikiTextParser {
	
	private String wikiText = null;
	private Vector<String> pageCats = null;
	private boolean redirect = false;
	private String redirectString = null;

	public WikiTextParser(String wtext) {
		wikiText = wtext;
		Pattern redirectPattern = Pattern.compile("#REDIRECT\\s+\\[\\[(.*?)\\]\\]");
		Matcher matcher = redirectPattern.matcher(wikiText);
		if(matcher.find()) {
			redirect = true;
			if(matcher.groupCount() == 1)
				redirectString = matcher.group(1); 
		}
	}
	
	public boolean isRedirect() {
		return redirect;
	}
	
	public String getRedirectText() {
		return redirectString;
	}

	public String getText() {
		return wikiText;
	}

	public Vector<String> getCategories() {
		if(pageCats == null) parseCategories();
		return pageCats;
	}

	private void parseCategories() {
		pageCats = new Vector<String>();
		Pattern catPattern = Pattern.compile("\\[\\[Category:(.*?)\\]\\]", Pattern.MULTILINE);
		Matcher matcher = catPattern.matcher(wikiText);
		while(matcher.find()) {
			String [] temp = matcher.group(1).split("\\|");
			pageCats.add(temp[0]);
		}
	}

	public String getPlainText() {
		// TODO Auto-generated method stub
		return null;
	}

}
