package eu.monnetproject.clesa.core.lang;


/**
 * An exception thrown if the language code is invalid or unknown
 *
 * @author John McCrae
 */
public class UnsupportedLanguageException extends Exception {

    private static final long serialVersionUID = 1L;

    public UnsupportedLanguageException(String str) {
        super(str);
    }

    public UnsupportedLanguageException(Language lang) {
        super("The language " + lang.getName() + " is not supported.");
    }
}
