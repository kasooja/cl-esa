package eu.monnetproject.core.tokenizer.latin.ptb;




import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import eu.monnetproject.clesa.core.lang.Script;
import eu.monnetproject.clesa.core.token.Token;
import eu.monnetproject.clesa.core.token.TokenImpl;
import eu.monnetproject.clesa.core.tokenizer.Tokenizer;

/**
 * Penn TreeBank Tokenization, based on original sed file
 * 
 * http://www.cis.upenn.edu/~treebank/tokenizer.sed
 * 
 * @author John McCrae
 */
public class PTBTokenizer implements Tokenizer {

    private final void replaceAll(StringBuilder sb, String match, String replace) {
        final Pattern pattern = Pattern.compile(match);
        Matcher matcher = pattern.matcher(sb);
        int start = 0;
        while(matcher.find(start)) {
            final String replace2 = matcher.groupCount() > 0 ? 
                    matcher.groupCount() > 1 ?
                  //  matcher.groupCount() > 2 ?
                    //  replace.replaceAll("\\\\1", matcher.group(1)).replaceAll("\\\\2", matcher.group(2)).replaceAll("\\\\3", matcher.group(3)) :
                      replace.replace("\\1", matcher.group(1)).replace("\\2", matcher.group(2)) :
                      replace.replace("\\1", matcher.group(1)) :
                      replace;
            sb.replace(matcher.start(), matcher.end(), replace2);
            start = matcher.start() + replace.length();
            if(start >= sb.length()) {
                break;
            } else {
                matcher = pattern.matcher(sb);
            }
        }
    }

	public List<Token> tokenize(String input) {
      final StringBuilder sb = new StringBuilder(input);
      // Generic Latin Charset rules
      replaceAll(sb,"^\"","`` ");
      replaceAll(sb,"(?<=[ \\(\\[\\{\\<])\"","`` ");
      replaceAll(sb,"\\.\\.\\."," ... ");
      replaceAll(sb,"([,;:@#$%&])"," \\1 ");
      replaceAll(sb,"([^\\.])\\.([\\]\\)\\}\\>\"'\\s])","\\1 . \\2 ");
      replaceAll(sb,"([^\\.])\\.$","\\1 .");
      replaceAll(sb,"([\\?\\!])"," \\1 ");
      replaceAll(sb,"([\\]\\[\\(\\)\\{\\}\\<\\>])"," \\1 ");
      replaceAll(sb,"--"," -- ");
      replaceAll(sb,"\""," '' ");
      replaceAll(sb,"([^'])' ","\\1 ' ");
      
      // English specific rules
      replaceAll(sb,"'([sSmMdD]) "," '\\1 ");
      replaceAll(sb,"'ll "," 'll ");
      replaceAll(sb,"'re "," 're ");
      replaceAll(sb,"'ve "," 've ");
      replaceAll(sb,"n't "," n't ");
      replaceAll(sb,"'LL "," 'LL ");
      replaceAll(sb,"'RE "," 'RE ");
      replaceAll(sb,"'VE "," 'VE ");
      replaceAll(sb,"N'T "," N'T ");
      replaceAll(sb,"([Cc])annot ","\\1an not ");
      replaceAll(sb,"([Dd])'ye ","\\' ye ");
      replaceAll(sb,"([Gg])imme ","\\1im me ");
      replaceAll(sb,"([Gg])onna ","\\1on na ");
      replaceAll(sb,"([Gg])otta ","\\1ot ta ");
      replaceAll(sb,"([Ll])emme ","\\1em me ");
      replaceAll(sb,"([Mm])ore'n ","\\1ore 'n ");
      replaceAll(sb,"'([Tt])is ","'\\1 is ");
      replaceAll(sb,"'([Tt])was ","'\\1 was ");
      replaceAll(sb,"([Ww])anna ","\\1an na ");
      replaceAll(sb,"([Ww])haddya ","\\1ha dd ya ");
      replaceAll(sb,"([Ww])hatcha ","\\1ha t cha ");
      
      // Extra Unicode rules
      replaceAll(sb,"\\s([\\p{Ps}\\p{Pi}])(\\S)"," \\1 \\2");
      replaceAll(sb,"^([\\p{Ps}\\p{Pi}])(\\S)","\\1 \\2");
      replaceAll(sb,"\\s([\\p{Ps}\\p{Pi}])$"," \\1");
      replaceAll(sb,"(\\S)([\\p{Pe}\\p{Pf}\u201f])\\s","\\1 \\2 ");
      replaceAll(sb,"^([\\p{Pe}\\p{Pf}\u201f])\\s","\\1 ");
      replaceAll(sb,"(\\S)([\\p{Pe}\\p{Pf}\u201f])$","\\1 \\2");
      replaceAll(sb,"\\p{C}",""); // remove "control" characters
      
      // Clean up extra
      //replaceAll(sb,"\\s{2,}"," ");
      //replaceAll(sb,"^ +","");
      //replaceAll(sb," +$","");
      
      // Maximum Token length = 30 (Mostly junky URLs etc.)
      replaceAll(sb,"\\S{30,}","");
      
      // Format into a token list \\p{Z} is the Unicode generalization of \\s
      List<String> tokenStrings = Arrays.asList(sb.toString().split("\\p{Z}+"));
      List<Token> tokens = new ArrayList<Token>();
      for(String tokenString : tokenStrings) {
    	  tokens.add(new TokenImpl(tokenString));    	  
      }      
      return tokens;
	}

	public Script getScript() {
		return Script.LATIN;
	}

}