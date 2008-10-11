package edu.jhu.nlp.wikipedia;
import java.util.Vector;

/**
 * A very simple callback for demo
 * @author delip
 *
 */

public class DemoHandler implements PageCallbackHandler {

	public void process(WikiPage page) {
		
		//String redir = "";
		//if(page.isRedirect()) redir = " [" + page.getRedirectPage() + "] "; 
		//System.err.println(page.getTitle() + redir);
		
		if(page.getTitle().equals("Abraham Lincoln")) {
			Vector<String> cats = page.getCategories();
			for(int i = 0; i < cats.size(); i++) {
				System.err.println("\t -> " + cats.elementAt(i));
			}
		}
		
		
	}

}
