package eu.monnetproject.clesa.core.utils;



import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import eu.monnetproject.clesa.core.lang.Language;
import eu.monnetproject.clesa.core.lang.Script;
import eu.monnetproject.clesa.core.token.Token;
import eu.monnetproject.clesa.core.tokenizer.Tokenizer;
import eu.monnetproject.clesa.core.tokenizer.TokenizerFactory;
import eu.monnetproject.clesa.core.tokenizer.latin.LatinTokenizerFactory;


/**
 *  
 * @author kasooja 
 */

public class TextNormalizer {

	private static TokenizerFactory tokenizerFactory = new LatinTokenizerFactory();
	
	
	
	public static Tokenizer getTokenizer(Language lang) {
		Script script = Script.LATIN;
		final Script[] knownScriptsForLanguage = Script.getKnownScriptsForLanguage(lang);
		if (knownScriptsForLanguage.length > 0) {
			script = knownScriptsForLanguage[0];
		}
		return tokenizerFactory.getTokenizer(script);
	}

	public static String convertToUnicode(String string) {
		char[] input = string.toCharArray();		
		return loadConvert(input, 0, input.length);
	}

	private static String loadConvert (char[] in, int off, int len) {
		char[] convtBuf = new char[0];
		if (convtBuf.length < len) {
			int newLen = len * 4;
			if (newLen < 0) {
				newLen = Integer.MAX_VALUE;
			}
			convtBuf = new char[newLen];
		}
		char aChar;
		char[] out = convtBuf;
		int outLen = 0;

		int end = off + len;

		while (off < end) {
			int q = 0;			
			aChar = in[off++];
			if (aChar == '\\') {  
				boolean flag = false; 
				char[] notUniCode = new char[15];
				notUniCode[q++] = aChar;

				// "/" at the end of file  (not added in out string)
				if(off<in.length) {
					aChar = in[off++];
					notUniCode[q++] = aChar;										
				} 		

				if(aChar == 'u') {					
					int value=0;
					for (int i=0; i<4; i++) {				
						if(off<in.length)
							aChar = in[off++];
						else
							aChar = ' ';

						notUniCode[q++] = aChar;						
						switch (aChar) {
						case '0': case '1': case '2': case '3': case '4':
						case '5': case '6': case '7': case '8': case '9':
							value = (value << 4) + aChar - '0';
							break;
						case 'a': case 'b': case 'c':
						case 'd': case 'e': case 'f':
							value = (value << 4) + 10 + aChar - 'a';
							break;
						case 'A': case 'B': case 'C':
						case 'D': case 'E': case 'F':
							value = (value << 4) + 10 + aChar - 'A';
							break;
						default:
							flag = true;
							notUniCode[q++] = '\0';
							q = 0;
							char a = notUniCode[q];
							while((a!='\0')){
								out[outLen++] = a;								
								a = notUniCode[++q];
							}							
						}
						if(flag == true) 
							break;
					}
					if(flag == false) 
						out[outLen++] = (char)value;
				} else {
					if (aChar == 't') aChar = '\t';
					else if (aChar == 'r') aChar = '\r';
					else if (aChar == 'n') aChar = '\n';
					else if (aChar == 'f') aChar = '\f';
					out[outLen++] = aChar;
				}
			} else {
				out[outLen++] = aChar;
			}
		}
		return new String (out, 0, outLen);
	}

	public static String deAccent(String str) {
		String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD); 
		Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
		return pattern.matcher(nfdNormalizedString).replaceAll("");
	}

	public static String joinTokens(List<Token> list) {
		StringBuffer buffer = new StringBuffer();
		for(Token token : list) {
			buffer.append(token + " ");
		}
		return buffer.toString().trim();		
	}

	public static String joinStrings(List<String> list) {
		StringBuffer buffer = new StringBuffer();
		for(String token : list) {
			buffer.append(token + " ");
		}
		return buffer.toString().trim();		
	}

	public static String removePunctuations(String text){
		text = text.replaceAll("\\p{P}", " ");
		return text;
	}

	public static String removeBooleans(String text) {		
		text = text.replace("AND", " ");
		text = text.replace("NOT", " ");
		text = text.replace("OR", " ");
		return text;
	}

	//		String[] split = text.replace("\n", " ").split(" ");
	//		text = text.replace("AND", " ");
	//		text = text.replace("NOT", " ");
	//		text = text.replace("OR", " ");

	List<String> output = new ArrayList<String>();
	//		for (int i=0;i<split.length;i++){
	//			String temp = split[i];
	//			if (!temp.equals("") && !temp.equals("\n") && !temp.equals("AND") && !temp.equals("NOT") &&!temp.equals("OR"))
	//				output.add(temp);
	//		}
	//		return joinStrings(output);
	//	}	

}
