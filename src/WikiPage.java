/**
 * @author delip
 * Data structures for a wikipedia page
 */
import java.util.Vector;


public class WikiPage {
	
	private String title = null;
	private WikiTextParser wikiText = null;
	
	private String redirect = null;
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public void setWikiText(String wtext) {
		wikiText = new WikiTextParser(wtext);
	}
	
	public String getTitle() {
		return title;
	}
	
	public boolean isDisambiguationPage() {
		return title.contains("(disambiguation)");
	}
	
	public String getWikiText() {
		return wikiText.getText();
	}
	
	public boolean isRedirect() {
		return wikiText.isRedirect();
	}
	
	public String getText() {
		return wikiText.getPlainText();
	}
	
	public Vector<String> getCategories() {
		return wikiText.getCategories();
	}
}
