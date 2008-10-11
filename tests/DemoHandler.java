
import java.util.Vector;

import edu.jhu.nlp.wikipedia.*;

/**
 * A very simple callback for demo. 																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																						qa
 * 
 * @author Delip Rao
 * @see PageCallbackHandler
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
