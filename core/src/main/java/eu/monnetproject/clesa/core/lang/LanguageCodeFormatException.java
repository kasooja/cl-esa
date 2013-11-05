package eu.monnetproject.clesa.core.lang;


/**
 * Exception thrown if the language code is not correctly formatted.
 *
 * @author John McCrae
 */
public class LanguageCodeFormatException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of
     * <code>LanguageCodeFormatException</code> without detail message.
     */
    public LanguageCodeFormatException(String code) {
        super("Invalid language code format: " + code);
    }
}
