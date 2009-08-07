package edu.jhu.nlp.wikipedia;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * For internal use only -- Used by the {@link WikiPage} class.
 * Can also be used as a stand alone class to parse wiki formatted text.
 * @author Delip Rao
 *
 */
public class WikiTextParser {
	
	private String wikiText = null;
	private Vector<String> pageCats = null;
	private Vector<String> pageLinks = null;
	private boolean redirect = false;
	private String redirectString = null;
	private static Pattern redirectPattern = 
	  Pattern.compile("#REDIRECT\\s+\\[\\[(.*?)\\]\\]");
	private boolean stub = false;
	private static Pattern stubPattern = Pattern.compile("\\-stub\\}\\}");
	
	public WikiTextParser(String wtext) {
		wikiText = wtext;		
		Matcher matcher = redirectPattern.matcher(wikiText);
		if(matcher.find()) {
			redirect = true;
			if(matcher.groupCount() == 1)
				redirectString = matcher.group(1); 
		}
		matcher = stubPattern.matcher(wikiText);
		stub = matcher.find();
	}
	
	public boolean isRedirect() {
		return redirect;
	}
	
	public boolean isStub() {
	  return stub;
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

	public Vector<String> getLinks() {
		if(pageLinks == null) parseLinks();
		return pageLinks;
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
	
	private void parseLinks() {
		pageLinks = new Vector<String>();  
		
		Pattern catPattern = Pattern.compile("\\[\\[(.*?)\\]\\]", Pattern.MULTILINE);
		Matcher matcher = catPattern.matcher(wikiText);
		while(matcher.find()) {
			String [] temp = matcher.group(1).split("\\|");
			if(temp == null || temp.length == 0) continue;
			String link = temp[0];
			if(link.contains(":") == false) {
				pageLinks.add(link);
			}
		}
	}

	public String getPlainText() {
		String text = wikiText.replaceAll("<ref>.*?</ref>", " ");
		text = text.replaceAll("</?.*?>", " ");
		text = text.replaceAll("\\{\\{.*?\\}\\}", " ");
		text = text.replaceAll("\\[\\[.*?:.*?\\]\\]", " ");
		text = text.replaceAll("\\[\\[(.*?)\\]\\]", "$1");
		text = text.replaceAll("\\s(.*?)\\|(\\w+\\s)", " $2");
		text = text.replaceAll("\\[.*?\\]", " ");
		text = text.replaceAll("\\'+", "");
		return text;
	}

	public InfoBox getInfoBox() {
		throw new UnsupportedOperationException();
	}

}
