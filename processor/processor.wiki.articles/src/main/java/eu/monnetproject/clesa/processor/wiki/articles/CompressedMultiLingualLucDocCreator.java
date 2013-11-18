package eu.monnetproject.clesa.processor.wiki.articles;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import eu.monnetproject.clesa.core.lang.Language;

public class CompressedMultiLingualLucDocCreator {

	private Document multiLingualArticlesOTDFLucDoc = new Document();	
	
	public enum Fields {
		Topic, URI_EN;	
		private static String fieldNameTopicContent = "TopicContent";		
		public static String getLanguageTopicContentField(Language language) {
			return language.getIso639_1() + fieldNameTopicContent;
		}	
	}

	public void addUriField(String uri) {
		Field uriField = new Field(Fields.URI_EN.toString(), uri, Field.Store.YES, Field.Index.NOT_ANALYZED);
		multiLingualArticlesOTDFLucDoc.add(uriField);			
	}
	
	public void addLanguageTopicContentField(Language language, String topicContent) {		
		String fieldNameLanguageTopicContent = language.getIso639_1() + Fields.fieldNameTopicContent;
		Field languageTopicContentField = new Field(fieldNameLanguageTopicContent, topicContent, Field.Store.NO, Field.Index.ANALYZED);		
		multiLingualArticlesOTDFLucDoc.add(languageTopicContentField);					
	}
	
	
	public static Field getLanguageTopicContentField(Language language, String topicContent) {		
		String fieldNameLanguageTopicContent = language.getIso639_1() + Fields.fieldNameTopicContent;
		Field languageTopicContentField = new Field(fieldNameLanguageTopicContent, topicContent, Field.Store.NO, Field.Index.ANALYZED);
		return languageTopicContentField;
	}
	
	public void addTopic(String topic) {		
		Field topicField = new Field(Fields.Topic.toString(), topic.toLowerCase(), Field.Store.YES, Field.Index.NOT_ANALYZED);		
		multiLingualArticlesOTDFLucDoc.add(topicField);				
	}

	public Document getLucDoc(){
		return multiLingualArticlesOTDFLucDoc;
	}
	
	public void reset() {
		multiLingualArticlesOTDFLucDoc = new Document();	
	}

}
