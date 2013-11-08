package eu.monnetproject.clesa.processor.wiki.articles;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

import eu.monnetproject.clesa.core.OTDF.OTDFFile;
import eu.monnetproject.clesa.core.OTDF.OTDFXmlReader;
import eu.monnetproject.clesa.core.lang.Language;
import eu.monnetproject.clesa.core.utils.TextNormalizer;
import eu.monnetproject.clesa.lucene.basic.AnalyzerFactory;
import eu.monnetproject.clesa.lucene.basic.Indexer;
import eu.monnetproject.clesa.lucene.basic.Searcher;


/**
 *  
 * @author kasooja 
 */

public class MultiLingualArticlesIndexer {	

	private final double BUFFERRAMSIZE = 512.0;
	private static Properties config =  new Properties();
	private String indexDirPathToRead;
	private String indexDirPathToWrite;	
	private Language otdfLanguage;
	private PerFieldAnalyzerWrapper analyzers;
	private String OTDFXmlFileToRead;
	private Indexer indexer;
	private Searcher searcher;

	public MultiLingualArticlesIndexer(){
		loadConfig();
		createPerFieldAnalyzer();
		openWriter();		
	}

	public static Analyzer getAnalyzer(Language language){
		return AnalyzerFactory.getAnalyzer(language);
	}

	public void createPerFieldAnalyzer(){
		Map<String, Analyzer> fieldAnalyzerMap = new HashMap<String, Analyzer>();
		String topicContentFieldName = MultiLingualArticleOTDFLucDocCreator.Fields.getLanguageTopicContentField(otdfLanguage);
		Analyzer analyzer = getAnalyzer(otdfLanguage);
		if(analyzer!=null)
			fieldAnalyzerMap.put(topicContentFieldName, analyzer);		
		analyzers = new PerFieldAnalyzerWrapper(new StandardAnalyzer(Version.LUCENE_36), fieldAnalyzerMap);
	}

	private void openReader() {		
		searcher = new Searcher(indexDirPathToRead);					
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

	private void loadConfig() {
		try {
			config.load(new FileInputStream("load/eu.monnetproject.clesa.processor.wiki.articles.MultiLingualArticlesIndexer.properties"));
			indexDirPathToRead = config.getProperty("indexDirPathToRead");			
			indexDirPathToWrite = config.getProperty("indexDirPathToWrite");
			OTDFXmlFileToRead = config.getProperty("OTDFXmlFileToRead");
			otdfLanguage = Language.getByIso639_1(config.getProperty("languageOTDF"));		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}			

	private void indexEngOTDFXml(){
		OTDFXmlReader reader = new OTDFXmlReader(OTDFXmlFileToRead);
		Iterator<OTDFFile> iterator = null;
		try {
			iterator = reader.getIterator();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(iterator != null){
			MultiLingualArticleOTDFLucDocCreator lucDocCreator = new MultiLingualArticleOTDFLucDocCreator();	
			int i = 0;
			while(iterator.hasNext()){
				OTDFFile file = iterator.next();				
				lucDocCreator.addUriField(file.getFeatureValue(ArticlesOTDFProcessor.Features.URI_EN.toString()));							
				lucDocCreator.addTopic(file.getFeatureValue(ArticlesOTDFProcessor.Features.Title.toString()));				
				lucDocCreator.addLanguageTopicContentField(otdfLanguage, file.getFeatureValue(ArticlesOTDFProcessor.Features.Article.toString()));
				indexer.addDoc(lucDocCreator.getLucDoc());
				System.out.println(++i);
				lucDocCreator.reset();												
			}
		}
		indexer.closeIndexer();
		reader.close();
	}

	private void indexXml() {
		if(otdfLanguage == Language.ENGLISH)
			indexEngOTDFXml();
		else {
			openReader();			
			OTDFXmlReader reader = new OTDFXmlReader(OTDFXmlFileToRead);
			Iterator<OTDFFile> iterator = null;
			try {
				iterator = reader.getIterator();
			} catch (IOException e) {
				e.printStackTrace();
			}
			int i = 0;			
			if(iterator != null){				
				while(iterator.hasNext()) {				
					System.out.println(++i);			
					OTDFFile readFile = iterator.next();			
					String uri = readFile.getFeatureValue(ArticlesOTDFProcessor.Features.URI_EN.toString());
					String content = readFile.getFeatureValue(ArticlesOTDFProcessor.Features.Article.toString());
					content = TextNormalizer.convertToUnicode(content);
					content = TextNormalizer.deAccent(content);				
					TopScoreDocCollector otherDocCollector = searcher.termQuerySearch(uri, 
							MultiLingualArticleOTDFLucDocCreator.Fields.URI_EN.toString(), 10);
					TopDocs topDocs = otherDocCollector.topDocs();
					if(topDocs.totalHits>0) {
						int docId = topDocs.scoreDocs[0].doc;
						Document searchedDoc = searcher.getDocumentWithDocID(docId);
						Field langTopicField = MultiLingualArticleOTDFLucDocCreator.getLanguageTopicContentField(otdfLanguage, content);
						searchedDoc.add(langTopicField);
						indexer.addDoc(searchedDoc);
					}
				}
			}
			indexer.closeIndexer();
			searcher.closeIndex();
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

	//	private Directory getIndex(String indexPath) {
	//		Directory dir = null;
	//		Directory index = null;
	//		try {
	//			dir = new SimpleFSDirectory(new File(indexPath + 
	//					System.getProperty("file.separator")));	
	//			index = new RAMDirectory(dir);
	//			dir.close();
	//		} catch (IOException e) {
	//			e.printStackTrace();
	//		}		
	//		return index;
	//	}


	public static void main(String[] args) {	
		MultiLingualArticlesIndexer indexer = new MultiLingualArticlesIndexer();
		System.out.println("Indexing Started");	
		indexer.indexXml();
		System.out.println("Indexing Finished");
	}

}
