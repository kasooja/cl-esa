package eu.monnetproject.clesa.processor.wiki.articles;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

import eu.monnetproject.clesa.core.lang.Language;
import eu.monnetproject.clesa.core.ontology.RDF;
import eu.monnetproject.clesa.core.utils.TextNormalizer;
import eu.monnetproject.clesa.lucene.basic.AnalyzerFactory;
import eu.monnetproject.clesa.lucene.basic.Indexer;
import eu.monnetproject.clesa.reader.rdf.nt.NTFileReader;


/**
 *  
 * @author kasooja 
 */

public class TitleURIIndexer {
	private static Properties config = new Properties();
	private String ntFilePath;
	private String indexPathToWrite;
	private Language language;
	private final double BUFFERRAMSIZE = 256.0;
	private Indexer indexer;

	public TitleURIIndexer(){
		loadConfig();
		openWriter(language);
	}

	private void loadConfig(){
		try {
			config.load(new FileInputStream("load/eu.monnetproject.clesa.processor.wiki.articles.TitleURIIndex.properties"));
			indexPathToWrite = config.getProperty("indexPathToWrite");
			ntFilePath = config.getProperty("DBpediaNTFilePathToRead");
			language = Language.getByIso639_1(config.getProperty("titleLanguageISOCode"));		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}			

	private void openWriter(Language language) {		
		try {

			IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36, AnalyzerFactory.getAnalyzer(language));
			config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
			Directory index = getIndex(indexPathToWrite);
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

	public void index() {
		NTFileReader reader = new NTFileReader(ntFilePath);
		TitleURILucDocCreator creator = new TitleURILucDocCreator();
		Iterator<RDF> rdfIter = null;
		int i = 0;
		try {
			rdfIter = reader.getRDFIter();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if(rdfIter!=null) {
			while(rdfIter.hasNext()) {
				System.out.println(++i);
				RDF rdf = rdfIter.next();
				String uri = rdf.getSub();
				String title = rdf.getObj();
				title = TextNormalizer.convertToUnicode(title);
				title = removeLanguageMark(title);
				title = title.replace("\"", " ");				
				//	replace with a regex
				title = title.replace(".", " ").trim();			
				title = TextNormalizer.deAccent(title);
				uri = getURI(uri);
				creator.addTitleField(title);
				creator.addUriField(uri);
				creator.addLanguageField(language);
				indexer.addDoc(creator.getLucDoc());
				creator.reset();
			}
		}		
		indexer.closeIndexer();		
	}

	private static String getURI(String line){
		String URI = Arrays.asList(line.split(">")).iterator().next();
		return URI.replace("<", "").trim();
	}

	private String removeLanguageMark(String text) {
		String regex = "(@\\w+)( *)";
		return text.replaceAll(regex, " ");		
	}

	public static void main(String[] args) {
		TitleURIIndexer indexer = new TitleURIIndexer();
		System.out.println("indexing start");
		indexer.index();
		System.out.println("indexing done");		
	}

	public static class TitleURILucDocCreator {

		private Document titleURILucDoc = new Document();	

		public enum Fields {
			//WikiTitle, URI_EN, Language
			Title, URI_EN, LanguageISOCode;	
		}

		public void addUriField(String uri) {
			Field uriField = new Field(Fields.URI_EN.toString(), uri, Field.Store.YES, Field.Index.NOT_ANALYZED);
			titleURILucDoc.add(uriField);			
		}

		public void addTitleField(String title) {
			Field titleField = new Field(Fields.Title.toString(), title, Field.Store.YES, Field.Index.NOT_ANALYZED);
			titleURILucDoc.add(titleField);			
		}	

		public void addLanguageField(Language language) {
			Field languageField = new Field(Fields.LanguageISOCode.toString(), language.getIso639_1(), Field.Store.YES, Field.Index.NOT_ANALYZED);
			titleURILucDoc.add(languageField);			
		}

		public Document getLucDoc() {		
			return titleURILucDoc;
		}

		public void reset(){
			titleURILucDoc = new Document();	
		}
	}

}
