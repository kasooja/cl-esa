package eu.monnetproject.clesa.processor.wiki.abstracts;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

import eu.monnetproject.clesa.core.OTDF.OTDFFile;
import eu.monnetproject.clesa.core.OTDF.OTDFXmlReader;
import eu.monnetproject.clesa.core.lang.Language;
import eu.monnetproject.clesa.core.utils.TextNormalizer;
import eu.monnetproject.clesa.lucene.basic.AnalyzerFactory;
import eu.monnetproject.clesa.lucene.basic.Indexer;

/**
 *  
 * @author kasooja 
 */

public class AbstractsOTDFIndexer {	
	private final double BUFFERRAMSIZE = 256.0;
	private static Properties config =  new Properties();
	private String indexDirPath;
	private Language language;
	private String OTDFXmlToRead;
	private Indexer indexer;
	
	public AbstractsOTDFIndexer(){
		loadConfig();
		openWriter(language);
	}

	public static Analyzer getAnalyzer(Language language){
		return AnalyzerFactory.getAnalyzer(language);
	}

	private void openWriter(Language language) {		
		try {
			IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36, getAnalyzer(language));
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

	private Directory getIndex(String indexPath) {
		Directory index = null;
		try {
			index = new SimpleFSDirectory(new File(indexPath));
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return index;
	}
	
	private void loadConfig() {
		try {
			config.load(new FileInputStream("load/eu.monnetproject.clesa.processor.wiki.abstracts.AbstractsOTDFIndexer.properties"));
			indexDirPath = config.getProperty("indexDirPathToWrite");
			language = Language.getByIso639_1(config.getProperty("LanguageISOCodeForIndexer"));
			OTDFXmlToRead = config.getProperty("OTDFXmlToRead");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}			
	
	public String normalize(String text) {
		text = TextNormalizer.deAccent(text);
		return text;
	}

	public void indexOTDFsInOTDFXml() {
		AbstractOTDFLucDocCreator lucDocCreator = new AbstractOTDFLucDocCreator();	
		OTDFXmlReader reader = new OTDFXmlReader(OTDFXmlToRead);	
		Iterator<OTDFFile> iterator = null;
		try {
			iterator = reader.getIterator();
		} catch (IOException e) {
			e.printStackTrace();
		}
		int i = 0;
		if(iterator!=null){
			while(iterator.hasNext()) {
				OTDFFile file = iterator.next();
				Language language = Language.getByIso639_1(file.getFeatureValue(AbstractsOTDFProcessor.Features.LanguageISOCode.toString()));
				String topicContent = file.getFeatureValue(AbstractsOTDFProcessor.Features.Abstract.toString());
				//initially normalized by the AbstractOTDFProcessor, here just gets deaccented for the indexer
				topicContent = normalize(topicContent);
				lucDocCreator.addTopicContentField(topicContent);
				lucDocCreator.addTopicField(file.getFeatureValue(AbstractsOTDFProcessor.Features.Title.toString()));
				lucDocCreator.addUriField(file.getFeatureValue(AbstractsOTDFProcessor.Features.URI_EN.toString()));			
				lucDocCreator.addLanguageField(language);			
				indexer.addDoc(lucDocCreator.getLucDoc());
				System.out.println(++i);
				lucDocCreator.reset();				
			}
		}
		reader.close();		
		indexer.closeIndexer();
	}	

	public static void main(String[] args) {	
		AbstractsOTDFIndexer indexer = new AbstractsOTDFIndexer();
		System.out.println("Indexing Started");		
		indexer.indexOTDFsInOTDFXml();
		System.out.println("Indexing Finished");
	}	

}
