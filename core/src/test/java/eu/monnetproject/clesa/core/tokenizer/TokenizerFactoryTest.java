package eu.monnetproject.clesa.core.tokenizer;

import java.util.List;

import org.junit.*;

import eu.monnetproject.clesa.core.lang.Language;
import eu.monnetproject.clesa.core.lang.Script;
import eu.monnetproject.clesa.core.tokenizer.latin.LatinTokenizerFactory;
import eu.monnetproject.clesa.core.token.Token;
import static org.junit.Assert.*;


public class TokenizerFactoryTest{

	public TokenizerFactoryTest(){

	}

	@BeforeClass
	public static void setUpClass() throws Exception {
	}	
	

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	/**
	 * Test of Latin Tokenizer Factory.
	 */
	@Test
	public void testLatinTokenizerFactory() {
		TokenizerFactory tokenizerFactory = new LatinTokenizerFactory();
		Language lang = Language.ENGLISH;
		Script script = Script.LATIN;
		final Script[] knownScriptsForLanguage = Script.getKnownScriptsForLanguage(lang);
		if (knownScriptsForLanguage.length > 0) {
			script = knownScriptsForLanguage[0];
		}
		Tokenizer tokenizer = tokenizerFactory.getTokenizer(script);
		List<Token> tokens = tokenizer.tokenize("Hello World");
		assertEquals("first token by tokenizer is not correct", "World", tokens.get(1).getValue());
	}

}
