package eu.monnetproject.clesa.lucene.basic;


import java.lang.reflect.InvocationTargetException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.util.Version;

import eu.monnetproject.clesa.core.lang.Language;


/**
 *  
 * @author kasooja 
 */
public class AnalyzerFactory {

	private static String getAnalyzerClassName(Language language) {
		return "org.apache.lucene.analysis" + "." + language.getIso639_1() + "." + language.getName() + "Analyzer";		
	}	

	public static Analyzer getAnalyzer(Language language) {
		try {
			try {
				return (Analyzer) Class.forName(getAnalyzerClassName(language)).getConstructor(Version.class).newInstance(Version.LUCENE_36);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}	

}	

