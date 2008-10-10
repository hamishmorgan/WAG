/**
 * @author delip
 * Data structures for a wikipedia page
 */
import java.util.Vector;


public class WikiPage {
	
	private String title = null;
	private WikiTextParser wikiTextParser = null;
	private String id = null;
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public void setWikiText(String wtext) {
		wikiTextParser = new WikiTextParser(wtext);
	}
	
	public String getTitle() {
		return title;
	}

	public boolean isDisambiguationPage() {
		return title.contains("(disambiguation)");
	}
	
	public String getWikiText() {
		return wikiTextParser.getText();
	}
	
	public boolean isRedirect() {
		return wikiTextParser.isRedirect();
	}
	
	public String getRedirectPage() {
		return wikiTextParser.getRedirectText();
	}
	
	public String getText() {
		return wikiTextParser.getPlainText();
	}
	
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
