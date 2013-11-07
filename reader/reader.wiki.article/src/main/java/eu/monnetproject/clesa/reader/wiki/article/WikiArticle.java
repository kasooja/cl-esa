package eu.monnetproject.clesa.reader.wiki.article;

import eu.monnetproject.clesa.core.lang.Language;



/**
 *  
 * @author kasooja 
 */

public class WikiArticle {

	private Language language;
	private String content = null;
	private String title = null;	
	
	
	public WikiArticle(String contennt, String title, Language language){
		this.content = contennt;
		this.title = title;
		this.language = language;
	}
	
	public String getContent() {
		return content;
	}
	
	public String getTitle() {
		return title;
	}
		
	public Language getLanguage() {
		return language;
	}
	
	public void setLanguage(Language language) {
		this.language = language;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public static void main(String[] args) {
		System.out.println("l");
	}
	
}
