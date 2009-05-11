package edu.jhu.nlp.wikipedia;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Semaphore;
import java.util.zip.GZIPInputStream;

import org.apache.tools.bzip2.CBZip2InputStream;
import org.xml.sax.InputSource;

/**
 * 
 * 
 * @author Delip Rao
 * @author Jason Smith
 *
 */
public abstract class WikiXMLParser extends Thread {
	
	private String wikiXMLFile = null;
	protected WikiPage currentPage = null;
	private Semaphore pageIteratorLock = null;
	
	public WikiXMLParser(String fileName){
		wikiXMLFile = fileName;
		pageIteratorLock = new Semaphore(1, true);
	}
	
	/**
	 * Set a callback handler. The callback is executed every time a
	 * page instance is detected in the stream. Custom handlers are
	 * implementations of {@link PageCallbackHandler}
	 * @param handler
	 * @throws Exception
	 */
	public abstract void setPageCallback(PageCallbackHandler handler) throws Exception;
	
	/**
	 * The main parse method.
	 * @throws Exception
	 */
	public abstract void parse() throws Exception;
	
	/**
	 * 
	 * @return an iterator to the list of pages
	 * @throws Exception
	 */
	public abstract WikiPageIterator getIterator() throws Exception;
	
	/**
	 * 
	 * @return An InputSource created from wikiXMLFile
	 * @throws Exception
	 */
	protected InputSource getInputSource() throws Exception
	{
		BufferedReader br = null;
		
		if(wikiXMLFile.endsWith(".gz")) {
			br = new BufferedReader(new InputStreamReader(
					new GZIPInputStream(new FileInputStream(wikiXMLFile))));
		} else if(wikiXMLFile.endsWith(".bz2")) {
			FileInputStream fis = new FileInputStream(wikiXMLFile);
			byte [] ignoreBytes = new byte[2];
			fis.read(ignoreBytes); //"B", "Z" bytes from commandline tools
			br = new BufferedReader(new InputStreamReader(
					new CBZip2InputStream(fis)));
		} else {
			br = new BufferedReader(new InputStreamReader( 
					new FileInputStream(wikiXMLFile)));
		}
		
		return new InputSource(br);
	}

	protected void notifyPage(WikiPage page) {
		acquireLock();
		currentPage = page;
	}

	private void acquireLock() {
		boolean keepTrying = true;
		while(keepTrying) {
			try {
				pageIteratorLock.acquire();
				keepTrying = false;
			} catch (InterruptedException e) {
				keepTrying = true;
				e.printStackTrace();

			}
		}
	}
	
	public void releaseLock() {
		// There should be exactly one thread waiting
		if(pageIteratorLock.getQueueLength() == 1)
			pageIteratorLock.release();
	}
	
}
