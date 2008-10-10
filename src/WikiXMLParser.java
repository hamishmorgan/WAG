
public class WikiXMLParser {
	

	public WikiXMLParser(String fileName) {
	}
	
	void process() throws Exception {
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length != 1) {
			System.err.println("Usage: Parser <XML-FILE>");
			System.exit(-1);
		}
		
		WikiXMLParser wxp = new WikiXMLParser(args[0]);
		
		try {
			wxp.process();
		}catch(Exception e) {
			e.printStackTrace();
		}

	}

}
