package eu.monnetproject.clesa.processor.wiki.abstracts;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import eu.monnetproject.clesa.core.OTDF.OTDFFile;
import eu.monnetproject.clesa.core.OTDF.OTDFXml;
import eu.monnetproject.clesa.core.OTDF.OTDFXmlReader;
import eu.monnetproject.clesa.core.OTDF.StringFeature;
import eu.monnetproject.clesa.core.lang.Language;



/**
 *  
 * @author kasooja 
 */

public class MinNoOfWordsInAllFilter {

	private static Properties config = new Properties();
	private int minNoOfWordsInAll = 500;
	private int maxHowManyDocs;
	private Set<Language> languages = new HashSet<Language>();
	private Set<Language> languagesCopy;
	private String multiLingualOTDFXmlToRead;
	private String multiLingualOTDFXmlToWrite;

	public MinNoOfWordsInAllFilter(){
		loadConfig();	
	}

	private void loadConfig(){
		try {
			config.load(new FileInputStream("load/eu.monnetproject.clesa.processor.wiki.abstracts.MinNoOfWordsInAllFilter.properties"));
			minNoOfWordsInAll = Integer.parseInt(config.getProperty("minNoOfWordsInAll"));	
			maxHowManyDocs = Integer.parseInt(config.getProperty("maxHowManyDocs"));
			multiLingualOTDFXmlToRead = config.getProperty("multiLingualOTDFXmlToRead");
			multiLingualOTDFXmlToWrite = config.getProperty("multiLingualOTDFXmlToWrite");
			String[] languageCodes = config.getProperty("languages").split(";");
			for(String languageCode : languageCodes) 
				languages.add(Language.getByIso639_1(languageCode));
			languagesCopy = new HashSet<Language>(languages);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}

	private int getNoOfWords(String string){
		List<String> words = new ArrayList<String>();
		int pos = 0, end;
		while ((end = string.indexOf(' ', pos)) >= 0) {
			words.add(string.substring(pos, end));
			pos = end + 1;
		}
		return words.size();
	}

	private boolean containsReqLang(String featureName) {		
		Set<Language> tempLanguages = new HashSet<Language>(languagesCopy);
		for(Language language : tempLanguages) {
			if(featureName.contains(language.getIso639_1())) {
				languagesCopy.remove(language);
				return true;
			}
		}
		return false;
	}
	public void filterXml(){		
		int j = 0;
		OTDFXmlReader reader = new OTDFXmlReader(multiLingualOTDFXmlToRead);
		OTDFXml xmlToWrite = new OTDFXml(multiLingualOTDFXmlToWrite);
		Iterator<OTDFFile> iterator = null;
		try {
			iterator = reader.getIterator();
		} catch (IOException e) {
			e.printStackTrace();
		}
		int i = 0;
		if(iterator!=null) {
			while(iterator.hasNext()) {
				OTDFFile file = iterator.next();
				if(j>=maxHowManyDocs) 
					break;
				System.out.println(++i);
				List<StringFeature> features = file.getFeatures(); 
				boolean writeThisInNew = false;
				for(StringFeature feature : features) {
					String featureName = feature.getFeatureName();
					String featureValue = feature.getFeatureValue();
					if(featureName.contains(MultiLingualAbstractsOTDFProcessor.Features.getFeatureNameAbstract())){
						if(containsReqLang(featureName)){
							if(getNoOfWords(featureValue)>minNoOfWordsInAll)
								writeThisInNew = true;
							else 
								writeThisInNew = false;
						}
					}
				}
				if(writeThisInNew && languagesCopy.isEmpty()) {
					xmlToWrite.addOTDF(file);
					j++;
				}
				languagesCopy = new HashSet<Language>(languages);
				file.reset();
			}
			reader.close();
			xmlToWrite.close();
		}
	}

	public static void main(String[] args) {
		MinNoOfWordsInAllFilter filter = new MinNoOfWordsInAllFilter();
		filter.filterXml();
	}

}
