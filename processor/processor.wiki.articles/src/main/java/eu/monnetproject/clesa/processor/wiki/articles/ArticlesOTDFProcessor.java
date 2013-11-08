package eu.monnetproject.clesa.processor.wiki.articles;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;

import eu.monnetproject.clesa.core.OTDF.OTDFFile;
import eu.monnetproject.clesa.core.OTDF.OTDFXml;
import eu.monnetproject.clesa.core.OTDF.StringFeature;
import eu.monnetproject.clesa.core.lang.Language;
import eu.monnetproject.clesa.core.token.Token;
import eu.monnetproject.clesa.core.tokenizer.Tokenizer;
import eu.monnetproject.clesa.core.utils.TextNormalizer;
import eu.monnetproject.clesa.lucene.basic.Searcher;
import eu.monnetproject.clesa.processor.wiki.articles.TitleURIIndexer.TitleURILucDocCreator;
import eu.monnetproject.clesa.reader.wiki.article.WikiArticle;
import eu.monnetproject.clesa.reader.wiki.article.WikiXMLFileReader;


/**
 *  
 * @author kasooja 
 */

public class ArticlesOTDFProcessor {
	private static Properties config = new Properties();
	private Language language; 
	private String xmlPath; 
	private Searcher searcher;
	private String titleURIindexPathToRead; 		
	private Tokenizer tokenizer;
	private String wikiArticleXmlToRead;

	public enum Features {
		Title, URI_EN, Article, LanguageISOCode;	
	}

	public ArticlesOTDFProcessor() {
		loadConfig();
		setTokenizer(language);
		setTitleUriSearcher();
	}

	private void setTitleUriSearcher() {
		if(!titleURIindexPathToRead.trim().equals(""))
			searcher = new Searcher(titleURIindexPathToRead);	
	}

	private void setTokenizer(Language articleLang) {		
		tokenizer = TextNormalizer.getTokenizer(articleLang);				
	}

	public String tokenize(String input) {	
		List<Token> tokens = tokenizer.tokenize(input);
		return joinTokens(tokens);		
	}

	private String joinTokens(List<Token> list) {
		StringBuffer buffer = new StringBuffer();
		for(Token token : list) {
			buffer.append(token + " ");
		}
		return buffer.toString().trim();		
	}

	private String joinStrings(List<String> list) {
		StringBuffer buffer = new StringBuffer();
		for(String token : list) {
			buffer.append(token + " ");
		}
		return buffer.toString().trim();		
	}

	public String clean(String text){
		text = text.replaceAll("\\p{P}", " ");
		String[] split = text.replace("\n", " ").split(" ");
		List<String> output = new ArrayList<String>();
		for (int i=0;i<split.length;i++){
			String temp = split[i];
			if (!temp.equals("") && !temp.equals("\n") && !temp.equals("AND") && !temp.equals("NOT") &&!temp.equals("OR"))
				output.add(temp);
		}
		return joinStrings(output);
	}

	private void loadConfig(){
		try {
			config.load(new FileInputStream("load/eu.monnetproject.clesa.processor.wiki.articles.ArticlesOTDFProcessor.properties"));
			xmlPath = config.getProperty("OTDFXmlToWrite");
			language = Language.getByIso639_1(config.getProperty("articleLanguageISOCode"));
			wikiArticleXmlToRead = config.getProperty("wikiArticleXmlToRead");		
			titleURIindexPathToRead = config.getProperty("titleURIindexPathToRead");			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}

	private String searchUri(String title, Language language) {
		BooleanClause.Occur[] flags = {BooleanClause.Occur.MUST,
				BooleanClause.Occur.MUST};	
		String uri = null;
		String[] queryStrings = new String[2];
		String[] fields = new String[2];
		queryStrings[0] = title;
		queryStrings[1] = language.getIso639_1();
		fields[0] = TitleURILucDocCreator.Fields.Title.toString();
		fields[1] = TitleURILucDocCreator.Fields.LanguageISOCode.toString();		
		TopScoreDocCollector docCollector = searcher.multiFieldTermSearch(queryStrings, fields, flags, 10);
		ScoreDoc[] scoreDocs = docCollector.topDocs().scoreDocs;
		if(scoreDocs.length>0) {
			ScoreDoc scoreDoc = scoreDocs[0];
			Document document = searcher.getDocumentWithDocID(scoreDoc.doc);
			uri = document.get(TitleURILucDocCreator.Fields.URI_EN.toString());
		}
		return uri;
	}

	public void processToXml() {				
		WikiXMLFileReader reader = new WikiXMLFileReader(wikiArticleXmlToRead, language);
		OTDFXml xml = new OTDFXml(xmlPath);
		Iterator<WikiArticle> rdfIter;
		try {
			rdfIter = reader.getWikiArticleIter();
			int i = 0;
			int j = 0;
			while(rdfIter.hasNext()){				
				System.out.println("i = " + i + "  " + "j =  "  + (++j));
				WikiArticle wikiArticle = rdfIter.next();
				String content = wikiArticle.getContent();
				if((content == null) || (content.isEmpty()))
					continue;
				String title =  wikiArticle.getTitle();
				title = TextNormalizer.convertToUnicode(title);
				title = TextNormalizer.deAccent(title);
				//	replace with a regex
				title = title.replace(".", " ").trim();			
				title = TextNormalizer.deAccent(title);
		
				
				Language language = wikiArticle.getLanguage();
				String uri = null;
				try{
					uri = searchUri(title, language);
				} catch (Exception e){
					System.err.println("error in searching title and language:  " + title); 
					continue;	
				}				
				if(uri!=null) {
					content = TextNormalizer.convertToUnicode(content);
					content =  tokenize(content);
					content = clean(content).toLowerCase().trim();			
					OTDFFile file = new OTDFFile();		
					file.addFeature(new StringFeature(Features.LanguageISOCode.toString(), language.getIso639_1()));
					file.addFeature(new StringFeature(Features.Title.toString(), title));				
					file.addFeature(new StringFeature(Features.URI_EN.toString(), uri));
					file.addFeature(new StringFeature(Features.Article.toString(), content));				
					xml.addOTDF(file);
					System.out.println("i = " + ++i);
				}
			}					
			searcher.closeIndex();
			xml.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {		
		ArticlesOTDFProcessor processor = new ArticlesOTDFProcessor();
		processor.processToXml();
		//
		//		String wikiXMLFilePath = "C:/Users/kat/workspaces/Resources/WikiXML/eswiki-20120515-pages-articles.xml";
		//		WikiXMLFileReader reader = new WikiXMLFileReader(wikiXMLFilePath, Language.SPANISH);
		//		Iterator<WikiArticle> articleIter = reader.getWikiArticleIter();
		//		while(articleIter.hasNext()) {
		//			WikiArticle article = articleIter.next();
		//			System.out.println(article.getContent());
		//		}
	}



}

