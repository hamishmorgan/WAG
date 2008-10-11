package edu.jhu.nlp.wikipedia;

import java.util.Vector;

public class WikiPageIterator {
	
	private int currentPage = 0;
	private int lastPage = 0;
	Vector<WikiPage> pageList = null;
	
	public WikiPageIterator(Vector<WikiPage> list) {
		pageList = list;
		if(pageList != null)
		  lastPage = pageList.size();
	}
	
	public boolean hasMorePages() {
		return (currentPage < lastPage);
	}
	
	public WikiPage nextPage() {
		if(hasMorePages())
			return pageList.elementAt(currentPage++); 
		return null;
	}
}
