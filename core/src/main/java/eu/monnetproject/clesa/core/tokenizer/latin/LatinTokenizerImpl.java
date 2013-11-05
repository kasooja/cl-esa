package eu.monnetproject.clesa.core.tokenizer.latin;

import java.util.*;
import java.io.*;

import eu.monnetproject.clesa.core.lang.Script;
import eu.monnetproject.clesa.core.tokenizer.Tokenizer;
import eu.monnetproject.clesa.core.token.Token;

/**
 * Roman Standard Tokenizer. This is based on Lucene's 
 *
 * @author John McCrae
 */
public class LatinTokenizerImpl implements Tokenizer {

	public LatinTokenizerImpl() {}

	private static final ArrayList<Script> supportedScripts = new ArrayList<Script>();

	static {
		supportedScripts.add(Script.LATIN);
		supportedScripts.add(Script.GREEK);
		supportedScripts.add(Script.CYRILLIC);
	}

	public Script getScript() {
		return Script.LATIN;
	}

	public List<Token> tokenize(String input) {
		try {
			List<Token> rval = new ArrayList<Token>();
			LatinTokenizerCC cc = new LatinTokenizerCC(new StringReader(input));
			while(true) {
				Token tk = cc.next();
				if(tk == null)
					break;
				if(tk.getValue() == null)
					continue;
				rval.add(tk);
			}
			return rval;
		} catch(IOException x) {
			x.printStackTrace();
			return Collections.EMPTY_LIST;
		} catch(ParseException x) {
			x.printStackTrace();
			return Collections.EMPTY_LIST;
		}
	}
	
}

