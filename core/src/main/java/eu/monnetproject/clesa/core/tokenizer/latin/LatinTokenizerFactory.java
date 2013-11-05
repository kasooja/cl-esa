package eu.monnetproject.clesa.core.tokenizer.latin;

import eu.monnetproject.clesa.core.lang.Script;
import eu.monnetproject.clesa.core.tokenizer.Tokenizer;
import eu.monnetproject.clesa.core.tokenizer.TokenizerFactory;




/**
 *
 * @author jmccrae
 */
public class LatinTokenizerFactory implements TokenizerFactory {

    public Tokenizer getTokenizer(Script script) {
        if(script.equals(Script.LATIN)) {
            return new LatinTokenizerImpl();
        } else {
            return null;
        }
    }
    
}
