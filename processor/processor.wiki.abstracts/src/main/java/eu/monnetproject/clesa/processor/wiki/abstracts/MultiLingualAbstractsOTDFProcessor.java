package eu.monnetproject.clesa.processor.wiki.abstracts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.SimpleFSDirectory;

import eu.monnetproject.clesa.core.OTDF.OTDFFile;
import eu.monnetproject.clesa.core.OTDF.OTDFXml;
import eu.monnetproject.clesa.core.OTDF.OTDFXmlReader;
import eu.monnetproject.clesa.core.OTDF.StringFeature;
import eu.monnetproject.clesa.core.lang.Language;
import eu.monnetproject.clesa.core.utils.TextNormalizer;
import eu.monnetproject.clesa.lucene.basic.Reader;
import eu.monnetproject.clesa.lucene.basic.Searcher;


/**
 *  
 * @author kasooja 
 */

public class MultiLingualAbstractsOTDFProcessor {

	private static Properties config = new Properties();	
	private Language language;
	private String englishOTDFIndexDirPathToRead;
	private Searcher englishIndexSearcher;
	private Searcher otherLanguageIndexSearcher;
	private Reader englishIndexReader;
	private String otherLanguageOTDFIndexDirPathToRead;
	private String multiLingualOTDFXmlToWrite;
	private String multiLingualOTDFXmlToRead;

	public enum Features {
		Title, URI_EN;	
		private static String featureNameAbstract = "Abstract";		
		public static String getFeatureNameAbstract(Language language) {
			return language.getIso639_1() + featureNameAbstract;
		}

		public static String getFeatureNameAbstract() {
			return featureNameAbstract;
		}

	}

	public MultiLingualAbstractsOTDFProcessor() {
		loadConfig();
	}

	private void loadConfig(){
		try {			
			config.load(new FileInputStream("load/eu.monnetproject.clesa.processor.wiki.abstracts.MultiLingualAbstractsOTDFProcessor.properties"));
			otherLanguageOTDFIndexDirPathToRead = config.getProperty("otherLanguageOTDFIndexDirPathToRead");
			englishOTDFIndexDirPathToRead = config.getProperty("englishOTDFIndexDirPathToRead");
			language = Language.getByIso639_1(config.getProperty("abstractLanguageISOCodeThisTime"));		
			multiLingualOTDFXmlToWrite = config.getProperty("multiLingualOTDFXmlToWrite");
			multiLingualOTDFXmlToRead = config.getProperty("multiLingualOTDFXmlToRead");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}

	private void setOtherLanguageIndexSearcher(){
		otherLanguageIndexSearcher = new Searcher(otherLanguageOTDFIndexDirPathToRead);		
	}

	private void setEnglishIndexSearcher(){
		englishIndexSearcher = new Searcher(englishOTDFIndexDirPathToRead);		
	}

	private void closeEnglishSearcher() {
		if(englishIndexSearcher != null ) 
			englishIndexSearcher.closeIndex();
	}

	private void closeOtherLanguageSearcher(){
		if(otherLanguageIndexSearcher != null ) 
			otherLanguageIndexSearcher.closeIndex();
	}

	private Directory getIndex(String indexPath) {
	Directory dir = null;
	Directory index = null;
	try {
		dir = new SimpleFSDirectory(new File(indexPath + 
				System.getProperty("file.separator")));	
		index = new RAMDirectory(dir);
		dir.close();
	} catch (IOException e) {
		e.printStackTrace();
	}		
	return index;
}
	
	private void initiateMultiLingualOTDFXmlWithEnglish(){
		englishIndexReader = new Reader(getIndex(englishOTDFIndexDirPathToRead));	
		OTDFXml xml = new OTDFXml(multiLingualOTDFXmlToWrite);
		for(int i=0; i<englishIndexReader.totalDocuments(); i++){
			System.out.println(i);			
			Document document = englishIndexReader.getDocumentWithDocId(i);
			String abstractTitle = document.get(AbstractOTDFLucDocCreator.Fields.Topic.toString());
			String abstractContent = TextNormalizer.convertToUnicode(document.get(AbstractOTDFLucDocCreator.Fields.TopicContent.toString()));
			String uri = document.get(AbstractOTDFLucDocCreator.Fields.URI_EN.toString());
			Language language =  Language.getByIso639_1(document.get(AbstractOTDFLucDocCreator.Fields.LanguageISOCode.toString()));
			OTDFFile file = new OTDFFile();
			file.addFeature(new StringFeature(Features.getFeatureNameAbstract(language), abstractContent));
			file.addFeature(new StringFeature(Features.Title.toString(), abstractTitle));
			file.addFeature(new StringFeature(Features.URI_EN.toString(), uri));
			xml.addOTDF(file);
		}		
		xml.close();
		englishIndexReader.closeIndex();
	}
	private void writeOtherLanguageInMultiLingualOTDFXml() {
		setOtherLanguageIndexSearcher();
		setEnglishIndexSearcher();		
		OTDFXmlReader reader = new OTDFXmlReader(multiLingualOTDFXmlToRead);
		OTDFXml xmlToWrite = new OTDFXml(multiLingualOTDFXmlToWrite);		
		Iterator<OTDFFile> iterator = null;
		try {
			iterator = reader.getIterator();
		} catch (IOException e) {
			e.printStackTrace();
		}
		int i = 0;
		if(iterator!=null){
			while(iterator.hasNext()) {
				System.out.println(++i);			
				OTDFFile readFile = iterator.next();			
				String uri = readFile.getFeatureValue(AbstractsOTDFProcessor.Features.URI_EN.toString());
				TopScoreDocCollector otherDocCollector = otherLanguageIndexSearcher.termQuerySearch(uri, 
						AbstractOTDFLucDocCreator.Fields.URI_EN.toString(), 10);
				TopDocs otherTopDocs = otherDocCollector.topDocs();
				TopScoreDocCollector englishDocCollector = englishIndexSearcher.termQuerySearch(uri, 
						AbstractOTDFLucDocCreator.Fields.URI_EN.toString(), 10);
				TopDocs englishTopDocs = englishDocCollector.topDocs();
				if(otherTopDocs.totalHits>0 && englishTopDocs.totalHits>0) {
					int otherDocId = otherTopDocs.scoreDocs[0].doc;
					int englishDocId = englishTopDocs.scoreDocs[0].doc;					
					Document otherSearchedDoc = otherLanguageIndexSearcher.getDocumentWithDocID(otherDocId);
					Document englishSearchedDoc = englishIndexSearcher.getDocumentWithDocID(englishDocId);
					String otherAbstractTitle = otherSearchedDoc.get(AbstractOTDFLucDocCreator.Fields.Topic.toString());
					String englishAbstractTitle = englishSearchedDoc.get(AbstractOTDFLucDocCreator.Fields.Topic.toString());
					String otherAbstractContent = otherSearchedDoc.get(AbstractOTDFLucDocCreator.Fields.TopicContent.toString());					
					if(otherAbstractTitle.equalsIgnoreCase(englishAbstractTitle))
						readFile.addFeature(new StringFeature(Features.getFeatureNameAbstract(language), TextNormalizer.convertToUnicode(otherAbstractContent)));						
				}
				xmlToWrite.addOTDF(readFile);				
			}
		}
		reader.close();
		xmlToWrite.close();
		closeEnglishSearcher();
		closeOtherLanguageSearcher();
	}

	public void processXml() {		
		if(language == Language.ENGLISH)
			initiateMultiLingualOTDFXmlWithEnglish();
		else 
			writeOtherLanguageInMultiLingualOTDFXml();	
	}	

	public static void main(String[] args) {
		MultiLingualAbstractsOTDFProcessor processor = new MultiLingualAbstractsOTDFProcessor();			
		processor.processXml();
	}

}