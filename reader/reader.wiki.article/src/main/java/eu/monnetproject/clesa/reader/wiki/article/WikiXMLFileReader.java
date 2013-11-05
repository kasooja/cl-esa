package eu.monnetproject.clesa.reader.wiki.article;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.StringEscapeUtils;

import eu.monnetproject.clesa.core.lang.Language;
import eu.monnetproject.clesa.core.utils.BasicFileTools;



/**
 *  
 * @author kasooja 
 */

public class WikiXMLFileReader {

	private String xmlFilePath = null;	
	//private final String STRANGE_SEQ = "|||__|||";
	private Language language;

	public WikiXMLFileReader(String xmlFilePath, Language language){
		if(isXMLFile(xmlFilePath)) 		
			this.xmlFilePath = xmlFilePath;
		this.language = language;
	}

	private boolean isXMLFile(String xmlFilePath) {
		if(xmlFilePath.endsWith(".xml"))
			return true;
		System.err.println("File not found or File is not .xml file");		
		return false;	
	}

	public Iterator<WikiArticle> getWikiArticleIter() throws IOException{
		return new XMLFileWikiArticleIter(BasicFileTools.getBufferedReaderFile(xmlFilePath), language);
	}


	private class XMLFileWikiArticleIter implements Iterator<WikiArticle> {

		private BufferedReader reader;
		private WikiArticle article = null;
		private Language language;

		public XMLFileWikiArticleIter(BufferedReader reader, Language language) throws IOException {
			this.reader = reader;
			this.language = language;
			toNext();
		}

		public void toNext() throws IOException{
			tryNext();
			if(article!=null)
				while(article.getContent() == null){
					tryNext();
					if(article==null)
						break;
				}
		}
		public void tryNext() throws IOException {
			String line = null;
			String articleTitle = null;
			String articleContent = null;
			boolean article = false;
			while((line=reader.readLine())!=null) {	
				if((line.contains("<title>")) && (line.contains("</title>")))
					articleTitle = line.substring(line.indexOf("<title>") +"<title>".length(), line.indexOf("</title>"));				
				if(line.contains("<page>"))
					article = true;
				if(article)
					articleContent = articleContent +"\n"+line;				
				if(line.contains("</page>")){
					article = false;
					break;
				}
			}
			if(articleTitle !=null)
				this.article = new WikiArticle(cleanArticleContent(articleContent, articleTitle), articleTitle, language);
			else
				this.article = null;
		}

		@Override
		public boolean hasNext() {
			return article != null;
		}

		@Override
		public WikiArticle next() {
			if(article != null) {
				WikiArticle currentArticle = article ;
				try {
					toNext();				
				} catch(Exception x) {
					throw new RuntimeException(x);
				}
				return currentArticle;
			} else {
				throw new NoSuchElementException();
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}		

		// return true if title is a absolute article not any nameSpace type e.g. Category 
		private Boolean isNameSpace(String title){
			for(String key: WikiNamespaces.values){
				if(title.toLowerCase().contains(key.toLowerCase()))
					return true;
			}
			return false;
		}

		private String cleanArticleContent(String content, String title) {
			if(isNameSpace(title))
				return null;
			if(title.toLowerCase().contains("(disambiguation)"))
				return null;
			BufferedReader reader = new BufferedReader(new StringReader(content));
			StringBuilder doc = null;
			String s;
			try {
				while ((s = reader.readLine()) != null) {
					if (s.contains("<text")) 
						doc = new StringBuilder();
					else if (s.contains("</text>")) {
						if (doc != null) {
							String cleanWiki = cleanText(doc.toString());
							doc = null;
							// remove all redirect articles
							if(cleanWiki.toLowerCase().contains("#REDIRECT".toLowerCase()))
								return null;							
							return cleanWiki.trim();
						}
					}
					if (doc != null) 
						doc.append(s).append(System.getProperty("line.separator"));					
				}
			} catch (IOException e) {				
				e.printStackTrace();
			}

			return null;
		}

		private String cleanText(String s) {
			return StringEscapeUtils.unescapeHtml3(s.replaceAll("\\{\\{[^\\}]*\\}\\}", " ").replaceAll("[='\\[\\]\\|]", " ").replaceAll("^[\\*\\:#]+", " ").replaceAll("<[^>]*>", " "));
		}	
	}
	
	private static class WikiNamespaces {				
		private static String [] list = {"Media:","Special:","Talk:","User:","User_talk:","Wikipedia:","Wikipedia_talk:",
				"File:","File_talk:","MediaWiki:","MediaWiki_talk:","Template:","Template_talk:","Help:","Help_talk:",
				"Category:","Category_talk:","Portal:","Portal_talk:","Book:","Book_talk:"};
		
		public static HashSet<String> values = new HashSet<String>(Arrays.asList(list));
	}

}
