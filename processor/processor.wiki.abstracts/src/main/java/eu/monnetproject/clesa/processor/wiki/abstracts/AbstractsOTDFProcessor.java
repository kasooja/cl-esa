package eu.monnetproject.clesa.processor.wiki.abstracts;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;

import eu.monnetproject.clesa.core.OTDF.OTDFFile;
import eu.monnetproject.clesa.core.OTDF.OTDFXml;
import eu.monnetproject.clesa.core.OTDF.StringFeature;
import eu.monnetproject.clesa.core.lang.Language;
import eu.monnetproject.clesa.core.ontology.RDF;
import eu.monnetproject.clesa.core.tokenizer.Tokenizer;
import eu.monnetproject.clesa.core.utils.TextNormalizer;
import eu.monnetproject.clesa.reader.rdf.nt.NTFileReader;



/**
 *  
 * @author kasooja 
 */
public class AbstractsOTDFProcessor {	
	
	private static Properties config = new Properties();
	private String ntFilePath; 		
	private String languageISOCode; 
	private Language language; 
	private String xmlPath;
	
	
	public enum Features {
		Title, URI_EN, Abstract, LanguageISOCode;	
	}

	private Tokenizer tokenizer; 

	public AbstractsOTDFProcessor() {
		loadConfig();
		setTokenizer(language);	
	}

	private void setTokenizer(Language abstractLang) {
		tokenizer = TextNormalizer.getTokenizer(language);
	}
	
	private void loadConfig(){
		try {
			config.load(new FileInputStream("load/eu.monnetproject.clesa.processor.wiki.abstracts.AbstractsOTDFProcessor.properties"));
			ntFilePath = config.getProperty("DBpediaNTFilePathToRead");		
			languageISOCode = config.getProperty("AbstractLanguageISOCode");
			xmlPath = config.getProperty("OTDFXmlToWrite");
			language = Language.getByIso639_1(languageISOCode);		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	private String removeLanguageMark(String text) {
		String regex = "(@\\w+)( *)";
		return text.replaceAll(regex, " ");		
	}
	
	private static String getURI(String line){
		String URI = Arrays.asList(line.split(">")).iterator().next();
		return URI.replace("<", "").trim();
	}	
	
	public String normalize(String text) {
		text = text.toLowerCase();
		text = TextNormalizer.convertToUnicode(text);		
		text = TextNormalizer.joinTokens(tokenizer.tokenize(text));
		text = TextNormalizer.removePunctuations(text);
		text = text.replaceAll("\n", " ").trim();
		return text;	
	}
	
	public void processToXml() {				
		NTFileReader rdfReader = new NTFileReader(ntFilePath);
		OTDFXml xml = new OTDFXml(xmlPath);
		Iterator<RDF> rdfIter;
		try {
			rdfIter = rdfReader.getRDFIter();
			int i = 0;
			while(rdfIter.hasNext()){				
				RDF rdf = rdfIter.next();	
				String uri = getURI(rdf.getSub());
				String abstractText = rdf.getObj();
				
				abstractText = removeLanguageMark(abstractText);
				abstractText = normalize(abstractText);				
			
				String fileName = uri.substring(uri.lastIndexOf("/")+1);
				OTDFFile file = new OTDFFile();		
				file.addFeature(new StringFeature(Features.LanguageISOCode.toString(), language.getIso639_1()));
				file.addFeature(new StringFeature(Features.Title.toString(), fileName));				
				file.addFeature(new StringFeature(Features.URI_EN.toString(), uri));
				file.addFeature(new StringFeature(Features.Abstract.toString(), abstractText));				
				xml.addOTDF(file);
				System.out.println(i);
			}					
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
	
	public static void main(String[] args) {
		AbstractsOTDFProcessor processor = new AbstractsOTDFProcessor();			
		processor.processToXml();
	}
	
}
