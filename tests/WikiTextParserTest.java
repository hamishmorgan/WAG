import java.io.BufferedReader;
import java.io.FileReader;

import edu.jhu.nlp.wikipedia.WikiTextParser;


public class WikiTextParserTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String wikiText = readFile(args[0]);
		WikiTextParser wtp = new WikiTextParser(wikiText);
		System.err.println(wtp.getPlainText());
	}

	private static String readFile(String file) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			StringBuffer sb = new StringBuffer();
			String line = null;
			while((line = br.readLine()) != null) {
				sb.append(line);
				sb.append('\n');
			}
			br.close();
			return sb.toString();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
