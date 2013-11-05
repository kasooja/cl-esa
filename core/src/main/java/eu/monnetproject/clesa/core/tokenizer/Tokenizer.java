package eu.monnetproject.clesa.core.tokenizer;


import java.util.List;

import eu.monnetproject.clesa.core.lang.Script;
import eu.monnetproject.clesa.core.token.Token;


/**
 * Interface to a tokenizer
 * @author John McCrae
 */
public interface Tokenizer {
    /**
     * Tokenize a single string
     * @param input The string to tokenize
     * @return A list of tokens
     */
	List<Token> tokenize(String input);
	
	/**
	 * Get the script the tokenizer supports
	 */
	Script getScript();
}
