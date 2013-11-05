package eu.monnetproject.clesa.processor.wiki.abstracts;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

import eu.monnetproject.clesa.core.OTDF.OTDFFile;
import eu.monnetproject.clesa.core.OTDF.OTDFXmlReader;
import eu.monnetproject.clesa.core.OTDF.StringFeature;
import eu.monnetproject.clesa.core.lang.Language;
import eu.monnetproject.clesa.lucene.basic.AnalyzerFactory;
import eu.monnetproject.clesa.lucene.basic.Indexer;



/**
 *  
 * @author kasooja 
 */

public class MultiLingualAbstractsOTDFIndexer {	

	private final double BUFFERRAMSIZE = 256.0;
	private static Properties config =  new Properties();
	private String indexDirPath;
	private PerFieldAnalyzerWrapper analyzers;
	private String OTDFXmlFileToRead;
	private Set<Language> languages = new HashSet<Language>();
	private Set<Language> languagesCopy;
	private Indexer indexer;

	public MultiLingualAbstractsOTDFIndexer(){
		loadConfig();
		createPerFieldAnalyzer();
		openWriter();
	}

	public static Analyzer getAnalyzer(Language language){
		return AnalyzerFactory.getAnalyzer(language);
	}

	public void createPerFieldAnalyzer(){
		Map<String, Analyzer> fieldAnalyzerMap = new HashMap<String, Analyzer>();
		for(Language language : languages) {
			String topicContentFieldName = MultiLingualAbstractOTDFLucDocCreator.Fields.getLanguageTopicContentField(language);
			Analyzer analyzer = getAnalyzer(language);
			if(analyzer!=null)
				fieldAnalyzerMap.put(topicContentFieldName, analyzer);
		}
		analyzers = new PerFieldAnalyzerWrapper(new StandardAnalyzer(Version.LUCENE_36), fieldAnalyzerMap);
	}

	private void openWriter() {		
		try {
			IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36, analyzers);
			config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
			Directory index = getIndex(indexDirPath);
			if(IndexReader.indexExists(index)) 
				config.setOpenMode(IndexWriterConfig.OpenMode.APPEND);			
			config.setRAMBufferSizeMB(BUFFERRAMSIZE);
			indexer = new Indexer(config, index);
		}
		catch (IOException e) {
			e.printStackTrace();
		}			
	}

	private void loadConfig() {
		try {
			config.load(new FileInputStream("load/eu.monnetproject.clesa.processor.wiki.abstracts.MultiLingualAbstractsOTDFIndexer.properties"));
			indexDirPath = config.getProperty("indexDirPathToWrite");
			OTDFXmlFileToRead = config.getProperty("OTDFXmlFileToRead");
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

	private Set<Language> findAvailableLanguages(List<StringFeature> features) {
		Set<Language> languages = new HashSet<Language>();
		for(StringFeature feature : features) {
			String featureName = feature.getFeatureName();
			if(featureName.contains(MultiLingualAbstractsOTDFProcessor.Features.getFeatureNameAbstract())){
				String langCode = featureName.replace(MultiLingualAbstractsOTDFProcessor.Features.getFeatureNameAbstract(), "").trim();
				languages.add(Language.getByIso639_1(langCode));		
			}
		}
		return languages;
	}

	private void indexXml() {
		OTDFXmlReader reader = new OTDFXmlReader(OTDFXmlFileToRead);
		Iterator<OTDFFile> iterator = null;
		try {
			iterator = reader.getIterator();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(iterator != null){
			MultiLingualAbstractOTDFLucDocCreator lucDocCreator = new MultiLingualAbstractOTDFLucDocCreator();	
			int i = 0;
			while(iterator.hasNext()){
				OTDFFile file = iterator.next();
				Set<Language> abstractLanguages = findAvailableLanguages(file.getFeatures());
				languagesCopy.removeAll(abstractLanguages);				
				if(languagesCopy.isEmpty()){
					lucDocCreator.addUriField(file.getFeatureValue(AbstractsOTDFProcessor.Features.URI_EN.toString()));							
					lucDocCreator.addTopic(file.getFeatureValue(MultiLingualAbstractsOTDFProcessor.Features.Title.toString()));				
					for(Language language : abstractLanguages) 					
						lucDocCreator.addLanguageTopicContentField(language, file.getFeatureValue(MultiLingualAbstractsOTDFProcessor.Features.getFeatureNameAbstract(language)));
					indexer.addDoc(lucDocCreator.getLucDoc());
					System.out.println(++i);					
				}
				languagesCopy = new HashSet<Language>(languages);
				lucDocCreator.reset();				
			}
		}
		reader.close();
		indexer.closeIndexer();
	}

	private Directory getIndex(String indexPath) {
		Directory index = null;
		try {
			index = new SimpleFSDirectory(new File(indexPath));
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return index;
	}

	public static void main(String[] args) {	
		MultiLingualAbstractsOTDFIndexer indexer = new MultiLingualAbstractsOTDFIndexer();
		System.out.println("Indexing Started");		
		indexer.indexXml();
		System.out.println("Indexing Finished");
	}

}
