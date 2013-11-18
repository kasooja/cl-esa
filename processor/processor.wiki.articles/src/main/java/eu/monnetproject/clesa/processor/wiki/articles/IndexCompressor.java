package eu.monnetproject.clesa.processor.wiki.articles;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
	
	
	public static void main(String[] args) {
		CompressedMultiLingualLucDocCreator docCreator = new CompressedMultiLingualLucDocCreator();
	
		Set<Language> languages = new HashSet<Language>();
		languages.add(Language.ENGLISH);
		languages.add(Language.SPANISH);
		languages.add(Language.GERMAN);
		languages.add(Language.DUTCH);
		languages.add(Language.FRENCH);
		languages.add(Language.PORTUGUESE);
		
		String indexDirPathToWrite = "/Users/kartik/Desktop/compSixth";
		
		createPerFieldAnalyzer(languages);		
		openWriter(indexDirPathToWrite);
		
		Reader reader = new Reader(getIndex("/Users/kartik/Desktop/sixth"));
		
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
	
	public static Analyzer getAnalyzer(Language language){
		return AnalyzerFactory.getAnalyzer(language);
	}

	
	public static void createPerFieldAnalyzer(Set<Language> languagesDone){
		Map<String, Analyzer> fieldAnalyzerMap = new HashMap<String, Analyzer>();
		for(Language language : languagesDone) {
			String topicContentFieldName = MultiLingualArticleOTDFLucDocCreator.Fields.getLanguageTopicContentField(language);
			Analyzer analyzer = getAnalyzer(language);
			if(analyzer!=null)
				fieldAnalyzerMap.put(topicContentFieldName, analyzer);
		}		
		analyzers = new PerFieldAnalyzerWrapper(new StandardAnalyzer(Version.LUCENE_36), fieldAnalyzerMap);
	}

	
	private static void openWriter(String indexDirPathToWrite) {		
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

	
	
	private static Directory getIndex(String indexPath) {
		Directory index = null;
		try {
			index = new SimpleFSDirectory(new File(indexPath));
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return index;
	}


}
