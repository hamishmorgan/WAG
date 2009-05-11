package edu.jhu.nlp.wikipedia;

public class IteratorHandler extends PageCallbackHandler {

	private WikiXMLParser parser = null;
	
	public IteratorHandler(WikiXMLParser myParser) {
		parser = myParser;
	}
	
	public void process(WikiPage page) {
		parser.notifyPage(page);
	}

}
