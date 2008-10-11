
import edu.jhu.nlp.wikipedia.*;

public class CallbackDemo {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if(args.length != 1) {
			System.err.println("Usage: Parser <XML-FILE>");
			System.exit(-1);
		}
		
		WikiXMLParser wxp = new WikiXMLParser(args[0]);
		wxp.setPageCallback(new DemoHandler());
		try {
			wxp.parse();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
