package eu.monnetproject.clesa.processor.wiki.articles;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

import eu.monnetproject.clesa.core.lang.Language;
import eu.monnetproject.clesa.lucene.basic.AnalyzerFactory;
import eu.monnetproject.clesa.lucene.basic.Indexer;
import eu.monnetproject.clesa.lucene.basic.Reader;

public class IndexCompressor {

	private static final double BUFFERRAMSIZE = 2048.0;
	private static PerFieldAnalyzerWrapper analyzers;
	private static Indexer indexer;
	private Set<Language> languages = new HashSet<Language>();
	private String indexDirPathToWrite;
	private String indexDirPathToRead;
	private Properties config = new Properties();

	public IndexCompressor() {
		loadConfig();
		createPerFieldAnalyzer(languages);	
		openWriter();
	}			

	private void loadConfig() {
		try {
			config.load(new FileInputStream("load/eu.monnetproject.clesa.processor.wiki.articles.IndexCompressor.properties"));			
			indexDirPathToWrite = config.getProperty("indexDirPathToWrite");
			indexDirPathToRead = config.getProperty("indexDirPathToRead");
			String[] languageCodes = config.getProperty("languages").split(";");
			for(String languageCode : languageCodes) 
				languages.add(Language.getByIso639_1(languageCode.trim().toLowerCase()));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}			

	private void compress(){
		CompressedMultiLingualLucDocCreator docCreator = new CompressedMultiLingualLucDocCreator();			
		Reader reader = new Reader(getIndex(indexDirPathToRead));
		int totalDocuments = reader.totalDocuments();
		for(int i = 0; i<totalDocuments; i++){
			System.out.println(i);	
			Document doc = reader.getDocumentWithDocId(i);
			String topic = doc.get(CompressedMultiLingualLucDocCreator.Fields.Topic.toString());
			docCreator.addTopic(topic);
			String uri = doc.get(CompressedMultiLingualLucDocCreator.Fields.URI_EN.toString());
			docCreator.addUriField(uri);
			for(Language lang : languages){
				String langTopicContent = doc.get(CompressedMultiLingualLucDocCreator.Fields.getLanguageTopicContentField(lang));
				docCreator.addLanguageTopicContentField(lang, langTopicContent);				
			}
			indexer.addDoc(docCreator.getLucDoc());
			docCreator.reset();
		}
		indexer.closeIndexer();
		reader.closeIndex();
	}

	public static void main(String[] args) {
		IndexCompressor indexCompressor = new IndexCompressor();
		indexCompressor.compress();
	}

	public Analyzer getAnalyzer(Language language){
		return AnalyzerFactory.getAnalyzer(language);
	}

	public void createPerFieldAnalyzer(Set<Language> languagesDone){
		Map<String, Analyzer> fieldAnalyzerMap = new HashMap<String, Analyzer>();
		for(Language language : languagesDone) {
			String topicContentFieldName = MultiLingualArticleOTDFLucDocCreator.Fields.getLanguageTopicContentField(language);
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
			Directory index = getIndex(indexDirPathToWrite);
			if(IndexReader.indexExists(index)) 
				config.setOpenMode(IndexWriterConfig.OpenMode.APPEND);			
			config.setRAMBufferSizeMB(BUFFERRAMSIZE);
			indexer = new Indexer(config, index);
		}
		catch (IOException e) {
			e.printStackTrace();
		}			
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


}
