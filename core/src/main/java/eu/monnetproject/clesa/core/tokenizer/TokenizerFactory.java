package eu.monnetproject.clesa.core.tokenizer;

import eu.monnetproject.clesa.core.lang.Script;




/**
 * A factory for tokenizers
 * 
 * @author John McCrae
 */
public interface TokenizerFactory {
    Tokenizer getTokenizer(Script script);
}
